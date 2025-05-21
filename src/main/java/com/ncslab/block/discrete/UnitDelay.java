package com.ncslab.block.discrete;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class UnitDelay extends DiscreteBlock {

    Parameter sampleTime;
    Parameter initialCondition;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("SampleTime");
        parameterNames.add("InitialCondition");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // Add a method to calculate signal indices
    private int[] calculateSignalIndices(int height, int width) {
        int[] indices = new int[height * width];
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                indices[index++] = i * width + j;
            }
        }
        return indices;
    }

    public UnitDelay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        sampleTime = new Parameter(this, 1, "SampleTime", paramValues.getString("SampleTime"));
        initialCondition = new Parameter(this, 2, "InitialCondition", paramValues.getString("InitialCondition"));

        parameterList.add(sampleTime);
        setSampleTime(sampleTime);
        parameterList.add(initialCondition);
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal", signal);

        String codeStr = TemplateManager.renderTemplate("c/discrete/UnitDelay/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("sampleTime", sampleTime);
        context.put("initialCondition", initialCondition);

        String codeStr = TemplateManager.renderTemplate("m/discrete/UnitDelay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("sampleTime", sampleTime);
        context.put("initialCondition", initialCondition);

        String codeStr = TemplateManager.renderTemplate("c/discrete/UnitDelay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("sampleTime", sampleTime);
        context.put("initialCondition", initialCondition);
        context.put("signal", signal);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discrete/UnitDelay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (sampleTime.getDataType() != DataType.REAL || initialCondition.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
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
        // No specific dimension checking needed
    }
}