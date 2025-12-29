package com.ncslab.block.elec;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class VariableCapacitor extends Block {
	private com.ncslab.circuit2.block.element.VariableCapacitor variableVariableE;
	public VariableCapacitor(JSONObject blockJSON,NCSLabModel model,com.ncslab.circuit2.block.element.VariableCapacitor variableVariableE) {
		super(blockJSON,model);
		this.variableVariableE=variableVariableE;
		inputPortList.add(new InputPort(this,1));
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Variable Indcutor:("+getBlockId()+")"+getBlockName()+"*/\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=";
		//outputCode+=voltageSensorE.getVoltageString();
		//outputCode+=";\n";
		code.addOutputCode(outputCode);
	}
}
