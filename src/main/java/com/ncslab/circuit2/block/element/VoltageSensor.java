package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;
import com.ncslab.circuit2.block.baseelement.*;

public class VoltageSensor extends CircuitBlockSingle implements InterCircuitBlock{
	com.ncslab.block.elec.VoltageSensor voltageSensorBlock;
	public VoltageSensor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		this.blockModeType=BlockModeType.VoltageSensor;
		//System.out.println(blockJSON);
		//System.out.println(getVoltageString());
		createBlock(null);
	}
	
	public VoltageSensor(JSONObject blockJSON,NCSLabModel model) {
		super(0,blockJSON,model);
		this.blockModeType=BlockModeType.VoltageSensor;
		//System.out.println(blockJSON);
		//System.out.println(getVoltageString());
		createBlock(null);
	}
	
	public VoltageSensor(JSONObject blockJSON,NCSLabModel model,NCSLabSystem targetSystem,java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		super(0,blockJSON,model);
		this.blockModeType=BlockModeType.VoltageSensor;
		//System.out.println(blockJSON);
		//System.out.println(getVoltageString());
		createBlock(blockSeqCounter);
	}
	
	public String getVoltageString() {
		String vString="";
		vString+="("+this.getCurcuitPortList().get(0).getCircuitNode().getNodeString()+"-"+this.getCurcuitPortList().get(1).getCircuitNode().getNodeString()+")";
		//System.out.println(vString);
		return vString;
	}
	
	private void createBlock(java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Voltage Sensor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		voltageSensorBlock=new com.ncslab.block.elec.VoltageSensor(addJSON,model,this);
		voltageSensorBlock.setBlockUUID(this.getBlockUUID());
		voltageSensorBlock.setBlockId(blockSeqCounter.incrementAndGet());
		model.addElectBlock(voltageSensorBlock);
	}
	
	public String getCurrentCode() {
		String code=super.getCurrentCode();
		code+=this.getCurrentString()+"=0;\n";
		return code;
	}
}
