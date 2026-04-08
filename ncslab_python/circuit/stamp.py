import math

import numpy as np


def _normalize_block_type(value):
    text = str(value or "").strip().lower()
    for token in (" ", "-", "_", "\t"):
        text = text.replace(token, "")
    return text


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


def _param_text_first(params, keys, default=""):
    for key in keys:
        if key in params:
            raw = params.get(key)
            if isinstance(raw, dict):
                raw = raw.get("value", default)
            return str(raw)
    return default


def _stamp_conductance(matrix, i_rhs, p_idx, n_idx, conductance, i_equiv=0.0):
    del i_rhs
    if p_idx is not None:
        matrix[p_idx, p_idx] += conductance
    if n_idx is not None:
        matrix[n_idx, n_idx] += conductance
    if p_idx is not None and n_idx is not None:
        matrix[p_idx, n_idx] -= conductance
        matrix[n_idx, p_idx] -= conductance


def _stamp_current_rhs(rhs, p_idx, n_idx, current_value):
    if p_idx is not None:
        rhs[p_idx] -= current_value
    if n_idx is not None:
        rhs[n_idx] += current_value


class StampBuilder:
    def __init__(self, node_count, voltage_source_count):
        total_unknowns = node_count + voltage_source_count
        self.matrix = np.zeros((total_unknowns, total_unknowns), dtype=float)
        self.rhs = np.zeros((total_unknowns,), dtype=float)
        self.node_count = node_count

    def node_index(self, node_id, ground_node):
        if node_id == ground_node:
            return None
        return node_id - 1 if node_id > ground_node else node_id

    def voltage_source_index(self, source_seq):
        return self.node_count + source_seq

    def stamp_voltage_source(self, p_idx, n_idx, source_idx, voltage):
        if p_idx is not None:
            self.matrix[p_idx, source_idx] += 1.0
            self.matrix[source_idx, p_idx] += 1.0
        if n_idx is not None:
            self.matrix[n_idx, source_idx] -= 1.0
            self.matrix[source_idx, n_idx] -= 1.0
        self.rhs[source_idx] += voltage

    def stamp_element(self, element, p_idx, n_idx, t_value, step_size, element_state):
        block_type = _normalize_block_type(element.block_type)
        params = element.params

        if block_type in {"ground", "electricalreference"}:
            return None

        if block_type in {"resistor", "variableresistor"}:
            resistance = _param_first(params, ["R", "Resistance", "resistance"], 1.0)
            if block_type == "variableresistor":
                gain = _param_first(params, ["Gain", "ControlGain", "K"], 1.0)
                offset = _param_first(params, ["Offset", "Bias"], 0.0)
                control = element_state.get("control_value")
                if control is not None:
                    resistance = gain * float(control) + offset
                r_min = _param_first(params, ["Rmin", "MinResistance"], 1e-6)
                r_max = _param_first(params, ["Rmax", "MaxResistance"], 1e9)
                resistance = min(max(resistance, r_min), r_max)
            resistance = max(abs(resistance), 1e-9)
            conductance = 1.0 / resistance
            _stamp_conductance(self.matrix, self.rhs, p_idx, n_idx, conductance)
            element_state["g"] = conductance
            element_state["i_offset"] = 0.0
            return None

        if block_type in {"capacitor", "variablecapacitor"}:
            capacitance = _param_first(params, ["C", "Capacitance", "capacitance"], 1e-6)
            capacitance = max(abs(capacitance), 1e-12)
            dt = max(step_size, 1e-6)
            conductance = capacitance / dt
            v_prev = element_state.get("v_prev", 0.0)
            _stamp_conductance(self.matrix, self.rhs, p_idx, n_idx, conductance)
            _stamp_current_rhs(self.rhs, p_idx, n_idx, -conductance * v_prev)
            element_state["g"] = conductance
            element_state["i_offset"] = -conductance * v_prev
            return None

        if block_type in {"inductor", "variableinductor"}:
            inductance = _param_first(params, ["L", "Inductance", "inductance"], 1e-6)
            if block_type == "variableinductor":
                control = element_state.get("control_value")
                if control is not None:
                    inductance = float(control)
                l_min = _param_first(params, ["Lmin", "MinInductance"], 1e-9)
                l_max = _param_first(params, ["Lmax", "MaxInductance"], 1e3)
                inductance = min(max(inductance, l_min), l_max)
            inductance = max(abs(inductance), 1e-9)
            dt = max(step_size, 1e-6)
            conductance = dt / inductance
            i_prev = element_state.get("i_prev", 0.0)
            _stamp_conductance(self.matrix, self.rhs, p_idx, n_idx, conductance)
            _stamp_current_rhs(self.rhs, p_idx, n_idx, -i_prev)
            element_state["g"] = conductance
            element_state["i_offset"] = -i_prev
            return None

        if block_type in {"dccurrentsource", "controlledcurrentsource"}:
            current = _param_first(params, ["Current", "I", "i0", "Amplitude", "value", "Value"], 0.0)
            if block_type == "controlledcurrentsource":
                control = element_state.get("control_value", 0.0)
                gain = _param_first(params, ["Gain", "ControlGain", "K"], 1.0)
                bias = _param_first(params, ["Bias", "Offset"], 0.0)
                current = gain * float(control) + bias
            _stamp_current_rhs(self.rhs, p_idx, n_idx, current)
            return None

        if block_type == "accurrentsource":
            amplitude = _param_first(params, ["Amplitude", "Ipk", "I0", "i0"], 1.0)
            freq = _param_first(params, ["Frequency", "f", "Hz"], 50.0)
            phase = math.radians(_param_first(params, ["Phase", "phi"], 0.0))
            bias = _param_first(params, ["Bias", "Offset", "dc"], 0.0)
            current = bias + amplitude * math.sin(2.0 * math.pi * freq * t_value + phase)
            _stamp_current_rhs(self.rhs, p_idx, n_idx, current)
            return None

        if block_type in {"voltagesensor", "voltmeter"}:
            # Ideal voltage probe: infinite input impedance (open circuit).
            return None

        if block_type in {"currentsensor", "ammeter"}:
            # Ideal current probe: zero voltage drop branch.
            return 0.0

        if block_type in {"dcvoltagesource", "controlledvoltagesource"}:
            voltage = _param_first(params, ["Voltage", "V", "v0", "Amplitude", "value", "Value"], 0.0)
            if block_type == "controlledvoltagesource":
                control = element_state.get("control_value", 0.0)
                gain = _param_first(params, ["Gain", "ControlGain", "K"], 1.0)
                bias = _param_first(params, ["Bias", "Offset"], 0.0)
                voltage = gain * float(control) + bias
            return voltage

        if block_type == "acvoltagesource":
            amplitude = _param_first(params, ["Amplitude", "Vpk", "V0", "v0"], 1.0)
            freq = _param_first(params, ["Frequency", "f", "Hz"], 50.0)
            phase = math.radians(_param_first(params, ["Phase", "phi"], 0.0))
            bias = _param_first(params, ["Bias", "Offset", "dc"], 0.0)
            return bias + amplitude * math.sin(2.0 * math.pi * freq * t_value + phase)

        if block_type in {"circuitswitch", "diode", "igbt", "mosfet"}:
            # Piecewise-linear resistance for iterative convergence.
            on_resistance = max(abs(_param_first(params, ["Ron", "OnResistance", "R_on"], 1e-3)), 1e-6)
            off_resistance = max(abs(_param_first(params, ["Roff", "OffResistance", "R_off"], 1e6)), 1e3)
            threshold = _param_first(params, ["Threshold", "Vf", "Vth"], 0.5)
            v_guess = element_state.get("v_guess", 0.0)
            if block_type == "circuitswitch":
                v_guess = element_state.get("control_value", v_guess)
            resistance = on_resistance if v_guess >= threshold else off_resistance
            conductance = 1.0 / resistance
            _stamp_conductance(self.matrix, self.rhs, p_idx, n_idx, conductance)
            element_state["active_resistance"] = resistance
            element_state["g"] = conductance
            element_state["i_offset"] = 0.0
            return None

        if block_type == "opamp":
            gain = _param_first(params, ["Gain", "OpenLoopGain", "A"], 1e4)
            max_output = _param_first(params, ["MaxOutput", "Vmax"], 15.0)
            min_output = _param_first(params, ["MinOutput", "Vmin"], -15.0)
            v_control = element_state.get("v_control", 0.0)
            output = max(min_output, min(max_output, gain * v_control))
            return output

        if block_type in {"seriesrlcbranch"}:
            dt = max(step_size, 1e-9)
            r_value = max(abs(_param_first(params, ["R", "Resistance"], 1.0)), 1e-9)
            l_value = max(abs(_param_first(params, ["L", "Inductance"], 1e-6)), 1e-12)
            c_value = max(abs(_param_first(params, ["C", "Capacitance"], 1e-6)), 1e-12)
            branch_type = _param_text_first(params, ["Branchtype", "BranchType", "type"], "R")
            branch_type = branch_type.strip().upper()
            if branch_type not in {"R", "L", "C", "RL", "RC", "LC", "RLC"}:
                branch_type = "R"

            if branch_type == "RL":
                g_denom = (1.0 / r_value) + 2.0 * l_value / dt
            elif branch_type == "RC":
                g_denom = (1.0 / r_value) + dt / (2.0 * c_value)
            elif branch_type == "R":
                g_denom = 1.0 / r_value
            elif branch_type == "L":
                g_denom = 2.0 * l_value / dt
            elif branch_type == "C":
                g_denom = dt / (2.0 * c_value)
            elif branch_type == "LC":
                g_denom = 2.0 * l_value / dt + dt / (2.0 * c_value)
            else:  # RLC
                g_denom = (1.0 / r_value) + 2.0 * l_value / dt + dt / (2.0 * c_value)

            g_denom = max(g_denom, 1e-12)
            conductance = 1.0 / g_denom
            his_value = element_state.get("his", 0.0)
            if branch_type == "R":
                his_value = 0.0

            _stamp_conductance(self.matrix, self.rhs, p_idx, n_idx, conductance)
            _stamp_current_rhs(self.rhs, p_idx, n_idx, his_value)

            element_state["branch_type"] = branch_type
            element_state["r"] = r_value
            element_state["l"] = l_value
            element_state["c"] = c_value
            element_state["g_denom"] = g_denom
            element_state["g"] = conductance
            element_state["i_offset"] = his_value
            return None

        return None
