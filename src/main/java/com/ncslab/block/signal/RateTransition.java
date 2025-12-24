package com.ncslab.block.signal;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.signal.RateTransitionDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * Rate Transition block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Handles sample rate transitions between blocks with different rates, providing
 * buffering and interpolation as needed. Supports fast-to-slow (downsampling),
 * slow-to-fast (upsampling), and same-rate (pass-through) transitions.
 *
 * SIMULINK Parameters:
 * - InputSampleTime: Input block sample rate (-1 for inherited)
 * - OutputSampleTime: Output block sample rate (-1 for inherited)
 * - InitialCondition: Initial output value
 * - OutDataTypeStr: Output data type specification
 * - EnsureDataIntegrity: Ensure deterministic output for code generation
 * - X0: Initial condition for internal state
 *
 * Rate Transition Behavior:
 * - Fast-to-Slow: Samples the fast input at slower output rate (zero-order hold)
 * - Slow-to-Fast: Repeats the slow input value at faster output rate
 * - Same-Rate: Pass-through without buffering
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class RateTransition extends DiscreteBlock {

    // === Internal State ===
    /** Current buffered value (held between rate transitions) */
    private Data currentValue;

    /** Previous input value (for fast-to-slow transitions) */
    private Data previousValue;

    /** State for holding sampled value */
    private State stateOutput;

    /** No feedthrough for rate transitions (introduces delay) */
    private final boolean feedthrough = false;

    // === SIMULINK-Compatible Parameters ===
    /** Input sample time parameter */
    @Getter
    private final Parameter inputSampleTime;

    /** Output sample time parameter */
    @Getter
    private final Parameter outputSampleTime;

    /** Initial condition parameter */
    @Getter
    private final Parameter initialCondition;

    /** Output data type specification parameter */
    private final Parameter outDataType;

    /** Data integrity flag parameter */
    private final Parameter ensureDataIntegrity;

    /** Initial state parameter */
    private final Parameter x0;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("InputSampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutputSampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("EnsureDataIntegrity", "on");
        PARAMETER_DEFAULTS.put("X0", "0");
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

        // Output port defaults (rate transition has no feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false); // Rate transition never has feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private RateTransition(Parameter inputSampleTime, Parameter outputSampleTime,
                          Parameter initialCondition, Parameter outDataType,
                          Parameter ensureDataIntegrity, Parameter x0,
                          String blockName, String blockPath, String blockUUID,
                          NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(inputSampleTime, outputSampleTime);

        // Assign parameters
        this.inputSampleTime = Objects.requireNonNull(inputSampleTime, "Input sample time parameter cannot be null");
        this.outputSampleTime = Objects.requireNonNull(outputSampleTime, "Output sample time parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.ensureDataIntegrity = Objects.requireNonNull(ensureDataIntegrity, "Data integrity parameter cannot be null");
        this.x0 = Objects.requireNonNull(x0, "X0 parameter cannot be null");

        // Set discrete sample time to output sample time
        setSampleTime(outputSampleTime);

        // Initialize ports
        postConstructionInitialization();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public RateTransition(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.inputSampleTime = getParameterByName("InputSampleTime");
        this.outputSampleTime = getParameterByName("OutputSampleTime");
        this.initialCondition = getParameterByName("InitialCondition");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.ensureDataIntegrity = getParameterByName("EnsureDataIntegrity");
        this.x0 = getParameterByName("X0");

        // Set discrete sample time to output sample time
        setSampleTime(outputSampleTime);

        // Initialize ports
        postConstructionInitialization();
    }

    /**
     * DTO-NATIVE Constructor - Creates RateTransition block directly from RateTransitionDto DTO
     */
    public RateTransition(RateTransitionDto dto, NCSLabModel model) {
        super(dto, model);

        // Get parameters by name from the automatically populated parameterList
        this.inputSampleTime = getParameterByName("InputSampleTime");
        this.outputSampleTime = getParameterByName("OutputSampleTime");
        this.initialCondition = getParameterByName("InitialCondition");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.ensureDataIntegrity = getParameterByName("EnsureDataIntegrity");
        this.x0 = getParameterByName("X0");

        // Set discrete sample time to output sample time
        setSampleTime(this.outputSampleTime);

        // Initialize ports
        postConstructionInitialization();

        log.info("DTO-NATIVE: RateTransition block created successfully - {}", dto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static RateTransition fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter inputSampleTime = createInputSampleTimeFromJSON(paramValues, blockName);
            Parameter outputSampleTime = createOutputSampleTimeFromJSON(paramValues, blockName);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter ensureDataIntegrity = createEnsureDataIntegrityFromJSON(paramValues, blockName);
            Parameter x0 = createX0FromJSON(paramValues, blockName);

            RateTransition block = new RateTransition(inputSampleTime, outputSampleTime,
                                                     initialCondition, outDataType,
                                                     ensureDataIntegrity, x0,
                                                     blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, inputSampleTime, outputSampleTime,
                                      initialCondition, outDataType,
                                      ensureDataIntegrity, x0);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create RateTransition block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static RateTransition create(String name, String path, double inputRate,
                                       double outputRate, NCSLabModel model) {
        return create(name, path, inputRate, outputRate, 0.0, model);
    }

    /**
     * Create a RateTransition block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param inputRate Input sample time (sample rate)
     * @param outputRate Output sample time (sample rate)
     * @param initialCondition Initial condition for the output
     * @param model Parent model
     * @return RateTransition block instance
     */
    public static RateTransition create(String name, String path, double inputRate,
                                       double outputRate, double initialCondition,
                                       NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        RateTransitionDto dto = RateTransitionDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .inputSampleTime(com.ncslab.dto.common.TypedParameter.of(inputRate))
            .outputSampleTime(com.ncslab.dto.common.TypedParameter.of(outputRate))
            .initialCondition(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .x0(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of("Inherit: Same as input"))
            .ensureDataIntegrity(com.ncslab.dto.common.TypedParameter.of(true))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid RateTransition parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new RateTransition(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter inputSampleTime, Parameter outputSampleTime) {
        double inputRate = inputSampleTime.getDouble();
        double outputRate = outputSampleTime.getDouble();

        // Both cannot be inherited
        if (inputRate == -1.0 && outputRate == -1.0) {
            log.warn("Both input and output sample times are inherited - rate transition may not function correctly");
        }

        // Validate positive values
        if (inputRate != -1.0 && inputRate <= 0.0) {
            throw new IllegalArgumentException("Input sample time must be positive or -1 (inherited)");
        }
        if (outputRate != -1.0 && outputRate <= 0.0) {
            throw new IllegalArgumentException("Output sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInputSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("InputSampleTime", "-1");
        return new Parameter(null, 1, "InputSampleTime", value);
    }

    private static Parameter createOutputSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("OutputSampleTime", "-1");
        return new Parameter(null, 2, "OutputSampleTime", value);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 3, "InitialCondition", value);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 4, "OutDataTypeStr", value);
    }

    private static Parameter createEnsureDataIntegrityFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("EnsureDataIntegrity", "on");
        return new Parameter(null, 5, "EnsureDataIntegrity", value);
    }

    private static Parameter createX0FromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("X0", "0.0");
        return new Parameter(null, 6, "X0", value);
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

    private static void setParameterBlockReference(RateTransition block, Parameter... parameters) {
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
        identity.put("blockType", "RateTransition");
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
            OutputPort outputPort = new OutputPort(this, 1, feedthrough);
            outputPortList.add(outputPort);
        }

        // Set references to the ports
        input = inputPortList.get(0);  // First input port
        output = outputPortList.get(0); // First output port

        // Rate transition never has feedthrough
        output.setFeedThrough(feedthrough); // false
    }

    // === Rate Transition Analysis Methods ===

    /**
     * Determine if this is a fast-to-slow rate transition (downsampling)
     */
    private boolean isFastToSlow() {
        double inRate = inputSampleTime.getDouble();
        double outRate = outputSampleTime.getDouble();

        // Both must be explicit (not inherited)
        if (inRate <= 0 || outRate <= 0) {
            return false;
        }

        // Fast-to-slow: input rate is smaller (faster) than output rate
        return inRate < outRate;
    }

    /**
     * Determine if this is a slow-to-fast rate transition (upsampling)
     */
    private boolean isSlowToFast() {
        double inRate = inputSampleTime.getDouble();
        double outRate = outputSampleTime.getDouble();

        // Both must be explicit (not inherited)
        if (inRate <= 0 || outRate <= 0) {
            return false;
        }

        // Slow-to-fast: input rate is larger (slower) than output rate
        return inRate > outRate;
    }

    /**
     * Determine if this is a same-rate transition (pass-through)
     */
    private boolean isSameRate() {
        double inRate = inputSampleTime.getDouble();
        double outRate = outputSampleTime.getDouble();

        // Both inherited or both equal
        if (inRate == -1.0 && outRate == -1.0) {
            return true;
        }

        if (inRate <= 0 || outRate <= 0) {
            return true; // Cannot determine, assume same
        }

        return Math.abs(inRate - outRate) < 1e-9;
    }

    // === Code Generation Methods ===

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add rate transition mode information to context
        context.put("isFastToSlow", isFastToSlow());
        context.put("isSlowToFast", isSlowToFast());
        context.put("isSameRate", isSameRate());
        context.put("inputRate", inputSampleTime.getDouble());
        context.put("outputRate", outputSampleTime.getDouble());

        String codeStr = TemplateManager.renderTemplate("c/signal/RateTransition/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/signal/RateTransition/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add initialization parameters to context
        context.put("initialCondition", initialCondition.getDouble());
        context.put("x0", x0.getDouble());

        String codeStr = TemplateManager.renderTemplate("c/signal/RateTransition/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add rate transition mode information to context
        context.put("isFastToSlow", isFastToSlow());
        context.put("isSlowToFast", isSlowToFast());
        context.put("isSameRate", isSameRate());

        // Get signal info for proper C variable names
        String inputSignalName = getInputPortVariable(0);
        context.put("signal", inputSignalName);
        context.put("signalName", inputSignalName);
        context.put("output1", getOutputPortVariable(0));

        if (stateOutput != null) {
            context.put("stateOutputName", stateOutput.getName());
        }

        String codeStr = TemplateManager.renderTemplate("c/signal/RateTransition/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Validate sample times are multiples of fixed step
        if (outputSampleTime.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(OutputSampleTime) of Block " + this.blockName + " must be a real double scalar!\n \n");
            throw(e);
        }

        double outRate = outputSampleTime.getDouble();
        if (outRate > 0 && !isSampleTimeMultiple(outRate, model.getConfig().getFixedStep())) {
            MatDimException e = new MatDimException("Parameter(OutputSampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        // Set output dimensions to match input
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());

        // Create state for holding value between transitions
        switch (signal.getDataType()) {
            case REAL:
                stateOutput = new State(this, 1, "stateOutput", 1, 1);
                break;
            case MATRIX:
                stateOutput = new State(this, 1, "stateOutput", signal.getHeight(), signal.getWidth());
                break;
        }
        stateList.add(stateOutput);
    }

    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);

        // Initialize both values with initial condition
        currentValue = new Data(initialCondition.getData().getInitValue());
        previousValue = new Data(initialCondition.getData().getInitValue());

        // Initialize state if available
        if (stateOutput != null) {
            stateOutput.setData(new Data(initialCondition.getData().getInitValue()));
        }
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Output the current held value
        if (stateOutput != null && stateOutput.getData() != null) {
            out.setData(stateOutput.getData());
        } else {
            out.setData(currentValue);
        }
    }

    @Override
    public void calculateUpdate(double t) {
        // For rate transition, updates happen in calculateDiscreteUpdate
        // This method is typically empty for discrete blocks
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort input = inputPortList.get(0);

        if (input.getData() == null) {
            return;
        }

        // Update the held value based on rate transition mode
        if (isSameRate()) {
            // Same rate: simple pass-through
            currentValue = input.getData();
            if (stateOutput != null) {
                stateOutput.setData(input.getData());
            }
        } else if (isFastToSlow()) {
            // Fast-to-slow: sample and hold (take current input)
            // This discrete update happens at the output rate (slower)
            currentValue = input.getData();
            if (stateOutput != null) {
                stateOutput.setData(input.getData());
            }
        } else if (isSlowToFast()) {
            // Slow-to-fast: hold the value (repeat input)
            // The input updates at the slower rate, output samples at faster rate
            currentValue = input.getData();
            if (stateOutput != null) {
                stateOutput.setData(input.getData());
            }
        } else {
            // Unknown transition type: default to pass-through
            currentValue = input.getData();
            if (stateOutput != null) {
                stateOutput.setData(input.getData());
            }
        }

        // Save previous value for potential use
        previousValue = currentValue;
    }

    /**
     * Generate discrete update code for C code generation.
     * This method handles the template-based code generation for discrete time updates.
     */
    public void generateDiscreteUpdateCodeCInside(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add rate transition mode information to context
        context.put("isFastToSlow", isFastToSlow());
        context.put("isSlowToFast", isSlowToFast());
        context.put("isSameRate", isSameRate());

        // Get signal info
        String inputSignalName = getInputPortVariable(0);
        context.put("signal", inputSignalName);
        context.put("signalName", inputSignalName);

        if (stateOutput != null) {
            context.put("stateOutputName", stateOutput.getName());
        }

        // Render discrete update template
        try {
            String codeStr = TemplateManager.renderTemplate("c/signal/RateTransition/discreteUpdate.vm", context);
            code.addDiscreteUpdateCode(codeStr);
        } catch (Exception e) {
            // Fallback to manual discrete update code if no template exists
            String discreteUpdateCode = String.format("/* Discrete update for RateTransition block %d: %s */\n",
                                                    getBlockId(), getBlockName());
            discreteUpdateCode += String.format("/* Mode: %s */\n",
                                               isFastToSlow() ? "Fast-to-Slow" :
                                               isSlowToFast() ? "Slow-to-Fast" : "Same-Rate");

            if (stateOutput != null) {
                discreteUpdateCode += String.format("%s = %s;\n",
                                                   stateOutput.getName(),
                                                   inputSignalName);
            }

            code.addDiscreteUpdateCode(discreteUpdateCode);
        }
    }
}
