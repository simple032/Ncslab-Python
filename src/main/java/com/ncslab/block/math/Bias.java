package com.ncslab.block.math;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class Bias extends Block{
	Parameter value;
	public Bias(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		value = new Parameter(this,1,"value",paramValues.getString("Bias"));
		parameterList.add(value);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Bias:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=value.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Bias:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+signal.getName()+"+("+value.getName()+");\n";
		code.addOutputCode(outputCode);
	}
     public void updateDimension() throws MatDimException{
	 }

	public void checkDimension() throws MatDimException{
	}
}
