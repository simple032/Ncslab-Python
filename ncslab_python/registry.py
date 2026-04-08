import sys

from .blocks.base import Block
from .blocks.continuous import DerivativeBlock, IntegratorBlock, PIDControllerBlock, StateSpaceBlock, TransferFcnBlock
from .blocks.circuit import (
    ACCurrentSourceBlock,
    ACVoltageSourceBlock,
    AmmeterBlock,
    CapacitorBlock,
    CircuitSwitchBlock,
    ControlledCurrentSourceBlock,
    ControlledVoltageSourceBlock,
    CurrentSensorBlock,
    DCCurrentSourceBlock,
    DCVoltageSourceBlock,
    DiodeBlock,
    ElectricalReferenceBlock,
    GroundBlock,
    IGBTBlock,
    InductorBlock,
    MosfetBlock,
    OpAmpBlock,
    PSSimulinkConverterBlock,
    ResistorBlock,
    SeriesRLCBranchBlock,
    SolverConfigurationBlock,
    VariableCapacitorBlock,
    VariableInductorBlock,
    VariableResistorBlock,
    VoltmeterBlock,
    VoltageSensorBlock,
)
from .blocks.discrete import (
    DelayBlock,
    DifferenceBlock,
    DiscreteTimeIntegratorBlock,
    DiscreteTransferFcnBlock,
    DiscreteTransferFcnZBlock,
    UnitDelayBlock,
    ZeroOrderHoldBlock,
)
from .blocks.discontinuous import BacklashBlock, CoulombBlock, DeadZoneBlock, RelayBlock, SaturationBlock
from .blocks.function import SFunctionBlock
from .blocks.hardware import PWMBlock
from .blocks.logic import CompareToConstantBlock, RelationalOperatorBlock
from .blocks.math import (
    AbsBlock,
    BiasBlock,
    GainBlock,
    MathFunctionBlock,
    MinMaxBlock,
    ProductBlock,
    SignBlock,
    SqrtBlock,
    SumBlock,
    TestPointBlock,
    TrigFunctionBlock,
    UnaryMinusBlock,
)
from .blocks.route import DemuxBlock, FromBlock, GotoBlock, MuxBlock, SwitchBlock
from .blocks.sink import DisplayBlock, ScopeBlock
from .blocks.source import (
    BandLimitedWhiteNoiseBlock,
    ClockBlock,
    ConstantBlock,
    PulseGeneratorBlock,
    RampBlock,
    RepeatingSequenceBlock,
    SineWaveBlock,
    StepBlock,
)
from .blocks.testrig import NewMotorBlock, ServoMotorSliderBlock


BLOCK_REGISTRY = {
    "Constant": ConstantBlock,
    "Step": StepBlock,
    "SineWave": SineWaveBlock,
    "Ramp": RampBlock,
    "Clock": ClockBlock,
    "BandLimitedWhiteNoise": BandLimitedWhiteNoiseBlock,
    "Band-LimitedWhiteNoise": BandLimitedWhiteNoiseBlock,
    "Band-LimitedWhite Noise": BandLimitedWhiteNoiseBlock,
    "Band-Limited White Noise": BandLimitedWhiteNoiseBlock,
    "Noise": BandLimitedWhiteNoiseBlock,
    "PulseGenerator": PulseGeneratorBlock,
    "Pulse": PulseGeneratorBlock,
    "RepeatingSequence": RepeatingSequenceBlock,
    "Repeating Sequence": RepeatingSequenceBlock,
    "Gain": GainBlock,
    "Bias": BiasBlock,
    "Sum": SumBlock,
    "Add": SumBlock,
    "Product": ProductBlock,
    "Abs": AbsBlock,
    "UnaryMinus": UnaryMinusBlock,
    "Sign": SignBlock,
    "TrigFunction": TrigFunctionBlock,
    "TrigonometricFunction": TrigFunctionBlock,
    "Trigonometric Function": TrigFunctionBlock,
    "Sqrt": SqrtBlock,
    "MinMax": MinMaxBlock,
    "MathFunction": MathFunctionBlock,
    "Integrator": IntegratorBlock,
    "Derivative": DerivativeBlock,
    "Zero-Order Hold": ZeroOrderHoldBlock,
    "ZeroOrderHold": ZeroOrderHoldBlock,
    "Zero_Order_Hold": ZeroOrderHoldBlock,
    "Discrete-Time Integrator": DiscreteTimeIntegratorBlock,
    "Discrete Time Integrator": DiscreteTimeIntegratorBlock,
    "Discrete_Time_Integrator": DiscreteTimeIntegratorBlock,
    "Unit Delay": UnitDelayBlock,
    "UnitDelay": UnitDelayBlock,
    "Delay": DelayBlock,
    "Difference": DifferenceBlock,
    "Discrete Transfer Fcn": DiscreteTransferFcnBlock,
    "Discrete_Transfer_Fcn": DiscreteTransferFcnBlock,
    "Discrete Transfer Fcn (z)": DiscreteTransferFcnZBlock,
    "Discrete_Transfer_Fcnz": DiscreteTransferFcnZBlock,
    "PIDController": PIDControllerBlock,
    "PID Controller": PIDControllerBlock,
    "PID Controller (s)": PIDControllerBlock,
    "PIDController(s)": PIDControllerBlock,
    "Scope": ScopeBlock,
    "Display": DisplayBlock,
    "Mux": MuxBlock,
    "Demux": DemuxBlock,
    "Switch": SwitchBlock,
    "Goto": GotoBlock,
    "From": FromBlock,
    "Saturation": SaturationBlock,
    "Relay": RelayBlock,
    "DeadZone": DeadZoneBlock,
    "Dead Zone": DeadZoneBlock,
    "Backlash": BacklashBlock,
    "Coulomb": CoulombBlock,
    "CoulombViscousFriction": CoulombBlock,
    "Coulomb Viscous Friction": CoulombBlock,
    "Coulomb & Viscous Friction": CoulombBlock,
    "PWM": PWMBlock,
    "PWMForStm32": PWMBlock,
    "S-Function": SFunctionBlock,
    "SFunction": SFunctionBlock,
    "S-FunctionBuilder": SFunctionBlock,
    "SFunctionBuilder": SFunctionBlock,
    "RelationalOperator": RelationalOperatorBlock,
    "CompareToConstant": CompareToConstantBlock,
    "TestPoint": TestPointBlock,
    "Test Point": TestPointBlock,
    "Sine Wave": SineWaveBlock,
    "Pulse Generator": PulseGeneratorBlock,
    "newMotor": NewMotorBlock,
    "ServoMotorSlider": ServoMotorSliderBlock,
    "ServoMotorSliderSimu": ServoMotorSliderBlock,
    "Resistor": ResistorBlock,
    "Capacitor": CapacitorBlock,
    "Inductor": InductorBlock,
    "DC Voltage Source": DCVoltageSourceBlock,
    "DCVoltageSource": DCVoltageSourceBlock,
    "DC Current Source": DCCurrentSourceBlock,
    "DCCurrentSource": DCCurrentSourceBlock,
    "AC Voltage Source": ACVoltageSourceBlock,
    "ACVoltageSource": ACVoltageSourceBlock,
    "AC Current Source": ACCurrentSourceBlock,
    "ACCurrentSource": ACCurrentSourceBlock,
    "Controlled Voltage Source": ControlledVoltageSourceBlock,
    "ControlledVoltageSource": ControlledVoltageSourceBlock,
    "Controlled Current Source": ControlledCurrentSourceBlock,
    "ControlledCurrentSource": ControlledCurrentSourceBlock,
    "Voltage Sensor": VoltageSensorBlock,
    "VoltageSensor": VoltageSensorBlock,
    "Voltage Meter": VoltmeterBlock,
    "VoltageMeter": VoltmeterBlock,
    "Voltmeter": VoltmeterBlock,
    "Current Sensor": CurrentSensorBlock,
    "CurrentSensor": CurrentSensorBlock,
    "Current Meter": AmmeterBlock,
    "CurrentMeter": AmmeterBlock,
    "Ammeter": AmmeterBlock,
    "Variable Resistor": VariableResistorBlock,
    "VariableResistor": VariableResistorBlock,
    "Variable Inductor": VariableInductorBlock,
    "VariableInductor": VariableInductorBlock,
    "Variable Capacitor": VariableCapacitorBlock,
    "VariableCapacitor": VariableCapacitorBlock,
    "SeriesRLCBranch": SeriesRLCBranchBlock,
    "Circuit\tSwitch": CircuitSwitchBlock,
    "Circuit Switch": CircuitSwitchBlock,
    "CircuitSwitch": CircuitSwitchBlock,
    "Diode": DiodeBlock,
    "Op Amp": OpAmpBlock,
    "OpAmp": OpAmpBlock,
    "IGBT": IGBTBlock,
    "Mosfet": MosfetBlock,
    "Ground": GroundBlock,
    "Electrical Reference": ElectricalReferenceBlock,
    "ElectricalReference": ElectricalReferenceBlock,
    "Solver Configuration": SolverConfigurationBlock,
    "SolverConfiguration": SolverConfigurationBlock,
    "PS-Simulink Converter": PSSimulinkConverterBlock,
    "PS-SimulinkConverter": PSSimulinkConverterBlock,
    "PS-S": PSSimulinkConverterBlock,
    "PSSimulinkConverter": PSSimulinkConverterBlock,
}


def _normalize_block_type(value):
    text = str(value or "").strip().lower()
    for token in (" ", "-", "_", "\t"):
        text = text.replace(token, "")
    return text


if TransferFcnBlock is not None:
    BLOCK_REGISTRY["TransferFcn"] = TransferFcnBlock
    BLOCK_REGISTRY["Transfer Fcn"] = TransferFcnBlock

if StateSpaceBlock is not None:
    BLOCK_REGISTRY["StateSpace"] = StateSpaceBlock
    BLOCK_REGISTRY["State-Space"] = StateSpaceBlock


def create_block(block_data):
    block_type = block_data.get("blockType", "")
    block_name = block_data.get("blockName", "")
    block_uuid = block_data.get("blockUUID", "")
    block_path = block_data.get("blockPath", "")
    param_values = block_data.get("paramValues", {})

    block_class = BLOCK_REGISTRY.get(block_type)
    if block_class is None:
        normalized_type = _normalize_block_type(block_type)
        for key, value in BLOCK_REGISTRY.items():
            if _normalize_block_type(key) == normalized_type:
                block_class = value
                break
    if block_class is None:
        src_block = block_data.get("srcBlock", "")
        print(
            "Warning: unsupported block degraded to pass-through "
            f"(blockType='{block_type}', blockName='{block_name}', srcBlock='{src_block}')",
            file=sys.stderr,
        )
        return Block(block_type, block_name, block_uuid, param_values, block_path)

    return block_class(block_type, block_name, block_uuid, param_values, block_path)
