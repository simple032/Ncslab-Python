from ..base import Block
from ...signal_utils import elementwise_unary_op


class AbsBlock(Block):
    def compute_output(self, t, states):
        self.outputs[0] = elementwise_unary_op(self.inputs.get(0, 0.0), abs)
