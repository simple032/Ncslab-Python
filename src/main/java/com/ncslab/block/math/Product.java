package com.ncslab.block.math;

import org.json.JSONObject;
import java.util.Vector;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

public class Product extends Block{

	private String seq;
	boolean multiplication=false;

	public Product(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		this.multiplication = "Matrix(*)".equals(paramValues.getString("Multiplication"));

		//因为输入的Dimension必须相互配合，因此设置成DimThrough
		OutputPort output=new OutputPort(this,1,true);
		output.setDimThrough(false);
		outputPortList.add(output);
		paraseParamValues();
	}

	//���������������������
	public void paraseParamValues() {
	//�˴���ȡ����productģ��Ĳ�����Ϊ��*/"��"**"��"//"��
	seq=paramValues.getString("Inputs");

	for(int i=0; i<seq.length();i++) {
		inputPortList.add(new InputPort(this,i+1));
	   }
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Product:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		OutputSignal signal[]=new OutputSignal[seq.length()];
		for(int i=0;i<seq.length();i++){
			signal[i]=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		}
		if(this.multiplication==false) {
			switch(ops1.getOutputSignalC().getDataType()) {
			case REAL:
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=1";
				for(int i=0;i<seq.length();i++) {
					if(seq.charAt(i)=='*') {
						outputCode+="*";
					}
					if(seq.charAt(i)=='/') {
						outputCode+="/";
					}
					outputCode+=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
				  }
				outputCode+=";\n";
				 code.addOutputCode(outputCode);
				break;
			case MATRIX:
				for(int m=0; m<ops1.getHeight(); m++) {
					for(int n=0;n<ops1.getWidth();n++) {
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+m+","+n+")=1";
				for(int i=0;i<seq.length();i++) {
					if(seq.charAt(i)=='*') {
						outputCode+="*";
					}
					if(seq.charAt(i)=='/') {
						outputCode+="/";
					}
					outputCode+=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+m+","+n+")";
				  }
				outputCode+=";\n";
				 code.addOutputCode(outputCode);
				}
			  }
				break;
			}
		}
		else {
				outputCode+=out.getOutputSignalC().getName()+"="+signal[0].getName();
				for(int i=1;i<seq.length();i++) {
					outputCode+="*"+signal[i].getName();
				}
		     outputCode+=";\n";
			 code.addOutputCode(outputCode);
		}

	}


	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal[]=new OutputSignal[seq.length()];
		int m[]=new int[seq.length()];
		int n[]=new int[seq.length()];
		for(int i=0;i<seq.length();i++){
			signal[i]=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			m[i]=signal[i].getHeight();
			n[i]=signal[i].getWidth();
		}
		int v=1;
		if(this.multiplication==false) {
		for(OutputSignal x:signal) {
			if((x.getHeight()!=m[0])||(x.getWidth()!=n[0])) {
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
		}else {
			for(int i=0;i<seq.length()-1;i++) {
				if(n[i]!=m[i+1]) {
					v=0;
					MatDimException e=new MatDimException("Block "+this.blockName+" "+seq.length()+" input dimensions doesn't match !\n \n");
					throw(e);
				}
			}
			out.setHeight(signal[0].getHeight());
			out.setWidth(signal[seq.length()-1].getWidth());
			out.getOutputSignalC().setHeight(signal[0].getHeight());
			out.getOutputSignalC().setWidth(signal[seq.length()-1].getWidth());
			out.getOutputSignalC().setDataType(DataType.MATRIX);
		}

	}

	public void checkDimension() throws MatDimException{
	}
}

