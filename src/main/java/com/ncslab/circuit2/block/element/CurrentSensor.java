package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.ncslablink.NCSLabModel;

public class CurrentSensor extends VoltageSource {
	block.elec.CurrentSensor currentSensorBlock;
	public CurrentSensor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		this.blockModeType=BlockModeType.VoltageSource;
		
		createBlock();
	}
	
	private void createBlock() {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Current Sensor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		currentSensorBlock=new block.elec.CurrentSensor(addJSON,model,this);
		model.addElectBlock(currentSensorBlock);
	}
	
	public String getVString() {
		return "0.0";
	}
	
	/*
	public String getCurrentString() {
		return "0.0";
	}*/
}
