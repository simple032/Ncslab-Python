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
import com.ncslab.dto.core.BlockDto;

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
     * DTO-NATIVE Constructor - Creates Rounding block directly from RoundingDto DTO
     */
    public Rounding(com.ncslab.dto.block.specialized.math.RoundingDto roundingDto, NCSLabModel model) {
        super(createRoundingJSON(roundingDto), model);
        
        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));

        // Get operator parameter and set operator string
        operator = getParameterByName("Operator");
        operatorString = operator.getInitString();

        if ("fix".equals(operatorString)) {
            operatorString = "trunc";
        }
        
        System.out.println("DTO-NATIVE: Rounding block created successfully from RoundingDto - " + roundingDto.getBlockName());
    }

    /**
     * Legacy DTO Constructor - Creates Rounding block from generic BlockDto (fallback)
     */
    public Rounding(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-GENERIC: Rounding block created from generic BlockDto - " + blockDto.getBlockName());
    }

    // === DTO Helper Methods ===
    private static JSONObject createRoundingJSON(com.ncslab.dto.block.specialized.math.RoundingDto roundingDto) {
        JSONObject json = new JSONObject();
        json.put("blockType", "Rounding");
        json.put("blockName", roundingDto.getBlockName());
        json.put("blockPath", roundingDto.getBlockPath());
        json.put("blockUUID", roundingDto.getBlockUUID() != null ? roundingDto.getBlockUUID() : "null");
        
        JSONObject paramValues = new JSONObject();
        paramValues.put("Operator", roundingDto.getOperatorValue());
        paramValues.put("SampleTime", roundingDto.getSampleTimeValue());
        paramValues.put("OutDataTypeStr", roundingDto.getOutDataTypeString());
        json.put("paramValues", paramValues);
        
        return json;
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