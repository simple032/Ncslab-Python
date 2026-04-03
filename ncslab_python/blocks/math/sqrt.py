import math

from ..base import Block
from ...signal_utils import elementwise_unary_op


class SqrtBlock(Block):
    def initialize(self):
        self.function = self._param_str_first(["Function", "SqrtFunction"], "sqrt")

    def compute_output(self, t, states):
        if self.function == "rSqrt":
            self.outputs[0] = elementwise_unary_op(
                self.inputs.get(0, 0.0),
                lambda value: 1.0 / math.sqrt(value) if value > 0 else 0.0,
            )
        elif self.function == "signedSqrt":
            self.outputs[0] = elementwise_unary_op(
                self.inputs.get(0, 0.0),
                lambda value: math.copysign(math.sqrt(abs(value)), value),
            )
        else:
            self.outputs[0] = elementwise_unary_op(
                self.inputs.get(0, 0.0),
                lambda value: math.sqrt(value) if value >= 0 else 0.0,
            )
