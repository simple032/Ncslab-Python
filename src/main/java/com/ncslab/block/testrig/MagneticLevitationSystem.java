package com.ncslab.block.testrig;

import com.ncslab.block.io.Parameter;
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

public class MagneticLevitationSystem extends Block {

    private String name = "MagneticLevitationSystem";

    @Getter
    public static final Vector<String> inputNames = new Vector<>();
    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        inputNames.add("in1");
        outputNames.add("Position");
        outputNames.add("Velocity");
        parameterNames.add("gravity");
        parameterNames.add("EQUILIBRIUM_POINT_x0");
        parameterNames.add("EQUILIBRIUM_POINT_i0");
        parameterNames.add("TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT");
        parameterNames.add("INPUT_RESISTANCE");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("gravity", "9.8");
        PARAMETER_DEFAULTS.put("EQUILIBRIUM_POINT_x0", "0.2");
        PARAMETER_DEFAULTS.put("EQUILIBRIUM_POINT_i0", "6.105");
        PARAMETER_DEFAULTS.put("TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT", "-4.5871056");
        PARAMETER_DEFAULTS.put("INPUT_RESISTANCE", "5.8929");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    State position;
    State velocity;

    Parameter gravity;
    Parameter x0;
    Parameter i0;
    Parameter Ks;
    Parameter Ka;

    public MagneticLevitationSystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Position", 1, false));
        outputPortList.add(new OutputPort(this, "Velocity", 2, false));

        position = new State(this, 1, "x0");
        stateList.add(position);
        velocity = new State(this, 2, "x1");
        stateList.add(velocity);

        gravity = new Parameter(this, 1, "gravity", "9.8");
        x0 = new Parameter(this, 2, "EQUILIBRIUM_POINT_x0", "0.2");
        i0 = new Parameter(this, 3, "EQUILIBRIUM_POINT_i0", "6.105");
        Ks = new Parameter(this, 4, "TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT", "-4.5871056 ");
        Ka = new Parameter(this, 5, "INPUT_RESISTANCE", "5.8929");
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

        String codeStr = TemplateManager.renderTemplate("c/testrig/MagneticLevitationSystem/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode = "/*Code for include files of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addIncludeCode(includeCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/MagneticLevitationSystem/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/MagneticLevitationSystem/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addStatementCode(statementCode);
    }
}
