package circuit.block.element;

import org.json.JSONObject;

import circuit.block.BlockModeType;
import circuit.block.CircuitBlock;
import circuit.block.io.CircuitPort;
import ncslablink.NCSLabModel;

public class Inductor extends CircuitBlock {
	public Inductor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		blockModeType=BlockModeType.LinkOnly;
	}
}
