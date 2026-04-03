from ...signal_utils import parse_signal_parameter, signal_add, signal_multiply
from .base import SampledSourceBlock


class RampBlock(SampledSourceBlock):
    def is_direct_feedthrough(self):
        return False

    def _initialize_source(self):
        self.slope = parse_signal_parameter(self.param_values.get("Slope", 1.0))
        self.start_time = self._param_first(["Start", "StartTime"], 0.0)
        self.initial_output = parse_signal_parameter(self.param_values.get("InitialOutput", 0.0))

    def _evaluate_source_output(self, t):
        if t < self.start_time:
            return self.initial_output
        return signal_add(
            signal_multiply(self.slope, t - self.start_time),
            self.initial_output,
        )
