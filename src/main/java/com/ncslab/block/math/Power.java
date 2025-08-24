package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.dto.block.specialized.math.PowerDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
    private Parameter powerMethod;
    private Parameter sampleTime;
    private Parameter outDataType;
    private Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("PowerMethod", "Element-wise(.^)");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1"); // Base
        inputNames.add("in2"); // Exponent
    }
    
    @Deprecated
    public Power(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        initializeBlock();
    }
    
    
    public Power(PowerDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Validate DTO
        dto.validate();
        
        // Extract parameters from PowerDto (relying on @Builder.Default for defaults)
        String powerMethodValue = dto.getPowerMethodValue();
        String sampleTimeValue = dto.getSampleTimeValue().toString();
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Initialize final parameters from DTO
        this.powerMethod = new Parameter(this, 1, "PowerMethod", powerMethodValue);
        this.sampleTime = new Parameter(this, 2, "SampleTime", sampleTimeValue);
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", outDataTypeValue);
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", saturateValue);
        
        initializePorts();
    }
    
    public static Power fromDto(PowerDto dto, NCSLabModel model) {
        return new Power(dto, model);
    }
    
    private void initializeBlock() {
        // Legacy JSONObject initialization
        this.powerMethod = getParameterByName("PowerMethod");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
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
            
            // Create block using existing constructor
            JSONObject blockIdentity = createBlockIdentity(blockName, blockPath, blockUUID);
            blockIdentity.getJSONObject("paramValues").put("PowerMethod", powerMethodParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("SampleTime", sampleTimeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("OutDataTypeStr", outDataTypeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("SaturateOnIntegerOverflow", saturateParam.getInitString());
            
            Power block = new Power(blockIdentity, model);
            
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
        
        // Create block using existing constructor
        JSONObject blockIdentity = createBlockIdentity(name, path, "null");
        blockIdentity.getJSONObject("paramValues").put("PowerMethod", powerMethodParam.getInitString());
        blockIdentity.getJSONObject("paramValues").put("SampleTime", sampleTimeParam.getInitString());
        blockIdentity.getJSONObject("paramValues").put("OutDataTypeStr", outDataTypeParam.getInitString());
        blockIdentity.getJSONObject("paramValues").put("SaturateOnIntegerOverflow", saturateParam.getInitString());
        
        Power block = new Power(blockIdentity, model);
        
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
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to satisfy base constructor
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

    // === DTO Helper Methods ===
    private static Parameter createParameterFromTyped(com.ncslab.dto.common.TypedParameter typedParam, int number, String name) {
        if (typedParam == null || typedParam.getValue() == null) {
            throw new IllegalArgumentException("TypedParameter " + name + " cannot be null");
        }
        
        String stringValue;
        if (typedParam.getValue() instanceof Boolean) {
            stringValue = ((Boolean) typedParam.getValue()) ? "on" : "off";
        } else {
            stringValue = typedParam.getValue().toString();
        }
        
        return new Parameter(null, number, name, stringValue);
    }
    
    // === Getter Methods ===
    public String getPowerMethod() {
        return powerMethod.getData().getInitString();
    }
}