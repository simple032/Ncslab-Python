package com.ncslab.block.testrig;

import lombok.Getter;
import com.ncslab.block.io.RWork;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LoongarchPLC extends Block {

    private String name = "LoongarchPLC";

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Position");
        outputNames.add("Angle");
        outputNames.add("dr");
        inputNames.add("in1");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    State x0State;
    State x1State;
    State x2State;

    public LoongarchPLC(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Position", 1, false));
        outputPortList.add(new OutputPort(this, "Angle", 2, false));
        outputPortList.add(new OutputPort(this, "dr", 3, false));

        stateList.add(new State(this, 1, "x0"));
        stateList.add(new State(this, 2, "x1"));
        stateList.add(new State(this, 3, "x2"));

        rworkList.add(new RWork(this, 1, "tem"));
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode = "";
        code.addInitCode(initCode);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);

        String derivativeCode = "";

        code.addDerivativeCode(derivativeCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode = "";

        code.addOutputCode(outputCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("states", stateList);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("name", name);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/include.vm", context);
        code.addIncludeCode(codeStr);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("name", name);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/statement.vm", context);
        code.addStatementCode(codeStr);
    }
}
