package com.ncslab.block.testrig;

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

public class InvertedPendulumSUST extends Block {

    Parameter Vspeed;
    Parameter ENAOrDIS;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("Real_X");
        outputNames.add("Angle");
        inputNames.add("in1");
        inputNames.add("in2");
        parameterNames.add("Vspeed");
        parameterNames.add("ENAOrDIS");
    }

    public InvertedPendulumSUST(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, "Real_X", 1, false));
        outputPortList.add(new OutputPort(this, "Angle", 2, false));

        Vspeed = new Parameter(this, parameterList.size() + 1, "Vspeed", paramValues.getString("Vspeed"));
        parameterList.add(Vspeed);
        ENAOrDIS = new Parameter(this, parameterList.size() + 1, "ENAOrDIS", paramValues.getString("ENAOrDIS"));
        parameterList.add(ENAOrDIS);
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
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/InvertedPendulumSUST/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputPortVariables", getInputPortVariables());
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/InvertedPendulumSUST/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/InvertedPendulumSUST/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + this.blockType + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        statementCode += "HANDLE hCommIPSUST;\n";
        statementCode += "bool IPSUSTbool=true;\n";
        statementCode += "float AngleIP=0,xPOSIP=0;\n";
        statementCode += "int SwingUpFlag=0;\n";
        code.addStatementCode(statementCode);
    }
}
