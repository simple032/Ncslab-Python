from ..base import Block


class DisplayBlock(Block):
    def initialize(self):
        self.last_value = 0.0

    def compute_output(self, t, states):
        self.last_value = self.inputs.get(0, 0.0)
        self.outputs[0] = self.last_value

