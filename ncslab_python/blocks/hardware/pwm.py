from ..base import Block
from ...signal_utils import scalar_of


class PWMBlock(Block):
    def initialize(self):
        self.frequency = self._param_first(
            ["PWMForStm32Frequency", "Frequency", "frequency"],
            1000.0,
        )
        self.duty_cycle = self._param_first(["DutyCycle", "dutyCycle"], 50.0)
        self.high_value = self._param_first(["HighValue", "Amplitude"], 1.0)
        self.low_value = self._param_first(["LowValue"], 0.0)
        self.phase_delay = self._param_first(["PhaseDelay"], 0.0)

    def compute_output(self, t, states):
        del states

        frequency = self.frequency if self.frequency > 0.0 else 1000.0
        duty_source = self.inputs.get(0, self.duty_cycle)
        duty_value = scalar_of(duty_source)
        duty_fraction = self._normalize_duty_cycle(duty_value)

        shifted_time = max(0.0, float(t) - self.phase_delay)
        period = 1.0 / frequency
        time_in_period = shifted_time % period
        on_time = duty_fraction * period

        self.outputs[0] = self.high_value if time_in_period < on_time else self.low_value

    @staticmethod
    def _normalize_duty_cycle(value):
        if value <= 0.0:
            return 0.0
        if value <= 1.0:
            return value
        if value <= 100.0:
            return value / 100.0
        if value <= 1000.0:
            return value / 1000.0
        return 1.0
