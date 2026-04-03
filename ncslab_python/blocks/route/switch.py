from ..base import Block
from ...signal_utils import scalar_of


class SwitchBlock(Block):
    def initialize(self):
        self.threshold = self._param("Threshold", 0.0)
        self.criteria = self._param_str_first(["Criteria", "Relop"], ">=")

    def compute_output(self, t, states):
        true_value = self.inputs.get(0, 0.0)
        control = scalar_of(self.inputs.get(1, 0.0))
        false_value = self.inputs.get(2, 0.0)
        criteria = self.criteria.strip()

        if criteria == ">":
            condition = control > self.threshold
        elif criteria in (">=", "u2 >= Threshold"):
            condition = control >= self.threshold
        elif criteria in ("~=", "!=", "u2 ~= 0"):
            condition = control != 0.0
        else:
            condition = control >= self.threshold

        self.outputs[0] = true_value if condition else false_value
