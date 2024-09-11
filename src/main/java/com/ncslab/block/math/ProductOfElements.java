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

public class ProductOfElements extends Block{

	private String seq;
	boolean allDimensions=true;
	boolean multiplication=false;
	private int dimension;

	public ProductOfElements(JSONObject blockJSON,NCSLabModel model) {
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

		allDimensions="All dimensions".equals(paramValues.getString("MultiplyOver"));
		dimension=paramValues.getInt("ElementsDimension");

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
			if(seq.length()==1) {
				switch (ops1.getOutputSignalC().getDataType()) {
					case REAL:
						outputCode+=out.getOutputSignalC().getName()+"=1*"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
						outputCode+=";\n";
					break;
					case MATRIX:
						if(allDimensions) {
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=1";
							for(int m=0; m<ops1.getHeight(); m++) {
								for(int n=0;n<ops1.getWidth();n++) {
									outputCode+=seq.charAt(0)+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+m+","+n+")";
								}
							}
							outputCode+=";\n";
						}else {
							if(dimension==2) {
								for(int m=0; m<ops1.getWidth(); m++) {
									outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+0+","+m+")=1";
									for(int n=0;n<ops1.getHeight();n++) {
										outputCode+=seq.charAt(0)+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+n+","+m+")";
									}
									outputCode+=";\n";
								}
							}else {
								for(int m=0; m<ops1.getHeight(); m++) {
									outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+m+","+0+")=1";
									for(int n=0;n<ops1.getWidth();n++) {
										outputCode+=seq.charAt(0)+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+m+","+n+")";
									}
									outputCode+=";\n";
								}
							}
						}
					break;
				}
			}else {
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
						 //code.addOutputCode(outputCode);
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

					}
				}
				break;
			}
			}
		}
		else {
				outputCode+=out.getOutputSignalC().getName()+"="+signal[0].getName();
				for(int i=1;i<seq.length();i++) {
					outputCode+="*"+signal[i].getName();
					if(seq.charAt(i)=='/') {
						outputCode+=".inv()";
					}
				}
		     outputCode+=";\n";
			 //code.addOutputCode(outputCode);
		}
		code.addOutputCode(outputCode);
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
			if(seq.length()==1) {
				int height=1,width=1;
				DataType type=DataType.REAL;
				if(!allDimensions) {
					if(dimension==2) {
						width=signal[0].getWidth();
					}else {
						height=signal[0].getHeight();
					}
					type=DataType.MATRIX;
				}
				out.setHeight(height);
				out.setWidth(width);
				out.getOutputSignalC().setHeight(height);
				out.getOutputSignalC().setWidth(width);
				out.getOutputSignalC().setDataType(type);
			}else {
			out.setHeight(signal[0].getHeight());
			out.setWidth(signal[0].getWidth());
			out.getOutputSignalC().setHeight(signal[0].getHeight());
			out.getOutputSignalC().setWidth(signal[0].getWidth());
			out.getOutputSignalC().setDataType(signal[0].getDataType());
			}
		  }
		}else {
			if(seq.length()==1) {
				if(seq.charAt(0)=='/' && n[0] != m[0]) {
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions doesn't match !\n \n");
					throw(e);
				}
			}else {
				for(int i=0;i<seq.length()-1;i++) {
					if(seq.charAt(i) == '/' && n[i] != m[i]) {
						v=0;
						MatDimException e=new MatDimException("Block "+this.blockName+" "+seq.length()+" input dimensions doesn't match !\n \n");
						throw(e);
					}
					if(n[i]!=m[i+1]) {
						v=0;
						MatDimException e=new MatDimException("Block "+this.blockName+" "+seq.length()+" input dimensions doesn't match !\n \n");
						throw(e);
					}
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

