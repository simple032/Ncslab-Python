package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;

public class ControlledVoltageSource extends VoltageSource implements InterCircuitBlock{

	private com.ncslab.block.elec.ControlledVoltageSource controlledVoltageSource;
	public ControlledVoltageSource(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		createBlock(null);
	}
	
	public ControlledVoltageSource(JSONObject blockJSON, NCSLabModel model,NCSLabSystem targetSystem,java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		super(0, blockJSON, model);
		// TODO Auto-generated constructor stub
		createBlock(blockSeqCounter);
	}

	private void createBlock(java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Controlled Voltage Sensor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		controlledVoltageSource=new com.ncslab.block.elec.ControlledVoltageSource(addJSON,model);
		controlledVoltageSource.setBlockUUID(this.getBlockUUID());
		controlledVoltageSource.setBlockId(blockSeqCounter.incrementAndGet());
		model.addElectBlock(controlledVoltageSource);
	}
	
	public String getVString() {
		com.ncslab.block.io.InputPort inputPort = controlledVoltageSource.getInputPortList().get(0);
		if (inputPort.getLinkedLine() == null) {
			System.err.println("Warning: ControlledVoltageSource '" + this.blockName + "' control input is not connected. Using 0 as fallback.");
			return "0";
		}
		return inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}

	@Override
	public double getVValue(double t) {
		// TODO Auto-generated method stub
		com.ncslab.block.io.InputPort inputPort = controlledVoltageSource.getInputPortList().get(0);
		if (inputPort.getLinkedLine() == null) {
			return 0.0;
		}
		return inputPort.getData().getInitValue();
	}
}
