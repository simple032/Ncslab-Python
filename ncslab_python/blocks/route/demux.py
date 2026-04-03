from ..base import Block
from ...signal_utils import flatten_signal_elements


class DemuxBlock(Block):
    def initialize(self):
        self.num_outputs = max(2, int(self._param("Outputs", 2)))

    def compute_output(self, t, states):
        value = self.inputs.get(0, 0.0)
        flattened = flatten_signal_elements(value)
        if len(flattened) > 1:
            for index in range(min(len(flattened), self.num_outputs)):
                self.outputs[index] = flattened[index]
        else:
            self.outputs[0] = flattened[0] if flattened else 0.0
