package block;

import org.json.JSONObject;

public class BlockType {
	public static Block createBlock(JSONObject blockJSON) {
		String blockType=blockJSON.getString("blockType");
		
		switch(blockType) {
		case "Scope":
			return new block.sink.Scope(blockJSON);
		}
		
		return null;
	}
}
