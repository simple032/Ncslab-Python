from ..base import Block


class FromBlock(Block):
    def initialize(self):
        self.goto_tag = self._param_str_first(["GotoTag", "Gototag", "gotoTag"], "").strip()
        self.tag_visibility = self._param_str_first(
            ["TagVisibility", "tagVisibility"],
            "scoped",
        ).strip().lower()

    def compute_output(self, t, states):
        del t, states
        self.outputs[0] = self.inputs.get(0, 0.0)
