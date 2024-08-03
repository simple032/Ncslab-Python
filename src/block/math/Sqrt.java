package block.math;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class Sqrt extends block.Block {

	String function;

	public Sqrt(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);
		outputPortList.add(new OutputPort(this, 1, true));

		inputPortList.add(new InputPort(this, 1));

		function = paramValues.getString("SqrtFunction");
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode = "/*Code for output of block Sqrt:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		String outName = outputPortList.get(0).getOutputSignalC().getName();
		OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		String inName = ops1.getOutputSignalC().getName();
		switch (ops1.getOutputSignalC().getDataType()) {
		case REAL:
			switch (function) {
				case "sqrt":
					outputCode += outName + "=sqrt(" + inName + ");\n";
					break;
				case "signedSqrt":
					outputCode += "if(" + inName + ">0){\n";
					outputCode += outName + "=sqrt(" + inName + ");}\n";
					outputCode += "else{\n" + outName + "=-sqrt(-" + inName + ");}\n";
					break;
				case "rSqrt":
					outputCode += outName + "=1/sqrt(" + inName + ");\n";
					break;
				}
			break;
		case MATRIX:
			for (int m = 1; m < ops1.getHeight() + 1; m++) {
				for (int n = 1; n < ops1.getWidth() + 1; n++) {
					switch (function) {
					case "sqrt":
						outputCode += outName + "=sqrt(" + inName + ");\n";
						break;
					case "signedSqrt":
						outputCode += "if(" + inName + ">0){\n";
						outputCode += outName + "=sqrt(" + inName + ");}\n";
						outputCode += "else{\n" + outName + "=-sqrt(-" + inName + ");}\n";
						break;
					case "rSqrt":
						outputCode += outName + "=1/sqrt(" + inName + ");\n";
						break;
					}
				}
			}
			break;
		}
		code.addOutputCode(outputCode);
	}

	public void updateDimension() throws MatDimException {
		OutputPort out = outputPortList.get(0);
		InputPort in = inputPortList.get(0);
		OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		out.setHeight(signal.getHeight());
		out.setWidth(signal.getWidth());
		out.getOutputSignalC().setHeight(signal.getHeight());
		out.getOutputSignalC().setWidth(signal.getWidth());
		out.getOutputSignalC().setDataType(signal.getDataType());
	}

	public void checkDimension() throws MatDimException {
	}
}
