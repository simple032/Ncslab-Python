package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.source.SineWave;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.ncslablink.NCSLabModel;

public class ACVoltageSource extends CircuitBlock {
    private SineWave acVoltageSource;
	
	public ACVoltageSource(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
		
	}
	
	protected void setupBlockList() {
		setupEquivilentBlockModels();
	}
	
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.BranchOnly;
	}
	
	private void setupEquivilentBlockModels() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		String v0=paramValues.getString("amp");
		String s=paramValues.getString("shift");
		String f=paramValues.getString("frequency");
		
		JSONObject acVoltageSourceJSON=new JSONObject();
		acVoltageSourceJSON.put("blockType", "SineWave");
		acVoltageSourceJSON.put("blockName", this.blockName.replaceAll(" ", "_")+"_v0");
		acVoltageSourceJSON.put("blockPath", this.blockPath);
		JSONObject acVoltageSourceParamValues=new JSONObject();
		acVoltageSourceParamValues.put("Amplitude", v0);
		acVoltageSourceParamValues.put("Bias", "0");
		acVoltageSourceParamValues.put("Frequency", "6.2831852*("+f+")");
		acVoltageSourceParamValues.put("Phase", "-1*("+s+")/("+f+")");
		acVoltageSourceJSON.put("paramValues", acVoltageSourceParamValues);
		acVoltageSource=new SineWave(acVoltageSourceJSON,this.model);
		this.blockList.add(acVoltageSource);
		
		//System.out.println(dcVoltageSourceJSON);
		
		this.setOutputBlock(acVoltageSource);
		this.setupInputBlock(null);
	}
}
