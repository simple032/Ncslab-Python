package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class RaspFan extends Block {
    String hardwareDefineName;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("FanSpeed");
        inputNames.add("in1");
    }

    public RaspFan(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        this.isHardware = true;
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "FanSpeed", 1, false));
    }

    public String getHardwareDefineCodeC() {
        String hardwareDefineCode = "";
        hardwareDefineName = "Block" + this.getBlockId() + "_RaspFan";
        hardwareDefineCode += "RASPFAN " + hardwareDefineName + ";\n";
        hardwareDefineCode += "HANDLE hComm;\n";
        return hardwareDefineCode;
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("modelMode", model.getModelMode().name());

        String initCode = "/* Code for initialization of block RaspFan:(" + getBlockId() + ")" + getBlockName() + " */\n";
        switch (model.getModelMode()) {
            case Simulation:
                break;
            case Compilation:
                hardwareDefineName = "Block" + this.getBlockId() + "_RaspFan";
                initCode += "initRaspFan(&" + hardwareDefineName + ");\n";
                String port = "\"/dev/ttyUSB0\"";
                int baudrate = 9600;
                initCode += "char msg[255];\n";
                initCode += "hComm = Serialport_Open((char *)" + port + ", " + baudrate + ",(char *)msg);\n";
                break;
        }
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/RaspFan/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/RaspFan/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
