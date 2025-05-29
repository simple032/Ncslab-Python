package com.ncslab.block.continuous;

import com.ncslab.block.BlockType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.*;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.util.TemplateManager;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;

import java.util.Vector;

public class Derivative extends Block {
	private State stateIntegral;
	OutputPort output;
	InputPort input;

    Parameter cParam;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }
	// G=s/(Ts+1) T->0
	public Derivative(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		input = new InputPort(this, 1);
		inputPortList.add(input);
		output = new OutputPort(this, 1, true);
		outputPortList.add(output);

        cParam = new Parameter(this, 1, "c", "100");
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		context.put("block", this);
		context.put("state", stateIntegral);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/init.vm", context);
		code.addInitCode(codeStr);
	}
	public void generateArraysCodeC(CodeStructC code) {
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
		context.put("signal", signal);
		String arraysCode = TemplateManager.renderTemplate("c/continuous/Derivative/arrays.vm", context);
		code.addArraysCode(arraysCode);
	}
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		context.put("block", this);
		context.put("state", stateIntegral);
		context.put("input", getInputPortVariables()[0]);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		context.put("block", this);
		context.put("output", getOutputPortVariables()[0]);
		context.put("state", stateIntegral);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		context.put("block", this);
		context.put("signal", signal);
		context.put("state", stateIntegral);
		context.put("output", getOutputPortVariables()[0]);


		String codeStr = TemplateManager.renderTemplate("c/continuous/Derivative/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("signal", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC());
		context.put("state", stateIntegral);
		context.put("solver", this.model.getConfig().getSolver());

		String codeStr = TemplateManager.renderTemplate("c/continuous/Derivative/output.vm", context);
		code.addOutputCode(codeStr);
	}


	public void generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode = "/*Code for Derivative of block Derivative:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";

		code.addDerivativeCode(derivativeCode);
	}

	public void updateDimension() throws MatDimException {

		OutputPort out = outputPortList.get(0);
		InputPort in = inputPortList.get(0);
		OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:
			out.setHeight(1);
			out.setWidth(1);
			out.getOutputSignalC().setHeight(1);
			out.getOutputSignalC().setWidth(1);
			out.getOutputSignalC().setDataType(DataType.REAL);
			stateIntegral = new State(this, 1, "integral", 1, 1);
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
		stateList.add(stateIntegral);
	}

	public void checkDimension() throws MatDimException {
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:

			break;
		case MATRIX:

			break;
		}

	}

    @Override
    public void calculateInit(){
        OutputPort output = outputPortList.get(0);
        output.setData(stateIntegral.getData());
    }

    @Override
    public void calculateDerivative(double t){
        Data data = stateIntegral.getData().divide(cParam.getData())
            .plus(inputPortList.get(0).getData().divide(cParam.getData()));
        stateIntegral.setDerivateData(data);
    }

    @Override
    public void calculateOutput(double t){
        OutputPort output = outputPortList.get(0);
        Data data = stateIntegral.getData().divide(cParam.getData()).negative()
            .plus(inputPortList.get(0).getData().divide(cParam.getData()));
        output.setData(data);
    }
}
