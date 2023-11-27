package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.math.Add;
import com.ncslab.block.math.Gain;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.io.BlockVoltage;
import com.ncslab.circuit.block.io.PortCurrent;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.circuit.block.electblock.*;

public class Resistor extends CircuitBlock {
	
	private GainElect gain;
	private AddElect add;
	
	public Resistor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
	}
	
	protected void setupBlockList() {
		switch(this.blockMode) {
		case Branch:
			setupBranchBlockList();
			break;
		case Link:
			setupLinkBlockList();
			break;
		}
	}
	
	private void setupBranchBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"' as a branch");
		
		String R=paramValues.getString("R");
		JSONObject gainJSON=new JSONObject();
		gainJSON.put("blockType", "Gain");
		gainJSON.put("blockName", this.blockName+"_R");
		gainJSON.put("blockPath", this.blockPath);
		JSONObject gainParamValues=new JSONObject();
		gainParamValues.put("Gain", R);
		gainParamValues.put("Multiplication", "Element-wise(K.*u)");
		gainJSON.put("paramValues", gainParamValues);
		
		gain=new GainElect(gainJSON,this.model);
		this.blockList.add(gain);
		
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Add");
		addJSON.put("blockName", this.blockName+"_Add");
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		String inputs="";
		for(PortCurrent current:this.getCurcuitPortList().get(0).getCurrentList()) {
			if(current.getSign()) {
				inputs+="+";
			}
			else {
				inputs+="-";
			}
		}
		addParamValues.put("Inputs", inputs);
		addJSON.put("paramValues", addParamValues);
		
		add=new AddElect(addJSON,this.model);
		this.blockList.add(add);
		
		createLine(add.getBlockName(), 1, gain.getBlockName(), 1);
		
		this.setOutputBlock(gain);
		this.setupInputBlock(add);
	}
	
	private void setupLinkBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"' as a link");
		
		String R=paramValues.getString("R");
		JSONObject gainJSON=new JSONObject();
		gainJSON.put("blockType", "Gain");
		gainJSON.put("blockName", this.blockName+"_R");
		gainJSON.put("blockPath", this.blockPath);
		JSONObject gainParamValues=new JSONObject();
		gainParamValues.put("Gain", "1.0/("+R+")");
		gainParamValues.put("Multiplication", "Element-wise(K.*u)");
		gainJSON.put("paramValues", gainParamValues);
		
		gain=new GainElect(gainJSON,this.model);
		this.blockList.add(gain);
		
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Add");
		addJSON.put("blockName", this.blockName+"_Add");
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		String inputs="";
		for(BlockVoltage voltage:getVoltageList()) {
			if(voltage.getSign()) {
				inputs+="+";
			}
			else {
				inputs+="-";
			}
		}
		addParamValues.put("Inputs", inputs);
		addJSON.put("paramValues", addParamValues);
		
		add=new AddElect(addJSON,this.model);
		this.blockList.add(add);
		
		createLine(add.getBlockName(), 1, gain.getBlockName(), 1);
		
		this.setOutputBlock(gain);
		this.setupInputBlock(add);
	}
	
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.Anything;
	}
}
