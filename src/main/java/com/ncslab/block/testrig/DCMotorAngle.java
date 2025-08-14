package com.ncslab.block.testrig;

import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DCMotorAngle extends Block {

    private String name = "DCMotorAngle";

    private State speedState;
    private State angleState;

    private double motorK=106.25;
    private double motorT=0.07;

    private double input_max = 1.0;
    private double input_min = -1.0;


    private static final Vector<String> outputNames = new Vector<>();
    private static final Vector<String> inputNames = new Vector<>();

    
    
    /**
     * DTO-NATIVE Constructor - Creates DCMotorAngle block directly from BlockJson DTO
     */
    public DCMotorAngle(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

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

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngle/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
