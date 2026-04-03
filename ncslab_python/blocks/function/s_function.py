from ..base import Block


class SFunctionBlock(Block):
    """Fallback Python simulation wrapper for user-defined S-Functions.

    The desktop product compiles and executes C S-Functions outside the Python
    simulator. Inside the lightweight Python engine we provide a safe fallback:
    outputs mirror connected inputs by port index when possible, otherwise the
    first input is reused, and disconnected outputs default to zero.
    """

    def initialize(self):
        self.output_num = max(
            1,
            int(self._param_first(["OutputNum", "NumOutputs"], 1.0)),
        )

    def compute_output(self, t, states):
        del t, states

        first_input = self.inputs.get(0, 0.0)
        for index in range(self.output_num):
            self.outputs[index] = self.inputs.get(index, first_input)
