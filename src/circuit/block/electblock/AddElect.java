package circuit.block.electblock;

import org.json.JSONObject;

import ncslablink.NCSLabModel;
import block.math.Add;

public class AddElect extends Add implements ElectBlock {
	public AddElect(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		return "1";
	}
}
