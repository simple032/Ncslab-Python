package com.ncslab.circuit.block.electblock;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.math.Gain;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.Block;

public class GainElect extends Gain implements ElectBlock {
	private String electLoopString;
	
	private List<Block> relatedBlockList = new ArrayList<>();
	
	public GainElect(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		return gain.getName();
	}
	
	public void setElecLoopString(String electLoopString) {
		this.electLoopString=electLoopString;
	}
	
	public void setRelatedBlockList(List<Block> relatedBlockList) {
		this.relatedBlockList=relatedBlockList;
	}
	
	public List<Block> getRelatedBlockList(){
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
			String outputCode="/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+" (Circuit Algebraic Loop Point) */\n";
			
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
		relatedBlockList=new ArrayList<>();
		electLoopString=null;
	}
}
