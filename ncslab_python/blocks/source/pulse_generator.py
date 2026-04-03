from ...signal_utils import parse_signal_parameter
from .base import SampledSourceBlock


class PulseGeneratorBlock(SampledSourceBlock):
    def is_direct_feedthrough(self):
        return False

    def _initialize_source(self):
        amplitude_value = self.param_values.get(
            "Amplitude",
            self.param_values.get("amplitude", 1.0),
        )
        self.amplitude = parse_signal_parameter(amplitude_value)
        self.period = self._param_first(["Period", "period"], 1.0)
        self.pulse_width = self._param_first(["PulseWidth", "pulseWidth"], 50.0)
        self.phase_delay = self._param_first(["PhaseDelay", "phaseDelay"], 0.0)

    def _evaluate_source_output(self, t):
        if self.period <= 0.0:
            return 0.0

        adjusted_time = float(t) - self.phase_delay
        if adjusted_time < 0.0:
            return 0.0

        pulse_width_time = self.pulse_width / 100.0 * self.period
        pulse_width_time = max(0.0, min(pulse_width_time, self.period))
        if pulse_width_time <= 0.0:
            return 0.0

        time_in_period = adjusted_time % self.period
        return self.amplitude if time_in_period < pulse_width_time else 0.0

    def get_preferred_output_times(self, start_time, stop_time):
        if self.period <= 0.0:
            return []

        pulse_width_time = self.pulse_width / 100.0 * self.period
        pulse_width_time = max(0.0, min(pulse_width_time, self.period))
        if pulse_width_time <= 0.0:
            return []

        start = float(start_time)
        stop = float(stop_time)
        phase_delay = float(self.phase_delay)
        period = float(self.period)
        epsilon = min(period * 1e-6, 1e-6)

        preferred_times = []

        if stop < phase_delay:
            return preferred_times

        cycle_index = int((start - phase_delay) // period) - 1
        cycle_index = max(cycle_index, -1)

        while True:
            pulse_start = phase_delay + cycle_index * period
            pulse_end = pulse_start + pulse_width_time

            if pulse_start > stop + epsilon:
                break

            if pulse_end >= start - epsilon:
                for candidate in (
                    pulse_start,
                    pulse_start + min(pulse_width_time * 0.5, max(pulse_width_time - epsilon, 0.0)),
                    pulse_end,
                ):
                    if start - epsilon <= candidate <= stop + epsilon:
                        preferred_times.append(float(candidate))

            cycle_index += 1

        return preferred_times
