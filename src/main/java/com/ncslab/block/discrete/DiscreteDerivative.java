package com.ncslab.block.discrete;

import Jama.Matrix;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.DiscreteDerivativeDto;

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
 * DiscreteDerivative block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Implements a discrete-time derivative using backward difference approximation.
 * The output is calculated as: y[n] = K * (u[n] - u[n-1]) / Ts
 * where K is the gain, Ts is the sample time, and u[n-1] is the previous input.
 *
 * SIMULINK Parameters:
 * - Gain: Gain multiplier (K in the formula)
 * - InitialCondition: Initial condition for u[n-1]
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Key Features:
 * - No feedthrough (output depends on previous input)
 * - Backward difference derivative: y[n] = K * (u[n] - u[n-1]) / Ts
 * - Supports scalar and matrix signals
 * - Configurable gain multiplier
 *
 * @author NCSLab
 * @version 1.0
 * @since 2025-12-03
 */
public class DiscreteDerivative extends DiscreteBlock {

    // === Internal State ===
    private Data previousInput;
    private final boolean feedthrough = false; // Discrete derivative has no feedthrough

    // === SIMULINK-Compatible Parameters ===
    private final Parameter gain;
    private final Parameter initialCondition;
    private final Parameter sampleTimeParam;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Gain", "1.0");
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

        // Output port defaults (discrete derivative has no feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false); // Discrete derivative never has feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private DiscreteDerivative(Parameter gain, Parameter initialCondition, Parameter sampleTimeParam,
                              Parameter outDataType, Parameter saturateOnIntegerOverflow,
                              String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(gain, sampleTimeParam);

        // Assign parameters
        this.gain = Objects.requireNonNull(gain, "Gain parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTimeParam, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DiscreteDerivative(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.gain = getParameterByName("Gain");
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Set discrete sample time
        setSampleTime(sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();
    }

    /**
     * DTO-NATIVE Constructor - Creates DiscreteDerivative block directly from BlockDto DTO
     */
    public DiscreteDerivative(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Get parameters by name from the automatically populated parameterList
        this.gain = getParameterByName("Gain");
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static DiscreteDerivative fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter gain = createGainFromJSON(paramValues, blockName);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            DiscreteDerivative block = new DiscreteDerivative(gain, initialCondition, sampleTime, outDataType, saturateParam,
                                                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, gain, initialCondition, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DiscreteDerivative block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static DiscreteDerivative create(String name, String path, double gain, double initialCondition, double sampleTime, NCSLabModel model) {
        return create(name, path, gain, initialCondition, sampleTime, "Inherit: Same as input", false, model);
    }

    /**
     * Create a DiscreteDerivative block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param gain Gain multiplier (K in the derivative formula)
     * @param initialCondition Initial condition for u[n-1]
     * @param sampleTime Sample time for discrete operation
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return DiscreteDerivative block instance
     */
    public static DiscreteDerivative create(String name, String path, double gain, double initialCondition, double sampleTime,
                                          String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DiscreteDerivativeDto dto = DiscreteDerivativeDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .gain(com.ncslab.dto.common.TypedParameter.of(gain))
            .initialCondition(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid DiscreteDerivative parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new DiscreteDerivative(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter gain, Parameter sampleTime) {
        double gainValue = gain.getDouble();
        if (Double.isNaN(gainValue) || Double.isInfinite(gainValue)) {
            throw new IllegalArgumentException("Gain must be finite");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            throw new IllegalArgumentException("Sample time must be positive and finite");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createGainFromJSON(JSONObject paramValues, String blockName) {
        String gainValue = paramValues.optString("Gain", "1.0");
        return new Parameter(null, 1, "Gain", gainValue);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 2, "InitialCondition", initialConditionValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");  // Default to inherited
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

    private static void setParameterBlockReference(DiscreteDerivative block, Parameter... parameters) {
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
        identity.put("blockType", "DiscreteDerivative");
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

        // Discrete derivative never has feedthrough, so set false
        output.setFeedThrough(feedthrough); // false
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/DiscreteDerivative/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/discrete/DiscreteDerivative/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/DiscreteDerivative/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/DiscreteDerivative/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();

        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (sampleTimeParam.getDataType() != DataType.REAL || gain.getDataType() != DataType.REAL || initialCondition.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameters of Block " + this.blockName + " must be real double scalars!\n \n");
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
     * - Gain is finite
     * - Initial condition is valid numeric value
     * - Sample time is integer multiple of fixed step size
     */
    @Override
    public void calculateCheckParameters() {
        // Validate sample time
        double sampleTimeValue = sampleTimeParam.getDouble();
        if (sampleTimeValue <= 0.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            throw new IllegalArgumentException("DiscreteDerivative " + blockName + ": Sample time must be positive and finite, got: " + sampleTimeValue);
        }

        // Validate gain
        double gainValue = gain.getDouble();
        if (Double.isNaN(gainValue) || Double.isInfinite(gainValue)) {
            throw new IllegalArgumentException("DiscreteDerivative " + blockName + ": Gain must be finite, got: " + gainValue);
        }

        // Validate initial condition is numeric
        if (initialCondition.getData() == null) {
            throw new IllegalArgumentException("DiscreteDerivative " + blockName + ": Initial condition cannot be null");
        }

        // Additional validation will be done in updateDimension() for sample time multiple check
    }

    @Override
    public void calculateInit() {
        // Initialize previous input with initial condition
        previousInput = new Data(initialCondition.getData().getInitValue());
    }

    /**
     * Perform one-time startup actions after initialization.
     * SIMULINK equivalent: mdlStart
     *
     * For DiscreteDerivative, no special startup actions are needed.
     */
    @Override
    public void calculateStart() {
        // No startup actions needed for DiscreteDerivative
        // Resources are already initialized in calculateInit()
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);

        Data currentInput = in.getData();
        double gainValue = gain.getDouble();
        double sampleTimeValue = sampleTimeParam.getDouble();

        // Calculate derivative: y[n] = K * (u[n] - u[n-1]) / Ts
        if (currentInput.getDataType() == DataType.REAL) {
            // Scalar input
            double current = currentInput.getInitValue();
            double previous = previousInput.getInitValue();
            double derivative = gainValue * (current - previous) / sampleTimeValue;
            out.setData(new Data(derivative));
        } else {
            // Matrix input - element-wise derivative
            Matrix currentMatrix = currentInput.getMatrix();
            Matrix previousMatrix = previousInput.getMatrix();

            int rows = currentMatrix.getRowDimension();
            int cols = currentMatrix.getColumnDimension();
            Matrix derivativeMatrix = new Matrix(rows, cols);

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    double current = currentMatrix.get(i, j);
                    double previous = previousMatrix.get(i, j);
                    double derivative = gainValue * (current - previous) / sampleTimeValue;
                    derivativeMatrix.set(i, j, derivative);
                }
            }

            out.setData(new Data(derivativeMatrix));
        }
    }

    @Override
    public void calculateUpdate(double t) {
        // This method is called to update internal state before calculateOutput
        // For discrete derivative, the update happens in calculateDiscreteUpdate
        // This method is typically empty for discrete blocks that do state updates in calculateDiscreteUpdate
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort input = inputPortList.get(0);
        // Store current input as previous input for next time step
        previousInput = input.getData();
    }

    /**
     * Graceful shutdown before termination.
     * SIMULINK equivalent: Part of mdlTerminate (pre-cleanup)
     *
     * For DiscreteDerivative, no pre-termination actions needed.
     */
    @Override
    public void calculateStop() {
        // No stop actions needed for DiscreteDerivative
        // No files, hardware, or external resources to flush
    }

    /**
     * Cleanup resources and finalize simulation.
     * SIMULINK equivalent: mdlTerminate
     *
     * For DiscreteDerivative, release internal state.
     *
     * @param t Final simulation time
     */
    @Override
    public void calculateTerminate(double t) {
        // Release internal state to help garbage collection
        previousInput = null;
    }

    /**
     * Reset block to initial conditions (mid-simulation reset).
     * SIMULINK equivalent: Reset port functionality
     *
     * Resets the previous input to initial condition.
     *
     * @param t Time of reset
     */
    @Override
    public void calculateReset(double t) {
        // Reset previous input back to initial condition
        previousInput = new Data(initialCondition.getData().getInitValue());
    }

    /**
     * Generate discrete update code for C code generation.
     * This method handles the template-based code generation for discrete time updates.
     */
    public void generateDiscreteUpdateCodeCInside(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Check if discrete update template exists, otherwise use inline code
        try {
            String codeStr = TemplateManager.renderTemplate("c/discrete/DiscreteDerivative/update.vm", context);
            code.addDiscreteUpdateCode(codeStr);
        } catch (Exception e) {
            // Fallback to manual discrete update code if no template exists
            String discreteUpdateCode = String.format("/* Discrete update for DiscreteDerivative block %d: %s */\n",
                                                    getBlockId(), getBlockName());
            discreteUpdateCode += String.format("Block%d_discrete_derivative_prev = %s;\n",
                                               getBlockId(), input.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
            code.addDiscreteUpdateCode(discreteUpdateCode);
        }
    }
}
