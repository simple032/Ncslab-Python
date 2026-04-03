from ...signal_utils import parse_signal_parameter
from .base import SampledSourceBlock


class StepBlock(SampledSourceBlock):
    def is_direct_feedthrough(self):
        return False

    def _initialize_source(self):
        self.step_time = self._param_first(["Time", "time", "StepTime", "stepTime"], 1.0)
        self.initial_value = parse_signal_parameter(
            self.param_values.get(
                "InitialValue",
                self.param_values.get(
                    "initialValue",
                    self.param_values.get(
                        "Before",
                        self.param_values.get("before", 0.0),
                    ),
                ),
            )
        )
        self.final_value = parse_signal_parameter(
            self.param_values.get(
                "FinalValue",
                self.param_values.get(
                    "finalValue",
                    self.param_values.get(
                        "After",
                        self.param_values.get("after", 1.0),
                    ),
                ),
            )
        )

    def _evaluate_source_output(self, t):
        return self.final_value if t >= self.step_time else self.initial_value
