package block;

import org.json.JSONObject;

import ncslablink.NCSLabModel;

import ncslablink.ModelException;

public class BlockType {
	/*根据BlockType的类型，生成不同的Block */
	public static Block createBlock(int id,JSONObject blockJSON,NCSLabModel model) throws ModelException {
		Block block=null;
		String blockType=blockJSON.getString("blockType");
		
		switch(blockType) {		
		//Sink
		case "Scope":
			block=new block.sink.Scope(blockJSON,model);
			break;
		case "Terminator":
			block=new block.sink.Terminator(blockJSON,model);
			break;
		//continuous
		case "PID Controller (s)":
			block=new block.continuous.PIDController(blockJSON,model);
			break;
		case "WaterLevel":
			block=new block.testrig.WaterLevel(blockJSON,model);
			break;
		//source
		case "Constant":
			block=new block.source.Constant(blockJSON,model);
			break;
		case "Clock":
			block=new block.source.Clock(blockJSON,model);
			break;	
		case "Sum":
			block=new block.math.Sum(blockJSON,model);
			break;
		case "Gain":
			block=new block.math.Gain(blockJSON,model);
			break;
		case "Derivative":
			block=new block.continuous.Derivative(blockJSON,model);
			break;
		case "Integrator":
			block=new block.continuous.Integrator(blockJSON,model);
			break;
		case "Transfer Fcn":
			block=new block.continuous.TransferFcn(blockJSON, model);
			break;
		case "VariableTransport Delay":
			block=new block.continuous.VariableTransportDelay(blockJSON, model);
			break;
		case "NewMotor":
			block=new block.testrig.NewMotor(blockJSON, model);
			break;
		case "InvertedPendulum":
			block=new block.testrig.InvertedPendulum(blockJSON, model);
			break;
		case "InvertedPendulumSUST":
			block=new block.testrig.InvertedPendulumSUST(blockJSON, model);
			break;
		case "EnergySwingUpInvertedPendulumSUST":
			block=new block.testrig.EnergySwingUpInvertedPendulumSUST(blockJSON, model);
			break;
		case "BangbangSwingUpInvertedPendulumSUST":
			block=new block.testrig.BangbangSwingUpInvertedPendulumSUST(blockJSON, model);
			break;
		case "xzInvertedPendulumSUST":
			block=new block.testrig.xzInvertedPendulumSUST(blockJSON, model);
			break;
		case "BallPlateSUST":
			block=new block.testrig.BallPlateSUST(blockJSON, model);
			break;
		case "BallBeamSystem":
			block=new block.testrig.BallBeamSystem(blockJSON, model);
			break;
		case "LoongarchPLC":
			block=new block.testrig.LoongarchPLC(blockJSON, model);
			break;
		case "MagneticLevitationSystem":
			block=new block.testrig.MagneticLevitationSystem(blockJSON, model);
			break;
		case "S-Function":
			block=new block.function.SFunction(blockJSON, model);
			break;
		case "S-FunctionBuilder":
			block=new block.function.SFunctionBuilder(blockJSON, model);
			break;
		case "UDPSender":
			block=new block.comm.UDPSender(blockJSON, model);
			break;
		case "UDPSend":
			block=new block.driver.UDPSend(blockJSON, model);
			break;
		case "UDPReceiver":
			block=new block.comm.UDPReceiver(blockJSON, model);
			break;
		case "UDPReceive":
			block=new block.driver.UDPReceive(blockJSON, model);
			break;
		//discrete
		case "DiscreteStateSpace":
			block=new block.discrete.DiscreteStateSpace(blockJSON, model);
			break;	
		case "Zero-Order Hold":
			block=new block.discrete.Zero_Order_Hold(blockJSON,model);
			break;
		case "Delay":
			block=new block.discrete.Delay(blockJSON,model);
			break;
		case "Unit Delay":
			block=new block.discrete.UnitDelay(blockJSON,model);
			break;
		case "Discrete-Time Integrator":
			block=new block.discrete.Discrete_Time_Integrator(blockJSON,model);
			break;
		case "Discrete Transfer Fcn":
			block=new block.discrete.Discrete_Transfer_Fcn(blockJSON,model);
			break;
		//routing
		case "Mux":
			block=new block.route.Mux(blockJSON, model);
			break;	
		case "Demux":
			block=new block.route.Demux(blockJSON, model);
			break;	
		case "Switch":
			block=new block.route.Switch(blockJSON, model);
			break;	
		case "From":
			block=new block.route.From(blockJSON, model);
			break;
		case "Goto":
			block=new block.route.To(blockJSON, model);
			break;
		case "State-Space":
			block=new block.continuous.StateSpace(blockJSON,model);
			break;
		case "Saturation":
			block=new block.discontinuous.Saturation(blockJSON,model);
			break;
		case "Relay":
			block=new block.discontinuous.Relay(blockJSON,model);
			break;
		case "Dead Zone":
			block=new block.discontinuous.DeadZone(blockJSON,model);
			break;
		case "Coulomb Viscous Friction":
			block=new block.discontinuous.Coulomb(blockJSON,model);
			break;
		case "RateLimiter":
			block=new block.discontinuous.RateLimiter(blockJSON,model);
			break;
		case "Backlash":
			block=new block.discontinuous.Backlash(blockJSON,model);
			break;
		case "Trigonometric Function":
			block=new block.math.TrigFunction(blockJSON,model);
			break;
		case "Add":
			block=new block.math.Add(blockJSON,model);
			break;
		case "Sign":
			block=new block.math.Sign(blockJSON,model);
			break;
		case "Product":
			block=new block.math.Product(blockJSON,model);
			break;
		case "Math Function":
			block=new block.math.MathFunction(blockJSON,model);
			break;
		case "TestPoint":
			block=new block.math.TestPoint(blockJSON,model);
			break;
		case "abc2dq":
			block=new block.math.abc2dq0(blockJSON, model);
			break;	
		case "dq02abc":
			block=new block.math.dq02abc(blockJSON, model);
			break;	
		case "SecondOrderFilter":
			block=new block.powerSystem.secondOrderFiliter(blockJSON, model);
			break;
		case "Abs":
			block=new block.math.Abs(blockJSON, model);
			break;	
		case "Bias":
			block=new block.math.Bias(blockJSON, model);
			break;
		case "Sqrt":
			block=new block.math.Sqrt(blockJSON, model);
			break;
		case "Compare To Constant":
			block=new block.logicAndBit.CompareToConstant(blockJSON,model);
			break;
		case "Relational Operator":
			block=new block.logicAndBit.RelationalOperator(blockJSON,model);
			break;
		case "Step":
			block=new block.source.Step(blockJSON,model);
			break;
		case "Pulse Generator":
			block=new block.source.Pulse(blockJSON,model);
			break;
		case "Repeating Sequence":
			block=new block.source.RepeatingSequence(blockJSON,model);
			break;
		case "Ramp":
			block=new block.source.Ramp(blockJSON,model);
			break;
		case "Sine Wave":
			block=new block.source.SineWave(blockJSON,model);
			break;
		case "In":
			block=new block.subsystem.In(blockJSON,model);
			break;
		case "Out":
			block=new block.subsystem.Out(blockJSON,model);
			break;	
		case "Subsystem":
			block=new block.subsystem.Subsystem(blockJSON,model);
			break;
		case "Ad":
			block=new block.driver.Ad(blockJSON, model);
			break;
		case "PWM":
			block=new block.driver.PWM(blockJSON, model);
			break;
		case "DA_Ouput":
			block=new block.driver.Da(blockJSON, model);
			break;
		//driver for stm32
		case "PWMForStm32":
			block=new block.driverForStm32.PWMForStm32(blockJSON, model);
			break;
		case "AD_Collect_Stm32":
			block=new block.driverForStm32.ADCForStm32(blockJSON, model);
			break;
		case "DA_Out_Stm32":
			block=new block.driverForStm32.DACForStm32(blockJSON, model);
			break;
		case "UDPReceiverForStm32":
			block=new block.driverForStm32.UDPReceiverForStm32(blockJSON, model);
			break;
		case "UDPSenderForStm32":
			block=new block.driverForStm32.UDPSenderForStm32(blockJSON, model);
			break;
		}
		
		
		if(block==null) {
			throw(new ModelException("Can not find blocktype \""+blockType+"\""));
		}
		
		block.setBlockId(id);
		block.updateBlock();
		
		return block;
	}
}
