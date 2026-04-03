from ..base import Block
from ...signal_utils import elementwise_unary_op


class SignBlock(Block):
    def compute_output(self, t, states):
        self.outputs[0] = elementwise_unary_op(
            self.inputs.get(0, 0.0),
            lambda value: 1.0 if value > 0 else (-1.0 if value < 0 else 0.0),
        )
