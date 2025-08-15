package com.ncslab.block.testrig;

import lombok.Getter;
import com.ncslab.block.io.RWork;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

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

public class LoongarchPLC extends Block {

    private String name = "LoongarchPLC";

    
    
    /**
     * DTO-NATIVE Constructor - Creates LoongarchPLC block directly from BlockJson DTO
     */
    public LoongarchPLC(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: LoongarchPLC block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Position");
        outputNames.add("Angle");
        outputNames.add("dr");
        inputNames.add("in1");
        
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/include.vm", context);
        code.addIncludeCode(codeStr);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String arraysCode = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/output.vm", context);
        code.addOutputCode(codeStr);
    }


    public void generateStatementCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/LoongarchPLC/statement.vm", context);
        code.addStatementCode(codeStr);
    }
}
