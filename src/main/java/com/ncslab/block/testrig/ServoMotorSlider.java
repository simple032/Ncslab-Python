package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.ServoMotorSliderDto;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ServoMotorSlider extends Block {

    private String name = "ServoMotorSlider";

    
    
    /**
     * DTO-NATIVE Constructor - Creates ServoMotorSlider block directly from BlockDto DTO
     */
    public ServoMotorSlider(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: ServoMotorSlider block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Position");
        inputNames.add("in1");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("num_0", "0");
        PARAMETER_DEFAULTS.put("num_1", "17.41");
        PARAMETER_DEFAULTS.put("num_2", "123.4");
        PARAMETER_DEFAULTS.put("den_0", "1");
        PARAMETER_DEFAULTS.put("den_1", "2.01");
        PARAMETER_DEFAULTS.put("den_2", "38.86");
        PARAMETER_DEFAULTS.put("den_3", "49.06");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    String hardwareDefineName;
    String realName;
    private List<State> xStateList = new ArrayList<>();
    // simulation parameter
    private double num[] = {0, 17.41, 123.4};
    private double den[] = {1, 2.01, 38.86, 49.06};

    public ServoMotorSlider(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Position", 1, false));
        this.isHardware = true;

        switch (model.getModelMode()) {
            case Simulation:
                for (int i = 0; i < 3; i++) {
                    State xState = new State(this, i + 1, "x" + (i + 1));
                    xStateList.add(xState);
                    stateList.add(xState);
                }
                break;

            case Compilation:
                break;
        }
    }

    public String getHardwareDefineCodeC() {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        return TemplateManager.renderTemplate("c/testrig/ServoMotorSlider/hardware_define.vm", context);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);

        String initCode = TemplateManager.renderTemplate("c/testrig/ServoMotorSlider/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/ServoMotorSlider/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);
        context.put("blockStateList", xStateList);
        context.put("blockOutputPortVariables", java.util.Arrays.asList(getOutputPortVariables()));

        // Add num and den arrays to context
        context.put("num", java.util.Arrays.stream(num).boxed().collect(java.util.stream.Collectors.toList()));
        context.put("den", java.util.Arrays.stream(den).boxed().collect(java.util.stream.Collectors.toList()));

        // Ensure hardwareDefineName is set
        if (hardwareDefineName == null) {
            hardwareDefineName = "Block" + this.getBlockId() + "_ServoMotorSlider";
        }
        context.put("hardwareDefineName", hardwareDefineName);

        String codeStr = TemplateManager.renderTemplate("c/testrig/ServoMotorSlider/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);
        context.put("blockStateList", xStateList);

        // Add num and den arrays to context
        context.put("num", java.util.Arrays.stream(num).boxed().collect(java.util.stream.Collectors.toList()));
        context.put("den", java.util.Arrays.stream(den).boxed().collect(java.util.stream.Collectors.toList()));

        if(model.getModelMode() == ModelMode.Simulation) {
            String codeStr = TemplateManager.renderTemplate("c/testrig/ServoMotorSlider/derivative.vm", context);
            code.addDerivativeCode(codeStr);
        }

    }

    @Override
    public void calculateInit() {
        // Initialize all states to zero
        for (State xState : xStateList) {
            xState.setData(new com.ncslab.block.data.Data(0.0));
        }

        // Initialize output to zero
        if (!outputPortList.isEmpty()) {
            outputPortList.get(0).setData(new com.ncslab.block.data.Data(0.0)); // Position
        }
    }

    @Override
    public void calculateOutput(double t) {
        if (xStateList.isEmpty() || outputPortList.isEmpty()) {
            return;
        }

        // Calculate output using numerator coefficients: y = num[0]*x1 + num[1]*x2 + num[2]*x3
        com.ncslab.block.data.Data output = new com.ncslab.block.data.Data(0.0);

        for (int i = 0; i < xStateList.size() && i < num.length; i++) {
            com.ncslab.block.data.Data stateContribution = xStateList.get(i).getData()
                .times(new com.ncslab.block.data.Data(num[i]));
            output = output.plus(stateContribution);
        }

        // Set output
        outputPortList.get(0).setData(output); // Position
    }

    @Override
    public void calculateDerivative(double t) {
        if (xStateList.isEmpty() || inputPortList.isEmpty()) {
            return;
        }

        // Get input value
        com.ncslab.block.data.Data inputData = inputPortList.get(0).getData();

        // Controller canonical form state-space representation:
        // x'1 = x2
        // x'2 = x3
        // x'3 = input - den[3]*x1 - den[2]*x2 - den[1]*x3

        // First n-1 states: x'i = x(i+1)
        for (int i = 0; i < xStateList.size() - 1; i++) {
            xStateList.get(i).setDerivateData(xStateList.get(i + 1).getData());
        }

        // Last state: x'n = input - sum(den[i] * x[i])
        // For den = [1, 2.01, 38.86, 49.06], we need: input - den[1]*x1 - den[2]*x2 - den[3]*x3
        com.ncslab.block.data.Data lastDerivative = inputData;

        // Subtract den coefficients times states (skip den[0] which is always 1)
        for (int i = 0; i < xStateList.size(); i++) {
            com.ncslab.block.data.Data term = xStateList.get(i).getData()
                .times(new com.ncslab.block.data.Data(den[i + 1]));
            lastDerivative = lastDerivative.minus(term);
        }

        xStateList.get(xStateList.size() - 1).setDerivateData(lastDerivative);
    }
}
