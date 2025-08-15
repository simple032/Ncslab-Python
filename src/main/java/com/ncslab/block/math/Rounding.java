package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

public class Rounding extends Block {

    Parameter operator;
    String operatorString;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    
    
    /**
     * DTO-NATIVE Constructor - Creates Rounding block directly from BlockJson DTO
     */
    public Rounding(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Rounding block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Operator", "floor");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        
        // SIMULINK parameter names
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Rounding(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        outputPortList.add(new OutputPort(this, 1, true));

        inputPortList.add(new InputPort(this, 1));

        // Use name-based parameter access
        operator = getParameterByName("Operator");
        operatorString = operator.getInitString();

        if ("fix".equals(operatorString)) {
            operatorString = "trunc";
        }
    }
}