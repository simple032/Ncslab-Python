import numbers

from ..base import Block
from ...signal_utils import parse_signal_parameter, signal_matrix_multiply, signal_multiply


class GainBlock(Block):
    def initialize(self):
        self.gain = parse_signal_parameter(self.param_values.get("Gain", 1.0))
        self.multiplication = self._param_str("Multiplication", "Element-wise(K.*u)").lower()
        self.scalar_gain = isinstance(self.gain, numbers.Real)

    def compute_output(self, t, states):
        del t, states

        input_value = self.inputs.get(0, 0.0)
        if self.scalar_gain and isinstance(input_value, numbers.Real):
            self.outputs[0] = float(self.gain) * float(input_value)
        elif "matrix" in self.multiplication:
            self.outputs[0] = signal_matrix_multiply(self.gain, input_value)
        else:
            self.outputs[0] = signal_multiply(self.gain, input_value)
