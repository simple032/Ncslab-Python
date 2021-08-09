package code;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import ncslablink.NCSLabModel;

public class CodeModel extends NCSLabModel {
	
	protected Vector<Block> terminalBlockList=new Vector<Block>();
	
	CodeModel(JSONObject jsonIn){
		super(jsonIn);
	}
	
	public void generate() {
		
	}
	
	protected void findTerminalBlocks() {
		System.out.println("Looking for terminal blocks");
		for(Block block:blockList) {
			if(block.isTerminalBlock()) {
				System.out.println("Found ("+block.getBlockId()+"): "+block.getBlockName());
				terminalBlockList.add(block);
			}
		}
	}
}
