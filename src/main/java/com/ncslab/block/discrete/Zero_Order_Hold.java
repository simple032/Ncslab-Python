package com.ncslab.block.discrete;

import com.ncslab.block.data.DataType;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class Zero_Order_Hold extends DiscreteBlock {

    Parameter sampleTime;
    State stateOutput;

    public Zero_Order_Hold(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new com.ncslab.block.io.InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		sampleTime=new Parameter(this,1,"sampleTime",paramValues.getString("SampleTime"));
		parameterList.add(sampleTime);

		setSampleTime(sampleTime);

        stateOutput = new State(this, 1, "output");
    }

	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Zero_Order_Hold:("+getBlockId()+")"+getBlockName()+"*/\n";
			initCode+=sampleTime.getInitCodeC();
			initCode+="sample_time[sample_i]="+sampleTime.getName()+";\n";
			initCode+="sample_i=sample_i+1;\n";
			code.addInitCode(initCode);
			}
	 public void generateOutputCodeC (CodeStructC code){
		  String outputCode="/*Code for output of block Zero_Order_Hold:("+getBlockId()+")"+getBlockName()+"*/\n";

		  OutputPort out  = outputPortList.get(0);
		  OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		  OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		  outputCode+="{real_T currentTime = model.time;\n";
		  outputCode+="real_T sampleTimeTmp = "+sampleTime.getName()+"==-1?model.stepSize:"+sampleTime.getName()+";\n";
		  switch(signal.getDataType()) {
		  case REAL:
//			  outputCode+="if(fabs(floor(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001) {\n";
			  outputCode+="if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001) {\n";
			  outputCode+=out.getOutputSignalC().getName()+"="+signal.getName()+";}\n";
			  break;
		  case MATRIX:
			  for(int i=0; i<ops.getHeight(); i++) {
					for(int j=0;j<ops.getWidth();j++) {
						outputCode+="if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001) {\n";
//						outputCode+="if(fabs(floor(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001) {\n";
						 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+");}\n";

					}
				}
			  break;
		  }
		  outputCode+="}\n";
		  code.addOutputCode(outputCode);
		 }


	 public void generateDiscreteUpdateCodeCInside(CodeStructC code) throws MatDimException{
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 String disceteUpdateCode="/*Code for discrete update of block Zero_Order_Hold(Inside):("+getBlockId()+")"+getBlockName()+"*/\n";
		 disceteUpdateCode+=stateOutput.getName()+"="+signal.getName()+";\n";
		 disceteUpdateCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+stateOutput.getName()+";\n";
		 code.addDiscreteUpdateCode(disceteUpdateCode);
	 }

	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if((Double.parseDouble(paramValues.getString("SampleTime").trim())*1000000)%(model.getConfig().getFixedStep()*1000000)>0.000001) {
				  MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be an integer multiple of the fixed-step size!\n \n");
					throw(e);
		  }
		    if(sampleTime.getDataType()!= DataType.REAL) {
			MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be a real double scalar(period)!\n \n");
			throw(e);
		}
		    out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());

			switch (signal.getDataType()) {
			case REAL:
				stateOutput = new State(this, 1, "stateOutput", 1, 1);
				break;
			case MATRIX:
				stateOutput = new State(this, 1, "stateOutput", signal.getHeight(),signal.getWidth());
				break;
			}
			stateList.add(stateOutput);
	 }
	 public void checkDimension() throws MatDimException{
	}
}
