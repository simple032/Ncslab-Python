package com.ncslab.block.continuous;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import org.json.JSONObject;

import java.util.Vector;

public class PIDController extends Block {

    private Parameter cparaP;
    private Parameter cparaI;
    private Parameter cparaD;
    private Parameter cparaN;
    private Parameter limitOutput;
    private Parameter lowerSaturationLimit = null;
    private Parameter upperSaturationLimit = null;
    private State stateIntegral;
    private State stateFilter;

    private Parameter externalReset;
    private Parameter sampleTime;

    public static final Vector<String> parameterNames = new Vector<>();
    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("P");
        parameterNames.add("I");
        parameterNames.add("D");
        parameterNames.add("N");
        parameterNames.add("LimitOutput");
        parameterNames.add("LowerSaturationLimit");
        parameterNames.add("UpperSaturationLimit");
        parameterNames.add("AntiWindupMode");
        parameterNames.add("Kb");
        parameterNames.add("ZeroCross");

        outputNames.add("out1");
        inputNames.add("in1");
    }

    public PIDController(JSONObject blockIn, NCSLabModel model) {
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

        stateIntegral = new State(this, 1, "stateIntegral", cparaP.getHeight(), cparaP.getWidth());
        stateFilter = new State(this, 2, "stateFilter", cparaP.getHeight(), cparaP.getWidth());

        stateList.add(stateIntegral);
        stateList.add(stateFilter);

        limitOutput = new Parameter(this, parameterList.size() + 1, "LimitOutput", paramValues.getString("LimitOutput"));
        parameterList.add(limitOutput);
        if (limitOutput.equals("on")) {
            lowerSaturationLimit = new Parameter(this, parameterList.size() + 1, "LowerSaturationLimit",
                    paramValues.getString("LowerSaturationLimit"));
            parameterList.add(lowerSaturationLimit);
            upperSaturationLimit = new Parameter(this, parameterList.size() + 1, "UpperSaturationLimit",
                    paramValues.getString("UpperSaturationLimit"));
            parameterList.add(upperSaturationLimit);
        }

        // Compatible with old versions, set to 0.01s
        sampleTime = new Parameter(this, parameterList.size() + 1, "sampleTime", paramValues.optString("sampleTime", "0.01"));
        parameterList.add(sampleTime);

        // Compatible with old versions, set to off
        externalReset = new Parameter(this, parameterList.size() + 1, "externalReset", paramValues.optString("externalReset", "off"));
        parameterList.add(externalReset);
        if (externalReset.equals("on")) {
            inputPortList.add(new InputPort(this, 2));
        }
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        Data data = new Data(cparaP.getData().getMatrix());
        stateIntegral.setData(data);
        stateFilter.setData(data);

        out.setData(data);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data currentState = new Data();

        if (cparaP.getDataType() == DataType.REAL && stateIntegral.getDataType() == DataType.REAL) {
            currentState = currentState.plus(cparaP.getData().times(inputPortList.get(0).getData()))
                .plus(stateIntegral.getData())
                .plus(stateFilter.getData().times(cparaN.getData()));

            if (externalReset.equals("on") && !inputPortList.get(1).getData().equals(new Data(0))) {
                stateIntegral.setData(new Data(0));
                stateFilter.setData(new Data(0));
            }

            if (limitOutput.equals("on")) {
                if (currentState.getInitValue() > upperSaturationLimit.getData().getInitValue()) {
                    currentState.setInitValue(upperSaturationLimit.getData().getInitValue());
                } else if (currentState.getInitValue() < lowerSaturationLimit.getData().getInitValue()) {
                    currentState.setInitValue(lowerSaturationLimit.getData().getInitValue());
                }
            }
        } else {
            for (int i = 0; i < stateIntegral.getHeight(); i++) {
                for (int j = 0; j < stateIntegral.getWidth(); j++) {
                    currentState = currentState.plus(new Data(cparaP.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j)))
                        .plus(new Data(stateIntegral.getData().getMatrix().get(i, j)))
                        .plus(new Data((stateFilter.getData().getMatrix().get(i, j) - stateFilter.getData().getMatrix().get(i, j)) * cparaN.getData().getMatrix().get(i, j)));

                    if (externalReset.equals("on") && inputPortList.get(1).getData().getMatrix().get(i, j) != 0) {
                        stateIntegral.getData().getMatrix().set(i, j, 0);
                        stateFilter.getData().getMatrix().set(i, j, 0);
                    }

                    if (limitOutput.equals("on")) {
                        double value = currentState.getMatrix().get(i, j);
                        if (value > upperSaturationLimit.getData().getMatrix().get(i, j)) {
                            currentState.getMatrix().set(i, j, upperSaturationLimit.getData().getMatrix().get(i, j));
                        } else if (value < lowerSaturationLimit.getData().getMatrix().get(i, j)) {
                            currentState.getMatrix().set(i, j, lowerSaturationLimit.getData().getMatrix().get(i, j));
                        }
                    }
                }
            }
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {
        Data derivativeData;

        if (cparaP.getDataType() == DataType.REAL && stateIntegral.getDataType() == DataType.REAL) {
            derivativeData = new Data(0);
            derivativeData = derivativeData.plus(cparaI.getData().times(inputPortList.get(0).getData()))
                .plus(cparaD.getData().times(inputPortList.get(0).getData()).minus(stateFilter.getData()).times(cparaN.getData()));
        } else {
            derivativeData = new Data(stateIntegral.getHeight(), stateIntegral.getWidth());
            for (int i = 0; i < stateIntegral.getHeight(); i++) {
                for (int j = 0; j < stateIntegral.getWidth(); j++) {
                    derivativeData = derivativeData.plus(new Data(
                        cparaI.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j)))
                        .plus(new Data((cparaD.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j) - stateFilter.getData().getMatrix().get(i, j)) * cparaN.getData().getMatrix().get(i, j)));
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
        context.put("block", this); // Current Block object (including blockId and blockName)
        context.put("cparaP", cparaP); // P parameter object
        context.put("cparaI", cparaI); // I parameter object
        context.put("cparaD", cparaD); // D parameter object
        context.put("cparaN", cparaN); // Noise parameter object
        context.put("limitOutput", limitOutput); // Output limit switch
        context.put("lowerSaturationLimit", lowerSaturationLimit); // Lower limit object
        context.put("upperSaturationLimit", upperSaturationLimit); // Upper limit object
        context.put("stateIntegral", stateIntegral); // Integral state object
        context.put("stateFilter", stateFilter); // Filter state object
        context.put("realDataType", DataType.REAL); // Real number type identifier

        String codeStr = TemplateManager.renderTemplate("c/continuous/PIDController/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode = "/*Code for output of block PID Controller:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("block", this); // 当前Block对象
        context.put("inputPortList", inputPortList); // 输入端口列表
        context.put("cparaP", cparaP); // 比例参数
        context.put("cparaD", cparaD); // 微分参数
        context.put("cparaI", cparaI); // 积分参数
        context.put("stateFilter", stateFilter); // 滤波状态
        context.put("stateIntegral", stateIntegral); // 积分状态
        context.put("externalReset", externalReset); // 外部复位标志
        context.put("limitOutput", limitOutput); // 输出限幅标志
        context.put("upperSaturationLimit", upperSaturationLimit); // 饱和上限
        context.put("lowerSaturationLimit", lowerSaturationLimit); // 饱和下限
        context.put("realDataType", DataType.REAL); // 实数类型标识
        context.put("matrixDataType", DataType.MATRIX); // 矩阵类型标识
        context.put("signal", signal);
        context.put("resetSig",
            inputPortList.size()>1?
            inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName():0
        );
        outputCode += TemplateManager.renderTemplate("c/continuous/PIDController/output.vm", context);
        code.addOutputCode(outputCode);
    }

    @Override
    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this); // Current Block object (including blockId)
        context.put("inputPortList", inputPortList); // Input port list
        context.put("cparaP", cparaP); // P parameter object
        context.put("stateIntegral", stateIntegral); // Integral state object
        context.put("stateFilter", stateFilter); // Filter state object
        context.put("realDataType", DataType.REAL); // Real number type identifier

        String derivativeCode = TemplateManager.renderTemplate("c/continuous/PIDController/derivative.vm", context);
        code.addDerivativeCode(derivativeCode);
    }

    @Override
    public void generateUpdateCodeC(CodeStructC code) {
        context.put("block", this); // Current Block object (including blockId)
        context.put("inputPortList", inputPortList); // Input port list
        context.put("cparaP", cparaP); // P parameter object
        context.put("stateIntegral", stateIntegral); // Integral state object
        context.put("stateFilter", stateFilter); // Filter state object
        context.put("realDataType", DataType.REAL); // Real number type identifier

        String updateCode = TemplateManager.renderTemplate("c/continuous/PIDController/update.vm", context);
        code.addUpdateCode(updateCode);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal.getDataType() != DataType.REAL) {
            stateIntegral = new State(this, 1, "stateIntegral", signal.getHeight(), signal.getWidth());
            stateFilter = new State(this, 2, "stateFilter", signal.getHeight(), signal.getWidth());
        }
        stateList.set(0, stateIntegral);
        stateList.set(1, stateFilter);

        if (cparaP.getWidth() != cparaD.getWidth() ||
           cparaP.getWidth() != cparaI.getWidth() ||
           cparaP.getWidth() != cparaN.getWidth() ||
           cparaP.getHeight() != cparaD.getHeight() ||
           cparaP.getHeight() != cparaI.getHeight() ||
           cparaP.getHeight() != cparaN.getHeight()) {
            throw new MatDimException("Block " + this.blockName + " input dimension doesn't match! All input dimensions must be the same!\n \n");
        }

        if (limitOutput.equals("on")) {
            if (lowerSaturationLimit.getHeight() != cparaP.getHeight() ||
               upperSaturationLimit.getHeight() != cparaP.getHeight() ||
               lowerSaturationLimit.getWidth() != cparaP.getWidth() ||
               upperSaturationLimit.getWidth() != cparaP.getWidth()) {
                throw new MatDimException("Block " + this.blockName + " input dimension doesn't match! All input dimensions must be the same!\n \n");
            }
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

    public void checkDimension() throws MatDimException {}
}
