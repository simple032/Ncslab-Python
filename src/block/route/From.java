package block.route;

import org.json.JSONObject;

import block.Block;
import block.io.OutputPort;
import ncslablink.NCSLabModel;

public class From extends Block {
	private String tagName;
	
	
	public From(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		outputPortList.add(new OutputPort(this,1,false));
		tagName=paramValues.getString("Tag");
	}
	
	public String getTagName() {
		return this.tagName;
	}
}
