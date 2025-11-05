package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.Dq02abcDto;
import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.ncslab.util.TemplateManager;

public class dq02abc extends MathBlock {
    private String function;


    
    
    /**
     * DTO-NATIVE Constructor - Creates dq02abc block directly from BlockDto DTO
     */
    public dq02abc(Dq02abcDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: dq02abc block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("rotatingFrame", "Stationary reference frame");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    static {
        
        outputNames.add("out1");
        outputNames.add("out2");
        outputNames.add("out3");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
        inputNames.add("in4");
    }

    public dq02abc(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        inputPortList.add(new InputPort(this, 3));
        inputPortList.add(new InputPort(this, 4));

        outputPortList.add(new OutputPort(this, 1, true));
        outputPortList.add(new OutputPort(this, 2, true));
        outputPortList.add(new OutputPort(this, 3, true));

        function = paramValues.getString("rotatingFrame");
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of block dq02abc:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/dq02abc/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private String getFunction() {
        return function;
    }

    public void updateDimension() throws MatDimException {
    }

    public void checkDimension() throws MatDimException {
    }
    
    @Override
    public void calculateInit() {
        // Initialization logic for dq02abc block
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK dq0 to abc transformation block
        // Inputs: d, q, 0 components (in1, in2, in3) and angle theta (in4)
        com.ncslab.block.data.Data dData = inputPortList.get(0).getData();
        com.ncslab.block.data.Data qData = inputPortList.get(1).getData();
        com.ncslab.block.data.Data zeroData = inputPortList.get(2).getData();
        com.ncslab.block.data.Data thetaData = inputPortList.get(3).getData();
        
        double d = dData.getInitValue();
        double q = qData.getInitValue();
        double zero = zeroData.getInitValue();
        double theta = thetaData.getInitValue();
        
        // dq0 to abc transformation (inverse of abc to dq0)
        // Standard inverse transformation matrix:
        // [ a ]   [  cos(theta)        -sin(theta)      1 ] [ d ]
        // [ b ] = [  cos(theta-2π/3)   -sin(theta-2π/3) 1 ] [ q ]
        // [ c ]   [  cos(theta+2π/3)   -sin(theta+2π/3) 1 ] [ 0 ]
        
        if ("Stationary reference frame".equals(function)) {
            // Stationary reference frame (theta = 0)
            theta = 0;
        }
        
        // Calculate a component
        double a = d * Math.cos(theta) - q * Math.sin(theta) + zero;
        
        // Calculate b component
        double angle_b = theta - 2.0 * Math.PI / 3.0;
        double b = d * Math.cos(angle_b) - q * Math.sin(angle_b) + zero;
        
        // Calculate c component  
        double angle_c = theta + 2.0 * Math.PI / 3.0;
        double c = d * Math.cos(angle_c) - q * Math.sin(angle_c) + zero;
        
        // Set output data
        com.ncslab.block.data.Data aOutputData = new com.ncslab.block.data.Data(1, 1);
        aOutputData.setInitValue(a);
        outputPortList.get(0).setData(aOutputData);
        
        com.ncslab.block.data.Data bOutputData = new com.ncslab.block.data.Data(1, 1);
        bOutputData.setInitValue(b);
        outputPortList.get(1).setData(bOutputData);
        
        com.ncslab.block.data.Data cOutputData = new com.ncslab.block.data.Data(1, 1);
        cOutputData.setInitValue(c);
        outputPortList.get(2).setData(cOutputData);
    }
}
