import math
import random

from .base import SampledSourceBlock


class BandLimitedWhiteNoiseBlock(SampledSourceBlock):
    def is_direct_feedthrough(self):
        return False

    def _initialize_source(self):
        self.seed = int(self._param_first(["seed", "Seed"], 0.0))
        self.covariance = self._param_first(["cov", "Cov"], 1.0)
        self.ts = self._param_first(["ts", "Ts", "SampleTime"], 0.1)
        stddev = math.sqrt(self.covariance) if self.covariance > 0.0 else 0.0
        self.stddev = stddev
        self._cache = {}

    def configure_timing(self, start_time, default_sample_time):
        if self.sample_time_value == 0.0 and self.ts > 0.0:
            self.sample_time_value = self.ts
        super().configure_timing(start_time, default_sample_time)

    def _evaluate_source_output(self, t):
        sample_period = self.sample_time if self.sample_time is not None else (self.ts if self.ts > 0.0 else 1.0)
        sample_index = int(math.floor((float(t) + 1e-12) / sample_period))

        if sample_index not in self._cache:
            base_seed = self.seed if self.seed != 0 else 0
            rng = random.Random(base_seed + sample_index)
            self._cache[sample_index] = rng.gauss(0.0, self.stddev)

        return self._cache[sample_index]
