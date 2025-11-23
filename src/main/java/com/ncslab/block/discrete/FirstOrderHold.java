package com.ncslab.block.discrete;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.FirstOrderHoldDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.OutputSignal;
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
 * FirstOrderHold block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * The First-Order Hold block implements a rate transition with linear extrapolation
 * between samples. Unlike zero-order hold which holds the last sample constant,
 * first-order hold performs linear extrapolation based on the slope between the
 * last two samples.
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 *
 * First-Order Hold Behavior:
 * The first-order hold block performs linear extrapolation between samples:
 *
 * y(t) = y[k-1] + (y[k-1] - y[k-2]) * (t - t[k-1]) / (t[k-1] - t[k-2])
 *
 * where:
 * - y[k-1] is the most recent sample
 * - y[k-2] is the previous sample
 * - t[k-1] is the time of the most recent sample
 * - t[k-2] is the time of the previous sample
 * - t is the current time
 *
 * This provides smoother reconstruction than zero-order hold for signals with
 * relatively constant derivatives.
 *
 * Key Features:
 * - Linear extrapolation between samples
 * - More accurate than zero-order hold for smooth signals
 * - Requires two samples before extrapolation begins
 * - No feedthrough (breaks algebraic loops)
 * - Supports scalar and matrix signals
 *
 * @author NCSLab
 * @version 1.0
 * @since First-Order Hold Implementation 2025
 */
public class FirstOrderHold extends DiscreteBlock {

    // === Internal State for First-Order Hold ===
    private Data lastSample;           // y[k-1]
    private Data secondLastSample;     // y[k-2]
    private double lastSampleTime;     // t[k-1]
    private double secondLastSampleTime; // t[k-2]
    private boolean initialized = false;
    private final boolean feedthrough = false; // First-order hold has no feedthrough

    // === SIMULINK-Compatible Parameters ===
    private final Parameter sampleTimeParam;
    private final Parameter outDataType;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
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

        // Output port defaults (first-order hold has no feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private FirstOrderHold(Parameter sampleTime, Parameter outDataType,
                          String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Initialize state
        this.lastSample = new Data(0.0);
        this.secondLastSample = new Data(0.0);
        this.lastSampleTime = 0.0;
        this.secondLastSampleTime = 0.0;
        this.initialized = false;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public FirstOrderHold(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize state
        this.lastSample = new Data(0.0);
        this.secondLastSample = new Data(0.0);
        this.lastSampleTime = 0.0;
        this.secondLastSampleTime = 0.0;
        this.initialized = false;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
    }

    /**
     * DTO-NATIVE Constructor - Creates FirstOrderHold block directly from FirstOrderHoldDto DTO
     */
    public FirstOrderHold(FirstOrderHoldDto dto, NCSLabModel model) {
        super(dto, model);

        // Create parameters from DTO
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize state
        this.lastSample = new Data(0.0);
        this.secondLastSample = new Data(0.0);
        this.lastSampleTime = 0.0;
        this.secondLastSampleTime = 0.0;
        this.initialized = false;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
        System.out.println("DTO-NATIVE: FirstOrderHold block created successfully - " + dto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static FirstOrderHold fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);

            FirstOrderHold block = new FirstOrderHold(sampleTime, outDataType,
                                                     blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create FirstOrderHold block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static FirstOrderHold create(String name, String path, double sampleTime, NCSLabModel model) {
        return create(name, path, sampleTime, "Inherit: Same as input", model);
    }

    /**
     * Create a FirstOrderHold block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param sampleTime Sample time for discrete operation
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return FirstOrderHold block instance
     */
    public static FirstOrderHold create(String name, String path, double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        FirstOrderHoldDto dto = FirstOrderHoldDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid FirstOrderHold parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new FirstOrderHold(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 2, "OutDataTypeStr", outDataTypeValue);
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

    private static void setParameterBlockReference(FirstOrderHold block, Parameter... parameters) {
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
        identity.put("blockType", "FirstOrderHold");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Code Generation Methods ===

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/FirstOrderHold/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("sampleTime", sampleTimeParam);

        String codeStr = TemplateManager.renderTemplate("c/discrete/FirstOrderHold/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Get signal info for proper C variable names
        InputPort inputPort = inputPortList.get(0);
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Add C variable name strings to context
        String inputSignalName = getInputPortVariable(0);
        context.put("inputVar", inputSignalName);
        context.put("outputVar", getOutputPortVariable(0));

        // Add data type and dimension information
        context.put("signalDataType", signal.getDataType());
        context.put("realDataType", DataType.REAL);
        context.put("signalHeight", signal.getHeight());
        context.put("signalWidth", signal.getWidth());

        String codeStr = TemplateManager.renderTemplate("c/discrete/FirstOrderHold/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDiscreteUpdateCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Get signal info for proper C variable names
        InputPort inputPort = inputPortList.get(0);
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Add C variable name strings to context
        String inputSignalName = getInputPortVariable(0);
        context.put("inputVar", inputSignalName);

        // Add data type information
        context.put("signalDataType", signal.getDataType());
        context.put("realDataType", DataType.REAL);

        String codeStr = TemplateManager.renderTemplate("c/discrete/FirstOrderHold/discreteUpdate.vm", context);
        code.addDiscreteUpdateCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();

        // TODO: Implement SIMULINK scalar zero expansion for IC
        // If IC is scalar "0" and input is vector/matrix, expand IC to zero matrix matching input dimensions
        // See Delay.java:503-526 for reference implementation

        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Validate sample time
        double sampleTimeValue = sampleTimeParam.getData().getInitValue();
        if ((sampleTimeValue * 1000000) % (model.getConfig().getFixedStep() * 1000000) > 0.000001) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        if (sampleTimeParam.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }

        // Set output dimensions to match input
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // No additional dimension checks needed for first-order hold
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort outputPort = outputPortList.get(0);

        if (!initialized || t == 0.0) {
            // At initialization, output the input value
            InputPort inputPort = inputPortList.get(0);
            if (inputPort.getLinkedLine() != null &&
                inputPort.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal inputSignal = inputPort.getLinkedLine()
                    .getLinkedOutputPort().getOutputSignalC();
                outputPort.setData(inputSignal.getData());
            } else {
                outputPort.setData(new Data(0.0));
            }
        } else if (secondLastSampleTime < lastSampleTime) {
            // Have at least two samples, perform linear extrapolation
            // y(t) = y[k-1] + (y[k-1] - y[k-2]) * (t - t[k-1]) / (t[k-1] - t[k-2])
            double timeDiff = lastSampleTime - secondLastSampleTime;
            double timeStep = t - lastSampleTime;

            if (lastSample.getDataType() == DataType.REAL) {
                double slope = (lastSample.getInitValue() - secondLastSample.getInitValue()) / timeDiff;
                double extrapolatedValue = lastSample.getInitValue() + slope * timeStep;
                outputPort.setData(new Data(extrapolatedValue));
            } else if (lastSample.getDataType() == DataType.MATRIX) {
                // For matrix, perform element-wise linear extrapolation
                Jama.Matrix lastMatrix = lastSample.getMatrix();
                Jama.Matrix secondLastMatrix = secondLastSample.getMatrix();
                Jama.Matrix result = new Jama.Matrix(lastMatrix.getRowDimension(), lastMatrix.getColumnDimension());

                for (int i = 0; i < lastMatrix.getRowDimension(); i++) {
                    for (int j = 0; j < lastMatrix.getColumnDimension(); j++) {
                        double slope = (lastMatrix.get(i, j) - secondLastMatrix.get(i, j)) / timeDiff;
                        double extrapolatedValue = lastMatrix.get(i, j) + slope * timeStep;
                        result.set(i, j, extrapolatedValue);
                    }
                }
                outputPort.setData(new Data(result));
            }
        } else {
            // Only have one sample, use zero-order hold (constant)
            outputPort.setData(lastSample);
        }
    }

    @Override
    public void calculateInit() {
        // Initialize first-order hold block
        OutputPort output = outputPortList.get(0);
        InputPort input = inputPortList.get(0);

        // Sample the initial input value
        if (input.getLinkedLine() != null &&
            input.getLinkedLine().getLinkedOutputPort() != null) {
            OutputSignal inputSignal = input.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            Data initialInput = inputSignal.getData();

            // Initialize state with input value
            lastSample = initialInput;
            secondLastSample = initialInput;
            lastSampleTime = 0.0;
            secondLastSampleTime = 0.0;

            // Initialize output with input value
            output.setData(initialInput);
            initialized = false; // Will be set to true on first discrete update
        } else {
            // No input available, initialize with zero
            Data zeroData = new Data(0.0);
            lastSample = zeroData;
            secondLastSample = zeroData;
            output.setData(zeroData);
            initialized = false;
        }
    }

    @Override
    public void calculateUpdate(double t) {
        // Update happens in calculateDiscreteUpdate for discrete blocks
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        // At sample time, update stored samples
        InputPort inputPort = inputPortList.get(0);
        if (inputPort.getLinkedLine() != null &&
            inputPort.getLinkedLine().getLinkedOutputPort() != null) {

            OutputSignal inputSignal = inputPort.getLinkedLine()
                .getLinkedOutputPort().getOutputSignalC();
            Data currentData = inputSignal.getData();

            // Shift history
            secondLastSample = lastSample;
            secondLastSampleTime = lastSampleTime;

            // Update current
            lastSample = currentData;
            lastSampleTime = t;

            initialized = true;
        }
    }
}
