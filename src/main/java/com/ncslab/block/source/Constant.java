package com.ncslab.block.source;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.source.ConstantDto;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Constant block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Value: Constant value (scalar or matrix)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - FramePeriod: Frame period for frame-based operations
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Constant extends SourceBlock {
    
    // === Constant-Specific SIMULINK Parameters ===
    private final Parameter value;
    private final Parameter framePeriod;
    
    // === Static Parameter Definitions ===
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        // Constant-specific defaults
        Map<String, String> constantDefaults = new HashMap<>();
        constantDefaults.put("Value", "1");
        constantDefaults.put("FramePeriod", "1");
        
        // Merge with common source block defaults
        PARAMETER_DEFAULTS = mergeWithCommonDefaults(constantDefaults);
    }

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    static {        
        // Port names
        outputNames.add("out1");
        // No input ports for constant block
    }
    // === Private Constructor with Typed Parameters ===
    private Constant(Parameter value, Parameter sampleTime, Parameter framePeriod,
                    Parameter outDataType, Parameter saturateOnIntegerOverflow,
                    String blockName, String blockPath, String blockUUID, 
                    NCSLabModel model) {
        super("Constant", sampleTime, outDataType, saturateOnIntegerOverflow, blockName, blockPath, blockUUID, model);
        
        // Validate parameters
        validateParameters(value, sampleTime, framePeriod);
        
        // Assign Constant-specific parameters
        this.value = Objects.requireNonNull(value, "Value parameter cannot be null");
        this.framePeriod = Objects.requireNonNull(framePeriod, "Frame period parameter cannot be null");
        
        // Add Constant-specific parameters to parameter list
        parameterList.add(value);
        parameterList.add(framePeriod);
        
        // Set port dimensions
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Constant(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model); // This calls parseParameterList() automatically
        
        // Get parameters by name from the automatically populated parameterList
        this.value = getParameterByName("Value");
        this.framePeriod = getParameterByName("FramePeriod");
        
        // Verify SourceBlock parameters are properly inherited
        if (getSampleTime() == null || getOutDataType() == null || getSaturateOnIntegerOverflow() == null) {
            throw new IllegalStateException("SourceBlock common parameters not properly initialized");
        }
        
        // Initialize ports
        initializePorts();
    }    
    
    /**
     * DTO-NATIVE Constructor - Creates Constant block directly from BlockDto DTO
     */
    public Constant(ConstantDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Extract parameters directly from DTO map - avoid JSONObject conversion
        Map<String, Object> paramValuesMap = blockDto.getParamValues();
        
        // Parse parameters directly or use defaults
        String valueStr = getParameterValue(paramValuesMap, "Value", "1");
        String framePeriodStr = getParameterValue(paramValuesMap, "FramePeriod", "1");
        
        // Initialize final parameters directly from DTO
        this.value = new Parameter(this, 1, "Value", valueStr);
        this.framePeriod = new Parameter(this, 2, "FramePeriod", framePeriodStr);

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    

    /**
     * Helper method to extract parameter value from Map - avoiding JSONObject conversion
     */
    private static String getParameterValue(Map<String, Object> paramValues, String paramName, String defaultValue) {
        if (paramValues == null) {
            return defaultValue;
        }
        Object value = paramValues.get(paramName);
        return value != null ? value.toString() : defaultValue;
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Constant fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            // Extract and validate JSON fields
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            // Create typed parameters from JSON with defaults
            Parameter value = createValueFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter framePeriod = createFramePeriodFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Constant block = new Constant(value, sampleTime, framePeriod, outDataType, saturateParam,
                                         blockName, blockPath, blockUUID, model);
            
            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, value, sampleTime, framePeriod, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Constant block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Constant create(String name, String path, double constantValue, NCSLabModel model) {
        return create(name, path, constantValue, 0.0, 1.0, "Inherit: Same as parameter", false, model);
    }
    
    public static Constant create(String name, String path, double constantValue,
                                 double sampleTime, double framePeriod, String outDataType, 
                                 boolean saturateOnOverflow, NCSLabModel model) {
        // Create parameters
        Parameter valueParam = new Parameter(null, 1, "Value", String.valueOf(constantValue));
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter framePeriodParam = new Parameter(null, 3, "FramePeriod", String.valueOf(framePeriod));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Constant block = new Constant(valueParam, sampleTimeParam, framePeriodParam, outDataTypeParam, saturateParam,
                                     name, path, "null", model);
        
        // Set block reference in parameters
        setParameterBlockReference(block, valueParam, sampleTimeParam, framePeriodParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter value, Parameter sampleTime, Parameter framePeriod) {
        // Validate value is finite
        double val = value.getDouble();
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            throw new IllegalArgumentException("Value must be finite");
        }
        
        // Validate sample time (0 for continuous, >0 for discrete, -1 for inherited)
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
        
        // Validate frame period (must be positive)
        double framePeriodValue = framePeriod.getDouble();
        if (framePeriodValue <= 0.0 || framePeriodValue == Double.NaN || framePeriodValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Frame period must be positive");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createValueFromJSON(JSONObject paramValues, String blockName) {
        String valueStr = paramValues.optString("Value", "1");
        return new Parameter(null, 1, "Value", valueStr);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createFramePeriodFromJSON(JSONObject paramValues, String blockName) {
        String framePeriodValue = paramValues.optString("FramePeriod", "1");
        return new Parameter(null, 3, "FramePeriod", framePeriodValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as parameter");
        return new Parameter(null, 4, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateValue);
    }
    
    // === Utility Methods ===
    private static String requireNonEmptyString(JSONObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing");
        }
        String value = json.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Field '" + key + "' cannot be empty");
        }
        return value;
    }
    
    private static void setParameterBlockReference(Constant block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null, but should work for basic operations
            }
        }
    }
    
    
    // === Port Dimension Setup ===
    private void initializePorts() {
        // Set port dimensions based on value parameter
        outputPortList.get(0).setHeight(value.getHeight());
        outputPortList.get(0).setWidth(value.getWidth());
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("value", value);

        String codeStr = TemplateManager.renderTemplate("m/source/Constant/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        context.put("block", this);
        context.put("value", value);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/source/Constant/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("value", value);

        String codeStr = TemplateManager.renderTemplate("c/source/Constant/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard context variables
        TemplateUtils.populateAllContext(context, this);
        
        // Add block-specific variables with proper C names
        context.put("value", value.getName());
        context.put("outputs", getOutputPortVariables());

        // Use the improved template (now updated directly)
        String codeStr = TemplateManager.renderTemplate("c/source/Constant/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateUpdateCodePLC(CodeStructC code) {
        // No update code needed for Constant
    }

    public void generateOutputCodePLC(CodeStructC code) {
        context.put("block", this);
        context.put("value", value);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/source/Constant/output_plc.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateInit(){
        outputPortList.get(0).getOutputSignalC().setData(value.getData());
    }
}

