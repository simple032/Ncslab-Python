package com.ncslab.block.discrete;

import java.util.Vector;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.State;
import com.ncslab.util.TemplateManager;

public class Discrete_Transfer_Fcn extends DiscreteBlock {
    Parameter sampleTime;
    Parameter num;
    Parameter den;
    Parameter initialStates;
    private boolean feedThrough = false;
    private Vector<State> xStateList = new Vector<>();

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("SampleTime");
        parameterNames.add("Numerator");
        parameterNames.add("Denominator");
        parameterNames.add("InitialStates");
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

    public Discrete_Transfer_Fcn(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedThrough));

        sampleTime = new Parameter(this, 1, "SampleTime", paramValues.getString("SampleTime"));
        num = new Parameter(this, 2, "Numerator", paramValues.getString("Numerator"));
        den = new Parameter(this, 3, "Denominator", paramValues.getString("Denominator"));
        initialStates = new Parameter(this, 4, "InitialStates", paramValues.getString("InitialStates"));

        for (int i = 0; i < den.getWidth() - 1; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }

        parameterList.add(sampleTime);
        parameterList.add(num);
        parameterList.add(den);
        parameterList.add(initialStates);

        setSampleTime(sampleTime);
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        Data data = new Data(initialStates.getMatrix());
        for (int i = 0; i < xStateList.size(); i++) {
            xStateList.get(i).setData(data);
        }
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        Data currentState = new Data();
        Data inputSignal = in.getData();

        if (feedThrough) {
            currentState = num.getData().times(inputSignal);
        } else {
            currentState = new Data();
        }

        for (int i = 0; i < xStateList.size(); i++) {
            currentState = currentState.plus(xStateList.get(i).getData());
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort in = inputPortList.get(0);
        Data inputSignal = in.getData();

        Data updatedX = new Data();
        for (int i = xStateList.size() - 1; i >= 0; i--) {
            if (i == 0) {
                updatedX = den.getData().times(xStateList.get(i).getData()).plus(inputSignal);
            } else {
                updatedX = den.getData().times(xStateList.get(i).getData()).plus(xStateList.get(i - 1).getData());
            }
            xStateList.get(i).setData(updatedX);
        }
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcn/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("sampleTime", sampleTime);
        context.put("num", num);
        context.put("den", den);
        context.put("initialStates", initialStates);
        context.put("states", xStateList);

        String codeStr = TemplateManager.renderTemplate("m/discrete/Discrete_Transfer_Fcn/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("sampleTime", sampleTime);
        context.put("num", num);
        context.put("den", den);
        context.put("initialStates", initialStates);
        context.put("states", xStateList);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcn/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal", signal);
        context.put("den", den);
        context.put("states", xStateList);
        context.put("feedThrough", feedThrough);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcn/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal", signal);
        context.put("den", den);
        context.put("states", xStateList);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcn/update.vm", context);
        code.addUpdateCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (den.getWidth() == num.getWidth()) {
            feedThrough = true;
        }

        if ((Double.parseDouble(paramValues.getString("SampleTime").trim()) * 1000000) % (model.getConfig().getFixedStep() * 1000000) > 0.000001) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        if (sampleTime.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }
    }
}