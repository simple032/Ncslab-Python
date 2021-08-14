package block;

import org.json.JSONObject;

import ncslablink.NCSLabModel;

public class BlockType {
	/*根据BlockType的类型，生成不同的Block */
	public static Block createBlock(int id,JSONObject blockJSON,NCSLabModel model) {
		Block block=null;
		String blockType=blockJSON.getString("blockType");
		
		switch(blockType) {
		case "Scope":
			block=new block.sink.Scope(blockJSON,model);
			break;
		case "PID Controller (s)":
			block=new block.continuous.PIDController(blockJSON,model);
			break;
		case "WaterLevel":
			block=new block.testrig.WaterLevel(blockJSON,model);
			break;
		case "Constant":
			block=new block.source.Constant(blockJSON,model);
			break;
		case "Sum":
			block=new block.math.Sum(blockJSON,model);
			break;
		}
		
		block.setBlockId(id);
		block.updateBlock();
		
		return block;
	}
}
