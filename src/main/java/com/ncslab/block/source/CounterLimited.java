package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.source.CounterLimitedDto;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CounterLimited block with SIMULINK-compatible parameters.
 * Counts up or down with wraparound limits.
 *
 * SIMULINK Parameters:
 * - CountDirection: "Up" or "Down" (default "Up")
 * - InitialCount: Starting count value (default 0)
 * - MaxCount: Maximum count before wrapping (default 255)
 * - OutputAtTerminalCount: "Hit" or "Wrap" (default "Wrap")
 * - SampleTime: Sample time for discrete operation (default -1, inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class CounterLimited extends DiscreteBlock {

    // === Internal State ===
    private double currentCount;

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter countDirection;
    @Getter
    private final Parameter initialCount;
    @Getter
    private final Parameter maxCount;
    @Getter
    private final Parameter outputAtTerminalCount;
    @Getter
    private final Parameter sampleTimeParam;
    @Getter
    private final Parameter outDataType;
    @Getter
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("CountDirection", "Up");
        PARAMETER_DEFAULTS.put("InitialCount", "0");
        PARAMETER_DEFAULTS.put("MaxCount", "255");
        PARAMETER_DEFAULTS.put("OutputAtTerminalCount", "Wrap");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        outputNames.add("out1");

        INPUT_PORT_DEFAULTS = new ArrayList<>();

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
    private CounterLimited(Parameter countDirection, Parameter initialCount,
                          Parameter maxCount, Parameter outputAtTerminalCount,
                          Parameter sampleTimeParam, Parameter outDataType,
                          Parameter saturateOnIntegerOverflow,
                          String blockName, String blockPath, String blockUUID,
                          NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(countDirection, outputAtTerminalCount, sampleTimeParam);

        // Assign parameters
        this.countDirection = Objects.requireNonNull(countDirection, "Count direction parameter cannot be null");
        this.initialCount = Objects.requireNonNull(initialCount, "Initial count parameter cannot be null");
        this.maxCount = Objects.requireNonNull(maxCount, "Max count parameter cannot be null");
        this.outputAtTerminalCount = Objects.requireNonNull(outputAtTerminalCount, "Output at terminal count parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTimeParam, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public CounterLimited(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Get parameters by name from the automatically populated parameterList
        this.countDirection = getParameterByName("CountDirection");
        this.initialCount = getParameterByName("InitialCount");
        this.maxCount = getParameterByName("MaxCount");
        this.outputAtTerminalCount = getParameterByName("OutputAtTerminalCount");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Set discrete sample time
        setSampleTime(sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();
    }

    /**
     * DTO-NATIVE Constructor - Creates CounterLimited block directly from BlockDto DTO
     */
    public CounterLimited(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Get parameters by name from the automatically populated parameterList
        this.countDirection = getParameterByName("CountDirection");
        this.initialCount = getParameterByName("InitialCount");
        this.maxCount = getParameterByName("MaxCount");
        this.outputAtTerminalCount = getParameterByName("OutputAtTerminalCount");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        postConstructionInitialization();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static CounterLimited fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter countDirection = createCountDirectionFromJSON(paramValues, blockName);
            Parameter initialCount = createInitialCountFromJSON(paramValues, blockName);
            Parameter maxCount = createMaxCountFromJSON(paramValues, blockName);
            Parameter outputAtTerminalCount = createOutputAtTerminalCountFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            CounterLimited block = new CounterLimited(countDirection, initialCount, maxCount,
                                                     outputAtTerminalCount, sampleTime,
                                                     outDataType, saturateParam,
                                                     blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, countDirection, initialCount, maxCount,
                                      outputAtTerminalCount, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create CounterLimited block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static CounterLimited create(String name, String path, NCSLabModel model) {
        return create(name, path, "Up", 0.0, 255.0, "Wrap", -1.0, "double", false, model);
    }

    public static CounterLimited create(String name, String path, String direction,
                                       double initialCount, double maxCount,
                                       String outputAtTerminalCount, double sampleTime,
                                       String outDataType, boolean saturateOnOverflow,
                                       NCSLabModel model) {
        CounterLimitedDto dto = CounterLimitedDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .countDirection(com.ncslab.dto.common.TypedParameter.of(direction))
            .initialCount(com.ncslab.dto.common.TypedParameter.of(initialCount))
            .maxCount(com.ncslab.dto.common.TypedParameter.of(maxCount))
            .outputAtTerminalCount(com.ncslab.dto.common.TypedParameter.of(outputAtTerminalCount))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid CounterLimited parameters: " + validation.getErrors());
        }

        return new CounterLimited(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter countDirection, Parameter outputAtTerminalCount, Parameter sampleTime) {
        String direction = countDirection.getInitString();
        if (!direction.equals("Up") && !direction.equals("Down")) {
            throw new IllegalArgumentException("Count direction must be 'Up' or 'Down'");
        }

        String terminal = outputAtTerminalCount.getInitString();
        if (!terminal.equals("Hit") && !terminal.equals("Wrap")) {
            throw new IllegalArgumentException("Output at terminal count must be 'Hit' or 'Wrap'");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 && sampleTimeValue != -1.0) {
            throw new IllegalArgumentException("Sample time must be > 0 or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createCountDirectionFromJSON(JSONObject paramValues, String blockName) {
        String directionValue = paramValues.optString("CountDirection", "Up");
        return new Parameter(null, 1, "CountDirection", directionValue);
    }

    private static Parameter createInitialCountFromJSON(JSONObject paramValues, String blockName) {
        String initialCountValue = paramValues.optString("InitialCount", "0");
        return new Parameter(null, 2, "InitialCount", initialCountValue);
    }

    private static Parameter createMaxCountFromJSON(JSONObject paramValues, String blockName) {
        String maxCountValue = paramValues.optString("MaxCount", "255");
        return new Parameter(null, 3, "MaxCount", maxCountValue);
    }

    private static Parameter createOutputAtTerminalCountFromJSON(JSONObject paramValues, String blockName) {
        String terminalValue = paramValues.optString("OutputAtTerminalCount", "Wrap");
        return new Parameter(null, 4, "OutputAtTerminalCount", terminalValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 5, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "double");
        return new Parameter(null, 6, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 7, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(CounterLimited block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null
            }
        }
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "CounterLimited");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void postConstructionInitialization() {
        if (outputPortList.isEmpty()) {
            OutputPort outputPort = new OutputPort(this, 1, false);
            outputPortList.add(outputPort);
        }

        output = outputPortList.get(0);
        output.setFeedThrough(false);
    }

    // === Code Generation Methods ===
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        String codeStr = TemplateManager.renderTemplate("c/source/CounterLimited/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        String codeStr = TemplateManager.renderTemplate("c/source/CounterLimited/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        String codeStr = TemplateManager.renderTemplate("c/source/CounterLimited/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDiscreteUpdateCodeCInside(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        String codeStr = TemplateManager.renderTemplate("c/source/CounterLimited/update.vm", context);
        code.addDiscreteUpdateCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();

        OutputPort out = outputPortList.get(0);
        out.setHeight(1);
        out.setWidth(1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(1);
    }

    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }

    // === SIMULINK-Compatible Lifecycle Methods ===

    @Override
    public void calculateCheckParameters() {
        String direction = countDirection.getInitString();
        if (!direction.equals("Up") && !direction.equals("Down")) {
            throw new IllegalArgumentException("CounterLimited " + blockName + ": Count direction must be 'Up' or 'Down', got: " + direction);
        }

        String terminal = outputAtTerminalCount.getInitString();
        if (!terminal.equals("Hit") && !terminal.equals("Wrap")) {
            throw new IllegalArgumentException("CounterLimited " + blockName + ": Output at terminal count must be 'Hit' or 'Wrap', got: " + terminal);
        }

        double sampleTimeValue = sampleTimeParam.getDouble();
        if (sampleTimeValue <= 0.0 && sampleTimeValue != -1.0) {
            throw new IllegalArgumentException("CounterLimited " + blockName + ": Sample time must be > 0 or -1 (inherited), got: " + sampleTimeValue);
        }
    }

    @Override
    public void calculateInit() {
        currentCount = initialCount.getDouble();
        output.setData(new Data(currentCount));
    }

    @Override
    public void calculateStart() {
        // No startup actions needed
    }

    @Override
    public void calculateOutput(double t) {
        output.setData(new Data(currentCount));
    }

    @Override
    public void calculateUpdate(double t) {
        // State updates happen in calculateDiscreteUpdate
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        boolean isUp = countDirection.getInitString().equals("Up");
        boolean isWrap = outputAtTerminalCount.getInitString().equals("Wrap");
        double maxCountValue = maxCount.getDouble();

        if (isUp) {
            if (currentCount >= maxCountValue) {
                if (isWrap) {
                    currentCount = 0.0;
                }
                // In "Hit" mode, stay at maxCount (do nothing)
            } else {
                currentCount += 1.0;
            }
        } else {
            // Counting down
            if (currentCount <= 0.0) {
                if (isWrap) {
                    currentCount = maxCountValue;
                }
                // In "Hit" mode, stay at 0 (do nothing)
            } else {
                currentCount -= 1.0;
            }
        }
    }

    @Override
    public void calculateStop() {
        // No stop actions needed
    }

    @Override
    public void calculateTerminate(double t) {
        // Release internal state
        currentCount = 0.0;
    }

    @Override
    public void calculateReset(double t) {
        currentCount = initialCount.getDouble();
    }
}
