package com.ncslab.block.continuous;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.continuous.IntegratorDto;

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
 * Integrator block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * Performs continuous-time integration using the fundamental theorem of calculus.
 * Supports initial conditions, external reset, and output saturation limits.
 * 
 * SIMULINK Parameters:
 * - InitialCondition: Initial output value at t=0
 * - ExternalReset: External reset mode (none, rising, falling, either, level, sampled level)
 * - InitialConditionSource: Source of initial condition (internal, external)
 * - LimitOutput: Whether to limit output values
 * - UpperSaturationLimit: Upper limit for output
 * - LowerSaturationLimit: Lower limit for output
 * - ShowSaturationPort: Show saturation status port
 * - ShowStatePort: Show state output port
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Integrator extends ContinuousBlock {
    
    // === Internal State ===
    /** Integration state variable maintaining the integral value */
    private State stateIntegral;
    
    // === Saturation Optimization ===
    /** Whether output saturation is enabled for performance optimization */
    private boolean saturationEnabled = false;
    
    /** Upper saturation limit value */
    private double upperLimit = Double.POSITIVE_INFINITY;
    
    /** Lower saturation limit value */
    private double lowerLimit = Double.NEGATIVE_INFINITY;
    
    // === SIMULINK-Compatible Parameters ===
    /** Initial condition parameter */
    private final Parameter initialCondition;
    
    /** External reset mode parameter */
    private final Parameter externalReset;
    
    /** Initial condition source parameter */
    private final Parameter conditionSource;
    
    /** Output limiting enable parameter */
    private final Parameter limitOutput;
    
    /** Upper saturation limit parameter */
    private final Parameter upperSaturationLimit;
    
    /** Lower saturation limit parameter */
    private final Parameter lowerSaturationLimit;
    
    /** Saturation port visibility parameter */
    private final Parameter showSaturationPort;
    
    /** State port visibility parameter */
    private final Parameter showStatePort;
    
    /** Sample time parameter */
    private final Parameter sampleTime;
    
    /** Output data type parameter */
    private final Parameter outDataType;
    
    /** Integer overflow handling parameter */
    private final Parameter saturateOnIntegerOverflow;
    
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
        PARAMETER_DEFAULTS.put("ExternalReset", "none");
        PARAMETER_DEFAULTS.put("InitialConditionSource", "internal");
        PARAMETER_DEFAULTS.put("LimitOutput", "off");
        PARAMETER_DEFAULTS.put("UpperSaturationLimit", "inf");
        PARAMETER_DEFAULTS.put("LowerSaturationLimit", "-inf");
        PARAMETER_DEFAULTS.put("ShowSaturationPort", "off");
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
    private Integrator(Parameter initialCondition, Parameter externalReset, Parameter conditionSource,
                      Parameter limitOutput, Parameter upperSaturationLimit, Parameter lowerSaturationLimit,
                      Parameter showSaturationPort, Parameter showStatePort,
                      Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                      String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(initialCondition, sampleTime, upperSaturationLimit, lowerSaturationLimit);
        
        // Assign parameters
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.externalReset = Objects.requireNonNull(externalReset, "External reset parameter cannot be null");
        this.conditionSource = Objects.requireNonNull(conditionSource, "Condition source parameter cannot be null");
        this.limitOutput = Objects.requireNonNull(limitOutput, "Limit output parameter cannot be null");
        this.upperSaturationLimit = Objects.requireNonNull(upperSaturationLimit, "Upper saturation limit parameter cannot be null");
        this.lowerSaturationLimit = Objects.requireNonNull(lowerSaturationLimit, "Lower saturation limit parameter cannot be null");
        this.showSaturationPort = Objects.requireNonNull(showSaturationPort, "Show saturation port parameter cannot be null");
        this.showStatePort = Objects.requireNonNull(showStatePort, "Show state port parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        
        // Parameters are automatically added to parameterList by parent Block class
        
        // Initialize ports
        initializePorts();
        
        // Pre-compute saturation settings for performance
        updateSaturationSettings();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Integrator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Use name-based parameter access instead of index-based
        this.initialCondition = getParameterByName("InitialCondition");
        this.externalReset = getParameterByName("ExternalReset");
        this.conditionSource = getParameterByName("InitialConditionSource");
        this.limitOutput = getParameterByName("LimitOutput");
        this.upperSaturationLimit = getParameterByName("UpperSaturationLimit");
        this.lowerSaturationLimit = getParameterByName("LowerSaturationLimit");
        this.showSaturationPort = getParameterByName("ShowSaturationPort");
        this.showStatePort = getParameterByName("ShowStatePort");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Initialize ports based on legacy logic
        input = new InputPort(this, 1);
        inputPortList.add(input);
        output = new OutputPort(this, 1, false);
        outputPortList.add(output);
        
        // Add additional input ports based on reset and condition source
        if (!externalReset.getInitString().equals("none") && conditionSource.getInitString().equals("external")) {
            inputPortList.add(new InputPort(this, 2)); // Reset port
            inputPortList.add(new InputPort(this, 3)); // External IC port
        } else if (!externalReset.getInitString().equals("none") && conditionSource.getInitString().equals("internal")
                || externalReset.getInitString().equals("none") && conditionSource.getInitString().equals("external")) {
            inputPortList.add(new InputPort(this, 2)); // Reset or External IC port
        }
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Integrator fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter externalReset = createExternalResetFromJSON(paramValues, blockName);
            Parameter conditionSource = createConditionSourceFromJSON(paramValues, blockName);
            Parameter limitOutput = createLimitOutputFromJSON(paramValues, blockName);
            Parameter upperSaturationLimit = createUpperSaturationLimitFromJSON(paramValues, blockName);
            Parameter lowerSaturationLimit = createLowerSaturationLimitFromJSON(paramValues, blockName);
            Parameter showSaturationPort = createShowSaturationPortFromJSON(paramValues, blockName);
            Parameter showStatePort = createShowStatePortFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Integrator block = new Integrator(initialCondition, externalReset, conditionSource,
                                             limitOutput, upperSaturationLimit, lowerSaturationLimit,
                                             showSaturationPort, showStatePort,
                                             sampleTime, outDataType, saturateParam,
                                             blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, initialCondition, externalReset, conditionSource,
                                     limitOutput, upperSaturationLimit, lowerSaturationLimit,
                                     showSaturationPort, showStatePort,
                                     sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getClass().getSimpleName() + " occurred";
            }
            throw new BlockCreationException("Failed to create Integrator block from JSON: " + errorMsg, e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Integrator create(String name, String path, double initialCondition, NCSLabModel model) {
        return create(name, path, initialCondition, "none", "internal", false, 
                     Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 
                     false, false, 0.0, "Inherit: Same as input", false, model);
    }
    
    public static Integrator create(String name, String path, double initialCondition,
                                   String externalReset, String conditionSource, boolean limitOutput,
                                   double upperLimit, double lowerLimit,
                                   boolean showSaturationPort, boolean showStatePort,
                                   double sampleTime, String outDataType, boolean saturateOnOverflow,
                                   NCSLabModel model) {
        Parameter initialConditionParam = new Parameter(null, 1, "InitialCondition", String.valueOf(initialCondition));
        Parameter externalResetParam = new Parameter(null, 2, "ExternalReset", externalReset);
        Parameter conditionSourceParam = new Parameter(null, 3, "InitialConditionSource", conditionSource);
        Parameter limitOutputParam = new Parameter(null, 4, "LimitOutput", limitOutput ? "on" : "off");
        Parameter upperSaturationLimitParam = new Parameter(null, 5, "UpperSaturationLimit", String.valueOf(upperLimit));
        Parameter lowerSaturationLimitParam = new Parameter(null, 6, "LowerSaturationLimit", String.valueOf(lowerLimit));
        Parameter showSaturationPortParam = new Parameter(null, 7, "ShowSaturationPort", showSaturationPort ? "on" : "off");
        Parameter showStatePortParam = new Parameter(null, 8, "ShowStatePort", showStatePort ? "on" : "off");
        Parameter sampleTimeParam = new Parameter(null, 9, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 10, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 11, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Integrator block = new Integrator(initialConditionParam, externalResetParam, conditionSourceParam,
                                         limitOutputParam, upperSaturationLimitParam, lowerSaturationLimitParam,
                                         showSaturationPortParam, showStatePortParam,
                                         sampleTimeParam, outDataTypeParam, saturateParam,
                                         name, path, "null", model);
        
        setParameterBlockReference(block, initialConditionParam, externalResetParam, conditionSourceParam,
                                 limitOutputParam, upperSaturationLimitParam, lowerSaturationLimitParam,
                                 showSaturationPortParam, showStatePortParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter initialCondition, Parameter sampleTime,
                                         Parameter upperLimit, Parameter lowerLimit) {
        try {
            double sampleTimeValue = sampleTime.getDouble();
            if (sampleTimeValue < -1.0 || Double.isNaN(sampleTimeValue) || sampleTimeValue == Double.POSITIVE_INFINITY) {
                throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited), got: " + sampleTimeValue);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Sample time parameter '" + sampleTime.getInitString() + "' is not a valid number", e);
        }
        
        // Validate saturation limits
        try {
            double upperLimitValue = upperLimit.getDouble();
            double lowerLimitValue = lowerLimit.getDouble();
            if (!Double.isInfinite(upperLimitValue) && !Double.isInfinite(lowerLimitValue)) {
                if (upperLimitValue <= lowerLimitValue) {
                    throw new IllegalArgumentException("Upper saturation limit must be greater than lower saturation limit");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Saturation limit parameters are not valid numbers: upper='" 
                + upperLimit.getInitString() + "', lower='" + lowerLimit.getInitString() + "'", e);
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0");
        return new Parameter(null, 1, "InitialCondition", initialConditionValue);
    }
    
    private static Parameter createExternalResetFromJSON(JSONObject paramValues, String blockName) {
        String externalResetValue = paramValues.optString("IntegratorExternalReset", "none");
        return new Parameter(null, 2, "ExternalReset", externalResetValue);
    }
    
    private static Parameter createConditionSourceFromJSON(JSONObject paramValues, String blockName) {
        String conditionSourceValue = paramValues.optString("InitialConditionSource", "internal");
        return new Parameter(null, 3, "InitialConditionSource", conditionSourceValue);
    }
    
    private static Parameter createLimitOutputFromJSON(JSONObject paramValues, String blockName) {
        String limitOutputValue = paramValues.optString("LimitOutput", "off");
        return new Parameter(null, 4, "LimitOutput", limitOutputValue);
    }
    
    private static Parameter createUpperSaturationLimitFromJSON(JSONObject paramValues, String blockName) {
        String upperLimitValue = paramValues.optString("UpperSaturationLimit", "inf");
        return new Parameter(null, 5, "UpperSaturationLimit", upperLimitValue);
    }
    
    private static Parameter createLowerSaturationLimitFromJSON(JSONObject paramValues, String blockName) {
        String lowerLimitValue = paramValues.optString("LowerSaturationLimit", "-inf");
        return new Parameter(null, 6, "LowerSaturationLimit", lowerLimitValue);
    }
    
    private static Parameter createShowSaturationPortFromJSON(JSONObject paramValues, String blockName) {
        String showSaturationPortValue = paramValues.optString("ShowSaturationPort", "off");
        return new Parameter(null, 7, "ShowSaturationPort", showSaturationPortValue);
    }
    
    private static Parameter createShowStatePortFromJSON(JSONObject paramValues, String blockName) {
        String showStatePortValue = paramValues.optString("ShowStatePort", "off");
        return new Parameter(null, 8, "ShowStatePort", showStatePortValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 9, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 10, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 11, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(Integrator block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
                // Update parameter name after setting block reference
                param.updateParameterName();
            } catch (Exception e) {
                // Fallback: parameter block reference will be null, but should work for basic operations
            }
        }
    }
    
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Integrator");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
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
        
        // Additional input ports based on reset and condition source
        String resetMode = externalReset.getInitString();
        String icSource = conditionSource.getInitString();
        
        if (!resetMode.equals("none") && icSource.equals("external")) {
            inputPortList.add(new InputPort(this, 2)); // Reset port
            inputPortList.add(new InputPort(this, 3)); // External IC port
            inputNames.add("reset");
            inputNames.add("IC0");
        } else if (!resetMode.equals("none") && icSource.equals("internal")) {
            inputPortList.add(new InputPort(this, 2)); // Reset port only
            inputNames.add("reset");
        } else if (resetMode.equals("none") && icSource.equals("external")) {
            inputPortList.add(new InputPort(this, 2)); // External IC port only
            inputNames.add("IC0");
        }
        
        // Additional output ports
        if (showStatePort.getInitString().equals("on")) {
            outputPortList.add(new OutputPort(this, 2, false)); // State port
            outputNames.add("state");
        }
        
        if (showSaturationPort.getInitString().equals("on")) {
            int portNum = showStatePort.getInitString().equals("on") ? 3 : 2;
            outputPortList.add(new OutputPort(this, portNum, false)); // Saturation port
            outputNames.add("saturation");
        }
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("state", stateIntegral);
        context.put("initialCondition", initialCondition);

        String codeStr = TemplateManager.renderTemplate("m/continuous/Integrator/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("state", stateIntegral);
        context.put("input", getInputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("m/continuous/Integrator/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("externalReset", externalReset.getData().getInitString());
        context.put("conditionSource", conditionSource.getData().getInitString());
        String arraysCode = TemplateManager.renderTemplate("c/continuous/Integrator/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("state", stateIntegral);
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("m/continuous/Integrator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("externalReset", externalReset.getData().getInitString());
        context.put("conditionSource", conditionSource.getData().getInitString());
        // Use proper C variable name instead of Java object reference
        InputPort inputPort;
        context.put("signal", getInputPortVariable(0));
        context.put("signalName", getInputPortVariable(0));
        context.put("state", stateIntegral.getName());
        context.put("initialCondition", initialCondition.getData().getInitString());
        context.put("InitialCondition", initialCondition.getData().getInitString());
        if (conditionSource.getInitString().equals("external")) {
            if (externalReset.getInitString().equals("none")) {
                inputPort = inputPortList.get(1);
            } else {
                inputPort = inputPortList.get(2);
            }
            context.put("input", inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        }
        String initCode = TemplateManager.renderTemplate("c/continuous/Integrator/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("externalReset", externalReset.getData().getInitString());
        context.put("conditionSource", conditionSource.getData().getInitString());
        context.put("state", stateIntegral.getName());
        context.put("outputs", getOutputPortVariables());
        String codeStr = TemplateManager.renderTemplate("c/continuous/Integrator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("externalReset", externalReset.getData().getInitString());
        context.put("conditionSource", conditionSource.getData().getInitString());
        context.put("state", stateIntegral.getName());
        context.put("stateDerivative", stateIntegral.getDerivativeName());
        context.put("inputSignal", getInputPortVariable(0));
        context.put("inputs", getInputPortVariables());
        
        // Add dimension variables for template loops
        if (stateIntegral.getHeight() > 1 || stateIntegral.getWidth() > 1) {
            context.put("signalHeight", stateIntegral.getHeight());
            context.put("signalWidth", stateIntegral.getWidth());
        }
        if (initialCondition.getHeight() > 1 || initialCondition.getWidth() > 1) {
            context.put("initialConditionHeight", initialCondition.getHeight());
            context.put("initialConditionWidth", initialCondition.getWidth());
        }
        
        String codeStr = TemplateManager.renderTemplate("c/continuous/Integrator/derivative.vm", context);
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
                        stateIntegral = new State(this, 1, "integral", 1, 1);
                        break;
                    case MATRIX:
                        out.setHeight(initialCondition.getHeight());
                        out.setWidth(initialCondition.getWidth());
                        out.getOutputSignalC().setHeight(initialCondition.getHeight());
                        out.getOutputSignalC().setWidth(initialCondition.getWidth());
                        out.getOutputSignalC().setDataType(DataType.MATRIX);
                        stateIntegral = new State(this, 1, "integral", initialCondition.getHeight(), initialCondition.getWidth());
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
                        stateIntegral = new State(this, 1, "integral", signal.getHeight(), signal.getWidth());
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
                break;
        }
        stateList.add(stateIntegral);
    }    /**
     * DTO-NATIVE Constructor - Creates Integrator block directly from BlockDto DTO
     */
    public Integrator(IntegratorDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.initialCondition = getParameterByName("InitialCondition");
        this.externalReset = getParameterByName("ExternalReset");
        this.conditionSource = getParameterByName("InitialConditionSource");
        this.limitOutput = getParameterByName("LimitOutput");
        this.upperSaturationLimit = getParameterByName("UpperSaturationLimit");
        this.lowerSaturationLimit = getParameterByName("LowerSaturationLimit");
        this.showSaturationPort = getParameterByName("ShowSaturationPort");
        this.showStatePort = getParameterByName("ShowStatePort");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

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
        stateIntegral.setData(initialCondition.getData());
        output.setData(stateIntegral.getData());
    }

    @Override
    public void calculateDerivative(double t) {
        InputPort inputPort = inputPortList.get(0);
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        stateIntegral.setDerivateData(signal.getData());
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort output = outputPortList.get(0);
        Data outputData = stateIntegral.getData();
        
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
        } else {
            // No saturation, but still handle NaN/Infinity
            if (outputData.getDataType() == DataType.REAL) {
                double value = outputData.getInitValue();
                
                if (Double.isNaN(value)) {
                    value = initialCondition.getData().getInitValue();
                    if (Double.isNaN(value)) {
                        value = 0.0;
                    }
                    outputData = new Data(value);
                } else if (Double.isInfinite(value)) {
                    // Keep infinity but could be clamped in future if needed
                    // For now, pass through to maintain SIMULINK compatibility
                }
            } else {
                // Matrix case without saturation but with NaN handling
                int height = outputData.getHeight();
                int width = outputData.getWidth();
                boolean needsCleaning = false;
                
                // Check if any element needs cleaning
                for (int i = 0; i < height && !needsCleaning; i++) {
                    for (int j = 0; j < width && !needsCleaning; j++) {
                        double value = outputData.getMatrix().get(i, j);
                        if (Double.isNaN(value)) {
                            needsCleaning = true;
                        }
                    }
                }
                
                if (needsCleaning) {
                    Data cleanedData = new Data(height, width);
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < width; j++) {
                            double value = outputData.getMatrix().get(i, j);
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
                            }
                            cleanedData.getMatrix().set(i, j, value);
                        }
                    }
                    outputData = cleanedData;
                }
            }
        }
        
        output.setData(outputData);
    }
    
    // === Saturation Optimization Methods ===
    private void updateSaturationSettings() {
        saturationEnabled = limitOutput.getInitString().equals("on");
        if (saturationEnabled) {
            upperLimit = upperSaturationLimit.getDouble();
            lowerLimit = lowerSaturationLimit.getDouble();
        }
    }
    
    private Data applySaturation(Data data) {
        if (!saturationEnabled || data.getDataType() != DataType.REAL) {
            return data;
        }
        
        double value = data.getInitValue();
        if (value > upperLimit) {
            return new Data(upperLimit);
        } else if (value < lowerLimit) {
            return new Data(lowerLimit);
        }
        return data; // Return original if no saturation needed
    }
}