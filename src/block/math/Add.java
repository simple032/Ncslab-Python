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
		
		//因为输入的Dimension必须相互配合，因此设置成DimThrough
		OutputPort output=new OutputPort(this,1,true);
		output.setDimThrough(false);
		outputPortList.add(output);
		
		paraseParamValues();
	}
	
	//�������빹���������
	public void paraseParamValues( ) {
		seq = paramValues.getString("Inputs");
		
		for(int i=0;i<seq.length();i++) {
			inputPortList.add(new InputPort(this,i+1));
		}
	}
	
	public boolean getSign(int n) {
		if(seq.charAt(n)=='+') {
			return true;
		}
		else {
			return false;
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
		OutputSignal signal[]=new OutputSignal[seq.length()];
		for(int i=0;i<seq.length();i++){
			signal[i]=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		}
		int m=signal[0].getHeight();
		int n=signal[0].getWidth();
		int v=1;
		for(OutputSignal x:signal) {
			if((x.getHeight()!=m)||(x.getWidth()!=n)) {
				v=0;
				MatDimException e=new MatDimException("Block "+this.blockName+" "+seq.length()+" input dimensions doesn't match !\n \n");
				throw(e);
			}
		}
		if(v==1) {
			out.setHeight(signal[0].getHeight());
			out.setWidth(signal[0].getWidth());
			out.getOutputSignalC().setHeight(signal[0].getHeight());
			out.getOutputSignalC().setWidth(signal[0].getWidth());
			out.getOutputSignalC().setDataType(signal[0].getDataType());	
		}
	}
	public void checkDimension() throws MatDimException{
	}
}
