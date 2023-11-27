package com.ncslab.block.source;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

public class Clock extends Block{
	public Clock(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		outputPortList.add(new OutputPort(this,1,false));
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Clock::("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=model.time;\n";
		code.addOutputCode(outputCode);
	}
	  public void updateDimension() throws MatDimException{
		}
	public void checkDimension() throws MatDimException{
	}
}
