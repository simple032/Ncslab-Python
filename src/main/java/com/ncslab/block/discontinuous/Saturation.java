package com.ncslab.block.discontinuous;

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
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Saturation extends Block {
    Parameter lowerLimit;
    Parameter upperLimit;

    
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("LowerLimit");
        parameterNames.add("UpperLimit");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Saturation(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Initialize ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));

        // Initialize parameters
        lowerLimit = new Parameter(this, 1, "LowerLimit", paramValues.getString("LowerLimit"));
        upperLimit = new Parameter(this, 2, "UpperLimit", paramValues.getString("UpperLimit"));
        parameterList.add(lowerLimit);
        parameterList.add(upperLimit);
    }

    @Override
    public void calculateInit() {
        // 初始化逻辑
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                double inputValue = inputData.getInitValue();
                double lowerLimitValue = lowerLimit.getDouble();
                double upperLimitValue = upperLimit.getDouble();
                if (inputValue >= upperLimitValue) {
                    resultData = new Data(upperLimitValue);
                } else if (inputValue <= lowerLimitValue) {
                    resultData = new Data(lowerLimitValue);
                } else {
                    resultData = new Data(inputValue);
                }
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        double inputValueMatrix = inputData.getMatrix().get(i, j);
                        double lowerLimitMatrix = lowerLimit.getMatrix().get(i, j);
                        double upperLimitMatrix = upperLimit.getMatrix().get(i, j);
                        if (inputValueMatrix >= upperLimitMatrix) {
                            matrixResult.set(i, j, upperLimitMatrix);
                        } else if (inputValueMatrix <= lowerLimitMatrix) {
                            matrixResult.set(i, j, lowerLimitMatrix);
                        } else {
                            matrixResult.set(i, j, inputValueMatrix);
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

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("lowerLimit", lowerLimit);
        context.put("upperLimit", upperLimit);

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Saturation/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);

        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("lowerLimit", lowerLimit);
        context.put("lowerLimitHeightIndex", lowerLimit.getHeight()-1);
        context.put("lowerLimitWidthIndex", lowerLimit.getWidth()-1);
        context.put("upperLimit", upperLimit);
        context.put("out", out);
        context.put("ops", ops);
        context.put("signal", signal);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Saturation/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("lowerLimit", lowerLimit);
        context.put("upperLimit", upperLimit);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Saturation/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("lowerLimit", lowerLimit);
        context.put("upperLimit", upperLimit);
        context.put("out", out);
        context.put("ops", ops);
        context.put("signal", signal);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Saturation/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (lowerLimit.getWidth() != upperLimit.getWidth() || lowerLimit.getHeight() != upperLimit.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }

        if (lowerLimit.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
            out.setHeight(lowerLimit.getHeight());
            out.setWidth(lowerLimit.getWidth());
            out.getOutputSignalC().setHeight(lowerLimit.getHeight());
            out.getOutputSignalC().setWidth(lowerLimit.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else if (lowerLimit.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            if (lowerLimit.getWidth() != signal.getWidth() || lowerLimit.getHeight() != signal.getHeight()) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimension doesn't match the Saturation dimension!\n \n");
                throw(e);
            }

            out.setHeight(lowerLimit.getHeight());
            out.setWidth(lowerLimit.getWidth());
            out.getOutputSignalC().setHeight(lowerLimit.getHeight());
            out.getOutputSignalC().setWidth(lowerLimit.getWidth());
            out.getOutputSignalC().setDataType(lowerLimit.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }
}
