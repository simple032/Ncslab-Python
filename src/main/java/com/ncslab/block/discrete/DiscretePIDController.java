package com.ncslab.block.discrete;

import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import org.apache.velocity.VelocityContext;
import lombok.Getter;

import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.DiscretePIDControllerDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * DiscretePIDController block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - P: Proportional gain
 * - I: Integral gain
 * - D: Derivative gain
 * - N: Filter coefficient for derivative term (1/Tf)
 * - SampleTime: Sample time Ts for discrete operation
 * - InitialConditionForIntegrator: Initial condition for integral term
 * - InitialConditionForFilter: Initial condition for derivative filter
 *
 * Discrete PID Implementation using Tustin (bilinear) discretization:
 * - u(k) = P*e(k) + I_sum(k) + Df(k)
 * - Integral: I_sum(k) = I_sum(k-1) + I*Ts*(e(k)+e(k-1))/2
 * - Filtered derivative: Df(k) = (Df(k-1)*(2-N*Ts) + 2*N*D*(e(k)-e(k-1))) / (2+N*Ts)
 *
 * States:
 * - integralState: Current integral accumulator value
 * - prevError: Previous error value for derivative calculation
 * - filterState: Current filtered derivative value
 */
public class DiscretePIDController extends DiscreteBlock {

    // === Internal State ===
    private State integralState;
    private State prevError;
    private State filterState;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter proportionalGain;
    private final Parameter integralGain;
    private final Parameter derivativeGain;
    private final Parameter filterCoefficient;
    private final Parameter sampleTimeParam;
    private final Parameter initialConditionForIntegrator;
    private final Parameter initialConditionForFilter;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("P", "1");                           // Proportional gain
        PARAMETER_DEFAULTS.put("I", "1");                           // Integral gain
        PARAMETER_DEFAULTS.put("D", "0");                           // Derivative gain
        PARAMETER_DEFAULTS.put("N", "100");                         // Filter coefficient
        PARAMETER_DEFAULTS.put("SampleTime", "-1");                 // -1 for inherited
        PARAMETER_DEFAULTS.put("InitialConditionForIntegrator", "0"); // Initial condition for integrator
        PARAMETER_DEFAULTS.put("InitialConditionForFilter", "0");   // Initial condition for filter
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

        // Output port defaults (Discrete PID has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private DiscretePIDController(Parameter proportionalGain, Parameter integralGain, Parameter derivativeGain,
                                  Parameter filterCoefficient, Parameter sampleTime,
                                  Parameter initialConditionForIntegrator, Parameter initialConditionForFilter,
                                  String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(filterCoefficient, sampleTime);

        // Assign parameters
        this.proportionalGain = Objects.requireNonNull(proportionalGain, "Proportional gain parameter cannot be null");
        this.integralGain = Objects.requireNonNull(integralGain, "Integral gain parameter cannot be null");
        this.derivativeGain = Objects.requireNonNull(derivativeGain, "Derivative gain parameter cannot be null");
        this.filterCoefficient = Objects.requireNonNull(filterCoefficient, "Filter coefficient parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.initialConditionForIntegrator = Objects.requireNonNull(initialConditionForIntegrator, "Initial condition for integrator parameter cannot be null");
        this.initialConditionForFilter = Objects.requireNonNull(initialConditionForFilter, "Initial condition for filter parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.proportionalGain);
        parameterList.add(this.integralGain);
        parameterList.add(this.derivativeGain);
        parameterList.add(this.filterCoefficient);
        parameterList.add(this.sampleTimeParam);
        parameterList.add(this.initialConditionForIntegrator);
        parameterList.add(this.initialConditionForFilter);

        // Initialize ports
        initializePorts();

        // Initialize states
        initializeStates();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DiscretePIDController(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.proportionalGain = getParameterByName("P");
        this.integralGain = getParameterByName("I");
        this.derivativeGain = getParameterByName("D");
        this.filterCoefficient = getParameterByName("N");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.initialConditionForIntegrator = getParameterByName("InitialConditionForIntegrator");
        this.initialConditionForFilter = getParameterByName("InitialConditionForFilter");

        // Initialize ports based on legacy logic
        input = new InputPort(this, 1);
        inputPortList.add(input);
        output = new OutputPort(this, 1, true); // feedthrough = true for discrete PID
        outputPortList.add(output);

        // Initialize states
        integralState = new State(this, 1, "integralState", proportionalGain.getHeight(), proportionalGain.getWidth());
        prevError = new State(this, 2, "prevError", proportionalGain.getHeight(), proportionalGain.getWidth());
        filterState = new State(this, 3, "filterState", proportionalGain.getHeight(), proportionalGain.getWidth());
        stateList.add(integralState);
        stateList.add(prevError);
        stateList.add(filterState);
    }

    /**
     * DTO-NATIVE Constructor - Creates DiscretePIDController block directly from BlockDto DTO
     */
    public DiscretePIDController(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO with proper null checking and correct names
        this.proportionalGain = getParameterOrDefault("P", 1, "P");
        this.integralGain = getParameterOrDefault("I", 2, "I");
        this.derivativeGain = getParameterOrDefault("D", 3, "D");
        this.filterCoefficient = getParameterOrDefault("N", 4, "N");
        this.sampleTimeParam = getParameterOrDefault("SampleTime", 5, "SampleTime");
        this.initialConditionForIntegrator = getParameterOrDefault("InitialConditionForIntegrator", 6, "InitialConditionForIntegrator");
        this.initialConditionForFilter = getParameterOrDefault("InitialConditionForFilter", 7, "InitialConditionForFilter");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());

        // Initialize states
        if (proportionalGain != null) {
            integralState = new State(this, 1, "integralState", proportionalGain.getHeight(), proportionalGain.getWidth());
            prevError = new State(this, 2, "prevError", proportionalGain.getHeight(), proportionalGain.getWidth());
            filterState = new State(this, 3, "filterState", proportionalGain.getHeight(), proportionalGain.getWidth());
            stateList.add(integralState);
            stateList.add(prevError);
            stateList.add(filterState);
        }

        System.out.println("DTO-NATIVE: DiscretePIDController block created successfully - " + blockDto.getBlockName());
    }

    private static DiscretePIDControllerDto castToDiscretePIDControllerDto(BlockDto blockDto) {
        if (blockDto instanceof DiscretePIDControllerDto) {
            return (DiscretePIDControllerDto) blockDto;
        }
        throw new BlockCreationException("DTO type mismatch for block type 'DiscretePIDController': expected DiscretePIDControllerDto but got " +
                                       blockDto.getClass().getSimpleName() + ". Block name: " + blockDto.getBlockName());
    }

    /**
     * Helper method to get parameter or create default if null
     */
    private Parameter getParameterOrDefault(String paramName, int paramId, String defaultKey) {
        Parameter param = getParameterByName(paramName);
        if (param == null) {
            param = new Parameter(this, paramId, paramName, PARAMETER_DEFAULTS.get(defaultKey));
        }
        return param;
    }

    // === Static Factory Method for JSON Deserialization ===
    public static DiscretePIDController fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter proportionalGain = createProportionalGainFromJSON(paramValues, blockName);
            Parameter integralGain = createIntegralGainFromJSON(paramValues, blockName);
            Parameter derivativeGain = createDerivativeGainFromJSON(paramValues, blockName);
            Parameter filterCoefficient = createFilterCoefficientFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter initialConditionForIntegrator = createInitialConditionForIntegratorFromJSON(paramValues, blockName);
            Parameter initialConditionForFilter = createInitialConditionForFilterFromJSON(paramValues, blockName);

            DiscretePIDController block = new DiscretePIDController(proportionalGain, integralGain, derivativeGain,
                                                                     filterCoefficient, sampleTime,
                                                                     initialConditionForIntegrator, initialConditionForFilter,
                                                                     blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, proportionalGain, integralGain, derivativeGain,
                                     filterCoefficient, sampleTime,
                                     initialConditionForIntegrator, initialConditionForFilter);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DiscretePIDController block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static DiscretePIDController create(String name, String path, double P, double I, double D, double sampleTime, NCSLabModel model) {
        return create(name, path, P, I, D, 100.0, sampleTime, 0.0, 0.0, model);
    }

    /**
     * Create a DiscretePIDController block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param P Proportional gain
     * @param I Integral gain
     * @param D Derivative gain
     * @param N Filter coefficient for derivative term
     * @param sampleTime Sample time for discrete operation (-1 for inherited, >0 for discrete)
     * @param initialConditionForIntegrator Initial condition for integrator
     * @param initialConditionForFilter Initial condition for filter
     * @param model Parent model
     * @return DiscretePIDController block instance
     */
    public static DiscretePIDController create(String name, String path, double P, double I, double D, double N,
                                               double sampleTime, double initialConditionForIntegrator,
                                               double initialConditionForFilter, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DiscretePIDControllerDto dto = DiscretePIDControllerDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .P(com.ncslab.dto.common.TypedParameter.of(P))
            .I(com.ncslab.dto.common.TypedParameter.of(I))
            .D(com.ncslab.dto.common.TypedParameter.of(D))
            .N(com.ncslab.dto.common.TypedParameter.of(N))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .initialConditionForIntegrator(com.ncslab.dto.common.TypedParameter.of(initialConditionForIntegrator))
            .initialConditionForFilter(com.ncslab.dto.common.TypedParameter.of(initialConditionForFilter))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid DiscretePIDController parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new DiscretePIDController(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter filterCoefficient, Parameter sampleTime) {
        double filterValue = filterCoefficient.getDouble();
        if (filterValue <= 0.0 || Double.isNaN(filterValue) || Double.isInfinite(filterValue)) {
            throw new IllegalArgumentException("Filter coefficient (N) must be positive and finite");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createProportionalGainFromJSON(JSONObject paramValues, String blockName) {
        String pValue = paramValues.optString("P", "1");
        return new Parameter(null, 1, "P", pValue);
    }

    private static Parameter createIntegralGainFromJSON(JSONObject paramValues, String blockName) {
        String iValue = paramValues.optString("I", "1");
        return new Parameter(null, 2, "I", iValue);
    }

    private static Parameter createDerivativeGainFromJSON(JSONObject paramValues, String blockName) {
        String dValue = paramValues.optString("D", "0");
        return new Parameter(null, 3, "D", dValue);
    }

    private static Parameter createFilterCoefficientFromJSON(JSONObject paramValues, String blockName) {
        String nValue = paramValues.optString("N", "100");
        return new Parameter(null, 4, "N", nValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 5, "SampleTime", sampleTimeValue);
    }

    private static Parameter createInitialConditionForIntegratorFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialConditionForIntegrator", "0");
        return new Parameter(null, 6, "InitialConditionForIntegrator", initialConditionValue);
    }

    private static Parameter createInitialConditionForFilterFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialConditionForFilter", "0");
        return new Parameter(null, 7, "InitialConditionForFilter", initialConditionValue);
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

    private static void setParameterBlockReference(DiscretePIDController block, Parameter... parameters) {
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
        identity.put("blockType", "DiscretePIDController");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to satisfy base constructor
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Main input port
        input = new InputPort(this, 1);
        inputPortList.add(input);

        // Main output port (feedthrough = true for discrete PID)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }

    // === State Initialization ===
    private void initializeStates() {
        integralState = new State(this, 1, "integralState", proportionalGain.getHeight(), proportionalGain.getWidth());
        prevError = new State(this, 2, "prevError", proportionalGain.getHeight(), proportionalGain.getWidth());
        filterState = new State(this, 3, "filterState", proportionalGain.getHeight(), proportionalGain.getWidth());
        stateList.add(integralState);
        stateList.add(prevError);
        stateList.add(filterState);
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        Data data = new Data(initialConditionForIntegrator.getData().getMatrix());
        integralState.setData(data);
        prevError.setData(new Data(proportionalGain.getHeight(), proportionalGain.getWidth())); // Initialize to zero
        filterState.setData(new Data(initialConditionForFilter.getData().getMatrix()));

        out.setData(data);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data currentState;

        // Validate input data
        if (inputPortList.isEmpty() || inputPortList.get(0).getData() == null) {
            // No input data available, use initial conditions
            out.setData(new Data(initialConditionForIntegrator.getData().getInitValue()));
            return;
        }

        Data inputData = inputPortList.get(0).getData();

        if (proportionalGain.getDataType() == DataType.REAL && integralState.getDataType() == DataType.REAL) {
            // Scalar case
            double error = inputData.getInitValue();
            double pGain = proportionalGain.getData().getInitValue();
            double integral = integralState.getData().getInitValue();
            double filter = filterState.getData().getInitValue();

            // Discrete PID output: u(k) = P*e(k) + I_sum(k) + Df(k)
            double output_val = pGain * error + integral + filter;

            currentState = new Data(output_val);
        } else {
            // Matrix case
            int height = integralState.getHeight();
            int width = integralState.getWidth();
            currentState = new Data(height, width);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    double error = (inputData.getDataType() == DataType.MATRIX) ?
                        inputData.getMatrix().get(i, j) : inputData.getInitValue();
                    double pGain = proportionalGain.getData().getMatrix().get(i, j);
                    double integral = integralState.getData().getMatrix().get(i, j);
                    double filter = filterState.getData().getMatrix().get(i, j);

                    // Discrete PID output: u(k) = P*e(k) + I_sum(k) + Df(k)
                    double output_val = pGain * error + integral + filter;

                    currentState.getMatrix().set(i, j, output_val);
                }
            }
        }

        out.setData(currentState);
    }

    @Override
    public void calculateUpdate(double t) {
        // This method is called to update internal state before calculateOutput
        // For discrete PID, the state update happens in calculateDiscreteUpdate
        // This method is typically empty for discrete blocks
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort in = inputPortList.get(0);
        Data inputSignal = in.getData();

        double Ts = sampleTimeParam.getDouble();
        if (Ts == -1.0) {
            Ts = model.getConfig().getFixedStep();
        }

        if (proportionalGain.getDataType() == DataType.REAL && integralState.getDataType() == DataType.REAL) {
            // Scalar discrete update
            double error = inputSignal.getInitValue();
            double prevErr = prevError.getData().getInitValue();
            double iGain = integralGain.getData().getInitValue();
            double dGain = derivativeGain.getData().getInitValue();
            double nCoeff = filterCoefficient.getData().getInitValue();
            double integral = integralState.getData().getInitValue();
            double filter = filterState.getData().getInitValue();

            // Tustin (Trapezoidal) discretization for integral:
            // I_sum(k) = I_sum(k-1) + I*Ts*(e(k)+e(k-1))/2
            double newIntegral = integral + iGain * Ts * (error + prevErr) / 2.0;

            // Filtered derivative using Tustin:
            // Df(k) = (Df(k-1)*(2-N*Ts) + 2*N*D*(e(k)-e(k-1))) / (2+N*Ts)
            double newFilter = (filter * (2.0 - nCoeff * Ts) + 2.0 * nCoeff * dGain * (error - prevErr)) / (2.0 + nCoeff * Ts);

            // Update states
            integralState.setData(new Data(newIntegral));
            prevError.setData(new Data(error));
            filterState.setData(new Data(newFilter));
        } else {
            // Matrix discrete update
            int height = integralState.getHeight();
            int width = integralState.getWidth();

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    double error = (inputSignal.getDataType() == DataType.MATRIX) ?
                        inputSignal.getMatrix().get(i, j) : inputSignal.getInitValue();
                    double prevErr = prevError.getData().getMatrix().get(i, j);
                    double iGain = integralGain.getData().getMatrix().get(i, j);
                    double dGain = derivativeGain.getData().getMatrix().get(i, j);
                    double nCoeff = filterCoefficient.getData().getMatrix().get(i, j);
                    double integral = integralState.getData().getMatrix().get(i, j);
                    double filter = filterState.getData().getMatrix().get(i, j);

                    // Tustin discretization
                    double newIntegral = integral + iGain * Ts * (error + prevErr) / 2.0;
                    double newFilter = (filter * (2.0 - nCoeff * Ts) + 2.0 * nCoeff * dGain * (error - prevErr)) / (2.0 + nCoeff * Ts);

                    // Update states
                    integralState.getData().getMatrix().set(i, j, newIntegral);
                    prevError.getData().getMatrix().set(i, j, error);
                    filterState.getData().getMatrix().set(i, j, newFilter);
                }
            }
        }
    }

    public void generateArraysCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Computed values (keep)
        context.put("signal", signal);

        String codeStr = TemplateManager.renderTemplate("c/discrete/DiscretePIDController/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Computed values (keep)
        context.put("integralState", integralState);
        context.put("prevError", prevError);
        context.put("filterState", filterState);

        String codeStr = TemplateManager.renderTemplate("c/discrete/DiscretePIDController/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Get the actual signal data object for dataType checking (computed - keep)
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Ensure all required template variables are set with non-null values (computed - keep)
        String outputVarName = getOutputPortVariable(0);
        String inputVarName = signal.getName();

        // Computed values (keep)
        context.put("out", outputVarName);
        context.put("outputSignal", outputVarName);
        context.put("signal", signal);
        context.put("signalName", inputVarName);
        context.put("integralState", integralState);
        context.put("prevError", prevError);
        context.put("filterState", filterState);
        context.put("integralStateName", integralState.getName());
        context.put("prevErrorName", prevError.getName());
        context.put("filterStateName", filterState.getName());
        context.put("PName", context.get(proportionalGain.getLocalName()));
        context.put("IName", context.get(integralGain.getLocalName()));
        context.put("DName", context.get(derivativeGain.getLocalName()));
        context.put("NName", context.get(filterCoefficient.getLocalName()));
        context.put("sampleTimeName", context.get(sampleTimeParam.getLocalName()));

        // Add missing dimension variables for matrix operations (computed - keep)
        context.put("PHeight", proportionalGain != null ? proportionalGain.getHeight() : 1);
        context.put("PWidth", proportionalGain != null ? proportionalGain.getWidth() : 1);
        context.put("realDataType", com.ncslab.block.data.DataType.REAL);
        context.put("matrixDataType", com.ncslab.block.data.DataType.MATRIX);

        String codeStr = TemplateManager.renderTemplate("c/discrete/DiscretePIDController/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        // Discrete blocks don't have derivatives - leave empty
    }

    public void generateUpdateCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Get signal reference
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("signal", signal);
        context.put("signalName", signal.getName());
        context.put("integralState", integralState);
        context.put("prevError", prevError);
        context.put("filterState", filterState);
        context.put("integralStateName", integralState.getName());
        context.put("prevErrorName", prevError.getName());
        context.put("filterStateName", filterState.getName());
        context.put("PHeight", proportionalGain.getHeight());
        context.put("PWidth", proportionalGain.getWidth());

        String codeStr = TemplateManager.renderTemplate("c/discrete/DiscretePIDController/update.vm", context);
        code.addUpdateCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal.getDataType() == DataType.REAL) {
            integralState = new State(this, 1, "integralState", proportionalGain.getHeight(), proportionalGain.getWidth());
            prevError = new State(this, 2, "prevError", proportionalGain.getHeight(), proportionalGain.getWidth());
            filterState = new State(this, 3, "filterState", proportionalGain.getHeight(), proportionalGain.getWidth());
        } else {
            integralState = new State(this, 1, "integralState", signal.getHeight(), signal.getWidth());
            prevError = new State(this, 2, "prevError", signal.getHeight(), signal.getWidth());
            filterState = new State(this, 3, "filterState", signal.getHeight(), signal.getWidth());
        }
        stateList.clear();
        stateList.add(integralState);
        stateList.add(prevError);
        stateList.add(filterState);

        if (!isSampleTimeMultiple(sampleTimeParam.getDouble(), model.getConfig().getFixedStep())) {
            MatDimException e = new MatDimException("Parameter(SampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        // Check that all PID parameters have the same dimensions
        if (proportionalGain.getWidth() != derivativeGain.getWidth() ||
           proportionalGain.getWidth() != integralGain.getWidth() ||
           proportionalGain.getWidth() != filterCoefficient.getWidth() ||
           proportionalGain.getHeight() != derivativeGain.getHeight() ||
           proportionalGain.getHeight() != integralGain.getHeight() ||
           proportionalGain.getHeight() != filterCoefficient.getHeight()) {
            throw new MatDimException("Block " + this.blockName + " PID parameters dimension don't match! All PID parameters must have the same dimensions!\n \n");
        }

        // Set output dimensions based on parameter/signal dimensions
        if (proportionalGain.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
            out.setHeight(proportionalGain.getHeight());
            out.setWidth(proportionalGain.getWidth());
            out.getOutputSignalC().setHeight(proportionalGain.getHeight());
            out.getOutputSignalC().setWidth(proportionalGain.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else if (proportionalGain.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            if (proportionalGain.getWidth() != signal.getWidth() || proportionalGain.getHeight() != signal.getHeight()) {
                throw new MatDimException("Block " + this.blockName + " input dimension doesn't match the proportional gain dimension!\n \n");
            }
            out.setHeight(proportionalGain.getHeight());
            out.setWidth(proportionalGain.getWidth());
            out.getOutputSignalC().setHeight(proportionalGain.getHeight());
            out.getOutputSignalC().setWidth(proportionalGain.getWidth());
            out.getOutputSignalC().setDataType(proportionalGain.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        if (sampleTimeParam.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(SampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }
    }
}
