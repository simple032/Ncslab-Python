package com.ncslab.block.logicAndBit;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class IntervalTest extends com.ncslab.block.Block{
	Parameter upLimit;
	Parameter lowLimit;
	public IntervalTest(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		lowLimit=new Parameter(this,1,"lowLimit",paramValues.getString("lowlimit"));
		upLimit=new Parameter(this,2,"upLimit",paramValues.getString("uplimit"));
		parameterList.add(lowLimit);
		parameterList.add(upLimit);
    }


	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Interval Iest:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=lowLimit.getInitCodeC();
		initCode+=upLimit.getInitCodeC();
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Interval Test:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch(signal.getDataType()) {
		case REAL:
			outputCode+="if("+signal.getName() + " > "+lowLimit.getName()+"&&"+signal.getName()+"<"+upLimit.getName()+") {\n";
            outputCode+=out.getOutputSignalC().getName()+"= 1.0;}else{\n";
            outputCode+=out.getOutputSignalC().getName()+"= 0.0;}\n";
			break;
		case MATRIX:
			for(int i=0; i < signal.getHeight(); i++) {
				for(int j=0; j < signal.getWidth(); j++) {
					outputCode+="if("+signal.getName() + "("+i+","+j+") > "+lowLimit.getName()+"&&"+signal.getName()+"("+i+","+j+")<"+upLimit.getName()+") {\n";
		            outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 1.0;}else{\n";
		            outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 0.0;}\n";
				   }
			   }
			break;
		}
		code.addOutputCode(outputCode);
	}

	public void updateDimension() throws MatDimException{
	    OutputPort out  = outputPortList.get(0);
	    InputPort in  = inputPortList.get(0);
	    OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		out.setHeight(signal.getHeight());
		out.setWidth(signal.getWidth());
		out.getOutputSignalC().setHeight(signal.getHeight());
		out.getOutputSignalC().setWidth(signal.getWidth());
		out.getOutputSignalC().setDataType(signal.getDataType());
    }

	public void checkDimension() throws MatDimException{
	}
}
