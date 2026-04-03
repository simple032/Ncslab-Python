from .base import DiscreteBlock


class ZeroOrderHoldBlock(DiscreteBlock):
    def initialize(self):
        self.current_output = 0.0

    def is_direct_feedthrough(self):
        return True

    def sample(self, t):
        del t
        self.current_output = self.read_input()
