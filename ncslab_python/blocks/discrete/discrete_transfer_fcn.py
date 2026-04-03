from ...signal_utils import flatten_signal_elements, parse_signal_parameter, signal_add, signal_multiply, signal_subtract
from .base import DiscreteBlock


def _parse_coefficients(value, default):
    if value is None:
        return list(default)
    if isinstance(value, str) and not value.strip():
        return list(default)

    coefficients = [float(item) for item in flatten_signal_elements(parse_signal_parameter(value))]
    return coefficients or list(default)


class DiscreteTransferFcnBlock(DiscreteBlock):
    def initialize(self):
        numerator = _parse_coefficients(self.param_values.get("Numerator"), [1.0])
        denominator = _parse_coefficients(self.param_values.get("Denominator"), [1.0, -1.0])
        leading = denominator[0] if denominator else 1.0
        if abs(leading) < 1e-12:
            leading = 1.0

        self.numerator = [value / leading for value in numerator]
        self.denominator = [value / leading for value in denominator]
        initial_states = _parse_coefficients(self.param_values.get("InitialStates"), [0.0])
        input_order = max(len(self.numerator) - 1, 0)
        output_order = max(len(self.denominator) - 1, 0)

        self.input_history = [0.0] * input_order
        self.output_history = []
        for index in range(output_order):
            initial_value = initial_states[index] if index < len(initial_states) else initial_states[0]
            self.output_history.append(parse_signal_parameter(initial_value))
        self.current_output = 0.0

    def is_direct_feedthrough(self):
        return bool(self.numerator) and abs(self.numerator[0]) > 1e-12

    def sample(self, t):
        del t
        input_signal = self.read_input()
        output_signal = signal_multiply(self.numerator[0], input_signal)

        for index in range(1, len(self.numerator)):
            history_index = index - 1
            if history_index < len(self.input_history):
                output_signal = signal_add(
                    output_signal,
                    signal_multiply(self.numerator[index], self.input_history[history_index]),
                )

        for index in range(1, len(self.denominator)):
            history_index = index - 1
            if history_index < len(self.output_history):
                output_signal = signal_subtract(
                    output_signal,
                    signal_multiply(self.denominator[index], self.output_history[history_index]),
                )

        self.current_output = output_signal

        if self.input_history:
            self.input_history = [self.clone_signal(input_signal)] + self.input_history[:-1]
        if self.output_history:
            self.output_history = [self.clone_signal(output_signal)] + self.output_history[:-1]

    def get_initial_states(self):
        return []

    def compute_derivative(self, t, states):
        del t, states
        return []
