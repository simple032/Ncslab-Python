package com.ncslab.block.source;

import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class SineWave extends Block {
    Parameter amplitude;
    Parameter bias;
    Parameter frequency;
    Parameter phase;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

    static {
        outputNames.add("out1");
        parameterNames.add("Amplitude");
        parameterNames.add("Bias");
        parameterNames.add("Frequency");
        parameterNames.add("Phase");
    }

    public SineWave(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Add output port
        outputPortList.add(new OutputPort(this, 1, false));

        // Initialize parameters
        amplitude = new Parameter(this, 1, "Amplitude", paramValues.getString("Amplitude"));
        bias = new Parameter(this, 2, "Bias", paramValues.getString("Bias"));
        frequency = new Parameter(this, 3, "Frequency", paramValues.getString("Frequency"));
        phase = new Parameter(this, 4, "Phase", paramValues.getString("Phase"));

        parameterList.add(amplitude);
        parameterList.add(bias);
        parameterList.add(frequency);
        parameterList.add(phase);

        outputPortList.get(0).setHeight(amplitude.getHeight());
        outputPortList.get(0).setWidth(amplitude.getWidth());
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("amplitude", amplitude);
        context.put("bias", bias);
        context.put("frequency", frequency);
        context.put("phase", phase);

        String codeStr = TemplateManager.renderTemplate("m/source/SineWave/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("amplitude", amplitude);
        context.put("bias", bias);
        context.put("frequency", frequency);
        context.put("phase", phase);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/source/SineWave/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("amplitude", amplitude);
        context.put("bias", bias);
        context.put("frequency", frequency);
        context.put("phase", phase);

        String codeStr = TemplateManager.renderTemplate("c/source/SineWave/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("amplitude", amplitude);
        context.put("bias", bias);
        context.put("frequency", frequency);
        context.put("phase", phase);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/source/SineWave/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        if (amplitude.getWidth() != bias.getWidth()
                || amplitude.getWidth() != frequency.getWidth()
                || amplitude.getWidth() != phase.getWidth()
                || amplitude.getHeight() != bias.getHeight()
                || amplitude.getHeight() != frequency.getHeight()
                || amplitude.getHeight() != phase.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }
    }
}
