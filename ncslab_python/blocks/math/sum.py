import numbers

from ..base import Block
from ...signal_utils import signal_add, signal_subtract


class SumBlock(Block):
    def initialize(self):
        signs_str = self._param_str("Inputs", "++")
        self.signs = []
        for char in signs_str:
            if char == "+":
                self.signs.append(1.0)
            elif char in ("-", "\u2212"):
                self.signs.append(-1.0)
            elif char == "|":
                continue
        if not self.signs:
            self.signs = [1.0, 1.0]

    def compute_output(self, t, states):
        del t, states

        scalar_result = 0.0
        scalar_mode = True
        cached_inputs = []
        for index, sign in enumerate(self.signs):
            value = self.inputs.get(index, 0.0)
            cached_inputs.append((sign, value))
            if isinstance(value, numbers.Real):
                scalar_result = scalar_result + float(value) if sign >= 0 else scalar_result - float(value)
            else:
                scalar_mode = False
                break

        if scalar_mode:
            self.outputs[0] = scalar_result
            return

        result = 0.0
        for sign, value in cached_inputs:
            if sign >= 0:
                result = signal_add(result, value)
            else:
                result = signal_subtract(result, value)

        for index in range(len(cached_inputs), len(self.signs)):
            value = self.inputs.get(index, 0.0)
            if self.signs[index] >= 0:
                result = signal_add(result, value)
            else:
                result = signal_subtract(result, value)
        self.outputs[0] = result
