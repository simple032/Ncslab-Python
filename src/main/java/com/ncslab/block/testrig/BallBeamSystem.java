package com.ncslab.block.testrig;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.RWork;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.BallBeamSystemDto;

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

public class BallBeamSystem extends Block {

    private String name = "BallBeamSystem";


    
    
    /**
     * DTO-NATIVE Constructor - Creates BallBeamSystem block directly from BlockDto DTO
     */
    public BallBeamSystem(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: BallBeamSystem block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Position");
        outputNames.add("Angle");
        outputNames.add("dr");
        inputNames.add("in1");

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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallBeamSystem/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallBeamSystem/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/testrig/BallBeamSystem/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String includeCode = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/include.vm", context);
        code.addIncludeCode(includeCode);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String arraysCode = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add template variables required by derivative.vm template
        context.put("inputPortVariable", safeGetInputPortVariable(0, "0.0"));
        
        // Add constraint variables from parameters or defaults
        Parameter lbAngleParam = getParameterByName("lb_angle");
        Parameter ubAngleParam = getParameterByName("ub_angle");
        context.put("la", lbAngleParam != null ? lbAngleParam.getData().getInitString() : PARAMETER_DEFAULTS.get("lb_angle"));
        context.put("ua", ubAngleParam != null ? ubAngleParam.getData().getInitString() : PARAMETER_DEFAULTS.get("ub_angle"));
        
        // Add state derivative variables
        if (stateList.size() >= 3) {
            context.put("stateX0DerivativeName", stateList.get(0).getDerivativeName());
            context.put("stateX1DerivativeName", stateList.get(1).getDerivativeName());
            context.put("stateX2DerivativeName", stateList.get(2).getDerivativeName());
            
            context.put("stateX0", stateList.get(0).getName());
            context.put("stateX1", stateList.get(1).getName());
            context.put("stateX2", stateList.get(2).getName());
        }
        
        // Add physical constants from parameters or defaults
        Parameter massParam = getParameterByName("mass_ball");
        Parameter gravityParam = getParameterByName("gravity");
        Parameter lengthParam = getParameterByName("length_beam");
        Parameter momentParam = getParameterByName("moment_of_inertial");
        Parameter radiusParam = getParameterByName("radius_ball");
        Parameter lengthLinkParam = getParameterByName("length_link");
        
        context.put("M", massParam != null ? massParam.getData().getInitString() : PARAMETER_DEFAULTS.get("mass_ball"));
        context.put("g", gravityParam != null ? gravityParam.getData().getInitString() : PARAMETER_DEFAULTS.get("gravity"));
        context.put("L", lengthParam != null ? lengthParam.getData().getInitString() : PARAMETER_DEFAULTS.get("length_beam"));
        context.put("J", momentParam != null ? momentParam.getData().getInitString() : PARAMETER_DEFAULTS.get("moment_of_inertial"));
        context.put("R", radiusParam != null ? radiusParam.getData().getInitString() : PARAMETER_DEFAULTS.get("radius_ball"));
        context.put("d", lengthLinkParam != null ? lengthLinkParam.getData().getInitString() : PARAMETER_DEFAULTS.get("length_link"));
        
        // Add local variable declarations if needed
        context.put("localVariableDeclarations", "double u = 0.0; // Control input");

        String derivativeCode = TemplateManager.renderTemplate("c/testrig/BallBeamSystem/derivative.vm", context);
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
