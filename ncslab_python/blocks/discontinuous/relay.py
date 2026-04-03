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


class RelayBlock(Block):
    def initialize(self):
        self.on_switch_value = parse_signal_parameter(
            self.param_values.get(
                "OnSwitchValue",
                self.param_values.get("SwitchOnPoint", 1.0),
            )
        )
        self.off_switch_value = parse_signal_parameter(
            self.param_values.get(
                "OffSwitchValue",
                self.param_values.get("SwitchOffPoint", 0.0),
            )
        )
        self.on_output_value = parse_signal_parameter(
            self.param_values.get(
                "OnOutputValue",
                self.param_values.get("OutputWhenOn", 1.0),
            )
        )
        self.off_output_value = parse_signal_parameter(
            self.param_values.get(
                "OffOutputValue",
                self.param_values.get("OutputWhenOff", 0.0),
            )
        )
        self.current_output = self.clone_signal(self.off_output_value)
        self.last_update_time = None

    def compute_output(self, t, states):
        del states

        input_value = deepcopy(normalize_signal_value(self.inputs.get(0, 0.0)))
        target_height, target_width = _resolve_target_shape(
            input_value,
            self.current_output,
            self.on_switch_value,
            self.off_switch_value,
            self.on_output_value,
            self.off_output_value,
        )
        input_flat = _broadcast_flat(input_value, target_height, target_width)
        current_flat = _broadcast_flat(self.current_output, target_height, target_width)
        on_switch_flat = _broadcast_flat(self.on_switch_value, target_height, target_width)
        off_switch_flat = _broadcast_flat(self.off_switch_value, target_height, target_width)
        on_output_flat = _broadcast_flat(self.on_output_value, target_height, target_width)
        off_output_flat = _broadcast_flat(self.off_output_value, target_height, target_width)

        result = []
        for value, previous, on_switch, off_switch, on_output, off_output in zip(
            input_flat,
            current_flat,
            on_switch_flat,
            off_switch_flat,
            on_output_flat,
            off_output_flat,
        ):
            if value >= on_switch:
                result.append(on_output)
            elif value <= off_switch:
                result.append(off_output)
            else:
                result.append(previous)

        self.current_output = reshape_signal(target_height, target_width, result)
        self.last_update_time = float(t)
        self.outputs[0] = self.clone_signal(self.current_output)

    def clone_signal(self, value):
        return deepcopy(normalize_signal_value(value))
