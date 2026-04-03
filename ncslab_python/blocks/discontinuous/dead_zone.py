from ..base import Block
from ...signal_utils import elementwise_unary_op


class DeadZoneBlock(Block):
    def initialize(self):
        self.lower_value = self._param_first(["LowerValue", "StartOfDeadZone"], -0.5)
        self.upper_value = self._param_first(["UpperValue", "EndOfDeadZone"], 0.5)

    def compute_output(self, t, states):
        del t, states

        def apply_dead_zone(value):
            if self.lower_value <= value <= self.upper_value:
                return 0.0
            if value < self.lower_value:
                return value - self.lower_value
            return value - self.upper_value

        self.outputs[0] = elementwise_unary_op(self.inputs.get(0, 0.0), apply_dead_zone)
