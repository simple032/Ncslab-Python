from ..base import Block
from ...signal_utils import normalize_signal_value


class TestPointBlock(Block):
    def initialize(self):
        self.outputs[0] = 0.0

    def compute_output(self, t, states):
        del t, states
        self.outputs[0] = normalize_signal_value(self.inputs.get(0, 0.0))
