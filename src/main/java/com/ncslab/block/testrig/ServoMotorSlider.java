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

public class ServoMotorSlider extends Block {

    private String name = "ServoMotorSlider";

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("Position");
        inputNames.add("in1");
    }

    String hardwareDefineName;
    String realName;
    private Vector<State> xStateList = new Vector<State>();
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
        String hardwareDefineCode = "";
        // add some head files
        hardwareDefineCode += "#include\"DEV_Config.h\"\n";
        hardwareDefineCode += "#include\"DAC8532.h\"\n";
        hardwareDefineCode += "HANDLE hComm;\n";
        return hardwareDefineCode;
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("states", xStateList);
        context.put("modelMode", model.getModelMode().name());

        String initCode = TemplateManager.renderTemplate("c/testrig/ServoMotorSlider/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateArraysCodeC(CodeStructC code) {
        String arraysCode = "/* Define arrays for block ServoMotorSlider:(" + getBlockId() + ")" + getBlockName() + " */\n";
        arraysCode += "double servodata=0;\n";
        arraysCode += "double servodata1=0;\n";
        code.addArraysCode(arraysCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", xStateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/ServoMotorSlider/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", xStateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/ServoMotorSlider/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
