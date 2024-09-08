package com.ncslab.block.discrete;

import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class Discrete_Transfer_Fcnz extends DiscreteBlock{

	public Discrete_Transfer_Fcnz(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		inputPortList.add(new InputPort(this,3));
		outputPortList.add(new OutputPort(this,1,true));
	}
	//define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block discrete_Transfer_Fcn(z):("+getBlockId()+")"+getBlockName()+"*/\n";
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 OutputSignal signal3=inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 arraysCode+="double "+"Block"+getBlockId()+"_discrete_transferz_savedata["+signal.getHeight()+"]["+signal.getWidth()+"];\n";
		 arraysCode+="double "+"Block"+getBlockId()+"_state["+signal.getHeight()+"]["+signal.getWidth()*(signal3.getWidth()-1)+"];\n";
		 code.addArraysCode(arraysCode);
	 }
	 public void generateInitCodeC(CodeStructC code) {
		 String initCode="/*Code for initialization of block discrete_Transfer_Fcn(z):("+getBlockId()+")"+getBlockName()+"*/\n";
		 initCode+="sample_time[sample_i]=0.2;\n";
			initCode+="sample_i=sample_i+1;\n";
		 code.addInitCode(initCode);
	}
	 public void generateOutputCodeC(CodeStructC code) {
		    OutputPort out  = outputPortList.get(0);
			OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
			OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal3=inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			  String outputCode="/*Code for output of discrete_transfer_fun(z):("+getBlockId()+")"+getBlockName()+"*/\n";
			  outputCode+="{real_T currentTime = model.time;\n";
			  outputCode+="if(fabs(floor(currentTime/0.2+0.5)-currentTime/0.2)<0.000001&&mp->majorStep>0) {\n";
			  if(signal2.getDataType()==DataType.REAL) {
				  outputCode+="REAL "+signal2.getName()+"_REAL="+signal2.getName()+";\n";
				  outputCode+="Matrix "+signal2.getName()+"(1,2);\n";
				  outputCode+=signal2.getName()+".set(0,0,"+signal2.getName()+"_REAL);\n";
			  }
			  if(signal3.getDataType()==DataType.REAL) {
				  outputCode+="REAL "+signal3.getName()+"_REAL="+signal3.getName()+";\n";
				  outputCode+="Matrix "+signal3.getName()+"(1,2);\n";
				  outputCode+=signal3.getName()+".set(0,0,"+signal3.getName()+"_REAL);\n";
			  }

			  int h=signal3.getWidth()-signal2.getWidth()-1;
			  switch(signal1.getDataType()) {
			  case REAL:
				  outputCode+="Block"+getBlockId()+"_discrete_transferz_savedata[0][0]=("+signal1.getName();
				  for(int i=1;i<signal3.getWidth();i++) {
						outputCode+="-"+signal3.getName()+"(0,"+i+")*Block"+getBlockId()+"_state[0]["+i+"-1]";
					}
					outputCode+=");\n";
					if(signal2.getWidth()==signal3.getWidth()) {
						  outputCode+=this.getOutputPortVariable(0)+"=Block"+getBlockId()+"_discrete_transferz_savedata[0][0]*"+signal2.getName()+"(0,0)";
						  if(signal2.getWidth()==1) {
						  }
						  else {
							  for(int i=1;i<signal2.getWidth();i++) {
								 outputCode+="+"+signal2.getName()+"(0,"+i+")*Block"+getBlockId()+"_state[0]["+i+"-1]";
							  }
						  }
					    }
						  else {
							  outputCode+=this.getOutputPortVariable(0)+"=0";
							  for(int j=0;j<signal2.getWidth();j++) {
								  outputCode+="+"+signal2.getName()+"(0,"+j+")*Block"+getBlockId()+"_state[0]["+j+"]";
							  }
						  }
						  outputCode+=";\n";
						  for(int i=signal3.getWidth()-2;i>0;i--) {
								outputCode+="Block"+getBlockId()+"_state[0]["+i+"]=Block"+getBlockId()+"_state[0]["+i+"-1];\n";
							}
							outputCode+="Block"+getBlockId()+"_state[0][0]=Block"+getBlockId()+"_discrete_transferz_savedata[0][0];\n";
							  outputCode+="}}\n";
							  break;
			  case MATRIX:
				  for(int m=0;m<signal1.getHeight();m++) {
					  for(int n=0;n<signal1.getWidth();n++) {
						  outputCode+="Block"+getBlockId()+"_discrete_transferz_savedata["+m+"]["+n+"]=("+signal1.getName()+"("+m+","+n+")";
							for(int i=1;i<signal3.getWidth();i++) {
								outputCode+="-"+signal3.getName()+"(0,"+i+")*Block"+getBlockId()+"_state["+m+"]["+(i-1)+"+"+n+"*"+(signal3.getWidth()-1)+"]";
							}
							outputCode+=");\n";

							if(signal2.getWidth()==signal3.getWidth()) {
								  outputCode+=this.getOutputPortVariable(0)+"("+m+","+n+")=Block"+getBlockId()+"_discrete_transferz_savedata["+m+"]["+n+"]*"+signal2.getName()+"(0,0)";
								  if(signal2.getWidth()==1) {
								  }
								  else {
									  for(int i=1;i<signal2.getWidth();i++) {
										 outputCode+="+"+signal2.getName()+"(0,"+i+")*Block"+getBlockId()+"_state["+m+"]["+(i-1)+"+"+n+"*"+(signal3.getWidth()-1)+"]";
									  }
								  }
							    }
								  else {
									  outputCode+=this.getOutputPortVariable(0)+"("+m+","+n+")=0";
									  for(int j=0;j<signal2.getWidth();j++) {
										  outputCode+="+"+signal2.getName()+"(0,"+j+")*Block"+getBlockId()+"_state["+m+"]["+j+"+"+n+"*"+(signal3.getWidth()-1)+"]";
									  }
								  }
								  outputCode+=";\n";
					  }
				  }
				  for(int m=0;m<signal1.getHeight();m++) {
					  for(int n=0;n<signal1.getWidth();n++) {
						  for(int i=signal3.getWidth()-2;i>0;i--) {
							  outputCode+="Block"+getBlockId()+"_state["+m+"]["+n+"*"+(signal3.getWidth()-1)+"+"+i+"]=Block"+getBlockId()+"_state["+m+"]["+n+"*"+(signal3.getWidth()-1)+"+"+i+"-1];\n";
						      }
							outputCode+="Block"+getBlockId()+"_state["+m+"]["+n+"*"+(signal3.getWidth()-1)+"]=Block"+getBlockId()+"_discrete_transferz_savedata["+m+"]["+n+"];\n";
						  }
					  }
				  outputCode+="}}\n";
				  break;
			  }
			  code.addOutputCode(outputCode);
	}
	public void  generateUpdateCodeC(CodeStructC code) {
		    String updateCode="/*Code for Update of  Discrete_Transfer_Fcn "+ ":("+getBlockId()+")"+getBlockName()+"*/\n";
			code.addUpdateCode(updateCode);
	}
	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal3=inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal2.getHeight()!=1||signal3.getHeight()!=1) {
				 MatDimException e=new MatDimException("The input port2 signal and input port3 signal of"+this.blockName+"must be Matrix(1*n)!\n");
				 throw(e);
			}
			if(signal2.getWidth()>signal3.getWidth()) {
				MatDimException e=new MatDimException("The order of the denominator must be greater than or equal to the order of the numerator.\n");
				 throw(e);
			}
			out.setHeight(signal1.getHeight());
			out.setWidth(signal1.getWidth());
			out.getOutputSignalC().setHeight(signal1.getHeight());
			out.getOutputSignalC().setWidth(signal1.getWidth());
			out.getOutputSignalC().setDataType(signal1.getDataType());
	}
	 public void checkDimension() throws MatDimException{

    }


}
