package com.ncslab.block.logicAndBit;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;
import com.ncslab.util.TemplateManager;

public class RelationalOperator extends Block {

    String relop;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
    }

    public RelationalOperator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        OutputPort output = new OutputPort(this, 1, true);
        outputPortList.add(output);

        if (paramValues.has("relop")) {
            relop = paramValues.getString("relop");
        } else {
            relop = paramValues.getString("Operator");
        }
        if (relop.equals("~=")) {
            relop = "!=";
        }
    }

    @Override
    public void calculateInit() {
        // Initialization logic for RelationalOperator block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData1 = inputPortList.get(0).getData();
        Data inputData2 = inputPortList.get(1).getData();

        Data resultData;
        switch (inputData1.getDataType()) {
            case REAL:
                resultData = new Data(compare(inputData1.getInitValue(), inputData2.getInitValue(), relop));
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData1.getMatrix().getRowDimension(), inputData1.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData1.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData1.getMatrix().getColumnDimension(); j++) {
                        matrixResult.set(i, j, compare(inputData1.getMatrix().get(i, j), inputData2.getMatrix().get(i, j), relop));
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private double compare(double inputValue1, double inputValue2, String operator) {
        switch (operator) {
            case "==":
                return inputValue1 == inputValue2 ? 1.0 : 0.0;
            case "!=":
                return inputValue1 != inputValue2 ? 1.0 : 0.0;
            case "<":
                return inputValue1 < inputValue2 ? 1.0 : 0.0;
            case "<=":
                return inputValue1 <= inputValue2 ? 1.0 : 0.0;
            case ">":
                return inputValue1 > inputValue2 ? 1.0 : 0.0;
            case ">=":
                return inputValue1 >= inputValue2 ? 1.0 : 0.0;
            default:
                return 0.0;
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal1", signal1);
        context.put("signal2", signal2);
        context.put("ops", out.getOutputSignalC());
        context.put("relop", relop);

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/RelationalOperator/output.vm", context);

        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal1.getHeight() != signal2.getHeight() || signal1.getWidth() != signal2.getWidth()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " two input dimension don't match!\n \n");
            throw (e);
        }
        out.setHeight(signal1.getHeight());
        out.setWidth(signal1.getWidth());
        out.getOutputSignalC().setHeight(signal1.getHeight());
        out.getOutputSignalC().setWidth(signal1.getWidth());
        out.getOutputSignalC().setDataType(signal1.getDataType());
    }

    public void checkDimension() throws MatDimException {
    }
}
