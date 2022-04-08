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
		case "Sum":
			block=new block.math.Sum(blockJSON,model);
			break;
		case "Gain":
			block=new block.math.Gain(blockJSON,model);
			break;
		case "Integrator":
			block=new block.continuous.Integrator(blockJSON,model);
			break;
		case "Transfer Fcn":
			block=new block.continuous.TransferFcn(blockJSON, model);
			break;
		case "newMotor":
			block=new block.testrig.NewMotor(blockJSON, model);
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
		case "UDPReceiver":
			block=new block.comm.UDPReceiver(blockJSON, model);
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
		//routing
		case "Mux":
			block=new block.route.Mux(blockJSON, model);
			break;	
		case "Demux":
			block=new block.route.Demux(blockJSON, model);
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
		}
		
		if(block==null) {
			throw(new ModelException("Can not find blocktype \""+blockType+"\""));
		}
		
		block.setBlockId(id);
		block.updateBlock();
		
		return block;
	}
}
