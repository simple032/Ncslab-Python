package com.ncslab.block.testrig;

import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class BallPlateSUST extends Block {

    private String name = "BallPlateSUST";


    
    
    /**
     * DTO-NATIVE Constructor - Creates BallPlateSUST block directly from BlockJson DTO
     */
    public BallPlateSUST(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: BallPlateSUST block created successfully - " + blockDto.getBlockName());
    }


    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("Real_X");
        outputNames.add("Real_Y");
        outputNames.add("MotorXPos");
        outputNames.add("MotorYPos");

        inputNames.add("in1");
        inputNames.add("in2");
        
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSUST/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSUST/include.vm", context);
        code.addIncludeCode(codeStr);
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSUST/output.vm", context);
        code.addOutputCode(codeStr);
    }
    
    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSUST/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/BallPlateSUST/statement.vm", context);
        code.addStatementCode(codeStr);
    }
}
