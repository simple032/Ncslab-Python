package com.ncslab.block.testrig;

import com.ncslab.block.io.Parameter;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.MagneticLevitationSystemDto;

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
import java.util.ArrayList;
import java.util.List;

public class MagneticLevitationSystem extends Block {

    private String name = "MagneticLevitationSystem";

    
    
    /**
     * DTO-NATIVE Constructor - Creates MagneticLevitationSystem block directly from BlockDto DTO
     */
    public MagneticLevitationSystem(MagneticLevitationSystemDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: MagneticLevitationSystem block created successfully - " + blockDto.getBlockName());

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Position", 1, false));
        outputPortList.add(new OutputPort(this, "Velocity", 2, false));

        position = new State(this, 1, "x0");
        stateList.add(position);
        velocity = new State(this, 2, "x1");
        stateList.add(velocity);

        gravity = getParameterByName("gravity");
        x0 = getParameterByName("EQUILIBRIUM_POINT_x0");
        i0 = getParameterByName("EQUILIBRIUM_POINT_i0");
        Ks = getParameterByName("TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT");
        Ka = getParameterByName("INPUT_RESISTANCE");
    }
    


    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        inputNames.add("in1");
        outputNames.add("Position");
        outputNames.add("Velocity");
        
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

        gravity = getParameterByName("gravity");
        x0 = getParameterByName("EQUILIBRIUM_POINT_x0");
        i0 = getParameterByName("EQUILIBRIUM_POINT_i0");
        Ks = getParameterByName("TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT");
        Ka = getParameterByName("INPUT_RESISTANCE");
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

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/MagneticLevitationSystem/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode = "/*Code for include files of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addIncludeCode(includeCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add specific variables needed by the template
        context.put("position", position.getName());
        context.put("velocity", velocity.getName());
        context.put("outputPosition", getOutputPortVariable(0));
        context.put("outputVelocity", getOutputPortVariable(1));

        String codeStr = TemplateManager.renderTemplate("c/testrig/MagneticLevitationSystem/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add specific variables needed by the template
        context.put("position", position.getName());
        context.put("velocity", velocity.getName());
        context.put("positionDerivative", position.getDerivativeName());
        context.put("velocityDerivative", velocity.getDerivativeName());
        // Handle unconnected input with safe fallback
        InputPort inputPort = inputPortList.get(0);
        if (inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null &&
            inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC() != null) {
            context.put("inputPortVariable", getInputPortVariable(0));
        } else {
            context.put("inputPortVariable", "0.0"); // Default input value for unconnected input
        }

        String codeStr = TemplateManager.renderTemplate("c/testrig/MagneticLevitationSystem/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addStatementCode(statementCode);
    }
}
