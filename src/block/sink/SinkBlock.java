package block.sink;

import org.json.JSONObject;

import block.Block;
import ncslablink.NCSLabModel;

public class SinkBlock extends Block {
	public SinkBlock(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);
	}
}
