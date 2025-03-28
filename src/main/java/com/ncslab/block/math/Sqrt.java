package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;


public class Sqrt extends com.ncslab.block.Block {

	String function;



    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }

	public Sqrt(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);
		outputPortList.add(new OutputPort(this, 1, true));

		inputPortList.add(new InputPort(this, 1));

        if(paramValues.has("SqrtFunction"))
		    function = paramValues.getString("SqrtFunction");
        else
            function = paramValues.getString("Function");
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
