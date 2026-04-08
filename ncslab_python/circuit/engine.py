from collections import defaultdict

import numpy as np

from .solve import solve_linear_system
from .stamp import StampBuilder


def _normalize_block_type(value):
    text = str(value or "").strip().lower()
    for token in (" ", "-", "_", "\t"):
        text = text.replace(token, "")
    return text


class CircuitEngine:
    def __init__(self, model, elements):
        self.model = model
        self.elements = elements
        self.element_state = defaultdict(dict)
        self.last_node_voltage = {}
        self.last_branch_current = {}
        self.ground_node = self._select_ground_node()
        self._node_count = self._resolve_node_count()
        self.ps_converter_inputs = self._build_ps_converter_inputs()

    def _build_ps_converter_inputs(self):
        block_by_uuid = {}
        for block in self.model.blocks.values():
            if block.block_uuid:
                block_by_uuid[block.block_uuid] = block

        mapping = {}
        graph_data = getattr(self.model, "graph_data", {}) or {}
        for cell in graph_data.get("cells", []):
            if cell.get("type") != "standard.Link":
                continue
            source = cell.get("source", {})
            target = cell.get("target", {})
            source_id = source.get("id")
            target_id = target.get("id")
            if not source_id or not target_id:
                continue
            source_block = block_by_uuid.get(source_id)
            target_block = block_by_uuid.get(target_id)
            if source_block is None or target_block is None:
                continue
            source_type = _normalize_block_type(source_block.block_type)
            target_type = _normalize_block_type(target_block.block_type)
            if target_type in {"pssimulinkconverter", "pss"} and source_type in {"voltagesensor", "voltmeter", "currentsensor", "ammeter"}:
                mapping[target_block.block_name] = source_block.block_name
        return mapping

    def _select_ground_node(self):
        ground_nodes = []
        for element in self.elements:
            normalized = _normalize_block_type(element.block_type)
            if normalized in {"ground", "electricalreference"}:
                ground_nodes.extend([element.p_node, element.n_node])
        if ground_nodes:
            return min(ground_nodes)
        if not self.elements:
            return 0
        return min(min(element.p_node, element.n_node) for element in self.elements)

    def _resolve_node_count(self):
        max_node = 0
        for element in self.elements:
            max_node = max(max_node, element.p_node, element.n_node)
        return max_node + 1

    def _iterative_solve(self, t_value, step_size):
        self._refresh_control_overrides()
        voltage_elements = []
        nonlinear_present = False
        for element in self.elements:
            normalized = _normalize_block_type(element.block_type)
            if normalized in {"dcvoltagesource", "acvoltagesource", "controlledvoltagesource", "currentsensor", "ammeter", "opamp"}:
                voltage_elements.append(element)
            if normalized in {"circuitswitch", "diode", "igbt", "mosfet", "opamp"}:
                nonlinear_present = True

        max_iterations = max(1, int(getattr(self.model, "circuit_max_iterations", 25)))
        tolerance = max(1e-12, float(getattr(self.model, "circuit_tolerance", 1e-7)))
        solution = None
        previous_solution = None
        for _ in range(max_iterations if nonlinear_present else 1):
            builder = StampBuilder(node_count=max(0, self._node_count - 1), voltage_source_count=len(voltage_elements))
            source_seq = 0
            for element in self.elements:
                p_idx = builder.node_index(element.p_node, self.ground_node)
                n_idx = builder.node_index(element.n_node, self.ground_node)
                state = self.element_state[element.block_uuid]
                source_value = builder.stamp_element(element, p_idx, n_idx, t_value, step_size, state)
                if source_value is not None:
                    normalized = _normalize_block_type(element.block_type)
                    if normalized == "opamp":
                        source_value = self._compute_opamp_output_source(element, state, step_size)
                        _, _, out_node = self._opamp_terminal_nodes(element)
                        p_idx = builder.node_index(out_node, self.ground_node)
                        n_idx = None
                    source_idx = builder.voltage_source_index(source_seq)
                    builder.stamp_voltage_source(p_idx, n_idx, source_idx, source_value)
                    source_seq += 1
            matrix = builder.matrix
            rhs = builder.rhs
            regularization = max(0.0, float(getattr(self.model, "circuit_regularization", 1e-10)))
            if regularization > 0.0 and matrix.size > 0:
                matrix = matrix + np.eye(matrix.shape[0], dtype=float) * regularization
            solution = solve_linear_system(matrix, rhs)
            if previous_solution is not None:
                delta = np.max(np.abs(solution - previous_solution)) if len(solution) else 0.0
                if delta <= tolerance:
                    break
            previous_solution = solution
            self._update_guesses(solution, builder, voltage_elements)

        return solution, voltage_elements

    @staticmethod
    def _coerce_scalar(value, default=0.0):
        try:
            if isinstance(value, dict) and "value" in value:
                return CircuitEngine._coerce_scalar(value["value"], default)
            if isinstance(value, (list, tuple)):
                if not value:
                    return default
                return CircuitEngine._coerce_scalar(value[0], default)
            return float(value)
        except (TypeError, ValueError):
            return default

    def _read_block_control_value(self, block, default=0.0):
        if 0 in block.inputs:
            return self._coerce_scalar(block.inputs.get(0), default)
        if 1 in block.inputs:
            return self._coerce_scalar(block.inputs.get(1), default)
        if block.inputs:
            first_key = next(iter(block.inputs))
            return self._coerce_scalar(block.inputs.get(first_key), default)
        return default

    def _refresh_control_overrides(self):
        controlled_types = {
            "variableresistor",
            "variableinductor",
            "variablecapacitor",
            "controlledvoltagesource",
            "controlledcurrentsource",
            "circuitswitch",
        }
        for element in self.elements:
            normalized = _normalize_block_type(element.block_type)
            if normalized not in controlled_types:
                continue
            block = self.model.blocks.get(element.block_name)
            if block is None:
                continue
            # Pull latest signal-domain control into element state.
            self.model._load_inputs_for_block(block)
            self.element_state[element.block_uuid]["control_value"] = self._read_block_control_value(block, 0.0)

    def _update_guesses(self, solution, builder, voltage_elements):
        for element in self.elements:
            p_idx = builder.node_index(element.p_node, self.ground_node)
            n_idx = builder.node_index(element.n_node, self.ground_node)
            vp = float(solution[p_idx]) if p_idx is not None and len(solution) > p_idx else 0.0
            vn = float(solution[n_idx]) if n_idx is not None and len(solution) > n_idx else 0.0
            self.element_state[element.block_uuid]["v_guess"] = vp - vn
            normalized = _normalize_block_type(element.block_type)
            if normalized == "opamp":
                plus_node, minus_node, out_node = self._opamp_terminal_nodes(element)
                plus_idx = builder.node_index(plus_node, self.ground_node) if plus_node is not None else None
                minus_idx = builder.node_index(minus_node, self.ground_node) if minus_node is not None else None
                out_idx = builder.node_index(out_node, self.ground_node) if out_node is not None else None
                v_plus = float(solution[plus_idx]) if plus_idx is not None and len(solution) > plus_idx else 0.0
                v_minus = float(solution[minus_idx]) if minus_idx is not None and len(solution) > minus_idx else 0.0
                v_out = float(solution[out_idx]) if out_idx is not None and len(solution) > out_idx else 0.0
                self.element_state[element.block_uuid]["v_control"] = v_plus - v_minus
                self.element_state[element.block_uuid]["v_out_guess"] = v_out
        for seq, element in enumerate(voltage_elements):
            current_idx = builder.voltage_source_index(seq)
            current = float(solution[current_idx]) if len(solution) > current_idx else 0.0
            self.element_state[element.block_uuid]["i_guess"] = current

    def step(self, t_value, step_size):
        if not self.elements:
            return
        step_size = max(float(step_size or 0.0), 1e-6)
        solution, voltage_elements = self._iterative_solve(float(t_value), step_size)

        builder = StampBuilder(node_count=max(0, self._node_count - 1), voltage_source_count=len(voltage_elements))
        self.last_node_voltage = {self.ground_node: 0.0}
        for element in self.elements:
            p_idx = builder.node_index(element.p_node, self.ground_node)
            n_idx = builder.node_index(element.n_node, self.ground_node)
            vp = float(solution[p_idx]) if p_idx is not None and len(solution) > p_idx else 0.0
            vn = float(solution[n_idx]) if n_idx is not None and len(solution) > n_idx else 0.0
            self.last_node_voltage[element.p_node] = vp if element.p_node != self.ground_node else 0.0
            self.last_node_voltage[element.n_node] = vn if element.n_node != self.ground_node else 0.0
            self.element_state[element.block_uuid]["v_prev"] = vp - vn

            normalized = _normalize_block_type(element.block_type)
            if normalized in {"inductor", "variableinductor"}:
                conductance = self.element_state[element.block_uuid].get("g", 0.0)
                self.element_state[element.block_uuid]["i_prev"] = conductance * (vp - vn) + self.element_state[element.block_uuid].get("i_prev", 0.0)
            if normalized in {"seriesrlcbranch"}:
                state = self.element_state[element.block_uuid]
                branch_type = state.get("branch_type", "R")
                dt = max(step_size, 1e-9)
                current = state.get("g", 0.0) * (vp - vn) + state.get("i_offset", 0.0)
                g_denom = max(state.get("g_denom", 1.0), 1e-12)
                l_value = max(state.get("l", 1e-6), 1e-12)
                c_value = max(state.get("c", 1e-6), 1e-12)
                if branch_type == "R":
                    state["his"] = 0.0
                elif branch_type == "L":
                    state["his"] = state.get("his", 0.0) + dt / l_value * (vp - vn)
                elif branch_type == "C":
                    state["his"] = -state.get("his", 0.0) - (4.0 * c_value / dt) * (vp - vn)
                elif branch_type == "RL":
                    l_his = (2.0 * l_value / dt * (current - state.get("i_his", 0.0)) - state.get("l_his", 0.0))
                    i_his = current
                    his = state.get("his", 0.0) + (2.0 * l_his / g_denom)
                    state["l_his"] = l_his
                    state["i_his"] = i_his
                    state["his"] = his
                elif branch_type == "RC":
                    c_his = (dt / (2.0 * c_value) * (current + state.get("i_his", 0.0)) + state.get("c_his", 0.0))
                    i_his = current
                    his = -state.get("his", 0.0) + (-2.0 * c_his / g_denom)
                    state["c_his"] = c_his
                    state["i_his"] = i_his
                    state["his"] = his
                else:  # LC / RLC
                    l_his = (2.0 * l_value / dt * (current - state.get("i_his", 0.0)) - state.get("l_his", 0.0))
                    i_his = current
                    his = state.get("his", 0.0) + ((2.0 * l_his - dt * i_his / c_value) / g_denom)
                    state["l_his"] = l_his
                    state["i_his"] = i_his
                    state["his"] = his

        self.last_branch_current = {}
        for seq, element in enumerate(voltage_elements):
            current_idx = builder.voltage_source_index(seq)
            current = float(solution[current_idx]) if len(solution) > current_idx else 0.0
            self.last_branch_current[element.block_uuid] = current
            self.element_state[element.block_uuid]["i_prev"] = current

    @staticmethod
    def _pick_port_node(port_nodes, name_candidates):
        for name in name_candidates:
            node = port_nodes.get(name)
            if node is not None:
                return node
        return None

    def _opamp_terminal_nodes(self, element):
        port_nodes = {str(key).lower(): value for key, value in (element.port_nodes or {}).items()}
        plus_node = self._pick_port_node(port_nodes, ["elconn2", "lconn2", "plus", "pos", "p"])
        minus_node = self._pick_port_node(port_nodes, ["elconn1", "lconn1", "minus", "neg", "n"])
        out_node = self._pick_port_node(port_nodes, ["erconn1", "rconn1", "out", "output"])
        if out_node is None:
            out_node = element.p_node
        if plus_node is None:
            plus_node = element.n_node
        if minus_node is None:
            minus_node = self.ground_node
        return plus_node, minus_node, out_node

    @staticmethod
    def _param_first(params, keys, default):
        for key in keys:
            if key in params:
                raw = params.get(key)
                if isinstance(raw, dict):
                    raw = raw.get("value", default)
                try:
                    return float(raw)
                except (TypeError, ValueError):
                    return default
        return default

    def _compute_opamp_output_source(self, element, state, step_size):
        params = element.params or {}
        gain = self._param_first(params, ["Gain", "OpenLoopGain", "A"], 1e4)
        v_max = self._param_first(params, ["MaxOutput", "Vmax"], 15.0)
        v_min = self._param_first(params, ["MinOutput", "Vmin"], -15.0)
        control_filter = float(getattr(self.model, "circuit_opamp_control_filter", 0.2))
        tau = float(getattr(self.model, "circuit_opamp_time_constant", 0.2))
        relax = float(getattr(self.model, "circuit_opamp_relaxation", 0.2))

        v_control = float(state.get("v_control", 0.0))
        v_control_prev = float(state.get("v_control_filtered", v_control))
        v_control_filtered = control_filter * v_control + (1.0 - control_filter) * v_control_prev
        state["v_control_filtered"] = v_control_filtered

        target_output = max(v_min, min(v_max, gain * v_control_filtered))
        prev_out = float(state.get("v_out_guess", 0.0))
        dyn_alpha = max(1e-4, min(1.0, step_size / (tau + step_size)))
        blended_alpha = max(1e-4, min(1.0, dyn_alpha * relax))
        next_out = prev_out + blended_alpha * (target_output - prev_out)
        state["v_out_guess"] = next_out
        return next_out

    def read_voltage(self, element):
        vp = self.last_node_voltage.get(element.p_node, 0.0)
        vn = self.last_node_voltage.get(element.n_node, 0.0)
        return vp - vn

    def read_current(self, element):
        if element.block_uuid in self.last_branch_current:
            return self.last_branch_current[element.block_uuid]
        state = self.element_state[element.block_uuid]
        if "g" in state:
            return state.get("g", 0.0) * self.read_voltage(element) + state.get("i_offset", 0.0)
        resistance = self.element_state[element.block_uuid].get("active_resistance")
        if resistance:
            voltage = self.read_voltage(element)
            return voltage / resistance
        conductance = self.element_state[element.block_uuid].get("g")
        if conductance:
            return conductance * self.read_voltage(element)
        return 0.0

    def apply_to_blocks(self):
        blocks = self.model.blocks
        for element in self.elements:
            block = blocks.get(element.block_name)
            if block is None:
                continue
            normalized = _normalize_block_type(element.block_type)
            voltage = self.read_voltage(element)
            current = self.read_current(element)

            if normalized in {"voltagesensor", "voltmeter"}:
                block.use_network_measurement = True
                block.measured_value = voltage
            elif normalized in {"currentsensor", "ammeter"}:
                block.use_network_measurement = True
                block.measured_value = current
            elif normalized in {"pssimulinkconverter", "pss"}:
                block.use_network_measurement = True
                source_name = self.ps_converter_inputs.get(block.block_name)
                if source_name and source_name in blocks:
                    source_block = blocks[source_name]
                    block.measured_value = getattr(source_block, "measured_value", 0.0)
                else:
                    block.measured_value = voltage
            elif normalized in {"dcvoltagesource", "acvoltagesource", "controlledvoltagesource"}:
                block.voltage_value = voltage
            elif normalized in {"dccurrentsource", "accurrentsource", "controlledcurrentsource"}:
                block.current_value = current
            elif normalized in {"igbt", "mosfet", "diode", "circuitswitch", "opamp"}:
                block.voltage_value = voltage
                block.current_value = current
