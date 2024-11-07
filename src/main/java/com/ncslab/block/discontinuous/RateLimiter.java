package com.ncslab.block.discontinuous;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class RateLimiter extends Block{
	Parameter lowerLimit;
	Parameter upperLimit;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("lowerLimit");
        parameterNames.add("upperLimit");
        outputNames.add("out1");
        inputNames.add("in1");
    }
	public RateLimiter(JSONObject blockIn,NCSLabModel model) {

		super(blockIn,model);
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		lowerLimit=new Parameter(this,1,"lowerLimit",paramValues.getString("LowerLimit"));
		upperLimit=new Parameter(this,2,"upperLimit",paramValues.getString("UpperLimit"));
		parameterList.add(lowerLimit);
		parameterList.add(upperLimit);
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=lowerLimit.getInitCodeM();
		initCode+=upperLimit.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String outputCode="";
		switch(lowerLimit.getDataType()) {
		case REAL:
			switch(ops.getOutputSignalC().getDataType()) {
			case REAL:
		outputCode+="if "+ signal.getName()+">"+upperLimit.getName()+"\n";
		outputCode+=out.getOutputSignalC().getName()+"="+upperLimit.getName()+";\n";
		outputCode+="elseif "+ signal.getName()+"<"+lowerLimit.getName()+"\n";
		outputCode+=out.getOutputSignalC().getName()+"="+lowerLimit.getName()+";\n";
		outputCode+="else\n";
		outputCode+=out.getOutputSignalC().getName()+"="+signal.getName()+";\n";
		outputCode+="end\n";
		    break;
			case MATRIX:
				for(int i=1; i<ops.getHeight()+1; i++) {
					for(int j=1;j<ops.getWidth()+1;j++) {
						outputCode+="if "+ signal.getName()+"("+i+","+j+")>"+upperLimit.getName()+"\n";
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+upperLimit.getName()+";\n";
						outputCode+="elseif "+ signal.getName()+"("+i+","+j+")<"+lowerLimit.getName()+"\n";
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+lowerLimit.getName()+";\n";
						outputCode+="else\n";
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+");\n";
						outputCode+="end\n";
					}
				}
				break;
			}
			break;
		case MATRIX:
			switch(ops.getOutputSignalC().getDataType()) {
			case REAL:
				for(int i=1; i<lowerLimit.getHeight()+1; i++) {
					for(int j=1;j<lowerLimit.getWidth()+1;j++) {
				outputCode+="if "+ signal.getName()+">"+upperLimit.getName()+"("+i+","+j+")\n";
				outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+upperLimit.getName()+"("+i+","+j+");\n";
				outputCode+="elseif "+ signal.getName()+"<"+lowerLimit.getName()+"("+i+","+j+")\n";
				outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+lowerLimit.getName()+"("+i+","+j+");\n";
				outputCode+="else\n";
				outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+";\n";
				outputCode+="end\n";
					}
				}
				break;
			case MATRIX:
				for(int i=1; i<lowerLimit.getHeight()+1; i++) {
					for(int j=1;j<lowerLimit.getWidth()+1;j++) {
						outputCode+="if "+ signal.getName()+"("+i+","+j+")>"+upperLimit.getName()+"("+i+","+j+")\n";
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+upperLimit.getName()+"("+i+","+j+");\n";
						outputCode+="elseif "+ signal.getName()+"("+i+","+j+")<"+lowerLimit.getName()+"("+i+","+j+")\n";
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+lowerLimit.getName()+"("+i+","+j+");\n";
						outputCode+="else\n";
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+");\n";
						outputCode+="end\n";
					}
				}
				break;
			}
			break;
		}
		code.addOutputCode(outputCode);
	}
	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Saturation:("+getBlockId()+")"+getBlockName()+"*/\n";
			initCode+=lowerLimit.getInitCodeC();
			initCode+=upperLimit.getInitCodeC();
			code.addInitCode(initCode);
		}

	//define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block RateLimiter:("+getBlockId()+")"+getBlockName()+"*/\n";

		 arraysCode+="double "+"Block"+getBlockId()+"RateLimiter_data=0;\n";
		 code.addArraysCode(arraysCode);
	 }

	 public void generateOutputCodeC(CodeStructC code) {
			String outputCode="/*Code for output of block RateLimiter:("+getBlockId()+")"+getBlockName()+"*/\n";
			OutputPort out  = outputPortList.get(0);
			OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
			OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			outputCode+="if(mp->majorStep>0) {\n";
//			switch(lowerLimit.getDataType()) {
//			case REAL:
//				switch(ops.getOutputSignalC().getDataType()) {
//				case REAL:
					outputCode+="if("+signal.getName()+"-Block"+getBlockId()+"RateLimiter_data>"+upperLimit.getName()+"*STEP_SIZE) {\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+upperLimit.getName()+"*STEP_SIZE+Block"+getBlockId()+"RateLimiter_data;}\n";
					outputCode+="else if("+ signal.getName()+"-Block"+getBlockId()+"RateLimiter_data<"+lowerLimit.getName()+"*STEP_SIZE) {\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+lowerLimit.getName()+"*STEP_SIZE+Block"+getBlockId()+"RateLimiter_data;}\n";
					outputCode+="else {\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+signal.getName()+";}\n";
					outputCode+="Block"+getBlockId()+"RateLimiter_data="+outputPortList.get(0).getOutputSignalC().getName()+";\n";
//					break;
//				case MATRIX:
//					for(int i=0; i<ops.getHeight(); i++) {
//						for(int j=0;j<ops.getWidth();j++) {
//							outputCode+="if("+signal.getName()+"("+i+","+j+")>"+upperLimit.getName()+") {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+upperLimit.getName()+";}\n";
//							outputCode+="else if("+ signal.getName()+"("+i+","+j+")<"+lowerLimit.getName()+") {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+lowerLimit.getName()+";}\n";
//							outputCode+="else {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+");}\n";
//						}
//					}
//					break;
//				}
//				break;
//			case MATRIX:
//				switch(ops.getOutputSignalC().getDataType()) {
//				case REAL:
//					for(int i=0; i<lowerLimit.getHeight(); i++) {
//						for(int j=0;j<lowerLimit.getWidth();j++) {
//							outputCode+="if("+signal.getName()+">"+upperLimit.getName()+"("+i+","+j+")) {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+upperLimit.getName()+"("+i+","+j+");}\n";
//							outputCode+="else if("+ signal.getName()+"<"+lowerLimit.getName()+"("+i+","+j+")) {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+lowerLimit.getName()+"("+i+","+j+");}\n";
//							outputCode+="else {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+";}\n";
//						}
//					}
//					break;
//				case MATRIX:
//					for(int i=0; i<lowerLimit.getHeight(); i++) {
//						for(int j=0;j<lowerLimit.getWidth();j++) {
//							outputCode+="if("+signal.getName()+"("+i+","+j+")>"+upperLimit.getName()+"("+i+","+j+")) {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+upperLimit.getName()+"("+i+","+j+");}\n";
//							outputCode+="else if("+ signal.getName()+"("+i+","+j+")<"+lowerLimit.getName()+"("+i+","+j+")) {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+lowerLimit.getName()+"("+i+","+j+");}\n";
//							outputCode+="else {\n";
//							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+");}\n";
//						}
//					}
//					break;
//				}
//				break;
//			}
			outputCode+="}\n";
			code.addOutputCode(outputCode);
	  }
	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(lowerLimit.getWidth()!=upperLimit.getWidth()||lowerLimit.getHeight()!=upperLimit.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
			}
			if(lowerLimit.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
				out.setHeight(lowerLimit.getHeight());
				out.setWidth(lowerLimit.getWidth());
				out.getOutputSignalC().setHeight(lowerLimit.getHeight());
				out.getOutputSignalC().setWidth(lowerLimit.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
			}
			else if(lowerLimit.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());
			}
			else{
				if(lowerLimit.getWidth()!=signal.getWidth()||lowerLimit.getHeight()!=signal.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the Saturation dimension!\n \n");
				throw(e);
				}
				out.setHeight(lowerLimit.getHeight());
				out.setWidth(lowerLimit.getWidth());
				out.getOutputSignalC().setHeight(lowerLimit.getHeight());
				out.getOutputSignalC().setWidth(lowerLimit.getWidth());
				out.getOutputSignalC().setDataType(lowerLimit.getDataType());
			}
	  }
	 public void checkDimension() throws MatDimException{
		}
}
