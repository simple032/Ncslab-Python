from ..base import Block
from ...signal_utils import infer_signal_shape, parse_signal_parameter, reshape_signal, scalar_of


def _resolve_target_shape(*values):
    for value in values:
        height, width, _ = infer_signal_shape(value)
        if height * width > 1:
            return height, width
    return 1, 1


def _broadcast_flat(value, target_height, target_width):
    height, width, flat = infer_signal_shape(value)
    target_total = max(1, target_height * target_width)
    source_total = max(1, height * width)

    if source_total == target_total:
        return flat[:target_total]
    if source_total == 1:
        return [flat[0]] * target_total
    return [scalar_of(value)] * target_total


class SaturationBlock(Block):
    def initialize(self):
        self.upper_limit = parse_signal_parameter(
            self.param_values.get(
                "UpperLimit",
                self.param_values.get("UpperSaturationLimit", 1.0),
            )
        )
        self.lower_limit = parse_signal_parameter(
            self.param_values.get(
                "LowerLimit",
                self.param_values.get("LowerSaturationLimit", -1.0),
            )
        )

    def compute_output(self, t, states):
        del t, states

        input_value = self.inputs.get(0, 0.0)
        target_height, target_width = _resolve_target_shape(
            input_value,
            self.lower_limit,
            self.upper_limit,
        )
        input_flat = _broadcast_flat(input_value, target_height, target_width)
        lower_flat = _broadcast_flat(self.lower_limit, target_height, target_width)
        upper_flat = _broadcast_flat(self.upper_limit, target_height, target_width)

        result = []
        for value, lower_limit, upper_limit in zip(input_flat, lower_flat, upper_flat):
            if value < lower_limit:
                result.append(lower_limit)
            elif value > upper_limit:
                result.append(upper_limit)
            else:
                result.append(value)

        self.outputs[0] = reshape_signal(target_height, target_width, result)
