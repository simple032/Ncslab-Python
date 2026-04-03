from ...signal_utils import parse_signal_parameter
from .base import DiscreteBlock


class UnitDelayBlock(DiscreteBlock):
    def initialize(self):
        self.stored_value = parse_signal_parameter(self.param_values.get("InitialCondition", 0.0))
        self.current_output = self.clone_signal(self.stored_value)

    def is_direct_feedthrough(self):
        return False

    def sample(self, t):
        del t
        self.current_output = self.clone_signal(self.stored_value)
        self.stored_value = self.read_input()
