package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import Jama.Matrix;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;
import com.ncslab.util.TemplateManager;

public class IntervalTest extends Block {
    Parameter upLimit;
    Parameter lowLimit;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
        parameterNames.add("lowLimit");
        parameterNames.add("upLimit");
    }

    public IntervalTest(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
        lowLimit = new Parameter(this, 1, "lowLimit", paramValues.getString("lowlimit"));
        upLimit = new Parameter(this, 2, "upLimit", paramValues.getString("uplimit"));
        parameterList.add(lowLimit);
        parameterList.add(upLimit);
    }

    @Override
    public void calculateInit() {
        // Initialization logic for IntervalTest block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        double lowerBound = lowLimit.getDouble();
        double upperBound = upLimit.getDouble();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                resultData = new Data(lowerBound <= inputData.getInitValue() && inputData.getInitValue() < upperBound ? 1.0 : 0.0);
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        matrixResult.set(i, j, lowerBound <= inputData.getMatrix().get(i, j) && inputData.getMatrix().get(i, j) < upperBound ? 1.0 : 0.0);
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of block Interval Iest:(${block.blockId}) ${block.blockName}*/\n";
        initCode += lowLimit.getInitCodeC();
        initCode += upLimit.getInitCodeC();
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputs", getInputPortVariables());
        context.put("outputs", getOutputPortVariables());
        context.put("lowLimit", lowLimit.getDouble());  // 直接传递Parameter对象
        context.put("upLimit", upLimit.getDouble());    // 由模板处理参数值获取
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("signal", signal);

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/IntervalTest/output.vm", context);
        code.addOutputCode(codeStr);
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
