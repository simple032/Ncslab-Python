package com.ncslab.block.discrete;

import Jama.Matrix;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.TappedDelayDto;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Tapped Delay block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * The Tapped Delay block implements a multi-tap delay line that outputs signals at multiple
 * delay intervals. It is commonly used to implement FIR filter delay structures and provides
 * access to the current sample plus multiple delayed samples simultaneously.
 *
 * SIMULINK Parameters:
 * - NumDelays: Number of delay taps (default: 2)
 * - DelayOrder: "Oldest first" or "Newest first" (default: "Oldest first")
 * - SampleTime: Sample time for discrete operation (default: -1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - InitialCondition: Initial values for delay buffer (default: 0.0)
 *
 * Key Features:
 * - Multi-tap delay line with configurable number of taps
 * - Vector output containing current + NumDelays delayed samples
 * - Configurable output ordering (oldest-first or newest-first)
 * - Supports scalar and matrix signals
 *
 * Output Format:
 * - Oldest first: [x[k], x[k-1], x[k-2], ..., x[k-NumDelays]]
 * - Newest first: [x[k-NumDelays], ..., x[k-2], x[k-1], x[k]]
 *
 * @author NCSLab
 * @version 1.0
 * @since Tapped Delay Implementation 2025
 */
public class TappedDelay extends DiscreteBlock {

    // === Internal State ===
    private double[] delayBuffer;  // Size = NumDelays
    private int numDelays;
    private boolean oldestFirst;
    private final boolean feedthrough = false; // Tapped delay has no feedthrough

    // === SIMULINK-Compatible Parameters ===
    private final Parameter numDelaysParam;
    private final Parameter delayOrderParam;
    private final Parameter sampleTimeParam;
    private final Parameter outDataType;
    private final Parameter initialCondition;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("NumDelays", "2");
        PARAMETER_DEFAULTS.put("DelayOrder", "Oldest first");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
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

        // Output port defaults (Tapped delay has no feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 3);  // Default: NumDelays=2 -> output size = 3
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false); // Tapped delay never has feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private TappedDelay(Parameter numDelaysParam, Parameter delayOrderParam, Parameter sampleTimeParam,
                       Parameter outDataType, Parameter initialCondition, String blockName, String blockPath,
                       String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(numDelaysParam, delayOrderParam, sampleTimeParam);

        // Assign parameters
        this.numDelaysParam = Objects.requireNonNull(numDelaysParam, "NumDelays parameter cannot be null");
        this.delayOrderParam = Objects.requireNonNull(delayOrderParam, "DelayOrder parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTimeParam, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public TappedDelay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.numDelaysParam = getParameterByName("NumDelays");
        this.delayOrderParam = getParameterByName("DelayOrder");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.initialCondition = getParameterByName("InitialCondition");

        // Set discrete sample time
        setSampleTime(sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();
    }

    /**
     * DTO-NATIVE Constructor - Creates TappedDelay block directly from BlockDto DTO
     */
    public TappedDelay(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Get parameters by name from the automatically populated parameterList
        this.numDelaysParam = getParameterByName("NumDelays");
        this.delayOrderParam = getParameterByName("DelayOrder");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.initialCondition = getParameterByName("InitialCondition");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for DTO-Based Creation ===
    public static TappedDelay createFromDto(TappedDelayDto dto, NCSLabModel model) {
        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid TappedDelay parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new TappedDelay(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static TappedDelay fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter numDelays = createNumDelaysFromJSON(paramValues, blockName);
            Parameter delayOrder = createDelayOrderFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);

            TappedDelay block = new TappedDelay(numDelays, delayOrder, sampleTime, outDataType,
                                               initialCondition, blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, numDelays, delayOrder, sampleTime, outDataType, initialCondition);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create TappedDelay block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static TappedDelay create(String name, String path, int numDelays, String delayOrder,
                                    double sampleTime, NCSLabModel model) {
        return create(name, path, numDelays, delayOrder, sampleTime, "Inherit: Same as input", 0.0, model);
    }

    /**
     * Create a TappedDelay block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param numDelays Number of delay taps
     * @param delayOrder Delay output ordering ("Oldest first" or "Newest first")
     * @param sampleTime Sample time for discrete operation
     * @param outDataType Output data type specification
     * @param initialCondition Initial values for delay buffer
     * @param model Parent model
     * @return TappedDelay block instance
     */
    public static TappedDelay create(String name, String path, int numDelays, String delayOrder,
                                    double sampleTime, String outDataType, double initialCondition,
                                    NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        TappedDelayDto dto = TappedDelayDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .numDelays(com.ncslab.dto.common.TypedParameter.of(numDelays))
            .delayOrder(com.ncslab.dto.common.TypedParameter.of(delayOrder))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .initialCondition(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .build();

        // Use factory method with validation
        return createFromDto(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter numDelays, Parameter delayOrder, Parameter sampleTime) {
        // Validate numDelays
        int numDelaysValue = Integer.parseInt(numDelays.getInitString());
        if (numDelaysValue < 1) {
            throw new IllegalArgumentException("NumDelays must be at least 1");
        }

        // Validate delayOrder
        String delayOrderValue = delayOrder.getInitString();
        if (!"Oldest first".equals(delayOrderValue) && !"Newest first".equals(delayOrderValue)) {
            throw new IllegalArgumentException("DelayOrder must be 'Oldest first' or 'Newest first'");
        }

        // Validate sample time
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && (sampleTimeValue <= 0.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY)) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createNumDelaysFromJSON(JSONObject paramValues, String blockName) {
        String numDelaysValue = paramValues.optString("NumDelays", "2");
        return new Parameter(null, 1, "NumDelays", numDelaysValue);
    }

    private static Parameter createDelayOrderFromJSON(JSONObject paramValues, String blockName) {
        String delayOrderValue = paramValues.optString("DelayOrder", "Oldest first");
        return new Parameter(null, 2, "DelayOrder", delayOrderValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 4, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 5, "InitialCondition", initialConditionValue);
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

    private static void setParameterBlockReference(TappedDelay block, Parameter... parameters) {
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
        identity.put("blockType", "TappedDelay");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void postConstructionInitialization() {
        // Get parameters
        numDelays = Integer.parseInt(this.numDelaysParam.getInitString());
        String order = this.delayOrderParam.getInitString();
        oldestFirst = "Oldest first".equals(order);

        // Initialize delay buffer with initial condition
        delayBuffer = new double[numDelays];
        double ic = this.initialCondition.getData().getInitValue();
        Arrays.fill(delayBuffer, ic);

        // Create ports if they don't exist (DTO constructor may not have created them)
        if (inputPortList.isEmpty()) {
            InputPort inputPort = new InputPort(this, 1, "in1");
            inputPortList.add(inputPort);
        }

        if (outputPortList.isEmpty()) {
            // Output is vector of size NumDelays+1
            OutputPort outputPort = new OutputPort(this, numDelays + 1, feedthrough);
            outputPortList.add(outputPort);
        }

        // Set references to the ports
        input = inputPortList.get(0);  // First input port
        output = outputPortList.get(0); // First output port

        // Tapped delay never has feedthrough, so set false
        output.setFeedThrough(feedthrough); // false
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("NumDelays", numDelays);

        String codeStr = TemplateManager.renderTemplate("c/discrete/TappedDelay/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("NumDelays", numDelays);
        context.put("InitialCondition", this.initialCondition.getData().getInitValue());

        String codeStr = TemplateManager.renderTemplate("m/discrete/TappedDelay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("NumDelays", numDelays);
        context.put("InitialCondition", this.initialCondition.getData().getInitValue());

        String codeStr = TemplateManager.renderTemplate("c/discrete/TappedDelay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("NumDelays", numDelays);
        context.put("DelayOrder", this.delayOrderParam.getInitString());

        // Get input variable name
        InputPort inputPort = inputPortList.get(0);
        String inputVar = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        context.put("inputVar", inputVar);

        // Get output variable name
        OutputPort outputPort = outputPortList.get(0);
        String outputVar = outputPort.getOutputSignalC().getName();
        context.put("outputVar", outputVar);

        String codeStr = TemplateManager.renderTemplate("c/discrete/TappedDelay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();

        // TODO: Implement SIMULINK scalar zero expansion for IC
        // If IC is scalar "0" and input is vector/matrix, expand IC to zero matrix matching input dimensions
        // See Delay.java:503-526 for reference implementation
        // Note: TappedDelay has special output dimensions (NumDelays+1), adapt accordingly

        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (sampleTimeParam.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }

        if (!isSampleTimeMultiple(sampleTimeParam.getDouble(), model.getConfig().getFixedStep())) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        // Output is vector of size NumDelays+1
        out.setHeight(1);
        out.setWidth(numDelays + 1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(numDelays + 1);
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }

    @Override
    public void calculateInit() {
        // Initialize delay buffer with initial condition
        double ic = this.initialCondition.getData().getInitValue();
        Arrays.fill(delayBuffer, ic);
    }

    @Override
    public void calculateOutput(double t) {
        InputPort inputPort = inputPortList.get(0);
        OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        double currentValue = inputSignal.getData().getInitValue();

        // Build output vector: [current, delayed values...]
        int outputSize = numDelays + 1;
        Matrix outputMatrix = new Matrix(1, outputSize);

        if (oldestFirst) {
            // [x[k], x[k-1], x[k-2], ..., x[k-NumDelays]]
            outputMatrix.set(0, 0, currentValue);
            for (int i = 0; i < numDelays; i++) {
                outputMatrix.set(0, i + 1, delayBuffer[i]);
            }
        } else {
            // [x[k-NumDelays], ..., x[k-2], x[k-1], x[k]]
            for (int i = 0; i < numDelays; i++) {
                outputMatrix.set(0, i, delayBuffer[numDelays - 1 - i]);
            }
            outputMatrix.set(0, numDelays, currentValue);
        }

        output.setData(new Data(outputMatrix));
    }

    @Override
    public void calculateUpdate(double t) {
        // This method is called to update internal state before calculateOutput
        // For TappedDelay, the update happens in calculateDiscreteUpdate
        // This method is typically empty for discrete blocks that do state updates in calculateDiscreteUpdate
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort inputPort = inputPortList.get(0);
        OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        double currentValue = inputSignal.getData().getInitValue();

        // Update delay buffer: shift and insert new value
        for (int i = numDelays - 1; i > 0; i--) {
            delayBuffer[i] = delayBuffer[i - 1];
        }

        // Insert current value at position 0
        delayBuffer[0] = currentValue;
    }

    /**
     * Generate discrete update code for C code generation.
     * This method handles the template-based code generation for discrete time updates.
     */
    public void generateDiscreteUpdateCodeCInside(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("NumDelays", numDelays);

        // Get input variable name
        InputPort inputPort = inputPortList.get(0);
        String inputVar = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        context.put("inputVar", inputVar);

        String codeStr = TemplateManager.renderTemplate("c/discrete/TappedDelay/discreteUpdate.vm", context);
        code.addDiscreteUpdateCode(codeStr);
    }
}
