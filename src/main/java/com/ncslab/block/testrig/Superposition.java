package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.SuperpositionDto;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Superposition extends Block {

    Parameter BCM1;
    Parameter BCM2;
    Parameter BCM3;
    Parameter AD1;
    Parameter AD2;
    Parameter AD3;
    Parameter AD4;


    
    
    /**
     * DTO-NATIVE Constructor - Creates Superposition block directly from BlockDto DTO
     */
    public Superposition(SuperpositionDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Superposition block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("BCM1", "18");
        PARAMETER_DEFAULTS.put("BCM2", "19");
        PARAMETER_DEFAULTS.put("BCM3", "20");
        PARAMETER_DEFAULTS.put("AD1", "0");
        PARAMETER_DEFAULTS.put("AD2", "1");

    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("AD1");
        outputNames.add("AD2");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
    }

    public Superposition(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // 三个输入
        inputPortList.add(new InputPort(this, 1));
        BCM1 = getParameterByName("BCM1");
        inputPortList.add(new InputPort(this, 2));
        BCM2 = getParameterByName("BCM2");
        inputPortList.add(new InputPort(this, 3));
        BCM3 = getParameterByName("BCM3");
        // 七个输出
        outputPortList.add(new OutputPort(this, "AD1", 1, false));
        AD1 = getParameterByName("AD1");
        outputPortList.add(new OutputPort(this, "AD2", 2, false));
        AD2 = getParameterByName("AD2");
        outputPortList.add(new OutputPort(this, "AD3", 3, false));
        AD3 = getParameterByName("AD3");
        outputPortList.add(new OutputPort(this, "AD4", 4, false));
        AD4 = getParameterByName("AD4");
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
        
        // Add necessary variables for template
        context.put("blockOutputPortVariables", getOutputPortVariables());
        
        // Add parameter values that template expects
        context.put("BCM1", BCM1.getDouble());
        context.put("BCM2", BCM2.getDouble());
        context.put("BCM3", BCM3.getDouble());
        context.put("AD1", AD1.getDouble());
        context.put("AD2", AD2.getDouble());
        context.put("AD3", AD3.getDouble());
        context.put("AD4", AD4.getDouble());

        // Add output variable names
        if (getOutputPortVariables().length >= 4) {
            context.put("outputAD1", getOutputPortVariables()[0]);
            context.put("outputAD2", getOutputPortVariables()[1]);
            context.put("outputAD3", getOutputPortVariables()[2]);
            context.put("outputAD4", getOutputPortVariables()[3]);
        }
        
        // TODO:Add mode constants for template comparison
        // context.put("compilationMode", com.ncslab.ncslablink.ModelMode.COMPILATION);
        // context.put("simulationMode", com.ncslab.ncslablink.ModelMode.SIMULATION);

        String codeStr = TemplateManager.renderTemplate("c/testrig/Superposition/output.vm", context);
        code.addOutputCode(codeStr);
    }

}
