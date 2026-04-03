from ..base import Block
from ...signal_utils import scalar_of


class NewMotorBlock(Block):
    def is_direct_feedthrough(self):
        return False

    def initialize(self):
        self.motor_k = 0.01
        self.motor_t = 0.07
        self.num_states = 1

    def get_initial_states(self):
        return [0.0]

    def compute_output(self, t, states):
        del t
        speed = float(states[self.state_offset])
        self.outputs[0] = 5000.0 * speed

    def compute_derivative(self, t, states):
        del t
        speed = float(states[self.state_offset])
        input_value = scalar_of(self.inputs.get(0, 0.0))
        return [(self.motor_k * input_value - speed) / self.motor_t]
