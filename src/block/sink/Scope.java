package block.sink;

import org.json.JSONObject;

import block.io.InputPort;
import ncslablink.NCSLabModel;

public class Scope extends block.Block{
	public Scope(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);
		
		//“ª∏ˆ ‰»Î
		inputPortList.add(new InputPort(this,1));
	}
}
