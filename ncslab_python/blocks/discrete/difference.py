from ...signal_utils import parse_signal_parameter, signal_subtract
from .base import DiscreteBlock


class DifferenceBlock(DiscreteBlock):
    def initialize(self):
        self.previous_input = parse_signal_parameter(self.param_values.get("InitialCondition", 0.0))
        self.current_output = 0.0

    def is_direct_feedthrough(self):
        return True

    def sample(self, t):
        del t
        input_signal = self.read_input()
        self.current_output = signal_subtract(input_signal, self.previous_input)
        self.previous_input = self.clone_signal(input_signal)
