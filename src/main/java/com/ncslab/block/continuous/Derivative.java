package com.ncslab.block.continuous;

import com.ncslab.block.BlockType;
import com.ncslab.block.io.*;
import lombok.Getter;
import org.json.JSONObject;
import org.apache.velocity.VelocityContext;
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

	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("state", stateIntegral);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/init.vm", context);
		code.addInitCode(codeStr);
	}
	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block Derivative(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("signal", signal);
		code.addArraysCode(arraysCode);
	}
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("state", stateIntegral);
		context.put("input", getInputPortVariables()[0]);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("output", getOutputPortVariables()[0]);
		context.put("state", stateIntegral);

		String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		VelocityContext context = new VelocityContext();
		context.put("block", this);
        context.put("realDataType", DataType.REAL);
		context.put("signal", signal);
		context.put("state", stateIntegral);
		context.put("output", getOutputPortVariables()[0]);


		String codeStr = TemplateManager.renderTemplate("c/continuous/Derivative/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
        context.put("realDataType", DataType.REAL);
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

	public void generateUpdateCodeC(CodeStructC code) throws MatDimException {
		String updateCode = "/*Code for update of block " + getBlockType() + ":(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		code.addUpdateCode(updateCode);
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
}
