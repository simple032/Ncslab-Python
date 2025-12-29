package com.ncslab.block.elec;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class VariableResistor extends Block {
	private com.ncslab.circuit2.block.element.VariableResistor variableResistorE;
	public VariableResistor(JSONObject blockJSON,NCSLabModel model,com.ncslab.circuit2.block.element.VariableResistor variableResistorE) {
		super(blockJSON,model);
		this.variableResistorE=variableResistorE;
		inputPortList.add(new InputPort(this,1));
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Variable Resisitor:("+getBlockId()+")"+getBlockName()+"*/\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=";
		//outputCode+=voltageSensorE.getVoltageString();
		//outputCode+=";\n";
		code.addOutputCode(outputCode);
	}
}
