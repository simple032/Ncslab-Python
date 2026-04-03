from ..base import Block
from ...signal_utils import (
    infer_signal_shape,
    parse_signal_parameter,
    reshape_signal,
    scalar_of,
)


def _broadcast_flat(value, target_height, target_width):
    height, width, flat = infer_signal_shape(value)
    target_total = max(1, target_height * target_width)
    source_total = max(1, height * width)

    if source_total == target_total:
        return flat[:target_total]
    if source_total == 1:
        return [flat[0]] * target_total
    return [scalar_of(value)] * target_total


class CoulombBlock(Block):
    def initialize(self):
        self.offset = parse_signal_parameter(self.param_values.get("Offset", 0.0))
        self.gain = parse_signal_parameter(self.param_values.get("Gain", 1.0))

    def compute_output(self, t, states):
        del t, states

        input_value = self.inputs.get(0, 0.0)
        input_height, input_width, input_flat = infer_signal_shape(input_value)
        gain_flat = _broadcast_flat(self.gain, input_height, input_width)
        offset_flat = _broadcast_flat(self.offset, input_height, input_width)

        result = []
        for value, gain_value, offset_value in zip(input_flat, gain_flat, offset_flat):
            magnitude = gain_value * abs(value) + offset_value
            if value > 0.0:
                result.append(magnitude)
            elif value < 0.0:
                result.append(-magnitude)
            else:
                result.append(0.0)

        self.outputs[0] = reshape_signal(input_height, input_width, result)
