from ...signal_utils import flatten_signal_elements, parse_signal_parameter
from .base import SampledSourceBlock


class RepeatingSequenceBlock(SampledSourceBlock):
    def is_direct_feedthrough(self):
        return False

    def _initialize_source(self):
        raw_times = self.param_values.get("rep_seq_t", self.param_values.get("TimeValues", "[0 1]"))
        raw_values = self.param_values.get("rep_seq_y", self.param_values.get("OutputValues", "[0 1]"))

        self.time_values = flatten_signal_elements(parse_signal_parameter(raw_times))
        self.output_values = flatten_signal_elements(parse_signal_parameter(raw_values))

        if len(self.time_values) < 2:
            self.time_values = [0.0, 1.0]
        if len(self.output_values) < 2:
            base = self.output_values[0] if self.output_values else 0.0
            self.output_values = [base, base]

        pair_count = min(len(self.time_values), len(self.output_values))
        self.time_values = [float(value) for value in self.time_values[:pair_count]]
        self.output_values = [float(value) for value in self.output_values[:pair_count]]

        if self.time_values[0] != 0.0:
            start_value = self.output_values[0]
            self.time_values.insert(0, 0.0)
            self.output_values.insert(0, start_value)

        self.period = self.time_values[-1] if self.time_values[-1] > 0.0 else 1.0

    def _evaluate_source_output(self, t):
        if self.period <= 0.0:
            return self.output_values[0]

        local_time = float(t) % self.period
        output = self.output_values[0]

        for index in range(len(self.time_values) - 1):
            start_time = self.time_values[index]
            end_time = self.time_values[index + 1]
            start_value = self.output_values[index]
            end_value = self.output_values[index + 1]

            if local_time < end_time or index == len(self.time_values) - 2:
                duration = end_time - start_time
                if duration <= 0.0:
                    output = end_value
                else:
                    ratio = (local_time - start_time) / duration
                    ratio = min(max(ratio, 0.0), 1.0)
                    output = start_value + ratio * (end_value - start_value)
                break

        return output
