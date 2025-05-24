package com.ncslab.block.source;

import com.ncslab.block.data.Data;

import com.ncslab.util.TemplateManager;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

import java.util.Vector;

public class Pulse extends Block{
	    Parameter amplitude;
	    Parameter period;
	    Parameter pulseWidth;
	    Parameter phaseDelay;


    // Removed @Getter as it might cause issues
    public static final Vector<String> parameterNames = new Vector<>();

    // Removed @Getter as it might cause issues
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");
        parameterNames.add("amplitude");
        parameterNames.add("period");
        parameterNames.add("pulseWidth");
        parameterNames.add("phaseDelay");


    }
	public Pulse(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
        amplitude=new Parameter(this,1,"amplitude",paramValues.getString("Amplitude"));
		period=new Parameter(this,2,"period",paramValues.getString("Period"));
		pulseWidth=new Parameter(this,3,"pulseWidth",paramValues.getString("PulseWidth"));
		phaseDelay=new Parameter(this,4,"phaseDelay",paramValues.getString("PhaseDelay"));
		parameterList.add(amplitude);
		parameterList.add(period);
		parameterList.add(pulseWidth);
		parameterList.add(phaseDelay);
		outputPortList.get(0).setHeight(amplitude.getHeight());
		outputPortList.get(0).setWidth(amplitude.getWidth());
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=amplitude.getInitCodeM();
		initCode+=period.getInitCodeM();
		initCode+=pulseWidth.getInitCodeM();
		initCode+=phaseDelay.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		context.put("block", this);
		context.put("outputs", getOutputPortVariables());
		context.put("amplitude", amplitude);
		context.put("period", period);
		context.put("pulseWidth", pulseWidth);
		context.put("phaseDelay", phaseDelay);

		String codeStr = TemplateManager.renderTemplate("m/source/Pulse/output.vm", context);
		code.addOutputCode(codeStr);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Pulse:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=amplitude.getInitCodeC();
		initCode+=period.getInitCodeC();
		initCode+=pulseWidth.getInitCodeC();
		initCode+=phaseDelay.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("outputs", getOutputPortVariables());
		context.put("amplitude", amplitude);
		context.put("period", period);
		context.put("pulseWidth", pulseWidth);
		context.put("phaseDelay", phaseDelay);

		String codeStr = TemplateManager.renderTemplate("c/source/Pulse/output.vm", context);
		code.addOutputCode(codeStr);
	}
	 public void updateDimension() throws MatDimException{
	    	if(amplitude.getWidth()!=period.getWidth()
	    			||amplitude.getWidth()!=pulseWidth.getWidth()
	    			||amplitude.getWidth()!=phaseDelay.getWidth()
	    			||amplitude.getHeight()!=period.getHeight()
	    			||amplitude.getHeight()!=pulseWidth.getHeight()
	    			||amplitude.getHeight()!=phaseDelay.getHeight()) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
	    	}
	    }
	@Override
	public void calculateOutput(double t) {
		// 实现具体的输出计算逻辑
		double amplitudeValue = amplitude.getData().getInitValue();
		double periodValue = period.getData().getInitValue();
		double pulseWidthValue = pulseWidth.getData().getInitValue();

		double output = amplitudeValue * (t % periodValue < pulseWidthValue ? 1 : 0);
		outputPortList.get(0).getOutputSignalC().setValue(output);
	}

	@Override
	public void calculateInit() {
		// 初始化逻辑
		outputPortList.get(0).getOutputSignalC().setValue(0.0);
	}
}
// Removed extra closing brace if present
