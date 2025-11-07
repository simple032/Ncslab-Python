package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.InvertedPendulumDto;

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

public class InvertedPendulum extends Block {

    private String name = "InvertedPendulum";

    
    
    /**
     * DTO-NATIVE Constructor - Creates InvertedPendulum block directly from BlockDto DTO
     */
    public InvertedPendulum(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        initializeBlock();
        System.out.println("DTO-NATIVE: InvertedPendulum block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializeBlock() {
        // Initialize ports and states (same as JSON constructor)
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Angle", 1, false));
        outputPortList.add(new OutputPort(this, "Set_X", 2, false));
        outputPortList.add(new OutputPort(this, "Real_X", 3, false));

        spState = new State(this, 1, "SerialPortState");
        stateList.add(spState);
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Angle");
        outputNames.add("Set_X");
        outputNames.add("out3");
        inputNames.add("Real_X");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    State spState;

    public InvertedPendulum(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Angle", 1, false));
        outputPortList.add(new OutputPort(this, "Set_X", 2, false));
        outputPortList.add(new OutputPort(this, "Real_X", 3, false));

        spState = new State(this, 1, "SerialPortState");
        stateList.add(spState);
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

        String codeStr = TemplateManager.renderTemplate("c/testrig/InvertedPendulum/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode = "/*Code for include files of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addIncludeCode(includeCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add testrig-specific template variables
        if (spState != null) {
            context.put("stateX0", spState.getName() + "(0,0)"); 
            context.put("stateX1", spState.getName() + "(1,0)");
            context.put("stateX2", spState.getName() + "(2,0)");
        }
        
        // Add output variables for template
        if (outputPortList.size() > 0) {
            context.put("output1", outputPortList.get(0).getOutputSignalC().getName());
        }
        if (outputPortList.size() > 1) {
            context.put("output2", outputPortList.get(1).getOutputSignalC().getName());
        }
        if (outputPortList.size() > 2) {
            context.put("output3", outputPortList.get(2).getOutputSignalC().getName());
        }
        
        // Add commonly needed testrig variables
        context.put("lb_position", -0.5); // Default lower bound
        context.put("ub_angle", 0.5);     // Default upper bound  
        context.put("rwork_tem", 0.0);    // Temporary work variable

        String codeStr = TemplateManager.renderTemplate("c/testrig/InvertedPendulum/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/InvertedPendulum/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
