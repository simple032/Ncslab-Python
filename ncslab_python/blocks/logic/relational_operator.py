from ..base import Block
from ...signal_utils import signal_compare


class RelationalOperatorBlock(Block):
    def initialize(self):
        self.operator = self._param_str_first(["Operator", "relop"], "==").strip()
        if self.operator == "~=":
            self.operator = "!="

    def compute_output(self, t, states):
        left = self.inputs.get(0, 0.0)
        right = self.inputs.get(1, 0.0)

        if self.operator == "==":
            result = signal_compare(left, right, lambda lhs, rhs: lhs == rhs)
        elif self.operator == "!=":
            result = signal_compare(left, right, lambda lhs, rhs: lhs != rhs)
        elif self.operator == "<":
            result = signal_compare(left, right, lambda lhs, rhs: lhs < rhs)
        elif self.operator == "<=":
            result = signal_compare(left, right, lambda lhs, rhs: lhs <= rhs)
        elif self.operator == ">":
            result = signal_compare(left, right, lambda lhs, rhs: lhs > rhs)
        else:
            result = signal_compare(left, right, lambda lhs, rhs: lhs >= rhs)

        self.outputs[0] = result
