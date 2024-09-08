package com.ncslab.circuit.block.electblock;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.math.Product;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class ProductElect extends Product implements ElectBlock {
	
	private String electLoopString;
	
	private Vector<Block> relatedBlockList=new Vector<Block>();
	
	public ProductElect(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		
		//return gain.getName();
		
		return inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}
	
	public void setElecLoopString(String electLoopString) {
		this.electLoopString=electLoopString;
	}
	
	public void setRelatedBlockList(Vector<Block> relatedBlockList) {
		this.relatedBlockList=relatedBlockList;
	}
	
	public Vector<Block> getRelatedBlockList(){
		return this.relatedBlockList;
	}
	
	public boolean isLoopPoint() {
		if(relatedBlockList.size()>0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		
		if(isLoopPoint()) {
			String outputCode="/*Code for output of block Product:("+getBlockId()+")"+getBlockName()+" (Circuit Algebraic Loop Point) */\n";
			
			outputCode+=electLoopString+";\n";
			
			code.addOutputCode(outputCode);
		}
		else {
			super.generateOutputCodeC(code);
		}
		
		
	}
	
	@Override
	public void clearLoop() {
		// TODO Auto-generated method stub
		relatedBlockList=new Vector<Block>();
		electLoopString=null;
	}
}

