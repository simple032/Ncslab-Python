package circuit.block.element;

import org.json.JSONObject;

import circuit.block.CircuitBlock;
import ncslablink.NCSLabModel;

public class Resistor extends CircuitBlock {
	public Resistor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}
}
