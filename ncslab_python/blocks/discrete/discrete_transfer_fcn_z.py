from ...signal_utils import flatten_signal_elements, signal_add, signal_multiply, signal_subtract
from .base import DiscreteBlock


def _coefficients_from_signal(value, default):
    coefficients = [float(item) for item in flatten_signal_elements(value)]
    return coefficients or list(default)


class DiscreteTransferFcnZBlock(DiscreteBlock):
    def initialize(self):
        self.input_history = []
        self.output_history = []
        self.current_output = 0.0

    def is_direct_feedthrough(self):
        return True

    def sample(self, t):
        del t
        input_signal = self.read_input(0)
        numerator = _coefficients_from_signal(self.inputs.get(1, [1.0]), [1.0])
        denominator = _coefficients_from_signal(self.inputs.get(2, [1.0]), [1.0])

        if not denominator or abs(denominator[0]) < 1e-12:
            self.current_output = 0.0
            return

        leading = denominator[0]
        numerator = [value / leading for value in numerator]
        denominator = [value / leading for value in denominator]

        input_order = max(len(numerator) - 1, 0)
        output_order = max(len(denominator) - 1, 0)
        self._resize_history(self.input_history, input_order)
        self._resize_history(self.output_history, output_order)

        output_signal = signal_multiply(numerator[0], input_signal)
        for index in range(1, len(numerator)):
            if index - 1 < len(self.input_history):
                output_signal = signal_add(
                    output_signal,
                    signal_multiply(numerator[index], self.input_history[index - 1]),
                )

        for index in range(1, len(denominator)):
            if index - 1 < len(self.output_history):
                output_signal = signal_subtract(
                    output_signal,
                    signal_multiply(denominator[index], self.output_history[index - 1]),
                )

        self.current_output = output_signal

        if input_order > 0:
            self.input_history = [self.clone_signal(input_signal)] + self.input_history[:input_order - 1]
        if output_order > 0:
            self.output_history = [self.clone_signal(output_signal)] + self.output_history[:output_order - 1]

    def _resize_history(self, history, size):
        while len(history) < size:
            history.append(0.0)
        if len(history) > size:
            del history[size:]
