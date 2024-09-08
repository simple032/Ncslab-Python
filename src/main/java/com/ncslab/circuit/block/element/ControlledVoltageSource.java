package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.source.Constant;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.electblock.GainElect;
import com.ncslab.ncslablink.NCSLabModel;

public class ControlledVoltageSource extends CircuitBlock {
	
	private GainElect controlledVoltageSource;
	
	public ControlledVoltageSource(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		circuitPortList.get(1).setName("RConn2");
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
		
	}
	
	@Override
	protected void setupBlockList() {
		// TODO Auto-generated method stub
		setupEquivilentBlockModels();
	}

	@Override
	protected void setupBlockModeType() {
		// TODO Auto-generated method stub
		blockModeType=BlockModeType.BranchOnly;
	}
	
	private void setupEquivilentBlockModels() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		
		JSONObject controlledVoltageSourceJSON=new JSONObject();
		controlledVoltageSourceJSON.put("blockType", "Gain");
		controlledVoltageSourceJSON.put("blockName", this.blockName.replaceAll(" ", "_")+"_v0");
		controlledVoltageSourceJSON.put("blockPath", this.blockPath);
		JSONObject controlledVoltageSourceParamValues=new JSONObject();
		controlledVoltageSourceParamValues.put("Gain", "1.0");
		controlledVoltageSourceParamValues.put("Multiplication", "Element-wise(K.*u)");
		controlledVoltageSourceJSON.put("paramValues", controlledVoltageSourceParamValues);
		
		controlledVoltageSource=new GainElect(controlledVoltageSourceJSON,this.model);
		this.blockList.add(controlledVoltageSource);
		
		this.setOutputBlock(controlledVoltageSource);
		
		//controlledVoltageSource模块的输出作为外界的输入,加入到列表中
		inputPortList.add(controlledVoltageSource.getInputPortList().get(0));
	}

}
