package com.ncslab.block.testrig;

import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class DCMotorAngle extends Block {

    private String name = "DCMotorAngle";

    private State speedState;
    private State angleState;

    private double motorK=106.25;
    private double motorT=0.07;

    private double input_max = 1.0;
    private double input_min = -1.0;

    @Getter
    private static final Vector<String> parameterNames = new Vector<>();

    @Getter
    private static final Vector<String> outputNames = new Vector<>();
    @Getter
    private static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("Speed");
        outputNames.add("Angle");
        inputNames.add("in1");
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
        String arraysCode="/*Define arrays for block DCMotorAngle:("+getBlockId()+")"+getBlockName()+"*/\n";
        arraysCode+="double angledata=0;\n";
        arraysCode+="double angledata1=0;\n";
        arraysCode+="int angle_N=0;\n";
        code.addArraysCode(arraysCode);
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
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariables", getInputPortVariables());
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/DCMotorAngle/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
        String real_input = getBlockName() + "_real_input";
        switch(model.getModelMode()) {
        case Simulation:
            derivativeCode+="double " + real_input + " = 0.0;\n";
            derivativeCode+="if("+ this.getInputPortVariable(0) +">"+input_max+")\n";
            derivativeCode+="\t" +real_input+ "="+input_max+";\n";
            derivativeCode+="else if("+ this.getInputPortVariable(0) +"<"+input_min + ")\n";
            derivativeCode+="\t" +real_input+ "="+input_min+";\n";
            derivativeCode+="else\n";
            derivativeCode+="\t" +real_input+ "="+this.getInputPortVariable(0)+";\n";
            derivativeCode+=speedState.getDerivativeName()+"=("
                    +"4*"+real_input
                    +"*"+motorK+"-"+speedState.getName()+")"
                    +"*"+(1/motorT)
                    +";\n";
            derivativeCode+=angleState.getDerivativeName()+"="
                    +speedState.getName()+";\n";
        case Compilation:
            break;
        }

        code.addDerivativeCode(derivativeCode);
    }
}
