package block;

import org.json.JSONArray;
import org.json.JSONObject;

import ncslablink.NCSLabModel;

import ncslablink.ModelException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class BlockType {
	private static final HashMap<String, Class<? extends Block>> blockClassTree = new HashMap<>();
	private static final HashMap<String, Class<? extends NCSLabModel>> blockParsers = new HashMap<>();

	static {
		blockClassTree.put("Scope", block.sink.Scope.class);
		blockClassTree.put("Terminator", block.sink.Terminator.class);
		blockClassTree.put("PID Controller (s)", block.continuous.PIDController.class);
		blockClassTree.put("WaterLevel", block.testrig.WaterLevel.class);
		blockClassTree.put("Constant", block.source.Constant.class);
		blockClassTree.put("Clock", block.source.Clock.class);
		blockClassTree.put("Sum", block.math.Sum.class);
		blockClassTree.put("Gain", block.math.Gain.class);
		blockClassTree.put("Derivative", block.continuous.Derivative.class);
		blockClassTree.put("Integrator", block.continuous.Integrator.class);
		blockClassTree.put("Transfer Fcn", block.continuous.TransferFcn.class);
		blockClassTree.put("TransferFcn", block.continuous.TransferFcn.class);
		blockClassTree.put("VariableTransport Delay", block.continuous.VariableTransportDelay.class);
		blockClassTree.put("NewMotor", block.testrig.NewMotor.class);
		blockClassTree.put("InvertedPendulum", block.testrig.InvertedPendulum.class);
		blockClassTree.put("InvertedPendulumSUST", block.testrig.InvertedPendulumSUST.class);
		blockClassTree.put("EnergySwingUpInvertedPendulumSUST", block.testrig.EnergySwingUpInvertedPendulumSUST.class);
		blockClassTree.put("BangbangSwingUpInvertedPendulumSUST", block.testrig.BangbangSwingUpInvertedPendulumSUST.class);
		blockClassTree.put("xzInvertedPendulumSUST", block.testrig.xzInvertedPendulumSUST.class);
		blockClassTree.put("BallPlateSUST", block.testrig.BallPlateSUST.class);
		blockClassTree.put("BallBeamSystem", block.testrig.BallBeamSystem.class);
		blockClassTree.put("LoongarchPLC", block.testrig.LoongarchPLC.class);
		blockClassTree.put("MagneticLevitationSystem", block.testrig.MagneticLevitationSystem.class);
		blockClassTree.put("S-Function", block.function.SFunction.class);
		blockClassTree.put("S-FunctionBuilder", block.function.SFunctionBuilder.class);
		blockClassTree.put("UDPSender", block.comm.UDPSender.class);
		blockClassTree.put("UDPSend", block.driver.UDPSend.class);
		blockClassTree.put("UDPReceiver", block.comm.UDPReceiver.class);
		blockClassTree.put("UDPReceive", block.driver.UDPReceive.class);
		blockClassTree.put("DiscreteStateSpace", block.discrete.DiscreteStateSpace.class);
		blockClassTree.put("Zero-Order Hold", block.discrete.Zero_Order_Hold.class);
		blockClassTree.put("Delay", block.discrete.Delay.class);
		blockClassTree.put("Unit Delay", block.discrete.UnitDelay.class);
		blockClassTree.put("Discrete-Time Integrator", block.discrete.Discrete_Time_Integrator.class);
		blockClassTree.put("Discrete Transfer Fcn", block.discrete.Discrete_Transfer_Fcn.class);
		blockClassTree.put("Mux", block.route.Mux.class);
		blockClassTree.put("Demux", block.route.Demux.class);
		blockClassTree.put("Switch", block.route.Switch.class);
		blockClassTree.put("From", block.route.From.class);
		blockClassTree.put("Goto", block.route.To.class);
		blockClassTree.put("State-Space", block.continuous.StateSpace.class);
		blockClassTree.put("Saturation", block.discontinuous.Saturation.class);
		blockClassTree.put("Relay", block.discontinuous.Relay.class);
		blockClassTree.put("Dead Zone", block.discontinuous.DeadZone.class);
		blockClassTree.put("Coulomb Viscous Friction", block.discontinuous.Coulomb.class);
		blockClassTree.put("RateLimiter", block.discontinuous.RateLimiter.class);
		blockClassTree.put("Backlash", block.discontinuous.Backlash.class);
		blockClassTree.put("Trigonometric Function", block.math.TrigFunction.class);
		blockClassTree.put("Add", block.math.Add.class);
		blockClassTree.put("Sign", block.math.Sign.class);
		blockClassTree.put("Product", block.math.Product.class);
		blockClassTree.put("Math Function", block.math.MathFunction.class);
		blockClassTree.put("TestPoint", block.math.TestPoint.class);
		blockClassTree.put("abc2dq", block.math.abc2dq0.class);
		blockClassTree.put("dq02abc", block.math.dq02abc.class);
		blockClassTree.put("SecondOrderFilter", block.powerSystem.secondOrderFiliter.class);
		blockClassTree.put("Abs", block.math.Abs.class);
		blockClassTree.put("Bias", block.math.Bias.class);
		blockClassTree.put("Sqrt", block.math.Sqrt.class);
		blockClassTree.put("Compare To Constant", block.logicAndBit.CompareToConstant.class);
		blockClassTree.put("Relational Operator", block.logicAndBit.RelationalOperator.class);
		blockClassTree.put("Step", block.source.Step.class);
		blockClassTree.put("Pulse Generator", block.source.Pulse.class);
		blockClassTree.put("Repeating Sequence", block.source.RepeatingSequence.class);
		blockClassTree.put("Ramp", block.source.Ramp.class);
		blockClassTree.put("Sine Wave", block.source.SineWave.class);
		blockClassTree.put("In", block.subsystem.In.class);
		blockClassTree.put("Out", block.subsystem.Out.class);
		blockClassTree.put("Subsystem", block.subsystem.Subsystem.class);
		blockClassTree.put("Ad", block.driver.Ad.class);
		blockClassTree.put("PWM", block.driver.PWM.class);
		blockClassTree.put("DA_Ouput", block.driver.Da.class);
		blockClassTree.put("PWMForStm32", block.driverForStm32.PWMForStm32.class);
		blockClassTree.put("AD_Collect_Stm32", block.driverForStm32.ADCForStm32.class);
		blockClassTree.put("DA_Out_Stm32", block.driverForStm32.DACForStm32.class);
		blockClassTree.put("UDPReceiverForStm32", block.driverForStm32.UDPReceiverForStm32.class);
		blockClassTree.put("UDPSenderForStm32", block.driverForStm32.UDPSenderForStm32.class);
	}



	private static Block createBlockInstance(String blockType, JSONObject blockJSON, NCSLabModel model) {
		Block block = null;
		try {
			Class<? extends Block> blockClass = blockClassTree.get(blockType);
			block = blockClass.getConstructor(JSONObject.class, NCSLabModel.class).newInstance(blockJSON, model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return block;
	}

	/*根据BlockType的类型，生成不同的Block */
	public static Block createBlock(int id,JSONObject blockJSON,NCSLabModel model) throws ModelException {
		Block block=null;
		String blockType=blockJSON.getString("blockType");

		block = createBlockInstance(blockType, blockJSON, model);

		if(block==null) {
			throw(new ModelException("Can not find blocktype \""+blockType+"\""));
		}
		
		block.setBlockId(id);
		block.updateBlock();
		
		return block;
	}

	public static Set<String> getBlockTypes(){
		return blockClassTree.keySet();
	}

	public static JSONArray getBlockProperties(JSONArray blockTypes) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < blockTypes.length(); i++) {
			String blockType=blockTypes.getString(i);

			// 获取Block类的Class对象
			Class<? extends Block> blockClass = blockClassTree.get(blockType);

			// 调用静态方法
			Vector<String> parameterNames = (Vector<String>) blockClass.getMethod("getParameterNames").invoke(null); // 注意这里是null，因为是静态方法
			Vector<String> inputNames = (Vector<String>) blockClass.getMethod("getInputNames").invoke(null); // 注意这里是null，因为是静态方法
			Vector<String> outputNames = (Vector<String>) blockClass.getMethod("getOutputNames").invoke(null); // 注意这里是null，因为是静态方法

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
