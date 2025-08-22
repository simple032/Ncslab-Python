package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

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


    
    
    /**
     * DTO-NATIVE Constructor - Creates Superposition block directly from BlockDto DTO
     */
    public Superposition(BlockDto blockDto, NCSLabModel model) {
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
        BCM1 = new Parameter(this, 1, "BCM1", paramValues.getString("BCM1"));
        inputPortList.add(new InputPort(this, 2));
        BCM2 = new Parameter(this, 2, "BCM2", paramValues.getString("BCM2"));
        inputPortList.add(new InputPort(this, 3));
        BCM3 = new Parameter(this, 3, "BCM3", paramValues.getString("BCM3"));
        // 七个输出
        outputPortList.add(new OutputPort(this, "AD1", 1, false));
        AD1 = new Parameter(this, 4, "AD1", paramValues.getString("AD1"));
        outputPortList.add(new OutputPort(this, "AD2", 2, false));
        AD2 = new Parameter(this, 5, "AD2", paramValues.getString("AD2"));
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

}
