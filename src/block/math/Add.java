package block.math;

import org.json.JSONObject;
import java.util.Vector;

import block.Block;
import block.data.DataType;
import block.io.OutputPort;
import block.io.OutputSignal;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
import block.io.InputPort;

public class Add extends Block{
	
	private String seq;
	
	public Add(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
		
		//����һ������
		outputPortList.add(new OutputPort(this,1,true));
		
		paraseParamValues();
	}
	
	//�������빹���������
	public void paraseParamValues( ) {
		seq = paramValues.getString("Inputs");
		
		for(int i=0;i<seq.length();i++) {
			inputPortList.add(new InputPort(this,i+1));
		}
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		OutputPort out  = outputPortList.get(0);
		OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		String outputCode="";
		switch(ops1.getOutputSignalC().getDataType()) {
		case REAL:
			outputCode+=out.getOutputSignalC().getName()+"=0";
		for(int i=0;i<seq.length();i++) {
			if(seq.charAt(i)=='+') {
				outputCode+="+";
			}
			if(seq.charAt(i)=='-') {
				outputCode+="-";
			}
			outputCode+=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		}
		outputCode+=";\n";
		break;
		case MATRIX:
			for(int m=1; m<ops1.getHeight()+1; m++) {
				for(int n=1;n<ops1.getWidth()+1;n++) {
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+m+","+n+")=0";
			for(int i=0;i<seq.length();i++) {
				if(seq.charAt(i)=='+') {
					outputCode+="+";
				}
				if(seq.charAt(i)=='-') {
					outputCode+="-";
				}
				outputCode+=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+m+","+n+")";
			  }
			outputCode+=";\n";
			}
		  }
			break;
		}
		code.addOutputCode(outputCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Add:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		switch(ops1.getOutputSignalC().getDataType()) {
		case REAL:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0";
			for(int i=0;i<seq.length();i++) {
				if(seq.charAt(i)=='+') {
					outputCode+="+";
				}
				if(seq.charAt(i)=='-') {
					outputCode+="-";
				}
				outputCode+=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
			  }
			outputCode+=";\n";
			break;
		case MATRIX:
		for(int m=0; m<ops1.getHeight(); m++) {
			for(int n=0;n<ops1.getWidth();n++) {
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+m+","+n+")=0";
		for(int i=0;i<seq.length();i++) {
			if(seq.charAt(i)=='+') {
				outputCode+="+";
			}
			if(seq.charAt(i)=='-') {
				outputCode+="-";
			}
			outputCode+=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+m+","+n+")";
		  }
		outputCode+=";\n";
		}
	  }
		break;
	}
		code.addOutputCode(outputCode);
	}
	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(signal1.getHeight()!=signal2.getHeight()||signal1.getWidth()!=signal2.getWidth()) {
			MatDimException e=new MatDimException("Block "+this.blockName+" two input dimensions doesn't match !\n \n");
			throw(e);
		}
		else {
			out.setHeight(signal1.getHeight());
			out.setWidth(signal1.getWidth());
			out.getOutputSignalC().setHeight(signal1.getHeight());
			out.getOutputSignalC().setWidth(signal1.getWidth());
			out.getOutputSignalC().setDataType(signal1.getDataType());	
		}
	}
	public void checkDimension() throws MatDimException{
	}
}
