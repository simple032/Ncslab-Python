package circuit.block.element;

import org.json.JSONObject;

import circuit.block.BlockModeType;
import circuit.block.CircuitBlock;
import ncslablink.NCSLabModel;

public class DCVoltageSource extends CircuitBlock {
	public DCVoltageSource(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		blockModeType=BlockModeType.BranchOnly;
	}
}
