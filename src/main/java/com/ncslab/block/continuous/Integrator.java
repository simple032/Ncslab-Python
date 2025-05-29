package com.ncslab.block.continuous;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.util.TemplateManager;
//import java.util.Vector;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.OutputSignal;

import java.util.Vector;

public class Integrator extends Block {
	private State stateIntegral;
	private Parameter initialCondition;

	OutputPort output;
	InputPort input;

    Parameter externalReset;//zhou_20240507 add externalReset
    Parameter conditionSource;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();


    //这个的inputNames需要再次修改
    static {
        parameterNames.add("InitialCondition");
        parameterNames.add("externalReset");//zhou_20240507 add externalReset
        parameterNames.add("conditionSource");
        outputNames.add("out1");
        inputNames.add("in1");
    }

	public Integrator(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);
		initialCondition = new Parameter(this, 1, "InitialCondition", paramValues.getString("InitialCondition"));
		parameterList.add(initialCondition);
		input = new InputPort(this, 1);
		inputPortList.add(input);
		output = new OutputPort(this, 1, false);
		outputPortList.add(output);

        //zhou_20240514 add externalReset
        externalReset=new Parameter(this,parameterList.size()+1,"externalReset",paramValues.optString("IntegratorExternalReset", "none"));
        parameterList.add(externalReset);
        conditionSource=new Parameter(this,parameterList.size()+1,"conditionSource",paramValues.optString("InitialConditionSource", "External"));
        parameterList.add(conditionSource);
        if(!externalReset.equals("none")&&conditionSource.equals("External")) {
            inputPortList.add(new InputPort(this,2));
            inputPortList.add(new InputPort(this,3));
        }else if(!externalReset.equals("none")&&conditionSource.equals("Internal")
            || externalReset.equals("none")&&conditionSource.equals("External")) {
            inputPortList.add(new InputPort(this,2));
        }
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		context.put("block", this);
		context.put("state", stateIntegral);
		context.put("initialCondition", initialCondition);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Integrator/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		context.put("block", this);
		context.put("state", stateIntegral);
		context.put("input", getInputPortVariables()[0]);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Integrator/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}

    //define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("externalReset",externalReset.getData().getInitString());
        context.put("conditionSource",conditionSource.getData().getInitString());
        String arraysCode = TemplateManager.renderTemplate("c/continuous/Integrator/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		context.put("block", this);
		context.put("state", stateIntegral);
		context.put("output", getOutputPortVariables()[0]);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Integrator/output.vm", context);
		code.addOutputCode(codeStr);
	}

    // TODO: requires check test


	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
        context.put("block", this);
        context.put("externalReset",externalReset.getData().getInitString());
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        InputPort inputPort;
        context.put("block", this);
        context.put("signal", signal);
        context.put("state", stateIntegral.getName());
        context.put("initialCondition", initialCondition);
        if(conditionSource.equals("External")){
            if(externalReset.equals("none")) {
                inputPort = inputPortList.get(1);
            }else {
                inputPort = inputPortList.get(2);
            }
            context.put("input", inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        }
        String initCode = TemplateManager.renderTemplate("c/continuous/Integrator/init.vm", context);
		code.addInitCode(initCode);
	}




	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("externalReset", externalReset.getData().getInitString());
		context.put("conditionSource", conditionSource.getData().getInitString());
        context.put("state", stateIntegral);
        context.put("outputs", getOutputPortVariables());
		String codeStr = TemplateManager.renderTemplate("c/continuous/Integrator/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("externalReset", externalReset.getData().getInitString());
		context.put("conditionSource", conditionSource.getData().getInitString());
		context.put("state", stateIntegral);
        context.put("stateDerivative", stateIntegral.getDerivativeName());
        context.put("inputs", getInputPortVariables());
        String codeStr = TemplateManager.renderTemplate("c/continuous/Integrator/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}
	public void updateDimension() throws MatDimException {

		OutputPort out = outputPortList.get(0);
		InputPort in = inputPortList.get(0);
		OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:
			switch (initialCondition.getDataType()) {
			case REAL:
				out.setHeight(1);
				out.setWidth(1);
				out.getOutputSignalC().setHeight(1);
				out.getOutputSignalC().setWidth(1);
				out.getOutputSignalC().setDataType(DataType.REAL);
				stateIntegral = new State(this, 1, "integral", 1, 1);
				break;
			case MATRIX:
				out.setHeight(initialCondition.getHeight());
				out.setWidth(initialCondition.getWidth());
				out.getOutputSignalC().setHeight(initialCondition.getHeight());
				out.getOutputSignalC().setWidth(initialCondition.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				stateIntegral = new State(this, 1, "integral", initialCondition.getHeight(),initialCondition.getWidth());
				break;
			}
			break;
		case MATRIX:
			switch (initialCondition.getDataType()) {
			case REAL:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				stateIntegral = new State(this, 1, "integral", signal.getHeight(), signal.getWidth());
				break;
			case MATRIX:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				stateIntegral = new State(this, 1, "integral", signal.getHeight(), signal.getWidth());
				break;
			}
			break;
		}
		stateList.add(stateIntegral);
	}

	public void checkDimension() throws MatDimException {
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:

			break;
		case MATRIX:
			switch (initialCondition.getDataType()) {
			case REAL:

				break;
			case MATRIX:
				if (initialCondition.getHeight() != signal.getHeight()
						|| initialCondition.getWidth() != signal.getWidth()) {
					MatDimException e = new MatDimException(
							"Dimension of input signal and Block " + this.blockName + " input dimension don't match!");
					throw (e);
				}
				break;
			}
			break;
		}

	}

    @Override
    public void calculateInit(){
        OutputPort output = outputPortList.get(0);
        stateIntegral.setData(initialCondition.getData());
        output.setData(stateIntegral.getData());
    }

    @Override
    public void calculateDerivative(double t){
        InputPort inputPort = inputPortList.get(0);
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
    	stateIntegral.setDerivateData(signal.getData());
    }

    @Override
    public void calculateOutput(double t){
        OutputPort output = outputPortList.get(0);
        output.setData(stateIntegral.getData());
    }
}
