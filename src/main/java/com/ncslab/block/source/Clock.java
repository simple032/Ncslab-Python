package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.source.ClockDto;

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
 * Clock block with SIMULINK-compatible parameters.
 * Extends SourceBlock for common source block functionality.
 * 
 * Clock-specific behavior: Outputs current simulation time.
 */
public class Clock extends SourceBlock {

    // === Static Parameter Definitions ===
    // Parameter defaults (inherited common ones from SourceBlock)
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // Clock-specific parameter defaults can be added here if needed
    }

    // === Private Constructor with Typed Parameters ===
    private Clock(Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super("Clock", sampleTime, outDataType, saturateOnIntegerOverflow, blockName, blockPath, blockUUID, model);

        // Validate parameters
        validateParameters(sampleTime);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Clock(JSONObject blockJSON, NCSLabModel model) {
        // Extract parameters from JSON and initialize SourceBlock properly
        this(
            createSampleTimeFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createOutDataTypeFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            createSaturateFromJSON(blockJSON.optJSONObject("paramValues"), blockJSON.optString("blockName")),
            blockJSON.optString("blockName", "Clock"),
            blockJSON.optString("blockPath", ""),
            blockJSON.optString("blockUUID", "null"),
            model
        );
    }
    
    /**
     * DTO-NATIVE Constructor - Creates Clock block directly from ClockDto DTO
     */
    public Clock(com.ncslab.dto.block.specialized.source.ClockDto clockDto, NCSLabModel model) {
        this(
            createParameterFromTyped(clockDto.getSampleTime(), 1, "SampleTime"),
            createParameterFromTyped(clockDto.getOutDataTypeStr(), 2, "OutDataTypeStr"),
            createParameterFromTyped(clockDto.getSaturateOnIntegerOverflow(), 3, "SaturateOnIntegerOverflow"),
            clockDto.getBlockName(),
            clockDto.getBlockPath(),
            clockDto.getBlockUUID() != null ? clockDto.getBlockUUID() : "null",
            model
        );
        System.out.println("DTO-NATIVE: Clock block created successfully from ClockDto - " + clockDto.getBlockName());
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Clock fromJSON(JSONObject blockJSON, NCSLabModel model) {
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

            Clock block = new Clock(sampleTime, outDataType, saturateParam,
                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Clock block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Clock create(String name, String path, NCSLabModel model) {
        return create(name, path, 0.0, "double", false, model);
    }

    public static Clock create(String name, String path, double sampleTime, String outDataType,
                              boolean saturateOnOverflow, NCSLabModel model) {
        Parameter sampleTimeParam = new Parameter(null, 1, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 2, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 3, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        Clock block = new Clock(sampleTimeParam, outDataTypeParam, saturateParam,
                               name, path, "null", model);

        setParameterBlockReference(block, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
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

    private static void setParameterBlockReference(Clock block, Parameter... parameters) {
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
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
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



    // === Code Generation Methods (preserved from original) ===
    // Clock blocks don't need initialization code, they inherit from SourceBlock

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        // Populate all standard context variables
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add Clock-specific context variables
        context.put("block", this);
        context.put("outputVar", getOutputPortVariable(0));
        context.put("outputSignal", getOutputPortVariable(0));
        context.put("outputSignalName", getOutputPortVariable(0));
        
        String outputCode = TemplateManager.renderTemplate("c/source/Clock/output.vm", context);
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException {
        // Clock block always outputs scalar time value
    }

    public void checkDimension() throws MatDimException {
        // No dimension checks needed for clock block
    }

    @Override
    public void calculateOutput(double t) {
        outputPortList.get(0).getOutputSignalC().setData(new Data(t));
    }

    @Override
    public void calculateInit() {
        outputPortList.get(0).getOutputSignalC().setData(new Data(0));
    }
}
