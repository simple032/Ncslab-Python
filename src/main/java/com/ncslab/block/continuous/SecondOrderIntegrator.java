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
import com.ncslab.dto.block.specialized.continuous.SecondOrderIntegratorDto;

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
 * SecondOrderIntegrator block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Performs double continuous-time integration: y'' = u
 *
 * The block maintains two continuous states:
 * - Position (x): The integrated output
 * - Velocity (v = dx/dt): The rate of change of position
 *
 * Mathematical Behavior:
 * - dx/dt = v
 * - dv/dt = u (input is acceleration)
 * - Output y = x (position)
 *
 * SIMULINK Parameters:
 * - InitialConditionX: Initial position value at t=0 (default: 0.0)
 * - InitialConditionDxDt: Initial velocity value at t=0 (default: 0.0)
 * - LimitX: Whether to limit position output (default: false)
 * - UpperLimitX: Upper limit for position (default: Infinity)
 * - LowerLimitX: Lower limit for position (default: -Infinity)
 * - LimitDxDt: Whether to limit velocity (default: false)
 * - UpperLimitDxDt: Upper limit for velocity (default: Infinity)
 * - LowerLimitDxDt: Lower limit for velocity (default: -Infinity)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class SecondOrderIntegrator extends ContinuousBlock {

    // === Internal States ===
    /** Position state variable (x) */
    @Getter
    @Setter
    private State statePosition;

    /** Velocity state variable (v = dx/dt) */
    @Getter
    @Setter
    private State stateVelocity;

    // === Saturation Optimization ===
    /** Whether position saturation is enabled */
    private boolean positionSaturationEnabled = false;

    /** Whether velocity saturation is enabled */
    private boolean velocitySaturationEnabled = false;

    /** Upper position limit value */
    private double upperLimitX = Double.POSITIVE_INFINITY;

    /** Lower position limit value */
    private double lowerLimitX = Double.NEGATIVE_INFINITY;

    /** Upper velocity limit value */
    private double upperLimitDxDt = Double.POSITIVE_INFINITY;

    /** Lower velocity limit value */
    private double lowerLimitDxDt = Double.NEGATIVE_INFINITY;

    // === SIMULINK-Compatible Parameters ===
    /** Initial position parameter */
    private final Parameter initialConditionX;

    /** Initial velocity parameter */
    private final Parameter initialConditionDxDt;

    /** Position limiting enable parameter */
    private final Parameter limitX;

    /** Upper position limit parameter */
    private final Parameter upperLimitXParam;

    /** Lower position limit parameter */
    private final Parameter lowerLimitXParam;

    /** Velocity limiting enable parameter */
    private final Parameter limitDxDt;

    /** Upper velocity limit parameter */
    private final Parameter upperLimitDxDtParam;

    /** Lower velocity limit parameter */
    private final Parameter lowerLimitDxDtParam;

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
        PARAMETER_DEFAULTS.put("InitialConditionX", "0");
        PARAMETER_DEFAULTS.put("InitialConditionDxDt", "0");
        PARAMETER_DEFAULTS.put("LimitX", "off");
        PARAMETER_DEFAULTS.put("UpperLimitX", "inf");
        PARAMETER_DEFAULTS.put("LowerLimitX", "-inf");
        PARAMETER_DEFAULTS.put("LimitDxDt", "off");
        PARAMETER_DEFAULTS.put("UpperLimitDxDt", "inf");
        PARAMETER_DEFAULTS.put("LowerLimitDxDt", "-inf");
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
    private SecondOrderIntegrator(Parameter initialConditionX, Parameter initialConditionDxDt,
                                 Parameter limitX, Parameter upperLimitX, Parameter lowerLimitX,
                                 Parameter limitDxDt, Parameter upperLimitDxDt, Parameter lowerLimitDxDt,
                                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(initialConditionX, initialConditionDxDt, upperLimitX, lowerLimitX,
                         upperLimitDxDt, lowerLimitDxDt);

        // Assign parameters
        this.initialConditionX = Objects.requireNonNull(initialConditionX, "Initial position parameter cannot be null");
        this.initialConditionDxDt = Objects.requireNonNull(initialConditionDxDt, "Initial velocity parameter cannot be null");
        this.limitX = Objects.requireNonNull(limitX, "Limit position parameter cannot be null");
        this.upperLimitXParam = Objects.requireNonNull(upperLimitX, "Upper position limit parameter cannot be null");
        this.lowerLimitXParam = Objects.requireNonNull(lowerLimitX, "Lower position limit parameter cannot be null");
        this.limitDxDt = Objects.requireNonNull(limitDxDt, "Limit velocity parameter cannot be null");
        this.upperLimitDxDtParam = Objects.requireNonNull(upperLimitDxDt, "Upper velocity limit parameter cannot be null");
        this.lowerLimitDxDtParam = Objects.requireNonNull(lowerLimitDxDt, "Lower velocity limit parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.initialConditionX);
        parameterList.add(this.initialConditionDxDt);
        parameterList.add(this.limitX);
        parameterList.add(this.upperLimitXParam);
        parameterList.add(this.lowerLimitXParam);
        parameterList.add(this.limitDxDt);
        parameterList.add(this.upperLimitDxDtParam);
        parameterList.add(this.lowerLimitDxDtParam);

        // Initialize ports
        initializePorts();

        // Pre-compute saturation settings for performance
        updateSaturationSettings();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public SecondOrderIntegrator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Use name-based parameter access instead of index-based
        this.initialConditionX = getParameterByName("InitialConditionX");
        this.initialConditionDxDt = getParameterByName("InitialConditionDxDt");
        this.limitX = getParameterByName("LimitX");
        this.upperLimitXParam = getParameterByName("UpperLimitX");
        this.lowerLimitXParam = getParameterByName("LowerLimitX");
        this.limitDxDt = getParameterByName("LimitDxDt");
        this.upperLimitDxDtParam = getParameterByName("UpperLimitDxDt");
        this.lowerLimitDxDtParam = getParameterByName("LowerLimitDxDt");

        // Initialize ports based on legacy logic
        input = new InputPort(this, 1);
        inputPortList.add(input);
        output = new OutputPort(this, 1, false);
        outputPortList.add(output);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static SecondOrderIntegrator fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter initialConditionX = createInitialConditionXFromJSON(paramValues, blockName);
            Parameter initialConditionDxDt = createInitialConditionDxDtFromJSON(paramValues, blockName);
            Parameter limitX = createLimitXFromJSON(paramValues, blockName);
            Parameter upperLimitX = createUpperLimitXFromJSON(paramValues, blockName);
            Parameter lowerLimitX = createLowerLimitXFromJSON(paramValues, blockName);
            Parameter limitDxDt = createLimitDxDtFromJSON(paramValues, blockName);
            Parameter upperLimitDxDt = createUpperLimitDxDtFromJSON(paramValues, blockName);
            Parameter lowerLimitDxDt = createLowerLimitDxDtFromJSON(paramValues, blockName);

            SecondOrderIntegrator block = new SecondOrderIntegrator(initialConditionX, initialConditionDxDt,
                                                                   limitX, upperLimitX, lowerLimitX,
                                                                   limitDxDt, upperLimitDxDt, lowerLimitDxDt,
                                                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, initialConditionX, initialConditionDxDt,
                                     limitX, upperLimitX, lowerLimitX,
                                     limitDxDt, upperLimitDxDt, lowerLimitDxDt);

            return block;

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getClass().getSimpleName() + " occurred";
            }
            throw new BlockCreationException("Failed to create SecondOrderIntegrator block from JSON: " + errorMsg, e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static SecondOrderIntegrator create(String name, String path, double initialX, double initialV, NCSLabModel model) {
        return create(name, path, initialX, initialV, false,
                     Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                     false, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, model);
    }

    /**
     * Create a SecondOrderIntegrator block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param initialX Initial position value at t=0
     * @param initialV Initial velocity value at t=0
     * @param limitX Whether to limit position values
     * @param upperX Upper limit for position
     * @param lowerX Lower limit for position
     * @param limitV Whether to limit velocity values
     * @param upperV Upper limit for velocity
     * @param lowerV Lower limit for velocity
     * @param model Parent model
     * @return SecondOrderIntegrator block instance
     */
    public static SecondOrderIntegrator create(String name, String path, double initialX, double initialV,
                                              boolean limitX, double upperX, double lowerX,
                                              boolean limitV, double upperV, double lowerV,
                                              NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        SecondOrderIntegratorDto dto = SecondOrderIntegratorDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .initialConditionX(com.ncslab.dto.common.TypedParameter.of(initialX))
            .initialConditionDxDt(com.ncslab.dto.common.TypedParameter.of(initialV))
            .limitX(com.ncslab.dto.common.TypedParameter.of(limitX ? "on" : "off"))
            .upperLimitX(com.ncslab.dto.common.TypedParameter.of(upperX))
            .lowerLimitX(com.ncslab.dto.common.TypedParameter.of(lowerX))
            .limitDxDt(com.ncslab.dto.common.TypedParameter.of(limitV ? "on" : "off"))
            .upperLimitDxDt(com.ncslab.dto.common.TypedParameter.of(upperV))
            .lowerLimitDxDt(com.ncslab.dto.common.TypedParameter.of(lowerV))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid SecondOrderIntegrator parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new SecondOrderIntegrator(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter initialX, Parameter initialV,
                                         Parameter upperX, Parameter lowerX,
                                         Parameter upperV, Parameter lowerV) {
        try {
            double xValue = initialX.getDouble();
            if (Double.isNaN(xValue) || Double.isInfinite(xValue)) {
                throw new IllegalArgumentException("Initial position must be finite, got: " + xValue);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Initial position parameter '" + initialX.getInitString() + "' is not a valid number", e);
        }

        try {
            double vValue = initialV.getDouble();
            if (Double.isNaN(vValue) || Double.isInfinite(vValue)) {
                throw new IllegalArgumentException("Initial velocity must be finite, got: " + vValue);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Initial velocity parameter '" + initialV.getInitString() + "' is not a valid number", e);
        }

        // Validate position limits
        try {
            double upperXValue = upperX.getDouble();
            double lowerXValue = lowerX.getDouble();
            if (!Double.isInfinite(upperXValue) && !Double.isInfinite(lowerXValue)) {
                if (upperXValue <= lowerXValue) {
                    throw new IllegalArgumentException("Upper position limit must be greater than lower position limit");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Position limit parameters are not valid numbers: upper='"
                + upperX.getInitString() + "', lower='" + lowerX.getInitString() + "'", e);
        }

        // Validate velocity limits
        try {
            double upperVValue = upperV.getDouble();
            double lowerVValue = lowerV.getDouble();
            if (!Double.isInfinite(upperVValue) && !Double.isInfinite(lowerVValue)) {
                if (upperVValue <= lowerVValue) {
                    throw new IllegalArgumentException("Upper velocity limit must be greater than lower velocity limit");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Velocity limit parameters are not valid numbers: upper='"
                + upperV.getInitString() + "', lower='" + lowerV.getInitString() + "'", e);
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInitialConditionXFromJSON(JSONObject paramValues, String blockName) {
        String initialXValue = paramValues.optString("InitialConditionX", "0");
        return new Parameter(null, 1, "InitialConditionX", initialXValue);
    }

    private static Parameter createInitialConditionDxDtFromJSON(JSONObject paramValues, String blockName) {
        String initialVValue = paramValues.optString("InitialConditionDxDt", "0");
        return new Parameter(null, 2, "InitialConditionDxDt", initialVValue);
    }

    private static Parameter createLimitXFromJSON(JSONObject paramValues, String blockName) {
        String limitXValue = paramValues.optString("LimitX", "off");
        return new Parameter(null, 3, "LimitX", limitXValue);
    }

    private static Parameter createUpperLimitXFromJSON(JSONObject paramValues, String blockName) {
        String upperXValue = paramValues.optString("UpperLimitX", "inf");
        return new Parameter(null, 4, "UpperLimitX", upperXValue);
    }

    private static Parameter createLowerLimitXFromJSON(JSONObject paramValues, String blockName) {
        String lowerXValue = paramValues.optString("LowerLimitX", "-inf");
        return new Parameter(null, 5, "LowerLimitX", lowerXValue);
    }

    private static Parameter createLimitDxDtFromJSON(JSONObject paramValues, String blockName) {
        String limitVValue = paramValues.optString("LimitDxDt", "off");
        return new Parameter(null, 6, "LimitDxDt", limitVValue);
    }

    private static Parameter createUpperLimitDxDtFromJSON(JSONObject paramValues, String blockName) {
        String upperVValue = paramValues.optString("UpperLimitDxDt", "inf");
        return new Parameter(null, 7, "UpperLimitDxDt", upperVValue);
    }

    private static Parameter createLowerLimitDxDtFromJSON(JSONObject paramValues, String blockName) {
        String lowerVValue = paramValues.optString("LowerLimitDxDt", "-inf");
        return new Parameter(null, 8, "LowerLimitDxDt", lowerVValue);
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

    private static void setParameterBlockReference(SecondOrderIntegrator block, Parameter... parameters) {
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
        identity.put("blockType", "SecondOrderIntegrator");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Main input port (acceleration)
        input = new InputPort(this, 1);
        inputPortList.add(input);

        // Main output port (position)
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
        context.put("statePosition", statePosition);
        context.put("stateVelocity", stateVelocity);

        String codeStr = TemplateManager.renderTemplate("m/continuous/SecondOrderIntegrator/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Computed values
        context.put("statePosition", statePosition);
        context.put("stateVelocity", stateVelocity);
        context.put("input", getInputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("m/continuous/SecondOrderIntegrator/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Computed values
        context.put("statePosition", statePosition.getName());
        context.put("stateVelocity", stateVelocity.getName());
        context.put("statePositionName", context.get(statePosition.getLocalName()));
        context.put("stateVelocityName", context.get(stateVelocity.getLocalName()));

        // Data type constants for template conditionals
        context.put("realDataType", com.ncslab.block.data.DataType.REAL);
        context.put("matrixDataType", com.ncslab.block.data.DataType.MATRIX);

        String arraysCode = TemplateManager.renderTemplate("c/continuous/SecondOrderIntegrator/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Computed values
        context.put("statePosition", statePosition);
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("m/continuous/SecondOrderIntegrator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Output signal object for template
        if (outputPortList != null && !outputPortList.isEmpty()) {
            context.put("outputSignalObject", outputPortList.get(0).getOutputSignalC());
        }

        // State variables for template
        context.put("statePosition", statePosition.getName());
        context.put("stateVelocity", stateVelocity.getName());

        String initCode = TemplateManager.renderTemplate("c/continuous/SecondOrderIntegrator/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // State variables
        context.put("statePosition", statePosition.getName());
        context.put("statePositionName", context.get(statePosition.getLocalName()));

        // Output variables
        context.put("outputs", getOutputPortVariables());
        context.put("output", getOutputPortVariable(0));

        // Data type constants for template conditionals
        context.put("realDataType", com.ncslab.block.data.DataType.REAL);
        context.put("matrixDataType", com.ncslab.block.data.DataType.MATRIX);

        String codeStr = TemplateManager.renderTemplate("c/continuous/SecondOrderIntegrator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // State variables
        context.put("statePosition", statePosition.getName());
        context.put("stateVelocity", stateVelocity.getName());
        context.put("statePositionName", context.get(statePosition.getLocalName()));
        context.put("stateVelocityName", context.get(stateVelocity.getLocalName()));
        context.put("statePositionDerivative", statePosition.getDerivativeName());
        context.put("stateVelocityDerivative", stateVelocity.getDerivativeName());

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

        String codeStr = TemplateManager.renderTemplate("c/continuous/SecondOrderIntegrator/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Output dimension matches input dimension (acceleration input determines position output dimension)
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());

        // Create two states: position and velocity
        statePosition = new State(this, 1, "position", signal.getHeight(), signal.getWidth());
        stateVelocity = new State(this, 2, "velocity", signal.getHeight(), signal.getWidth());

        stateList.add(statePosition);
        stateList.add(stateVelocity);
    }

    /**
     * DTO-NATIVE Constructor - Creates SecondOrderIntegrator block directly from BlockDto DTO
     */
    public SecondOrderIntegrator(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.initialConditionX = getParameterByName("InitialConditionX");
        this.initialConditionDxDt = getParameterByName("InitialConditionDxDt");
        this.limitX = getParameterByName("LimitX");
        this.upperLimitXParam = getParameterByName("UpperLimitX");
        this.lowerLimitXParam = getParameterByName("LowerLimitX");
        this.limitDxDt = getParameterByName("LimitDxDt");
        this.upperLimitDxDtParam = getParameterByName("UpperLimitDxDt");
        this.lowerLimitDxDtParam = getParameterByName("LowerLimitDxDt");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    public void checkDimension() throws MatDimException {
        // No dimension checking needed for second-order integrator
        // It accepts any dimension input and outputs same dimension
    }

    @Override
    public void calculateInit() {
        OutputPort output = outputPortList.get(0);

        // Initialize position state with initial condition
        statePosition.setData(initialConditionX.getData());

        // Initialize velocity state with initial condition
        stateVelocity.setData(initialConditionDxDt.getData());

        // Output is initially the position
        output.setData(statePosition.getData());
    }

    @Override
    public void calculateDerivative(double t) {
        InputPort inputPort = inputPortList.get(0);
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // dx/dt = v (velocity)
        Data velocityData = stateVelocity.getData();

        // Apply velocity saturation if enabled
        if (velocitySaturationEnabled) {
            velocityData = applySaturation(velocityData, upperLimitDxDt, lowerLimitDxDt);
        }

        statePosition.setDerivateData(velocityData);

        // dv/dt = u (input acceleration)
        stateVelocity.setDerivateData(signal.getData());
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort output = outputPortList.get(0);
        Data outputData = statePosition.getData();

        // Handle edge cases for position state
        if (outputData == null) {
            // Fallback to initial condition if state is null
            outputData = initialConditionX.getData();
        }

        // Apply position saturation limits if enabled
        if (positionSaturationEnabled) {
            outputData = applySaturation(outputData, upperLimitX, lowerLimitX);
        }

        output.setData(outputData);
    }

    // === Saturation Optimization Methods ===
    private void updateSaturationSettings() {
        positionSaturationEnabled = limitX.getInitString().equals("on");
        velocitySaturationEnabled = limitDxDt.getInitString().equals("on");

        if (positionSaturationEnabled) {
            upperLimitX = upperLimitXParam.getDouble();
            lowerLimitX = lowerLimitXParam.getDouble();
        }

        if (velocitySaturationEnabled) {
            upperLimitDxDt = upperLimitDxDtParam.getDouble();
            lowerLimitDxDt = lowerLimitDxDtParam.getDouble();
        }
    }

    private Data applySaturation(Data data, double upperLimit, double lowerLimit) {
        if (data.getDataType() == DataType.REAL) {
            double value = data.getInitValue();

            // Handle NaN and infinity cases
            if (Double.isNaN(value)) {
                value = 0.0; // Reset to zero on NaN
            } else if (Double.isInfinite(value)) {
                value = value > 0 ? upperLimit : lowerLimit;
            }

            // Apply saturation limits
            if (value > upperLimit) {
                return new Data(upperLimit);
            } else if (value < lowerLimit) {
                return new Data(lowerLimit);
            } else {
                return new Data(value);
            }
        } else {
            // Matrix case with saturation
            int height = data.getHeight();
            int width = data.getWidth();
            Data saturatedData = new Data(height, width);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    double value = data.getMatrix().get(i, j);

                    // Handle NaN and infinity cases
                    if (Double.isNaN(value)) {
                        value = 0.0;
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
            return saturatedData;
        }
    }
}
