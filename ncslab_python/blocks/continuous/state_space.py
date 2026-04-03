import json

import numpy as np

from ..base import Block
from ...signal_utils import evaluate_numeric_expression, infer_signal_shape, reshape_signal


class StateSpaceBlock(Block):
    """State-space block: dx/dt = Ax + Bu, y = Cx + Du."""

    def is_direct_feedthrough(self):
        return bool(np.any(np.abs(self.D_mat) > 1e-12))

    def initialize(self):
        self.A_mat = np.array(self._parse_matrix("A", [[0]]), dtype=float)
        self.B_mat = np.array(self._parse_matrix("B", [[1]]), dtype=float)
        self.C_mat = np.array(self._parse_matrix("C", [[1]]), dtype=float)
        self.D_mat = np.array(self._parse_matrix("D", [[0]]), dtype=float)

        self.num_states = self.A_mat.shape[0]
        # Linear CUDA batching expects fixed input/output widths.
        # We treat signals as flattened vectors of these dimensions.
        self.input_width = self.B_mat.shape[1] if self.B_mat.ndim == 2 else 1
        self.output_width = self.C_mat.shape[0] if self.C_mat.ndim == 2 else 1
        initial_raw = self.param_values.get("InitialCondition", [0.0] * self.num_states)
        self.x0 = self._to_float_list(initial_raw, self.num_states)

    def _parse_matrix(self, key, default):
        value = self.param_values.get(key, default)
        if isinstance(value, str):
            cleaned = value.strip()
            if cleaned.startswith("["):
                try:
                    return json.loads(cleaned)
                except json.JSONDecodeError:
                    pass
            rows = cleaned.strip("[]").split(";")
            return [[evaluate_numeric_expression(item) for item in row.split()] for row in rows]
        if isinstance(value, (list, tuple)):
            return value
        return default

    def _to_float_list(self, value, length):
        if isinstance(value, (list, tuple)):
            return [evaluate_numeric_expression(item) for item in value][:length]
        if isinstance(value, str):
            parts = value.strip("[]").replace(",", " ").split()
            return [evaluate_numeric_expression(part) for part in parts][:length]
        return [evaluate_numeric_expression(value)] * length

    def get_initial_states(self):
        return self.x0

    def _to_numpy_signal(self, value):
        height, width, flat = infer_signal_shape(value)
        if height == 1 and width == 1:
            return np.array([flat[0]], dtype=float)
        if width == 1:
            return np.array(flat, dtype=float)
        return np.array(reshape_signal(height, width, flat), dtype=float)

    def _from_numpy_signal(self, value):
        array = np.asarray(value, dtype=float)
        if array.ndim == 0:
            return float(array)
        if array.ndim == 1:
            return reshape_signal(array.shape[0], 1, array.tolist())
        return reshape_signal(array.shape[0], array.shape[1], array.flatten().tolist())

    def compute_output(self, t, states):
        x = np.array(states[self.state_offset:self.state_offset + self.num_states])
        input_value = self.inputs.get(0, 0.0)
        u = self._to_numpy_signal(input_value)
        y = self.C_mat @ x + self.D_mat @ u
        self.outputs[0] = self._from_numpy_signal(y)

    def compute_derivative(self, t, states):
        x = np.array(states[self.state_offset:self.state_offset + self.num_states])
        input_value = self.inputs.get(0, 0.0)
        u = self._to_numpy_signal(input_value)
        dx = self.A_mat @ x + self.B_mat @ u
        return np.asarray(dx, dtype=float).reshape(-1).tolist()

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
        if int(self.output_width) <= 1:
            self.outputs[0] = float(vec[0]) if vec.size else 0.0
            return
        # For linear CUDA batching we only represent outputs as a vector (height=output_width, width=1),
        # which matches `compute_output()`'s `_from_numpy_signal()` for 1D numpy arrays.
        self.outputs[0] = vec[: int(self.output_width)].astype(float).tolist()
