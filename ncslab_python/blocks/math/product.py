from ..base import Block
from ...signal_utils import signal_divide, signal_matrix_multiply, signal_multiply


class ProductBlock(Block):
    def initialize(self):
        raw_inputs = self.param_values.get("Inputs", 2)
        if isinstance(raw_inputs, dict) and "value" in raw_inputs:
            raw_inputs = raw_inputs["value"]

        self.operations = []
        try:
            count = int(raw_inputs)
            self.operations = ["*"] * max(count, 1)
        except (TypeError, ValueError):
            ops = [char for char in str(raw_inputs) if char in "*/"]
            self.operations = ops if ops else ["*", "*"]

        self.num_inputs = len(self.operations)
        self.multiplication = self._param_str("Multiplication", "Element-wise(.*)").lower()

    def compute_output(self, t, states):
        result = 1.0
        for index in range(self.num_inputs):
            value = self.inputs.get(index, 1.0)
            operation = self.operations[index] if index < len(self.operations) else "*"

            if operation == "/":
                result = signal_divide(result, value)
            elif "matrix" in self.multiplication:
                result = signal_matrix_multiply(result, value)
            else:
                result = signal_multiply(result, value)
        self.outputs[0] = result
