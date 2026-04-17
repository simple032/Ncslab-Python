package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.block.data.Data;
import com.ncslab.circuit2.block.baseelement.CurrentSource;
import com.ncslab.ncslablink.NCSLabModel;

public class DCCurrentSource extends CurrentSource {
private String iString;
	double iValue;
	public DCCurrentSource(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
		//System.out.println(blockJSON);
		
		iString=paramValues.getString("i0");
		iValue=Double.parseDouble(iString);
	}
	
	public DCCurrentSource(JSONObject blockJSON,NCSLabModel model) {
		super(0,blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
		//System.out.println(blockJSON);
		
		iString=paramValues.getString("i0");
		iValue=Double.parseDouble(iString);
	}
	
	public String getIString() {
		return iString;
	}
	
	public double getIValue(double t) {
		return iValue;
	}
}
