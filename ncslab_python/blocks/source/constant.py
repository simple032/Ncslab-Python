from ...signal_utils import parse_signal_parameter
from .base import SampledSourceBlock


class ConstantBlock(SampledSourceBlock):
    def is_direct_feedthrough(self):
        return False

    def _initialize_source(self):
        self.value = parse_signal_parameter(self.param_values.get("Value", 1.0))

    def _evaluate_source_output(self, t):
        del t
        return self.value
