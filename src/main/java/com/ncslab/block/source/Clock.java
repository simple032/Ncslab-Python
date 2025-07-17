package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import com.ncslab.util.TemplateManager;

/**
 * Clock block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Clock extends com.ncslab.block.Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter sampleTime;
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
        PARAMETER_DEFAULTS.put("SampleTime", "0");  // Continuous
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
        // Port names
        outputNames.add("out1");
        // No input ports for clock block
    }
    
    // === Private Constructor with Typed Parameters ===
    private Clock(Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(sampleTime);
        
        // Assign parameters
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Clock(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create missing SIMULINK parameters with defaults
        this.sampleTime = new Parameter(this, 1, "SampleTime", "0"); // 0 for continuous clock
        this.outDataType = new Parameter(this, 2, "OutDataTypeStr", "double");
        this.saturateOnIntegerOverflow = new Parameter(this, 3, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Clock fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Clock block = new Clock(sampleTime, outDataType, saturateParam,
                                   blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Clock block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Clock create(String name, String path, NCSLabModel model) {
        return create(name, path, 0.0, "double", false, model);
    }
    
    public static Clock create(String name, String path, double sampleTime, String outDataType, 
                              boolean saturateOnOverflow, NCSLabModel model) {
        Parameter sampleTimeParam = new Parameter(null, 1, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 2, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 3, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Clock block = new Clock(sampleTimeParam, outDataTypeParam, saturateParam,
                               name, path, "null", model);
        
        setParameterBlockReference(block, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "double");
        return new Parameter(null, 2, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 3, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(Clock block, Parameter... parameters) {
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
        identity.put("blockType", "Clock");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        outputPortList.add(new OutputPort(this, 1, false));
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        context.put("block", this);
        context.put("outputVar", outputPortList.get(0).getOutputSignalC().getName());
        String outputCode = TemplateManager.renderTemplate("c/source/Clock/output.vm", context);
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException {
        // Clock block always outputs scalar time value
    }

    public void checkDimension() throws MatDimException {
        // No dimension checks needed for clock block
    }

    @Override
    public void calculateOutput(double t) {
        outputPortList.get(0).getOutputSignalC().setData(new Data(t));
    }

    @Override
    public void calculateInit() {
        outputPortList.get(0).getOutputSignalC().setData(new Data(0));
    }
}