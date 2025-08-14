package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DCMotorAngleDirect extends Block {

    String hardwareDefineName;

    private String name = "DCMotorAngleDirect";

    private State speedState;

    private double motorK = 106.25;
    private double motorT = 0.07;

    private Parameter baudrate_encoder;
    private Parameter port_encoder;


    
    
    /**
     * DTO-NATIVE Constructor - Creates DCMotorAngleDirect block directly from BlockJson DTO
     */
    public DCMotorAngleDirect(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: DCMotorAngleDirect block created successfully - " + blockDto.getBlockName());
    }


    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Speed");
        outputNames.add("Angle");
        inputNames.add("in1");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("motorK", "106.25");
        PARAMETER_DEFAULTS.put("motorT", "0.07");
        PARAMETER_DEFAULTS.put("port", "\"/dev/ttyAMA0\"");
        PARAMETER_DEFAULTS.put("baudrate", "115200");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    public DCMotorAngleDirect(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Speed", 1, false));
        outputPortList.add(new OutputPort(this, "Angle", 2, false));
        this.isHardware = true;

        port_encoder = new Parameter(this, 1, "port", blockJSON.optString("port", "\"/dev/ttyAMA0\""));

        baudrate_encoder = new Parameter(this, 2, "baudrate", blockJSON.optString("baudrate", "115200"));

        switch (model.getModelMode()) {
            case Simulation:
                speedState = new State(this, 1, "speedState");
                stateList.add(speedState);
                break;
            case Compilation:
                break;
        }
    }

    public String getHardwareDefineCodeC() {
        String hardwareDefineCode = "";
        hardwareDefineName = "Block" + this.getBlockId() + "_DCMotorAngleDirect";
        hardwareDefineCode += "DCMOTORANGLEDIRECT " + hardwareDefineName + ";";

        return hardwareDefineCode;
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngleDirect/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("name", name);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngleDirect/include.vm", context);
        code.addIncludeCode(codeStr);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngleDirect/output.vm", context);
        code.addOutputCode(codeStr);
    }

}
