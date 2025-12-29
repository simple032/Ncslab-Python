package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CurrentSource;
import com.ncslab.ncslablink.NCSLabModel;

public class DCCurrentSource extends CurrentSource {
private String iString;
	
	public DCCurrentSource(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
		//System.out.println(blockJSON);
		
		iString=paramValues.getString("i0");
		
	}
	
	public String getIString() {
		return iString;
	}
}
