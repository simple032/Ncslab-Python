package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.WaterLevelDto;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaterLevel extends Block {
    private final double pumpK = 1;
    private final double pumpT = 2;

    private final double waterLevelK = 0.1;
    private final double waterLevelT = 50;

    State pumpState;
    State levelState;

    String hardwareDefineName;

    
    
    /**
     * DTO-NATIVE Constructor - Creates WaterLevel block directly from BlockDto DTO
     */
    public WaterLevel(WaterLevelDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: WaterLevel block created successfully - " + blockDto.getBlockName());
    }



    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Pump_Speed");
        outputNames.add("Water_Level");
        inputNames.add("in1");
        
        // WaterLevel block doesn't use configurable parameters - uses hardcoded values
        // Empty defaults to satisfy Block.java reflection requirement
    }

    public WaterLevel(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        this.isHardware = true;

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Pump_Speed", 1, false));
        outputPortList.add(new OutputPort(this, "Water_Level", 2, false));

        switch (model.getModelMode()) {
            case Simulation:
                pumpState = new State(this, 1, "pumpState");
                stateList.add(pumpState);
                levelState = new State(this, 2, "levelState");
                stateList.add(levelState);
                break;
            case Compilation:
                break;
        }
    }

    public String getHardwareDefineCodeC() {
        hardwareDefineName = "Block" + this.getBlockId() + "_WaterLevel";
        context.put("block", this);
        context.put("hardwareDefineName", hardwareDefineName);
        
        return TemplateManager.renderTemplate("c/testrig/WaterLevel/hardware_define.vm", context);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("states", stateList);

        String codeStr = TemplateManager.renderTemplate("m/testrig/WaterLevel/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));

        String codeStr = TemplateManager.renderTemplate("m/testrig/WaterLevel/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("states", stateList);
        context.put("outputPortVariables", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/testrig/WaterLevel/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/WaterLevel/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/WaterLevel/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/WaterLevel/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
