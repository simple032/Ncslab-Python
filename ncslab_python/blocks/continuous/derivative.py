import numbers

import numpy as np

from ..base import Block
from ...signal_utils import (
    infer_signal_shape,
    parse_signal_parameter,
    reshape_signal,
    signal_divide,
    signal_subtract,
)


class DerivativeBlock(Block):
    """Filtered derivative approximation."""

    def initialize(self):
        filter_coefficient = self._param("FilterCoefficient", 100.0)
        if "TimeConstant" in self.param_values:
            self.tau = max(self._param("TimeConstant", 1e-3), 1e-6)
        else:
            self.tau = 1.0 / max(filter_coefficient, 1e-6)

        self.initial_condition = parse_signal_parameter(self.param_values.get("InitialCondition", 0.0))
        self.state_height, self.state_width, self.initial_state_flat = infer_signal_shape(self.initial_condition)
        self.num_states = len(self.initial_state_flat)

        # CUDA linear batching interface:
        # Internal filtered state x evolves as: dx/dt = (u - x) / tau
        # Output y equals the same expression: y = (u - x) / tau
        # So we can represent it as:
        #   dx = A x + B u,  y = C x + D u
        #
        # We standardize input/output widths to `num_states` and rely on get_input_vector()
        # to broadcast scalar inputs when needed.
        self.input_width = int(self.num_states)
        self.output_width = int(self.num_states)

        a = -1.0 / float(self.tau)
        b = 1.0 / float(self.tau)
        self.A_mat = np.eye(self.num_states, dtype=float) * a
        self.B_mat = np.eye(self.num_states, dtype=float) * b
        self.C_mat = np.eye(self.num_states, dtype=float) * a
        self.D_mat = np.eye(self.num_states, dtype=float) * b

        self.show_state_port = self._param_str("ShowStatePort", "off").strip().lower() == "on"
        self.scalar_state = self.num_states == 1 and isinstance(self.initial_condition, numbers.Real)

    def get_initial_states(self):
        return self.initial_state_flat

    def _state_signal(self, states):
        return reshape_signal(
            self.state_height,
            self.state_width,
            list(states[self.state_offset:self.state_offset + self.num_states]),
        )

    def _filtered_derivative(self, states):
        input_signal = self.inputs.get(0, 0.0)
        if self.scalar_state and isinstance(input_signal, numbers.Real):
            state_signal = float(states[self.state_offset])
            output_signal = (float(input_signal) - state_signal) / self.tau
            return state_signal, output_signal

        state_signal = self._state_signal(states)
        output_signal = signal_divide(signal_subtract(input_signal, state_signal), self.tau)
        return state_signal, output_signal

    def compute_output(self, t, states):
        del t

        state_signal, output_signal = self._filtered_derivative(states)
        self.outputs[0] = output_signal
        if self.show_state_port:
            self.outputs[1] = state_signal

    def compute_derivative(self, t, states):
        del t

        _, derivative_signal = self._filtered_derivative(states)
        _, _, flat = infer_signal_shape(derivative_signal)
        return flat

    # --- CUDA batching interface (acceleration.py expects these attributes/methods) ---
    def get_input_vector(self):
        """Return flattened input vector of fixed length `input_width`."""
        value = self.inputs.get(0, 0.0)
        _h, _w, flat = infer_signal_shape(value)
        if not flat:
            vec = [0.0] * int(self.input_width)
        elif len(flat) == 1 and int(self.input_width) > 1:
            vec = [float(flat[0])] * int(self.input_width)
        else:
            vec = [float(x) for x in flat[: int(self.input_width)]]
            if len(vec) < int(self.input_width):
                vec.extend([0.0] * (int(self.input_width) - len(vec)))
        return np.asarray(vec, dtype=np.float64)

    def set_output_vector(self, output_vector):
        """Set `outputs[0]` from a flattened output vector."""
        vec = np.asarray(output_vector, dtype=np.float64).reshape(-1)
        if vec.size == 0:
            vec = np.zeros(int(self.output_width), dtype=np.float64)
        vec = vec[: int(self.output_width)]
        if vec.size < int(self.output_width):
            vec = np.pad(vec, (0, int(self.output_width) - vec.size), mode="constant")

        if int(self.output_width) <= 1 and self.scalar_state:
            self.outputs[0] = float(vec[0])
        else:
            self.outputs[0] = reshape_signal(self.state_height, self.state_width, vec.tolist())
