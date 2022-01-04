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
		case "Delay":
			block=new block.discrete.Delay(blockJSON, model);
			break;	
		//routing
		case "Mux":
			block=new block.route.Mux(blockJSON, model);
			break;	
		case "Demux":
			block=new block.route.Demux(blockJSON, model);
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
