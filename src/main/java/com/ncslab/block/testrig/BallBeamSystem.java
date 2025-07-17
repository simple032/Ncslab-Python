package com.ncslab.block.testrig;

import com.ncslab.block.data.DataType;
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
import org.apache.velocity.VelocityContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class BallBeamSystem extends Block {

    private String name = "BallBeamSystem";

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

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

        // Parameter defaults
        PARAMETER_DEFAULTS.put("gravity", "9.8");
        PARAMETER_DEFAULTS.put("mass_ball", "0.1");
        PARAMETER_DEFAULTS.put("moment_of_inertial", "0.001");
        PARAMETER_DEFAULTS.put("length_beam", "1.0");
        PARAMETER_DEFAULTS.put("length_link", "0.5");
        PARAMETER_DEFAULTS.put("radius_ball", "0.02");
        PARAMETER_DEFAULTS.put("lb_angle", "-30.0");
        PARAMETER_DEFAULTS.put("ub_angle", "30.0");
        PARAMETER_DEFAULTS.put("lb_position", "-0.5");
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

    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallBeamSystem/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallBeamSystem/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallBeamSystem/output.vm", context);
        code.addOutputCode(codeStr);
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
        VelocityContext context = new VelocityContext();
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("block", this);

        String includeCode = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/include.vm", context);
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
        VelocityContext context = new VelocityContext();
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("block", this);
        context.put("inputPortVariable", getInputPortVariable(0));

        String derivativeCode = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/derivative.vm", context);
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
