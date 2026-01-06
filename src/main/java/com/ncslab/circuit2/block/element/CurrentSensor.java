package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;

public class CurrentSensor extends VoltageSource implements InterCircuitBlock{
	com.ncslab.block.elec.CurrentSensor currentSensorBlock;
	public CurrentSensor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		this.blockModeType=BlockModeType.VoltageSource;
		
		createBlock(null);
	}
	
	public CurrentSensor(JSONObject blockJSON,NCSLabModel model,NCSLabSystem targetSystem,java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		super(0,blockJSON,model);
		this.blockModeType=BlockModeType.VoltageSource;
		//System.out.println(blockJSON);
		//System.out.println(getVoltageString());
		createBlock(blockSeqCounter);
	}
	
	private void createBlock(java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Current Sensor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		currentSensorBlock=new com.ncslab.block.elec.CurrentSensor(addJSON,model,this);
		currentSensorBlock.setBlockUUID(this.getBlockUUID());
		currentSensorBlock.setBlockId(blockSeqCounter.incrementAndGet());
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
