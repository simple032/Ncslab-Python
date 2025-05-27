package com.ncslab.block.testrig;

import com.ncslab.block.io.RWork;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class BallBeamSystem extends Block {

    private String name = "BallBeamSystem";

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

    public BallBeamSystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "Position", 1, false));
        outputPortList.add(new OutputPort(this, "Angle", 2, false));
        outputPortList.add(new OutputPort(this, "dr", 3, false));

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

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode = "/*Code for include files of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addIncludeCode(includeCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", getStates());
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        String derivativeCode = "/*Code for Derivative of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";

        String content = "if (" + getInputPortVariable(0) + "< la)"
                + "    u=la;"
                + "else if ((" + getInputPortVariable(0) + " >= la) && (" + getInputPortVariable(0) + " <= ua))"
                + "    u=" + getInputPortVariable(0) + ";"
                + "else"
                + "    u=ua;"
                + getDerivativeVariable(0) + "=" + getStateVariable(1) + ";"
                + getDerivativeVariable(2) + ">=u-" + getStateVariable(2) + ";"
                + getDerivativeVariable(1) + "=-M*g*sin(d*u/L)/(J/(R*R)+M)+M*" + getStateVariable(0)
                + "*d*d*" + getDerivativeVariable(2) + "*" + getDerivativeVariable(2)
                + "/(L*L*(J/(R*R)+M));";

        derivativeCode += content + ";\n";

        code.addDerivativeCode(derivativeCode);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addStatementCode(statementCode);
    }

    private Vector<State> getStates() {
        return stateList;
    }
}
