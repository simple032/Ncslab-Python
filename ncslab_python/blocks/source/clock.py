from .base import SampledSourceBlock


class ClockBlock(SampledSourceBlock):
    def is_direct_feedthrough(self):
        return False

    def _evaluate_source_output(self, t):
        return t
