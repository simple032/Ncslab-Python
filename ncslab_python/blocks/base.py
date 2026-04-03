from ..signal_utils import evaluate_numeric_expression


class Block:
    """Base class for all simulation blocks."""

    def __init__(self, block_type, block_name, block_uuid, param_values, block_path=""):
        self.block_type = block_type
        self.block_name = block_name
        self.block_uuid = block_uuid
        self.block_path = block_path
        self.param_values = param_values or {}
        self.inputs = {}
        self.outputs = {}
        self.num_states = 0
        self.state_offset = 0

    def initialize(self):
        pass

    def is_direct_feedthrough(self):
        """Whether current outputs depend on current inputs."""
        return True

    def is_observer(self):
        """Observers consume finalized signals but do not affect other block outputs."""
        return False

    def compute_output(self, t, states):
        pass

    def compute_derivative(self, t, states):
        return []

    def get_initial_states(self):
        return []

    def get_preferred_output_times(self, start_time, stop_time):
        del start_time, stop_time
        return []

    def _param(self, key, default=0.0):
        value = self.param_values.get(key, default)
        if value is None:
            return default
        if isinstance(value, dict) and "value" in value:
            value = value["value"]
        try:
            return evaluate_numeric_expression(value)
        except (ValueError, TypeError):
            return default

    def _param_str(self, key, default=""):
        value = self.param_values.get(key, default)
        if value is None:
            return default
        if isinstance(value, dict) and "value" in value:
            value = value["value"]
        return str(value)

    def _param_first(self, keys, default=0.0):
        for key in keys:
            if key in self.param_values:
                return self._param(key, default)
        return default

    def _param_str_first(self, keys, default=""):
        for key in keys:
            if key in self.param_values:
                return self._param_str(key, default)
        return default
