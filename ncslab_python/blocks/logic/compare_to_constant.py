from ..base import Block
from ...signal_utils import parse_signal_parameter, signal_compare


class CompareToConstantBlock(Block):
    def initialize(self):
        self.constant = parse_signal_parameter(self.param_values.get("ConstantValue", 0.0))
        self.operator = self._param_str_first(["RelationalOperator", "Operator"], "==").strip()
        if self.operator == "~=":
            self.operator = "!="

    def compute_output(self, t, states):
        value = self.inputs.get(0, 0.0)

        if self.operator == "==":
            result = signal_compare(value, self.constant, lambda lhs, rhs: lhs == rhs)
        elif self.operator == "!=":
            result = signal_compare(value, self.constant, lambda lhs, rhs: lhs != rhs)
        elif self.operator == "<":
            result = signal_compare(value, self.constant, lambda lhs, rhs: lhs < rhs)
        elif self.operator == "<=":
            result = signal_compare(value, self.constant, lambda lhs, rhs: lhs <= rhs)
        elif self.operator == ">":
            result = signal_compare(value, self.constant, lambda lhs, rhs: lhs > rhs)
        else:
            result = signal_compare(value, self.constant, lambda lhs, rhs: lhs >= rhs)

        self.outputs[0] = result
