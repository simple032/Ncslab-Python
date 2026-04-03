from ...signal_utils import parse_signal_parameter, signal_add, signal_multiply
from .base import DiscreteBlock


class DiscreteTimeIntegratorBlock(DiscreteBlock):
    def initialize(self):
        self.gain = parse_signal_parameter(self.param_values.get("Gain", self.param_values.get("gainval", 1.0)))
        self.state_value = parse_signal_parameter(self.param_values.get("InitialCondition", 0.0))
        self.method = self._param_str("IntegratorMethod", "Forward Euler").strip().lower()
        self.current_output = self.clone_signal(self.state_value)

    def is_direct_feedthrough(self):
        return False

    def sample(self, t):
        del t
        input_signal = self.read_input()
        self.current_output = self.clone_signal(self.state_value)

        scale = self._integration_scale()
        increment = signal_multiply(scale, signal_multiply(self.gain, input_signal))
        self.state_value = signal_add(self.state_value, increment)

    def _integration_scale(self):
        if "accumulation" in self.method:
            return 0.5 if "trapezoidal" in self.method else 1.0
        return self.sample_time * (0.5 if "trapezoidal" in self.method else 1.0)
