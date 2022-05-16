package block.continuous;

import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.State;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;

public class Derivative extends Block {
	private State stateIntegral;
	OutputPort output;
	InputPort input;

	// G=s/(Ts+1) T->0
	public Derivative(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		input = new InputPort(this, 1);
		inputPortList.add(input);
		output = new OutputPort(this, 1, true);
		outputPortList.add(output);

	}

	public void generateInitCodeM(CodeStructM code) {
		String initCode = "";

		super.generateInitCodeM(code);

		initCode += stateIntegral.getName() + "=0;\n";

		code.addInitCode(initCode);
	}
	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block Derivative(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		arraysCode+= "double " + "Block" + getBlockId() + "tout["+signal.getHeight()+"]["+signal.getWidth()+"];\n";
		arraysCode+= "double " + "Block" + getBlockId() + "yout["+signal.getHeight()+"]["+signal.getWidth()+"];\n";
		code.addArraysCode(arraysCode);
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
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String initCode = "/*Code for initialization of block Derivative:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		switch (signal.getDataType()) {
		case REAL:
			initCode += stateIntegral.getName() + "=0;\n";
			break;
		case MATRIX:
			for (int i = 0; i < signal.getHeight(); i++) {
				for (int j = 0; j < signal.getWidth(); j++) {
					initCode += stateIntegral.getName() + "(" + i + "," + j + ")=0;\n";
				}
			}
			break;
		}
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode = "/*Code for output of block Derivative:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String ss=this.model.getConfig().getSolver();
		if(ss.equals("ode1")||ss.equals("ode2")||ss.equals("ode3")||ss.equals("ode4")||ss.equals("ode5")||ss.equals("ode6")) {
		switch (signal.getDataType()) {
		case REAL:
			  outputCode+="if(mp->majorStep>0) {\n";
			outputCode += this.getOutputPortVariable(0) + "=(" + signal.getName() + "-" + stateIntegral.getName()
					+ ")/model.stepSize" + ";\n";
			outputCode += stateIntegral.getName() + "=" + signal.getName() + ";\n";
			outputCode+="}\n";
			break;
		case MATRIX:
			for (int i = 0; i < signal.getHeight(); i++) {
				for (int j = 0; j < signal.getWidth(); j++) {
					outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")" + "=(" + signal.getName()
							+ "(" + i + "," + j + ")" + "-" + stateIntegral.getName() + "(" + i + "," + j + ")"
							+ ")/model.stepSize" + ";\n";
					outputCode += stateIntegral.getName() + "(" + i + "," + j + ")" + "=" + signal.getName() + "(" + i
							+ "," + j + ")" + ";\n";
				}
			}
			break;
		  }
		}else {
			switch (signal.getDataType()) {
			case REAL:
				outputCode+="if(mp->majorStep>0) {\n";
				 outputCode += this.getOutputPortVariable(0) + "=(" + signal.getName() + "-" + "Block" + getBlockId() + "yout[0][0]"
						+ ")/(model.time-Block" + getBlockId() + "tout[0][0]);\n";
				 outputCode +="Block" + getBlockId() + "yout[0][0]=" + signal.getName() + ";\n";
				  outputCode+="Block" + getBlockId() + "tout[0][0]=model.time;\n";
				  outputCode+="}\n";
				break;	
			case MATRIX:
				outputCode+="if(mp->majorStep>0) {\n";
				for (int i = 0; i < signal.getHeight(); i++) {
					for (int j = 0; j < signal.getWidth(); j++) {
						outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")" + "=(" + signal.getName()	+ "(" + i + "," + j + ")" + "-" + "Block" + getBlockId() + "yout[" + i+ "][" + j + "])/(model.time-Block" + getBlockId() + "tout[" + i+ "][" + j + "]);\n";
						outputCode += "Block" + getBlockId() + "yout[" + i+ "][" + j + "]=" + signal.getName() + "(" + i+ "," + j + ")" + ";\n";
						 outputCode+="Block" + getBlockId() + "tout[" + i+ "][" + j + "]=model.time;\n";
					}
				}
				 outputCode+="}\n";
				break;
		  }
		}
		code.addOutputCode(outputCode);
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
