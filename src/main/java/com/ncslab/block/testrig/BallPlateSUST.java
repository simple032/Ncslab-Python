package com.ncslab.block.testrig;

import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class BallPlateSUST extends Block {

    private String name = "BallPlateSUST";

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("Real_X");
        outputNames.add("Real_Y");
        outputNames.add("MotorXPos");
        outputNames.add("MotorYPos");

        inputNames.add("in1");
        inputNames.add("in2");
    }

    public BallPlateSUST(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, "Real_X", 1, false));
        outputPortList.add(new OutputPort(this, "Real_Y", 2, false));
        outputPortList.add(new OutputPort(this, "MotorXPos", 3, false));
        outputPortList.add(new OutputPort(this, "MotorYPos", 4, false));

        this.isHardware = true;
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode = "";
        code.addInitCode(initCode);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        String derivativeCode = "";
        code.addDerivativeCode(derivativeCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode = "";
        code.addOutputCode(outputCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("states", stateList);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSUST/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSUST/output.vm", context);
        code.addOutputCode(codeStr);
    }
    
    public void generateDerivativeCodeC(CodeStructC code) {
        String derivativeCode = "/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
        code.addDerivativeCode(derivativeCode);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
        statementCode += "#define TOUCHPAD_NAME \"Name=\\\"Touch p303\\\"\"\n";
        statementCode += "int hCommBPSUST;\n";
        statementCode += "int BPfd;\n";
        statementCode += "bool BPSUSTbool=true;\n";
        statementCode += "struct input_event ev;\n";
        statementCode += "int t_x,t_y;\n" + "int BPpos1=0,BPpos2=0;\n";
        statementCode += "fd_set read_bpfds,write_bpfds;\n" + "struct timeval bptimeout;\n" + "int maxfdBP=0;\n";
        code.addStatementCode(statementCode);
    }
}
