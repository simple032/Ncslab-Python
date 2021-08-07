package block;

import org.json.JSONObject;

public class Block {
	protected String blockType;
	protected String blockName;
	
	protected JSONObject paramValues;
	
	protected Block(JSONObject blockIn) {
		this.blockType=blockIn.getString("blockType");
		this.blockName=blockIn.getString("blockName");
		this.paramValues=blockIn.getJSONObject("paramValues");
		
	}
}
