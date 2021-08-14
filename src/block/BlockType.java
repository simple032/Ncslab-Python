package block;

import org.json.JSONObject;

import ncslablink.NCSLabModel;

public class BlockType {
	/*根据BlockType的类型，生成不同的Block */
	public static Block createBlock(JSONObject blockJSON,NCSLabModel model) {
		String blockType=blockJSON.getString("blockType");
		
		switch(blockType) {
		case "Scope":
			return new block.sink.Scope(blockJSON,model);
		case "PID Controller (s)":
			return new block.continuous.PIDController(blockJSON,model);
		case "WaterLevel":
			return new block.testrig.WaterLevel(blockJSON,model);
		case "Constant":
			return new block.source.Constant(blockJSON,model);
		case "Sum":
			return new block.math.Sum(blockJSON,model);
		}
		
		return null;
	}
}
