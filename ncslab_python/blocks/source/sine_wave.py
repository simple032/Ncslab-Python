import math

from ...signal_utils import elementwise_unary_op, parse_signal_parameter, signal_add, signal_multiply
from .base import SampledSourceBlock


class SineWaveBlock(SampledSourceBlock):
    def is_direct_feedthrough(self):
        return False

    def _initialize_source(self):
        self.amplitude = parse_signal_parameter(self.param_values.get("Amplitude", 1.0))
        self.frequency = parse_signal_parameter(self.param_values.get("Frequency", 1.0))
        self.phase = parse_signal_parameter(self.param_values.get("Phase", 0.0))
        self.bias = parse_signal_parameter(self.param_values.get("Bias", 0.0))

    def _evaluate_source_output(self, t):
        phase_signal = signal_add(signal_multiply(self.frequency, t), self.phase)
        wave_signal = elementwise_unary_op(phase_signal, math.sin)
        return signal_add(signal_multiply(self.amplitude, wave_signal), self.bias)
