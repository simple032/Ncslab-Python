package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import com.ncslab.block.io.State;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.source.DigitalClockDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ncslab.util.TemplateManager;

/**
 * Digital Clock block with SIMULINK-compatible parameters.
 * Extends SourceBlock for common source block functionality.
 *
 * Digital Clock-specific behavior:
 * Outputs current simulation time at discrete sample intervals (sample-and-hold behavior).
 * Between sample times, holds the previous sampled time value.
 *
 * Key differences from Clock block:
 * - Clock: continuous time output (always outputs model.time)
 * - DigitalClock: discrete time output (samples and holds model.time at sample intervals)
 */
public class DigitalClock extends SourceBlock {

    // State variable to hold the sampled time value
    private State stateOutput;

    // === Static Parameter Definitions ===
    // Parameter defaults (inherited common ones from SourceBlock)
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "1.0");  // Digital Clock default: 1.0 second discrete sample time
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    // === Private Constructor with Typed Parameters ===
    private DigitalClock(Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super("DigitalClock", sampleTime, outDataType, saturateOnIntegerOverflow, blockName, blockPath, blockUUID, model);

        // Validate parameters
        validateParameters(sampleTime);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DigitalClock(JSONObject blockJSON, NCSLabModel model) {
        // Extract parameters from JSON and initialize SourceBlock properly
        this(
            createSampleTimeFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createOutDataTypeFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createSaturateFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            blockJSON.optString("blockName", "DigitalClock"),
            blockJSON.optString("blockPath", ""),
            blockJSON.optString("blockUUID", "null"),
            model
        );
    }

    /**
     * DTO-NATIVE Constructor - Creates DigitalClock block directly from DigitalClockDto DTO
     */
    public DigitalClock(DigitalClockDto digitalClockDto, NCSLabModel model) {
        this(
            createParameterFromTyped(digitalClockDto.getSampleTime(), 1, "SampleTime"),
            createParameterFromTyped(digitalClockDto.getOutDataTypeStr(), 2, "OutDataTypeStr"),
            createParameterFromTyped(digitalClockDto.getSaturateOnIntegerOverflow(), 3, "SaturateOnIntegerOverflow"),
            digitalClockDto.getBlockName(),
            digitalClockDto.getBlockPath(),
            digitalClockDto.getBlockUUID() != null ? digitalClockDto.getBlockUUID() : "null",
            model
        );
        System.out.println("DTO-NATIVE: DigitalClock block created successfully from DigitalClockDto - " + digitalClockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static DigitalClock fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            DigitalClock block = new DigitalClock(sampleTime, outDataType, saturateParam,
                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DigitalClock block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static DigitalClock create(String name, String path, NCSLabModel model) {
        return create(name, path, 1.0, "double", false, model);
    }

    /**
     * Create a DigitalClock block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param sampleTime Sample time (must be > 0 for discrete operation)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return DigitalClock block instance
     */
    public static DigitalClock create(String name, String path, double sampleTime, String outDataType,
                              boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DigitalClockDto dto = DigitalClockDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid DigitalClock parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new DigitalClock(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Digital Clock sample time must be > 0, got: " + sampleTimeValue);
        }
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

    private static void setParameterBlockReference(DigitalClock block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                // Use setBlock method if available
                if (param != null) {
                    java.lang.reflect.Method setBlockMethod = Parameter.class.getDeclaredMethod("setBlock", com.ncslab.block.Block.class);
                    setBlockMethod.setAccessible(true);
                    setBlockMethod.invoke(param, block);
                }
            } catch (Exception e) {
                // Try reflection to access private field as fallback
                try {
                    if (param != null) {
                        java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                        blockField.setAccessible(true);
                        blockField.set(param, block);
                    }
                } catch (Exception e2) {
                    // Fallback: parameter block reference will be null, but should work for basic operations
                    System.err.println("Warning: Could not set block reference for parameter " + (param != null ? param.getName() : "null"));
                }
            }
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        if (paramValues == null) {
            paramValues = new JSONObject();
        }
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        if (paramValues == null) {
            paramValues = new JSONObject();
        }
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "double");
        return new Parameter(null, 2, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        if (paramValues == null) {
            paramValues = new JSONObject();
        }
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 3, "SaturateOnIntegerOverflow", saturateValue);
    }

    // === DTO Helper Methods ===
    private static Parameter createParameterFromTyped(com.ncslab.dto.common.TypedParameter typedParam, int number, String name) {
        if (typedParam == null || typedParam.getValue() == null) {
            throw new IllegalArgumentException("TypedParameter " + name + " cannot be null");
        }

        String stringValue;
        if (typedParam.getValue() instanceof Boolean) {
            stringValue = ((Boolean) typedParam.getValue()) ? "on" : "off";
        } else {
            stringValue = typedParam.getValue().toString();
        }

        return new Parameter(null, number, name, stringValue);
    }



    // === Code Generation Methods ===

    /**
     * Generate array declarations for state variables
     */
    public void generateArraysCodeC(CodeStructC code) {
        super.generateArraysCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add state variable name to context
        context.put("stateOutputName", stateOutput != null ? stateOutput.getName() : "stateOutput");

        String arraysCode = TemplateManager.renderTemplate("c/source/DigitalClock/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    /**
     * Generate initialization code for Digital Clock
     */
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add Digital Clock-specific context variables
        context.put("sampleTime", sampleTime);
        context.put("sampleTimeName", sampleTime.getName());
        context.put("stateOutputName", stateOutput != null ? stateOutput.getName() : "stateOutput");

        String initCode = TemplateManager.renderTemplate("c/source/DigitalClock/init.vm", context);
        code.addInitCode(initCode);
    }

    /**
     * Generate output code for Digital Clock
     */
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        // Populate all standard context variables
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add Digital Clock-specific context variables
        context.put("outputSignal", getOutputPortVariable(0));
        context.put("outputSignalName", getOutputPortVariable(0));
        context.put("sampleTime", sampleTime);
        context.put("sampleTimeName", sampleTime.getName());
        context.put("stateOutputName", stateOutput != null ? stateOutput.getName() : "stateOutput");

        String outputCode = TemplateManager.renderTemplate("c/source/DigitalClock/output.vm", context);
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException {
        // Digital Clock block always outputs scalar time value
        // Validate sample time is integer multiple of fixed step
        double sampleTimeValue = sampleTime.getData().getInitValue();
        double fixedStep = model.getConfig().getFixedStep();

        if ((sampleTimeValue * 1000000) % (fixedStep * 1000000) > 0.000001) {
            MatDimException e = new MatDimException("Parameter(SampleTime) of Block " + this.blockName +
                " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        // Create state variable for holding the sampled time value
        stateOutput = new State(this, 1, "stateOutput", 1, 1);
        stateList.add(stateOutput);
    }

    public void checkDimension() throws MatDimException {
        // No dimension checks needed for digital clock block
    }

    @Override
    public void calculateOutput(double t) {
        // Digital Clock: outputs the held (sampled) time value
        // The state is updated by calculateUpdate at discrete sample times
        if (stateOutput == null || stateOutput.getData() == null) {
            // If no state available yet, output current time
            outputPortList.get(0).getOutputSignalC().setData(new Data(t));
        } else {
            // Output the held (sampled) time value
            outputPortList.get(0).getOutputSignalC().setData(stateOutput.getData());
        }
    }

    @Override
    public void calculateInit() {
        // Initialize digital clock block with time 0
        outputPortList.get(0).getOutputSignalC().setData(new Data(0.0));

        // Initialize state with 0
        if (stateOutput != null) {
            stateOutput.setData(new Data(0.0));
        }
    }

    @Override
    public void calculateUpdate(double t) {
        // Update the held time value at discrete sample times
        if (stateOutput == null) {
            return;
        }

        double sampleTimeValue = sampleTime.getData().getInitValue();

        // Check if it's time to sample (at discrete time boundaries)
        // This is typically controlled by the solver calling this at sample times
        if (sampleTimeValue > 0) {
            // Sample the current time
            stateOutput.setData(new Data(t));
        }
    }
}
