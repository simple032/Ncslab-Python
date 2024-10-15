package com.ncslab.block.testrig;

import com.ncslab.block.Block;
import com.ncslab.block.io.*;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MagneticLevitationSystem extends Block {


    private String name = "MagneticLevitationSystem";

//	State speedState;
//	State spState;
    Parameter gravity;
    Parameter x0;
    Parameter i0;
    Parameter Ks;
    Parameter Ka;

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    static {
        inputNames.add("in1"); // 假设输入端口的名称为"in1"，因为构造函数中没有提供输入端口的名称
        outputNames.add("Position");
        outputNames.add("Velocity");
        parameterNames.add("gravity");
        parameterNames.add("EQUILIBRIUM_POINT_x0");
        parameterNames.add("EQUILIBRIUM_POINT_i0");
        parameterNames.add("TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT");
        parameterNames.add("INPUT_RESISTANCE");
    }

    public MagneticLevitationSystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON,model);

        //一输入，两输出
        inputPortList.add(new InputPort(this,1));
        outputPortList.add(new OutputPort(this,"Position",1,false));
		outputPortList.add(new OutputPort(this,"Velocity",2,false));

        stateList.add(new State(this, 1, "x0"));
        stateList.add(new State(this, 2, "x1"));

        rworkList.add(new RWork(this, 1, "tem"));

        gravity = new Parameter(this, 1, "gravity", "9.8");
        parameterList.add(gravity);
        x0 = new Parameter(this, 2, "EQUILIBRIUM_POINT_x0", "0");
        parameterList.add(x0);
        i0 = new Parameter(this, 3, "EQUILIBRIUM_POINT_i0", "0");
        parameterList.add(i0);
        Ks = new Parameter(this, 4, "TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT", "0");
        parameterList.add(Ks);
        Ka = new Parameter(this, 5, "INPUT_RESISTANCE", "0");
        parameterList.add(Ka);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode="";
        code.addInitCode(initCode);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);

        String derivativeCode="";

        code.addDerivativeCode(derivativeCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode="";

        code.addOutputCode(outputCode);
    }


    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        String initCode = "/* Code for initialization of block " + name + ":( " + getBlockId() +" ) " + getBlockName() + " */\n";

        code.addInitCode(initCode);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		`code.addIncludeCode(includeCode);
    }

    public void addLine(String originCode, String newLine) {

    }

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

        outputCode+="{\n";

        String content = "<OUTPUT0> = <STATE0>;\n"
                + "<OUTPUT1> = <STATE1>;\n";

        outputCode += M2PCode2C(content) +"}\n";
        code.addOutputCode(outputCode);
    }


    public void  generateDerivativeCodeC(CodeStructC code) {
        String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";


        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("{\n")
                .append("real_T g=").append(gravity.getName()).append(";\n")
                .append("real_T  x_0    = ").append(x0.getName()).append(";\n")
                .append("real_T  i_0    = ").append(i0.getName()).append(";\n")
                .append("real_T  Ks    = ").append(Ks.getName()).append(";\n")
                .append("real_T  Ka    = ").append(Ka.getName()).append(";\n")
                .append("<STATE0> = 2*<INPUT0>;\n")
                .append("<DSTATE0>=<STATE1>;\n")
                .append("<DSTATE1>=2*g*<STATE0>/x_0-2*g*Ks*<INPUT0>/(Ka*i_0);\n")
                .append("}");

        String content = contentBuilder.toString();

        derivativeCode += M2PCode2C(content);

        code.addDerivativeCode(derivativeCode);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

        code.addStatementCode(statementCode);
    }
}
