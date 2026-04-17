package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.block.data.Data;
import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.ncslablink.NCSLabModel;

public class DCVoltageSource extends VoltageSource {
	
	private String vString;
	
	private double vValue;
	
	public DCVoltageSource(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
		//System.out.println(blockJSON);
		
		vString=paramValues.getString("v0");
		vValue=Double.parseDouble(vString);
		
	}
	
	public DCVoltageSource(JSONObject blockJSON,NCSLabModel model) {
		super(0,blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
		//System.out.println(blockJSON);
		
		vString=paramValues.getString("v0");
		vValue=Double.parseDouble(vString);
		
	}
	
	public String getVeString() {
		return this.vString;
	}
	
	protected void setupBlockList() {
		//setupEquivilentBlockModels();
	}
	
	public String getVString() {
		return vString;
	}
	
	public double getVValue(double t) {
		return vValue;
	}

}
