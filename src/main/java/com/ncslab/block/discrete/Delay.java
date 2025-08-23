package com.ncslab.block.discrete;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.DelayDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
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
 * Delay block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - DelayLength: Number of samples to delay
 * - InitialCondition: Initial condition for the delay buffer
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Delay extends DiscreteBlock {

    // === Internal State ===
    private List<Data> buffer;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter delayLength;
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
        PARAMETER_DEFAULTS.put("DelayLength", "1");
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    // === Private Constructor with Typed Parameters ===
    private Delay(Parameter delayLength, Parameter initialCondition, Parameter sampleTime,
                 Parameter outDataType, Parameter saturateOnIntegerOverflow,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(delayLength, sampleTime);

        // Assign parameters
        this.delayLength = Objects.requireNonNull(delayLength, "Delay length parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Delay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.sampleTimeParam = new Parameter(this, 1, "SampleTime", paramValues.getString("SampleTime"));
        this.initialCondition = new Parameter(this, 2, "InitialCondition", paramValues.getString("InitialCondition"));
        this.delayLength = new Parameter(this, 3, "DelayLength", paramValues.getString("DelayLength"));

        // Create missing SIMULINK parameters with defaults
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");

        // Add all parameters to parameter list

        // Set discrete sample time
        setSampleTime(sampleTimeParam);

        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates Delay block directly from DelayDto DTO
     */
    public Delay(DelayDto delayDto, NCSLabModel model) {
        super(delayDto, model);

        // Initialize final parameters from DelayDto
        this.delayLength = new Parameter(this, 1, "DelayLength", String.valueOf(delayDto.getDelayLengthValue()));
        this.initialCondition = new Parameter(this, 2, "InitialCondition", String.valueOf(delayDto.getInitialConditionValue()));
        this.sampleTimeParam = new Parameter(this, 3, "SampleTime", String.valueOf(delayDto.getSampleTimeValue()));
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", delayDto.getOutDataTypeStr().getValue(String.class));
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", delayDto.getSaturateOnIntegerOverflow().getValue(String.class));

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + delayDto.getBlockName());
    }
    
    /**
     * DTO-NATIVE Constructor - Creates Delay block directly from generic BlockDto DTO
     */
    public Delay(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO with defaults
        this.delayLength = new Parameter(this, 1, "DelayLength", "1");
        this.initialCondition = new Parameter(this, 2, "InitialCondition", "0");
        this.sampleTimeParam = new Parameter(this, 3, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Delay fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter delayLength = createDelayLengthFromJSON(paramValues, blockName);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Delay block = new Delay(delayLength, initialCondition, sampleTime, outDataType, saturateParam,
                                  blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, delayLength, initialCondition, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Delay block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Delay create(String name, String path, int delayLength, double initialCondition, double sampleTime, NCSLabModel model) {
        return create(name, path, delayLength, initialCondition, sampleTime, "Inherit: Same as input", false, model);
    }

    public static Delay create(String name, String path, int delayLength, double initialCondition, double sampleTime,
                              String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter delayLengthParam = new Parameter(null, 1, "DelayLength", String.valueOf(delayLength));
        Parameter initialConditionParam = new Parameter(null, 2, "InitialCondition", String.valueOf(initialCondition));
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        Delay block = new Delay(delayLengthParam, initialConditionParam, sampleTimeParam, outDataTypeParam, saturateParam,
                              name, path, "null", model);

        setParameterBlockReference(block, delayLengthParam, initialConditionParam, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter delayLength, Parameter sampleTime) {
        int delayLengthValue = (int) delayLength.getDouble();
        if (delayLengthValue <= 0) {
            throw new IllegalArgumentException("Delay length must be positive");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be positive and finite");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createDelayLengthFromJSON(JSONObject paramValues, String blockName) {
        String delayLengthValue = paramValues.optString("DelayLength", "1");
        return new Parameter(null, 1, "DelayLength", delayLengthValue);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 2, "InitialCondition", initialConditionValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
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

    private static void setParameterBlockReference(Delay block, Parameter... parameters) {
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
        identity.put("blockType", "Delay");
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

        // Main output port (no feedthrough for delay)
        output = new OutputPort(this, 1, false);
        outputPortList.add(output);
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        context.put("block", this);
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("signal", signal);
        context.put("signalHeight", signal.getHeight());
        context.put("signalWidth", signal.getWidth());
        context.put("delayLength", delayLength.getData().getIntValue());
        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("sampleTime", sampleTimeParam);
        context.put("initialCondition", initialCondition);
        context.put("delayLength", delayLength);

        String codeStr = TemplateManager.renderTemplate("m/discrete/Delay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("sampleTime", sampleTimeParam);
        context.put("sampleTimeName", sampleTimeParam.getName());
        context.put("initialCondition", initialCondition);
        context.put("initialConditionName", initialCondition.getName());
        context.put("delayLength", delayLength);
        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("sampleTime", sampleTimeParam);
        context.put("sampleTimeName", sampleTimeParam.getName());
        context.put("initialCondition", initialCondition);
        context.put("initialConditionName", initialCondition.getName());
        context.put("delayLength", delayLength.getData().getIntValue());
        context.put("inputSignal", getInputPortVariable(0));
        context.put("outputSignal", getOutputPortVariable(0));
        context.put("outputs", getOutputPortVariables());
        context.put("inputPortList", inputPortList);
        context.put("outputPortList", outputPortList);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/output.vm", context);
        code.addOutputCode(codeStr);
    }
}
