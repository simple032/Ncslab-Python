package com.ncslab.block.testrig;

import lombok.Getter;
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

public class DoubleTank extends Block {

    private String name = "DoubleTank";

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Pump_Speed");
        outputNames.add("Water_Level");
        inputNames.add("in1");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    State pumpState;
    State levelState;

    public DoubleTank(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        this.isHardware = true;

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Pump_Speed", 1, false));
        outputPortList.add(new OutputPort(this, "Water_Level", 2, false));

        pumpState = new State(this, 1, "pumpState");
        stateList.add(pumpState);
        levelState = new State(this, 2, "levelState");
        stateList.add(levelState);
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

        String codeStr = TemplateManager.renderTemplate("c/testrig/DoubleTank/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode = "/*Code for include files of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addIncludeCode(includeCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/DoubleTank/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/DoubleTank/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
