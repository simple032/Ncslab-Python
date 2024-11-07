package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;

//import com.greenpineyu.fel.parser.FelParser.integerLiteral_return;

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

public class SumOfElements extends Block{

	private String seq;
	private boolean allDimensions=true;
	private int dimension;


    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        //输入待根据循环确定
    }

	public SumOfElements(JSONObject blockJSON, NCSLabModel model) {
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

		allDimensions="All dimentions".equals(paramValues.getString("SumOver"));
		dimension=paramValues.getInt("ElementsDimension");
	}

	public boolean getSign(int n) {
		if(seq.charAt(n)=='+') {
			return true;
		}
		else {
			return false;
		}

	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Add:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();

		if(seq.length()==1) {
			switch (ops1.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+=out.getOutputSignalC().getName()+"=0+"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
					outputCode+=";\n";
				break;
				case MATRIX:
					if(allDimensions) {
						outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0";
						for(int m=0; m<ops1.getHeight(); m++) {
							for(int n=0;n<ops1.getWidth();n++) {
								outputCode+=seq.charAt(0)+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+m+","+n+")";
							}
						}
						outputCode+=";\n";
					}else {
						if(dimension==2) {
							for(int m=0; m<ops1.getWidth(); m++) {
								outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+0+","+m+")=0";
								for(int n=0;n<ops1.getHeight();n++) {
									outputCode+=seq.charAt(0)+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+n+","+m+")";
								}
								outputCode+=";\n";
							}
						}else {
							for(int m=0; m<ops1.getHeight(); m++) {
								outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+m+","+0+")=0";
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
	}
	public void checkDimension() throws MatDimException{
	}
}
