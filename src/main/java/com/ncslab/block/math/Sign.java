package com.ncslab.block.math;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class Sign extends Block{
	public Sign (JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,true));
		//����һ������
		inputPortList.add(new InputPort(this,1));	
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		String outputCode="";
		switch(ops1.getOutputSignalC().getDataType()) {
		case REAL:
		outputCode+="if "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+">0\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=1.0;\n";
		outputCode+="elseif "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"==0\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
		outputCode+="else\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=-1.0;\n";
		outputCode+="end\n";
		break;
		case MATRIX:
			for(int i=1;i<ops1.getHeight()+1;i++) {
				for(int j=1;j<ops1.getWidth()+1;j++) {	
					outputCode+="if "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+i+","+j+")>0\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=1.0;\n";
					outputCode+="elseif "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+i+","+j+")==0\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;\n";
					outputCode+="else\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=-1.0;\n";
					outputCode+="end\n";	
				}
			}
		break;	
		}
		code.addOutputCode(outputCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Sign:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch(signal.getDataType()) {
		case REAL:
			outputCode+="if("+signal.getName()+">0) {\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=1;}\n";
			outputCode+="else if("+signal.getName()+"==0) {\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;}\n";
			outputCode+="else{\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=-1;}\n";
			break;
		case MATRIX:
				for(int i=0;i<signal.getHeight();i++) {
					for(int j=0;j<signal.getWidth();j++) {
						outputCode+="if("+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+i+","+j+")>0){\n";
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=1;}\n";
					    outputCode+="else if("+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+i+","+j+")==0){\n";
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
					    outputCode+="else{\n";
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=-1;}\n";
			          }
				   }
			break;
		}
		code.addOutputCode(outputCode);
	}
	
	public void updateDimension() throws MatDimException{
		if(inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getDataType()==DataType.MATRIX) {
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());	
	   }
	}
	public void checkDimension() throws MatDimException{
		
	}
}
