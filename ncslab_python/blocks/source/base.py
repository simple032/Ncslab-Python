from copy import deepcopy

from ..base import Block
from ...signal_utils import normalize_signal_value


class SampledSourceBlock(Block):
    """Base class for source blocks that support continuous or sampled output."""

    time_tolerance = 1e-9

    def __init__(self, block_type, block_name, block_uuid, param_values, block_path=""):
        super().__init__(block_type, block_name, block_uuid, param_values, block_path)
        self.sample_time = None
        self.next_sample_time = 0.0
        self.last_sample_time = None
        self.current_output = 0.0

    def initialize(self):
        self.sample_time_value = self._param_first(["SampleTime", "sampleTime"], 0.0)
        self._initialize_source()

    def configure_timing(self, start_time, default_sample_time):
        raw_sample_time = self.sample_time_value

        if raw_sample_time == 0.0:
            self.sample_time = None
            return

        if raw_sample_time == -1.0:
            effective_sample_time = default_sample_time if default_sample_time > 0.0 else None
        elif raw_sample_time > 0.0:
            effective_sample_time = raw_sample_time
        else:
            effective_sample_time = None

        if effective_sample_time is None:
            self.sample_time = None
            return

        self.sample_time = float(effective_sample_time)
        self.next_sample_time = float(start_time)
        self.last_sample_time = None
        self.current_output = self.clone_signal(self._evaluate_source_output(float(start_time)))

    def compute_output(self, t, states):
        del states

        if self.sample_time is None:
            self.outputs[0] = self.clone_signal(self._evaluate_source_output(float(t)))
            return

        if self._should_sample(t):
            self.current_output = self.clone_signal(self._evaluate_source_output(float(t)))
            self.last_sample_time = float(t)
            self.next_sample_time = self.last_sample_time + self.sample_time

        self.outputs[0] = self.clone_signal(self.current_output)

    def clone_signal(self, value):
        return deepcopy(normalize_signal_value(value))

    def _should_sample(self, t):
        current_time = float(t)
        if (
            self.last_sample_time is not None
            and abs(current_time - self.last_sample_time) <= self.time_tolerance
        ):
            return False
        return current_time + self.time_tolerance >= self.next_sample_time

    def _initialize_source(self):
        pass

    def _evaluate_source_output(self, t):
        raise NotImplementedError
