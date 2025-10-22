package com.ncslab.block.discrete;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;
import Jama.Matrix;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.DelayDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * Delay block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Implements a discrete-time delay (z^-n) by buffering input samples and outputting
 * them after a specified number of time steps. Essential for discrete control systems.
 *
 * SIMULINK Parameters:
 * - DelayLength: Number of samples to delay (n in z^-n)
 * - InitialCondition: Initial condition for the delay buffer
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Delay extends DiscreteBlock {

    // === Internal State ===
    /** Delay buffer storing historical input samples */
    private List<Data> buffer;
    
    // === SIMULINK-Compatible Parameters ===
    /** Delay length parameter (number of samples) */
    private final Parameter delayLength;
    
    /** Initial condition parameter for delay buffer */
    private final Parameter initialCondition;
    
    /** Sample time parameter */
    private final Parameter sampleTimeParam;
    
    /** Output data type specification parameter */
    private final Parameter outDataType;
    
    /** Integer overflow handling parameter */
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("DelayLength", "1");
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (delay has no feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }
    // === Private Constructor with Typed Parameters ===
    private Delay(Parameter delayLength, Parameter initialCondition, Parameter sampleTime,
                 Parameter outDataType, Parameter saturateOnIntegerOverflow,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(delayLength, sampleTime);

        // Assign parameters
        this.delayLength = Objects.requireNonNull(delayLength, "Delay length parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.delayLength);
        parameterList.add(this.initialCondition);
        parameterList.add(this.sampleTimeParam);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);
        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        // Port initialization is now handled by the centralized parseInputOutputPorts() method in parent constructor
        
        // Call post-construction initialization to ensure ports are properly set up
        postConstructionInitialization();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Delay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.initialCondition = getParameterByName("InitialCondition");
        this.delayLength = getParameterByName("DelayLength");

        // Create missing SIMULINK parameters with defaults
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        // Set discrete sample time
        setSampleTime(sampleTimeParam);

        // Initialize ports
        // Port initialization is now handled by the centralized parseInputOutputPorts() method in parent constructor
        
        // Call post-construction initialization to ensure ports are properly set up
        postConstructionInitialization();
    }    /**
     * DTO-NATIVE Constructor - Creates Delay block directly from DelayDto DTO
     */
    public Delay(DelayDto delayDto, NCSLabModel model) {
        super(delayDto, model);

        // Initialize final parameters from DelayDto
        this.delayLength = getParameterByName("DelayLength");
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        // Port initialization is now handled by the centralized parseInputOutputPorts() method in parent constructor
        
        // Call post-construction initialization to ensure ports are properly set up
        postConstructionInitialization();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + delayDto.getBlockName());
    }

    
    // === Static Factory Method for JSON Deserialization ===
    public static Delay fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter delayLength = createDelayLengthFromJSON(paramValues, blockName);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Delay block = new Delay(delayLength, initialCondition, sampleTime, outDataType, saturateParam,
                                  blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, delayLength, initialCondition, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Delay block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Delay create(String name, String path, int delayLength, double initialCondition, double sampleTime, NCSLabModel model) {
        return create(name, path, delayLength, initialCondition, sampleTime, "Inherit: Same as input", false, model);
    }

    public static Delay create(String name, String path, int delayLength, double initialCondition, double sampleTime,
                              String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter delayLengthParam = new Parameter(null, 1, "DelayLength", String.valueOf(delayLength));
        Parameter initialConditionParam = new Parameter(null, 2, "InitialCondition", String.valueOf(initialCondition));
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        Delay block = new Delay(delayLengthParam, initialConditionParam, sampleTimeParam, outDataTypeParam, saturateParam,
                              name, path, "null", model);

        setParameterBlockReference(block, delayLengthParam, initialConditionParam, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter delayLength, Parameter sampleTime) {
        int delayLengthValue = (int) delayLength.getDouble();
        if (delayLengthValue <= 0) {
            throw new IllegalArgumentException("Delay length must be positive");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be positive and finite");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createDelayLengthFromJSON(JSONObject paramValues, String blockName) {
        String delayLengthValue = paramValues.optString("DelayLength", "1");
        return new Parameter(null, 1, "DelayLength", delayLengthValue);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 2, "InitialCondition", initialConditionValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
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

    private static void setParameterBlockReference(Delay block, Parameter... parameters) {
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
        identity.put("blockType", "Delay");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    // === Port Initialization ===
    protected void postConstructionInitialization() {
        // Assign ports from centrally-created defaults
        if (inputPortList != null && !inputPortList.isEmpty()) {
            input = inputPortList.get(0);
        }
        if (outputPortList != null && !outputPortList.isEmpty()) {
            output = outputPortList.get(0);
        }
        
        // For standalone delay blocks (created for testing), initialize ports if they don't exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            inputPortList = new ArrayList<>();
            input = new InputPort(this, 1);
            inputPortList.add(input);
        }
        if (outputPortList == null || outputPortList.isEmpty()) {
            outputPortList = new ArrayList<>();
            output = new OutputPort(this, 1, false); // No feedthrough for delay
            outputPortList.add(output);
        }
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("blockId", getBlockId());
        
        // Fail fast - validate required connections exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("Delay block requires input port for code generation");
        }
        
        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null) {
            throw new BlockCreationException("Delay block input port cannot be null");
        }
        
        if (inputPort.getLinkedLine() == null || inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            throw new BlockCreationException("Delay block requires valid input signal connection for code generation");
        }
        
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new BlockCreationException("Delay block requires valid output signal for code generation");
        }
        
        context.put("signal", signal);
        context.put("signalHeight", signal.getHeight());
        context.put("signalWidth", signal.getWidth());
        
        // Fail fast - validate delay length parameter exists
        if (delayLength == null || delayLength.getData() == null) {
            throw new BlockCreationException("Delay block requires valid delay length parameter");
        }
        context.put("delayLength", delayLength.getData().getIntValue()); 
        
        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("sampleTime", sampleTimeParam);
        context.put("initialCondition", initialCondition);
        context.put("delayLength", delayLength);

        String codeStr = TemplateManager.renderTemplate("m/discrete/Delay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("sampleTime", sampleTimeParam);
        context.put("initialCondition", initialCondition);
        context.put("delayLength", delayLength);
        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Fail fast - validate required ports exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new BlockCreationException("Delay block requires output port for code generation");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("Delay block requires input port for code generation");
        }
        
        context.put("outputPortList", outputPortList);
        context.put("inputPortList", inputPortList);
        
        // Fail fast - validate required parameters exist
        if (sampleTimeParam == null) {
            throw new BlockCreationException("Delay block requires sample time parameter");
        }
        if (initialCondition == null) {
            throw new BlockCreationException("Delay block requires initial condition parameter");
        }
        if (delayLength == null || delayLength.getData() == null) {
            throw new BlockCreationException("Delay block requires valid delay length parameter");
        }
        
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add block-specific context
        context.put("delayLength", delayLength.getData().getIntValue());
        context.put("sampleTime", sampleTimeParam);
        // sampleTimeName is already set by TemplateUtils.populateAllContext() with correct prefix

        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateOutput(double t) {
        // Discrete delay: y[k] = u[k-n] where n is the delay length
        
        // Fail fast - validate critical components exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("Delay block cannot calculate output: no output ports configured");
        }
        
        OutputPort output = outputPortList.get(0);
        if (output == null) {
            throw new IllegalStateException("Delay block cannot calculate output: output port is null");
        }
        
        if (delayLength == null || delayLength.getData() == null) {
            throw new IllegalStateException("Delay block cannot calculate output: delay length parameter is missing");
        }
        
        if (initialCondition == null) {
            throw new IllegalStateException("Delay block cannot calculate output: initial condition parameter is missing");
        }
        
        if (buffer == null || buffer.isEmpty()) {
            // If buffer not initialized or empty, output initial condition
            double ic = initialCondition.getDouble();
            output.setData(new Data(ic));
            return;
        }
        
        int delayLengthValue = (int) delayLength.getData().getInitValue();
        
        // Output the delayed sample from buffer
        // Buffer stores samples in chronological order: [oldest, ..., newest]
        // For delay of n, we want the sample from n steps ago
        if (buffer.size() >= delayLengthValue) {
            // Get the delayed sample (from n steps ago)
            int delayedIndex = buffer.size() - delayLengthValue;
            if (delayedIndex >= 0 && delayedIndex < buffer.size()) {
                Data delayedData = buffer.get(delayedIndex);
                output.setData(delayedData);
            } else {
                // Index out of bounds, use initial condition
                double ic = initialCondition.getDouble();
                output.setData(new Data(ic));
            }
        } else {
            // Not enough samples in buffer yet, use initial condition
            double ic = initialCondition.getDouble();
            output.setData(new Data(ic));
        }
    }

    @Override
    public void calculateInit() {
        // Initialize delay block
        
        // Fail fast - validate critical components exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("Delay block cannot initialize: no output ports configured");
        }
        
        OutputPort output = outputPortList.get(0);
        if (output == null) {
            throw new IllegalStateException("Delay block cannot initialize: output port is null");
        }
        
        if (delayLength == null || delayLength.getData() == null) {
            throw new IllegalStateException("Delay block cannot initialize: delay length parameter is missing");
        }
        
        if (initialCondition == null) {
            throw new IllegalStateException("Delay block cannot initialize: initial condition parameter is missing");
        }
        
        int delayLengthValue = (int) delayLength.getData().getInitValue();
        double ic = initialCondition.getDouble();
        
        // Initialize buffer with initial condition values
        buffer = new ArrayList<>();
        
        // Pre-fill buffer with initial conditions for the delay length
        for (int i = 0; i < delayLengthValue; i++) {
            buffer.add(new Data(ic));
        }
        
        // Initial output is the initial condition
        output.setData(new Data(ic));
    }
    
    @Override
    public void calculateUpdate(double t) {
        // Update delay buffer with new input sample
        
        // Fail fast - validate critical components exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new IllegalStateException("Delay block cannot update: no input ports configured");
        }
        
        if (buffer == null) {
            throw new IllegalStateException("Delay block cannot update: buffer not initialized (call calculateInit() first)");
        }
        
        InputPort input = inputPortList.get(0);
        if (input == null) {
            throw new IllegalStateException("Delay block cannot update: input port is null");
        }
        
        if (input.getData() == null) {
            throw new IllegalStateException("Delay block cannot update: input data is null");
        }
        
        if (delayLength == null || delayLength.getData() == null) {
            throw new IllegalStateException("Delay block cannot update: delay length parameter is missing");
        }
        
        Data inputData = input.getData();
        int delayLengthValue = (int) delayLength.getData().getInitValue();
        
        // Add new sample to buffer
        buffer.add(inputData);
        
        // Keep buffer size manageable - only keep what we need for delay
        // We need delayLength + 1 samples to output the properly delayed value
        int maxBufferSize = delayLengthValue + 10; // Keep a few extra for stability
        while (buffer.size() > maxBufferSize) {
            buffer.remove(0); // Remove oldest sample
        }
    }
}
