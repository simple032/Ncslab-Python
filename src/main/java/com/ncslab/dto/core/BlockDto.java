package com.ncslab.dto.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.block.Block;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.block.BlockDimensionDto;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.common.PortDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Base DTO class for all block types in the NCSLabLink system.
 * This class maps the core Block entity and serves as the foundation
 * for all specific block DTOs.
 * 
 * Provides type-safe, validated replacement for JSONObject-based block representation.
 * 
 * @author DTO Migration Framework
 * @version 1.0
 * @since DTO Migration Week 5
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "blockType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.ConstantDto.class, name = "Constant"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.StepDto.class, name = "Step"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.GainDto.class, name = "Gain"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.SumDto.class, name = "Sum"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.AddDto.class, name = "Add"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.AbsDto.class, name = "Abs"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.BiasDto.class, name = "Bias"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.DivideDto.class, name = "Divide"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ProductDto.class, name = "Product"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ReciprocalDto.class, name = "Reciprocal"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.SignDto.class, name = "Sign"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.SqrtDto.class, name = "Sqrt"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.MinMaxDto.class, name = "MinMax"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ReverseParkTransformDto.class, name = "Reverse Park Transform"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ParkTransformDto.class, name = "Park Transform"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.IntegratorDto.class, name = "Integrator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.DerivativeDto.class, name = "Derivative"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.TransferFcnDto.class, name = "Transfer Fcn"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.StateSpaceDto.class, name = "State-Space"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.StateSpaceDto.class, name = "StateSpace"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.ZeroOrderHoldDto.class, name = "ZeroOrderHold"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.UnitDelayDto.class, name = "UnitDelay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.UnitDelayDto.class, name = "Unit Delay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.DiscreteIntegratorDto.class, name = "DiscreteIntegrator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.ScopeDto.class, name = "Scope"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.TerminatorDto.class, name = "Terminator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.XYGraphDto.class, name = "XYGraph"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.StopSimulationDto.class, name = "StopSimulation"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.StopSimulationDto.class, name = "Stop Simulation"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.SwitchDto.class, name = "Switch"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.MultiportSwitchDto.class, name = "Multiport Switch"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.MultiportSwitchDto.class, name = "MultiportSwitch"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.ManualSwitchDto.class, name = "ManualSwitch"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.ManualSwitchDto.class, name = "Manual Switch"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.MergeDto.class, name = "Merge"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.IndexVectorDto.class, name = "IndexVector"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.IndexVectorDto.class, name = "Index Vector"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.BusCreatorDto.class, name = "BusCreator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.BusCreatorDto.class, name = "Bus Creator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.BusSelectorDto.class, name = "BusSelector"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.BusSelectorDto.class, name = "Bus Selector"),
    // Verification blocks
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.verification.AssertDto.class, name = "Assert"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.verification.CheckSignalAttributesDto.class, name = "CheckSignalAttributes"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.verification.CheckSignalAttributesDto.class, name = "Check Signal Attributes"),
    @JsonSubTypes.Type(value = com.ncslab.dto.communication.CircuitBlockDto.class, name = "CircuitBlock"),
    // New block DTOs - Week 6 Update
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.ClockDto.class, name = "Clock"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.DisplayDto.class, name = "Display"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.DisplayDto.class, name = "display"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.MatplotlibDto.class, name = "Matplotlib"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ExponentialDto.class, name = "Exponential"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.LogarithmDto.class, name = "Logarithm"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ModuloDto.class, name = "Modulo"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.PowerDto.class, name = "Power"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.RoundingDto.class, name = "Rounding"),
    // Additional Week 6 DTOs - Recently fixed imports
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.LogicalOperatorDto.class, name = "LogicalOperator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.LogicalOperatorDto.class, name = "Logical Operator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.BitwiseOperatorDto.class, name = "BitwiseOperator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.BitwiseOperatorDto.class, name = "Bitwise Operator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.RelationalOperatorDto.class, name = "RelationalOperator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.RelationalOperatorDto.class, name = "Relational Operator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.SineWaveDto.class, name = "SineWave"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.SineWaveDto.class, name = "Sin"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.SineWaveDto.class, name = "Sine"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.SineWaveDto.class, name = "Sine Wave"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.DelayDto.class, name = "Delay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.DemuxDto.class, name = "Demux"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.MuxDto.class, name = "Mux"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.PulseDto.class, name = "PulseGenerator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.PulseDto.class, name = "Pulse Generator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.PulseDto.class, name = "DiscretePulseGenerator"),
    // New DTOs for missing test blocks
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.MagneticLevitationSystemDto.class, name = "MagneticLevitationSystem"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.SubsystemDto.class, name = "Subsystem"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.InportDto.class, name = "Inport"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.InDto.class, name = "In"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.OutportDto.class, name = "Outport"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.OutDto.class, name = "Out"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.EnableDto.class, name = "Enable"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.TriggerDto.class, name = "Trigger"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.ForIteratorSubsystemDto.class, name = "ForIteratorSubsystem"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.subsystem.ForIteratorSubsystemDto.class, name = "For Iterator Subsystem"),
    // Additional block types from BlockType.java
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.PIDControllerDto.class, name = "PID Controller (s)"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.PIDControllerDto.class, name = "PID Controller"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.PIDControllerDto.class, name = "PIDController"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.SaturationDto.class, name = "Saturate"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.SaturationDto.class, name = "Saturation"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.RelayDto.class, name = "Relay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.DeadZoneDto.class, name = "DeadZone"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.DeadZoneDto.class, name = "Dead Zone"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.RateLimiterDto.class, name = "RateLimiter"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.BacklashDto.class, name = "Backlash"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.CoulombDto.class, name = "CoulombViscousFriction"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discontinuous.CoulombDto.class, name = "Coulomb Viscous Friction"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.RampDto.class, name = "Ramp"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.BandLimitedWhiteNoiseDto.class, name = "Band-LimitedWhiteNoise"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.BandLimitedWhiteNoiseDto.class, name = "Band-LimitedWhite Noise"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.BandLimitedWhiteNoiseDto.class, name = "Band-Limited White Noise"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.RepeatingSequenceDto.class, name = "RepeatingSequence"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.RepeatingSequenceDto.class, name = "Repeating Sequence"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.TrigFunctionDto.class, name = "TrigonometricFunction"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.TrigFunctionDto.class, name = "Trigonometric Function"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.MathFunctionDto.class, name = "MathFunction"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.MathFunctionDto.class, name = "Math Function"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.MathFunctionDto.class, name = "Math"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.TestPointDto.class, name = "TestPoint"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.Abc2dq0Dto.class, name = "abc2dq"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.Dq02abcDto.class, name = "dq02abc"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ProductOfElementsDto.class, name = "ProductOfElements"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.SumOfElementsDto.class, name = "SumOfElements"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ComplexToMagnitudeAngleDto.class, name = "ComplexToMagnitudeAngle"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.DotProductDto.class, name = "DotProduct"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.MatrixDto.class, name = "Matrix"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.ReciprocalSqrtDto.class, name = "ReciprocalSqrt"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.VariableTransportDelayDto.class, name = "VariableTransportDelay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.TransportDelayDto.class, name = "TransportDelay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.continuous.TransportDelayDto.class, name = "Transport Delay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.DiscreteStateSpaceDto.class, name = "DiscreteStateSpace"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.ZeroOrderHoldDto.class, name = "Zero-OrderHold"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.ZeroOrderHoldDto.class, name = "Zero-Order Hold"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.Discrete_Time_IntegratorDto.class, name = "Discrete_Time_Integrator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.Discrete_Time_IntegratorDto.class, name = "Discrete-TimeIntegrator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.Discrete_Time_IntegratorDto.class, name = "Discrete-Time Integrator"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.DiscreteTransferFcnDto.class, name = "DiscreteTransferFcn"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.DiscreteTransferFcnDto.class, name = "Discrete Transfer Fcn"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.Discrete_Transfer_FcnzDto.class, name = "DiscreteTransferFcn(z)"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.Discrete_Transfer_FcnzDto.class, name = "Discrete Transfer Fcn (z)"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.FromDto.class, name = "From"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.route.GotoDto.class, name = "Goto"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.lookupTable.OneDimensionLookupTableDto.class, name = "OneDimensionLookupTable"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.lookupTable.TwoDimensionLookupTableDto.class, name = "TwoDimensionLookupTable"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.powerSystem.SecondOrderFilterDto.class, name = "SecondOrderFilter"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.CompareToConstantDto.class, name = "CompareToConstant"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.CompareToConstantDto.class, name = "Compare To Constant"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.ShiftArithmeticDto.class, name = "ShiftArithmetic"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.ShiftArithmeticDto.class, name = "Shift Arithmetic"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.CompareToZeroDto.class, name = "CompareToZero"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.logic.CompareToZeroDto.class, name = "Compare To Zero"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.TransposeDto.class, name = "Transpose"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.CreateDiagonalMatrixDto.class, name = "CreateDiagonalMatrix"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.CrossProductDto.class, name = "CrossProduct"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.ExtractDiagonalDto.class, name = "ExtractDiagonal"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.IdentityMatrixDto.class, name = "IdentityMatrix"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.IsHermitianDto.class, name = "IsHermitian"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.IsSymmetricDto.class, name = "IsSymmetric"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.IsTriangularDto.class, name = "IsTriangular"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.MatrixMultiplyDto.class, name = "MatrixMultiply"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.MatrixConcatenateDto.class, name = "MatrixConcatenate"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.MatrixSquareDto.class, name = "MatrixSquare"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.HermitianTransposeDto.class, name = "HermitianTranspose"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.PermuteMatrixDto.class, name = "PermuteMatrix"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.SubmatrixDto.class, name = "Submatrix"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.advancedControl.LQRControllerDto.class, name = "LQRController"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.machineLearning.DataCollectorDto.class, name = "DataCollector"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.machineLearning.pt.LinearRegressionDto.class, name = "LinearRegression"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.machineLearning.pt.LogisticRegressionDto.class, name = "LogisticRegression"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.machineLearning.pt.MultilayerPerceptronDto.class, name = "MultilayerPerceptron"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.machineLearning.pt.CNNDto.class, name = "CNN1dModel"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.machineLearning.pt.A2CDto.class, name = "A2CBlock"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.math.FcnDto.class, name = "Fcn"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.function.SFunctionDto.class, name = "S-Function"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.function.SFunctionBuilderDto.class, name = "S-FunctionBuilder"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.comm.UDPSenderDto.class, name = "UDPSender"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.comm.UDPReceiverDto.class, name = "UDPReceiver"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.comm.UDPSenderDto.class, name = "UDPSend"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.comm.UDPReceiverDto.class, name = "UDPReceive"),
    // Serial communication blocks (legacy) - now in instrument package
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.instrument.SerialSenderDto.class, name = "SerialSender"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.instrument.SerialReceiverDto.class, name = "SerialReceiver"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.instrument.SerialBidirectionalDto.class, name = "SerialBidirectional"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.instrument.SerialConfigurationDto.class, name = "SerialConfiguration"),
    // Refactored serial blocks following Simulink R2024b architecture
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.instrument.SerialSendDto.class, name = "SerialSend"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.instrument.SerialReceiveDto.class, name = "SerialReceive"),
    // Data blocks - byte packing and type conversion
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.BytePackDto.class, name = "Byte pack"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.BytePackDto.class, name = "BytePack"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.ByteUnpackDto.class, name = "Byte Unpack"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.ByteUnpackDto.class, name = "ByteUnpack"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.DataTypeConversionDto.class, name = "DataTypeConversion"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.DataTypeConversionDto.class, name = "Data Type Conversion"),
    // Workspace blocks - data logging and playback
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.ToWorkspaceDto.class, name = "To Workspace"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.ToWorkspaceDto.class, name = "ToWorkspace"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.FromWorkspaceDto.class, name = "From Workspace"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.FromWorkspaceDto.class, name = "FromWorkspace"),
    // File blocks - MAT file I/O
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.ToFileDto.class, name = "To File"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.sink.ToFileDto.class, name = "ToFile"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.FromFileDto.class, name = "From File"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.source.FromFileDto.class, name = "FromFile"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.driver.EtherCATAIDto.class, name = "EtherCATAI"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.driver.EtherCATAODto.class, name = "EtherCATAO"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.driver.EtherCATDIDto.class, name = "EtherCATDI"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.driver.EtherCATDODto.class, name = "EtherCATDO"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.driver.EtherCATServoDto.class, name = "EtherCATservo"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.driver.ObserverDto.class, name = "Observer"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.rasp.ADDto.class, name = "AD"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.rasp.DADto.class, name = "DA"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.rasp.PWMDto.class, name = "PWM"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.rasp.GPIODto.class, name = "GPIO"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.PWMForStm32Dto.class, name = "PWMForStm32"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.stm32.ADCDto.class, name = "AD_Collect_Stm32"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.stm32.DACDto.class, name = "DA_Out_Stm32"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.stm32.UDPReceiverDto.class, name = "UDPReceiverForStm32"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.hardware.stm32.UDPSenderDto.class, name = "UDPSenderForStm32"),
    // Testrig blocks
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.WaterLevelDto.class, name = "WaterLevel"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.NewMotorDto.class, name = "newMotor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.NewMotorDto.class, name = "NewMotor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.DCMotorAngleDto.class, name = "DCMotorAngle"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.DCMotorAngleDirectDto.class, name = "DCMotorAngleNew"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.DCMotorAngleDirectDto.class, name = "DCMotorAngleDirect"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.ServoMotorSliderDto.class, name = "ServoMotorSlider"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.AlpDto.class, name = "ALP"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.AlpDto.class, name = "Alp"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.RaspFanDto.class, name = "Fans"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.RaspFanDto.class, name = "Fan"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.RaspFanDto.class, name = "FanRasp"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.InvertedPendulumDto.class, name = "InvertedPendulum"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.InvertedPendulumDto.class, name = "L1IP"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.InvertedPendulumSUSTDto.class, name = "InvertedPendulumSUST"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.EnergySwingUpInvertedPendulumSUSTDto.class, name = "EnergySwingUpInvertedPendulumSUST"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.BangbangSwingUpInvertedPendulumSUSTDto.class, name = "BangbangSwingUpInvertedPendulumSUST"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.xzInvertedPendulumSUSTDto.class, name = "xzInvertedPendulumSUST"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.BallPlateSUSTDto.class, name = "BallPlateSUST"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.BallPlateSUSTDto.class, name = "BallPlateSystemSUST"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.BallBeamSystemDto.class, name = "BallBeamSystem"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.LoongarchPLCDto.class, name = "LoongarchPLC"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.SubstitutionDto.class, name = "Substitution"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.SuperpositionDto.class, name = "Superposition"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.TelegenicDto.class, name = "Telegenic"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.KirchhoffDto.class, name = "Kirchhoff"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.DoubleTankDto.class, name = "DoubleTank"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.SecondOrderInvertedPendulumDto.class, name = "L2IP"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.RotaryInvertedPendulumDto.class, name = "R1IP"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.SecondOrderRotaryInvertedPendulumDto.class, name = "R2IP"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.BallPlateSystemDto.class, name = "BallPlateSystem"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.WaterLevelDto.class, name = "NetWaterLevel"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.HGGenerator7Dto.class, name = "HG Generator7"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.testrig.HGGenerator12Dto.class, name = "HG Generator12"),
    // Data processing blocks (added Week 6)
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.BytePackDto.class, name = "Byte pack"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.ByteUnpackDto.class, name = "Byte Unpack"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.data.DataTypeConversionDto.class, name = "DataTypeConversion"),
    // String blocks - text manipulation and string operations (Phases 1-4)
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.ASCIIToStringDto.class, name = "ASCIIToString"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.ComposeStringDto.class, name = "ComposeString"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.ScanStringDto.class, name = "ScanString"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringCompareDto.class, name = "StringCompare"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringConcatenateDto.class, name = "StringConcatenate"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringConstantDto.class, name = "StringConstant"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringContainsDto.class, name = "StringContains"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringFindDto.class, name = "StringFind"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringLengthDto.class, name = "StringLength"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringLowerDto.class, name = "StringLower"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringReplaceDto.class, name = "StringReplace"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringToASCIIDto.class, name = "StringToASCII"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringToDoubleDto.class, name = "StringToDouble"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringToEnumDto.class, name = "StringToEnum"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringToSingleDto.class, name = "StringToSingle"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringTrimDto.class, name = "StringTrim"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.StringUpperDto.class, name = "StringUpper"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.SubstringDto.class, name = "Substring"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.string.ToStringDto.class, name = "ToString"),
    // Data Store blocks (signal package)
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.DataStoreMemoryDto.class, name = "DataStoreMemory"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.DataStoreMemoryDto.class, name = "Data Store Memory"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.DataStoreReadDto.class, name = "DataStoreRead"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.DataStoreReadDto.class, name = "Data Store Read"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.DataStoreWriteDto.class, name = "DataStoreWrite"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.DataStoreWriteDto.class, name = "Data Store Write"),
    // Signal attribute blocks (Quick-Win implementation)
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.WidthDto.class, name = "Width"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.ProbeDto.class, name = "Probe"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.ICDto.class, name = "IC"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.SignalSpecificationDto.class, name = "SignalSpecification"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.signal.SignalSpecificationDto.class, name = "Signal Specification"),
    // Discrete blocks (Quick-Win implementation)
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.MemoryDto.class, name = "Memory"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.TappedDelayDto.class, name = "TappedDelay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.TappedDelayDto.class, name = "Tapped Delay"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.FirstOrderHoldDto.class, name = "FirstOrderHold"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.FirstOrderHoldDto.class, name = "First-Order Hold"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.discrete.FirstOrderHoldDto.class, name = "First Order Hold"),
    // Matrix blocks (Quick-Win implementation)
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.ReshapeDto.class, name = "Reshape"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.PermuteDimensionsDto.class, name = "PermuteDimensions"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.PermuteDimensionsDto.class, name = "Permute Dimensions"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.matrix.AssignmentDto.class, name = "Assignment"),
    // All DTOs with javax.validation issues now fixed!
    
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.ACVoltageSourceDto.class, name = "AC Voltage Source"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.DCVoltageSourceDto.class, name = "DC Voltage Source"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.DCCurrentSourceDto.class, name = "DC Current Source"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.ACCurrentSourceDto.class, name = "AC Current Source"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.ControlledVoltageSourceDto.class, name = "Controlled Voltage Source"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.ControlledCurrentSourceDto.class, name = "Controlled Current Source"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.SeriesRLCBranchDto.class, name = "SeriesRLCBranch"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.ResistorDto.class, name = "Resistor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.CapacitorDto.class, name = "Capacitor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.InductorDto.class, name = "Inductor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.VoltageSensorDto.class, name = "Voltage Sensor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.multielement.DiodeDto.class, name = "Diode"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.CircuitSwitchDto.class, name = "Circuit Switch"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.CurrentSensorDto.class, name = "Current Sensor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.VariableResistorDto.class, name = "Variable Resistor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.VariableInductorDto.class, name = "Variable Inductor"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.circuit2.element.VariableCapacitorDto.class, name = "Variable Capacitor"),
    // Stateflow Chart block
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.stateflow.StateflowChartDto.class, name = "StateflowChart"),
    @JsonSubTypes.Type(value = com.ncslab.dto.block.specialized.stateflow.StateflowChartDto.class, name = "Stateflow-chart-block")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@MigrationCompatible(originalClass = "com.ncslab.block.Block")
public class BlockDto implements BaseDto {
    
    // ===== CORE BLOCK IDENTIFICATION =====
    
    /**
     * Unique identifier for this block instance.
     * Corresponds to Block.blockId
     */
    protected Integer blockId;
    
    /**
     * The type of this block (e.g., "Constant", "Gain", "Sum", "Integrator").
     * This is handled by Jackson's @JsonTypeInfo and should not be a separate field.
     * Use getDtoType() to get the block type.
     */
    // @JsonProperty("blockType")  -- Removed to avoid conflict with @JsonTypeInfo
    // protected String blockType;  -- Removed - Jackson handles this via type discrimination
    
    /**
     * Human-readable name for this block instance.
     * Corresponds to Block.blockName
     */
    @JsonProperty("blockName")
    protected String blockName;
    
    /**
     * Unique UUID for this block.
     * Used for cross-reference and persistence.
     * Corresponds to Block.blockUUID
     */
    @JsonProperty("blockUUID")
    @Builder.Default
    protected String blockUUID = "null";
    
    /**
     * Path indicating this block's location in the model hierarchy.
     * Format: "modelName" or "modelName/subsystem/..."
     * Corresponds to Block.blockPath
     */
    @JsonProperty("blockPath")
    @Builder.Default
    protected String blockPath = "";
    
    /**
     * Source block reference for library blocks.
     * Optional field for blocks derived from libraries.
     */
    @JsonProperty("srcBlock")
    protected String srcBlock;
    
    // ===== POSITIONING AND LAYOUT =====
    
    /**
     * Visual position of the block in the UI.
     * Contains x, y coordinates and optional rotation/scaling.
     */
    protected BlockPositionDto position;

    /**
     * Visual dimensions of the block in the UI.
     * Contains width, height, and optional aspect ratio.
     */
    protected BlockDimensionDto dimension;
    
    /**
     * Visual appearance settings (color, icon, etc.).
     */
    protected AppearanceDto appearance;
    
    // ===== CONNECTIVITY =====
    
    /**
     * Input port definitions for this block.
     * Defines the data inputs this block accepts.
     */
    protected List<PortDto> inputPorts;
    
    /**
     * Output port definitions for this block.
     * Defines the data outputs this block produces.
     */
    protected List<PortDto> outputPorts;
    
    // ===== PARAMETERS AND CONFIGURATION =====
    
    /**
     * Block-specific parameters stored as typed parameters.
     * Modern replacement for JSONObject paramValues.
     */
    protected Map<String, TypedParameter> parameters;

    /**
     * Legacy parameter values for backward compatibility.
     * Used by legacy JSON parsing and some existing blocks.
     * Note: While typed parameters are preferred for new code,
     * this field remains necessary for backward compatibility.
     */
    @JsonProperty("paramValues")    
    protected Map<String, Object> paramValues;
    
    /**
     * Sample time for data collection/processing
     * -1: Inherited from connected block or model fixed step
     * 0: Continuous time (use model fixed step for data collection)
     * >0: Explicit discrete sample time
     */
    protected TypedParameter sampleTime;
    
    /**
     * Execution priority for this block.
     * Lower numbers execute first.
     */
    protected Integer priority;
    
    // ===== METADATA =====
    
    /**
     * Documentation and description for this block.
     */
    protected String description;
    
    /**
     * Tags for categorization and searching.
     */
    protected Set<String> tags;
    
    /**
     * Custom properties for extensibility.
     */
    protected Map<String, Object> customProperties;
    
    /**
     * Metadata for BaseDto implementation.
     */
    protected Map<String, Object> metadata;
    
    /**
     * Creation timestamp for auditing.
     */
    protected LocalDateTime createdAt;

    /**
     * Last modification timestamp.
     */
    protected LocalDateTime modifiedAt;
    
    /**
     * Version information for compatibility tracking.
     */
    protected String version;

    /*
     * 
     */
    protected boolean isHardware;
    
    // ===== INITIALIZATION =====

    protected BlockDto(String blockName, String blockPath) {
        // blockType is now handled by Jackson @JsonTypeInfo - no field assignment needed
        this.blockName = blockName;
        this.blockPath = blockPath;
        initializeCollections();
    }

    protected BlockDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimensions) {
        // blockType is now handled by Jackson @JsonTypeInfo - no field assignment needed
        this.blockName = blockName;
        this.blockPath = blockPath;
        this.position = position;
        this.dimension = dimensions;
        initializeCollections();
    }

    /**
     * Initialize collections to prevent null pointer exceptions.
     */
    protected void initializeCollections() {
        if (inputPorts == null) inputPorts = new ArrayList<>();
        if (outputPorts == null) outputPorts = new ArrayList<>();
        if (parameters == null) parameters = new HashMap<>();
        if (tags == null) tags = new HashSet<>();
        if (customProperties == null) customProperties = new HashMap<>();
        if (metadata == null) metadata = new HashMap<>();
    }
    

    // ===== PARAMETER ACCESS HELPERS =====
    
    /**
     * Get a parameter value by name with type safety.
     */
    public <T> T getParameterValue(String name, Class<T> type) {
        TypedParameter param = parameters != null ? parameters.get(name) : null;
        return param != null ? param.getValue(type) : null;
    }
    
    /**
     * Set a parameter value with type safety.
     */
    public void setParameterValue(String name, Object value) {
        if (parameters == null) parameters = new HashMap<>();
        parameters.put(name, TypedParameter.of(value));
        if (paramValues == null) paramValues = new HashMap<>();
        paramValues.put(name, value);
    }
    
    /**
     * Check if a parameter exists.
     */
    public boolean hasParameter(String name) {
        return parameters != null && parameters.containsKey(name);
    }
    
    /**
     * Get parameter with default value if not present.
     */
    public <T> T getParameterValue(String name, Class<T> type, T defaultValue) {
        T value = getParameterValue(name, type);
        return value != null ? value : defaultValue;
    }
    
    // ===== PORT ACCESS HELPERS =====
    
    /**
     * Get input port by index.
     */
    public PortDto getInputPort(int index) {
        return (inputPorts != null && index >= 0 && index < inputPorts.size()) 
               ? inputPorts.get(index) : null;
    }
    
    /**
     * Get output port by index.
     */
    public PortDto getOutputPort(int index) {
        return (outputPorts != null && index >= 0 && index < outputPorts.size()) 
               ? outputPorts.get(index) : null;
    }
    
    /**
     * Get number of input ports.
     */
    public int getInputPortCount() {
        return inputPorts != null ? inputPorts.size() : 0;
    }
    
    /**
     * Get number of output ports.
     */
    public int getOutputPortCount() {
        return outputPorts != null ? outputPorts.size() : 0;
    }

    // ===== LEGACY PARAMETER ACCESS HELPERS =====
    
    /**
     * Get parameter value from legacy paramValues or typed parameters
     */
    public Object getParam(String key) {
        // Check legacy paramValues first
        if (paramValues != null && paramValues.containsKey(key)) {
            return paramValues.get(key);
        }
        // Check typed parameters
        if (parameters != null && parameters.containsKey(key)) {
            TypedParameter param = parameters.get(key);
            return param != null ? param.getAsString() : null;
        }
        return null;
    }
    
    /**
     * Get parameter as string
     */
    public String getParamAsString(String key) {
        Object value = getParam(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Get parameter as double
     */
    public Double getParamAsDouble(String key) {
        Object value = getParam(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value != null ? Double.parseDouble(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Get parameter as integer
     */
    public Integer getParamAsInteger(String key) {
        Object value = getParam(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value != null ? Integer.parseInt(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Check if parameter exists
     */
    public boolean hasParam(String key) {
        return (paramValues != null && paramValues.containsKey(key)) ||
               (parameters != null && parameters.containsKey(key));
    }

    /**
     * Get the sample time parameter for this block
     */
    public TypedParameter getSampleTime() {
        return sampleTime;
    }
    
    /**
     * Check if sample time is inherited (-1)
     */
    public boolean isInheritedSampleTime() {
        return getSampleTime() != null && getSampleTime().getAsDouble() == -1.0;
    }
    
    /**
     * Check if sample time is discrete (positive value)
     */
    public boolean isDiscreteSampleTime() {
        return getSampleTime() != null && getSampleTime().getAsDouble() > 0.0;
    }
    
    /**
     * Get the delay value in seconds based on sample time
     */
    public double getDelayInSeconds() {
        if (isDiscreteSampleTime()) {
            return getSampleTime().getAsDouble(); // One sample delay
        }
        return Double.NaN; // Cannot determine without knowing inherited sample time
    }
    
    // ===== SIMPLE VALIDATION METHODS =====

    /**
     * Get simple validation error message for backward compatibility
     * @return error message or null if valid
     */
    public String getValidationError() {
        if (blockName == null || blockName.trim().isEmpty()) {
            return "Block name is required";
        }
        return null;
    }

    // ===== UTILITY METHODS =====
    
    /**
     * Create a deep copy of this block DTO.
     * Default implementation uses reflection - subclasses can override for performance.
     */
    public BlockDto copy() {
        // Default reflection-based copy - subclasses should override for performance
        try {
            @SuppressWarnings("unchecked")
            Class<? extends BlockDto> clazz = (Class<? extends BlockDto>) this.getClass();
            BlockDto copy = clazz.getDeclaredConstructor().newInstance();
            
            // Copy basic fields
            copy.blockId = this.blockId;
            // blockType is handled by class type - no field to copy
            copy.blockName = this.blockName;
            copy.blockPath = this.blockPath;
            copy.blockUUID = this.blockUUID;
            copy.sampleTime = this.sampleTime != null ? this.sampleTime.copy() : null;
            copy.position = this.position;
            copy.dimension = this.dimension;
            copy.description = this.description;
            copy.version = this.version;
            
            // Copy collections
            if (this.inputPorts != null) {
                copy.inputPorts = new ArrayList<>(this.inputPorts);
            }
            if (this.outputPorts != null) {
                copy.outputPorts = new ArrayList<>(this.outputPorts);
            }
            if (this.parameters != null) {
                copy.parameters = new HashMap<>();
                for (Map.Entry<String, TypedParameter> entry : this.parameters.entrySet()) {
                    copy.parameters.put(entry.getKey(), entry.getValue().copy());
                }
            }
            if (this.tags != null) {
                copy.tags = new HashSet<>(this.tags);
            }
            if (this.customProperties != null) {
                copy.customProperties = new HashMap<>(this.customProperties);
            }
            if (this.metadata != null) {
                copy.metadata = new HashMap<>(this.metadata);
            }
            
            return copy;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create copy of " + getClass().getSimpleName(), e);
        }
    }
    
    /**
     * Check if this DTO has a valid configuration for block operation.
     * Default implementation checks basic validation.
     * 
     * @return true if the configuration is valid and the block can operate correctly
     */
    public boolean isValidConfiguration() {
        return validate().isValid() && 
               getBlockType() != null && !getBlockType().trim().isEmpty() &&
               (parameters == null || parameters.values().stream().allMatch(p -> p == null || p.isValid()));
    }
    
    /**
     * Convert this DTO's parameters to a TypedParameterMap.
     * Default implementation returns the existing parameters map or empty map.
     * 
     * @return TypedParameterMap containing all block-specific parameters
     */
    public TypedParameterMap toParameterMap() {
        if (parameters != null) {
            TypedParameterMap result = new TypedParameterMap();
            for (Map.Entry<String, TypedParameter> entry : parameters.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }
        return new TypedParameterMap();
    }
    
    /**
     * Check if this block is compatible with another block for connections.
     */
    public boolean isCompatibleWith(BlockDto other) {
        // Basic compatibility check - subclasses can override
        return other != null && 
               getBlockType() != null && 
               other.getBlockType() != null;
    }
    
    /**
     * Get a summary string for debugging and logging.
     */
    public String getSummary() {
        return String.format("%s[id=%d, name='%s', type='%s', inputs=%d, outputs=%d]",
                getClass().getSimpleName(),
                blockId != null ? blockId : -1,
                blockName != null ? blockName : "unnamed",
                getBlockType() != null ? getBlockType() : "unknown",
                getInputPortCount(),
                getOutputPortCount());
    }
    
    // ===== VALIDATION HELPERS =====
    public List<String> validateParameters() {
        return new ArrayList<>();
    }
    /**
     * Validate block type against known types.
     */
    private boolean isValidBlockType(String type) {
        // Basic validation - could be enhanced with actual BlockType enum
        return type.matches("[A-Za-z][A-Za-z0-9_]*");
    }
    
    /**
     * Validate UUID format.
     */
    private boolean isValidUUID(String uuid) {
        if ("null".equals(uuid)) return true; // Legacy compatibility
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    // ===== VALIDATION METHODS (BaseDto Implementation) =====
    
    /**
     * Validation errors collected during validation process.
     */
    @Builder.Default
    protected List<String> validationErrors = new ArrayList<>();
    
    /**
     * Add a validation error to the list.
     * 
     * @param errorMessage Error message to add
     */
    protected void addValidationError(String errorMessage) {
        if (validationErrors == null) {
            validationErrors = new ArrayList<>();
        }
        validationErrors.add(errorMessage);
    }
    
    /**
     * Clear all validation errors.
     */
    protected void clearValidationErrors() {
        if (validationErrors != null) {
            validationErrors.clear();
        }
    }
    
    /**
     * Check if there are any validation errors.
     * 
     * @return true if there are validation errors, false otherwise
     */
    protected boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }
    
    @Override
    public ValidationResult validate() {
        clearValidationErrors();
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        String blockType = getBlockType();
        if (blockType == null || blockType.trim().isEmpty()) {
            result.addError("blockType", "Block type cannot be null or empty");
        }
        
        if (blockName == null || blockName.trim().isEmpty()) {
            result.addError("blockName", "Block name cannot be null or empty");
        }
        
        if (blockPath == null) {
            result.addWarning("blockPath", "Block path is null");
        }
        
        // Validate position if present
        if (position != null) {
            ValidationResult positionResult = position.validate();
            result.merge(positionResult);
        }
        
        // Validate dimension if present
        if (dimension != null) {
            ValidationResult dimensionResult = dimension.validate();
            result.merge(dimensionResult);
        }
        
        // Allow subclasses to add additional validation
        validateParameters();
        
        return result;
    }
    
    
    @Override
    public String getDtoType() {
        // Extract block type from class name (remove "Dto" suffix)
        String className = getClass().getSimpleName();
        return className.endsWith("Dto") ? className.substring(0, className.length() - 3) : className;
    }
    
    /**
     * Get the block type for this DTO.
     * This is derived from the class name and corresponds to the @JsonTypeName value.
     * 
     * @return the block type (e.g., "Constant", "Gain", "Sum")
     */
    public String getBlockType() {
        return getDtoType();
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }
    
    @Override
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }

    
}

/**
 * Position information for block layout.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
class PositionDto {
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private Double rotation;
    
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (x != null && !Double.isFinite(x)) {
            result.addError("Position X must be finite");
        }
        if (y != null && !Double.isFinite(y)) {
            result.addError("Position Y must be finite");
        }
        if (width != null && width <= 0) {
            result.addError("Width must be positive");
        }
        if (height != null && height <= 0) {
            result.addError("Height must be positive");
        }
        
        return result;
    }
}

/**
 * Appearance settings for block visualization.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
class AppearanceDto {
    private String backgroundColor;
    private String foregroundColor;
    private String iconPath;
    private Boolean showName;
    private String fontFamily;
    private Integer fontSize;
    
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        if (fontSize != null && fontSize <= 0) {
            result.addError("Font size must be positive");
        }
        
        return result;
    }
}