from copy import deepcopy

from ..base import Block
from ...signal_utils import infer_signal_shape, normalize_signal_value, parse_signal_parameter, reshape_signal, scalar_of


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


class BacklashBlock(Block):
    def initialize(self):
        self.backlash_width = parse_signal_parameter(self.param_values.get("BacklashWidth", 0.5))
        self.initial_output = parse_signal_parameter(self.param_values.get("InitialOutput", 0.0))
        self.current_output = self.clone_signal(self.initial_output)
        self.last_update_time = None

    def compute_output(self, t, states):
        del states

        input_value = deepcopy(normalize_signal_value(self.inputs.get(0, 0.0)))
        target_height, target_width = _resolve_target_shape(
            input_value,
            self.current_output,
            self.backlash_width,
        )
        input_flat = _broadcast_flat(input_value, target_height, target_width)
        previous_flat = _broadcast_flat(self.current_output, target_height, target_width)
        width_flat = _broadcast_flat(self.backlash_width, target_height, target_width)

        result = []
        for value, previous, width in zip(input_flat, previous_flat, width_flat):
            width = abs(width)
            if value <= previous - width:
                result.append(value + width)
            elif value >= previous + width:
                result.append(value - width)
            else:
                result.append(previous)

        self.current_output = reshape_signal(target_height, target_width, result)
        self.last_update_time = float(t)
        self.outputs[0] = self.clone_signal(self.current_output)

    def clone_signal(self, value):
        return deepcopy(normalize_signal_value(value))
