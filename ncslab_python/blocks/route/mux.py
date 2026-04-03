from ..base import Block
from ...signal_utils import flatten_signal_elements


class MuxBlock(Block):
    def initialize(self):
        self.num_inputs = max(2, int(self._param("Inputs", 2)))

    def compute_output(self, t, states):
        mux_values = []
        for index in range(self.num_inputs):
            mux_values.extend(flatten_signal_elements(self.inputs.get(index, 0.0)))

        if len(mux_values) <= 1:
            self.outputs[0] = mux_values[0] if mux_values else 0.0
        else:
            self.outputs[0] = mux_values
