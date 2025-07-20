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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class InvertedPendulumSUST extends Block {

    Parameter Vspeed;
    Parameter ENAOrDIS;


    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Vspeed", "1.0");
        PARAMETER_DEFAULTS.put("ENAOrDIS", "1");

    }

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("Real_X");
        outputNames.add("Angle");
        inputNames.add("in1");
        inputNames.add("in2");
    }

    public InvertedPendulumSUST(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, "Real_X", 1, false));
        outputPortList.add(new OutputPort(this, "Angle", 2, false));

        Vspeed = new Parameter(this, parameterList.size() + 1, "Vspeed", paramValues.getString("Vspeed"));
        ENAOrDIS = new Parameter(this, parameterList.size() + 1, "ENAOrDIS", paramValues.getString("ENAOrDIS"));
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

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/InvertedPendulumSUST/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/InvertedPendulumSUST/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

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
