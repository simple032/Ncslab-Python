package com.ncslab.block.discrete;

import java.util.Vector;

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
import com.ncslab.block.io.State;

public class Discrete_Transfer_Fcn extends Block{
	Parameter sampleTime;
	Parameter num;
	Parameter den;
	Parameter initialStates;
	private boolean feedThrough=false;
	private Vector<State> xStateList=new Vector<State>();
	private State xState;


	public Discrete_Transfer_Fcn(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,feedThrough));
		sampleTime=new Parameter(this,1,"sampleTime",paramValues.getString("SampleTime"));
		num=new Parameter(this,2,"num",paramValues.getString("Numerator"));
		den=new Parameter(this,3,"den",paramValues.getString("Denominator"));
		initialStates=new Parameter(this,4,"initialStates",paramValues.getString("InitialStates"));
		for(int i=0;i<den.getWidth()-1;i++) {
			State xState=new State(this,i+1,"x"+(i+1));
			xStateList.add(xState);
			stateList.add(xState);
		}
		parameterList.add(sampleTime);
		parameterList.add(num);
		parameterList.add(den);
		parameterList.add(initialStates);
  }

	//define arrays to save data
		 public void generateArraysCodeC(CodeStructC code) {
			 String arraysCode="/*Define arrays for block discrete_Delay:("+getBlockId()+")"+getBlockName()+"*/\n";
			 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			 arraysCode+="double "+"Block"+getBlockId()+"_discrete_transfer_savedata[2];\n";
			 code.addArraysCode(arraysCode);
		 }

	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Discrete_Transfer_Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
			initCode+=sampleTime.getInitCodeC();
			initCode+=num.getInitCodeC();
			initCode+=den.getInitCodeC();
			initCode+=initialStates.getInitCodeC();
			  int i=0;
				for(State xState:xStateList) {
					initCode+=xState.getName()+
							"="+initialStates.getName()+"(0,"+i+");\n";
					i++;
				}
			code.addInitCode(initCode);
			}
	 public void generateOutputCodeC(CodeStructC code) {
		    OutputPort out  = outputPortList.get(0);
			OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
			OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		  String outputCode="/*Code for output of discrete_transfer_fun:("+getBlockId()+")"+getBlockName()+"*/\n";
		  outputCode+="{real_T currentTime = model.time;\n";
		  outputCode+="real_T sampleTimeTmp = "+sampleTime.getName()+"==-1?model.stepSize:"+sampleTime.getName()+";\n";
		  outputCode+="if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001) {\n";
//		  outputCode+="if(fabs(floor(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001) {\n";
		  int h=den.getWidth()-num.getWidth()-1;


		  if(feedThrough==true) {
		  outputCode+=this.getOutputPortVariable(0)+"=Block"+getBlockId()+"_discrete_transfer_savedata[0]*"+num.getName()+"(0,0)";
		  if(num.getWidth()==1) {
		  }
		  else {
			  for(int i=1;i<num.getWidth();i++) {
				 outputCode+="+"+num.getName()+"(0,"+i+")*"+xStateList.get(i-1).getName();
			  }
		  }
	    }
		  else {
			  outputCode+=this.getOutputPortVariable(0)+"=0";
			  for(int j=0;j<num.getWidth();j++) {
				  outputCode+="+"+num.getName()+"(0,"+j+")*"+xStateList.get(j+h).getName();
			  }
		  }
		  outputCode+=";}}\n";
		  code.addOutputCode(outputCode);
	 }


	public void  generateUpdateCodeC(CodeStructC code) {
		    OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			String updateCode="/*Code for Update of  Discrete_Transfer_Fcn "+ ":("+getBlockId()+")"+getBlockName()+"*/\n";
			updateCode+="Block"+getBlockId()+"_discrete_transfer_savedata[0]=("+signal.getName();
			for(int i=1;i<den.getWidth();i++) {
			updateCode+="-"+den.getName()+"(0,"+i+")*"+xStateList.get(i-1).getName();
			}
			updateCode+=")/"+den.getName()+"(0,0);\n";
			for(int i=xStateList.size()-1;i>0;i--) {
			updateCode+=xStateList.get(i).getName()+"="+xStateList.get(i-1).getName()+";\n";
			}
			updateCode+=xStateList.get(0).getName()+"=Block"+getBlockId()+"_discrete_transfer_savedata[0];\n";
			code.addUpdateCode(updateCode);
	}


    public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(den.getWidth()==num.getWidth()) {
			feedThrough=true;
		}
		if((Double.parseDouble(paramValues.getString("SampleTime").trim())*1000000)%(model.getConfig().getFixedStep()*1000000)>0.000001) {
			  MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be an integer multiple of the fixed-step size!\n \n");
				throw(e);
	  }

		out.setHeight(signal.getHeight());
		out.setWidth(signal.getWidth());
		out.getOutputSignalC().setHeight(signal.getHeight());
		out.getOutputSignalC().setWidth(signal.getWidth());
		out.getOutputSignalC().setDataType(signal.getDataType());
	}

	public void checkDimension() throws MatDimException{
		 if(sampleTime.getDataType()!=DataType.REAL) {
				MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be a real double scalar(period)!\n \n");
				throw(e);
		}
	}
}
