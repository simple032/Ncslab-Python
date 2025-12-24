package com.ncslab.block.testrig;

import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.DCMotorAngleDto;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class DCMotorAngle extends Block {

    private String name = "DCMotorAngle";

    private State speedState;
    private State angleState;

    private double motorK=106.25;
    private double motorT=0.07;

    private double input_max = 1.0;
    private double input_min = -1.0;
    
    @Getter @Setter
    private double angledata = 0;
    @Getter @Setter
    private int angle_N = 0;


    private static final List<String> outputNames = new ArrayList<>();
    private static final List<String> inputNames = new ArrayList<>();

    
    
    /**
     * DTO-NATIVE Constructor - Creates DCMotorAngle block directly from BlockDto DTO
     */
    public DCMotorAngle(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        inputPortList.add(new InputPort(this,1));
        outputPortList.add(new OutputPort(this,"Speed",1,false));
        outputPortList.add(new OutputPort(this,"Angle",2,false));
        this.isHardware=true;

        switch(model.getModelMode()) {
        case Simulation:
            speedState=new State(this,1,"speedState");
            stateList.add(speedState);
            angleState=new State(this,2,"angleState");
            stateList.add(angleState);
            break;
        case Compilation:
            break;
        }
        System.out.println("DTO-NATIVE: DCMotorAngle block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Speed");
        outputNames.add("Angle");
        inputNames.add("in1");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("motorK", "106.25");
        PARAMETER_DEFAULTS.put("motorT", "0.07");
        PARAMETER_DEFAULTS.put("input_max", "1.0");
        PARAMETER_DEFAULTS.put("input_min", "-1.0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    public DCMotorAngle(JSONObject blockJSON,NCSLabModel model) {
        super(blockJSON,model);

        inputPortList.add(new InputPort(this,1));
        outputPortList.add(new OutputPort(this,"Speed",1,false));
        outputPortList.add(new OutputPort(this,"Angle",2,false));
        this.isHardware=true;

        switch(model.getModelMode()) {
        case Simulation:
            speedState=new State(this,1,"speedState");
            stateList.add(speedState);
            angleState=new State(this,2,"angleState");
            stateList.add(angleState);
            break;
        case Compilation:
            break;
        }
    }

    public String getHardwareDefineCodeC() {
        String hardwareDefineCode="";
        hardwareDefineCode+="HANDLE hComm;\n";
        hardwareDefineCode+="HANDLE hComm1;\n";
        return hardwareDefineCode;
    }

    public void generateArraysCodeC(CodeStructC code) {
        context.put("block", this);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngle/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("states", stateList);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngle/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
        includeCode += "#include \"ncs_serialport.h\"\n";
        code.addIncludeCode(includeCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        // Use standard context population for consistency
        com.ncslab.util.TemplateUtils.populateStandardContext(context, this);

        // Add block-specific context
        context.put("blockStateList", stateList);
        context.put("blockOutputPortVariables", java.util.Arrays.asList(getOutputPortVariables()));
        // Note: modelMode is already set by populateStandardContext

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngle/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add DCMotorAngle-specific variables
        context.put("name", name);
        context.put("realInput", getBlockName() + "_real_input");
        context.put("inputMax", input_max);
        context.put("inputMin", input_min);
        context.put("speedState", speedState);
        context.put("angleState", angleState);
        context.put("motorK", motorK);
        context.put("motorT", motorT);

        // Add state name variables for template
        context.put("speedStateName", speedState.getName());
        context.put("speedStateDerivativeName", speedState.getDerivativeName());
        context.put("angleStateName", angleState.getName());
        context.put("angleStateDerivativeName", angleState.getDerivativeName());
        context.put("inputVar", getInputPortVariable(0));

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngle/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    /**
     * Calculate motor derivatives based on first-order dynamics with input saturation
     * Motor model:
     *   d(speed)/dt = (4 * saturated_input * motorK - speed) / motorT
     *   d(angle)/dt = speed
     */
    @Override
    public void calculateDerivative(double t) {
        // Get input value from input port
        double inputValue = inputPortList.get(0).getLinkedLine()
                .getLinkedOutputPort().getOutputSignalC().getData().getInitValue();

        // Apply input saturation
        double saturatedInput;
        if (inputValue > input_max) {
            saturatedInput = input_max;
        } else if (inputValue < input_min) {
            saturatedInput = input_min;
        } else {
            saturatedInput = inputValue;
        }

        // Speed derivative: (4 * saturated_input * motorK - speed) / motorT
        double speedValue = speedState.getData().getInitValue();
        double speedDerivative = (4 * saturatedInput * motorK - speedValue) / motorT;
        speedState.setDerivateData(new Data(speedDerivative));

        // Angle derivative: speed (angle is integral of speed)
        angleState.setDerivateData(new Data(speedValue));
    }

    /**
     * Calculate motor outputs
     * Output port 0: Speed (rad/s)
     * Output port 1: Angle (degrees)
     */
    @Override
    public void calculateOutput(double t) {
        // Output speed state to port 0
        double speedValue = speedState.getData().getInitValue();
        outputPortList.get(0).getOutputSignalC().setValue(speedValue);

        // Output angle state to port 1
        double angleValue = angleState.getData().getInitValue();
        outputPortList.get(1).getOutputSignalC().setValue(angleValue);
    }
}
