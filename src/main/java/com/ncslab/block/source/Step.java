package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * Step block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Time: Step time when transition occurs
 * - InitialValue: Initial value before step time
 * - FinalValue: Final value after step time
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Step extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter time;
    @Getter
    private final Parameter initialValue;
    @Getter
    private final Parameter finalValue;
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
        PARAMETER_DEFAULTS.put("Time", "1");                    // Step time
        PARAMETER_DEFAULTS.put("InitialValue", "0");           // Before step
        PARAMETER_DEFAULTS.put("FinalValue", "1");             // After step
        PARAMETER_DEFAULTS.put("SampleTime", "0");             // Continuous
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        parameterNames.add("Time");
        parameterNames.add("InitialValue");
        parameterNames.add("FinalValue");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
        // Port names
        outputNames.add("out1");
        // No input ports for step block
    }
    
    // === Private Constructor with Typed Parameters ===
    private Step(Parameter time, Parameter initialValue, Parameter finalValue,
                Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(time, sampleTime);
        
        // Assign parameters
        this.time = Objects.requireNonNull(time, "Time parameter cannot be null");
        this.initialValue = Objects.requireNonNull(initialValue, "Initial value parameter cannot be null");
        this.finalValue = Objects.requireNonNull(finalValue, "Final value parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Step(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.time = getParameterByName("Time");
        this.initialValue = getParameterByName("InitialValue");
        this.finalValue = getParameterByName("FinalValue");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Initialize ports
        initializePorts();
    }
    // === Static Factory Method for JSON Deserialization ===
    public static Step fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter time = createTimeFromJSON(paramValues, blockName);
            Parameter initialValue = createInitialValueFromJSON(paramValues, blockName);
            Parameter finalValue = createFinalValueFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Step block = new Step(time, initialValue, finalValue, sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, time, initialValue, finalValue, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Step block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Step create(String name, String path, double stepTime, double initialValue, double finalValue, NCSLabModel model) {
        return create(name, path, stepTime, initialValue, finalValue, 0.0, "Inherit: Same as parameter", false, model);
    }
    
    public static Step create(String name, String path, double stepTime, double initialValue, double finalValue,
                             double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter time = new Parameter(null, 1, "Time", String.valueOf(stepTime));
        Parameter initialValueParam = new Parameter(null, 2, "InitialValue", String.valueOf(initialValue));
        Parameter finalValueParam = new Parameter(null, 3, "FinalValue", String.valueOf(finalValue));
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Step block = new Step(time, initialValueParam, finalValueParam, sampleTimeParam, outDataTypeParam, saturateParam,
                             name, path, "null", model);
        
        setParameterBlockReference(block, time, initialValueParam, finalValueParam, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter time, Parameter sampleTime) {
        double timeValue = time.getDouble();
        if (timeValue < 0.0 || Double.isNaN(timeValue) || Double.isInfinite(timeValue)) {
            throw new IllegalArgumentException("Step time must be non-negative and finite");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createTimeFromJSON(JSONObject paramValues, String blockName) {
        String timeValue = paramValues.optString("Time", "1");
        return new Parameter(null, 1, "Time", timeValue);
    }
    
    private static Parameter createInitialValueFromJSON(JSONObject paramValues, String blockName) {
        String initialValueStr = paramValues.optString("InitialValue", paramValues.optString("Before", "0"));
        return new Parameter(null, 2, "InitialValue", initialValueStr);
    }
    
    private static Parameter createFinalValueFromJSON(JSONObject paramValues, String blockName) {
        String finalValueStr = paramValues.optString("FinalValue", paramValues.optString("After", "1"));
        return new Parameter(null, 3, "FinalValue", finalValueStr);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 4, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as parameter");
        return new Parameter(null, 5, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(Step block, Parameter... parameters) {
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
        identity.put("blockType", "Step");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        outputPortList.add(new OutputPort(this, 1, false));
        outputPortList.get(0).setHeight(time.getHeight());
        outputPortList.get(0).setWidth(time.getWidth());
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("time", time);
        context.put("after", finalValue);
        context.put("before", initialValue);

        String codeStr = TemplateManager.renderTemplate("m/source/Step/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("time", time);
        context.put("after", finalValue);
        context.put("before", initialValue);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/source/Step/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("time", time);
        context.put("after", finalValue);
        context.put("before", initialValue);

        String codeStr = TemplateManager.renderTemplate("c/source/Step/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("time", time);
        context.put("after", finalValue);
        context.put("before", initialValue);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/source/Step/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        if (time.getWidth() != finalValue.getWidth()
                || time.getWidth() != initialValue.getWidth()
                || time.getHeight() != finalValue.getHeight()
                || time.getHeight() != initialValue.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }
    }
    
    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateInit() {
        outputPortList.get(0).getOutputSignalC().setData(initialValue.getData());
    }

    @Override
    public void calculateOutput(double t) {
        double timeValue = time.getData().getInitValue();
        Data output = t >= timeValue ? finalValue.getData() : initialValue.getData();
        outputPortList.get(0).getOutputSignalC().setData(output);
    }
}
