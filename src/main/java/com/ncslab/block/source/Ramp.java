package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

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
public class Ramp extends SourceBlock {

    // === Ramp-Specific SIMULINK Parameters ===
    private final Parameter slope;
    private final Parameter start;
    private final Parameter initialOutput;
    
    // === Static Parameter Definitions ===
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        // Ramp-specific defaults
        Map<String, String> rampDefaults = new HashMap<>();
        rampDefaults.put("Slope", "1");
        rampDefaults.put("Start", "0");
        rampDefaults.put("InitialOutput", "0");
        
        // Merge with common source block defaults
        PARAMETER_DEFAULTS = mergeWithCommonDefaults(rampDefaults);
    }

    public static final Vector<String> outputNames = new Vector<>();
    
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // Port names
        outputNames.add("out1");
        // No input ports for ramp block
    }
    // === Private Constructor with Typed Parameters ===
    private Ramp(Parameter slope, Parameter start, Parameter initialOutput,
                Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super("Ramp", sampleTime, outDataType, saturateOnIntegerOverflow, blockName, blockPath, blockUUID, model);
        
        // Validate parameters
        validateParameters(slope, start, sampleTime);
        
        // Assign Ramp-specific parameters
        this.slope = Objects.requireNonNull(slope, "Slope parameter cannot be null");
        this.start = Objects.requireNonNull(start, "Start parameter cannot be null");
        this.initialOutput = Objects.requireNonNull(initialOutput, "Initial output parameter cannot be null");
        
        // Add Ramp-specific parameters to parameter list
        parameterList.add(slope);
        parameterList.add(start);
        parameterList.add(initialOutput);
        
        // Set port dimensions
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Ramp(JSONObject blockJSON, NCSLabModel model) {
        // Extract parameters from JSON and initialize SourceBlock properly
        this(
            createSlopeFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createStartFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createInitialOutputFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createSampleTimeFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createOutDataTypeFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createSaturateFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            blockJSON.optString("blockName", "Ramp"),
            blockJSON.optString("blockPath", ""),
            blockJSON.optString("blockUUID", "null"),
            model
        );
        
        // Set parameter block references for legacy compatibility
        setParameterBlockReference(this, slope, start, initialOutput);
    }    /**
     * DTO-NATIVE Constructor - Creates Ramp block directly from BlockJson DTO
     */
    public Ramp(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.slope = new Parameter(this, 1, "Slope", "0");
        this.start = new Parameter(this, 2, "Start", "0");
        this.initialOutput = new Parameter(this, 3, "Initialoutput", "0");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
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
    
    
    // === Port Dimension Setup ===
    private void initializePorts() {
        // Set port dimensions based on slope parameter
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String codeStr = TemplateManager.renderTemplate("c/source/Ramp/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

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