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
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Delay extends DiscreteBlock {
    Parameter sampleTime;
    Parameter initialCondition;
    Parameter delayLength;

    // Define arrays to save data
    Vector<Data> buffer;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("sampleTime");
        parameterNames.add("initialCondition");
        parameterNames.add("delayLength");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Delay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, false));

        sampleTime = new Parameter(this, 1, "SampleTime", paramValues.getString("SampleTime"));
        initialCondition = new Parameter(this, 2, "InitialCondition", paramValues.getString("InitialCondition"));
        delayLength = new Parameter(this, 3, "DelayLength", paramValues.getString("DelayLength"));

        parameterList.add(sampleTime);
        setSampleTime(sampleTime);
        parameterList.add(initialCondition);
        parameterList.add(delayLength);
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("sampleTime", sampleTime);
        context.put("initialCondition", initialCondition);
        context.put("delayLength", delayLength);

        String codeStr = TemplateManager.renderTemplate("m/discrete/Delay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("sampleTime", sampleTime);
        context.put("initialCondition", initialCondition);
        context.put("delayLength", delayLength);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("sampleTime", sampleTime);
        context.put("initialCondition", initialCondition);
        context.put("delayLength", delayLength.getDouble());
        context.put("signal", signal);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateInit() {
        buffer = new Vector<>();
        OutputPort out = outputPortList.get(0);
        Data data = new Data(out.getHeight(), out.getWidth());
        for (int i = 0; i < delayLength.getDouble(); i++) {
            buffer.add(createCopy(data));
        }
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        if (!buffer.isEmpty()) {
            out.setData(buffer.firstElement());
        } else {
            out.setData(new Data(0));
        }
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort input = inputPortList.get(0);
        buffer.remove(0);
        buffer.add(createCopy(input.getData()));
    }

    private Data createCopy(Data original) {
        if (original.getDataType() == DataType.REAL) {
            return new Data(original.getInitValue());
        } else if (original.getDataType() == DataType.MATRIX) {
            Matrix matrix = original.getMatrix();
            double[][] array = matrix.getArrayCopy();
            return new Data(new Matrix(array));
        } else {
            return new Data(0);
        }
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if ((Double.parseDouble(paramValues.getString("SampleTime").trim()) * 100) % (model.getConfig().getFixedStep() * 100) > 0.000001) {
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
        if (sampleTime.getDataType() != DataType.REAL || initialCondition.getDataType() != DataType.REAL || delayLength.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period) and the Parameter(initialCondition/delayLength) can't be Matrix!\n \n");
            throw(e);
        }
    }
}
