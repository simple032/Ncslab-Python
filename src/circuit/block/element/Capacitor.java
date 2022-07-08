package circuit.block.element;

import org.json.JSONObject;

import circuit.block.CircuitBlock;
import ncslablink.NCSLabModel;

public class Capacitor extends CircuitBlock {
	public Capacitor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}
}
