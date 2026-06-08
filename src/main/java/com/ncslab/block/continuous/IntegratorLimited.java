package com.ncslab.block.continuous;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.continuous.IntegratorLimitedDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
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
 * IntegratorLimited block - Integrator with saturation limits.
 *
 * Performs continuous-time integration with output saturation to prevent wind-up.
 * When the output reaches the upper or lower limit, integration stops in that
 * direction to prevent the integrator from winding up.
 *
 * Parameters:
 * - InitialCondition: Initial output value at t=0 (default: 0.0)
 * - UpperLimit: Upper saturation limit (default: Inf)
 * - LowerLimit: Lower saturation limit (default: -Inf)
 * - LimitOutput: Whether to limit output (default: true)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class IntegratorLimited extends ContinuousBlock {

    // === Internal State ===
    /** Integration state variable maintaining the integral value */
    @Getter
    @Setter
    private State state;

    // === Saturation Optimization ===
    /** Whether output saturation is enabled for performance optimization */
    private boolean saturationEnabled = true;

    /** Upper saturation limit value */
    private double upperLimit = Double.POSITIVE_INFINITY;

    /** Lower saturation limit value */
    private double lowerLimit = Double.NEGATIVE_INFINITY;

    // === SIMULINK-Compatible Parameters ===
    /** Initial condition parameter */
    private final Parameter initialCondition;

    /** Upper limit parameter */
    private final Parameter upperLimitParam;

    /** Lower limit parameter */
    private final Parameter lowerLimitParam;

    /** Output limiting enable parameter */
    private final Parameter limitOutput;

    // === Port References ===
    /** Output port reference */
    private OutputPort output;

    /** Input port reference */
    private InputPort input;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
        PARAMETER_DEFAULTS.put("UpperLimit", "inf");
        PARAMETER_DEFAULTS.put("LowerLimit", "-inf");
        PARAMETER_DEFAULTS.put("LimitOutput", "on");
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
        Map<String, Object> basicInput = new HashMap<>();
        basicInput.put("name", "in1");
        basicInput.put("width", 1);
        basicInput.put("height", 1);
        basicInput.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(basicInput);

        // Output port defaults
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> basicOutput = new HashMap<>();
        basicOutput.put("name", "out1");
        basicOutput.put("width", 1);
        basicOutput.put("height", 1);
        basicOutput.put("dataType", "REAL");
        OUTPUT_PORT_DEFAULTS.add(basicOutput);
    }

    // === Private Constructor with Typed Parameters ===
    private IntegratorLimited(Parameter initialCondition, Parameter upperLimit, Parameter lowerLimit,
                             Parameter limitOutput, String blockName, String blockPath,
                             String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(initialCondition, upperLimit, lowerLimit);

        // Assign parameters
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.upperLimitParam = Objects.requireNonNull(upperLimit, "Upper limit parameter cannot be null");
        this.lowerLimitParam = Objects.requireNonNull(lowerLimit, "Lower limit parameter cannot be null");
        this.limitOutput = Objects.requireNonNull(limitOutput, "Limit output parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.initialCondition);
        parameterList.add(this.upperLimitParam);
        parameterList.add(this.lowerLimitParam);
        parameterList.add(this.limitOutput);

        // Initialize ports
        initializePorts();

        // Pre-compute saturation settings for performance
        updateSaturationSettings();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public IntegratorLimited(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Use name-based parameter access instead of index-based
        this.initialCondition = getParameterByName("InitialCondition");
        this.upperLimitParam = getParameterByName("UpperLimit");
        this.lowerLimitParam = getParameterByName("LowerLimit");
        this.limitOutput = getParameterByName("LimitOutput");

        // Initialize ports based on legacy logic
        input = new InputPort(this, 1);
        inputPortList.add(input);
        output = new OutputPort(this, 1, false);
        outputPortList.add(output);

        // Update saturation settings
        updateSaturationSettings();
    }

    // === Static Factory Method for JSON Deserialization ===
    public static IntegratorLimited fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter upperLimit = createUpperLimitFromJSON(paramValues, blockName);
            Parameter lowerLimit = createLowerLimitFromJSON(paramValues, blockName);
            Parameter limitOutput = createLimitOutputFromJSON(paramValues, blockName);

            IntegratorLimited block = new IntegratorLimited(initialCondition, upperLimit, lowerLimit,
                                                           limitOutput, blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, initialCondition, upperLimit, lowerLimit, limitOutput);

            return block;

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getClass().getSimpleName() + " occurred";
            }
            throw new BlockCreationException("Failed to create IntegratorLimited block from JSON: " + errorMsg, e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static IntegratorLimited create(String name, String path, double initialCondition, NCSLabModel model) {
        return create(name, path, initialCondition, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, true, model);
    }

    /**
     * Create an IntegratorLimited block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param initialCondition Initial output value at t=0
     * @param upperLimit Upper limit for output
     * @param lowerLimit Lower limit for output
     * @param limitOutput Whether to limit output values
     * @param model Parent model
     * @return IntegratorLimited block instance
     */
    public static IntegratorLimited create(String name, String path, double initialCondition,
                                          double upperLimit, double lowerLimit,
                                          boolean limitOutput, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        IntegratorLimitedDto dto = IntegratorLimitedDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .initialCondition(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .upperLimit(com.ncslab.dto.common.TypedParameter.of(upperLimit))
            .lowerLimit(com.ncslab.dto.common.TypedParameter.of(lowerLimit))
            .limitOutput(com.ncslab.dto.common.TypedParameter.of(limitOutput ? "on" : "off"))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid IntegratorLimited parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new IntegratorLimited(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter initialCondition, Parameter upperLimit, Parameter lowerLimit) {
        // Validate saturation limits
        try {
            double upperLimitValue = upperLimit.getDouble();
            double lowerLimitValue = lowerLimit.getDouble();
            if (!Double.isInfinite(upperLimitValue) && !Double.isInfinite(lowerLimitValue)) {
                if (upperLimitValue <= lowerLimitValue) {
                    throw new IllegalArgumentException("Upper limit must be greater than lower limit");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Limit parameters are not valid numbers: upper='"
                + upperLimit.getInitString() + "', lower='" + lowerLimit.getInitString() + "'", e);
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0");
        return new Parameter(null, 1, "InitialCondition", initialConditionValue);
    }

    private static Parameter createUpperLimitFromJSON(JSONObject paramValues, String blockName) {
        String upperLimitValue = paramValues.optString("UpperLimit", "inf");
        return new Parameter(null, 2, "UpperLimit", upperLimitValue);
    }

    private static Parameter createLowerLimitFromJSON(JSONObject paramValues, String blockName) {
        String lowerLimitValue = paramValues.optString("LowerLimit", "-inf");
        return new Parameter(null, 3, "LowerLimit", lowerLimitValue);
    }

    private static Parameter createLimitOutputFromJSON(JSONObject paramValues, String blockName) {
        String limitOutputValue = paramValues.optString("LimitOutput", "on");
        return new Parameter(null, 4, "LimitOutput", limitOutputValue);
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

    private static void setParameterBlockReference(IntegratorLimited block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
                // Update parameter name after setting block reference
                param.updateName();
            } catch (Exception e) {
                // Fallback: parameter block reference will be null, but should work for basic operations
            }
        }
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "IntegratorLimited");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Main input port
        input = new InputPort(this, 1);
        inputPortList.add(input);

        // Main output port
        output = new OutputPort(this, 1, false);
        outputPortList.add(output);

        // Pre-compute saturation settings for performance
        updateSaturationSettings();
    }

    // === Code Generation Methods ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Computed values
        context.put("state", state);

        String codeStr = TemplateManager.renderTemplate("m/continuous/IntegratorLimited/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Computed values
        context.put("state", state);
        context.put("input", getInputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("m/continuous/IntegratorLimited/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Computed values
        context.put("state", state.getName());
        context.put("stateName", context.get(state.getLocalName()));

        // Data type constants for template conditionals
        context.put("realDataType", com.ncslab.block.data.DataType.REAL);
        context.put("matrixDataType", com.ncslab.block.data.DataType.MATRIX);

        String arraysCode = TemplateManager.renderTemplate("c/continuous/IntegratorLimited/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Computed values
        context.put("state", state);
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("m/continuous/IntegratorLimited/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Output signal object for template
        if (outputPortList != null && !outputPortList.isEmpty()) {
            context.put("outputSignalObject", outputPortList.get(0).getOutputSignalC());
        }

        // State variable for template
        context.put("state", state.getName());

        String initCode = TemplateManager.renderTemplate("c/continuous/IntegratorLimited/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // State variables
        context.put("state", state.getName());
        context.put("stateName", context.get(state.getLocalName()));

        // Output variables
        context.put("outputs", getOutputPortVariables());
        context.put("output", getOutputPortVariable(0));

        // Data type constants for template conditionals
        context.put("realDataType", com.ncslab.block.data.DataType.REAL);
        context.put("matrixDataType", com.ncslab.block.data.DataType.MATRIX);

        String codeStr = TemplateManager.renderTemplate("c/continuous/IntegratorLimited/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // State variables
        context.put("state", state.getName());
        context.put("stateName", context.get(state.getLocalName()));
        context.put("stateDerivative", state.getDerivativeName());
        context.put("stateDerivativeName", state.getDerivativeName());

        // Input/output variables
        context.put("inputSignal", getInputPortVariable(0));
        context.put("inputs", getInputPortVariables());
        context.put("input", getInputPortVariable(0));

        // Data type constants for template conditionals
        context.put("realDataType", com.ncslab.block.data.DataType.REAL);
        context.put("matrixDataType", com.ncslab.block.data.DataType.MATRIX);

        // Get actual data types for template conditionals
        InputPort inputPort = inputPortList.get(0);
        if (inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null) {
            OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("signalDataType", signal.getDataType());
        } else {
            context.put("signalDataType", com.ncslab.block.data.DataType.REAL);
        }
        context.put("initialConditionDataType", initialCondition.getData().getDataType());

        // Add dimension variables for template loops
        if (state.getHeight() > 1 || state.getWidth() > 1) {
            context.put("signalHeight", state.getHeight());
            context.put("signalWidth", state.getWidth());
        }
        if (initialCondition.getHeight() > 1 || initialCondition.getWidth() > 1) {
            context.put("initialConditionHeight", initialCondition.getHeight());
            context.put("initialConditionWidth", initialCondition.getWidth());
        }

        String codeStr = TemplateManager.renderTemplate("c/continuous/IntegratorLimited/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        switch (signal.getDataType()) {
            case REAL:
                switch (initialCondition.getDataType()) {
                    case REAL:
                        out.setHeight(1);
                        out.setWidth(1);
                        out.getOutputSignalC().setHeight(1);
                        out.getOutputSignalC().setWidth(1);
                        out.getOutputSignalC().setDataType(DataType.REAL);
                        state = new State(this, 1, "integral", 1, 1);
                        break;
                    case MATRIX:
                        out.setHeight(initialCondition.getHeight());
                        out.setWidth(initialCondition.getWidth());
                        out.getOutputSignalC().setHeight(initialCondition.getHeight());
                        out.getOutputSignalC().setWidth(initialCondition.getWidth());
                        out.getOutputSignalC().setDataType(DataType.MATRIX);
                        state = new State(this, 1, "integral", initialCondition.getHeight(), initialCondition.getWidth());
                        break;
                }
                break;
            case MATRIX:
                switch (initialCondition.getDataType()) {
                    case REAL:
                        out.setHeight(signal.getHeight());
                        out.setWidth(signal.getWidth());
                        out.getOutputSignalC().setHeight(signal.getHeight());
                        out.getOutputSignalC().setWidth(signal.getWidth());
                        out.getOutputSignalC().setDataType(DataType.MATRIX);
                        state = new State(this, 1, "integral", signal.getHeight(), signal.getWidth());
                        break;
                    case MATRIX:
                        out.setHeight(signal.getHeight());
                        out.setWidth(signal.getWidth());
                        out.getOutputSignalC().setHeight(signal.getHeight());
                        out.getOutputSignalC().setWidth(signal.getWidth());
                        out.getOutputSignalC().setDataType(DataType.MATRIX);
                        state = new State(this, 1, "integral", signal.getHeight(), signal.getWidth());
                        break;
                }
                break;
        }
        stateList.clear();
        stateList.add(state);
    }

    /**
     * DTO-NATIVE Constructor - Creates IntegratorLimited block directly from BlockDto DTO
     */
    public IntegratorLimited(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.initialCondition = getParameterByName("InitialCondition");
        this.upperLimitParam = getParameterByName("UpperLimit");
        this.lowerLimitParam = getParameterByName("LowerLimit");
        this.limitOutput = getParameterByName("LimitOutput");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    public void checkDimension() throws MatDimException {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        switch (signal.getDataType()) {
            case REAL:
                break;
            case MATRIX:
                switch (initialCondition.getDataType()) {
                    case REAL:
                        break;
                    case MATRIX:
                        if (initialCondition.getHeight() != signal.getHeight()
                                || initialCondition.getWidth() != signal.getWidth()) {
                            MatDimException e = new MatDimException(
                                    "Dimension of input signal and Block " + this.blockName + " input dimension don't match!");
                            throw(e);
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void calculateInit() {
        OutputPort output = outputPortList.get(0);
        state.setData(initialCondition.getData());

        // Apply saturation to initial condition if enabled
        if (saturationEnabled) {
            Data initialData = state.getData();
            if (initialData.getDataType() == DataType.REAL) {
                double value = initialData.getInitValue();
                if (value > upperLimit) {
                    state.setData(new Data(upperLimit));
                } else if (value < lowerLimit) {
                    state.setData(new Data(lowerLimit));
                }
            }
        }

        output.setData(state.getData());
    }

    @Override
    public void calculateDerivative(double t) {
        InputPort inputPort = inputPortList.get(0);
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Get current state value
        Data stateData = state.getData();
        Data inputData = signal.getData();

        // Check if we're at saturation limits and should stop integrating
        if (saturationEnabled) {
            if (stateData.getDataType() == DataType.REAL) {
                double currentValue = stateData.getInitValue();
                double inputValue = inputData.getInitValue();

                // If at upper limit and input is positive, stop integrating
                if (currentValue >= upperLimit && inputValue > 0) {
                    state.setDerivateData(new Data(0.0));
                    return;
                }

                // If at lower limit and input is negative, stop integrating
                if (currentValue <= lowerLimit && inputValue < 0) {
                    state.setDerivateData(new Data(0.0));
                    return;
                }
            } else {
                // Matrix case - handle element-wise saturation
                int height = stateData.getHeight();
                int width = stateData.getWidth();
                Data derivData = new Data(height, width);

                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        double currentValue = stateData.getMatrix().get(i, j);
                        double inputValue = inputData.getMatrix().get(i, j);

                        // Check saturation per element
                        if ((currentValue >= upperLimit && inputValue > 0) ||
                            (currentValue <= lowerLimit && inputValue < 0)) {
                            derivData.getMatrix().set(i, j, 0.0);
                        } else {
                            derivData.getMatrix().set(i, j, inputValue);
                        }
                    }
                }
                state.setDerivateData(derivData);
                return;
            }
        }

        // Normal integration (not saturated)
        state.setDerivateData(signal.getData());
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort output = outputPortList.get(0);
        Data outputData = state.getData();

        // Handle edge cases for integral state
        if (outputData == null) {
            // Fallback to initial condition if state is null
            outputData = initialCondition.getData();
        }

        // Apply saturation limits if enabled
        if (saturationEnabled) {
            if (outputData.getDataType() == DataType.REAL) {
                double value = outputData.getInitValue();

                // Handle NaN and infinity cases
                if (Double.isNaN(value)) {
                    // Reset to initial condition on NaN
                    value = initialCondition.getData().getInitValue();
                    if (Double.isNaN(value)) {
                        value = 0.0; // Ultimate fallback
                    }
                } else if (Double.isInfinite(value)) {
                    // Clamp infinite values to limits
                    value = value > 0 ? upperLimit : lowerLimit;
                }

                // Apply saturation limits
                if (value > upperLimit) {
                    outputData = new Data(upperLimit);
                } else if (value < lowerLimit) {
                    outputData = new Data(lowerLimit);
                } else {
                    outputData = new Data(value);
                }
            } else {
                // Matrix case with saturation
                int height = outputData.getHeight();
                int width = outputData.getWidth();
                Data saturatedData = new Data(height, width);

                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        double value = outputData.getMatrix().get(i, j);

                        // Handle NaN and infinity cases
                        if (Double.isNaN(value)) {
                            if (initialCondition.getDataType() == DataType.MATRIX &&
                                i < initialCondition.getHeight() && j < initialCondition.getWidth()) {
                                value = initialCondition.getData().getMatrix().get(i, j);
                                if (Double.isNaN(value)) {
                                    value = 0.0;
                                }
                            } else {
                                value = 0.0;
                            }
                        } else if (Double.isInfinite(value)) {
                            value = value > 0 ? upperLimit : lowerLimit;
                        }

                        // Apply saturation limits
                        if (value > upperLimit) {
                            saturatedData.getMatrix().set(i, j, upperLimit);
                        } else if (value < lowerLimit) {
                            saturatedData.getMatrix().set(i, j, lowerLimit);
                        } else {
                            saturatedData.getMatrix().set(i, j, value);
                        }
                    }
                }
                outputData = saturatedData;
            }
        }

        output.setData(outputData);
    }

    // === Saturation Optimization Methods ===
    private void updateSaturationSettings() {
        saturationEnabled = limitOutput.getInitString().equals("on");
        if (saturationEnabled) {
            upperLimit = upperLimitParam.getDouble();
            lowerLimit = lowerLimitParam.getDouble();
        }
    }
}
