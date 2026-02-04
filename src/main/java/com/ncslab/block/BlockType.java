package com.ncslab.block;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ncslab.block.hardware.rasp.GPIO;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.util.JsonUtils;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.element.InterCircuitBlock;
/**
 * Generate corresponding <code>Block</code> according to <code>BlockType</code>.
 * If your block is an instance of <code>Block</code> but not an instance of <code>SoughtedBlock</code>,
 * ples put the operation manually in the hashmap <code>blockFactory</code>;
 * If your block is an instance of <code>SoughtedBlock</code>, you do not need to do any operation here,
 * but follow the javadoc mentioned in <code>SearchableBlock</code>
 * @see SearchableBlock
 */
@Slf4j
public class BlockType{
	/**
	 * put key-value set into the map.
	 * @param key instance of String, corresponding to that in the frontend.
	 * @param value
	 */
	public static void put(String key, Class<? extends Block> value){
        blockClassTree.put(key, value);
	}

	/**
	 * Check whether the key you want to use has been used in the map.
	 * @param key the key you want to use
	 * @return whether the key has already exist in the map. Exists, return true; else return false.
	 */
	public static boolean isKeyExist(String key){
		return blockClassTree.containsKey(key);
	}

    private static final HashMap<String, Class<? extends Block>> blockClassTree = new HashMap<>();
    private static final HashMap<String, Class<? extends CircuitBlock>> circuitBlockClassTree = new HashMap<>();
    private static final HashMap<String, Class<? extends NCSLabModel>> blockParsers = new HashMap<>();

    static {
        // Sink
        blockClassTree.put("Scope", com.ncslab.block.sink.Scope.class);
        blockClassTree.put("Terminator", com.ncslab.block.sink.Terminator.class);
        blockClassTree.put("Display", com.ncslab.block.sink.Display.class);
        blockClassTree.put("Matplotlib", com.ncslab.block.sink.Matplotlib.class);
        blockClassTree.put("XYGraph", com.ncslab.block.sink.XYGraph.class);
        blockClassTree.put("StopSimulation", com.ncslab.block.sink.StopSimulation.class);
        blockClassTree.put("Stop Simulation", com.ncslab.block.sink.StopSimulation.class);

        // Verification
        blockClassTree.put("Assert", com.ncslab.block.verification.Assert.class);
        blockClassTree.put("CheckSignalAttributes", com.ncslab.block.verification.CheckSignalAttributes.class);
        blockClassTree.put("Check Signal Attributes", com.ncslab.block.verification.CheckSignalAttributes.class);

        // Source
        blockClassTree.put("Constant", com.ncslab.block.source.Constant.class);
        blockClassTree.put("Clock", com.ncslab.block.source.Clock.class);
        blockClassTree.put("Step", com.ncslab.block.source.Step.class);
        blockClassTree.put("PulseGenerator", com.ncslab.block.source.Pulse.class);
        blockClassTree.put("DiscretePulseGenerator", com.ncslab.block.source.Pulse.class);
        blockClassTree.put("RepeatingSequence", com.ncslab.block.source.RepeatingSequence.class);
        blockClassTree.put("Ramp", com.ncslab.block.source.Ramp.class);
        blockClassTree.put("SineWave", com.ncslab.block.source.SineWave.class);
        blockClassTree.put("Sin", com.ncslab.block.source.SineWave.class);
        blockClassTree.put("Band-LimitedWhiteNoise", com.ncslab.block.source.BandLimitedWhiteNoise.class);
        blockClassTree.put("Band-Limited White Noise", com.ncslab.block.source.BandLimitedWhiteNoise.class);

        blockClassTree.put("Sum", com.ncslab.block.math.Sum.class);
        blockClassTree.put("Gain", com.ncslab.block.math.Gain.class);
        blockClassTree.put("TrigonometricFunction", com.ncslab.block.math.TrigFunction.class);
        blockClassTree.put("Add", com.ncslab.block.math.Add.class);
        blockClassTree.put("Sign", com.ncslab.block.math.Sign.class);
        blockClassTree.put("Product", com.ncslab.block.math.Product.class);
        blockClassTree.put("MathFunction", com.ncslab.block.math.MathFunction.class);
        blockClassTree.put("Math Function", com.ncslab.block.math.MathFunction.class);
        blockClassTree.put("Math", com.ncslab.block.math.MathFunction.class);
        blockClassTree.put("TestPoint", com.ncslab.block.math.TestPoint.class);
        blockClassTree.put("abc2dq", com.ncslab.block.math.abc2dq0.class);
        blockClassTree.put("dq02abc", com.ncslab.block.math.dq02abc.class);
        blockClassTree.put("Abs", com.ncslab.block.math.Abs.class);
        blockClassTree.put("Bias", com.ncslab.block.math.Bias.class);
        blockClassTree.put("Sqrt", com.ncslab.block.math.Sqrt.class);
        blockClassTree.put("MinMax", com.ncslab.block.math.MinMax.class);
        blockClassTree.put("Divide", com.ncslab.block.math.Divide.class);
        blockClassTree.put("Power", com.ncslab.block.math.Power.class);
        blockClassTree.put("ProductOfElements", com.ncslab.block.math.ProductOfElements.class);
        blockClassTree.put("SumOfElements", com.ncslab.block.math.SumOfElements.class);
        blockClassTree.put("Rounding", com.ncslab.block.math.Rounding.class);
        blockClassTree.put("Logarithm", com.ncslab.block.math.Logarithm.class);
        blockClassTree.put("Exponential", com.ncslab.block.math.Exponential.class);
        blockClassTree.put("Modulo", com.ncslab.block.math.Modulo.class);
        blockClassTree.put("Reciprocal", com.ncslab.block.math.Reciprocal.class);
        blockClassTree.put("ReciprocalSqrt", com.ncslab.block.math.ReciprocalSqrt.class);
        blockClassTree.put("DotProduct", com.ncslab.block.math.DotProduct.class);
        blockClassTree.put("ComplexToMagnitudeAngle", com.ncslab.block.math.ComplexToMagnitudeAngle.class);
        blockClassTree.put("ReverseParkTransform", com.ncslab.block.math.ReverseParkTransform.class);
        blockClassTree.put("ParkTransform", com.ncslab.block.math.ParkTransform.class);

        // Continuous
        blockClassTree.put("Derivative", com.ncslab.block.continuous.Derivative.class);
        blockClassTree.put("Integrator", com.ncslab.block.continuous.Integrator.class);
        blockClassTree.put("TransferFcn", com.ncslab.block.continuous.TransferFcn.class);
        blockClassTree.put("VariableTransportDelay", com.ncslab.block.continuous.VariableTransportDelay.class);
        blockClassTree.put("PIDController(s)", com.ncslab.block.continuous.PIDController.class);
        blockClassTree.put("State-Space", com.ncslab.block.continuous.StateSpace.class);
        blockClassTree.put("TransportDelay", com.ncslab.block.continuous.TransportDelay.class);
        blockClassTree.put("PIDController", com.ncslab.block.continuous.PIDController.class);

        // Testrig
        blockClassTree.put("WaterLevel", com.ncslab.block.testrig.WaterLevel.class);
        blockClassTree.put("newMotor", com.ncslab.block.testrig.NewMotor.class);
        blockClassTree.put("DCMotorAngle", com.ncslab.block.testrig.DCMotorAngle.class);
        blockClassTree.put("DCMotorAngleNew", com.ncslab.block.testrig.DCMotorAngleDirect.class);
        blockClassTree.put("ServoMotorSlider", com.ncslab.block.testrig.ServoMotorSlider.class);
        blockClassTree.put("ALP", com.ncslab.block.testrig.Alp.class);
        blockClassTree.put("Fans", com.ncslab.block.testrig.RaspFan.class);
        blockClassTree.put("NewMotor", com.ncslab.block.testrig.NewMotor.class);
        blockClassTree.put("InvertedPendulum", com.ncslab.block.testrig.InvertedPendulum.class);
        blockClassTree.put("InvertedPendulumSUST", com.ncslab.block.testrig.InvertedPendulumSUST.class);
        blockClassTree.put("EnergySwingUpInvertedPendulumSUST", com.ncslab.block.testrig.EnergySwingUpInvertedPendulumSUST.class);
        blockClassTree.put("BangbangSwingUpInvertedPendulumSUST", com.ncslab.block.testrig.BangbangSwingUpInvertedPendulumSUST.class);
        blockClassTree.put("xzInvertedPendulumSUST", com.ncslab.block.testrig.xzInvertedPendulumSUST.class);
        blockClassTree.put("BallPlateSUST", com.ncslab.block.testrig.BallPlateSUST.class);
        blockClassTree.put("BallBeamSystem", com.ncslab.block.testrig.BallBeamSystem.class);
        blockClassTree.put("LoongarchPLC", com.ncslab.block.testrig.LoongarchPLC.class);
        blockClassTree.put("MagneticLevitationSystem", com.ncslab.block.testrig.MagneticLevitationSystem.class);
        blockClassTree.put("Substitution", com.ncslab.block.testrig.Substitution.class);
        blockClassTree.put("Superposition", com.ncslab.block.testrig.Superposition.class);
        blockClassTree.put("Telegenic", com.ncslab.block.testrig.Telegenic.class);
        blockClassTree.put("Kirchhoff", com.ncslab.block.testrig.Kirchhoff.class);
        blockClassTree.put("DoubleTank", com.ncslab.block.testrig.DoubleTank.class);
        blockClassTree.put("Fan", com.ncslab.block.testrig.RaspFan.class);

        blockClassTree.put("L1IP", com.ncslab.block.testrig.InvertedPendulum.class);
        blockClassTree.put("L2IP", com.ncslab.block.testrig.SecondOrderInvertedPendulum.class);
        blockClassTree.put("R1IP", com.ncslab.block.testrig.RotaryInvertedPendulum.class);
        blockClassTree.put("R2IP", com.ncslab.block.testrig.SecondOrderRotaryInvertedPendulum.class);
        blockClassTree.put("BallPlateSystem", com.ncslab.block.testrig.BallPlateSystem.class);
        blockClassTree.put("FanRasp", com.ncslab.block.testrig.RaspFan.class);
        blockClassTree.put("RaspFan", com.ncslab.block.testrig.RaspFan.class);  // DTO compatibility
        blockClassTree.put("SecondOrderInvertedPendulum", com.ncslab.block.testrig.SecondOrderInvertedPendulum.class);  // DTO compatibility  
        blockClassTree.put("SecondOrderRotaryInvertedPendulum", com.ncslab.block.testrig.SecondOrderRotaryInvertedPendulum.class);  // DTO compatibility
        blockClassTree.put("NetWaterLevel", com.ncslab.block.testrig.WaterLevel.class);

        blockClassTree.put("BallPlateSystemSUST", com.ncslab.block.testrig.BallPlateSUST.class);
        
        blockClassTree.put("HGGenerator7", com.ncslab.block.testrig.HGGenerator7.class);
        blockClassTree.put("HGGenerator12", com.ncslab.block.testrig.HGGenerator12.class);

        // Function
        blockClassTree.put("Fcn", com.ncslab.block.function.Fcn.class);
        blockClassTree.put("S-Function", com.ncslab.block.function.SFunction.class);
        blockClassTree.put("S-FunctionBuilder", com.ncslab.block.function.SFunctionBuilder.class);

        // Comm
        blockClassTree.put("UDPSender", com.ncslab.block.comm.UDPSender.class);
        blockClassTree.put("UDPReceiver", com.ncslab.block.comm.UDPReceiver.class);

        // Instrument (serial port blocks)
        blockClassTree.put("SerialSender", com.ncslab.block.instrument.SerialSender.class);
        blockClassTree.put("SerialReceiver", com.ncslab.block.instrument.SerialReceiver.class);
        blockClassTree.put("SerialBidirectional", com.ncslab.block.instrument.SerialBidirectional.class);
        blockClassTree.put("SerialConfiguration", com.ncslab.block.instrument.SerialConfiguration.class);
        // Refactored serial blocks following Simulink R2024b architecture
        blockClassTree.put("SerialSend", com.ncslab.block.instrument.SerialSend.class);
        blockClassTree.put("SerialReceive", com.ncslab.block.instrument.SerialReceive.class);
        // Legacy aliases for backward compatibility
        blockClassTree.put("Serial", com.ncslab.block.instrument.SerialBidirectional.class);  // Alias for bidirectional

        // Driver
        blockClassTree.put("UDPSend", com.ncslab.block.driver.UDPSend.class);
        blockClassTree.put("UDPReceive", com.ncslab.block.driver.UDPReceive.class);
        blockClassTree.put("EtherCATAI", com.ncslab.block.driver.EtherCATAI.class);
        blockClassTree.put("EtherCATAO", com.ncslab.block.driver.EtherCATAO.class);
        blockClassTree.put("EtherCATDI", com.ncslab.block.driver.EtherCATDI.class);
        blockClassTree.put("EtherCATDO", com.ncslab.block.driver.EtherCATDO.class);
        blockClassTree.put("EtherCATservo", com.ncslab.block.driver.EtherCATservo.class);
        blockClassTree.put("Observer", com.ncslab.block.driver.Observer.class);

        // Data blocks - byte packing and type conversion
        blockClassTree.put("Byte pack", com.ncslab.block.instrument.BytePack.class);
        blockClassTree.put("BytePack", com.ncslab.block.instrument.BytePack.class);
        blockClassTree.put("Byte Unpack", com.ncslab.block.instrument.ByteUnpack.class);
        blockClassTree.put("ByteUnpack", com.ncslab.block.instrument.ByteUnpack.class);
        blockClassTree.put("DataTypeConversion", com.ncslab.block.instrument.DataTypeConversion.class);
        blockClassTree.put("Data Type Conversion", com.ncslab.block.instrument.DataTypeConversion.class);

        // Workspace blocks - data logging and playback
        blockClassTree.put("To Workspace", com.ncslab.block.sink.ToWorkspace.class);
        blockClassTree.put("ToWorkspace", com.ncslab.block.sink.ToWorkspace.class);
        blockClassTree.put("From Workspace", com.ncslab.block.source.FromWorkspace.class);
        blockClassTree.put("FromWorkspace", com.ncslab.block.source.FromWorkspace.class);

        // File blocks - MAT file I/O
        blockClassTree.put("To File", com.ncslab.block.sink.ToFile.class);
        blockClassTree.put("ToFile", com.ncslab.block.sink.ToFile.class);
        blockClassTree.put("From File", com.ncslab.block.source.FromFile.class);
        blockClassTree.put("FromFile", com.ncslab.block.source.FromFile.class);

        // Discrete
        blockClassTree.put("DiscreteStateSpace", com.ncslab.block.discrete.DiscreteStateSpace.class);
        blockClassTree.put("Zero-OrderHold", com.ncslab.block.discrete.Zero_Order_Hold.class);
        blockClassTree.put("FirstOrderHold", com.ncslab.block.discrete.FirstOrderHold.class);
        blockClassTree.put("First-Order Hold", com.ncslab.block.discrete.FirstOrderHold.class);
        blockClassTree.put("Delay", com.ncslab.block.discrete.Delay.class);
        blockClassTree.put("UnitDelay", com.ncslab.block.discrete.UnitDelay.class);
        blockClassTree.put("Memory", com.ncslab.block.discrete.Memory.class);
        blockClassTree.put("TappedDelay", com.ncslab.block.discrete.TappedDelay.class);
        blockClassTree.put("Tapped Delay", com.ncslab.block.discrete.TappedDelay.class);
        blockClassTree.put("Discrete-TimeIntegrator", com.ncslab.block.discrete.Discrete_Time_Integrator.class);
        blockClassTree.put("DiscreteTransferFcn", com.ncslab.block.discrete.Discrete_Transfer_Fcn.class);
        blockClassTree.put("DiscreteTransferFcn(z)", com.ncslab.block.discrete.Discrete_Transfer_Fcnz.class);
        // DTO compatibility mappings (class name format)
        blockClassTree.put("Discrete_Time_Integrator", com.ncslab.block.discrete.Discrete_Time_Integrator.class);
        blockClassTree.put("Discrete_Transfer_Fcnz", com.ncslab.block.discrete.Discrete_Transfer_Fcnz.class);
        blockClassTree.put("Zero_Order_Hold", com.ncslab.block.discrete.Zero_Order_Hold.class);

        // Route
        blockClassTree.put("Mux", com.ncslab.block.route.Mux.class);
        blockClassTree.put("Demux", com.ncslab.block.route.Demux.class);
        blockClassTree.put("Switch", com.ncslab.block.route.Switch.class);
        blockClassTree.put("Multiport Switch", com.ncslab.block.route.MultiportSwitch.class);
        blockClassTree.put("ManualSwitch", com.ncslab.block.route.ManualSwitch.class);
        blockClassTree.put("Manual Switch", com.ncslab.block.route.ManualSwitch.class);
        blockClassTree.put("Merge", com.ncslab.block.route.Merge.class);
        blockClassTree.put("IndexVector", com.ncslab.block.route.IndexVector.class);
        blockClassTree.put("Index Vector", com.ncslab.block.route.IndexVector.class);
        blockClassTree.put("From", com.ncslab.block.route.From.class);
        blockClassTree.put("Goto", com.ncslab.block.route.To.class);
        blockClassTree.put("BusCreator", com.ncslab.block.route.BusCreator.class);
        blockClassTree.put("Bus Creator", com.ncslab.block.route.BusCreator.class);
        blockClassTree.put("BusSelector", com.ncslab.block.route.BusSelector.class);
        blockClassTree.put("Bus Selector", com.ncslab.block.route.BusSelector.class);

        // Discontinuous
        blockClassTree.put("Saturation", com.ncslab.block.discontinuous.Saturation.class);
        blockClassTree.put("Saturate", com.ncslab.block.discontinuous.Saturation.class);
        blockClassTree.put("Relay", com.ncslab.block.discontinuous.Relay.class);
        blockClassTree.put("DeadZone", com.ncslab.block.discontinuous.DeadZone.class);
        blockClassTree.put("CoulombViscousFriction", com.ncslab.block.discontinuous.Coulomb.class);
        blockClassTree.put("RateLimiter", com.ncslab.block.discontinuous.RateLimiter.class);
        blockClassTree.put("Backlash", com.ncslab.block.discontinuous.Backlash.class);

        blockClassTree.put("OneDimensionLookupTable", com.ncslab.block.lookupTable.OneDimensionLookupTableBlock.class);
        blockClassTree.put("TwoDimensionLookupTable", com.ncslab.block.lookupTable.TwoDimensionLookupTableBlock.class);

        // PowerSystem
        blockClassTree.put("SecondOrderFilter", com.ncslab.block.powerSystem.secondOrderFiliter.class);
        
        // Additional alias mappings for database compatibility
        // These map database block type names to existing registry entries
        blockClassTree.put("StateSpace", com.ncslab.block.continuous.StateSpace.class);  // maps to "State-Space"
        blockClassTree.put("Coulomb", com.ncslab.block.discontinuous.Coulomb.class);  // maps to "CoulombViscousFriction"
        blockClassTree.put("ZeroOrderHold", com.ncslab.block.discrete.Zero_Order_Hold.class);  // maps to "Zero-OrderHold"
        blockClassTree.put("DiscreteTimeIntegrator", com.ncslab.block.discrete.Discrete_Time_Integrator.class);  // maps to "Discrete-TimeIntegrator"
        blockClassTree.put("DiscreteTransferFcnz", com.ncslab.block.discrete.Discrete_Transfer_Fcnz.class);  // maps to "DiscreteTransferFcn(z)"
        blockClassTree.put("TrigFunction", com.ncslab.block.math.TrigFunction.class);  // maps to "TrigonometricFunction"
        blockClassTree.put("Pulse", com.ncslab.block.source.Pulse.class);  // maps to "PulseGenerator"
        blockClassTree.put("BandLimitedWhiteNoise", com.ncslab.block.source.BandLimitedWhiteNoise.class);  // maps to "Band-LimitedWhiteNoise"
        blockClassTree.put("RotaryInvertedPendulum", com.ncslab.block.testrig.RotaryInvertedPendulum.class);  // maps to "R1IP"
        blockClassTree.put("Alp", com.ncslab.block.testrig.Alp.class);  // maps to "ALP"
        blockClassTree.put("DCMotorAngleDirect", com.ncslab.block.testrig.DCMotorAngleDirect.class);  // maps to "DCMotorAngleNew"

        // LogicAndBit
        blockClassTree.put("CompareToConstant", com.ncslab.block.logicAndBit.CompareToConstant.class);
        blockClassTree.put("RelationalOperator", com.ncslab.block.logicAndBit.RelationalOperator.class);
        blockClassTree.put("ShiftArithmetic", com.ncslab.block.logicAndBit.ShiftArithmetic.class);
        blockClassTree.put("LogicalOperator", com.ncslab.block.logicAndBit.LogicOperator.class);
        blockClassTree.put("BitwiseOperator", com.ncslab.block.logicAndBit.BitwiseOperator.class);
        blockClassTree.put("CompareToZero", com.ncslab.block.logicAndBit.CompareToZero.class);
        blockClassTree.put("Transpose", com.ncslab.block.matrix.Transpose.class);

        // Signal attribute blocks
        blockClassTree.put("Width", com.ncslab.block.signal.Width.class);
        blockClassTree.put("Probe", com.ncslab.block.signal.Probe.class);
        blockClassTree.put("IC", com.ncslab.block.signal.IC.class);
        blockClassTree.put("SignalSpecification", com.ncslab.block.signal.SignalSpecification.class);
        blockClassTree.put("Signal Specification", com.ncslab.block.signal.SignalSpecification.class);

        // Data Store blocks
        blockClassTree.put("DataStoreMemory", com.ncslab.block.signal.DataStoreMemory.class);
        blockClassTree.put("Data Store Memory", com.ncslab.block.signal.DataStoreMemory.class);
        blockClassTree.put("DataStoreRead", com.ncslab.block.signal.DataStoreRead.class);
        blockClassTree.put("Data Store Read", com.ncslab.block.signal.DataStoreRead.class);
        blockClassTree.put("DataStoreWrite", com.ncslab.block.signal.DataStoreWrite.class);
        blockClassTree.put("Data Store Write", com.ncslab.block.signal.DataStoreWrite.class);

        // Subsystem
        blockClassTree.put("In", com.ncslab.block.subsystem.In.class);
        blockClassTree.put("Out", com.ncslab.block.subsystem.Out.class);
        blockClassTree.put("Inport", com.ncslab.block.subsystem.In.class);
        blockClassTree.put("Outport", com.ncslab.block.subsystem.Out.class);
        blockClassTree.put("Subsystem", com.ncslab.block.subsystem.Subsystem.class);
        blockClassTree.put("Enable", com.ncslab.block.subsystem.Enable.class);
        blockClassTree.put("Trigger", com.ncslab.block.subsystem.Trigger.class);
        blockClassTree.put("ActionPort", com.ncslab.block.subsystem.ActionPort.class);
        blockClassTree.put("FunctionCallGenerator", com.ncslab.block.subsystem.FunctionCallGenerator.class);
        blockClassTree.put("FunctionCallSubsystem", com.ncslab.block.subsystem.FunctionCallSubsystem.class);
        blockClassTree.put("ForIteratorSubsystem", com.ncslab.block.subsystem.ForIteratorSubsystem.class);
        blockClassTree.put("WhileIteratorSubsystem", com.ncslab.block.subsystem.WhileIteratorSubsystem.class);
        blockClassTree.put("EnabledSubsystem", com.ncslab.block.subsystem.EnabledSubsystem.class);
        blockClassTree.put("TriggeredSubsystem", com.ncslab.block.subsystem.TriggeredSubsystem.class);
        blockClassTree.put("EnabledAndTriggeredSubsystem", com.ncslab.block.subsystem.EnabledAndTriggeredSubsystem.class);
        // Subsystem aliases for alternative naming conventions
        blockClassTree.put("EnableBlock", com.ncslab.block.subsystem.Enable.class);
        blockClassTree.put("TriggerBlock", com.ncslab.block.subsystem.Trigger.class);
        blockClassTree.put("ActionPortBlock", com.ncslab.block.subsystem.ActionPort.class);
        blockClassTree.put("FunctionCallGeneratorBlock", com.ncslab.block.subsystem.FunctionCallGenerator.class);
        blockClassTree.put("FunctionCallSubsystemBlock", com.ncslab.block.subsystem.FunctionCallSubsystem.class);
        blockClassTree.put("ForIteratorSubsystemBlock", com.ncslab.block.subsystem.ForIteratorSubsystem.class);
        blockClassTree.put("WhileIteratorSubsystemBlock", com.ncslab.block.subsystem.WhileIteratorSubsystem.class);
        blockClassTree.put("EnabledSubsystemBlock", com.ncslab.block.subsystem.EnabledSubsystem.class);
        blockClassTree.put("TriggeredSubsystemBlock", com.ncslab.block.subsystem.TriggeredSubsystem.class);
        blockClassTree.put("EnabledAndTriggeredSubsystemBlock", com.ncslab.block.subsystem.EnabledAndTriggeredSubsystem.class);
        // Matrix
        blockClassTree.put("CreateDiagonalMatrix", com.ncslab.block.matrix.CreateDiagonalMatrix.class);
        blockClassTree.put("CrossProduct", com.ncslab.block.matrix.CrossProduct.class);
        blockClassTree.put("ExtractDiagonal", com.ncslab.block.matrix.ExtractDiagonal.class);
        blockClassTree.put("IdentityMatrix", com.ncslab.block.matrix.IdentityMatrix.class);
        blockClassTree.put("IsHermitian", com.ncslab.block.matrix.IsHermitian.class);
        blockClassTree.put("IsSymmetric", com.ncslab.block.matrix.IsSymmetric.class);
        blockClassTree.put("IsTriangular", com.ncslab.block.matrix.IsTriangular.class);
        blockClassTree.put("MatrixMultiply", com.ncslab.block.matrix.MatrixMultiply.class);
        blockClassTree.put("MatrixConcatenate", com.ncslab.block.matrix.MatrixConcatenate.class);
        blockClassTree.put("MatrixSquare", com.ncslab.block.matrix.MatrixSquare.class);
        blockClassTree.put("PermuteMatrix", com.ncslab.block.matrix.PermuteMatrix.class);
        blockClassTree.put("Submatrix", com.ncslab.block.matrix.Submatrix.class);
        blockClassTree.put("PermuteDimensions", com.ncslab.block.matrix.PermuteDimensions.class);
        blockClassTree.put("Assignment", com.ncslab.block.matrix.Assignment.class);
        blockClassTree.put("Reshape", com.ncslab.block.matrix.Reshape.class);

        // Advanced Control
        blockClassTree.put("LQRController", com.ncslab.block.advancedControl.LQRController.class);

        // machine learning
        blockClassTree.put("DataCollector", com.ncslab.block.machineLearning.DataCollector.class);

        // Machine Learning
        blockClassTree.put("LinearRegression", com.ncslab.block.machineLearning.pt.LinearRegression.class);
        blockClassTree.put("LogisticRegression", com.ncslab.block.machineLearning.pt.LogisticRegression.class);
        blockClassTree.put("MultilayerPerceptron", com.ncslab.block.machineLearning.pt.MultilayerPerceptron.class);
        blockClassTree.put("CNN1dModel", com.ncslab.block.machineLearning.pt.CNN.class);
        blockClassTree.put("A2CBlock", com.ncslab.block.machineLearning.pt.A2C.class);

        // String - text manipulation and serial communication
        blockClassTree.put("ASCIIToString", com.ncslab.block.string.ASCIIToString.class);
        blockClassTree.put("ComposeString", com.ncslab.block.string.ComposeString.class);
        blockClassTree.put("ScanString", com.ncslab.block.string.ScanString.class);
        blockClassTree.put("StringCompare", com.ncslab.block.string.StringCompare.class);
        blockClassTree.put("StringConcatenate", com.ncslab.block.string.StringConcatenate.class);
        blockClassTree.put("StringConstant", com.ncslab.block.string.StringConstant.class);
        blockClassTree.put("StringContains", com.ncslab.block.string.StringContains.class);
        blockClassTree.put("StringFind", com.ncslab.block.string.StringFind.class);
        blockClassTree.put("StringLength", com.ncslab.block.string.StringLength.class);
        blockClassTree.put("StringLower", com.ncslab.block.string.StringLower.class);
        blockClassTree.put("StringReplace", com.ncslab.block.string.StringReplace.class);
        blockClassTree.put("StringToASCII", com.ncslab.block.string.StringToASCII.class);
        blockClassTree.put("StringToDouble", com.ncslab.block.string.StringToDouble.class);
        blockClassTree.put("StringToEnum", com.ncslab.block.string.StringToEnum.class);
        blockClassTree.put("StringToSingle", com.ncslab.block.string.StringToSingle.class);
        blockClassTree.put("StringTrim", com.ncslab.block.string.StringTrim.class);
        blockClassTree.put("StringUpper", com.ncslab.block.string.StringUpper.class);
        blockClassTree.put("Substring", com.ncslab.block.string.Substring.class);
        blockClassTree.put("ToString", com.ncslab.block.string.ToString.class);

        // Hardware
        // Raspberry
        blockClassTree.put("AD", com.ncslab.block.hardware.rasp.AD.class);
        blockClassTree.put("DA", com.ncslab.block.hardware.rasp.DA.class);
        blockClassTree.put("PWM", com.ncslab.block.hardware.rasp.PWM.class);
        blockClassTree.put("GPIO", com.ncslab.block.hardware.rasp.GPIO.class);

        // DriverForSTM32
        blockClassTree.put("PWMForStm32", com.ncslab.block.hardware.stm32.PWM.class);
        blockClassTree.put("AD_Collect_Stm32", com.ncslab.block.hardware.stm32.ADC.class);
        blockClassTree.put("DA_Out_Stm32", com.ncslab.block.hardware.stm32.DAC.class);
        blockClassTree.put("UDPReceiverForStm32", com.ncslab.block.hardware.stm32.UDPReceiver.class);
        blockClassTree.put("UDPSenderForStm32", com.ncslab.block.hardware.stm32.UDPSender.class);
        
        //Elect
        circuitBlockClassTree.put("ACVoltageSource", com.ncslab.circuit2.block.element.ACVoltageSource.class);
        circuitBlockClassTree.put("DCVoltageSource", com.ncslab.circuit2.block.element.DCVoltageSource.class);
        circuitBlockClassTree.put("DCCurrentSource", com.ncslab.circuit2.block.element.DCCurrentSource.class);
        circuitBlockClassTree.put("ACCurrentSource", com.ncslab.circuit2.block.element.ACCurrentSource.class);
        circuitBlockClassTree.put("ControlledVoltageSource", com.ncslab.circuit2.block.element.ControlledVoltageSource.class);
        circuitBlockClassTree.put("ControlledCurrentSource", com.ncslab.circuit2.block.element.ControlledCurrentSource.class);
        circuitBlockClassTree.put("Resistor", com.ncslab.circuit2.block.element.Resistor.class);
        circuitBlockClassTree.put("Capacitor", com.ncslab.circuit2.block.element.Capacitor.class);
        circuitBlockClassTree.put("Inductor", com.ncslab.circuit2.block.element.Inductor.class);
        circuitBlockClassTree.put("VoltageSensor", com.ncslab.circuit2.block.element.VoltageSensor.class);
        circuitBlockClassTree.put("CurrentSensor", com.ncslab.circuit2.block.element.CurrentSensor.class);
        circuitBlockClassTree.put("Diode", com.ncslab.circuit2.block.multielement.Diode.class);
        circuitBlockClassTree.put("CircuitSwitch", com.ncslab.circuit2.block.element.CircuitSwitch.class);
        circuitBlockClassTree.put("VariableResistor", com.ncslab.circuit2.block.element.VariableResistor.class);
        circuitBlockClassTree.put("VariableInductor", com.ncslab.circuit2.block.element.VariableInductor.class);
        circuitBlockClassTree.put("VariableCapacitor", com.ncslab.circuit2.block.element.VariableCapacitor.class);
        circuitBlockClassTree.put("SeriesRLCBranch", com.ncslab.circuit2.block.element.SeriesRLCBranch.class);
    }

	public static Block createBlock(int id, JSONObject blockJSON, NCSLabModel model) throws ModelException {
        String blockType = blockJSON.getString("blockType")
            .replace("Block", "")
            .replace(" ", "")
            .replace("\n ","");

        Block block = null;
        try {
            Class<? extends Block> blockClass = blockClassTree.get(blockType);
            if(blockClass != null) {
//                TODO: change the interface to fromJSON.
//                try {
//                    // Try to use the new fromJSON factory method first
//                    java.lang.reflect.Method fromJSONMethod = blockClass.getMethod("fromJSON", JSONObject.class, NCSLabModel.class);
//                    block = (Block) fromJSONMethod.invoke(null, blockJSON, model);
//                } catch (NoSuchMethodException e) {
                    // Fall back to deprecated constructor if fromJSON method doesn't exist
//                    log.warn("Block type '{}' does not have fromJSON method, using deprecated constructor", blockType);
            		
                    block = blockClass.getConstructor(JSONObject.class, NCSLabModel.class).newInstance(blockJSON, model);
//                }
            }
        } catch (Exception ee){
            log.error("Error creating block of type '{}': ", blockType, ee);
        }
        if(block == null)
            throw(new ModelException("Can not find blocktype \" "+ blockType+ " \" in mapped function"));

        block.setBlockId(id);
        block.updateBlock();

		return block;
		// return null;
	}
	
	public static CircuitBlock createCircuitBlock(int id, java.util.concurrent.atomic.AtomicInteger blockSeqCounter,JSONObject blockJSON, NCSLabModel model,NCSLabSystem targetSystem) throws ModelException {
        String blockType = blockJSON.getString("blockType")
            .replace("Block", "")
            .replace(" ", "")
            .replace("\t", "")
            .replace("\n ","");

        CircuitBlock block = null;
        try {
            Class<? extends CircuitBlock> blockClass = circuitBlockClassTree.get(blockType);
            if(blockClass != null) {
//                TODO: change the interface to fromJSON.
//                try {
//                    // Try to use the new fromJSON factory method first
//                    java.lang.reflect.Method fromJSONMethod = blockClass.getMethod("fromJSON", JSONObject.class, NCSLabModel.class);
//                    block = (Block) fromJSONMethod.invoke(null, blockJSON, model);
//                } catch (NoSuchMethodException e) {
                    // Fall back to deprecated constructor if fromJSON method doesn't exist
//                    log.warn("Block type '{}' does not have fromJSON method, using deprecated constructor", blockType);
            		if(InterCircuitBlock.class.isAssignableFrom(blockClass)) {
            			block = blockClass.getConstructor(JSONObject.class, NCSLabModel.class, NCSLabSystem.class,java.util.concurrent.atomic.AtomicInteger.class).newInstance(blockJSON, model,targetSystem,blockSeqCounter);
            		}
            		//if(block instanceof InterCircuitBlock) {
            		//	block = blockClass.getConstructor(JSONObject.class, NCSLabModel.class, NCSLabSystem.class).newInstance(blockJSON, model,targetSystem);
            		//}
            		else {
            			block = blockClass.getConstructor(JSONObject.class, NCSLabModel.class).newInstance(blockJSON, model);
            		}
                    
//                }
            }
        } catch (Exception ee){
            log.error("Error creating block of type '{}': ", blockType, ee);
        }
        if(block == null)
            throw(new ModelException("Can not find blocktype \" "+ blockType+ " \" in mapped function"));

        block.setBlockId(id);
        //block.updateBlock();

		return block;
		// return null;
	}
	
    public static Set<String> getBlockTypes(){
        return blockClassTree.keySet();
    }

    public static JSONArray getBlockProperties(JSONArray blockTypes) throws  NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        JSONArray jsonArray = new JSONArray();
        for (Object blockType : blockTypes) {

            // 获取Block类的Class对象
            Class<? extends Block> blockClass = blockClassTree.get(((String)blockType).replace(" ",""));

            if(blockClass == null){
                log.warn("Block type not found:{}", blockType);
                continue;
            }
            // 调用静态方法
            Set<?> parameterNames = null;
            try {
                // Try to get PARAMETER_DEFAULTS map and extract keys
                java.util.Map<?, ?> parameterDefaults = (java.util.Map<?, ?>) blockClass.getField("PARAMETER_DEFAULTS").get(null);
                parameterNames = parameterDefaults.keySet();
            } catch (Exception e) {
                // Fallback to empty set if PARAMETER_DEFAULTS doesn't exist
                parameterNames = new java.util.HashSet<>();
            }
            List<?> inputNames = (List<?>) blockClass.getMethod("getInputNames").invoke(null); // 注意这里是null，因为是静态方法
            List<?> outputNames = (List<?>) blockClass.getMethod("getOutputNames").invoke(null); // 注意这里是null，因为是静态方法

            JSONObject jo = new JSONObject();
            jo.put("type", blockType);
            jo.put("params", parameterNames);
            jo.put("inputs", inputNames);
            jo.put("outputs", outputNames);
            jsonArray.put(jo);
        }
        return jsonArray;
    }
}
