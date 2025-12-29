package com.ncslab.circuit2.block.baseelement;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.ncslablink.NCSLabModel;

public class CurrentSource extends CircuitBlockSingle {
	public CurrentSource(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		this.blockModeType=BlockModeType.CurrentSource;
	}
	
	public String getIString() {
		return null;
	}
}
