package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class DCMotorAngleDirect extends Block {

    String hardwareDefineName;

    private String name = "DCMotorAngleDirect";

    private State speedState;

    private double motorK = 106.25;
    private double motorT = 0.07;

    private Parameter baudrate_encoder;
    private Parameter port_encoder;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("Speed");
        outputNames.add("Angle");
        inputNames.add("in1");
    }

    public DCMotorAngleDirect(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Speed", 1, false));
        outputPortList.add(new OutputPort(this, "Angle", 2, false));
        this.isHardware = true;

        port_encoder = new Parameter(this, 1, "port", blockJSON.optString("port", "\"/dev/ttyAMA0\""));
        parameterList.add(port_encoder);

        baudrate_encoder = new Parameter(this, 2, "baudrate", blockJSON.optString("baudrate", "115200"));
        parameterList.add(baudrate_encoder);

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

        context.put("block", this);
        context.put("states", stateList);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngleDirect/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode = "/*Code for include files of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        includeCode += "#include \"ncs_serialport.h\"\n";
        code.addIncludeCode(includeCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngleDirect/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngleDirect/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
