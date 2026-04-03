import math

from ..base import Block
from ...signal_utils import elementwise_binary_op, elementwise_unary_op


class TrigFunctionBlock(Block):
    def initialize(self):
        self.function = self._param_str_first(
            ["Function", "TrigonometricFunction", "FunctionType"],
            "sin",
        ).strip()

    def compute_output(self, t, states):
        input_value = self.inputs.get(0, 0.0)

        if self.function == "sin":
            self.outputs[0] = elementwise_unary_op(input_value, math.sin)
        elif self.function == "cos":
            self.outputs[0] = elementwise_unary_op(input_value, math.cos)
        elif self.function == "tan":
            self.outputs[0] = elementwise_unary_op(input_value, math.tan)
        elif self.function == "asin":
            self.outputs[0] = elementwise_unary_op(
                input_value,
                lambda value: math.asin(value) if -1.0 <= value <= 1.0 else 0.0,
            )
        elif self.function == "acos":
            self.outputs[0] = elementwise_unary_op(
                input_value,
                lambda value: math.acos(value) if -1.0 <= value <= 1.0 else 0.0,
            )
        elif self.function == "atan":
            self.outputs[0] = elementwise_unary_op(input_value, math.atan)
        elif self.function == "atan2":
            second_input = self.inputs.get(1, 0.0)
            self.outputs[0] = elementwise_binary_op(
                input_value,
                second_input,
                lambda y_value, x_value: math.atan2(y_value, x_value),
            )
        else:
            self.outputs[0] = input_value

