package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.ncslablink.NCSLabModel;

public class ControlledVoltageSource extends VoltageSource {

	private com.ncslab.block.elec.ControlledVoltageSource controlledVoltageSource;
	public ControlledVoltageSource(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		createBlock();
	}

	private void createBlock() {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Controlled Voltage Sensor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		controlledVoltageSource=new com.ncslab.block.elec.ControlledVoltageSource(addJSON,model);
		model.addElectBlock(controlledVoltageSource);
	}
	
	public String getVString() {
		return controlledVoltageSource.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}
}
