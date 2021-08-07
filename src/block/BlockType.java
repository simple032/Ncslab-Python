package block;

import org.json.JSONObject;

public class BlockType {
	public static Block createBlock(JSONObject blockJSON) {
		String blockType=blockJSON.getString("blockType");
		
		switch(blockType) {
		case "Scope":
			return new block.sink.Scope(blockJSON);
		case "PID Controller (s)":
			return new block.continuous.PIDController(blockJSON);
		case "WaterLevel":
			return new block.testrig.WaterLevel(blockJSON);
		case "Constant":
			return new block.source.Constant(blockJSON);
		case "Sum":
			return new block.math.Sum(blockJSON);
		}
		
		return null;
	}
}
