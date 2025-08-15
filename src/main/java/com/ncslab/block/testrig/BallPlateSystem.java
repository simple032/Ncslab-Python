package com.ncslab.block.testrig;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.RWork;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

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
import java.util.ArrayList;
import java.util.List;

public class BallPlateSystem extends Block {

    private String name = "BallPlateSystem";


    
    
    /**
     * DTO-NATIVE Constructor - Creates BallPlateSystem block directly from BlockJson DTO
     */
    public BallPlateSystem(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: BallPlateSystem block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("x");
        outputNames.add("xdot");
        outputNames.add("thetax");
        outputNames.add("thetaxdot");
        outputNames.add("y");
        outputNames.add("ydot");
        outputNames.add("thetay");
        outputNames.add("thetaydot");
        inputNames.add("x");
        inputNames.add("y");

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

    public BallPlateSystem(JSONObject blockJSON, NCSLabModel model) {
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallPlateSystem/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallPlateSystem/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallPlateSystem/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSystem/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String includeCode = TemplateManager.renderTemplate("c/testrig/BallPlateSystem/include.vm", context);
        code.addIncludeCode(includeCode);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String arraysCode = TemplateManager.renderTemplate("c/testrig/BallPlateSystem/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSystem/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String derivativeCode = TemplateManager.renderTemplate("c/testrig/BallPlateSystem/derivative.vm", context);
        code.addDerivativeCode(derivativeCode);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addStatementCode(statementCode);
    }

    private List<State> getStates() {
        return stateList;
    }
}
