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
 * Reciprocal block implementing SIMULINK reciprocal functionality.
 * 
 * SIMULINK Parameters:
 * - EnableSaturation: Enable output saturation ("on" or "off")
 * - UpperLimit: Upper saturation limit
 * - LowerLimit: Lower saturation limit
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Reciprocal extends Block {
    
    // === Parameters ===
    private final Parameter enableSaturation;
    private final Parameter upperLimit;
    private final Parameter lowerLimit;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("EnableSaturation", "off");
        PARAMETER_DEFAULTS.put("UpperLimit", "inf");
        PARAMETER_DEFAULTS.put("LowerLimit", "-inf");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }
    
    // === Constructor ===
    private Reciprocal(Parameter enableSaturation, Parameter upperLimit, Parameter lowerLimit,
                       Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                       String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        this.enableSaturation = enableSaturation;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.sampleTime = sampleTime;
        this.outDataType = outDataType;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
        
        // Add parameters to parameter list
        parameterList.add(enableSaturation);
        parameterList.add(upperLimit);
        parameterList.add(lowerLimit);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
        initializePorts();
    }
    
    private void initializePorts() {
        // One input port
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 1));
        
        // One output port with feedthrough
        outputPortList.add(new com.ncslab.block.io.OutputPort(this, 1, true));
    }
    
    // === Static Factory Methods ===
    public static Reciprocal fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter enableSaturationParam = new Parameter(null, 1, "EnableSaturation", 
                paramValues.optString("EnableSaturation", "off"));
            Parameter upperLimitParam = new Parameter(null, 2, "UpperLimit", 
                paramValues.optString("UpperLimit", "inf"));
            Parameter lowerLimitParam = new Parameter(null, 3, "LowerLimit", 
                paramValues.optString("LowerLimit", "-inf"));
            Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", 
                paramValues.optString("SampleTime", "-1"));
            Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", 
                paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
            Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", 
                paramValues.optString("SaturateOnIntegerOverflow", "off"));
            
            Reciprocal block = new Reciprocal(enableSaturationParam, upperLimitParam, lowerLimitParam,
                                             sampleTimeParam, outDataTypeParam, saturateParam,
                                             blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, enableSaturationParam, upperLimitParam, lowerLimitParam,
                                     sampleTimeParam, outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Reciprocal block: " + e.getMessage(), e);
        }
    }
    
    public static Reciprocal create(String name, String path, boolean enableSaturation, NCSLabModel model) {
        return create(name, path, enableSaturation, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 
                     -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Reciprocal create(String name, String path, boolean enableSaturation, 
                                   double upperLimit, double lowerLimit,
                                   double sampleTime, String outDataType, boolean saturateOnOverflow,
                                   NCSLabModel model) {
        Parameter enableSaturationParam = new Parameter(null, 1, "EnableSaturation", enableSaturation ? "on" : "off");
        Parameter upperLimitParam = new Parameter(null, 2, "UpperLimit", 
            upperLimit == Double.POSITIVE_INFINITY ? "inf" : String.valueOf(upperLimit));
        Parameter lowerLimitParam = new Parameter(null, 3, "LowerLimit", 
            lowerLimit == Double.NEGATIVE_INFINITY ? "-inf" : String.valueOf(lowerLimit));
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Reciprocal block = new Reciprocal(enableSaturationParam, upperLimitParam, lowerLimitParam,
                                         sampleTimeParam, outDataTypeParam, saturateParam,
                                         name, path, "null", model);
        
        setParameterBlockReference(block, enableSaturationParam, upperLimitParam, lowerLimitParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    /**
     * Create Reciprocal block from DTO (modern approach)
     */
    public static Reciprocal fromDTO(com.ncslab.dto.block.specialized.math.ReciprocalDto dto, NCSLabModel model) {
        try {
            // Extract typed parameters with defaults
            String enableSaturationValue = dto.getEnableSaturationValue() ? "on" : "off";
            
            String upperLimitValue;
            Double upperDouble = dto.getUpperLimitValue();
            if (upperDouble == null || Double.isInfinite(upperDouble) && upperDouble > 0) {
                upperLimitValue = "inf";
            } else {
                upperLimitValue = String.valueOf(upperDouble);
            }
            
            String lowerLimitValue;
            Double lowerDouble = dto.getLowerLimitValue();
            if (lowerDouble == null || Double.isInfinite(lowerDouble) && lowerDouble < 0) {
                lowerLimitValue = "-inf";
            } else {
                lowerLimitValue = String.valueOf(lowerDouble);
            }
            
            String sampleTimeValue = dto.getSampleTime() != null ? String.valueOf(dto.getSampleTime()) : "-1";
            String outDataTypeValue = dto.getOutDataTypeStrValue();
            String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
            
            // Create parameter objects
            Parameter enableSaturationParam = new Parameter(null, 1, "EnableSaturation", enableSaturationValue);
            Parameter upperLimitParam = new Parameter(null, 2, "UpperLimit", upperLimitValue);
            Parameter lowerLimitParam = new Parameter(null, 3, "LowerLimit", lowerLimitValue);
            Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", sampleTimeValue);
            Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataTypeValue);
            Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateValue);
            
            // Create block
            Reciprocal block = new Reciprocal(enableSaturationParam, upperLimitParam, lowerLimitParam,
                                             sampleTimeParam, outDataTypeParam, saturateParam,
                                             dto.getBlockName(), dto.getBlockPath(), dto.getBlockUUID(), model);
            
            setParameterBlockReference(block, enableSaturationParam, upperLimitParam, lowerLimitParam,
                                     sampleTimeParam, outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Reciprocal block from DTO: " + e.getMessage(), e);
        }
    }
    
    /**
     * Constructor for DTO-based creation (dual constructor pattern)
     */
    public Reciprocal(com.ncslab.dto.block.specialized.math.ReciprocalDto dto, NCSLabModel model) {
        super(createBlockIdentity(dto.getBlockName(), dto.getBlockPath(), dto.getBlockUUID()), model);
        
        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }
        
        // Initialize from DTO parameters using TypedParameter value extraction
        this.enableSaturation = createParameterFromDto("EnableSaturation", dto.getEnableSaturationValue(), 1);
        this.upperLimit = createParameterFromDto("UpperLimit", dto.getUpperLimitValue(), 2);
        this.lowerLimit = createParameterFromDto("LowerLimit", dto.getLowerLimitValue(), 3);
        this.sampleTime = createParameterFromDto("SampleTime", dto.getSampleTime(), 4);
        this.outDataType = createParameterFromDto("OutDataTypeStr", dto.getOutDataTypeStrValue(), 5);
        this.saturateOnIntegerOverflow = createParameterFromDto("SaturateOnIntegerOverflow", dto.getSaturateOnIntegerOverflowValue(), 6);
        
        // Add parameters to parameter list
        parameterList.add(enableSaturation);
        parameterList.add(upperLimit);
        parameterList.add(lowerLimit);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
        initializePorts();
        
        setParameterBlockReference(this, enableSaturation, upperLimit, lowerLimit,
                                 sampleTime, outDataType, saturateOnIntegerOverflow);
    }
    
    /**
     * Static factory method for DTO creation
     */
    public static Reciprocal fromDto(com.ncslab.dto.block.specialized.math.ReciprocalDto dto, NCSLabModel model) {
        return new Reciprocal(dto, model);
    }
    
    /**
     * Helper method to create Parameter from DTO values
     */
    private static Parameter createParameterFromDto(String paramName, Object value, int index) {
        String stringValue;
        if (value instanceof Boolean) {
            stringValue = ((Boolean) value) ? "on" : "off";
        } else if (value instanceof Double) {
            Double doubleValue = (Double) value;
            if (Double.isInfinite(doubleValue)) {
                // Use Java's infinity representation that Data class can parse
                stringValue = doubleValue > 0 ? "Infinity" : "-Infinity";
            } else {
                stringValue = String.valueOf(doubleValue);
            }
        } else {
            stringValue = String.valueOf(value);
        }
        return new Parameter(null, index, paramName, stringValue);
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Reciprocal");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }
    
    private static void setParameterBlockReference(Reciprocal block, Parameter... parameters) {
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
    public boolean isEnableSaturation() {
        return "on".equals(enableSaturation.getData().getInitString());
    }
    
    public double getUpperLimit() {
        return upperLimit.getData().getInitValue();
    }
    
    public double getLowerLimit() {
        return lowerLimit.getData().getInitValue();
    }
}