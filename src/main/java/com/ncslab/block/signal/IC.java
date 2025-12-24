package com.ncslab.block.signal;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.signal.ICDto;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
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
 * IC (Initial Condition) block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * The IC block sets the initial condition for a signal at the start of simulation,
 * then passes the input through unchanged:
 * - At t=0: Outputs the InitialCondition value
 * - At t>0: Transparently passes through the input signal
 *
 * Common use: Initialize integrators, delays, and other stateful blocks.
 *
 * SIMULINK Parameters:
 * - InitialCondition: Initial value to output before first input arrives (default: 0.0)
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification (default: "Inherit: Same as input")
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
public class IC extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter initialCondition;
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // === State Management ===
    private boolean firstExecution = true;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("InitialCondition", "0.0");  // Default initial value
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");  // Inherit from input
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

        // Input port defaults (accepts any signal type)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (matches input)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);  // IC has feedthrough after t=0
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private IC(Parameter initialCondition, Parameter sampleTime, Parameter outDataType,
               String blockName, String blockPath,
               String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(initialCondition, sampleTime);

        // Assign parameters
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.initialCondition);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public IC(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        initializePorts();
    }

    /**
     * DTO Constructor - Creates IC block directly from ICDto DTO
     */
    public IC(ICDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO with defaults
        String initialConditionValue = dto.getInitialConditionValue() != null ?
            String.valueOf(dto.getInitialConditionValue()) : "0.0";
        String sampleTimeValue = dto.getSampleTime() != null ? (dto.getSampleTime().getAsString()) : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue();

        // Validate initial condition (must be finite)
        double icValue = Double.parseDouble(initialConditionValue);
        if (!Double.isFinite(icValue)) {
            throw new IllegalArgumentException("Initial condition must be finite (not NaN or Infinite)");
        }

        // Validate sample time
        double sampleTimeDouble = Double.parseDouble(sampleTimeValue);
        if (sampleTimeDouble != -1.0 && sampleTimeDouble < 0.0) {
            throw new IllegalArgumentException("Sample time must be non-negative or -1 (inherited)");
        }

        // Initialize parameters
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        initializePorts();

        System.out.println("DTO-SPECIFIC: IC block created successfully from ICDto - " + dto.getBlockName());
    }

    /**
     * Factory method to create IC block from ICDto.
     *
     * @param dto The ICDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New IC block instance
     * @throws BlockCreationException if block creation fails
     */
    public static IC createFromDto(ICDto dto, NCSLabModel model) throws BlockCreationException {
        return new IC(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static IC fromJSON(JSONObject blockJSON, NCSLabModel model) {
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

            IC block = new IC(initialCondition, sampleTime, outDataType,
                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, initialCondition, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create IC block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create an IC block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return Configured IC block instance
     */
    public static IC create(String name, String path, NCSLabModel model) {
        return create(name, path, 0.0, -1.0, "Inherit: Same as input", model);
    }

    /**
     * Create an IC block with full parameters using DTO-based construction.
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param initialCondition Initial condition value
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return Configured IC block instance
     */
    public static IC create(String name, String path, double initialCondition,
                           double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        ICDto dto = ICDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .initialCondition(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid IC parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new IC(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter initialCondition, Parameter sampleTime) {
        // Validate initial condition is finite
        double icValue = initialCondition.getDouble();
        if (!Double.isFinite(icValue)) {
            throw new IllegalArgumentException("Initial condition must be finite (not NaN or Infinite)");
        }

        // Validate sample time
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue < 0.0) {
            throw new IllegalArgumentException("Sample time must be non-negative or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 1, "InitialCondition", initialConditionValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 3, "OutDataTypeStr", outDataTypeValue);
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

    private static void setParameterBlockReference(IC block, Parameter... parameters) {
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
        identity.put("blockType", "IC");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));  // Feedthrough after t=0
    }

    // === Code Generation Methods ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // Use template for initialization code
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        String initCode = TemplateManager.renderTemplate("c/signal/IC/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/signal/IC/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // IC block output dimensions match input dimensions
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);

        if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
            OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

            out.setHeight(inputSignal.getHeight());
            out.setWidth(inputSignal.getWidth());
            out.getOutputSignalC().setHeight(inputSignal.getHeight());
            out.getOutputSignalC().setWidth(inputSignal.getWidth());
            out.getOutputSignalC().setDataType(inputSignal.getDataType());
        } else {
            // Default to scalar if no input connected
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        }
    }

    public void checkDimension() throws MatDimException {
        // IC block accepts any input dimension
        // Output matches input dimension
        // No dimension checking needed
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK IC block behavior:
        // - At t=0: output the initial condition
        // - At t>0: pass through input signal unchanged

        OutputPort outputPort = outputPortList.get(0);

        if (firstExecution || t == 0.0) {
            // At t=0, output the initial condition
            double icValue = initialCondition != null ?
                Double.parseDouble(initialCondition.getInitString()) : 0.0;

            Data outputData = new Data(icValue);
            outputPort.setData(outputData);
            firstExecution = false;
        } else {
            // After t=0, pass through input signal
            InputPort inputPort = inputPortList.get(0);
            if (inputPort.getLinkedLine() != null &&
                inputPort.getLinkedLine().getLinkedOutputPort() != null) {

                // Get input data and pass it through
                Data inputData = inputPort.getData();
                if (inputData != null) {
                    outputPort.setData(inputData);
                }

                // Also set output signal for code generation
                OutputSignal inputSignal = inputPort.getLinkedLine()
                    .getLinkedOutputPort().getOutputSignalC();
                outputPort.setOutputSignalC(inputSignal);
            }
        }
    }
}
