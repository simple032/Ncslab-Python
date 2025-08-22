package com.ncslab.block.continuous;

import com.ncslab.block.BlockType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.*;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.util.TemplateManager;
import java.util.HashMap;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;

import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Derivative block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - FilterCoefficient: Filter coefficient for filtered derivative (T in s/(Ts+1))
 * - InitialCondition: Initial condition for internal state
 * - CoefficientSource: Source of filter coefficient (internal, external)
 * - ExternalReset: External reset mode (none, rising, falling, either, level)
 * - InitialConditionSource: Source of initial condition (internal, external)
 * - ShowStatePort: Show state output port
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Note: Implements filtered derivative G=s/(Ts+1) where T->0 gives ideal derivative
 */
public class Derivative extends Block {

    // === Internal State ===
    private State stateIntegral;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter filterCoefficient;
    private final Parameter initialCondition;
    private final Parameter coefficientSource;
    private final Parameter externalReset;
    private final Parameter conditionSource;
    private final Parameter showStatePort;
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
        PARAMETER_DEFAULTS.put("FilterCoefficient", "1");
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
        PARAMETER_DEFAULTS.put("CoefficientSource", "internal");
        PARAMETER_DEFAULTS.put("ExternalReset", "none");
        PARAMETER_DEFAULTS.put("InitialConditionSource", "internal");
        PARAMETER_DEFAULTS.put("ShowStatePort", "off");
        PARAMETER_DEFAULTS.put("SampleTime", "0");  // Continuous
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    // === Private Constructor with Typed Parameters ===
    private Derivative(Parameter filterCoefficient, Parameter initialCondition, Parameter coefficientSource,
                      Parameter externalReset, Parameter conditionSource, Parameter showStatePort,
                      Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                      String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(filterCoefficient, sampleTime);

        // Assign parameters
        this.filterCoefficient = Objects.requireNonNull(filterCoefficient, "Filter coefficient parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.coefficientSource = Objects.requireNonNull(coefficientSource, "Coefficient source parameter cannot be null");
        this.externalReset = Objects.requireNonNull(externalReset, "External reset parameter cannot be null");
        this.conditionSource = Objects.requireNonNull(conditionSource, "Condition source parameter cannot be null");
        this.showStatePort = Objects.requireNonNull(showStatePort, "Show state port parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Derivative(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        // Legacy implementation used 'c' parameter with default value 100
        this.filterCoefficient = new Parameter(this, 1, "FilterCoefficient", "100");
        this.initialCondition = new Parameter(this, 2, "InitialCondition", "0");

        // Create missing SIMULINK parameters with defaults
        this.coefficientSource = new Parameter(this, 3, "CoefficientSource", "internal");
        this.externalReset = new Parameter(this, 4, "ExternalReset", "none");
        this.conditionSource = new Parameter(this, 5, "InitialConditionSource", "internal");
        this.showStatePort = new Parameter(this, 6, "ShowStatePort", "off");
        this.sampleTime = new Parameter(this, 7, "SampleTime", "0"); // 0 for continuous derivative
        this.outDataType = new Parameter(this, 8, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 9, "SaturateOnIntegerOverflow", "off");

        // Add all parameters to parameter list

        // Initialize ports based on legacy logic
        input = new InputPort(this, 1);
        inputPortList.add(input);
        output = new OutputPort(this, 1, true); // feedthrough = true for derivative
        outputPortList.add(output);
    }    /**
     * DTO-NATIVE Constructor - Creates Derivative block directly from BlockDto DTO
     */
    public Derivative(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Extract parameters from DTO using same names and defaults as JSON constructor
        JSONObject paramValues = new JSONObject();
        if (blockDto.getParamValues() != null) {
            paramValues = new JSONObject(blockDto.getParamValues());
        }
        
        // Initialize final parameters using exact same logic as JSON constructor
        this.filterCoefficient = new Parameter(this, 1, "FilterCoefficient", paramValues.optString("FilterCoefficient", "100"));
        this.initialCondition = new Parameter(this, 2, "InitialCondition", paramValues.optString("InitialCondition", "0"));
        this.coefficientSource = new Parameter(this, 3, "CoefficientSource", paramValues.optString("CoefficientSource", "internal"));
        this.externalReset = new Parameter(this, 4, "ExternalReset", paramValues.optString("ExternalReset", "none"));
        this.conditionSource = new Parameter(this, 5, "InitialConditionSource", paramValues.optString("InitialConditionSource", "internal"));
        this.showStatePort = new Parameter(this, 6, "ShowStatePort", paramValues.optString("ShowStatePort", "off"));
        this.sampleTime = new Parameter(this, 7, "SampleTime", paramValues.optString("SampleTime", "0"));
        this.outDataType = new Parameter(this, 8, "OutDataTypeStr", paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
        this.saturateOnIntegerOverflow = new Parameter(this, 9, "SaturateOnIntegerOverflow", paramValues.optString("SaturateOnIntegerOverflow", "off"));

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // ===== DUAL CONSTRUCTOR PATTERN - MIGRATION SUPPORT =====
    // This pattern maintains backward compatibility while enabling DTO migration

    /**
     * Enhanced DTO-based constructor - preferred for new implementations
     * @param dto The DTO containing block configuration
     * @param model The parent model
     */
    public Derivative(com.ncslab.dto.block.specialized.continuous.DerivativeDto dto, NCSLabModel model) {
        super(createBlockIdentity(dto.getBlockName(), dto.getBlockPath(), dto.getBlockUUID()), model);
        
        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }
        
        // Initialize from DTO parameters using new Parameter creation
        this.filterCoefficient = new Parameter(this, 1, "FilterCoefficient", String.valueOf(dto.getFilterCoefficientValue()));
        this.initialCondition = new Parameter(this, 2, "InitialCondition", String.valueOf(dto.getInitialConditionValue()));
        this.coefficientSource = new Parameter(this, 3, "CoefficientSource", dto.getCoefficientSourceValue());
        this.externalReset = new Parameter(this, 4, "ExternalReset", dto.getExternalResetValue());
        this.conditionSource = new Parameter(this, 5, "InitialConditionSource", dto.getInitialConditionSourceValue());
        this.showStatePort = new Parameter(this, 6, "ShowStatePort", dto.getShowStatePortValue() ? "on" : "off");
        this.sampleTime = new Parameter(this, 7, "SampleTime", String.valueOf(dto.getSampleTime()));
        this.outDataType = new Parameter(this, 8, "OutDataTypeStr", dto.getOutDataTypeStrValue());
        this.saturateOnIntegerOverflow = new Parameter(this, 9, "SaturateOnIntegerOverflow", dto.getSaturateOnIntegerOverflowValue() ? "on" : "off");
        
        // Execute initialization logic exactly like JSONObject constructor
        initializePorts();
        
        // Complete initialization
        System.out.println("Enhanced DTO: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Derivative fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter filterCoefficient = createFilterCoefficientFromJSON(paramValues, blockName);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter coefficientSource = createCoefficientSourceFromJSON(paramValues, blockName);
            Parameter externalReset = createExternalResetFromJSON(paramValues, blockName);
            Parameter conditionSource = createConditionSourceFromJSON(paramValues, blockName);
            Parameter showStatePort = createShowStatePortFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Derivative block = new Derivative(filterCoefficient, initialCondition, coefficientSource,
                                            externalReset, conditionSource, showStatePort,
                                            sampleTime, outDataType, saturateParam,
                                            blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, filterCoefficient, initialCondition, coefficientSource,
                                     externalReset, conditionSource, showStatePort,
                                     sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Derivative block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Derivative create(String name, String path, double filterCoefficient, NCSLabModel model) {
        return create(name, path, filterCoefficient, 0.0, "internal", "none", "internal",
                     false, 0.0, "Inherit: Same as input", false, model);
    }
    public static Derivative create(String name, String path, double filterCoefficient, double initialCondition,
                                   String coefficientSource, String externalReset, String conditionSource,
                                   boolean showStatePort, double sampleTime, String outDataType,
                                   boolean saturateOnOverflow, NCSLabModel model) {
        Parameter filterCoefficientParam = new Parameter(null, 1, "FilterCoefficient", String.valueOf(filterCoefficient));
        Parameter initialConditionParam = new Parameter(null, 2, "InitialCondition", String.valueOf(initialCondition));
        Parameter coefficientSourceParam = new Parameter(null, 3, "CoefficientSource", coefficientSource);
        Parameter externalResetParam = new Parameter(null, 4, "ExternalReset", externalReset);
        Parameter conditionSourceParam = new Parameter(null, 5, "InitialConditionSource", conditionSource);
        Parameter showStatePortParam = new Parameter(null, 6, "ShowStatePort", showStatePort ? "on" : "off");
        Parameter sampleTimeParam = new Parameter(null, 7, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 8, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 9, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        Derivative block = new Derivative(filterCoefficientParam, initialConditionParam, coefficientSourceParam,
                                        externalResetParam, conditionSourceParam, showStatePortParam,
                                        sampleTimeParam, outDataTypeParam, saturateParam,
                                        name, path, "null", model);

        setParameterBlockReference(block, filterCoefficientParam, initialConditionParam, coefficientSourceParam,
                                 externalResetParam, conditionSourceParam, showStatePortParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }
    // === Parameter Validation ===
    private static void validateParameters(Parameter filterCoefficient, Parameter sampleTime) {
        double filterValue = filterCoefficient.getDouble();
        if (filterValue <= 0.0 || Double.isNaN(filterValue) || Double.isInfinite(filterValue)) {
            throw new IllegalArgumentException("Filter coefficient must be positive and finite");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createFilterCoefficientFromJSON(JSONObject paramValues, String blockName) {
        String filterValue = paramValues.optString("FilterCoefficient", "100");
        return new Parameter(null, 1, "FilterCoefficient", filterValue);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0");
        return new Parameter(null, 2, "InitialCondition", initialConditionValue);
    }

    private static Parameter createCoefficientSourceFromJSON(JSONObject paramValues, String blockName) {
        String coefficientSourceValue = paramValues.optString("CoefficientSource", "internal");
        return new Parameter(null, 3, "CoefficientSource", coefficientSourceValue);
    }

    private static Parameter createExternalResetFromJSON(JSONObject paramValues, String blockName) {
        String externalResetValue = paramValues.optString("ExternalReset", "none");
        return new Parameter(null, 4, "ExternalReset", externalResetValue);
    }

    private static Parameter createConditionSourceFromJSON(JSONObject paramValues, String blockName) {
        String conditionSourceValue = paramValues.optString("InitialConditionSource", "internal");
        return new Parameter(null, 5, "InitialConditionSource", conditionSourceValue);
    }
    private static Parameter createShowStatePortFromJSON(JSONObject paramValues, String blockName) {
        String showStatePortValue = paramValues.optString("ShowStatePort", "off");
        return new Parameter(null, 6, "ShowStatePort", showStatePortValue);
    }
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 7, "SampleTime", sampleTimeValue);
    }
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 8, "OutDataTypeStr", outDataTypeValue);
    }
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 9, "SaturateOnIntegerOverflow", saturateValue);
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
    private static void setParameterBlockReference(Derivative block, Parameter... parameters) {
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
        identity.put("blockType", "Derivative");
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

        // Main output port (feedthrough = true for derivative)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);

        // Additional input ports based on settings
        String coeffSource = coefficientSource.getInitString();
        String resetMode = externalReset.getInitString();
        String icSource = conditionSource.getInitString();

        int nextInputPort = 2;

        if (coeffSource.equals("external")) {
            inputPortList.add(new InputPort(this, nextInputPort++)); // Filter coefficient port
            inputNames.add("fc");
        }
        if (!resetMode.equals("none")) {
            inputPortList.add(new InputPort(this, nextInputPort++)); // Reset port
            inputNames.add("reset");
        }
        if (icSource.equals("external")) {
            inputPortList.add(new InputPort(this, nextInputPort++)); // External IC port
            inputNames.add("IC0");
        }
        // Additional output ports
        if (showStatePort.getInitString().equals("on")) {
            outputPortList.add(new OutputPort(this, 2, false)); // State port
            outputNames.add("state");
        }
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("state", stateIntegral);

        String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/init.vm", context);
        code.addInitCode(codeStr);
    }
    public void generateArraysCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("signal", signal);
        String arraysCode = TemplateManager.renderTemplate("c/continuous/Derivative/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }
    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("state", stateIntegral);
        context.put("input", getInputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("output", getOutputPortVariables()[0]);
        context.put("state", stateIntegral);

        String codeStr = TemplateManager.renderTemplate("m/continuous/Derivative/output.vm", context);
        code.addOutputCode(codeStr);
    }
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("signal", signal);
        context.put("state", stateIntegral);
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("c/continuous/Derivative/init.vm", context);
        code.addInitCode(codeStr);
    }
    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("signal", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC());
        context.put("state", stateIntegral);
        context.put("solver", this.model.getConfig().getSolver());

        String codeStr = TemplateManager.renderTemplate("c/continuous/Derivative/output.vm", context);
        code.addOutputCode(codeStr);
    }
    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", stateList);
        context.put("inputs", getInputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/continuous/Derivative/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        switch (signal.getDataType()) {
            case REAL:
                out.setHeight(1);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(DataType.REAL);
                stateIntegral = new State(this, 1, "integral", 1, 1);
                break;
            case MATRIX:
                out.setHeight(signal.getHeight());
                out.setWidth(signal.getWidth());
                out.getOutputSignalC().setHeight(signal.getHeight());
                out.getOutputSignalC().setWidth(signal.getWidth());
                out.getOutputSignalC().setDataType(DataType.MATRIX);
                stateIntegral = new State(this, 1, "integral", signal.getHeight(), signal.getWidth());
                break;
        }
        stateList.add(stateIntegral);
    }
    public void checkDimension() throws MatDimException {
        // No special dimension checks needed for derivative block
    }
}
