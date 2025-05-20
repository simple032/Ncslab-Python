package com.ncslab.block.discrete;

import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Zero_Order_Hold extends DiscreteBlock {

    Parameter sampleTime;
    State stateOutput;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("SampleTime");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Zero_Order_Hold(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        sampleTime = new Parameter(this, 1, "SampleTime", paramValues.getString("SampleTime"));
        parameterList.add(sampleTime);

        setSampleTime(sampleTime);

        stateOutput = new State(this, 1, "output");
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("sampleTime", sampleTime);

        String codeStr = TemplateManager.renderTemplate("m/discrete/Zero_Order_Hold/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("sampleTime", sampleTime);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Zero_Order_Hold/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("realDataType", DataType.REAL); // 直接传递枚举实例
        context.put("sampleTime", sampleTime);
        context.put("signal", signal);
        context.put("ops", ops);
        context.put("optHeightIndex", ops.getHeight()-1);
        context.put("optWidthIndex", ops.getWidth()-1);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discrete/Zero_Order_Hold/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDiscreteUpdateCodeCInside(CodeStructC code) throws MatDimException {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("stateOutput", stateOutput);
        context.put("signal", signal);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discrete/Zero_Order_Hold/discrete_update.vm", context);
        code.addDiscreteUpdateCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if ((Double.parseDouble(paramValues.getString("SampleTime").trim()) * 1000000) % (model.getConfig().getFixedStep() * 1000000) > 0.000001) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        if (sampleTime.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());

        switch (signal.getDataType()) {
            case REAL:
                stateOutput = new State(this, 1, "stateOutput", 1, 1);
                break;
            case MATRIX:
                stateOutput = new State(this, 1, "stateOutput", signal.getHeight(), signal.getWidth());
                break;
        }
        stateList.add(stateOutput);
    }

    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }
}
