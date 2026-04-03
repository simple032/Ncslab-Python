from ..base import Block
from ...signal_utils import scalar_of


class ServoMotorSliderBlock(Block):
    def is_direct_feedthrough(self):
        return False

    def initialize(self):
        self.numerator = [
            self._param("num_0", 0.0),
            self._param("num_1", 17.41),
            self._param("num_2", 123.4),
        ]
        self.denominator = [
            self._param("den_0", 1.0),
            self._param("den_1", 2.01),
            self._param("den_2", 38.86),
            self._param("den_3", 49.06),
        ]
        self.num_states = 3

    def get_initial_states(self):
        return [0.0, 0.0, 0.0]

    def compute_output(self, t, states):
        del t
        x1, x2, x3 = [float(value) for value in states[self.state_offset:self.state_offset + self.num_states]]
        self.outputs[0] = (
            self.numerator[0] * x1
            + self.numerator[1] * x2
            + self.numerator[2] * x3
        )

    def compute_derivative(self, t, states):
        del t
        x1, x2, x3 = [float(value) for value in states[self.state_offset:self.state_offset + self.num_states]]
        input_value = scalar_of(self.inputs.get(0, 0.0))
        x1_dot = x2
        x2_dot = x3
        x3_dot = (
            input_value
            - self.denominator[1] * x1
            - self.denominator[2] * x2
            - self.denominator[3] * x3
        )
        return [x1_dot, x2_dot, x3_dot]
