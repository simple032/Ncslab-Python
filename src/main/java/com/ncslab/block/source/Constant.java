package com.ncslab.block.source;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

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
public class Constant extends Block {
    
    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter value;
    @Getter
    private final Parameter sampleTime;
    @Getter
    private final Parameter framePeriod;
    @Getter
    private final Parameter outDataType;
    @Getter
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Parameter Definitions ===
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Value", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "0");  // 0 for continuous constant
        PARAMETER_DEFAULTS.put("FramePeriod", "1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as parameter");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        parameterNames.add("Value");
        parameterNames.add("SampleTime");
        parameterNames.add("FramePeriod");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
        // Port names
        outputNames.add("out1");
        // No input ports for constant block
    }
    // === Private Constructor with Typed Parameters ===
    private Constant(Parameter value, Parameter sampleTime, Parameter framePeriod,
                    Parameter outDataType, Parameter saturateOnIntegerOverflow,
                    String blockName, String blockPath, String blockUUID, 
                    NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(value, sampleTime, framePeriod);
        
        // Assign parameters
        this.value = Objects.requireNonNull(value, "Value parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.framePeriod = Objects.requireNonNull(framePeriod, "Frame period parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Constant(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model); // This calls parseParameterList() automatically
        
        // Get parameters by name from the automatically populated parameterList
        this.value = getParameterByName("Value");
        this.sampleTime = getParameterByName("SampleTime");
        this.framePeriod = getParameterByName("FramePeriod");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Initialize ports
        initializePorts();
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
    public static Constant create(String name, String path, String constantValue, NCSLabModel model) {
        return create(name, path, constantValue, 0.0, 1.0, "Inherit: Same as parameter", false, model);
    }
    
    public static Constant create(String name, String path, String constantValue,
                                 double sampleTime, double framePeriod, String outDataType, 
                                 boolean saturateOnOverflow, NCSLabModel model) {
        // Create parameters
        Parameter value = new Parameter(null, 1, "Value", constantValue);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter framePeriodParam = new Parameter(null, 3, "FramePeriod", String.valueOf(framePeriod));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Constant block = new Constant(value, sampleTimeParam, framePeriodParam, outDataTypeParam, saturateParam,
                                     name, path, "null", model);
        
        // Set block reference in parameters
        setParameterBlockReference(block, value, sampleTimeParam, framePeriodParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter value, Parameter sampleTime, Parameter framePeriod) {
        // Validate value is not null/empty
        String valueStr = value.getInitString();
        if (valueStr == null || valueStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Constant value cannot be empty");
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
    
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Constant");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        // Create output port (constant blocks have no input)
        outputPortList.add(new OutputPort(this, 1, true));
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
        // Context is automatically populated with standard variables by TemplateUtils
        // Just add block-specific variables
        context.put("value", value);
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

