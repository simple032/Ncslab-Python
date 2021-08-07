package block.sink;

import org.json.JSONObject;

import block.io.InputPort;

public class Scope extends block.Block{
	public Scope(JSONObject scopeIn) {
		super(scopeIn);
		
		//“ª∏ˆ ‰»Î
		inputPortList.add(new InputPort(this,1));
	}
}
