package com.ncslab.block.testrig;

import lombok.Getter;
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

public class SecondOrderRotaryInvertedPendulum extends Block {

    private String blockType = "SecondOrderRotaryInvertedPendulum";

    State speedState;
    State spState;

    
    
    /**
     * DTO-NATIVE Constructor - Creates SecondOrderRotaryInvertedPendulum block directly from BlockJson DTO
     */
    public SecondOrderRotaryInvertedPendulum(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: SecondOrderRotaryInvertedPendulum block created successfully - " + blockDto.getBlockName());
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

    public SecondOrderRotaryInvertedPendulum(JSONObject blockJSON, NCSLabModel model) {
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
        context.put("block", this);
        context.put("states", stateList);

        String codeStr = TemplateManager.renderTemplate("m/testrig/SecondOrderRotaryInvertedPendulum/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));

        String codeStr = TemplateManager.renderTemplate("m/testrig/SecondOrderRotaryInvertedPendulum/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("states", stateList);
        context.put("outputPortVariables", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/testrig/SecondOrderRotaryInvertedPendulum/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/SecondOrderRotaryInvertedPendulum/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode = "/*Code for include files of block " + blockType + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addIncludeCode(includeCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputPortVariables", getInputPortVariables());
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/SecondOrderRotaryInvertedPendulum/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/SecondOrderRotaryInvertedPendulum/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
