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

public class Step extends Block {
    Parameter time;
    Parameter after;
    Parameter before;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

    static {
        outputNames.add("out1");

        parameterNames.add("Time");
        parameterNames.add("After");
        parameterNames.add("Before");
    }

    public Step(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Add output port
        outputPortList.add(new OutputPort(this, 1, false));

        // Initialize parameters
        time = new Parameter(this, 1, "Time", paramValues.getString("Time"));
        after = new Parameter(this, 2, "After", paramValues.getString("After"));
        before = new Parameter(this, 3, "Before", paramValues.getString("Before"));

        parameterList.add(time);
        parameterList.add(after);
        parameterList.add(before);

        outputPortList.get(0).setHeight(time.getHeight());
        outputPortList.get(0).setWidth(time.getWidth());
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("time", time);
        context.put("after", after);
        context.put("before", before);

        String codeStr = TemplateManager.renderTemplate("m/source/Step/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("time", time);
        context.put("after", after);
        context.put("before", before);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/source/Step/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("time", time);
        context.put("after", after);
        context.put("before", before);

        String codeStr = TemplateManager.renderTemplate("c/source/Step/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        VelocityContext context = new VelocityContext();
        context.put("block", this);
context.put("realDataType", DataType.REAL);
        context.put("time", time);
        context.put("after", after);
        context.put("before", before);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/source/Step/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        if (time.getWidth() != after.getWidth()
                || time.getWidth() != before.getWidth()
                || time.getHeight() != after.getHeight()
                || time.getHeight() != before.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }
    }
}
