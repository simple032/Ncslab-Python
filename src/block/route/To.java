package block.route;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import ncslablink.NCSLabModel;

public class To extends Block {
	private String tagName;
	public To(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		tagName=paramValues.getString("GotoTag");
		//System.out.println(tagName);
	}
	
	public String getTagName() {
		return this.tagName;
	}
}
