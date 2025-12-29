package com.ncslab.block.elec;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.NCSLabModel;

public class ControlledVoltageSource extends Block {

	public ControlledVoltageSource(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);
		// TODO Auto-generated constructor stub
		inputPortList.add(new InputPort(this, 1));
	}

}
