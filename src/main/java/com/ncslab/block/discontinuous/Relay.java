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
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Relay extends Block {
    Parameter onSwitchValue;
    Parameter offSwitchValue;
    Parameter onOutputValue;
    Parameter offOutputValue;
    private State xState;

    
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("OnSwitchValue");
        parameterNames.add("OffSwitchValue");
        parameterNames.add("OnOutputValue");
        parameterNames.add("OffOutputValue");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // Add a method to calculate state indices
    private int[] calculateStateIndices(int height, int width) {
        int[] indices = new int[height * width];
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                indices[index++] = i * width + j;
            }
        }
        return indices;
    }

    public Relay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Initialize ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));

        // Initialize parameters
        onSwitchValue = new Parameter(this, 1, "OnSwitchValue", paramValues.getString("OnSwitchValue"));
        offSwitchValue = new Parameter(this, 2, "OffSwitchValue", paramValues.getString("OffSwitchValue"));
        onOutputValue = new Parameter(this, 3, "OnOutputValue", paramValues.getString("OnOutputValue"));
        offOutputValue = new Parameter(this, 4, "OffOutputValue", paramValues.getString("OffOutputValue"));
        parameterList.add(onSwitchValue);
        parameterList.add(offSwitchValue);
        parameterList.add(onOutputValue);
        parameterList.add(offOutputValue);
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
                double currentInput = inputData.getInitValue();
                double previousState = xState != null ? xState.getData().getInitValue() : 0.0;
                double onSwitchValueReal = onSwitchValue.getDouble();
                double offSwitchValueReal = offSwitchValue.getDouble();
                double onOutputValueReal = onOutputValue.getDouble();
                double offOutputValueReal = offOutputValue.getDouble();

                if (currentInput >= onSwitchValueReal && previousState < onSwitchValueReal) {
                    resultData = new Data(onOutputValueReal);
                } else if (currentInput <= offSwitchValueReal && previousState > offSwitchValueReal) {
                    resultData = new Data(offOutputValueReal);
                } else {
                    resultData = new Data(previousState);
                }
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        double currentInputMatrix = inputData.getMatrix().get(i, j);
                        double previousStateMatrix = xState != null ? xState.getData().getMatrix().get(i, j) : 0.0;
                        double onSwitchValueMatrix = onSwitchValue.getMatrix().get(i, j);
                        double offSwitchValueMatrix = offSwitchValue.getMatrix().get(i, j);
                        double onOutputValueMatrix = onOutputValue.getMatrix().get(i, j);
                        double offOutputValueMatrix = offOutputValue.getMatrix().get(i, j);

                        if (currentInputMatrix >= onSwitchValueMatrix && previousStateMatrix < onSwitchValueMatrix) {
                            matrixResult.set(i, j, onOutputValueMatrix);
                        } else if (currentInputMatrix <= offSwitchValueMatrix && previousStateMatrix > offSwitchValueMatrix) {
                            matrixResult.set(i, j, offOutputValueMatrix);
                        } else {
                            matrixResult.set(i, j, previousStateMatrix);
                        }
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
        if (xState != null) {
            xState.setData(resultData);
        }
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("onSwitchValue", onSwitchValue);
        context.put("offSwitchValue", offSwitchValue);
        context.put("onOutputValue", onOutputValue);
        context.put("offOutputValue", offOutputValue);
        context.put("xState", xState);

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Relay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);

        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("onSwitchValue", onSwitchValue);
        context.put("offSwitchValue", offSwitchValue);
        context.put("onOutputValue", onOutputValue);
        context.put("offOutputValue", offOutputValue);
        context.put("out", out);
        context.put("ops", ops);
        context.put("signal", signal);
        context.put("xState", xState);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Relay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("onSwitchValue", onSwitchValue);
        context.put("offSwitchValue", offSwitchValue);
        context.put("onOutputValue", onOutputValue);
        context.put("offOutputValue", offOutputValue);
        context.put("xState", xState);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Relay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("onSwitchValue", onSwitchValue);
        context.put("offSwitchValue", offSwitchValue);
        context.put("onOutputValue", onOutputValue);
        context.put("offOutputValue", offOutputValue);
        context.put("out", out);
        context.put("ops", ops);
        context.put("signal", signal);
        context.put("xState", xState);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Relay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal.getDataType() == DataType.REAL) {
            xState = new State(this, 1, "save_data", onSwitchValue.getHeight(), onSwitchValue.getWidth());
        } else {
            xState = new State(this, 1, "save_data", signal.getHeight(), signal.getWidth());
        }
        stateList.add(xState);

        if (onSwitchValue.getWidth() != offSwitchValue.getWidth()
                || onSwitchValue.getWidth() != onOutputValue.getWidth()
                || onSwitchValue.getWidth() != offOutputValue.getWidth()
                || onSwitchValue.getHeight() != offSwitchValue.getHeight()
                || onSwitchValue.getHeight() != onOutputValue.getHeight()
                || onSwitchValue.getHeight() != offOutputValue.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }

        if (onSwitchValue.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
            out.setHeight(onSwitchValue.getHeight());
            out.setWidth(onSwitchValue.getWidth());
            out.getOutputSignalC().setHeight(onSwitchValue.getHeight());
            out.getOutputSignalC().setWidth(onSwitchValue.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else if (onSwitchValue.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            if (onSwitchValue.getWidth() != signal.getWidth() || onSwitchValue.getHeight() != signal.getHeight()) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
                throw(e);
            }
            out.setHeight(onSwitchValue.getHeight());
            out.setWidth(onSwitchValue.getWidth());
            out.getOutputSignalC().setHeight(onSwitchValue.getHeight());
            out.getOutputSignalC().setWidth(onSwitchValue.getWidth());
            out.getOutputSignalC().setDataType(onSwitchValue.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }
}