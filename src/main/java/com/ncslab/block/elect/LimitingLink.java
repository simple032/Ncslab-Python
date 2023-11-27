package com.ncslab.block.elect;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class LimitingLink extends Block{
	protected Parameter rmin;
	public LimitingLink(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		InputPort in;
		OutputPort out;
		out = new OutputPort(this,1,true);
		in = new InputPort(this,1);
		
		outputPortList.add(out);
		inputPortList.add(in);
		rmin=new Parameter(this,1,"Rmin",paramValues.getString("Rmin"));
		
		parameterList.add(rmin);
		
		System.out.println(paramValues);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Limiting:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=rmin.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Limiting:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		
		
		outputCode+="if("+ops.getOutputSignalC().getName()+">="+rmin.getName()+"){\n";
		outputCode+=out.getOutputSignalC().getName()+"=1/";
		outputCode+=ops.getOutputSignalC().getName()+";\n";
		outputCode+="}\n";
		outputCode+="else{\n";
		outputCode+=out.getOutputSignalC().getName()+"=1/"+rmin.getName()+";\n";
		outputCode+="}\n";	
		code.addOutputCode(outputCode);
	}
	
	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		//switch(signal.getDataType()) {
		//case MATRIX:
			out.setWidth(signal.getWidth());
			out.setHeight(signal.getHeight());
			out.getOutputSignalC().setHeight(signal.getWidth());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
			//break;
		//}
	}
}

