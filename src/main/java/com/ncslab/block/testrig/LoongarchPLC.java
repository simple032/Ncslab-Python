package com.ncslab.block.testrig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.RWork;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class LoongarchPLC extends Block {


    private String name = "LoongarchPLC";

//	State speedState;
//	State spState;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("Position");
        outputNames.add("Angle");
        outputNames.add("dr");
        inputNames.add("in1");
        parameterNames.add("gravity");
        parameterNames.add("mass_ball");
        parameterNames.add("moment_of_inertial");
        parameterNames.add("length_beam");
        parameterNames.add("length_link");
        parameterNames.add("radius_ball");
        parameterNames.add("lb_angle");
        parameterNames.add("ub_angle");
        parameterNames.add("lb_position");

    }

    public LoongarchPLC(JSONObject blockJSON,NCSLabModel model) {
        super(blockJSON,model);

        //һ�����룬�������
        inputPortList.add(new InputPort(this,1));
        outputPortList.add(new OutputPort(this,"Position",1,false));
//		outputPortList.add(new OutputPort(this,"AngleSpeed",2,false));
        outputPortList.add(new OutputPort(this,"Angle",2,false));
        outputPortList.add(new OutputPort(this,"dr",3,false));
        //outputPortList.add(new OutputPort(this,"Water_Level",2,false));

        stateList.add(new State(this, 1, "x0"));
        stateList.add(new State(this, 2, "x1"));
        stateList.add(new State(this, 3, "x2"));

        rworkList.add(new RWork(this, 1, "tem"));

        parameterList.add(new Parameter(this, 1, "gravity", ""));
        parameterList.add(new Parameter(this, 2, "mass_ball", ""));
        parameterList.add(new Parameter(this, 3, "moment_of_inertial", ""));
        parameterList.add(new Parameter(this, 4, "length_beam", ""));
        parameterList.add(new Parameter(this, 5, "length_link", ""));
        parameterList.add(new Parameter(this, 6, "radius_ball", ""));
        parameterList.add(new Parameter(this, 7, "lb_angle", ""));
        parameterList.add(new Parameter(this, 8, "ub_angle", ""));
        parameterList.add(new Parameter(this, 9, "lb_position", ""));
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

        //1.Open the serial port

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

        // String content = "if (<STATE0> <= lp) \n"
        //     + "\t<OUTPUT0> = lp;"
        //     + "else if ((<STATE0> > lp) && (<STATE0> <= up)) "
        //     + "\t<OUTPUT0> = <STATE0>;"
        //     + "else"
        //     + "\t<OUTPUT0> = up;"
        //     + "<OUTPUT1>=<STATE1>;"
        //     + "<OUTPUT2>=<RWORK0>*180/AERO_PI;";

        String content = "<OUTPUT0> = 2*<STATE0>;\n";


        outputCode += M2PCode2C(content) +"}\n";
        code.addOutputCode(outputCode);
    }

    public void  generateDerivativeCodeC(CodeStructC code) {
        String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

        // String content = "if (<INPUT0>< la)"
        // +"    u=la;"
        // +"else if ((<INPUT0> >= la) && (<INPUT0> <= ua))"
        // +"    u=<INPUT0>;"
        // +"else"
        // +"    u=ua;"
        // +"<STATED0>=x[1];"
        // +"<STATED2>=u-x[2];"
        // +"<STATED1>=-M*g*sin(d*u/L)/(J/(R*R)+M)+M*x[0]*d*d*<STATED2>*<STATED2>/(L*L*(J/(R*R)+M));";

        String content = "<STATE0> = 2*<INPUT0>;";

        derivativeCode += M2PCode2C(content);

        code.addDerivativeCode(derivativeCode);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
        statementCode +="#define TOUCHPAD_NAME \"Name=\\\"Touch p303\\\"\"\n";
        statementCode +="int hCommBPSUST;\n";
        statementCode +="int BPfd;\n";
        statementCode +="bool BPSUSTbool=true;\n";
        //statementCode +="struct input_event ev;\n";
        statementCode +="int t_x,t_y;\n"
                + "int BPpos1=0,BPpos2=0;\n";
        statementCode +="fd_set read_bpfds,write_bpfds;\n"
                + "struct timeval bptimeout;\n"
                + "int maxfdBP=0;\n";
        code.addStatementCode(statementCode);
    }
}
