package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.util.TemplateManager;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * Ramp block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Slope: Rate of change of the ramp signal
 * - Start: Time when ramp starts  
 * - InitialOutput: Initial output value before ramp starts
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Ramp extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter slope;
    @Getter
    private final Parameter start;
    @Getter
    private final Parameter initialOutput;
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
        PARAMETER_DEFAULTS.put("Slope", "1");
        PARAMETER_DEFAULTS.put("Start", "0");
        PARAMETER_DEFAULTS.put("InitialOutput", "0");
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
        parameterNames.add("Slope");
        parameterNames.add("Start");
        parameterNames.add("InitialOutput");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
        // Port names
        outputNames.add("out1");
        // No input ports for ramp block
    }
    // === Private Constructor with Typed Parameters ===
    private Ramp(Parameter slope, Parameter start, Parameter initialOutput,
                Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(slope, start, sampleTime);
        
        // Assign parameters
        this.slope = Objects.requireNonNull(slope, "Slope parameter cannot be null");
        this.start = Objects.requireNonNull(start, "Start parameter cannot be null");
        this.initialOutput = Objects.requireNonNull(initialOutput, "Initial output parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Ramp(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility
        this.slope = new Parameter(this, 1, "Slope", paramValues.getString("slope"));
        this.start = new Parameter(this, 2, "Start", paramValues.getString("start"));
        this.initialOutput = new Parameter(this, 3, "InitialOutput", paramValues.getString("X0"));
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = new Parameter(this, 4, "SampleTime", "0"); // 0 for continuous ramp
        this.outDataType = new Parameter(this, 5, "OutDataTypeStr", "Inherit: Same as parameter");
        this.saturateOnIntegerOverflow = new Parameter(this, 6, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Ramp fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter slope = createSlopeFromJSON(paramValues, blockName);
            Parameter start = createStartFromJSON(paramValues, blockName);
            Parameter initialOutput = createInitialOutputFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Ramp block = new Ramp(slope, start, initialOutput, sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, slope, start, initialOutput, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Ramp block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Ramp create(String name, String path, double slope, double start, double initialOutput, NCSLabModel model) {
        return create(name, path, slope, start, initialOutput, 0.0, "Inherit: Same as parameter", false, model);
    }
    
    public static Ramp create(String name, String path, double slope, double start, double initialOutput,
                             double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter slopeParam = new Parameter(null, 1, "Slope", String.valueOf(slope));
        Parameter startParam = new Parameter(null, 2, "Start", String.valueOf(start));
        Parameter initialOutputParam = new Parameter(null, 3, "InitialOutput", String.valueOf(initialOutput));
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Ramp block = new Ramp(slopeParam, startParam, initialOutputParam, sampleTimeParam, outDataTypeParam, saturateParam,
                             name, path, "null", model);
        
        setParameterBlockReference(block, slopeParam, startParam, initialOutputParam, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter slope, Parameter start, Parameter sampleTime) {
        double slopeValue = slope.getDouble();
        if (Double.isNaN(slopeValue) || Double.isInfinite(slopeValue)) {
            throw new IllegalArgumentException("Slope must be finite");
        }
        
        double startValue = start.getDouble();
        if (Double.isNaN(startValue) || Double.isInfinite(startValue)) {
            throw new IllegalArgumentException("Start time must be finite");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSlopeFromJSON(JSONObject paramValues, String blockName) {
        String slopeValue = paramValues.optString("slope", "1");
        return new Parameter(null, 1, "Slope", slopeValue);
    }
    
    private static Parameter createStartFromJSON(JSONObject paramValues, String blockName) {
        String startValue = paramValues.optString("start", "0");
        return new Parameter(null, 2, "Start", startValue);
    }
    
    private static Parameter createInitialOutputFromJSON(JSONObject paramValues, String blockName) {
        String initialOutputValue = paramValues.optString("X0", "0");
        return new Parameter(null, 3, "InitialOutput", initialOutputValue);
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
    
    private static void setParameterBlockReference(Ramp block, Parameter... parameters) {
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
        identity.put("blockType", "Ramp");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        outputPortList.add(new OutputPort(this, 1, false));
        outputPortList.get(0).setHeight(slope.getHeight());
        outputPortList.get(0).setWidth(slope.getWidth());
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("slope", slope);
        context.put("start", start);
        context.put("initialOutput", initialOutput);
        
        String codeStr = TemplateManager.renderTemplate("m/source/Ramp/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("outputs", getOutputPortVariables());
        context.put("slope", slope);
        context.put("start", start);
        context.put("initial_output", initialOutput);

        String codeStr = TemplateManager.renderTemplate("m/source/Ramp/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("slope", slope);
        context.put("start", start);
        context.put("initialOutput", initialOutput);
        
        String codeStr = TemplateManager.renderTemplate("c/source/Ramp/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputSignal signal = outputPortList.get(0).getOutputSignalC();
        context.put("block", this);
        context.put("outputs", getOutputPortVariables());
        context.put("signal", signal);
        context.put("slope", slope);
        context.put("slopeValue", slope.getDataString());
        context.put("start", start.getInitString());
        context.put("slopeHeightIndex", slope.getHeight() - 1);
        context.put("slopeWidthIndex", slope.getWidth() - 1);
        context.put("initial_output", initialOutput.getInitString());

        String codeStr = TemplateManager.renderTemplate("c/source/Ramp/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        if (slope.getWidth() != initialOutput.getWidth()
                || slope.getWidth() != start.getWidth()
                || slope.getHeight() != start.getHeight()
                || slope.getHeight() != initialOutput.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateOutput(double t) {
        double slopeValue = slope.getData().getInitValue();
        double startValue = start.getData().getInitValue();
        double initialOutputValue = initialOutput.getData().getInitValue();

        double output = t >= startValue ? slopeValue * (t - startValue) + initialOutputValue : initialOutputValue;
        outputPortList.get(0).getOutputSignalC().setData(new Data(output));
    }

    @Override
    public void calculateInit() {
        double initialOutputValue = initialOutput.getData().getInitValue();
        outputPortList.get(0).getOutputSignalC().setData(new Data(initialOutputValue));
    }
}