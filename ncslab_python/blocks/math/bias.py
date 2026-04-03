from ..base import Block
from ...signal_utils import parse_signal_parameter, signal_add


class BiasBlock(Block):
    def initialize(self):
        self.bias = parse_signal_parameter(self.param_values.get("Bias", 0.0))

    def compute_output(self, t, states):
        self.outputs[0] = signal_add(self.inputs.get(0, 0.0), self.bias)
