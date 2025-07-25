package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Power block implementing SIMULINK power functionality.
 * 
 * SIMULINK Parameters:
 * - PowerMethod: "Element-wise(.^)" or "Matrix(^)"
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Power extends Block {
    
    // === Parameters ===
    private final Parameter powerMethod;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("PowerMethod", "Element-wise(.^)");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1"); // Base
        inputNames.add("in2"); // Exponent
    }
    
    // === Constructor ===
    private Power(Parameter powerMethod, Parameter sampleTime,
                  Parameter outDataType, Parameter saturateOnIntegerOverflow,
                  String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        this.powerMethod = powerMethod;
        this.sampleTime = sampleTime;
        this.outDataType = outDataType;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
        
        // Add parameters to parameter list
        parameterList.add(powerMethod);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
        initializePorts();
    }
    
    private void initializePorts() {
        // Two input ports: base and exponent
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 1));
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 2));
        
        // One output port with feedthrough
        outputPortList.add(new com.ncslab.block.io.OutputPort(this, 1, true));
    }
    
    // === Static Factory Methods ===
    public static Power fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter powerMethodParam = new Parameter(null, 1, "PowerMethod", 
                paramValues.optString("PowerMethod", "Element-wise(.^)"));
            Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", 
                paramValues.optString("SampleTime", "-1"));
            Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", 
                paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
            Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", 
                paramValues.optString("SaturateOnIntegerOverflow", "off"));
            
            Power block = new Power(powerMethodParam, sampleTimeParam,
                                   outDataTypeParam, saturateParam,
                                   blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, powerMethodParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Power block: " + e.getMessage(), e);
        }
    }
    
    public static Power create(String name, String path, String powerMethod, NCSLabModel model) {
        return create(name, path, powerMethod, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Power create(String name, String path, String powerMethod,
                              double sampleTime, String outDataType, boolean saturateOnOverflow,
                              NCSLabModel model) {
        Parameter powerMethodParam = new Parameter(null, 1, "PowerMethod", powerMethod);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Power block = new Power(powerMethodParam, sampleTimeParam,
                               outDataTypeParam, saturateParam,
                               name, path, "null", model);
        
        setParameterBlockReference(block, powerMethodParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Power");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    private static void setParameterBlockReference(Power block, Parameter... parameters) {
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
    public String getPowerMethod() {
        return powerMethod.getData().getInitString();
    }
}