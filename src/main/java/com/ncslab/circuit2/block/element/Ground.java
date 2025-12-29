package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.ncslablink.NCSLabModel;

public class Ground extends CircuitBlockSingle {

	public Ground(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		this.blockModeType = blockModeType.Ground;
		this.circuitPortList.remove(1);
	}

	
}
