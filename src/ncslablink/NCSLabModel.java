package ncslablink;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;

import block.Block;
import block.BlockType;

import line.Line;

public class NCSLabModel {
	
	private JSONObject jsonIn;
	
	private String modelName;
	private String modelRealName;
	
	private Config config;
	
	private Vector<Block> blockList=new Vector<Block>();
	
	private Vector<Line> lineList=new Vector<Line>();
	
	NCSLabModel(JSONObject jsonIn){
		this.jsonIn=jsonIn;
		
		parseModel();
		
		System.out.println(config.getFixedStep());
	}
	
	public static NCSLabModel createFromJSON(JSONObject jsonIn) {
		NCSLabModel model=new NCSLabModel(jsonIn);
		
		return model;
	}
	
	public Vector<Block> getBlockList(){
		return this.blockList;
	}
	
	private void parseModel() {
		modelName=jsonIn.getString("modelName");
		modelRealName=jsonIn.getString("modelRealName");
		
		config=Config.createFromJSON(jsonIn.getJSONObject("config"));
		
		parseBlocks();
		
		parseLines();
		
		//showBlocks();
	}
	
	private void showBlocks() {
		for(Block block:blockList) {
			System.out.println("+++++++++++++++++++++++++++");
			System.out.println("Name: "+block.getBlockName());
			System.out.println("Type: "+block.getBlockType());
			System.out.println("In: "+block.getInputPortList().size()+" Out:"+block.getOutputPortList().size());
		}
	}
	
	private void parseBlocks() {
		
		JSONArray blockJSONList=jsonIn.getJSONArray("blocks");
		for(int i=0;i<blockJSONList.length();i++) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);
			//String blockType=blockJSON.getString("blockType");
			
			Block block=BlockType.createBlock(blockJSON);
			block.setBlockId(i+1);
			
			System.out.println("Parsing block ("+block.getBlockId()+"): '"+block.getBlockName()+"'...");
			
			if(block!=null) {
				blockList.add(block);
			}
			
		}
	}
	
	private void parseLines() {
		JSONArray lineJSONList=jsonIn.getJSONArray("lines");
		for(int i=0;i<lineJSONList.length();i++) {
			JSONObject lineJSON=lineJSONList.getJSONObject(i);
			
			Line line=Line.createLine(lineJSON, this);
			line.setLineId(i+1);
			
			System.out.println("Parsing line ("+line.getLineId()+"): '"+line.getLinkedOutputPort().getBLock().getBlockName()+"("+line.getLinkedOutputPort().getNumber()+")-->"+line.getLinkedInputPort().getBLock().getBlockName()+"("+line.getLinkedInputPort().getNumber()+")"); 
			
			if(line!=null) {
				lineList.add(line);
			}
		}
	}

}
