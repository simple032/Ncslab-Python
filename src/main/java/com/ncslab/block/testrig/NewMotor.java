package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.NewMotorDto;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class NewMotor extends Block {

    private String name = "NewMotor";

    private final double motorK = 0.01;
    private final double motorT = 0.07;
    
    /**
     * DTO-NATIVE Constructor - Creates NewMotor block directly from BlockDto DTO
     */
    public NewMotor(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        initializeBlock();
        System.out.println("DTO-NATIVE: NewMotor block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializeBlock() {
        // Initialize ports and states (same as JSON constructor)
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Speed", 1, false));

        this.isHardware = true;

        switch (model.getModelMode()) {
            case Simulation:
                speedState = new State(this, 1, "speedState");
                stateList.add(speedState);
                break;
            case Compilation:
                break;
        }
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Speed");
        inputNames.add("in1");

        // Parameter defaults        
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    State speedState;

  

    public NewMotor(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        initializeBlock();
    }

    String hardwareDefineName;

    public String getHardwareDefineCodeC() {
        hardwareDefineName = "Block" + this.getBlockId() + "_NewMotor";
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("hardwareDefineName", hardwareDefineName);

        return TemplateManager.renderTemplate("c/testrig/NewMotor/hardware_define.vm", context);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        
        String codeStr = TemplateManager.renderTemplate("m/testrig/NewMotor/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);
        
        String codeStr = TemplateManager.renderTemplate("m/testrig/NewMotor/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        
        String codeStr = TemplateManager.renderTemplate("m/testrig/NewMotor/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        hardwareDefineName = "Block" + this.getBlockId() + "_NewMotor";
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("hardwareDefineName", hardwareDefineName);

        String codeStr = TemplateManager.renderTemplate("c/testrig/NewMotor/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        //String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
        //code.addIncludeCode(includeCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Ensure hardwareDefineName is set
        if (hardwareDefineName == null) {
            hardwareDefineName = "Block" + this.getBlockId() + "_NewMotor";
        }
        context.put("hardwareDefineName", hardwareDefineName);

        String codeStr = TemplateManager.renderTemplate("c/testrig/NewMotor/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add NewMotor-specific variables
        context.put("speedState", speedState);
        context.put("motorK", motorK);
        context.put("motorT", motorT);

        String derivativeCode = TemplateManager.renderTemplate("c/testrig/NewMotor/derivative.vm", context);
        code.addDerivativeCode(derivativeCode);
    }

    /**
     * Calculate motor speed derivative based on first-order motor dynamics
     * Motor model: dSpeed/dt = (K*input - speed) / T
     * This represents a first-order lag system with gain K and time constant T
     */
    @Override
    public void calculateDerivative(double t) {        
            // Get input value
        double inputValue = inputPortList.get(0).getLinkedLine()
                .getLinkedOutputPort().getOutputSignalC().getData().getInitValue();        

        // Motor speed derivative: (K*input - speed) / T
        double speedValue = speedState.getData().getInitValue();
        double derivative = (motorK * inputValue - speedValue) / motorT;
        Data derivativeData = new Data(derivative);
        speedState.setDerivateData(derivativeData);        
    }

    /**
     * Calculate motor output
     * In simulation mode: output = 5000 * speedState
     */
    @Override
    public void calculateOutput(double t) {                
        // Output is speed state scaled by 5000 (matches C code: output = 5000 * speedState)
        double speedValue = speedState.getData().getInitValue();
        double output = 5000.0 * speedValue;

        outputPortList.get(0).getOutputSignalC().setValue(output);        
    }
}
