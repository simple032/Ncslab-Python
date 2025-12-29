package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CurrentSource;
import com.ncslab.ncslablink.NCSLabModel;

public class ControlledCurrentSource extends CurrentSource {

	private block.elec.ControlledCurrentSource controlledCurrentSource;
	public ControlledCurrentSource(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		createBlock();
	}

	private void createBlock() {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Controlled Current Sensor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		controlledCurrentSource=new block.elec.ControlledCurrentSource(addJSON,model);
		model.addElectBlock(controlledCurrentSource);
	}
	
	public String getIString() {
		return controlledCurrentSource.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}
}
