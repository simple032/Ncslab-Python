package com.ncslab.block.source;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.source.ConstantDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * Constant block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * Generates a constant output signal with configurable scalar or matrix values.
 * The output remains constant throughout the simulation duration.
 * 
 * SIMULINK Parameters:
 * - Value: Constant value (scalar or matrix)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Constant extends SourceBlock {
    
    // === Constant-Specific SIMULINK Parameters ===
    /** Constant value parameter (scalar or matrix) */
    private final Parameter value;
    
    // === Static Parameter Definitions ===
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        // Constant-specific defaults
        Map<String, String> constantDefaults = new HashMap<>();
        constantDefaults.put("Value", "1");
        
        // Merge with common source block defaults
        // PARAMETER_DEFAULTS = mergeWithCommonDefaults(constantDefaults);
        PARAMETER_DEFAULTS = constantDefaults;
    }

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    static {        
        // Port names
        outputNames.add("out1");
        // No input ports for constant block
    }
    // === Private Constructor with Typed Parameters ===
    private Constant(Parameter value, Parameter sampleTime,
                    Parameter outDataType, Parameter saturateOnIntegerOverflow,
                    String blockName, String blockPath, String blockUUID, 
                    NCSLabModel model) {
        super("Constant", sampleTime, outDataType, saturateOnIntegerOverflow, blockName, blockPath, blockUUID, model);
        
        // Validate parameters
        validateParameters(value, sampleTime);
        
        // Assign Constant-specific parameters
        this.value = Objects.requireNonNull(value, "Value parameter cannot be null");
        
        // Add Constant-specific parameters to parameter list
        parameterList.add(value);        
        
        // Set port dimensions
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Constant(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model); // This calls parseParameterList() automatically
        
        // Get parameters by name from the automatically populated parameterList
        this.value = getParameterByName("Value");
        
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

        // Use centralized parameter management via getParameterByName
        this.value = getParameterByName("Value");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
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

            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Constant block = new Constant(value, sampleTime, outDataType, saturateParam,
                                         blockName, blockPath, blockUUID, model);
            
            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, value, sampleTime, outDataType, saturateParam);

            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Constant block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Constant create(String name, String path, double constantValue, NCSLabModel model) {
        return create(name, path, constantValue, 0.0, "Inherit: Same as parameter", false, model);
    }
    
    public static Constant create(String name, String path, double constantValue, double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Create parameters
        Parameter valueParam = new Parameter(null, 1, "Value", String.valueOf(constantValue));
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        Constant block = new Constant(valueParam, sampleTimeParam, outDataTypeParam, saturateParam,
                                     name, path, "null", model);
        
        // Set block reference in parameters
        setParameterBlockReference(block, valueParam, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter value, Parameter sampleTime) {
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
        context.put("blockId", this.getBlockId());
        context.put("blockName", this.getBlockName());
        context.put("value", value);

        String codeStr = TemplateManager.renderTemplate("c/source/Constant/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard context variables
        TemplateUtils.populateAllContext(context, this);
        
        // Add block-specific variables with proper C names
        context.put("value", value.getName());        

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

