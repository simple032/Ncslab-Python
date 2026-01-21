package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CurrentSource;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;

public class ControlledCurrentSource extends CurrentSource implements InterCircuitBlock{

	private com.ncslab.block.elec.ControlledCurrentSource controlledCurrentSource;
	public ControlledCurrentSource(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		createBlock(null);
	}
	
	public ControlledCurrentSource(JSONObject blockJSON, NCSLabModel model,NCSLabSystem targetSystem,java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		super(0, blockJSON, model);
		// TODO Auto-generated constructor stub
		createBlock(blockSeqCounter);
	}

	private void createBlock(java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Controlled Current Sensor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		controlledCurrentSource=new com.ncslab.block.elec.ControlledCurrentSource(addJSON,model);
		controlledCurrentSource.setBlockUUID(this.getBlockUUID());
		controlledCurrentSource.setBlockId(blockSeqCounter.incrementAndGet());
		model.addElectBlock(controlledCurrentSource);
	}
	
	public String getIString() {
		return controlledCurrentSource.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}

	@Override
	public double getIValue(double t) {
		// TODO Auto-generated method stub
		return controlledCurrentSource.getInputPortList().get(0).getData().getInitValue();
	}
}
