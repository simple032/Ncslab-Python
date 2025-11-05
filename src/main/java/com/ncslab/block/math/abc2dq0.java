package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.Abc2dq0Dto;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class abc2dq0 extends MathBlock{
    String function;


    
    
    /**
     * DTO-NATIVE Constructor - Creates abc2dq0 block directly from BlockDto DTO
     */
    public abc2dq0(Abc2dq0Dto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: abc2dq0 block created successfully - " + blockDto.getBlockName());
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

    public abc2dq0(JSONObject blockJSON, NCSLabModel model) {
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
        String initCode = "/*Code for initialization of block abc2dq0:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/abc2dq0/output.vm", context);
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
        // Initialization logic for abc2dq0 block
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK abc to dq0 transformation block
        // Inputs: a, b, c phases (in1, in2, in3) and angle theta (in4)
        com.ncslab.block.data.Data aData = inputPortList.get(0).getData();
        com.ncslab.block.data.Data bData = inputPortList.get(1).getData();
        com.ncslab.block.data.Data cData = inputPortList.get(2).getData();
        com.ncslab.block.data.Data thetaData = inputPortList.get(3).getData();
        
        double a = aData.getInitValue();
        double b = bData.getInitValue();
        double c = cData.getInitValue();
        double theta = thetaData.getInitValue();
        
        // abc to dq0 transformation
        // Standard transformation matrix:
        // [ d ]   [  cos(theta)     cos(theta-2π/3)     cos(theta+2π/3) ] [ a ]
        // [ q ] = [ -sin(theta)    -sin(theta-2π/3)    -sin(theta+2π/3) ] [ b ]
        // [ 0 ]   [  1/2            1/2                  1/2             ] [ c ]
        
        double sqrt3_2 = Math.sqrt(3.0) / 2.0;
        
        if ("Stationary reference frame".equals(function)) {
            // Stationary reference frame (theta = 0)
            theta = 0;
        }
        
        // Calculate d component
        double d = (2.0/3.0) * (a * Math.cos(theta) + 
                               b * Math.cos(theta - 2.0*Math.PI/3.0) + 
                               c * Math.cos(theta + 2.0*Math.PI/3.0));
        
        // Calculate q component  
        double q = (2.0/3.0) * (-a * Math.sin(theta) - 
                               b * Math.sin(theta - 2.0*Math.PI/3.0) - 
                               c * Math.sin(theta + 2.0*Math.PI/3.0));
        
        // Calculate 0 component (zero sequence)
        double zero = (1.0/3.0) * (a + b + c);
        
        // Set output data
        com.ncslab.block.data.Data dOutputData = new com.ncslab.block.data.Data(1, 1);
        dOutputData.setInitValue(d);
        outputPortList.get(0).setData(dOutputData);
        
        com.ncslab.block.data.Data qOutputData = new com.ncslab.block.data.Data(1, 1);
        qOutputData.setInitValue(q);
        outputPortList.get(1).setData(qOutputData);
        
        com.ncslab.block.data.Data zeroOutputData = new com.ncslab.block.data.Data(1, 1);
        zeroOutputData.setInitValue(zero);
        outputPortList.get(2).setData(zeroOutputData);
    }
}
