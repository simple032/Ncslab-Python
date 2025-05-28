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

import java.util.Objects;
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
        Data updatedX;
        String option = paramValues.getString("IntegratorMethod");

        switch (option) {
            case "Integration: Forward Euler":
            case "Integration: Backward Euler":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal).times(new Data(sampleTime.getDouble())));
                break;
            case "Integration: Trapezoidal":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal).times(new Data(sampleTime.getDouble() / 2.0)));
                break;
            case "Accumulation: Forward Euler":
            case "Accumulation: Backward Euler":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal));
                break;
            case "Accumulation: Trapezoidal":
                updatedX = xState.getData().plus(gainval.getData().times(inputSignal).times(new Data(0.5)));
                break;
            default:
                throw new RuntimeException("Unsupported IntegratorMethod: " + option);
        }
        xState.setData(updatedX);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of block discrete_time_integrator:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        context.put("blockId", blockId);
        context.put("blockName", blockName);
        context.put("gainval", gainval);
        context.put("sampleTime", sampleTime);
        context.put("initialCondition", initialCondition);
        context.put("xState", xState);
        initCode += TemplateManager.renderTemplate("c/discrete/Discrete_Time_Integrator/init.vm", context);

        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode = "/*Code for output of block Unit Delay:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("out", out);
        context.put("ops", ops);
        context.put("signal", signal);
        context.put("xState", xState);
        context.put("gainval", gainval);
        context.put("sampleTime", sampleTime);
        context.put("option", paramValues.getString("IntegratorMethod"));

        outputCode += TemplateManager.renderTemplate("c/discrete/Discrete_Time_Integrator/output.vm", context);
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
