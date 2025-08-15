package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * MinMax block implementing SIMULINK MinMax functionality.
 * 
 * SIMULINK Parameters:
 * - Function: "min" or "max"
 * - NumInputs: Number of inputs
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class MinMax extends Block {
    
    // === Parameters ===
    private final Parameter function;
    private final Parameter numInputs;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("Function", "min");
        PARAMETER_DEFAULTS.put("NumInputs", "2");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2"); // Default 2 inputs
    }
    
    // === Constructor ===
    private MinMax(Parameter function, Parameter numInputs, Parameter sampleTime,
                   Parameter outDataType, Parameter saturateOnIntegerOverflow,
                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        this.function = function;
        this.numInputs = numInputs;
        this.sampleTime = sampleTime;
        this.outDataType = outDataType;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
        
        // Add parameters to parameter list
        parameterList.add(function);
        parameterList.add(numInputs);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
        initializePorts();
    }
    
    private void initializePorts() {
        // Create input ports based on NumInputs parameter
        int numInputsValue = (int) numInputs.getData().getInitValue();
        for (int i = 1; i <= numInputsValue; i++) {
            inputPortList.add(new com.ncslab.block.io.InputPort(this, i));
        }
        
        // Create single output port
        outputPortList.add(new com.ncslab.block.io.OutputPort(this, 1, true)); // Has feedthrough
    }
    
    // === Static Factory Methods ===
    public static MinMax fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter functionParam = new Parameter(null, 1, "Function", 
                paramValues.optString("Function", "min"));
            Parameter numInputsParam = new Parameter(null, 2, "NumInputs", 
                paramValues.optString("NumInputs", "2"));
            Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", 
                paramValues.optString("SampleTime", "-1"));
            Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", 
                paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
            Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", 
                paramValues.optString("SaturateOnIntegerOverflow", "off"));
            
            MinMax block = new MinMax(functionParam, numInputsParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam,
                                     blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, functionParam, numInputsParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create MinMax block: " + e.getMessage(), e);
        }
    }
    
    public static MinMax create(String name, String path, String function, int numInputs, NCSLabModel model) {
        return create(name, path, function, numInputs, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static MinMax create(String name, String path, String function, int numInputs,
                               double sampleTime, String outDataType, boolean saturateOnOverflow,
                               NCSLabModel model) {
        Parameter functionParam = new Parameter(null, 1, "Function", function);
        Parameter numInputsParam = new Parameter(null, 2, "NumInputs", String.valueOf(numInputs));
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        MinMax block = new MinMax(functionParam, numInputsParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam,
                                 name, path, "null", model);
        
        setParameterBlockReference(block, functionParam, numInputsParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "MinMax");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    private static void setParameterBlockReference(MinMax block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter will work for basic operations
            }
        }
    }
    
    // === Getter Methods ===
    public String getFunction() {
        return function.getData().getInitString();
    }
    
    public int getNumInputs() {
        return (int) numInputs.getData().getInitValue();
    }
}