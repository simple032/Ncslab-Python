package com.ncslab.block.continuous;

import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.ncslablink.MatDimException;

import java.util.Vector;

public class OldPIDController extends Block {

    private Parameter cparaP;
    private Parameter cparaI;
    private Parameter cparaD;
    private Parameter cparaN;
    private State stateIntegral;
    private State stateFilter;

    public static final Vector<String> parameterNames = new Vector<>();
    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("P");
        parameterNames.add("I");
        parameterNames.add("D");
        parameterNames.add("N");

        outputNames.add("out1");
        inputNames.add("in1");
    }

    public OldPIDController(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // One input, one output
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));

        cparaP = new Parameter(this, parameterList.size() + 1, "P", paramValues.getString("P"));
        parameterList.add(cparaP);
        cparaI = new Parameter(this, parameterList.size() + 1, "I", paramValues.getString("I"));
        parameterList.add(cparaI);
        cparaD = new Parameter(this, parameterList.size() + 1, "D", paramValues.getString("D"));
        parameterList.add(cparaD);
        cparaN = new Parameter(this, parameterList.size() + 1, "N", paramValues.getString("N"));
        parameterList.add(cparaN);
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        Data data = cparaP.getData();
        stateIntegral = new State(this, 1, "stateIntegral", in.getHeight(), in.getWidth());
        stateFilter = new State(this, 2, "stateFilter", in.getHeight(), in.getWidth());

        stateIntegral.setData(data);
        stateFilter.setData(data);

        out.setData(data);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data currentState = new Data();

        if (cparaP.getDataType() == DataType.REAL && stateIntegral.getDataType() == DataType.REAL) {
            currentState = currentState.plus(cparaP.getData().times(inputPortList.get(0).getData()));
            currentState = currentState.plus(stateIntegral.getData());
            currentState = currentState.plus(stateFilter.getData().times(cparaN.getData()));
        } else {
            for (int i = 0; i < stateIntegral.getHeight(); i++) {
                for (int j = 0; j < stateIntegral.getWidth(); j++) {
                    currentState = currentState.plus(new Data(cparaP.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j)));
                    currentState = currentState.plus(new Data(stateIntegral.getData().getMatrix().get(i, j)));
                    currentState = currentState.plus(new Data((stateFilter.getData().getMatrix().get(i, j) - stateFilter.getData().getMatrix().get(i, j)) * cparaN.getData().getMatrix().get(i, j)));
                }
            }
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {
        Data derivativeData = new Data();

        if (cparaP.getDataType() == DataType.REAL && stateIntegral.getDataType() == DataType.REAL) {
            derivativeData = derivativeData.plus(cparaI.getData().times(inputPortList.get(0).getData()));
            derivativeData = derivativeData.plus(cparaD.getData().times(inputPortList.get(0).getData()).minus(stateFilter.getData()).times(cparaN.getData()));
        } else {
            for (int i = 0; i < stateIntegral.getHeight(); i++) {
                for (int j = 0; j < stateIntegral.getWidth(); j++) {
                    derivativeData = derivativeData.plus(new Data(cparaI.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j)));
                    derivativeData = derivativeData.plus(new Data((cparaD.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j) - stateFilter.getData().getMatrix().get(i, j)) * cparaN.getData().getMatrix().get(i, j)));
                }
            }
        }

        stateIntegral.setDerivateData(derivativeData);
        stateFilter.setDerivateData(derivativeData);
    }

    public void generateArraysCodeC(CodeStructC code) {
        String arraysCode = "/*Define arrays for block discrete_Delay:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal.getDataType() == DataType.MATRIX) {
            arraysCode += "double " + "Block" + getBlockId() + "save_data[" + signal.getHeight() + "][" + signal.getWidth() + "*5];\n";
        } else if (signal.getDataType() == DataType.REAL && cparaP.getDataType() == DataType.REAL) {
            arraysCode += "double " + "Block" + getBlockId() + "save_data[5];\n";
        } else {
            arraysCode += "double " + "Block" + getBlockId() + "save_data[" + cparaP.getHeight() + "][" + cparaP.getWidth() + "*5];\n";
        }
        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("stateIntegral", stateIntegral);
        context.put("stateFilter", stateFilter);
        String codeStr = TemplateManager.renderTemplate("c/continuous/OldPIDController/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("stateIntegral", stateIntegral);
        context.put("stateFilter", stateFilter);
        String codeStr = TemplateManager.renderTemplate("c/continuous/OldPIDController/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("stateIntegral", stateIntegral);
        context.put("stateFilter", stateFilter);
        String codeStr = TemplateManager.renderTemplate("c/continuous/OldPIDController/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        String updateCode = "/*Code for Update of " + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal.getDataType() == DataType.REAL && cparaP.getDataType() == DataType.REAL) {
            updateCode += stateIntegral.getName() + "=" + stateIntegral.getName() + "+" + stateIntegral.getDerivativeName() + "*model.stepSize;\n";
            updateCode += stateFilter.getName() + "=" + stateFilter.getName() + "+" + stateFilter.getDerivativeName() + "*model.stepSize;\n";
        } else {
            for (int i = 0; i < stateFilter.getHeight(); i++) {
                for (int j = 0; j < stateFilter.getWidth(); j++) {
                    updateCode += stateIntegral.getName() + "(" + i + "," + j + ")=" + stateIntegral.getName() + "(" + i + "," + j + ")+" + stateIntegral.getDerivativeName() + "(" + i + "," + j + ")*model.stepSize;\n";
                    updateCode += stateFilter.getName() + "(" + i + "," + j + ")=" + stateFilter.getName() + "(" + i + "," + j + ")+" + stateFilter.getDerivativeName() + "(" + i + "," + j + ")*model.stepSize;\n";
                }
            }
        }
        code.addUpdateCode(updateCode);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal.getDataType() == DataType.REAL) {
            stateIntegral = new State(this, 1, "stateIntegral", cparaP.getHeight(), cparaP.getWidth());
            stateFilter = new State(this, 2, "stateFilter", cparaP.getHeight(), cparaP.getWidth());
        } else {
            stateIntegral = new State(this, 1, "stateIntegral", signal.getHeight(), signal.getWidth());
            stateFilter = new State(this, 2, "stateFilter", signal.getHeight(), signal.getWidth());
        }
        stateList.add(stateIntegral);
        stateList.add(stateFilter);

        if (cparaP.getWidth() != cparaD.getWidth() ||
           cparaP.getWidth() != cparaI.getWidth() ||
           cparaP.getWidth() != cparaN.getWidth() ||
           cparaP.getHeight() != cparaD.getHeight() ||
           cparaP.getHeight() != cparaI.getHeight() ||
           cparaP.getHeight() != cparaN.getHeight()) {
            throw new MatDimException("Block " + this.blockName + " input dimension doesn't match! All input dimensions must be the same!\n \n");
        }

        if (cparaP.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
            out.setHeight(cparaP.getHeight());
            out.setWidth(cparaP.getWidth());
            out.getOutputSignalC().setHeight(cparaP.getHeight());
            out.getOutputSignalC().setWidth(cparaP.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else if (cparaP.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            if (cparaP.getWidth() != signal.getWidth() || cparaP.getHeight() != signal.getHeight()) {
                throw new MatDimException("Block " + this.blockName + " input dimension doesn't match the P dimension!\n \n");
            }
            out.setHeight(cparaP.getHeight());
            out.setWidth(cparaP.getWidth());
            out.getOutputSignalC().setHeight(cparaP.getHeight());
            out.getOutputSignalC().setWidth(cparaP.getWidth());
            out.getOutputSignalC().setDataType(cparaP.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
    }
}
