import numbers

import numpy as np

from ..base import Block
from ...signal_utils import evaluate_numeric_expression, infer_signal_shape, scalar_of, signal_multiply


class TransferFcnBlock(Block):
    """Transfer function converted to controllable canonical state space."""

    def is_direct_feedthrough(self):
        if self.num_states == 0:
            return True
        return abs(self.D) > 1e-12

    def initialize(self):
        numerator_raw = self.param_values.get("Numerator", [1])
        denominator_raw = self.param_values.get("Denominator", [1, 1])
        self.num = self._to_float_list(numerator_raw)
        self.den = self._to_float_list(denominator_raw)

        self.order = len(self.den) - 1
        self.num_states = self.order

        if self.order == 0:
            self.static_gain = self.num[0] / self.den[0] if self.den[0] != 0 else 0.0
            self.num_states = 0
            return

        leading = self.den[0]
        self.den = [value / leading for value in self.den]
        self.num = [value / leading for value in self.num]

        while len(self.num) < len(self.den):
            self.num.insert(0, 0.0)

        self.A = np.zeros((self.order, self.order))
        for index in range(self.order - 1):
            self.A[index][index + 1] = 1.0
        for index in range(self.order):
            self.A[self.order - 1][index] = -self.den[self.order - index]

        self.B = np.zeros(self.order)
        self.B[self.order - 1] = 1.0

        self.C = np.zeros(self.order)
        for index in range(self.order):
            self.C[index] = self.num[self.order - index] - self.num[0] * self.den[self.order - index]

        self.D = self.num[0]
        # Linear CUDA batching expects A_mat/B_mat/C_mat/D_mat and fixed input/output widths.
        # We represent transfer function output as a scalar/vector of length 1.
        self.A_mat = self.A
        self.B_mat = np.asarray(self.B, dtype=float).reshape(self.order, 1)
        self.C_mat = np.asarray(self.C, dtype=float).reshape(1, self.order)
        self.D_mat = np.asarray([[float(self.D)]], dtype=float)
        self.input_width = 1
        self.output_width = 1

    def _to_float_list(self, value):
        if isinstance(value, (list, tuple)):
            return [evaluate_numeric_expression(item) for item in value]
        if isinstance(value, str):
            cleaned = value.strip()
            if cleaned.startswith("["):
                cleaned = cleaned.strip("[]")
            parts = cleaned.replace(",", " ").split()
            return [evaluate_numeric_expression(part) for part in parts]
        return [evaluate_numeric_expression(value)]

    def get_initial_states(self):
        return [0.0] * self.num_states

    def compute_output(self, t, states):
        del t

        input_value = self.inputs.get(0, 0.0)
        if self.num_states == 0:
            if isinstance(input_value, numbers.Real):
                self.outputs[0] = self.static_gain * float(input_value)
            else:
                self.outputs[0] = signal_multiply(self.static_gain, input_value)
            return

        x = states[self.state_offset:self.state_offset + self.order]
        u = scalar_of(input_value)
        self.outputs[0] = float(np.dot(self.C, x) + self.D * u)

    def compute_derivative(self, t, states):
        del t

        if self.num_states == 0:
            return []

        x = states[self.state_offset:self.state_offset + self.order]
        u = scalar_of(self.inputs.get(0, 0.0))
        dx = self.A @ x + self.B * u
        return dx.tolist()

    # --- CUDA batching interface (acceleration.py expects these attributes/methods) ---
    def get_input_vector(self):
        """Return flattened input vector of fixed length 1 (scalar input)."""
        value = self.inputs.get(0, 0.0)
        _h, _w, flat = infer_signal_shape(value)
        if not flat:
            return np.asarray([0.0], dtype=np.float64)
        return np.asarray([float(flat[0])], dtype=np.float64)

    def set_output_vector(self, output_vector):
        """Set `outputs[0]` from a flattened output vector of length 1."""
        vec = np.asarray(output_vector, dtype=np.float64).reshape(-1)
        self.outputs[0] = float(vec[0]) if vec.size else 0.0
