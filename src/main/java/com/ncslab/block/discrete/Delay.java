package com.ncslab.block.discrete;

import lombok.Getter;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;
import org.json.JSONObject;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Delay extends DiscreteBlock {
	Parameter sampleTime;
	Parameter initialCondition;
	Parameter delayLength;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("sampleTime");
        parameterNames.add("initialCondition");
        parameterNames.add("delayLength");
        outputNames.add("out1");
        inputNames.add("in1");
    }
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
	 	context.put("blockId", getBlockId());
	 	context.put("blockName", getBlockName());
	 	context.put("sampleTime", sampleTime);
	 	context.put("initialCondition", initialCondition);
	 	context.put("delayLength", delayLength);

	 	String initCode = TemplateManager.renderTemplate("c/discrete/Delay/init.vm", context);
	 	code.addInitCode(initCode);
	 }
	 public void generateOutputCodeC(CodeStructC code) {
	 	context.put("blockId", getBlockId());
	 	context.put("blockName", getBlockName());
	 	context.put("inputPortList", getInputPortList());
	 	context.put("outputPortList", getOutputPortList());
	 	context.put("sampleTime", sampleTime);
	 	context.put("initialCondition", initialCondition);
	 	context.put("delayLength", delayLength.getDouble());

	 	String outputCode = TemplateManager.renderTemplate("c/discrete/Delay/output.vm", context);
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
