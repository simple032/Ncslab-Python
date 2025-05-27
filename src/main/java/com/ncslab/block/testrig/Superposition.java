package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Superposition extends Block {

    Parameter BCM1;
    Parameter BCM2;
    Parameter BCM3;
    Parameter AD1;
    Parameter AD2;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("AD1");
        outputNames.add("AD2");

        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
        parameterNames.add("BCM1");
        parameterNames.add("BCM2");
        parameterNames.add("BCM3");
        parameterNames.add("AD1");
        parameterNames.add("AD2");
    }

    public Superposition(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // 三个输入
        inputPortList.add(new InputPort(this, 1));
        BCM1 = new Parameter(this, 1, "BCM1", paramValues.getString("BCM1"));
        parameterList.add(BCM1);
        inputPortList.add(new InputPort(this, 2));
        BCM2 = new Parameter(this, 2, "BCM2", paramValues.getString("BCM2"));
        parameterList.add(BCM2);
        inputPortList.add(new InputPort(this, 3));
        BCM3 = new Parameter(this, 3, "BCM3", paramValues.getString("BCM3"));
        parameterList.add(BCM3);
        // 七个输出
        outputPortList.add(new OutputPort(this, "AD1", 1, false));
        AD1 = new Parameter(this, 4, "AD1", paramValues.getString("AD1"));
        parameterList.add(AD1);
        outputPortList.add(new OutputPort(this, "AD2", 2, false));
        AD2 = new Parameter(this, 5, "AD2", paramValues.getString("AD2"));
        parameterList.add(AD2);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode = "";

        code.addInitCode(initCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode = "";

        code.addOutputCode(outputCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("parameterList", parameterList);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/Superposition/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputPortVariables", getInputPortVariables());
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/Superposition/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/Superposition/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
