package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

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

public class Substitution extends Block {

    Parameter BCM;
    Parameter DA;
    Parameter AD1;
    Parameter AD2;
    Parameter AD3;
    Parameter AD4;
    Parameter AD5;
    Parameter AD6;
    Parameter AD7;


    
    
    /**
     * DTO-NATIVE Constructor - Creates Substitution block directly from BlockJson DTO
     */
    public Substitution(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Substitution block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("AD1");
        outputNames.add("AD2");
        outputNames.add("AD3");
        outputNames.add("AD4");
        outputNames.add("AD5");
        outputNames.add("AD6");
        outputNames.add("AD7");

        inputNames.add("in1");
        inputNames.add("in2");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("BCM", "18");
        PARAMETER_DEFAULTS.put("DA", "0");
        PARAMETER_DEFAULTS.put("AD1", "0");
        PARAMETER_DEFAULTS.put("AD2", "1");
        PARAMETER_DEFAULTS.put("AD3", "2");
        PARAMETER_DEFAULTS.put("AD4", "3");
        PARAMETER_DEFAULTS.put("AD5", "4");
        PARAMETER_DEFAULTS.put("AD6", "5");
        PARAMETER_DEFAULTS.put("AD7", "6");
    }

    public Substitution(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // 两个输入
        inputPortList.add(new InputPort(this, 1));
        BCM = new Parameter(this, 1, "BCM", paramValues.getString("BCM"));
        inputPortList.add(new InputPort(this, 2));
        DA = new Parameter(this, 2, "DA", paramValues.getString("DA"));
        // 七个输出
        outputPortList.add(new OutputPort(this, "AD1", 1, false));
        AD1 = new Parameter(this, 3, "AD1", paramValues.getString("AD1"));
        outputPortList.add(new OutputPort(this, "AD2", 2, false));
        AD2 = new Parameter(this, 4, "AD2", paramValues.getString("AD2"));
        outputPortList.add(new OutputPort(this, "AD3", 3, false));
        AD3 = new Parameter(this, 5, "AD3", paramValues.getString("AD3"));
        outputPortList.add(new OutputPort(this, "AD4", 4, false));
        AD4 = new Parameter(this, 6, "AD4", paramValues.getString("AD4"));
        outputPortList.add(new OutputPort(this, "AD5", 5, false));
        AD5 = new Parameter(this, 7, "AD5", paramValues.getString("AD5"));
        outputPortList.add(new OutputPort(this, "AD6", 6, false));
        AD6 = new Parameter(this, 8, "AD6", paramValues.getString("AD6"));
        outputPortList.add(new OutputPort(this, "AD7", 7, false));
        AD7 = new Parameter(this, 9, "AD7", paramValues.getString("AD7"));
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

        String codeStr = TemplateManager.renderTemplate("c/testrig/Substitution/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("parameterList", parameterList);
        context.put("inputPortVariables", getInputPortVariables());
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/Substitution/output.vm", context);
        code.addOutputCode(codeStr);
    }

}
