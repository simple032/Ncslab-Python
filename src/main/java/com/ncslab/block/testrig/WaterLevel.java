package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class WaterLevel extends Block {
    private final double pumpK = 1;
    private final double pumpT = 2;

    private final double waterLevelK = 0.1;
    private final double waterLevelT = 50;

    State pumpState;
    State levelState;

    String hardwareDefineName;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("Pump_Speed");
        outputNames.add("Water_Level");
        inputNames.add("in1");
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

        context.put("block", this);
        context.put("states", stateList);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/WaterLevel/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/WaterLevel/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/WaterLevel/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
