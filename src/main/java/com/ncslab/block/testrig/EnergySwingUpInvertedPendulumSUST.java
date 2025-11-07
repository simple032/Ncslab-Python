package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.EnergySwingUpInvertedPendulumSUSTDto;

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
import java.util.ArrayList;
import java.util.List;

public class EnergySwingUpInvertedPendulumSUST extends Block {

    private String name = "EnergySwingUpInvertedPendulumSUST";
    Parameter InnerFactor, InitSpeed;


    
    
    /**
     * DTO-NATIVE Constructor - Creates EnergySwingUpInvertedPendulumSUST block directly from BlockDto DTO
     */
    public EnergySwingUpInvertedPendulumSUST(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: EnergySwingUpInvertedPendulumSUST block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("AccOutput");
        outputNames.add("SpeedOutput");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
        inputNames.add("in4");
        inputNames.add("in5");
        inputNames.add("in6");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("InnerFactor", "1.0");
        PARAMETER_DEFAULTS.put("InitSpeed", "0.0");
    }

    public EnergySwingUpInvertedPendulumSUST(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        inputPortList.add(new InputPort(this, 1)); // Exe Flag
        inputPortList.add(new InputPort(this, 2)); // Angle (degree)
        inputPortList.add(new InputPort(this, 3)); // dif-Angle
        inputPortList.add(new InputPort(this, 4)); // xishu
        inputPortList.add(new InputPort(this, 5)); // Invel
        inputPortList.add(new InputPort(this, 6)); // xishu
        outputPortList.add(new OutputPort(this, "AccOutput", 1, false));
        outputPortList.add(new OutputPort(this, "SpeedOutput", 2, false));

        InnerFactor = getParameterByName("InnerFactor");

        InitSpeed = getParameterByName("InitSpeed");
    }

    public void generateIncludeCodeC(CodeStructC code) {
        String includeCode = "/*Code for include files of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
    }

    public void addLine(String originCode, String newLine) {
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/testrig/EnergySwingUpInvertedPendulumSUST/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        String derivativeCode = "/*Code for Derivative of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";

        code.addDerivativeCode(derivativeCode);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addStatementCode(statementCode);
    }
}
