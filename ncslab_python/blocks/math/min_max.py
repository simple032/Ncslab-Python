from ..base import Block
from ...signal_utils import signal_max, signal_min


class MinMaxBlock(Block):
    def initialize(self):
        self.function = self._param_str("Function", "min").strip().lower()
        self.num_inputs = max(2, int(self._param("NumInputs", 2)))

    def compute_output(self, t, states):
        values = [self.inputs.get(index, 0.0) for index in range(self.num_inputs)]
        reducer = signal_min if self.function == "min" else signal_max
        result = values[0] if values else 0.0
        for value in values[1:]:
            result = reducer(result, value)
        self.outputs[0] = result
