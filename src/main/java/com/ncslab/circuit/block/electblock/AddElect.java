package com.ncslab.circuit.block.electblock;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;
import com.ncslab.block.math.Add;

public class AddElect extends Add implements ElectBlock {
	
	private String electLoopString;
	
	private List<Block> relatedBlockList = new ArrayList<>();
	
	public AddElect(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		return "1";
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

	@Override
	public void clearLoop() {
		// TODO Auto-generated method stub
		relatedBlockList=new ArrayList<>();
		electLoopString=null;
	}
}
