from collections import deque

from ...signal_utils import parse_signal_parameter
from .base import DiscreteBlock


class DelayBlock(DiscreteBlock):
    def initialize(self):
        self.delay_length = max(0, int(round(self._param("DelayLength", 1.0))))
        self.initial_condition = parse_signal_parameter(self.param_values.get("InitialCondition", 0.0))
        self.buffer = deque(
            (self.clone_signal(self.initial_condition) for _ in range(self.delay_length)),
            maxlen=max(self.delay_length, 1),
        )
        self.current_output = self.clone_signal(self.initial_condition)

    def is_direct_feedthrough(self):
        return self.delay_length == 0

    def sample(self, t):
        del t
        input_signal = self.read_input()

        if self.delay_length == 0:
            self.current_output = input_signal
            return

        delayed_value = self.buffer.popleft() if self.buffer else self.clone_signal(self.initial_condition)
        self.buffer.append(input_signal)
        self.current_output = self.clone_signal(delayed_value)
