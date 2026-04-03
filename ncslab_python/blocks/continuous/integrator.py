import numpy as np

from ..base import Block
from ...signal_utils import infer_signal_shape, parse_signal_parameter, reshape_signal, signal_add


class IntegratorBlock(Block):
    def is_direct_feedthrough(self):
        return False

    def initialize(self):
        self.initial_condition = parse_signal_parameter(self.param_values.get("InitialCondition", 0.0))
        self.state_height, self.state_width, self.initial_state_flat = infer_signal_shape(self.initial_condition)
        self.num_states = len(self.initial_state_flat)
        # Linear CUDA batching expects fixed input/output widths and matrices.
        self.input_width = self.num_states
        self.output_width = self.num_states
        self.A_mat = np.zeros((self.num_states, self.num_states), dtype=float)
        self.B_mat = np.eye(self.num_states, dtype=float)
        self.C_mat = np.eye(self.num_states, dtype=float)
        # No direct feedthrough, but still provide D_mat for completeness.
        self.D_mat = np.zeros((self.num_states, self.num_states), dtype=float)

    def get_initial_states(self):
        return self.initial_state_flat

    def compute_output(self, t, states):
        state_values = states[self.state_offset:self.state_offset + self.num_states]
        self.outputs[0] = reshape_signal(self.state_height, self.state_width, list(state_values))

    def compute_derivative(self, t, states):
        zero_signal = reshape_signal(self.state_height, self.state_width, [0.0] * self.num_states)
        derivative_signal = signal_add(zero_signal, self.inputs.get(0, 0.0))
        _, _, flat = infer_signal_shape(derivative_signal)
        return flat

    # --- CUDA batching interface (acceleration.py expects these attributes/methods) ---
    def get_input_vector(self):
        """Return flattened input vector of fixed length `input_width`."""
        value = self.inputs.get(0, 0.0)
        _h, _w, flat = infer_signal_shape(value)
        flat = flat if flat else [0.0]
        if len(flat) == 1 and self.input_width > 1:
            vec = [float(flat[0])] * int(self.input_width)
        else:
            vec = [float(x) for x in flat[: int(self.input_width)]]
            if len(vec) < int(self.input_width):
                vec.extend([0.0] * (int(self.input_width) - len(vec)))
        return np.asarray(vec, dtype=np.float64)

    def set_output_vector(self, output_vector):
        """Set `outputs[0]` from a flattened output vector."""
        vec = np.asarray(output_vector, dtype=np.float64).reshape(-1)
        vec = vec[: int(self.output_width)] if vec.size else np.zeros(int(self.output_width), dtype=float)
        # Restore to the signal shape that the non-CUDA path would generate.
        self.outputs[0] = reshape_signal(self.state_height, self.state_width, list(vec))
