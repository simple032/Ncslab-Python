package com.ncslab.block.discrete;

import org.json.JSONObject;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class Delay extends DiscreteBlock {
	Parameter sampleTime;
	Parameter initialCondition;
	Parameter delayLength;
	public Delay(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,false));
		sampleTime=new Parameter(this,1,"sampleTime",paramValues.getString("SampleTime"));
		initialCondition=new Parameter(this,2,"initialCondition",paramValues.getString("InitialCondition"));
		delayLength=new Parameter(this,3,"delayLength",paramValues.getString("DelayLength"));
		parameterList.add(sampleTime);
		setSampleTime(sampleTime);
		parameterList.add(initialCondition);
		parameterList.add(delayLength);
  }

	 //define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block discrete_Delay:("+getBlockId()+")"+getBlockName()+"*/\n";
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 arraysCode+="double "+"Block"+getBlockId()+"_discrete_delay_savedata["+signal.getHeight()+"]["+signal.getWidth()+"*((int)"+paramValues.getDouble("DelayLength")+"+1)];\n";
		 code.addArraysCode(arraysCode);
	 }
	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Delay:("+getBlockId()+")"+getBlockName()+"*/\n";
			initCode+=sampleTime.getInitCodeC();
			initCode+=initialCondition.getInitCodeC();
			initCode+=delayLength.getInitCodeC();
			initCode+="sample_time[sample_i]="+sampleTime.getName()+";\n";
			initCode+="sample_i=sample_i+1;\n";
			code.addInitCode(initCode);
			}
	 public void generateOutputCodeC (CodeStructC code){
		  String outputCode="/*Code for output of block Delay:("+getBlockId()+")"+getBlockName()+"*/\n";

		  OutputPort out  = outputPortList.get(0);
		  OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		  OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		  int h=(int)paramValues.getDouble("DelayLength")+1;
		  outputCode+="{real_T currentTime = model.time;\n";
		  switch(signal.getDataType()) {
		  case REAL:
			  outputCode+="if(fabs((int)(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001&&mp->majorStep>0) {\n";
			  for(int i=0;i<(int)paramValues.getDouble("DelayLength");i++){
				  int k=i+1;
			  outputCode+="Block"+getBlockId()+"_discrete_delay_savedata[0]["+i+"]="+"Block"+getBlockId()+"_discrete_delay_savedata[0]["+k+"];\n";}
			  outputCode+="Block"+getBlockId()+"_discrete_delay_savedata[0][(int)"+paramValues.getDouble("DelayLength")+"]="+signal.getName()+";}\n";
			  outputCode+="if((currentTime+0.00001)<("+delayLength.getName()+"*"+sampleTime.getName()+")) {\n";
			  outputCode+=out.getOutputSignalC().getName()+"="+initialCondition.getName()+";}\n";
			  outputCode+="else {\n";
			  outputCode+="if(fabs((int)(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.00001) {\n";
			  outputCode+=out.getOutputSignalC().getName()+"=Block"+getBlockId()+"_discrete_delay_savedata[0][0];}}\n";
		      break;
		  case MATRIX:
			  for(int i=0; i<ops.getHeight(); i++) {
					for(int j=0;j<ops.getWidth();j++) {
						outputCode+="if(fabs((int)(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001&&mp->majorStep>0) {\n";
                          for(int n=0;n<(int)paramValues.getDouble("DelayLength");n++){
                                int k=n+1;
                                outputCode+="Block"+getBlockId()+"_discrete_delay_savedata["+i+"]["+n+"+"+h+"*"+j+"]="+"Block"+getBlockId()+"_discrete_delay_savedata["+i+"]["+k+"+"+h+"*"+j+"];\n";
                          }
                          outputCode+="Block"+getBlockId()+"_discrete_delay_savedata["+i+"][(int)"+paramValues.getDouble("DelayLength")+"+"+h+"*"+j+"]="+signal.getName()+"("+i+","+j+");}\n";
                          outputCode+="if(currentTime+0.00001<"+delayLength.getName()+"*"+sampleTime.getName()+") {\n";
                          outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+initialCondition.getName()+";}\n";
                          outputCode+="else {\n";
                          outputCode+="if(fabs((int)(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001) {\n";
                          outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=Block"+getBlockId()+"_discrete_delay_savedata["+i+"]["+h+"*"+j+"];}}\n";
				   }
				}
			  break;
			  }
		  outputCode+="}\n";
		  code.addOutputCode(outputCode);
		  }
    public void updateDimension() throws MatDimException{
    	super.updateDimension();
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

		if((Double.parseDouble(paramValues.getString("SampleTime").trim())*100)%(model.getConfig().getFixedStep()*100)>0.000001) {
			  MatDimException e=new MatDimException("Parameter(sampleTime/Initial condition/delay length) of Block "+this.blockName+" must be an integer multiple of the fixed-step size!\n \n");
				throw(e);
	  }
		out.setHeight(signal.getHeight());
		out.setWidth(signal.getWidth());
		out.getOutputSignalC().setHeight(signal.getHeight());
		out.getOutputSignalC().setWidth(signal.getWidth());
		out.getOutputSignalC().setDataType(signal.getDataType());
	}
	public void checkDimension() throws MatDimException{
		 if(sampleTime.getDataType()!=DataType.REAL||initialCondition.getDataType()!=DataType.REAL||delayLength.getDataType()!=DataType.REAL) {
				MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be a real double scalar(period) and the Parameter(initialCondition delayLength) can't be Matrix!\n \n");
				throw(e);
			}
	}
}
