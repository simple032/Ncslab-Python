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
import com.ncslab.ncslablink.ModelException;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.util.JsonUtils;
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
    private static final HashMap<String, Class<? extends NCSLabModel>> blockParsers = new HashMap<>();

    static {
        // Sink
        blockClassTree.put("Scope", com.ncslab.block.sink.Scope.class);
        blockClassTree.put("Terminator", com.ncslab.block.sink.Terminator.class);
        blockClassTree.put("Display", com.ncslab.block.sink.Display.class);
        blockClassTree.put("Matplotlib", com.ncslab.block.sink.Matplotlib.class);

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

        blockClassTree.put("Sum", com.ncslab.block.math.Sum.class);
        blockClassTree.put("Gain", com.ncslab.block.math.Gain.class);
        blockClassTree.put("TrigonometricFunction", com.ncslab.block.math.TrigFunction.class);
        blockClassTree.put("Add", com.ncslab.block.math.Add.class);
        blockClassTree.put("Sign", com.ncslab.block.math.Sign.class);
        blockClassTree.put("Product", com.ncslab.block.math.Product.class);
        blockClassTree.put("MathFunction", com.ncslab.block.math.MathFunction.class);
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

        // Continuous
        blockClassTree.put("Derivative", com.ncslab.block.continuous.Derivative.class);
        blockClassTree.put("Integrator", com.ncslab.block.continuous.Integrator.class);
        blockClassTree.put("TransferFcn", com.ncslab.block.continuous.TransferFcn.class);
        blockClassTree.put("VariableTransportDelay", com.ncslab.block.continuous.VariableTransportDelay.class);
        blockClassTree.put("PIDController(s)", com.ncslab.block.continuous.PIDController.class);
        blockClassTree.put("State-Space", com.ncslab.block.continuous.StateSpace.class);
        blockClassTree.put("TransportDelay", com.ncslab.block.continuous.TransportDelay.class);
        blockClassTree.put("PIDController", com.ncslab.block.continuous.OldPIDController.class);

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
        blockClassTree.put("NetWaterLevel", com.ncslab.block.testrig.WaterLevel.class);

        blockClassTree.put("BallPlateSystemSUST", com.ncslab.block.testrig.BallPlateSUST.class);

        // Function
        blockClassTree.put("Fcn", com.ncslab.block.function.Fcn.class);
        blockClassTree.put("S-Function", com.ncslab.block.function.SFunction.class);
        blockClassTree.put("S-FunctionBuilder", com.ncslab.block.function.SFunctionBuilder.class);

        // Comm
        blockClassTree.put("UDPSender", com.ncslab.block.comm.UDPSender.class);
        blockClassTree.put("UDPReceiver", com.ncslab.block.comm.UDPReceiver.class);

        // Driver
        blockClassTree.put("UDPSend", com.ncslab.block.driver.UDPSend.class);
        blockClassTree.put("UDPReceive", com.ncslab.block.driver.UDPReceive.class);
        blockClassTree.put("EtherCATAI", com.ncslab.block.driver.EtherCATAI.class);
        blockClassTree.put("EtherCATAO", com.ncslab.block.driver.EtherCATAO.class);
        blockClassTree.put("EtherCATDI", com.ncslab.block.driver.EtherCATDI.class);
        blockClassTree.put("EtherCATDO", com.ncslab.block.driver.EtherCATDO.class);
        blockClassTree.put("EtherCATservo", com.ncslab.block.driver.EtherCATservo.class);
        blockClassTree.put("Observer", com.ncslab.block.driver.Observer.class);

        // Discrete
        blockClassTree.put("DiscreteStateSpace", com.ncslab.block.discrete.DiscreteStateSpace.class);
        blockClassTree.put("Zero-OrderHold", com.ncslab.block.discrete.Zero_Order_Hold.class);
        blockClassTree.put("Delay", com.ncslab.block.discrete.Delay.class);
        blockClassTree.put("UnitDelay", com.ncslab.block.discrete.UnitDelay.class);
        blockClassTree.put("Discrete-TimeIntegrator", com.ncslab.block.discrete.Discrete_Time_Integrator.class);
        blockClassTree.put("DiscreteTransferFcn", com.ncslab.block.discrete.Discrete_Transfer_Fcn.class);
        blockClassTree.put("DiscreteTransferFcn(z)", com.ncslab.block.discrete.Discrete_Transfer_Fcnz.class);

        // Route
        blockClassTree.put("Mux", com.ncslab.block.route.Mux.class);
        blockClassTree.put("Demux", com.ncslab.block.route.Demux.class);
        blockClassTree.put("Switch", com.ncslab.block.route.Switch.class);
        blockClassTree.put("From", com.ncslab.block.route.From.class);
        blockClassTree.put("Goto", com.ncslab.block.route.To.class);

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

        // LogicAndBit
        blockClassTree.put("CompareToConstant", com.ncslab.block.logicAndBit.CompareToConstant.class);
        blockClassTree.put("RelationalOperator", com.ncslab.block.logicAndBit.RelationalOperator.class);
        blockClassTree.put("ShiftArithmetic", com.ncslab.block.logicAndBit.ShiftArithmetic.class);
        blockClassTree.put("LogicalOperator", com.ncslab.block.logicAndBit.LogicOperator.class);
        blockClassTree.put("CompareToZero", com.ncslab.block.logicAndBit.CompareToZero.class);
        blockClassTree.put("Transpose", com.ncslab.block.matrix.Transpose.class);

        // Subsystem
        blockClassTree.put("In", com.ncslab.block.subsystem.In.class);
        blockClassTree.put("Out", com.ncslab.block.subsystem.Out.class);
        blockClassTree.put("Inport", com.ncslab.block.subsystem.In.class);
        blockClassTree.put("Outport", com.ncslab.block.subsystem.Out.class);
        blockClassTree.put("Subsystem", com.ncslab.block.subsystem.Subsystem.class);
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

    }

	public static Block createBlock(int id, JSONObject blockJSON, NCSLabModel model) throws ModelException {
        String blockType = blockJSON.getString("blockType")
            .replace("Block", "")
            .replace(" ", "")
            .replace("\n","");

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
	
	/**
	 * High-performance DTO factory method using OptimizedBlockFactory
	 * @param id Block ID
	 * @param blockDto BlockDto DTO
	 * @param model NCSLabModel instance
	 * @return Block instance created with optimal performance
	 * @throws ModelException if block creation fails
	 */
	public static Block createBlockFromDto(int id, BlockDto blockDto, NCSLabModel model) throws ModelException {
		// Use optimized factory for better performance
		return OptimizedBlockFactory.createOptimizedBlock(id, blockDto, model);
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
