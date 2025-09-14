package com.ncslab.block.continuous;

import com.ncslab.block.continuous.ContinuousBlock;
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
import com.ncslab.dto.block.specialized.continuous.PIDControllerDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * PIDController block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - P: Proportional gain
 * - I: Integral gain
 * - D: Derivative gain
 * - N: Filter coefficient for derivative term
 * - FormulationType: PID form (parallel, standard)
 * - ExternalReset: External reset mode (none, rising, falling, either, level)
 * - InitialConditionForIntegrator: Initial condition for integrator
 * - InitialConditionForFilter: Initial condition for filter
 * - LimitOutput: Whether to limit output values
 * - UpperSaturationLimit: Upper limit for output
 * - LowerSaturationLimit: Lower limit for output
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class PIDController extends ContinuousBlock {

    // === Internal State ===
    private State stateIntegral;
    private State stateFilter;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter proportionalGain;
    private final Parameter integralGain;
    private final Parameter derivativeGain;
    private final Parameter filterCoefficient;
    private final Parameter formulationType;
    private final Parameter externalReset;
    private final Parameter initialConditionForIntegrator;
    private final Parameter initialConditionForFilter;
    private final Parameter limitOutput;
    private final Parameter upperSaturationLimit;
    private final Parameter lowerSaturationLimit;
    private final Parameter sampleTime;
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
        PARAMETER_DEFAULTS.put("P", "1");                           // Proportional gain
        PARAMETER_DEFAULTS.put("I", "1");                           // Integral gain
        PARAMETER_DEFAULTS.put("D", "0");                           // Derivative gain
        PARAMETER_DEFAULTS.put("N", "100");                         // Filter coefficient
        PARAMETER_DEFAULTS.put("FormulationType", "Parallel");       // PID form
        PARAMETER_DEFAULTS.put("ExternalReset", "none");            // External reset mode
        PARAMETER_DEFAULTS.put("InitialConditionForIntegrator", "0"); // Initial condition for integrator
        PARAMETER_DEFAULTS.put("InitialConditionForFilter", "0");   // Initial condition for filter
        PARAMETER_DEFAULTS.put("LimitOutput", "off");               // Whether to limit output
        PARAMETER_DEFAULTS.put("UpperSaturationLimit", "inf");      // Upper saturation limit
        PARAMETER_DEFAULTS.put("LowerSaturationLimit", "-inf");     // Lower saturation limit
        PARAMETER_DEFAULTS.put("SampleTime", "0");                  // 0 for continuous
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
        
        // Output port defaults (PID has feedthrough)
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
    private PIDController(Parameter proportionalGain, Parameter integralGain, Parameter derivativeGain,
                         Parameter filterCoefficient, Parameter formulationType, Parameter externalReset,
                         Parameter initialConditionForIntegrator, Parameter initialConditionForFilter,
                         Parameter limitOutput, Parameter upperSaturationLimit, Parameter lowerSaturationLimit,
                         Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                         String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(proportionalGain, integralGain, derivativeGain, filterCoefficient, sampleTime);

        // Assign parameters
        this.proportionalGain = Objects.requireNonNull(proportionalGain, "Proportional gain parameter cannot be null");
        this.integralGain = Objects.requireNonNull(integralGain, "Integral gain parameter cannot be null");
        this.derivativeGain = Objects.requireNonNull(derivativeGain, "Derivative gain parameter cannot be null");
        this.filterCoefficient = Objects.requireNonNull(filterCoefficient, "Filter coefficient parameter cannot be null");
        this.formulationType = Objects.requireNonNull(formulationType, "Formulation type parameter cannot be null");
        this.externalReset = Objects.requireNonNull(externalReset, "External reset parameter cannot be null");
        this.initialConditionForIntegrator = Objects.requireNonNull(initialConditionForIntegrator, "Initial condition for integrator parameter cannot be null");
        this.initialConditionForFilter = Objects.requireNonNull(initialConditionForFilter, "Initial condition for filter parameter cannot be null");
        this.limitOutput = Objects.requireNonNull(limitOutput, "Limit output parameter cannot be null");
        this.upperSaturationLimit = Objects.requireNonNull(upperSaturationLimit, "Upper saturation limit parameter cannot be null");
        this.lowerSaturationLimit = Objects.requireNonNull(lowerSaturationLimit, "Lower saturation limit parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.proportionalGain);
        parameterList.add(this.integralGain);
        parameterList.add(this.derivativeGain);
        parameterList.add(this.filterCoefficient);
        parameterList.add(this.formulationType);
        parameterList.add(this.externalReset);
        parameterList.add(this.initialConditionForIntegrator);
        parameterList.add(this.initialConditionForFilter);
        parameterList.add(this.limitOutput);
        parameterList.add(this.upperSaturationLimit);
        parameterList.add(this.lowerSaturationLimit);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        // Initialize ports
        initializePorts();

        // Initialize states
        initializeStates();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public PIDController(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.proportionalGain = getParameterByName("P");
        this.integralGain = getParameterByName("I");
        this.derivativeGain = getParameterByName("D");
        this.filterCoefficient = getParameterByName("N");

        // Create missing SIMULINK parameters with defaults
        this.formulationType = getParameterByName("FormulationType");
        this.externalReset = getParameterByName("ExternalReset");
        this.initialConditionForIntegrator = getParameterByName("InitialConditionForIntegrator");
        this.initialConditionForFilter = getParameterByName("InitialConditionForFilter");
        this.limitOutput = getParameterByName("LimitOutput");

        // Handle saturation limits
        if (limitOutput.getInitString().equals("on")) {
            this.upperSaturationLimit = getParameterByName("UpperSaturationLimit");
            this.lowerSaturationLimit = getParameterByName("LowerSaturationLimit");
        } else {
            this.upperSaturationLimit = getParameterByName("UpperSaturationLimit");
            this.lowerSaturationLimit = getParameterByName("LowerSaturationLimit");
        }

        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.proportionalGain != null) parameterList.add(this.proportionalGain);
        if (this.integralGain != null) parameterList.add(this.integralGain);
        if (this.derivativeGain != null) parameterList.add(this.derivativeGain);
        if (this.filterCoefficient != null) parameterList.add(this.filterCoefficient);
        if (this.formulationType != null) parameterList.add(this.formulationType);
        if (this.externalReset != null) parameterList.add(this.externalReset);
        if (this.initialConditionForIntegrator != null) parameterList.add(this.initialConditionForIntegrator);
        if (this.initialConditionForFilter != null) parameterList.add(this.initialConditionForFilter);
        if (this.limitOutput != null) parameterList.add(this.limitOutput);
        if (this.upperSaturationLimit != null) parameterList.add(this.upperSaturationLimit);
        if (this.lowerSaturationLimit != null) parameterList.add(this.lowerSaturationLimit);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        // Initialize ports based on legacy logic
        input = new InputPort(this, 1);
        inputPortList.add(input);
        output = new OutputPort(this, 1, true); // feedthrough = true for PID
        outputPortList.add(output);

        // Add external reset port if enabled
        if (externalReset.getInitString().equals("on")) {
            inputPortList.add(new InputPort(this, 2));
            inputNames.add("reset");
        }

        // Initialize states
        stateIntegral = new State(this, 1, "stateIntegral", proportionalGain.getHeight(), proportionalGain.getWidth());
        stateFilter = new State(this, 2, "stateFilter", proportionalGain.getHeight(), proportionalGain.getWidth());
        stateList.add(stateIntegral);
        stateList.add(stateFilter);
    }    /**
     * DTO-NATIVE Constructor - Creates PIDController block directly from BlockDto DTO
     */
    public PIDController(PIDControllerDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO with proper null checking and correct names
        this.proportionalGain = getParameterOrDefault("P", 1, "P");
        this.integralGain = getParameterOrDefault("I", 2, "I");
        this.derivativeGain = getParameterOrDefault("D", 3, "D");
        this.filterCoefficient = getParameterOrDefault("N", 4, "N");
        this.formulationType = getParameterOrDefault("FormulationType", 5, "FormulationType");
        this.externalReset = getParameterOrDefault("ExternalReset", 6, "ExternalReset");
        this.initialConditionForIntegrator = getParameterOrDefault("InitialConditionForIntegrator", 7, "InitialConditionForIntegrator");
        this.initialConditionForFilter = getParameterOrDefault("InitialConditionForFilter", 8, "InitialConditionForFilter");
        this.limitOutput = getParameterOrDefault("LimitOutput", 9, "LimitOutput");
        this.upperSaturationLimit = getParameterOrDefault("UpperSaturationLimit", 10, "UpperSaturationLimit");
        this.lowerSaturationLimit = getParameterOrDefault("LowerSaturationLimit", 11, "LowerSaturationLimit");
        this.sampleTime = getParameterOrDefault("SampleTime", 12, "SampleTime");
        this.outDataType = getParameterOrDefault("OutDataTypeStr", 13, "OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterOrDefault("SaturateOnIntegerOverflow", 14, "SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.proportionalGain != null) parameterList.add(this.proportionalGain);
        if (this.integralGain != null) parameterList.add(this.integralGain);
        if (this.derivativeGain != null) parameterList.add(this.derivativeGain);
        if (this.filterCoefficient != null) parameterList.add(this.filterCoefficient);
        if (this.formulationType != null) parameterList.add(this.formulationType);
        if (this.externalReset != null) parameterList.add(this.externalReset);
        if (this.initialConditionForIntegrator != null) parameterList.add(this.initialConditionForIntegrator);
        if (this.initialConditionForFilter != null) parameterList.add(this.initialConditionForFilter);
        if (this.limitOutput != null) parameterList.add(this.limitOutput);
        if (this.upperSaturationLimit != null) parameterList.add(this.upperSaturationLimit);
        if (this.lowerSaturationLimit != null) parameterList.add(this.lowerSaturationLimit);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    

        // Initialize states
        if (proportionalGain != null) {
            stateIntegral = new State(this, 1, "stateIntegral", proportionalGain.getHeight(), proportionalGain.getWidth());
            stateFilter = new State(this, 2, "stateFilter", proportionalGain.getHeight(), proportionalGain.getWidth());
            stateList.add(stateIntegral);
            stateList.add(stateFilter);
        }
        
        System.out.println("DTO-NATIVE: PIDController block created successfully - " + blockDto.getBlockName());
    }
    
    /**
     * Generic DTO Constructor for factory compatibility
     */
    public PIDController(BlockDto blockDto, NCSLabModel model) {
        this(castToPIDControllerDto(blockDto), model);
    }
    
    private static PIDControllerDto castToPIDControllerDto(BlockDto blockDto) {
        if (blockDto instanceof PIDControllerDto) {
            return (PIDControllerDto) blockDto;
        }
        throw new BlockCreationException("DTO type mismatch for block type 'PIDController': expected PIDControllerDto but got " + 
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
    public static PIDController fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter formulationType = createFormulationTypeFromJSON(paramValues, blockName);
            Parameter externalReset = createExternalResetFromJSON(paramValues, blockName);
            Parameter initialConditionForIntegrator = createInitialConditionForIntegratorFromJSON(paramValues, blockName);
            Parameter initialConditionForFilter = createInitialConditionForFilterFromJSON(paramValues, blockName);
            Parameter limitOutput = createLimitOutputFromJSON(paramValues, blockName);
            Parameter upperSaturationLimit = createUpperSaturationLimitFromJSON(paramValues, blockName);
            Parameter lowerSaturationLimit = createLowerSaturationLimitFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            PIDController block = new PIDController(proportionalGain, integralGain, derivativeGain,
                                                   filterCoefficient, formulationType, externalReset,
                                                   initialConditionForIntegrator, initialConditionForFilter,
                                                   limitOutput, upperSaturationLimit, lowerSaturationLimit,
                                                   sampleTime, outDataType, saturateParam,
                                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, proportionalGain, integralGain, derivativeGain,
                                     filterCoefficient, formulationType, externalReset,
                                     initialConditionForIntegrator, initialConditionForFilter,
                                     limitOutput, upperSaturationLimit, lowerSaturationLimit,
                                     sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create PIDController block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static PIDController create(String name, String path, double P, double I, double D, NCSLabModel model) {
        return create(name, path, P, I, D, 100.0, "parallel", "none",
                     0.0, 0.0, false, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                     0.0, "Inherit: Same as input", false, model);
    }

    public static PIDController create(String name, String path, double P, double I, double D, double N,
                                      String formulationType, String externalReset,
                                      double initialConditionForIntegrator, double initialConditionForFilter,
                                      boolean limitOutput, double upperLimit, double lowerLimit,
                                      double sampleTime, String outDataType, boolean saturateOnOverflow,
                                      NCSLabModel model) {
        Parameter proportionalGainParam = new Parameter(null, 1, "P", String.valueOf(P));
        Parameter integralGainParam = new Parameter(null, 2, "I", String.valueOf(I));
        Parameter derivativeGainParam = new Parameter(null, 3, "D", String.valueOf(D));
        Parameter filterCoefficientParam = new Parameter(null, 4, "N", String.valueOf(N));
        Parameter formulationTypeParam = new Parameter(null, 5, "FormulationType", formulationType);
        Parameter externalResetParam = new Parameter(null, 6, "ExternalReset", externalReset);
        Parameter initialConditionForIntegratorParam = new Parameter(null, 7, "InitialConditionForIntegrator", String.valueOf(initialConditionForIntegrator));
        Parameter initialConditionForFilterParam = new Parameter(null, 8, "InitialConditionForFilter", String.valueOf(initialConditionForFilter));
        Parameter limitOutputParam = new Parameter(null, 9, "LimitOutput", limitOutput ? "on" : "off");
        Parameter upperSaturationLimitParam = new Parameter(null, 10, "UpperSaturationLimit", String.valueOf(upperLimit));
        Parameter lowerSaturationLimitParam = new Parameter(null, 11, "LowerSaturationLimit", String.valueOf(lowerLimit));
        Parameter sampleTimeParam = new Parameter(null, 12, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 13, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 14, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        PIDController block = new PIDController(proportionalGainParam, integralGainParam, derivativeGainParam,
                                               filterCoefficientParam, formulationTypeParam, externalResetParam,
                                               initialConditionForIntegratorParam, initialConditionForFilterParam,
                                               limitOutputParam, upperSaturationLimitParam, lowerSaturationLimitParam,
                                               sampleTimeParam, outDataTypeParam, saturateParam,
                                               name, path, "null", model);

        setParameterBlockReference(block, proportionalGainParam, integralGainParam, derivativeGainParam,
                                 filterCoefficientParam, formulationTypeParam, externalResetParam,
                                 initialConditionForIntegratorParam, initialConditionForFilterParam,
                                 limitOutputParam, upperSaturationLimitParam, lowerSaturationLimitParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter proportionalGain, Parameter integralGain,
                                         Parameter derivativeGain, Parameter filterCoefficient, Parameter sampleTime) {
        double filterValue = filterCoefficient.getDouble();
        if (filterValue <= 0.0 || Double.isNaN(filterValue) || Double.isInfinite(filterValue)) {
            throw new IllegalArgumentException("Filter coefficient (N) must be positive and finite");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
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

    private static Parameter createFormulationTypeFromJSON(JSONObject paramValues, String blockName) {
        String formulationTypeValue = paramValues.optString("FormulationType", "parallel");
        return new Parameter(null, 5, "FormulationType", formulationTypeValue);
    }

    private static Parameter createExternalResetFromJSON(JSONObject paramValues, String blockName) {
        String externalResetValue = paramValues.optString("externalReset", "none");
        return new Parameter(null, 6, "ExternalReset", externalResetValue);
    }

    private static Parameter createInitialConditionForIntegratorFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialConditionForIntegrator", "0");
        return new Parameter(null, 7, "InitialConditionForIntegrator", initialConditionValue);
    }

    private static Parameter createInitialConditionForFilterFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialConditionForFilter", "0");
        return new Parameter(null, 8, "InitialConditionForFilter", initialConditionValue);
    }

    private static Parameter createLimitOutputFromJSON(JSONObject paramValues, String blockName) {
        String limitOutputValue = paramValues.optString("LimitOutput", "off");
        return new Parameter(null, 9, "LimitOutput", limitOutputValue);
    }

    private static Parameter createUpperSaturationLimitFromJSON(JSONObject paramValues, String blockName) {
        String upperLimitValue = paramValues.optString("UpperSaturationLimit", "inf");
        return new Parameter(null, 10, "UpperSaturationLimit", upperLimitValue);
    }

    private static Parameter createLowerSaturationLimitFromJSON(JSONObject paramValues, String blockName) {
        String lowerLimitValue = paramValues.optString("LowerSaturationLimit", "-inf");
        return new Parameter(null, 11, "LowerSaturationLimit", lowerLimitValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("sampleTime", "0");
        return new Parameter(null, 12, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 13, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 14, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(PIDController block, Parameter... parameters) {
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
        identity.put("blockType", "PIDController");
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

        // Main output port (feedthrough = true for PID)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);

        // Additional input ports based on external reset
        if (!externalReset.getInitString().equals("none")) {
            inputPortList.add(new InputPort(this, 2)); // Reset port
            inputNames.add("reset");
        }
    }

    // === State Initialization ===
    private void initializeStates() {
        stateIntegral = new State(this, 1, "stateIntegral", proportionalGain.getHeight(), proportionalGain.getWidth());
        stateFilter = new State(this, 2, "stateFilter", proportionalGain.getHeight(), proportionalGain.getWidth());
        stateList.add(stateIntegral);
        stateList.add(stateFilter);
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        Data data = new Data(initialConditionForIntegrator.getData().getMatrix());
        stateIntegral.setData(data);
        stateFilter.setData(new Data(initialConditionForFilter.getData().getMatrix()));

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
        
        if (proportionalGain.getDataType() == DataType.REAL && stateIntegral.getDataType() == DataType.REAL) {
            // Scalar case with enhanced edge case handling
            double inputValue = inputData.getInitValue();
            double pGain = proportionalGain.getData().getInitValue();
            double integralState = stateIntegral.getData().getInitValue();
            double filterState = stateFilter.getData().getInitValue();
            double fCoeff = filterCoefficient.getData().getInitValue();
            
            // Handle NaN and infinite inputs
            if (Double.isNaN(inputValue) || Double.isInfinite(inputValue)) {
                inputValue = 0.0; // Safe fallback
            }
            
            // Handle NaN and infinite gains
            if (Double.isNaN(pGain) || Double.isInfinite(pGain)) {
                pGain = 0.0;
            }
            if (Double.isNaN(fCoeff) || Double.isInfinite(fCoeff)) {
                fCoeff = 0.0;
            }
            
            // Handle NaN states (reset to initial conditions)
            if (Double.isNaN(integralState)) {
                integralState = initialConditionForIntegrator.getData().getInitValue();
                if (Double.isNaN(integralState)) {
                    integralState = 0.0;
                }
                stateIntegral.setData(new Data(integralState));
            }
            if (Double.isNaN(filterState)) {
                filterState = initialConditionForFilter.getData().getInitValue();
                if (Double.isNaN(filterState)) {
                    filterState = 0.0;
                }
                stateFilter.setData(new Data(filterState));
            }

            // Calculate PID output: P + I + D
            double output = pGain * inputValue + integralState + filterState * fCoeff;
            
            // Handle external reset
            if (externalReset.getInitString().equals("on") && inputPortList.size() > 1) {
                Data resetData = inputPortList.get(1).getData();
                if (resetData != null && !resetData.equals(new Data(0))) {
                    stateIntegral.setData(new Data(initialConditionForIntegrator.getData().getInitValue()));
                    stateFilter.setData(new Data(initialConditionForFilter.getData().getInitValue()));
                    // Recalculate output after reset
                    output = pGain * inputValue + initialConditionForIntegrator.getData().getInitValue() + 
                            initialConditionForFilter.getData().getInitValue() * fCoeff;
                }
            }

            // Apply output saturation
            if (limitOutput.getInitString().equals("on")) {
                double upperLimit = upperSaturationLimit.getData().getInitValue();
                double lowerLimit = lowerSaturationLimit.getData().getInitValue();
                
                // Handle NaN limits
                if (Double.isNaN(upperLimit) || Double.isInfinite(upperLimit)) {
                    upperLimit = Double.POSITIVE_INFINITY;
                }
                if (Double.isNaN(lowerLimit) || Double.isInfinite(lowerLimit)) {
                    lowerLimit = Double.NEGATIVE_INFINITY;
                }
                
                if (output > upperLimit) {
                    output = upperLimit;
                } else if (output < lowerLimit) {
                    output = lowerLimit;
                }
            }
            
            // Handle final output NaN/Infinity
            if (Double.isNaN(output)) {
                output = initialConditionForIntegrator.getData().getInitValue();
                if (Double.isNaN(output)) {
                    output = 0.0;
                }
            }
            
            currentState = new Data(output);
        } else {
            // Matrix case with enhanced edge case handling
            int height = stateIntegral.getHeight();
            int width = stateIntegral.getWidth();
            currentState = new Data(height, width);
            
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    double inputValue = (inputData.getDataType() == DataType.MATRIX) ? 
                        inputData.getMatrix().get(i, j) : inputData.getInitValue();
                    double pGain = proportionalGain.getData().getMatrix().get(i, j);
                    double integralState = stateIntegral.getData().getMatrix().get(i, j);
                    double filterState = stateFilter.getData().getMatrix().get(i, j);
                    double fCoeff = filterCoefficient.getData().getMatrix().get(i, j);

                    // Handle NaN and infinite inputs
                    if (Double.isNaN(inputValue) || Double.isInfinite(inputValue)) {
                        inputValue = 0.0;
                    }
                    if (Double.isNaN(pGain) || Double.isInfinite(pGain)) {
                        pGain = 0.0;
                    }
                    if (Double.isNaN(fCoeff) || Double.isInfinite(fCoeff)) {
                        fCoeff = 0.0;
                    }
                    
                    // Handle NaN states
                    if (Double.isNaN(integralState)) {
                        integralState = initialConditionForIntegrator.getData().getMatrix().get(i, j);
                        if (Double.isNaN(integralState)) {
                            integralState = 0.0;
                        }
                        stateIntegral.getData().getMatrix().set(i, j, integralState);
                    }
                    if (Double.isNaN(filterState)) {
                        filterState = initialConditionForFilter.getData().getMatrix().get(i, j);
                        if (Double.isNaN(filterState)) {
                            filterState = 0.0;
                        }
                        stateFilter.getData().getMatrix().set(i, j, filterState);
                    }

                    // Calculate PID output
                    double output = pGain * inputValue + integralState + filterState * fCoeff;

                    // Handle external reset
                    if (externalReset.getInitString().equals("on") && inputPortList.size() > 1) {
                        Data resetData = inputPortList.get(1).getData();
                        if (resetData != null && resetData.getDataType() == DataType.MATRIX) {
                            double resetValue = resetData.getMatrix().get(i, j);
                            if (resetValue != 0) {
                                double resetIntegral = initialConditionForIntegrator.getData().getMatrix().get(i, j);
                                double resetFilter = initialConditionForFilter.getData().getMatrix().get(i, j);
                                stateIntegral.getData().getMatrix().set(i, j, resetIntegral);
                                stateFilter.getData().getMatrix().set(i, j, resetFilter);
                                output = pGain * inputValue + resetIntegral + resetFilter * fCoeff;
                            }
                        }
                    }

                    // Apply output saturation
                    if (limitOutput.getInitString().equals("on")) {
                        double upperLimit = upperSaturationLimit.getData().getMatrix().get(i, j);
                        double lowerLimit = lowerSaturationLimit.getData().getMatrix().get(i, j);
                        
                        if (Double.isNaN(upperLimit) || Double.isInfinite(upperLimit)) {
                            upperLimit = Double.POSITIVE_INFINITY;
                        }
                        if (Double.isNaN(lowerLimit) || Double.isInfinite(lowerLimit)) {
                            lowerLimit = Double.NEGATIVE_INFINITY;
                        }
                        
                        if (output > upperLimit) {
                            output = upperLimit;
                        } else if (output < lowerLimit) {
                            output = lowerLimit;
                        }
                    }
                    
                    // Handle final output NaN
                    if (Double.isNaN(output)) {
                        output = 0.0;
                    }

                    currentState.getMatrix().set(i, j, output);
                }
            }
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {
        Data integralDerivative;
        Data filterDerivative;

        if (proportionalGain.getDataType() == DataType.REAL && stateIntegral.getDataType() == DataType.REAL) {
            // Integral derivative: Ki * u(t)
            integralDerivative = integralGain.getData().times(inputPortList.get(0).getData());

            // Filter derivative: N * (Kd * u(t) - filter_state)
            filterDerivative = filterCoefficient.getData().times(
                derivativeGain.getData().times(inputPortList.get(0).getData()).minus(stateFilter.getData())
            );
        } else {
            integralDerivative = new Data(stateIntegral.getHeight(), stateIntegral.getWidth());
            filterDerivative = new Data(stateFilter.getHeight(), stateFilter.getWidth());

            for (int i = 0; i < stateIntegral.getHeight(); i++) {
                for (int j = 0; j < stateIntegral.getWidth(); j++) {
                    // Integral derivative: Ki * u(t)
                    double integralDeriv = integralGain.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j);
                    integralDerivative.getMatrix().set(i, j, integralDeriv);

                    // Filter derivative: N * (Kd * u(t) - filter_state)
                    double filterDeriv = filterCoefficient.getData().getMatrix().get(i, j) *
                        (derivativeGain.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j) -
                         stateFilter.getData().getMatrix().get(i, j));
                    filterDerivative.getMatrix().set(i, j, filterDeriv);
                }
            }
        }

        stateIntegral.setDerivateData(integralDerivative);
        stateFilter.setDerivateData(filterDerivative);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add PID-specific context variables for arrays template
        context.put("proportionalGainHeight", proportionalGain.getHeight());
        context.put("proportionalGainWidth", proportionalGain.getWidth());
        context.put("proportionalGainDataType", proportionalGain.getDataType());
        context.put("inputSignalDataType", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getDataType());
        context.put("inputSignalHeight", inputPortList.get(0).getHeight());
        context.put("inputSignalWidth", inputPortList.get(0).getWidth());
        
        String arraysCode = TemplateManager.renderTemplate("c/continuous/PIDController/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add missing template variables for init
        // Note: State name mappings (integralStateName, filterStateName) are now handled
        // automatically by TemplateUtils.populateAllContext() using local names
        context.put("integralStateHeight", stateIntegral.getHeight());
        context.put("integralStateWidth", stateIntegral.getWidth());
        context.put("filterStateHeight", stateFilter.getHeight());
        context.put("filterStateWidth", stateFilter.getWidth());
        context.put("realDataType", DataType.REAL);
        
        String codeStr = TemplateManager.renderTemplate("c/continuous/PIDController/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add PID-specific context variables
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("signal", signal.getName()); // Use signal name string, not object
        context.put("resetSig",
            inputPortList.size()>1?
            inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName():0
        );

        // Add missing template variables that aren't in populateAllContext
        context.put("outputVar", outputPortList.get(0).getOutputSignalC().getName());
        context.put("signalName", signal.getName()); // Already includes full block prefix like Block2_Output1
        context.put("signalDataType", signal.getDataType());
        context.put("signalHeight", signal.getHeight());
        context.put("signalWidth", signal.getWidth());

        // Note: PID-specific parameter names (proportionalGainName, integralGainName, etc.)
        // are now handled automatically by TemplateUtils.populateAllContext()

        context.put("proportionalGainHeight", proportionalGain.getHeight());
        context.put("proportionalGainWidth", proportionalGain.getWidth());
        context.put("proportionalGainDataType", proportionalGain.getDataType());

        // Note: State name mappings (stateIntegralName, stateFilterName) are now handled
        // automatically by TemplateUtils.populateAllContext() using local names

        // Add saturation limit name mappings
        context.put("upperSaturationLimitName", context.get("UpperSaturationLimit"));
        context.put("lowerSaturationLimitName", context.get("LowerSaturationLimit"));
        
        // Add reset signal name if external reset is enabled
        if (inputPortList.size() > 1) {
            context.put("resetSigName", inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        }

        String codeStr = TemplateManager.renderTemplate("c/continuous/PIDController/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        String derivativeCode = TemplateManager.renderTemplate("c/continuous/PIDController/derivative.vm", context);
        code.addDerivativeCode(derivativeCode);
    }

    @Override
    public void generateUpdateCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add missing template variables for update
        // Note: State name mappings (integralStateName, filterStateName) are now handled
        // automatically by TemplateUtils.populateAllContext() using local names
        context.put("integralStateDerivativeName", stateIntegral.getDerivativeName());
        context.put("filterStateDerivativeName", stateFilter.getDerivativeName());
        context.put("integralStateHeight", stateIntegral.getHeight());
        context.put("integralStateWidth", stateIntegral.getWidth());
        context.put("filterStateHeight", stateFilter.getHeight());
        context.put("filterStateWidth", stateFilter.getWidth());
        context.put("signalDataType", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getDataType());
        
        String updateCode = TemplateManager.renderTemplate("c/continuous/PIDController/update.vm", context);
        code.addUpdateCode(updateCode);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal.getDataType() != DataType.REAL) {
            stateIntegral = new State(this, 1, "stateIntegral", signal.getHeight(), signal.getWidth());
            stateFilter = new State(this, 2, "stateFilter", signal.getHeight(), signal.getWidth());
        }
        stateList.set(0, stateIntegral);
        stateList.set(1, stateFilter);

        // Check that all PID parameters have the same dimensions
        if (proportionalGain.getWidth() != derivativeGain.getWidth() ||
           proportionalGain.getWidth() != integralGain.getWidth() ||
           proportionalGain.getWidth() != filterCoefficient.getWidth() ||
           proportionalGain.getHeight() != derivativeGain.getHeight() ||
           proportionalGain.getHeight() != integralGain.getHeight() ||
           proportionalGain.getHeight() != filterCoefficient.getHeight()) {
            throw new MatDimException("Block " + this.blockName + " PID parameters dimension don't match! All PID parameters must have the same dimensions!\n \n");
        }

        // Check saturation limits if output limiting is enabled
        if (limitOutput.getInitString().equals("on")) {
            if (lowerSaturationLimit.getHeight() != proportionalGain.getHeight() ||
               upperSaturationLimit.getHeight() != proportionalGain.getHeight() ||
               lowerSaturationLimit.getWidth() != proportionalGain.getWidth() ||
               upperSaturationLimit.getWidth() != proportionalGain.getWidth()) {
                throw new MatDimException("Block " + this.blockName + " saturation limits dimension don't match PID parameters dimension!\n \n");
            }
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

    public void checkDimension() throws MatDimException {}
}
