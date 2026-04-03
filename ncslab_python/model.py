import os
import sys
from collections import defaultdict, deque
from fractions import Fraction
from math import gcd

import numpy as np

from .blocks.route import FromBlock, GotoBlock
from .blocks.sink import ScopeBlock
from .profile_counters import bump as _profile_bump
from .registry import create_block
from .signal_utils import infer_signal_shape, signal_add, signal_multiply


_DEBUG_ENABLED = os.environ.get("NCSLAB_PYTHON_DEBUG", "").strip().lower() in {
    "1",
    "true",
    "yes",
    "on",
}


def _debug_log(message):
    if _DEBUG_ENABLED:
        print(message, file=sys.stderr)


class SimulationModel:
    """Holds all blocks, connections, and runtime state."""

    def __init__(self, config, blocks_data, lines_data):
        self.start_time = self._parse_float(config.get("StartTime"), 0.0)
        self.stop_time = self._parse_float(config.get("StopTime"), 10.0)

        if self.stop_time <= self.start_time:
            self.stop_time = self.start_time + 10.0
        if self.stop_time > 1e6:
            self.stop_time = 1e6

        self.solver_name = self._normalize_solver_name(config.get("Solver", "ode45"))
        self.step_type = config.get("Step", "VariableStep")

        fixed_step = config.get("FixedStep", "auto")
        if fixed_step in ("auto", None):
            self.fixed_step = 0.01
            self.fixed_step_auto = True
        else:
            self.fixed_step = self._parse_float(fixed_step, 0.01)
            self.fixed_step_auto = False

        max_data_points = config.get("MaxDataPoints", 1000)
        try:
            self.max_data_points = max(2, int(max_data_points))
        except (ValueError, TypeError):
            self.max_data_points = 1000

        self.max_step = self._parse_float_with_auto(config.get("MaxStep"), self.fixed_step * 10.0)
        self.min_step = self._parse_float_with_auto(config.get("MinStep"), 1e-6)
        self.initial_step = self._parse_float_with_auto(config.get("InitialStep"), self.fixed_step)
        self.rel_tol = self._parse_float_with_auto(config.get("RelTol"), 1e-3)
        self.abs_tol = self._parse_float_with_auto(config.get("AbsTol"), 1e-6)
        self.algebraic_loop_tolerance = self._parse_float_with_auto(
            config.get("AlgebraicLoopTolerance"),
            1e-9,
        )
        self.algebraic_loop_relaxation = self._parse_float_with_auto(
            config.get("AlgebraicLoopRelaxation"),
            0.5,
        )
        self.algebraic_loop_relaxation = min(max(self.algebraic_loop_relaxation, 1e-3), 1.0)
        try:
            self.algebraic_loop_max_iterations = max(
                1,
                int(config.get("AlgebraicLoopMaxIterations", 50)),
            )
        except (ValueError, TypeError):
            self.algebraic_loop_max_iterations = 50

        self.blocks = {}
        self.block_order = []
        for block_data in blocks_data:
            block = create_block(block_data)
            self.blocks[block.block_name] = block
            self.block_order.append(block.block_name)

        self.block_order_index = {
            name: index for index, name in enumerate(self.block_order)
        }

        self.blocks_by_uuid = {}
        for block in self.blocks.values():
            if block.block_uuid and block.block_uuid != "null":
                self.blocks_by_uuid[block.block_uuid] = block

        raw_connections = []
        for line in lines_data:
            raw_connections.append((
                line.get("fromBlockName", ""),
                int(line.get("fromPortNo", 0)),
                line.get("toBlockName", ""),
                int(line.get("toPortNo", 0)),
                line.get("fromBlockUUID", ""),
                line.get("toBlockUUID", ""),
            ))

        all_ports = [port for (_, from_port, _, to_port, _, _) in raw_connections for port in (from_port, to_port)]
        is_one_based = bool(all_ports) and all(port >= 1 for port in all_ports)
        if is_one_based:
            _debug_log("Port numbering: detected 1-based, converting to 0-based")

        self.connections = []
        self.connections_by_to = defaultdict(list)
        for from_name, from_port, to_name, to_port, from_uuid, to_uuid in raw_connections:
            if is_one_based:
                from_port -= 1
                to_port -= 1

            resolved_from = from_name
            if from_name and from_name not in self.blocks and from_uuid:
                uuid_block = self.blocks_by_uuid.get(from_uuid)
                if uuid_block:
                    resolved_from = uuid_block.block_name
                    _debug_log(
                        f"Connection: resolved '{from_name}' by UUID -> '{resolved_from}'",
                    )

            resolved_to = to_name
            if to_name and to_name not in self.blocks and to_uuid:
                uuid_block = self.blocks_by_uuid.get(to_uuid)
                if uuid_block:
                    resolved_to = uuid_block.block_name
                    _debug_log(
                        f"Connection: resolved '{to_name}' by UUID -> '{resolved_to}'",
                    )

            if resolved_from and resolved_to:
                connection = (resolved_from, from_port, resolved_to, to_port)
                self.connections.append(connection)
                self.connections_by_to[resolved_to].append((resolved_from, from_port, to_port))
            else:
                print(
                    f"Warning: skipping connection {from_name}->{to_name} (unresolved)",
                    file=sys.stderr,
                )

        for block in self.blocks.values():
            block.initialize()
            block.input_sources = ()

        self._attach_goto_from_connections()

        for to_name, incoming in self.connections_by_to.items():
            block = self.blocks.get(to_name)
            if block is None:
                continue

            block.input_sources = tuple(
                (self.blocks[from_name], from_port, to_port)
                for from_name, from_port, to_port in incoming
                if from_name in self.blocks
            )

        default_discrete_step = self.fixed_step if self.fixed_step > 0 else 0.01
        for _ in range(max(1, len(self.blocks))):
            for block in self.blocks.values():
                if hasattr(block, "configure_timing"):
                    block.configure_timing(self.start_time, default_discrete_step)

        self.discrete_blocks = []
        for block in self.blocks.values():
            if getattr(block, "sample_time", None) is not None:
                self.discrete_blocks.append(block)

        self.has_discrete_blocks = bool(self.discrete_blocks)
        self.discrete_step = self._resolve_discrete_step(
            [block.sample_time for block in self.discrete_blocks],
            default_discrete_step,
        )

        self.observer_blocks = [
            self.blocks[name] for name in self.block_order if self.blocks[name].is_observer()
        ]
        self.execution_components = self._build_execution_components()
        self.active_blocks = [
            block
            for component in self.execution_components
            for block in component["blocks"]
        ]
        self.sorted_blocks = self.active_blocks + self.observer_blocks

        self.total_states = 0
        initial_states = []
        for block in self.active_blocks:
            block.state_offset = self.total_states
            self.total_states += block.num_states
            initial_states.extend(block.get_initial_states())
        self.initial_states = np.array(initial_states, dtype=float)

        _debug_log(
            f"Model: {len(self.blocks)} blocks, {len(self.connections)} connections, "
            f"{self.total_states} states, algebraic loops: {len(self.algebraic_loops)}"
        )
        if _DEBUG_ENABLED:
            for from_name, from_port, to_name, to_port in self.connections:
                _debug_log(
                    f"  Connection: {from_name}[{from_port}] -> {to_name}[{to_port}]"
                )
            for index, loop in enumerate(self.algebraic_loops, start=1):
                loop_text = " -> ".join(loop)
                _debug_log(f"  Algebraic loop {index}: {loop_text}")

    def _attach_goto_from_connections(self):
        goto_blocks = [block for block in self.blocks.values() if isinstance(block, GotoBlock)]
        from_blocks = [block for block in self.blocks.values() if isinstance(block, FromBlock)]

        for from_block in from_blocks:
            match = self._resolve_goto_match(from_block, goto_blocks)
            if match is None:
                print(
                    f"Warning: From block '{from_block.block_name}' has no matching Goto tag '{from_block.goto_tag}'",
                    file=sys.stderr,
                )
                continue

            connection = (match.block_name, 0, from_block.block_name, 0)
            self.connections.append(connection)
            self.connections_by_to[from_block.block_name].append((match.block_name, 0, 0))

    @staticmethod
    def _resolve_goto_match(from_block, goto_blocks):
        tag = (from_block.goto_tag or "").strip()
        if not tag:
            return None

        visibility = (from_block.tag_visibility or "scoped").strip().lower()
        same_path_matches = [
            block
            for block in goto_blocks
            if block.goto_tag == tag and block.block_path == from_block.block_path
        ]
        any_path_matches = [block for block in goto_blocks if block.goto_tag == tag]

        if visibility == "local":
            return same_path_matches[0] if same_path_matches else None
        if same_path_matches:
            return same_path_matches[0]
        if visibility in {"global", "scoped"}:
            return any_path_matches[0] if any_path_matches else None
        return any_path_matches[0] if any_path_matches else None

    @staticmethod
    def _parse_float(value, default):
        if value in (None, ""):
            return default
        try:
            return float(value)
        except (TypeError, ValueError):
            return default

    @staticmethod
    def _normalize_solver_name(value):
        solver = (value or "").strip()
        aliases = {
            "rk4": "ode4",
            "FixedStepAuto": "ode4",
        }
        return aliases.get(solver, solver or "ode45")

    @classmethod
    def _parse_float_with_auto(cls, value, auto_value):
        if value in ("auto", None, ""):
            return auto_value
        return cls._parse_float(value, auto_value)

    @staticmethod
    def _resolve_discrete_step(sample_times, default_step):
        positive_times = [float(value) for value in sample_times if value and value > 0.0]
        if not positive_times:
            return default_step if default_step > 0.0 else 0.01

        fractions = [
            Fraction(str(round(value, 12))).limit_denominator(1_000_000)
            for value in positive_times
        ]
        numerator = fractions[0].numerator
        denominator = fractions[0].denominator

        for fraction in fractions[1:]:
            numerator = gcd(numerator, fraction.numerator)
            denominator = denominator * fraction.denominator // gcd(denominator, fraction.denominator)

        resolved = float(Fraction(numerator, denominator))
        return resolved if resolved > 0.0 else (default_step if default_step > 0.0 else 0.01)

    def _build_execution_components(self):
        active_names = [name for name in self.block_order if not self.blocks[name].is_observer()]
        adjacency = {name: set() for name in active_names}

        for from_name, _, to_name, _ in self.connections:
            if from_name not in adjacency or to_name not in adjacency:
                continue
            if self.blocks[to_name].is_direct_feedthrough():
                adjacency[from_name].add(to_name)

        components_by_name = {}
        components = []
        for component_names in self._tarjan_scc(active_names, adjacency):
            ordered_names = sorted(component_names, key=self.block_order_index.get)
            component_id = len(components)
            has_self_loop = any(name in adjacency[name] for name in ordered_names)
            is_loop = has_self_loop or len(ordered_names) > 1
            component = {
                "id": component_id,
                "names": ordered_names,
                "blocks": [self.blocks[name] for name in ordered_names],
                "is_algebraic_loop": is_loop,
            }
            components.append(component)
            for name in ordered_names:
                components_by_name[name] = component_id

        component_adjacency = {component["id"]: set() for component in components}
        in_degree = {component["id"]: 0 for component in components}
        for from_name, dependents in adjacency.items():
            from_component = components_by_name[from_name]
            for to_name in dependents:
                to_component = components_by_name[to_name]
                if from_component == to_component:
                    continue
                if to_component not in component_adjacency[from_component]:
                    component_adjacency[from_component].add(to_component)
                    in_degree[to_component] += 1

        component_priority = {
            component["id"]: min(self.block_order_index[name] for name in component["names"])
            for component in components
        }

        ready = sorted(
            [component_id for component_id, degree in in_degree.items() if degree == 0],
            key=component_priority.get,
        )
        queue = deque(ready)
        ordered_components = []
        while queue:
            component_id = queue.popleft()
            ordered_components.append(components[component_id])
            next_components = sorted(
                component_adjacency[component_id],
                key=component_priority.get,
            )
            for dependent_id in next_components:
                in_degree[dependent_id] -= 1
                if in_degree[dependent_id] == 0:
                    queue.append(dependent_id)

        if len(ordered_components) < len(components):
            remaining = [
                component
                for component in components
                if component not in ordered_components
            ]
            remaining.sort(key=lambda item: component_priority[item["id"]])
            ordered_components.extend(remaining)

        self.algebraic_loops = [
            component["names"] for component in ordered_components if component["is_algebraic_loop"]
        ]
        return ordered_components

    def _tarjan_scc(self, node_names, adjacency):
        index = 0
        stack = []
        stack_members = set()
        indices = {}
        lowlinks = {}
        components = []

        def strong_connect(name):
            nonlocal index
            indices[name] = index
            lowlinks[name] = index
            index += 1
            stack.append(name)
            stack_members.add(name)

            for dependent in adjacency.get(name, ()):
                if dependent not in indices:
                    strong_connect(dependent)
                    lowlinks[name] = min(lowlinks[name], lowlinks[dependent])
                elif dependent in stack_members:
                    lowlinks[name] = min(lowlinks[name], indices[dependent])

            if lowlinks[name] == indices[name]:
                component = []
                while stack:
                    member = stack.pop()
                    stack_members.remove(member)
                    component.append(member)
                    if member == name:
                        break
                components.append(component)

        for name in node_names:
            if name not in indices:
                strong_connect(name)

        return components

    def _load_inputs_for_block(self, block):
        inputs = block.inputs
        inputs.clear()
        for from_block, from_port, to_port in block.input_sources:
            inputs[to_port] = from_block.outputs.get(from_port, 0.0)

    def _refresh_observer_inputs(self):
        for block in self.observer_blocks:
            self._load_inputs_for_block(block)

    def _signal_delta(self, left, right):
        left_height, left_width, left_flat = infer_signal_shape(left)
        right_height, right_width, right_flat = infer_signal_shape(right)
        size = max(left_height * left_width, right_height * right_width, 1)

        if len(left_flat) < size:
            left_flat = left_flat + [0.0] * (size - len(left_flat))
        if len(right_flat) < size:
            right_flat = right_flat + [0.0] * (size - len(right_flat))

        return max(abs(left_value - right_value) for left_value, right_value in zip(left_flat, right_flat))

    def _output_delta(self, previous_outputs, current_outputs):
        keys = set(previous_outputs.keys()) | set(current_outputs.keys())
        if not keys:
            return 0.0
        return max(
            self._signal_delta(previous_outputs.get(port, 0.0), current_outputs.get(port, 0.0))
            for port in keys
        )

    def _blend_outputs(self, previous_outputs, current_outputs):
        if self.algebraic_loop_relaxation >= 1.0:
            return current_outputs

        blended = {}
        keys = set(previous_outputs.keys()) | set(current_outputs.keys())
        for port in keys:
            old_value = previous_outputs.get(port, 0.0)
            new_value = current_outputs.get(port, 0.0)
            blended[port] = signal_add(
                signal_multiply(self.algebraic_loop_relaxation, new_value),
                signal_multiply(1.0 - self.algebraic_loop_relaxation, old_value),
            )
        return blended

    def _evaluate_component(self, component, t, states):
        for block in component["blocks"]:
            self._load_inputs_for_block(block)
            block.compute_output(t, states)

    def _solve_algebraic_loop(self, component, t, states):
        loop_names = component["names"]
        loop_text = " -> ".join(loop_names)

        for iteration in range(self.algebraic_loop_max_iterations):
            _profile_bump("algebraic_loop_iterations", 1)
            max_delta = 0.0
            for block in component["blocks"]:
                previous_outputs = dict(block.outputs)
                self._load_inputs_for_block(block)
                block.compute_output(t, states)
                block.outputs = self._blend_outputs(previous_outputs, dict(block.outputs))
                max_delta = max(max_delta, self._output_delta(previous_outputs, block.outputs))

            if max_delta <= self.algebraic_loop_tolerance:
                return

        raise RuntimeError(
            "Algebraic loop did not converge "
            f"after {self.algebraic_loop_max_iterations} iterations: {loop_text}"
        )

    def get_initial_states(self):
        return self.initial_states.copy()

    def propagate_signals(self, t, states, include_observers=True):
        _profile_bump("propagate_signals_calls", 1)
        for component in self.execution_components:
            if component["is_algebraic_loop"]:
                self._solve_algebraic_loop(component, t, states)
            else:
                self._evaluate_component(component, t, states)

        if not include_observers or not self.observer_blocks:
            return

        self._refresh_observer_inputs()
        for block in self.observer_blocks:
            block.compute_output(t, states)

    def compute_derivatives(self, t, states):
        _profile_bump("compute_derivatives_calls", 1)
        self.propagate_signals(t, states, include_observers=False)
        derivatives = np.empty(self.total_states, dtype=float)
        for block in self.active_blocks:
            if block.num_states > 0:
                block_derivative = block.compute_derivative(t, states)
                offset = block.state_offset
                derivatives[offset:offset + block.num_states] = block_derivative
        return derivatives

    def get_scopes(self):
        scopes = []
        for block in self.observer_blocks:
            if isinstance(block, ScopeBlock):
                scopes.append(block.get_scope_data(staircase=self.has_discrete_blocks))
        return scopes
