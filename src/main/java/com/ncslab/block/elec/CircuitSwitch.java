package com.ncslab.block.elec;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class CircuitSwitch extends Block {
	private com.ncslab.circuit2.block.element.CircuitSwitch circuitSwitchE;
	public CircuitSwitch(JSONObject blockJSON,NCSLabModel model,com.ncslab.circuit2.block.element.CircuitSwitch circuitSwitchE) {
		super(blockJSON,model);
		this.circuitSwitchE=circuitSwitchE;
		inputPortList.add(new InputPort(this,1));
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Circuit Switch:("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addOutputCode(outputCode);
	}
}
