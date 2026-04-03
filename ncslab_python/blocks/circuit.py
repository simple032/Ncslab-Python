from copy import deepcopy

from .base import Block
from ..signal_utils import normalize_signal_value


def _clone_signal(value):
    return deepcopy(normalize_signal_value(value))


def _coerce_scalar(value, default=0.0):
    if isinstance(value, dict):
        if "value" in value:
            return _coerce_scalar(value["value"], default)
        return default
    if isinstance(value, (list, tuple)):
        if not value:
            return default
        return _coerce_scalar(value[0], default)
    try:
        return float(value)
    except (TypeError, ValueError):
        return default


class CircuitCompatibilityBlock(Block):
    """Signal-side compatibility layer for circuit blocks."""

    def initialize(self):
        self.compatibility_mode = "signal_only"
        self.control_value = 0.0

    def is_circuit_block(self):
        return True

    def is_direct_feedthrough(self):
        return False

    def _read_control_input(self, default=0.0):
        self.control_value = _coerce_scalar(self.inputs.get(0, default), default)
        return self.control_value


class PassiveCircuitBlock(CircuitCompatibilityBlock):
    def compute_output(self, t, states):
        del t, states
        self.outputs.clear()


class SensorCircuitBlock(CircuitCompatibilityBlock):
    output_default = 0.0

    def initialize(self):
        super().initialize()
        self.scale = self._param_first(["Scale", "scale", "Gain", "gain"], 1.0)
        self.offset = self._param_first(["Offset", "offset", "Bias", "bias"], 0.0)
        self.measured_value = self.output_default

    def compute_output(self, t, states):
        del t, states
        measured_value = self.measured_value
        if 0 in self.inputs:
            measured_value = _coerce_scalar(self.inputs.get(0), self.measured_value)
        self.outputs[0] = _clone_signal(self.scale * measured_value + self.offset)


class VoltageSensorBlock(SensorCircuitBlock):
    pass


class CurrentSensorBlock(SensorCircuitBlock):
    pass


class ControlledCircuitBlock(CircuitCompatibilityBlock):
    def compute_output(self, t, states):
        del t, states
        self._read_control_input()
        self.outputs.clear()


class SwitchingCircuitBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        self.voltage_value = 0.0
        self.current_value = 0.0

    def is_direct_feedthrough(self):
        return True

    def compute_output(self, t, states):
        del t, states
        self._read_control_input()
        self.outputs[0] = _clone_signal([self.voltage_value, self.current_value])


class IGBTBlock(SwitchingCircuitBlock):
    pass


class MosfetBlock(SwitchingCircuitBlock):
    pass
