package com.ncslab.block.discrete;

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

public class Discrete_Time_Integrator extends Block {
    Parameter gainval;
    Parameter sampleTime;
    Parameter initialCondition;
    private State xState;

    public static final Vector<String> parameterNames = new Vector<>();
    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("gainval");
        parameterNames.add("sampleTime");
        parameterNames.add("initialCondition");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Discrete_Time_Integrator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, false));

        gainval = new Parameter(this, 1, "gainval", paramValues.getString("gainval"));
        sampleTime = new Parameter(this, 2, "sampleTime", paramValues.getString("SampleTime"));
        initialCondition = new Parameter(this, 3, "initialCondition", paramValues.getString("InitialCondition"));

        parameterList.add(gainval);
        parameterList.add(sampleTime);
        parameterList.add(initialCondition);
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        xState.setData(initialCondition.getData());
        out.setData(initialCondition.getData());
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        Data currentState = xState.getData();
        Data inputSignal = in.getData();

        // y(k) = xState(k)
        out.setData(currentState);
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort in = inputPortList.get(0);
        Data inputSignal = in.getData();

        String option = paramValues.getString("IntegratorMethod");

        switch (option) {
            case "Integration: Forward Euler":
                Data updatedX = xState.getData().plus(gainval.getData().times(inputSignal).times(new Data(sampleTime.getDouble())));
                xState.setData(updatedX);
                break;
            case "Integration: Backward Euler":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal).times(new Data(sampleTime.getDouble())));
                xState.setData(updatedX);
                break;
            case "Integration: Trapezoidal":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal).times(new Data(sampleTime.getDouble() / 2.0)));
                xState.setData(updatedX);
                break;
            case "Accumulation: Forward Euler":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal));
                xState.setData(updatedX);
                break;
            case "Accumulation: Backward Euler":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal));
                xState.setData(updatedX);
                break;
            case "Accumulation: Trapezoidal":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal).times(new Data(0.5)));
                xState.setData(updatedX);
                break;
            default:
                throw new RuntimeException("Unsupported IntegratorMethod: " + option);
        }
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of block discrete_time_integrator:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        initCode += gainval.getInitCodeC();
        initCode += sampleTime.getInitCodeC();
        initCode += initialCondition.getInitCodeC();

        if (gainval.getDataType() == DataType.REAL && xState.getDataType() == DataType.REAL) {
            initCode += xState.getName() + "=" + initialCondition.getName() + ";\n";
        } else if (gainval.getDataType() == DataType.REAL && xState.getDataType() == DataType.MATRIX) {
            for (int i = 0; i < xState.getHeight(); i++) {
                for (int j = 0; j < xState.getWidth(); j++) {
                    initCode += xState.getName() + "(" + i + "," + j + ")=" + initialCondition.getName() + ";\n";
                }
            }
        } else {
            for (int i = 0; i < xState.getHeight(); i++) {
                for (int j = 0; j < xState.getWidth(); j++) {
                    initCode += xState.getName() + "(" + i + "," + j + ")=" + initialCondition.getName() + "(" + i + "," + j + ");\n";
                }
            }
        }

        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode = "/*Code for output of block Unit Delay:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        outputCode += "{real_T currentTime = model.time;\n";
        outputCode += "real_T sampleTimeTmp = " + sampleTime.getName() + "==-1?model.stepSize:" + sampleTime.getName() + ";\n";
        outputCode += "if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001&&mp->majorStep>0) {\n";

        String option = paramValues.getString("IntegratorMethod");
        switch (signal.getDataType()) {
            case REAL:
                switch (gainval.getDataType()) {
                    case REAL:
                        switch (option) {
                            case "Integration: Forward Euler":
                                outputCode += out.getOutputSignalC().getName() + "=" + xState.getName() + ";\n";
                                outputCode += xState.getName() + "=" + xState.getName() + "+" + gainval.getName() + "*" + sampleTime.getName() + "*" + signal.getName() + ";}}}\n";
                                break;
                            case "Integration: Backward Euler":
                                outputCode += out.getOutputSignalC().getName() + "=" + xState.getName() + "+" + gainval.getName() + "*" + signal.getName() + "*" + sampleTime.getName() + ";\n";
                                outputCode += xState.getName() + "=" + out.getOutputSignalC().getName() + ";}}}\n";
                                break;
                            case "Integration: Trapezoidal":
                                outputCode += out.getOutputSignalC().getName() + "=" + xState.getName() + "+" + sampleTime.getName() + "/2*" + signal.getName() + "*" + gainval.getName() + ";\n";
                                outputCode += xState.getName() + "=" + out.getOutputSignalC().getName() + "+" + sampleTime.getName() + "/2*" + signal.getName() + "*" + gainval.getName() + ";}}}\n";
                                break;
                            case "Accumulation: Forward Euler":
                                outputCode += out.getOutputSignalC().getName() + "=" + xState.getName() + ";\n";
                                outputCode += xState.getName() + "=" + xState.getName() + "+" + gainval.getName() + "*" + signal.getName() + ";}}}\n";
                                break;
                            case "Accumulation: Backward Euler":
                                outputCode += out.getOutputSignalC().getName() + "=" + xState.getName() + "+" + gainval.getName() + "*" + signal.getName() + ";}}}\n";
                                outputCode += xState.getName() + "=" + out.getOutputSignalC().getName() + ";}}}\n";
                                break;
                            case "Accumulation: Trapezoidal":
                                outputCode += out.getOutputSignalC().getName() + "=" + xState.getName() + "+" + signal.getName() + "*" + gainval.getName() + "*0.5;\n";
                                outputCode += xState.getName() + "=" + out.getOutputSignalC().getName() + "+" + signal.getName() + "*" + gainval.getName() + "*0.5;}}}\n";
                                break;
                        }
                        break;
                    case MATRIX:
                        for (int i = 0; i < gainval.getHeight(); i++) {
                            for (int j = 0; j < gainval.getWidth(); j++) {
                                switch (option) {
                                    case "Integration: Forward Euler":
                                        outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=" + xState.getName() + "(" + i + "," + j + ");\n";
                                        outputCode += xState.getName() + "(" + i + "," + j + ")=" + xState.getName() + "(" + i + "," + j + ")+" + gainval.getName() + "(" + i + "," + j + ")*" + sampleTime.getName() + "*" + signal.getName() + ";}}}\n";
                                        break;
                                    case "Integration: Backward Euler":
                                        outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=" + xState.getName() + "(" + i + "," + j + ")+" + gainval.getName() + "(" + i + "," + j + ")*" + signal.getName() + "*" + sampleTime.getName() + ";\n";
                                        outputCode += xState.getName() + "(" + i + "," + j + ")=" + out.getOutputSignalC().getName() + "(" + i + "," + j + ");}}}\n";
                                        break;
                                    case "Integration: Trapezoidal":
                                        outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=" + xState.getName() + "(" + i + "," + j + ")+" + sampleTime.getName() + "/2*" + signal.getName() + "*" + gainval.getName() + "(" + i + "," + j + ");\n";
                                        outputCode += xState.getName() + "(" + i + "," + j + ")=" + out.getOutputSignalC().getName() + "(" + i + "," + j + ")+" + sampleTime.getName() + "/2*" + signal.getName() + "*" + gainval.getName() + "(" + i + "," + j + ");}}}\n";
                                        break;
                                    case "Accumulation: Forward Euler":
                                        outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=" + xState.getName() + "(" + i + "," + j + ");\n";
                                        outputCode += xState.getName() + "(" + i + "," + j + ")=" + xState.getName() + "(" + i + "," + j + ")+" + gainval.getName() + "(" + i + "," + j + ")*" + signal.getName() + ";}}}\n";
                                        break;
                                    case "Accumulation: Backward Euler":
                                        outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=" + xState.getName() + "(" + i + "," + j + ")+" + gainval.getName() + "(" + i + "," + j + ")*" + signal.getName() + ";}}}\n";
                                        outputCode += xState.getName() + "(" + i + "," + j + ")=" + out.getOutputSignalC().getName() + "(" + i + "," + j + ");}}}\n";
                                        break;
                                    case "Accumulation: Trapezoidal":
                                        outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=" + xState.getName() + "(" + i + "," + j + ")+" + signal.getName() + "*" + gainval.getName() + "(" + i + "," + j + ")*0.5;\n";
                                        outputCode += xState.getName() + "(" + i + "," + j + ")=" + out.getOutputSignalC().getName() + "(" + i + "," + j + ")+" + signal.getName() + "*" + gainval.getName() + "(" + i + "," + j + ")*0.5;}}}\n";
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        code.addOutputCode(outputCode);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);

        String derivativeCode = "/*Code for Derivative of block Backlash:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        code.addDerivativeCode(derivativeCode);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        String updateCode = "/*Code for Update of " + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";

        code.addUpdateCode(updateCode);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal.getDataType() == DataType.REAL) {
            xState = new State(this, 1, "save_data", gainval.getHeight(), gainval.getWidth());
        } else {
            xState = new State(this, 1, "save_data", signal.getHeight(), signal.getWidth());
        }
        stateList.add(xState);

        if ((Double.parseDouble(paramValues.getString("SampleTime").trim()) * 1000000) % (model.getConfig().getFixedStep() * 1000000) > 0.000001) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        if (gainval.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
            out.setHeight(gainval.getHeight());
            out.setWidth(gainval.getWidth());
            out.getOutputSignalC().setHeight(gainval.getHeight());
            out.getOutputSignalC().setWidth(gainval.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else if (gainval.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            if (gainval.getWidth() != signal.getWidth() || gainval.getHeight() != signal.getHeight()) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
                throw(e);
            }
            out.setHeight(gainval.getHeight());
            out.setWidth(gainval.getWidth());
            out.getOutputSignalC().setHeight(gainval.getHeight());
            out.getOutputSignalC().setWidth(gainval.getWidth());
            out.getOutputSignalC().setDataType(gainval.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        if (sampleTime.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }
        if (gainval.getWidth() != initialCondition.getWidth()
                || gainval.getHeight() != initialCondition.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! Gain and initialCondition input dimensions should be same!");
            throw(e);
        }
    }
}
