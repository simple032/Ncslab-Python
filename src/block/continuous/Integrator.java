package block.continuous;

import org.json.JSONObject;
//import java.util.Vector;

import block.Block;
import block.data.DataType;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.OutputSignal;

public class Integrator extends Block {
	private State stateIntegral;
	private Parameter initialCondition;

	OutputPort output;
	InputPort input;

	public Integrator(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);
		initialCondition = new Parameter(this, 1, "InitialCondition", paramValues.getString("InitialCondition"));
		parameterList.add(initialCondition);
		input = new InputPort(this, 1);
		inputPortList.add(input);
		output = new OutputPort(this, 1, false);
		outputPortList.add(output);
	}

	public void generateInitCodeM(CodeStructM code) {
		String initCode = "";

		super.generateInitCodeM(code);

		initCode += initialCondition.getName() + "=" + paramValues.getDouble("InitialCondition") + ";\n";
		initCode += stateIntegral.getName() + "=" + initialCondition.getName() + ";\n";

		code.addInitCode(initCode);
	}

	public void generateDerivativeCodeM(CodeStructM code) {
		String derivativeCode = "";

		super.generateDerivativeCodeM(code);

		derivativeCode += stateIntegral.getDerivativeName() + "="
				+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ";\n";

		code.addDerivativeCode(derivativeCode);
	}

	public void generateOutputCodeM(CodeStructM code) {

		String outputCode = "";

		super.generateOutputCodeM(code);

		outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=" + stateIntegral.getName() + ";\n";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode = "/*Code for initialization of block Intergator:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:
			switch (initialCondition.getDataType()) {
			case REAL:
				initCode += initialCondition.getInitCodeC();
				initCode += stateIntegral.getName() + "=" + initialCondition.getName() + ";\n";
				break;
			case MATRIX:
				initCode += initialCondition.getInitCodeC();
				for (int i = 0; i < initialCondition.getHeight(); i++) {
					for (int j = 0; j < initialCondition.getWidth(); j++) {
						initCode += stateIntegral.getName() + "(" + i + "," + j + ")=" + initialCondition.getName()
								+ "(" + i + "," + j + ")" + ";\n";
					}
				}
				break;
			}
			break;
		case MATRIX:
			switch (initialCondition.getDataType()) {
			case REAL:
				initCode += initialCondition.getInitCodeC();
				for (int i = 0; i < signal.getHeight(); i++) {
					for (int j = 0; j < signal.getWidth(); j++) {
						initCode += stateIntegral.getName() + "(" + i + "," + j + ")=" + initialCondition.getName()
								+ ";\n";
					}
				}
				break;
			case MATRIX:
				initCode += initialCondition.getInitCodeC();
				initCode += stateIntegral.getName() + "=" + initialCondition.getName() + ";\n";
				break;
			}
			break;
		}
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode = "/*Code for output of block Intergator:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:
			switch (initialCondition.getDataType()) {
			case REAL:
				outputCode += this.getOutputPortVariable(0) + "=" + stateIntegral.getName() + ";\n";
				break;
			case MATRIX:
				for (int i = 0; i < initialCondition.getHeight(); i++) {
					for (int j = 0; j < initialCondition.getWidth(); j++) {
						outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")=" + stateIntegral.getName()
								+ "(" + i + "," + j + ")" + ";\n";
					}
				}
				break;
			}
			break;
		case MATRIX:
			for (int i = 0; i < signal.getHeight(); i++) {
				for (int j = 0; j < signal.getWidth(); j++) {
					outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")=" + stateIntegral.getName()
							+ "(" + i + "," + j + ")" + ";\n";
				}
			}
//			outputCode += this.getOutputPortVariable(0) + "=" + stateIntegral.getName() + ";\n";
			break;
		}
		outputCode += "\n";
		code.addOutputCode(outputCode);
	}

	public void generateDerivativeCodeC(CodeStructC code) {

		String derivativeCode = "/*Code for Derivative of block Intergator:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:
			switch (initialCondition.getDataType()) {
			case REAL:
				derivativeCode += stateIntegral.getDerivativeName() + "="
						+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
						+ ";\n";
				break;
			case MATRIX:
				for (int i = 0; i < initialCondition.getHeight(); i++) {
					for (int j = 0; j < initialCondition.getWidth(); j++) {
						derivativeCode += stateIntegral.getDerivativeName() + "(" + i + "," + j + ")=" + inputPortList
								.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ";\n";
					}
				}
				break;
			}
			break;
		case MATRIX:
			for (int i = 0; i < signal.getHeight(); i++) {
				for (int j = 0; j < signal.getWidth(); j++) {
					derivativeCode += stateIntegral.getDerivativeName() + "(" + i + "," + j + ")="
							+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
							+ "(" + i + "," + j + ")" + ";\n";
				}
			}
//			derivativeCode += stateIntegral.getDerivativeName() + "="
//					+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ";\n";
			break;
		}
		code.addDerivativeCode(derivativeCode);

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
				stateIntegral = new State(this, 1, "integral", initialCondition.getHeight(),
						initialCondition.getWidth());
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
}
