package com.ncslab.block.discontinuous;
import org.json.JSONObject;

import Jama.Matrix;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;

public class Backlash extends Block{
	Parameter backlashWidth;
	Parameter initialOutput;
	
	private State xState;
	public Backlash(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		backlashWidth=new Parameter(this,1,"backlashWidth",paramValues.getString("BacklashWidth"));
		initialOutput=new Parameter(this,2,"initialOutput",paramValues.getString("InitialOutput"));
		parameterList.add(backlashWidth);
		parameterList.add(initialOutput);
	}
	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Backlash:("+getBlockId()+")"+getBlockName()+"*/\n";
			OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			initCode+=backlashWidth.getInitCodeC();
			initCode+=initialOutput.getInitCodeC();
			if(initialOutput.getDataType()==DataType.REAL&&signal.getDataType()==DataType.REAL) {
			initCode+=xState.getName()+"="+initialOutput.getName()+";\n";
			}
			else if(initialOutput.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
				for(int i=0; i<xState.getHeight(); i++) {
					for(int j=0;j<xState.getWidth();j++) {	
						initCode+=xState.getName()+"("+i+","+j+")="+initialOutput.getName()+";\n";
					}
				}
			}
			else {
				for(int i=0; i<xState.getHeight(); i++) {
					for(int j=0;j<xState.getWidth();j++) {	
						initCode+=xState.getName()+"("+i+","+j+")="+initialOutput.getName()+"("+i+","+j+");\n";
					}
				}
			}
			code.addInitCode(initCode);
		}
	 public void generateOutputCodeC(CodeStructC code) {
			String outputCode="/*Code for output of block Backlash:("+getBlockId()+")"+getBlockName()+"*/\n";
			
			OutputPort out  = outputPortList.get(0);
			OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
			OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			switch(backlashWidth.getDataType()) {
			case REAL:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+="if("+signal.getName()+"<"+xState.getName()+"-0.5*"+backlashWidth.getName()+") {\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+signal.getName()+"+0.5*"+backlashWidth.getName()+";}\n";
					outputCode+="else if("+signal.getName()+"<="+xState.getName()+"+0.5*"+backlashWidth.getName()+") {\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+xState.getName()+";}\n";
					outputCode+="else {\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+signal.getName()+"-0.5*"+backlashWidth.getName()+";}\n";
					outputCode+=xState.getName()+"="+outputPortList.get(0).getOutputSignalC().getName()+";\n";
					break;
				case MATRIX:
					for(int i=0; i<ops.getHeight(); i++) {
						for(int j=0;j<ops.getWidth();j++) {
							outputCode+="if("+signal.getName()+"("+i+","+j+")<"+xState.getName()+"("+i+","+j+")-0.5*"+backlashWidth.getName()+") {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+")+0.5*"+backlashWidth.getName()+";}\n";
							outputCode+="else if("+signal.getName()+"("+i+","+j+")<="+xState.getName()+"("+i+","+j+")+0.5*"+backlashWidth.getName()+") {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+xState.getName()+"("+i+","+j+");}\n";
							outputCode+="else {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+")-0.5*"+backlashWidth.getName()+";}\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+");\n";
						}
					}
					break;
				}
				break;
			case MATRIX:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					for(int i=0; i<backlashWidth.getHeight(); i++) {
						for(int j=0;j<backlashWidth.getWidth();j++) {
							outputCode+="if("+signal.getName()+"<"+xState.getName()+"("+i+","+j+")-0.5*"+backlashWidth.getName()+"("+i+","+j+")) {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"+0.5*"+backlashWidth.getName()+"("+i+","+j+");}\n";
							outputCode+="else if("+signal.getName()+"<="+xState.getName()+"("+i+","+j+")+0.5*"+backlashWidth.getName()+"("+i+","+j+")) {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+xState.getName()+"("+i+","+j+");}\n";
							outputCode+="else {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"-0.5*"+backlashWidth.getName()+"("+i+","+j+");}\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+");\n";
						}
					}
					break;
				case MATRIX:
					for(int i=0; i<ops.getHeight(); i++) {
						for(int j=0;j<ops.getWidth();j++) {
							outputCode+="if("+signal.getName()+"("+i+","+j+")<"+xState.getName()+"("+i+","+j+")-0.5*"+backlashWidth.getName()+"("+i+","+j+")) {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+")+0.5*"+backlashWidth.getName()+"("+i+","+j+");}\n";
							outputCode+="else if("+signal.getName()+"("+i+","+j+")<="+xState.getName()+"("+i+","+j+")+0.5*"+backlashWidth.getName()+"("+i+","+j+")) {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+xState.getName()+"("+i+","+j+");}\n";
							outputCode+="else {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+")-0.5*"+backlashWidth.getName()+"("+i+","+j+");}\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+");\n";
						}
					}
					
					break;
				}
		      break;
			}
		      code.addOutputCode(outputCode);
	 }
	 
	 
	 public void generateDerivativeCodeC(CodeStructC code) {
		   super.generateDerivativeCodeC(code);
		   
		   String derivativeCode="/*Code for Derivative of block Backlash:("+getBlockId()+")"+getBlockName()+"*/\n";
		   
		   derivativeCode+=xState.getDerivativeName()+"=0*"+xState.getName()+";\n";
		   
		   code.addDerivativeCode(derivativeCode);
	   }
	 
	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal.getDataType()==DataType.REAL) {
			xState=new State(this,1,"save_data",backlashWidth.getHeight(),backlashWidth.getWidth());}
			else {
				xState=new State(this,1,"save_data",signal.getHeight(),signal.getWidth());	
			}
			stateList.add(xState);
			if(backlashWidth.getWidth()!=initialOutput.getWidth()||backlashWidth.getHeight()!=initialOutput.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);	
			}
			if(backlashWidth.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
				out.setHeight(backlashWidth.getHeight());
				out.setWidth(backlashWidth.getWidth());
				out.getOutputSignalC().setHeight(backlashWidth.getHeight());
				out.getOutputSignalC().setWidth(backlashWidth.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);	
			}
			else if(backlashWidth.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());	
			}
			else{
				if(backlashWidth.getWidth()!=signal.getWidth()||backlashWidth.getHeight()!=signal.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the gain dimension!\n \n");
				throw(e);
				}
				out.setHeight(backlashWidth.getHeight());
				out.setWidth(backlashWidth.getWidth());
				out.getOutputSignalC().setHeight(backlashWidth.getHeight());
				out.getOutputSignalC().setWidth(backlashWidth.getWidth());
				out.getOutputSignalC().setDataType(backlashWidth.getDataType());	
			}
	  }
	 public void checkDimension() throws MatDimException{
		}    
}