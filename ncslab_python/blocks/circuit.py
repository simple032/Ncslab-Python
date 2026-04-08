from copy import deepcopy
import math

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
        self.control_value = _coerce_scalar(self._read_input_any_port(default), default)
        return self.control_value

    def _read_input_any_port(self, default=0.0):
        # Some exported models mix 0-based and 1-based ports.
        if 0 in self.inputs:
            return self.inputs.get(0, default)
        if 1 in self.inputs:
            return self.inputs.get(1, default)
        if self.inputs:
            first_key = next(iter(self.inputs))
            return self.inputs.get(first_key, default)
        return default

    def _write_output_scalar_compat(self, value):
        scalar = _clone_signal(value)
        # Keep mirrored scalar outputs for mixed port conventions.
        self.outputs[0] = scalar
        self.outputs[1] = _clone_signal(scalar)
        self.outputs[2] = _clone_signal(scalar)
        self.outputs[3] = _clone_signal(scalar)

    def _param_with_aliases(self, aliases, default=0.0):
        return self._param_first(aliases, default)

    @staticmethod
    def _clamp_min(value, min_value, fallback):
        try:
            numeric = float(value)
        except (TypeError, ValueError):
            return fallback
        if numeric < min_value:
            return fallback
        return numeric


class PassiveCircuitBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        self.element_value = 0.0

    def compute_output(self, t, states):
        del t, states
        self.outputs.clear()


class ResistorBlock(PassiveCircuitBlock):
    def initialize(self):
        super().initialize()
        value = self._param_with_aliases(["R", "Resistance", "resistance"], 1.0)
        self.element_value = self._clamp_min(value, 1e-12, 1.0)


class InductorBlock(PassiveCircuitBlock):
    def initialize(self):
        super().initialize()
        value = self._param_with_aliases(["L", "Inductance", "inductance"], 1e-6)
        self.element_value = self._clamp_min(value, 1e-15, 1e-6)


class CapacitorBlock(PassiveCircuitBlock):
    def initialize(self):
        super().initialize()
        value = self._param_with_aliases(["C", "Capacitance", "capacitance"], 1e-6)
        self.element_value = self._clamp_min(value, 1e-15, 1e-6)


class SensorCircuitBlock(CircuitCompatibilityBlock):
    output_default = 0.0

    def initialize(self):
        super().initialize()
        self.scale = self._param_first(["Scale", "scale", "Gain", "gain"], 1.0)
        self.offset = self._param_first(["Offset", "offset", "Bias", "bias"], 0.0)
        self.measured_value = self.output_default
        self.use_network_measurement = False

    def is_direct_feedthrough(self):
        return True

    def compute_output(self, t, states):
        del t, states
        if self.use_network_measurement:
            measured_value = _coerce_scalar(self.measured_value, self.measured_value)
        else:
            measured_value = _coerce_scalar(
                self._read_input_any_port(self.measured_value),
                self.measured_value,
            )
        self._write_output_scalar_compat(self.scale * measured_value + self.offset)


class VoltageSensorBlock(SensorCircuitBlock):
    pass


class CurrentSensorBlock(SensorCircuitBlock):
    pass


class VoltmeterBlock(VoltageSensorBlock):
    pass


class AmmeterBlock(CurrentSensorBlock):
    pass


class ControlledCircuitBlock(CircuitCompatibilityBlock):
    def compute_output(self, t, states):
        del t, states
        self._read_control_input()
        self.outputs.clear()


class DCVoltageSourceBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        raw_voltage = self._param_with_aliases(
            [
                "Amplitude",
                "Voltage",
                "V",
                "value",
                "Value",
                "v0",
                "V0",
                "dc",
            ],
            0.0,
        )
        self.voltage_value = _coerce_scalar(raw_voltage, 0.0)

    def is_direct_feedthrough(self):
        return False

    def compute_output(self, t, states):
        del t, states
        self._write_output_scalar_compat(self.voltage_value)


class DCCurrentSourceBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        raw_current = self._param_with_aliases(
            ["Amplitude", "Current", "I", "value", "Value", "i0", "I0", "dc"],
            0.0,
        )
        self.current_value = _coerce_scalar(raw_current, 0.0)

    def is_direct_feedthrough(self):
        return False

    def compute_output(self, t, states):
        del t, states
        self._write_output_scalar_compat(self.current_value)


class ACVoltageSourceBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        self.amplitude = _coerce_scalar(
            self._param_with_aliases(["Amplitude", "A", "Vpk", "Vac", "v0", "V0"], 1.0),
            1.0,
        )
        self.frequency = _coerce_scalar(
            self._param_with_aliases(["Frequency", "f", "Freq", "Hz"], 50.0),
            50.0,
        )
        self.phase_deg = _coerce_scalar(
            self._param_with_aliases(["Phase", "PhaseDeg", "phi"], 0.0),
            0.0,
        )
        self.bias = _coerce_scalar(
            self._param_with_aliases(["Bias", "Offset", "DC", "dc"], 0.0),
            0.0,
        )

    def is_direct_feedthrough(self):
        return False

    def compute_output(self, t, states):
        del states
        phase_rad = math.radians(self.phase_deg)
        value = self.bias + self.amplitude * math.sin(2.0 * math.pi * self.frequency * float(t) + phase_rad)
        self._write_output_scalar_compat(value)


class ACCurrentSourceBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        self.amplitude = _coerce_scalar(
            self._param_with_aliases(["Amplitude", "A", "Ipk", "Iac", "i0", "I0"], 1.0),
            1.0,
        )
        self.frequency = _coerce_scalar(
            self._param_with_aliases(["Frequency", "f", "Freq", "Hz"], 50.0),
            50.0,
        )
        self.phase_deg = _coerce_scalar(
            self._param_with_aliases(["Phase", "PhaseDeg", "phi"], 0.0),
            0.0,
        )
        self.bias = _coerce_scalar(
            self._param_with_aliases(["Bias", "Offset", "DC", "dc"], 0.0),
            0.0,
        )

    def is_direct_feedthrough(self):
        return False

    def compute_output(self, t, states):
        del states
        phase_rad = math.radians(self.phase_deg)
        value = self.bias + self.amplitude * math.sin(2.0 * math.pi * self.frequency * float(t) + phase_rad)
        self._write_output_scalar_compat(value)


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


class ControlledVoltageSourceBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        self.gain = _coerce_scalar(self._param_with_aliases(["Gain", "K", "k"], 1.0), 1.0)
        self.bias = _coerce_scalar(self._param_with_aliases(["Bias", "Offset"], 0.0), 0.0)

    def is_direct_feedthrough(self):
        return True

    def compute_output(self, t, states):
        del t, states
        control = self._read_control_input()
        self._write_output_scalar_compat(self.gain * control + self.bias)


class ControlledCurrentSourceBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        self.gain = _coerce_scalar(self._param_with_aliases(["Gain", "K", "k"], 1.0), 1.0)
        self.bias = _coerce_scalar(self._param_with_aliases(["Bias", "Offset"], 0.0), 0.0)

    def is_direct_feedthrough(self):
        return True

    def compute_output(self, t, states):
        del t, states
        control = self._read_control_input()
        self._write_output_scalar_compat(self.gain * control + self.bias)


class CircuitSwitchBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        self.closed_value = _coerce_scalar(self._param_with_aliases(["OnValue", "ClosedValue"], 1.0), 1.0)
        self.open_value = _coerce_scalar(self._param_with_aliases(["OffValue", "OpenValue"], 0.0), 0.0)
        self.threshold = _coerce_scalar(self._param_with_aliases(["Threshold", "th"], 0.5), 0.5)

    def is_direct_feedthrough(self):
        return True

    def compute_output(self, t, states):
        del t, states
        control = self._read_control_input()
        self._write_output_scalar_compat(self.closed_value if control >= self.threshold else self.open_value)


class SeriesRLCBranchBlock(PassiveCircuitBlock):
    pass


class VariableResistorBlock(PassiveCircuitBlock):
    pass


class VariableInductorBlock(PassiveCircuitBlock):
    pass


class VariableCapacitorBlock(PassiveCircuitBlock):
    pass


class DiodeBlock(PassiveCircuitBlock):
    pass


class OpAmpBlock(CircuitCompatibilityBlock):
    def initialize(self):
        super().initialize()
        self.gain = _coerce_scalar(self._param_with_aliases(["Gain", "A", "OpenLoopGain"], 1e5), 1e5)
        self.min_output = _coerce_scalar(self._param_with_aliases(["MinOutput", "Vmin"], -1e9), -1e9)
        self.max_output = _coerce_scalar(self._param_with_aliases(["MaxOutput", "Vmax"], 1e9), 1e9)

    def is_direct_feedthrough(self):
        return True

    def compute_output(self, t, states):
        del t, states
        value = self.gain * self._read_control_input()
        value = max(self.min_output, min(self.max_output, value))
        self._write_output_scalar_compat(value)


class GroundBlock(CircuitCompatibilityBlock):
    def compute_output(self, t, states):
        del t, states
        self._write_output_scalar_compat(0.0)


class ElectricalReferenceBlock(GroundBlock):
    pass


class SolverConfigurationBlock(CircuitCompatibilityBlock):
    """Simscape-like solver config placeholder for Python MNA mode."""

    def is_direct_feedthrough(self):
        return False

    def compute_output(self, t, states):
        del t, states
        self.outputs.clear()


class PSSimulinkConverterBlock(CircuitCompatibilityBlock):
    """Bridge physical-signal scalar into signal domain."""

    def initialize(self):
        super().initialize()
        self.scale = _coerce_scalar(self._param_with_aliases(["Scale", "Gain", "scale", "gain"], 1.0), 1.0)
        self.offset = _coerce_scalar(self._param_with_aliases(["Offset", "Bias", "offset", "bias"], 0.0), 0.0)
        self.measured_value = 0.0
        self.use_network_measurement = False

    def is_direct_feedthrough(self):
        return True

    def compute_output(self, t, states):
        del t, states
        fallback = self.measured_value if hasattr(self, "measured_value") else 0.0
        if self.use_network_measurement:
            value = _coerce_scalar(fallback, fallback)
        else:
            value = _coerce_scalar(self._read_input_any_port(fallback), fallback)
        self.measured_value = value
        self._write_output_scalar_compat(self.scale * value + self.offset)
