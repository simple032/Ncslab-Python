import sys

from .blocks.base import Block
from .blocks.continuous import DerivativeBlock, IntegratorBlock, PIDControllerBlock, StateSpaceBlock, TransferFcnBlock
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
}

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
        print(
            f"Warning: unsupported block type '{block_type}', treating as pass-through",
            file=sys.stderr,
        )
        return Block(block_type, block_name, block_uuid, param_values, block_path)

    return block_class(block_type, block_name, block_uuid, param_values, block_path)
