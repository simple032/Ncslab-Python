package com.ncslab.block.discrete;

import Jama.Matrix;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.DifferenceDto;

import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Difference block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - InitialCondition: Initial condition for u[n-1]
 * - SampleTime: Sample time for discrete operation
 *
 * Block Behavior:
 * y[n] = u[n] - u[n-1]
 *
 * The Difference block computes the difference between the current input and
 * the previous input. This is simpler than DiscreteDerivative (no division by Ts, no gain).
 * Supports scalar and matrix inputs.
 */
public class Difference extends DiscreteBlock {

    // === Internal State ===
    private Data currentValue;
    private Data previousValue;
    private final boolean feedthrough = false; // Difference has no feedthrough

    // === SIMULINK-Compatible Parameters ===
    private final Parameter initialCondition;
    private final Parameter sampleTimeParam;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
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

        // Output port defaults (Difference has no feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false); // Difference never has feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private Difference(Parameter initialCondition, Parameter sampleTimeParam,
                      String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(sampleTimeParam);

        // Assign parameters
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTimeParam, "Sample time parameter cannot be null");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Difference(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTimeParam = getParameterByName("SampleTime");

        // Set discrete sample time
        setSampleTime(sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();
    }

    /**
     * DTO-NATIVE Constructor - Creates Difference block directly from BlockDto DTO
     */
    public Difference(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Get parameters by name from the automatically populated parameterList
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTimeParam = getParameterByName("SampleTime");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Difference fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);

            Difference block = new Difference(initialCondition, sampleTime,
                                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, initialCondition, sampleTime);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Difference block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Difference create(String name, String path, double initialCondition, double sampleTime, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DifferenceDto dto = DifferenceDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .initialCondition(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Difference parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Difference(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be positive and finite");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 1, "InitialCondition", initialConditionValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");  // Default to inherited
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
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

    private static void setParameterBlockReference(Difference block, Parameter... parameters) {
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
        identity.put("blockType", "Difference");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void postConstructionInitialization() {
        // Create ports if they don't exist (DTO constructor may not have created them)
        if (inputPortList.isEmpty()) {
            InputPort inputPort = new InputPort(this, 1, "in1");
            inputPortList.add(inputPort);
        }

        if (outputPortList.isEmpty()) {
            OutputPort outputPort = new OutputPort(this, 1, feedthrough); // Use Block, int, boolean constructor
            outputPortList.add(outputPort);
        }

        // Set references to the ports
        input = inputPortList.get(0);  // First input port
        output = outputPortList.get(0); // First output port

        // Difference never has feedthrough, so set false
        output.setFeedThrough(feedthrough); // false
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Difference/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/discrete/Difference/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Difference/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Difference/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();

        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (sampleTimeParam.getDataType() != DataType.REAL || initialCondition.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }

        if (!isSampleTimeMultiple(sampleTimeParam.getDouble(), model.getConfig().getFixedStep())) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }

    // === SIMULINK-Compatible Lifecycle Methods ===

    /**
     * Validate block parameters before simulation starts.
     * SIMULINK equivalent: mdlCheckParameters
     *
     * Validates:
     * - Sample time is positive and finite
     * - Initial condition is valid numeric value
     * - Sample time is integer multiple of fixed step size
     */
    @Override
    public void calculateCheckParameters() {
        // Validate sample time
        double sampleTimeValue = sampleTimeParam.getDouble();
        if (sampleTimeValue <= 0.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            throw new IllegalArgumentException("Difference " + blockName + ": Sample time must be positive and finite, got: " + sampleTimeValue);
        }

        // Validate initial condition is numeric
        if (initialCondition.getData() == null) {
            throw new IllegalArgumentException("Difference " + blockName + ": Initial condition cannot be null");
        }
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        // Initialize previousValue with initial condition
        // currentValue will be set in first discrete update
        previousValue = new Data(initialCondition.getData().getInitValue());
        currentValue = new Data(initialCondition.getData().getInitValue());
    }

    /**
     * Perform one-time startup actions after initialization.
     * SIMULINK equivalent: mdlStart
     *
     * For Difference, no special startup actions are needed.
     */
    @Override
    public void calculateStart() {
        // No startup actions needed for Difference
        // Resources are already initialized in calculateInit()
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);

        // Get current input
        Data currentInput = in.getData();

        // Calculate difference: y[n] = u[n] - u[n-1]
        Data outputData;
        if (currentInput.getDataType() == DataType.MATRIX) {
            // Matrix input
            Matrix current = currentInput.getMatrix();
            Matrix previous = previousValue.getMatrix();
            Matrix difference = current.minus(previous);
            outputData = new Data(difference);
        } else {
            // Scalar input
            double current = currentInput.getInitValue();
            double previous = previousValue.getInitValue();
            double difference = current - previous;
            outputData = new Data(difference);
        }

        // Set output
        out.setData(outputData);
    }

    @Override
    public void calculateUpdate(double t) {
        // This method is called to update internal state before calculateOutput
        // For Difference, the update happens in calculateDiscreteUpdate
        // This method is typically empty for discrete blocks that do state updates in calculateDiscreteUpdate
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort input = inputPortList.get(0);
        // Store current input as previous value for next time step
        previousValue = input.getData();
    }

    /**
     * Graceful shutdown before termination.
     * SIMULINK equivalent: Part of mdlTerminate (pre-cleanup)
     *
     * For Difference, no pre-termination actions needed.
     */
    @Override
    public void calculateStop() {
        // No stop actions needed for Difference
        // No files, hardware, or external resources to flush
    }

    /**
     * Cleanup resources and finalize simulation.
     * SIMULINK equivalent: mdlTerminate
     *
     * For Difference, release internal state.
     *
     * @param t Final simulation time
     */
    @Override
    public void calculateTerminate(double t) {
        // Release internal state to help garbage collection
        currentValue = null;
        previousValue = null;
    }

    /**
     * Reset block to initial conditions (mid-simulation reset).
     * SIMULINK equivalent: Reset port functionality
     *
     * Resets the previous value to initial condition.
     *
     * @param t Time of reset
     */
    @Override
    public void calculateReset(double t) {
        // Reset previous value back to initial condition
        previousValue = new Data(initialCondition.getData().getInitValue());
        currentValue = new Data(initialCondition.getData().getInitValue());
    }

    /**
     * Generate discrete update code for C code generation.
     * This method handles the template-based code generation for discrete time updates.
     */
    public void generateDiscreteUpdateCodeCInside(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Use template for discrete update
        try {
            String codeStr = TemplateManager.renderTemplate("c/discrete/Difference/update.vm", context);
            code.addDiscreteUpdateCode(codeStr);
        } catch (Exception e) {
            // Fallback to manual discrete update code if no template exists
            String discreteUpdateCode = String.format("/* Discrete update for Difference block %d: %s */\n",
                                                    getBlockId(), getBlockName());
            discreteUpdateCode += String.format("Block%d_difference_previous = %s;\n",
                                               getBlockId(), input.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
            code.addDiscreteUpdateCode(discreteUpdateCode);
        }
    }
}
