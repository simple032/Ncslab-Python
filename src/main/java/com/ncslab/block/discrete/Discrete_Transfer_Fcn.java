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

public class Discrete_Transfer_Fcn extends DiscreteBlock{
	Parameter sampleTime;
	Parameter num;
	Parameter den;
	Parameter initialStates;
	private boolean feedThrough=false;
	private State xState;
	
	
	public Discrete_Transfer_Fcn(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,feedThrough));
		sampleTime=new Parameter(this,1,"sampleTime",paramValues.getString("SampleTime"));
		num=new Parameter(this,2,"num",paramValues.getString("Numerator"));
		den=new Parameter(this,3,"den",paramValues.getString("Denominator"));
		initialStates=new Parameter(this,4,"initialStates",paramValues.getString("InitialStates"));
		/*for(int i=0;i<den.getWidth()-1;i++) {
			State xState=new State(this,i+1,"x"+(i+1));
			xStateList.add(xState);	
			stateList.add(xState);
		}	*/
		
		parameterList.add(sampleTime);
		
		setSampleTime(sampleTime);
		
		parameterList.add(num);
		parameterList.add(den);	
		parameterList.add(initialStates);
  }
	
	//define arrays to save data
	
		 //public void generateArraysCodeC(CodeStructC code) {
		//	 String arraysCode="/*Define arrays for block discrete_Transfer_Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
			// OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			 //arraysCode+="double "+"Block"+getBlockId()+"_discrete_transfer_savedata["+signal.getHeight()+"]["+signal.getWidth()+"];\n";
			 //code.addArraysCode(arraysCode);
		 //}
		 
	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Discrete_Transfer_Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
			initCode+=sampleTime.getInitCodeC();
			initCode+="sample_time[sample_i]="+sampleTime.getName()+";\n";
			initCode+="sample_i=sample_i+1;\n";
			initCode+=num.getInitCodeC();
			initCode+=den.getInitCodeC();
			initCode+=initialStates.getInitCodeC();
		    int k;
		    if(initialStates.getDataType()==DataType.REAL&&xState.getDataType()==DataType.REAL) {
		    	initCode+=xState.getName()+"(0,0)="+initialStates.getName()+";\n";
		    }else {
			for(int i=0;i<xState.getHeight();i++) {
				for(int j=0;j<xState.getWidth();j++) {
					k=j%(den.getWidth()-1);
					initCode+=xState.getName()+"("+i+","+j+")="+initialStates.getName()+"(0,"+k+");\n";
				}
			  }
		    }
			code.addInitCode(initCode);
			}
	 
	 public void generateDiscreteUpdateCodeCInside(CodeStructC code) throws MatDimException{
		 
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 
		 String disceteUpdateCode="/*Code for discrete update of discrete_transfer_fun(Inside):("+getBlockId()+")"+getBlockName()+"*/\n";
		 disceteUpdateCode+="double "+"Block"+getBlockId()+"_discrete_transfer_savedata["+signal.getHeight()+"]["+signal.getWidth()+"];\n";
		 
		 switch(signal.getDataType()) {
		  case REAL:
			  
			  disceteUpdateCode+="Block"+getBlockId()+"_discrete_transfer_savedata[0][0]=("+signal.getName();
			  for(int i=1;i<den.getWidth();i++) {
				disceteUpdateCode+="-"+den.getName()+"(0,"+i+")*"+xState.getName()+"(0,"+(i-1)+")";
			  }
			  disceteUpdateCode+=")/"+den.getName()+"(0,0);\n";
			  for(int i=den.getWidth()-2;i>0;i--) {
				disceteUpdateCode+=xState.getName()+"(0,"+i+")="+xState.getName()+"(0,"+i+"-1);\n";
			  }
			  disceteUpdateCode+=xState.getName()+"(0,0)=Block"+getBlockId()+"_discrete_transfer_savedata[0][0];\n";
			  break;
		  case MATRIX:
			  for(int m=0;m<signal.getHeight();m++) {
				  for(int n=0;n<signal.getWidth();n++) {
					  disceteUpdateCode+="Block"+getBlockId()+"_discrete_transfer_savedata["+m+"]["+n+"]=("+signal.getName()+"("+m+","+n+")";
					  for(int i=1;i<den.getWidth();i++) {
						  disceteUpdateCode+="-"+den.getName()+"(0,"+i+")*"+xState.getName()+"("+m+","+(i-1)+"+"+n+"*"+(den.getWidth()-1)+")";
					  }
					  disceteUpdateCode+=")/"+den.getName()+"(0,0);\n";
				  }
			  }
			  for(int m=0;m<signal.getHeight();m++) {
				  for(int n=0;n<signal.getWidth();n++) {
					  for(int i=den.getWidth()-2;i>0;i--) {
						  disceteUpdateCode+=xState.getName()+"("+m+","+n+"*"+(den.getWidth()-1)+"+"+i+")="+xState.getName()+"("+m+","+n+"*"+(den.getWidth()-1)+"+"+i+"-1);\n";
					  }
					  disceteUpdateCode+=xState.getName()+"("+m+","+n+"*"+(den.getWidth()-1)+")=Block"+getBlockId()+"_discrete_transfer_savedata["+m+"]["+n+"];\n";
				  }
			  }
			  break;
		 }
		 
		 code.addDiscreteUpdateCode(disceteUpdateCode);
	 }
	 
	 public void generateOutputCodeC(CodeStructC code) {
		    //OutputPort out  = outputPortList.get(0);
			//OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
			OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		  String outputCode="/*Code for output of discrete_transfer_fun:("+getBlockId()+")"+getBlockName()+"*/\n";
		  //outputCode+="{real_T currentTime = model.time;\n";
		  //outputCode+="if(fabs(floor(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001&&mp->majorStep>0) {\n";
		  //int h=den.getWidth()-num.getWidth()-1;
		  outputCode+="double "+"Block"+getBlockId()+"_discrete_transfer_savedata["+signal.getHeight()+"]["+signal.getWidth()+"];\n";
		  switch(signal.getDataType()) {
			  case REAL:
				  /*
		  outputCode+="Block"+getBlockId()+"_discrete_transfer_savedata[0][0]=("+signal.getName();
			for(int i=1;i<den.getWidth();i++) {
				outputCode+="-"+den.getName()+"(0,"+i+")*"+xState.getName()+"(0,"+(i-1)+")";
			}
			outputCode+=")/"+den.getName()+"(0,0);\n";*/
			
			if(feedThrough==true) {
				  outputCode+=this.getOutputPortVariable(0)+"=Block"+getBlockId()+"_discrete_transfer_savedata[0][0]*"+num.getName()+"(0,0)";
				  if(num.getWidth()==1) {
				  }
				  else {
					  for(int i=1;i<num.getWidth();i++) {
						 outputCode+="+"+num.getName()+"(0,"+i+")*"+xState.getName()+"(0,"+i+"-1)";
					  }
				  }
			    }
				  else {
					  outputCode+=this.getOutputPortVariable(0)+"=0";
					  for(int j=0;j<num.getWidth();j++) {
						  outputCode+="+"+num.getName()+"(0,"+j+")*"+xState.getName()+"(0,"+j+")";
					  }
				  }  
				  outputCode+=";\n";
			/*
			for(int i=den.getWidth()-2;i>0;i--) {
				outputCode+=xState.getName()+"(0,"+i+")="+xState.getName()+"(0,"+i+"-1);\n";
			}
			outputCode+=xState.getName()+"(0,0)=Block"+getBlockId()+"_discrete_transfer_savedata[0][0];\n";
			*/
			  //outputCode+="}}\n";
			  break;
			  case MATRIX:
				  for(int m=0;m<signal.getHeight();m++) {
					  for(int n=0;n<signal.getWidth();n++) {
						  /*
						  outputCode+="Block"+getBlockId()+"_discrete_transfer_savedata["+m+"]["+n+"]=("+signal.getName()+"("+m+","+n+")";
							for(int i=1;i<den.getWidth();i++) {
								outputCode+="-"+den.getName()+"(0,"+i+")*"+xState.getName()+"("+m+","+(i-1)+"+"+n+"*"+(den.getWidth()-1)+")";
							}
							outputCode+=")/"+den.getName()+"(0,0);\n";
							*/
							
							if(feedThrough==true) {
								  outputCode+=this.getOutputPortVariable(0)+"("+m+","+n+")=Block"+getBlockId()+"_discrete_transfer_savedata["+m+"]["+n+"]*"+num.getName()+"(0,0)";
								  if(num.getWidth()==1) {
								  }
								  else {
									  for(int i=1;i<num.getWidth();i++) {
										 outputCode+="+"+num.getName()+"(0,"+i+")*"+xState.getName()+"("+m+","+(i-1)+"+"+n+"*"+(den.getWidth()-1)+")";
									  }
								  }
							    }
								  else {
									  outputCode+=this.getOutputPortVariable(0)+"("+m+","+n+")=0";
									  for(int j=0;j<num.getWidth();j++) {
										  outputCode+="+"+num.getName()+"(0,"+j+")*"+xState.getName()+"("+m+","+j+"+"+n+"*"+(den.getWidth()-1)+")";
									  }
								  }  
								  outputCode+=";\n";  
					  }
				  }
				  /*
				  for(int m=0;m<signal.getHeight();m++) {
					  for(int n=0;n<signal.getWidth();n++) {
						  for(int i=den.getWidth()-2;i>0;i--) {
							  outputCode+=xState.getName()+"("+m+","+n+"*"+(den.getWidth()-1)+"+"+i+")="+xState.getName()+"("+m+","+n+"*"+(den.getWidth()-1)+"+"+i+"-1);\n";
						      }
							outputCode+=xState.getName()+"("+m+","+n+"*"+(den.getWidth()-1)+")=Block"+getBlockId()+"_discrete_transfer_savedata["+m+"]["+n+"];\n";
						  }
					  }
					  */
				  //outputCode+="}}\n";
				  break;
		  }
		  code.addOutputCode(outputCode);
	 }
	 
	 
	public void  generateUpdateCodeC(CodeStructC code) {
		    OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			String updateCode="/*Code for Update of  Discrete_Transfer_Fcn "+ ":("+getBlockId()+")"+getBlockName()+"*/\n";
			code.addUpdateCode(updateCode);
	} 
    public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		xState=new State(this,1,"x",signal.getHeight(),signal.getWidth()*(den.getWidth()-1));	
		stateList.add(xState);
		if(den.getWidth()==num.getWidth()) {
			feedThrough=true;
		}
		System.out.println(feedThrough);
		 if(sampleTime.getDataType()!=DataType.REAL) {
				MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be a real double scalar(period)!\n \n");
				throw(e);	
		}
		if((Double.parseDouble(paramValues.getString("SampleTime").trim())*100)%(model.getConfig().getFixedStep()*100)>0.000001) {
			  MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be an integer multiple of the fixed-step size!\n \n");
				throw(e);
	   }
		if(den.getWidth()!=initialStates.getWidth()+1) {
			 MatDimException e=new MatDimException("Den's dimensions and initialStates' dimensions of Block "+this.blockName+" doesn't match!\n Den's dimensions is 1*n,then initialStates dimensions is 1*(n-1)!\n ");
				throw(e);
		}else {
			if(initialStates.getDataType()==DataType.MATRIX&&initialStates.getWidth()==1) {
				 MatDimException e=new MatDimException("If Den's dimensions is 1*2,then initialStates should be REAL!\n");
				 throw(e); 
				}
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
