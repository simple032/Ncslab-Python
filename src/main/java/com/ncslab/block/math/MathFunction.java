package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Objects;
import java.util.Vector;

public class MathFunction extends Block {

    private String seq;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public MathFunction(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));

        if (paramValues.has("Operator"))
            seq = paramValues.getString("Operator");
        else
            seq = paramValues.getString("MathFunctionOperator");
        if (Objects.equals(seq, "pow")) {
            inputPortList.add(new InputPort(this, 2));
        }
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);

        String outputCode = "j=" + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ";\n";
        outputCode += "Block" + this.getBlockId() + "_Output1=" + getSeq() + "(j);\n";
        code.addOutputCode(outputCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("function", getFunction());

        String codeStr = TemplateManager.renderTemplate("c/math/MathFunction/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                double inputValue = inputData.getInitValue();
                if ("pow".equals(seq)) {
                    double exponent = inputPortList.get(1).getData().getInitValue();
                    resultData = new Data(Math.pow(inputValue, exponent));
                } else {
                    resultData = new Data(applyMathFunction(inputValue, seq));
                }
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        double inputValueMatrix = inputData.getMatrix().get(i, j);
                        if ("pow".equals(seq)) {
                            double exponent = inputPortList.get(1).getData().getMatrix().get(i, j);
                            matrixResult.set(i, j, Math.pow(inputValueMatrix, exponent));
                        } else {
                            matrixResult.set(i, j, applyMathFunction(inputValueMatrix, seq));
                        }
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private double applyMathFunction(double value, String function) {
        switch (function) {
            case "sin":
                return Math.sin(value);
            case "cos":
                return Math.cos(value);
            case "tan":
                return Math.tan(value);
            case "asin":
                return Math.asin(value);
            case "acos":
                return Math.acos(value);
            case "atan":
                return Math.atan(value);
            case "sqrt":
                return Math.sqrt(value);
            case "exp":
                return Math.exp(value);
            case "log":
                return Math.log(value);
            case "abs":
                return Math.abs(value);
            case "floor":
                return Math.floor(value);
            case "ceil":
                return Math.ceil(value);
            case "round":
                return Math.round(value);
            case "sign":
                return value > 0 ? 1.0 : (value < 0 ? -1.0 : 0.0);
            default:
                return 0;
        }
    }

    private String getFunction() {
        return seq;
    }

    private String getSeq() {
        return seq;
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (getFunction().equals("transpose")) {
            out.setHeight(signal.getWidth());
            out.setWidth(signal.getHeight());
            out.getOutputSignalC().setHeight(signal.getWidth());
            out.getOutputSignalC().setWidth(signal.getHeight());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
    }
}
