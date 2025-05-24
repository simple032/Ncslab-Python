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
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;
import com.ncslab.util.TemplateManager;

public class DetectIncrease extends Block {

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    private Data previousData;

    public DetectIncrease(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));

        context.put("block", this);
    }

    @Override
    public void calculateInit() {
        // Initialization logic for DetectIncrease block
        previousData = null;
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        Data resultData;
        if (previousData == null || !inputData.equals(previousData)) {
            switch (inputData.getDataType()) {
                case REAL:
                    double previousValue = previousData != null && previousData.getDataType() == DataType.REAL ? previousData.getInitValue() : inputData.getInitValue();
                    resultData = new Data(inputData.getInitValue() > previousValue ? 1.0 : 0.0);
                    break;
                case MATRIX:
                    Matrix previousMatrix = previousData != null && previousData.getDataType() == DataType.MATRIX ? previousData.getMatrix() : inputData.getMatrix();
                    Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                    for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                        for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                            matrixResult.set(i, j, inputData.getMatrix().get(i, j) > previousMatrix.get(i, j) ? 1.0 : 0.0);
                        }
                    }
                    resultData = new Data(matrixResult);
                    break;
                default:
                    resultData = new Data(0);
            }
        } else {
            resultData = new Data(0);
        }

        out.setData(resultData);
        previousData = inputData;
    }

    public void generateArraysCodeC(CodeStructC code) {
        try {
            OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("signal", signal);

            String codeStr = TemplateManager.renderTemplate("c/logicAndBit/DetectIncrease/arrays.vm", context);
            code.addArraysCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        try {
            String templatePath = "c/logicAndBit/DetectIncrease/init.vm";
            String codeStr = TemplateManager.renderTemplate(templatePath, context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        try {
            String templatePath = "c/logicAndBit/DetectIncrease/output.vm";
            String codeStr = TemplateManager.renderTemplate(templatePath, context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDimension() throws MatDimException{
        OutputPort out  = outputPortList.get(0);
        InputPort in  = inputPortList.get(0);
        OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
   }

    public void checkDimension() throws MatDimException{
    }
}
