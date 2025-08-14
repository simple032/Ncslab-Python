package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.RWork;
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

    
    
    /**
     * DTO-NATIVE Constructor - Creates DoubleTank block directly from BlockJson DTO
     */
    public DoubleTank(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: DoubleTank block created successfully - " + blockDto.getBlockName());
    }


    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Pump_Speed");
        outputNames.add("Water_Level");
        inputNames.add("in1");
        
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

        String codeStr = TemplateManager.renderTemplate("c/testrig/DoubleTank/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String includeCode = TemplateManager.renderTemplate("c/testrig/DoubleTank/include.vm", context);
        code.addIncludeCode(includeCode);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String arraysCode = TemplateManager.renderTemplate("c/testrig/DoubleTank/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/DoubleTank/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/DoubleTank/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
