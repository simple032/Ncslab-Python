package com.ncslab.block.discontinuous;

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

public class DeadZone extends Block {
	Parameter lowervalue;
	Parameter uppervalue;

	public DeadZone(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);
		inputPortList.add(new InputPort(this, 1));
		outputPortList.add(new OutputPort(this, 1, true));
		lowervalue = new Parameter(this, 1, "lowervalue", paramValues.getString("LowerValue"));
		uppervalue = new Parameter(this, 2, "uppervalue", paramValues.getString("UpperValue"));
		parameterList.add(lowervalue);
		parameterList.add(uppervalue);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode = "";
		initCode += uppervalue.getInitCodeM();
		initCode += lowervalue.getInitCodeM();
		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		OutputPort out = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String outputCode = "";
		switch (uppervalue.getDataType()) {
			case REAL:
				switch (ops.getOutputSignalC().getDataType()) {
					case REAL:
						outputCode += "if " + signal.getName() + ">" + uppervalue.getName() + "\n";
						outputCode += out.getOutputSignalC().getName() + "=" + signal.getName() + "-"
								+ uppervalue.getName() + ";\n";
						outputCode += "elseif " + signal.getName() + "<" + lowervalue.getName() + "\n";
						outputCode += out.getOutputSignalC().getName() + "=" + signal.getName() + "-"
								+ lowervalue.getName() + ";\n";
						outputCode += "else\n";
						outputCode += out.getOutputSignalC().getName() + "=0;\n";
						outputCode += "end\n";
						break;
					case MATRIX:
						for (int i = 1; i < ops.getHeight() + 1; i++) {
							for (int j = 1; j < ops.getWidth() + 1; j++) {
								outputCode += "if " + signal.getName() + "(" + i + "," + j + ")>" + uppervalue.getName()
										+ "\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")="
										+ signal.getName() + "(" + i + "," + j + ")-" + uppervalue.getName() + ";\n";
								outputCode += "elseif " + signal.getName() + "(" + i + "," + j + ")<"
										+ lowervalue.getName() + "\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")="
										+ signal.getName() + "(" + i + "," + j + ")-" + lowervalue.getName() + ";\n";
								outputCode += "else\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=0;\n";
								outputCode += "end\n";
							}
						}
						break;
				}
				break;
			case MATRIX:
				switch (ops.getOutputSignalC().getDataType()) {
					case REAL:
						for (int i = 1; i < uppervalue.getHeight() + 1; i++) {
							for (int j = 1; j < uppervalue.getWidth() + 1; j++) {
								outputCode += "if " + signal.getName() + ">" + uppervalue.getName() + "(" + i + "," + j
										+ ")\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")="
										+ signal.getName() + "-" + uppervalue.getName() + "(" + i + "," + j + ");\n";
								outputCode += "elseif " + signal.getName() + "<" + lowervalue.getName() + "(" + i + ","
										+ j + ")\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")="
										+ signal.getName() + "-" + lowervalue.getName() + "(" + i + "," + j + ");\n";
								outputCode += "else\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=0;\n";
								outputCode += "end\n";
							}
						}
						break;
					case MATRIX:
						for (int i = 1; i < uppervalue.getHeight() + 1; i++) {
							for (int j = 1; j < uppervalue.getWidth() + 1; j++) {
								outputCode += "if " + signal.getName() + "(" + i + "," + j + ")>" + uppervalue.getName()
										+ "(" + i + "," + j + ")\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")="
										+ signal.getName() + "(" + i + "," + j + ")-" + uppervalue.getName() + "(" + i
										+ "," + j + ");\n";
								outputCode += "elseif " + signal.getName() + "(" + i + "," + j + ")<"
										+ lowervalue.getName() + "(" + i + "," + j + ")\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")="
										+ signal.getName() + "(" + i + "," + j + ")-" + lowervalue.getName() + "(" + i
										+ "," + j + ");\n";
								outputCode += "else\n";
								outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=0;\n";
								outputCode += "end\n";
							}
						}
						break;
				}
				break;
		}
		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode = "/*Code for initialization of block Dead Zone:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		initCode += uppervalue.getInitCodeC();
		initCode += lowervalue.getInitCodeC();
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode = "/*Code for output of block Dead Zone:(" + getBlockId() + ")" + getBlockName() + "*/\n";

		OutputPort out = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (lowervalue.getDataType()) {
			case REAL:
				switch (ops.getOutputSignalC().getDataType()) {
					case REAL:
						outputCode += "if(" + signal.getName() + ">" + uppervalue.getName() + ") {\n";
						outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=" + signal.getName() + "-"
								+ uppervalue.getName() + ";}\n";
						outputCode += "else if(" + signal.getName() + "<" + lowervalue.getName() + ") {\n";
						outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=" + signal.getName() + "-"
								+ lowervalue.getName() + ";}\n";
						outputCode += "else {\n";
						outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=0;}\n";
						break;
					case MATRIX:
						for (int i = 0; i < ops.getHeight(); i++) {
							for (int j = 0; j < ops.getWidth(); j++) {
								outputCode += "if(" + signal.getName() + "(" + i + "," + j + ")>" + uppervalue.getName()
										+ ") {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=" + signal.getName() + "(" + i + "," + j + ")-" + uppervalue.getName()
										+ ";}\n";
								outputCode += "else if(" + signal.getName() + "(" + i + "," + j + ")<"
										+ lowervalue.getName() + ") {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=" + signal.getName() + "(" + i + "," + j + ")-" + lowervalue.getName()
										+ ";}\n";
								outputCode += "else {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=0;}\n";
							}
						}
						break;
				}
				break;
			case MATRIX:
				switch (ops.getOutputSignalC().getDataType()) {
					case REAL:
						for (int i = 0; i < lowervalue.getHeight(); i++) {
							for (int j = 0; j < lowervalue.getWidth(); j++) {
								outputCode += "if(" + signal.getName() + ">" + uppervalue.getName() + "(" + i + "," + j
										+ ")) {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=" + signal.getName() + "-" + uppervalue.getName() + "(" + i + "," + j
										+ ");}\n";
								outputCode += "else if(" + signal.getName() + "<" + lowervalue.getName() + "(" + i + ","
										+ j + ")) {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=" + signal.getName() + "-" + lowervalue.getName() + "(" + i + "," + j
										+ ");}\n";
								outputCode += "else {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=0;}\n";
							}
						}
						break;
					case MATRIX:
						for (int i = 0; i < lowervalue.getHeight(); i++) {
							for (int j = 0; j < lowervalue.getWidth(); j++) {
								outputCode += "if(" + signal.getName() + "(" + i + "," + j + ")>" + uppervalue.getName()
										+ "(" + i + "," + j + ")) {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=" + signal.getName() + "(" + i + "," + j + ")-" + uppervalue.getName()
										+ "(" + i + "," + j + ");}\n";
								outputCode += "else if(" + signal.getName() + "(" + i + "," + j + ")<"
										+ lowervalue.getName() + "(" + i + "," + j + ")) {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=" + signal.getName() + "(" + i + "," + j + ")-" + lowervalue.getName()
										+ "(" + i + "," + j + ");}\n";
								outputCode += "else {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(" + i + "," + j
										+ ")=0;}\n";
							}
						}
						break;
				}
		}
		code.addOutputCode(outputCode);
	}

	public void updateDimension() throws MatDimException {
		OutputPort out = outputPortList.get(0);
		InputPort in = inputPortList.get(0);
		OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if (lowervalue.getWidth() != uppervalue.getWidth() || lowervalue.getHeight() != uppervalue.getHeight()) {
			MatDimException e = new MatDimException(
					"Block " + this.blockName + " input dimensions don't match!All input dimensions should be same!");
			throw (e);
		}
		if (lowervalue.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
			out.setHeight(lowervalue.getHeight());
			out.setWidth(lowervalue.getWidth());
			out.getOutputSignalC().setHeight(lowervalue.getHeight());
			out.getOutputSignalC().setWidth(lowervalue.getWidth());
			out.getOutputSignalC().setDataType(DataType.MATRIX);
		} else if (lowervalue.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
		} else {
			if (lowervalue.getWidth() != signal.getWidth() || lowervalue.getHeight() != signal.getHeight()) {
				MatDimException e = new MatDimException(
						"Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
				throw (e);
			}
			out.setHeight(lowervalue.getHeight());
			out.setWidth(lowervalue.getWidth());
			out.getOutputSignalC().setHeight(lowervalue.getHeight());
			out.getOutputSignalC().setWidth(lowervalue.getWidth());
			out.getOutputSignalC().setDataType(lowervalue.getDataType());
		}
	}

	public void checkDimension() throws MatDimException {
	}
}
