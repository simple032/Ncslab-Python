import math

from ..base import Block
from ...signal_utils import elementwise_binary_op, elementwise_unary_op


class MathFunctionBlock(Block):
    def initialize(self):
        self.operator = self._param_str_first(
            ["Operator", "FunctionType", "MathFunctionOperator"],
            "exp",
        ).strip()

    def compute_output(self, t, states):
        u = self.inputs.get(0, 0.0)
        op = self.operator

        if op == "sin":
            y = elementwise_unary_op(u, math.sin)
        elif op == "cos":
            y = elementwise_unary_op(u, math.cos)
        elif op == "tan":
            y = elementwise_unary_op(u, math.tan)
        elif op == "asin":
            y = elementwise_unary_op(u, lambda value: math.asin(value) if -1.0 <= value <= 1.0 else 0.0)
        elif op == "acos":
            y = elementwise_unary_op(u, lambda value: math.acos(value) if -1.0 <= value <= 1.0 else 0.0)
        elif op == "atan":
            y = elementwise_unary_op(u, math.atan)
        elif op == "sqrt":
            y = elementwise_unary_op(u, lambda value: math.sqrt(value) if value >= 0 else 0.0)
        elif op == "exp":
            y = elementwise_unary_op(u, math.exp)
        elif op in ("log", "ln"):
            y = elementwise_unary_op(u, lambda value: math.log(value) if value > 0 else 0.0)
        elif op == "log10":
            y = elementwise_unary_op(u, lambda value: math.log10(value) if value > 0 else 0.0)
        elif op == "abs":
            y = elementwise_unary_op(u, abs)
        elif op == "square":
            y = elementwise_unary_op(u, lambda value: value * value)
        elif op == "reciprocal":
            y = elementwise_unary_op(u, lambda value: 1.0 / value if value != 0 else 0.0)
        elif op == "pow":
            exponent = self.inputs.get(1, 1.0)
            y = elementwise_binary_op(u, exponent, lambda left, right: math.pow(left, right))
        elif op == "sign":
            y = elementwise_unary_op(u, lambda value: 1.0 if value > 0 else (-1.0 if value < 0 else 0.0))
        elif op == "ceil":
            y = elementwise_unary_op(u, math.ceil)
        elif op == "floor":
            y = elementwise_unary_op(u, math.floor)
        elif op == "round":
            y = elementwise_unary_op(u, round)
        else:
            y = u

        self.outputs[0] = y
