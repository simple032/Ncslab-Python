package circuit.block.electblock;

import org.json.JSONObject;

import ncslablink.NCSLabModel;
import block.math.Gain;

public class GainElect extends Gain implements ElectBlock {
	public GainElect(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		return gain.getName();
	}
}
