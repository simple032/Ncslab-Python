package circuit.block;

import org.json.JSONObject;

import ncslablink.ModelException;
import ncslablink.NCSLabModel;

public class CircuitBlockType {
	/*根据BlockType的类型，生成不同的Block */
	public static CircuitBlock createBlock(int id,JSONObject blockJSON,NCSLabModel model) throws ModelException {
		CircuitBlock block=null;
		String blockType=blockJSON.getString("blockType");
		
		switch(blockType) {		
		case "DC Voltage Source":
			block=new circuit.block.element.DCVoltageSource(blockJSON, model);
			break;
		case "Resistor":
			block=new circuit.block.element.Resistor(blockJSON, model);
			break;
		case "Inductor":
			block=new circuit.block.element.Inductor(blockJSON, model);
			break;
		case "Capacitor":
			block=new circuit.block.element.Capacitor(blockJSON, model);
			break;
		case "Voltage Sensor":
			block=new circuit.block.element.VoltageSensor(blockJSON, model);
			break;
		case "Controlled Voltage Source":
			block=new circuit.block.element.ControlledVoltageSource(blockJSON, model);
			break;
		}
		
		if(block==null) {
			throw(new ModelException("Can not find blocktype \""+blockType+"\""));
		}
		
		//block.setBlockId(id);
		//block.updateBlock();
		
		return block;
	}
}
