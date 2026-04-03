from copy import deepcopy

from ..base import Block
from ...signal_utils import normalize_signal_value


class DiscreteBlock(Block):
    """Base class for sampled-data blocks with held outputs."""

    time_tolerance = 1e-9

    def __init__(self, block_type, block_name, block_uuid, param_values, block_path=""):
        super().__init__(block_type, block_name, block_uuid, param_values, block_path)
        self.sample_time = None
        self.next_sample_time = 0.0
        self.last_sample_time = None
        self.current_output = 0.0

    def configure_timing(self, start_time, default_sample_time):
        sample_time = self._param_first(["SampleTime", "sampleTime"], -1.0)
        if sample_time <= 0.0:
            inherited_sample_time = None
            for upstream_block, _, _ in getattr(self, "input_sources", ()):
                upstream_sample_time = getattr(upstream_block, "sample_time", None)
                if upstream_sample_time is not None and upstream_sample_time > 0.0:
                    inherited_sample_time = float(upstream_sample_time)
                    break

            if inherited_sample_time is not None:
                sample_time = inherited_sample_time
            else:
                sample_time = default_sample_time if default_sample_time > 0.0 else 0.01

        self.sample_time = float(sample_time)
        self.next_sample_time = float(start_time)
        self.last_sample_time = None

    def compute_output(self, t, states):
        del states

        if self._should_sample(t):
            self.sample(t)
            self.last_sample_time = float(t)
            self.next_sample_time = self.last_sample_time + self.sample_time

        self.outputs[0] = self.clone_signal(self.current_output)

    def sample(self, t):
        raise NotImplementedError

    def clone_signal(self, value):
        return deepcopy(normalize_signal_value(value))

    def read_input(self, port=0, default=0.0):
        return self.clone_signal(self.inputs.get(port, default))

    def _should_sample(self, t):
        if self.sample_time is None:
            return False

        current_time = float(t)
        if (
            self.last_sample_time is not None
            and abs(current_time - self.last_sample_time) <= self.time_tolerance
        ):
            return False

        return current_time + self.time_tolerance >= self.next_sample_time
