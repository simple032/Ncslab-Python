package ncslablink;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;

import block.Block;
import block.BlockType;

public class NCSLabModel {
	
	private JSONObject jsonIn;
	
	private String modelName;
	private String modelRealName;
	
	private Config config;
	
	private Vector<Block> blockList=new Vector<Block>();
	
	NCSLabModel(JSONObject jsonIn){
		this.jsonIn=jsonIn;
		
		parseModel();
		
		System.out.println(config.getFixedStep());
	}
	
	public static NCSLabModel createFromJSON(JSONObject jsonIn) {
		NCSLabModel model=new NCSLabModel(jsonIn);
		
		return model;
	}
	
	private void parseModel() {
		modelName=jsonIn.getString("modelName");
		modelRealName=jsonIn.getString("modelRealName");
		
		config=Config.createFromJSON(jsonIn.getJSONObject("config"));
		
		parseBlocks();
	}
	
	private void parseBlocks() {
		
		JSONArray blockJSONList=jsonIn.getJSONArray("blocks");
		for(int i=0;i<blockJSONList.length();i++) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);
			//String blockType=blockJSON.getString("blockType");
			
			Block block=BlockType.createBlock(blockJSON);
			
			if(block!=null) {
				blockList.add(block);
			}
			
		}
		
		System.out.println(blockList.size());
	}

}
