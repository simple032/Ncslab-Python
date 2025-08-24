package com.ncslab.block.discrete;

import Jama.Matrix;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.UnitDelayDto;

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
 * UnitDelay block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - InitialCondition: Initial condition for the delay
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class UnitDelay extends DiscreteBlock {

    // === Internal State ===
    private Data currentValue;
    private Data previousValue;
    private final boolean feedthrough = false; // Unit delay has no feedthrough

    // === SIMULINK-Compatible Parameters ===
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
    private UnitDelay(Parameter initialCondition, Parameter sampleTimeParam, Parameter outDataType, 
                     Parameter saturateOnIntegerOverflow, String blockName, String blockPath, 
                     String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(sampleTimeParam);

        // Assign parameters
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTimeParam, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public UnitDelay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Set discrete sample time
        setSampleTime(sampleTimeParam);

        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates UnitDelay block directly from BlockDto DTO
     */
    public UnitDelay(UnitDelayDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.initialCondition = new Parameter(this, 1, "Initialcondition", "0");
        this.sampleTimeParam = new Parameter(this, 2, "Sampletimeparam", "0");
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static UnitDelay fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            UnitDelay block = new UnitDelay(initialCondition, sampleTime, outDataType, saturateParam,
                                           blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, initialCondition, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create UnitDelay block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static UnitDelay create(String name, String path, double initialCondition, double sampleTime, NCSLabModel model) {
        return create(name, path, initialCondition, sampleTime, "Inherit: Same as input", false, model);
    }

    public static UnitDelay create(String name, String path, double initialCondition, double sampleTime,
                                  String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter initialConditionParam = new Parameter(null, 1, "InitialCondition", String.valueOf(initialCondition));
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        UnitDelay block = new UnitDelay(initialConditionParam, sampleTimeParam, outDataTypeParam, saturateParam,
                                       name, path, "null", model);

        setParameterBlockReference(block, initialConditionParam, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be positive and finite");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 1, "InitialCondition", initialConditionValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 3, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 4, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(UnitDelay block, Parameter... parameters) {
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
        identity.put("blockType", "UnitDelay");
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

        // Main output port (no feedthrough for unit delay)
        output = new OutputPort(this, 1, feedthrough);
        outputPortList.add(output);
    }

    // === Helper Methods ===
    private int[] calculateSignalIndices(int height, int width) {
        int[] indices = new int[height * width];
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                indices[index++] = i * width + j;
            }
        }
        return indices;
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        // Use proper C variable name instead of Java object reference
        context.put("signal", getInputPortVariable(0));
        context.put("signalName", getInputPortVariable(0));
        
        // Add dimension variables for arrays
        InputPort inputPort = inputPortList.get(0);
        if (inputPort.getLinkedLine() == null) {
            // Use default values if no line is linked (shouldn't happen in normal cases)
            context.put("signalHeight", 1);
            context.put("signalWidth", 1);
        } else {
            OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("signalHeight", signal.getHeight());
            context.put("signalWidth", signal.getWidth());
        }

        String codeStr = TemplateManager.renderTemplate("c/discrete/UnitDelay/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/discrete/UnitDelay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add parameter objects and their names for template
        context.put("sampleTime", sampleTimeParam);
        context.put("sampleTimeName", sampleTimeParam.getName());
        context.put("initialCondition", initialCondition);
        context.put("initialConditionName", initialCondition.getName());

        String codeStr = TemplateManager.renderTemplate("c/discrete/UnitDelay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        // Use proper C variable name instead of Java object reference
        context.put("signal", getInputPortVariable(0));
        context.put("signalName", getInputPortVariable(0));
        context.put("output1", getOutputPortVariable(0));
        
        // Add parameter objects and their names
        context.put("sampleTime", sampleTimeParam);
        context.put("sampleTimeName", sampleTimeParam.getName());
        context.put("initialCondition", initialCondition);
        context.put("initialConditionName", initialCondition.getName());
        
        // Add dimension variables for template loops
        InputPort inputPort = inputPortList.get(0);
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("signalHeight", signal.getHeight());
        context.put("signalWidth", signal.getWidth());

        String codeStr = TemplateManager.renderTemplate("c/discrete/UnitDelay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (sampleTimeParam.getDataType() != DataType.REAL || initialCondition.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
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

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        // Initialize both values with initial condition
        currentValue = new Data(initialCondition.getData().getInitValue());
        previousValue = new Data(initialCondition.getData().getInitValue());
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        // Output the previous value (unit delay behavior)
        out.setData(previousValue);
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort input = inputPortList.get(0);
        // Shift values: previous becomes current input
        previousValue = currentValue;
        currentValue = input.getData();
    }
}
