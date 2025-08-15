package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.Block;
import Jama.Matrix;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

/**
 * CompareToConstant block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - ConstantValue: Constant value to compare against
 * - RelationalOperator: Comparison operator (==, !=, <, <=, >, >=)
 * - LogicDataType: Output data type for logic operations
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class CompareToConstant extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter constantValue;
    private final Parameter relationalOperator;
    private final Parameter logicDataType;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("ConstantValue", "0");
        PARAMETER_DEFAULTS.put("RelationalOperator", "==");
        PARAMETER_DEFAULTS.put("LogicDataType", "boolean");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");

        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Private Constructor with Typed Parameters ===
    private CompareToConstant(Parameter constantValue, Parameter relationalOperator, Parameter logicDataType,
                             Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                             String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.constantValue = Objects.requireNonNull(constantValue, "Constant value parameter cannot be null");
        this.relationalOperator = Objects.requireNonNull(relationalOperator, "Relational operator parameter cannot be null");
        this.logicDataType = Objects.requireNonNull(logicDataType, "Logic data type parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public CompareToConstant(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        String relopValue = "";
        // Create legacy parameters for backward compatibility
        if(paramValues.has("RelationalOperator")) {
            relopValue = paramValues.getString("RelationalOperator");
        }else if(paramValues.has("Operator")) {
            relopValue = paramValues.getString("Operator");
        }
        if ("~=".equals(relopValue)) {
            relopValue = "!=";
        }

        this.constantValue = new Parameter(this, 1, "ConstantValue", "0");
        this.relationalOperator = new Parameter(this, 2, "RelationalOperator", relopValue);

        // Create missing SIMULINK parameters with defaults
        this.logicDataType = new Parameter(this, 3, "LogicDataType", "boolean");
        this.sampleTime = new Parameter(this, 4, "SampleTime", "-1"); // -1 for inherited
        this.outDataType = new Parameter(this, 5, "OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        this.saturateOnIntegerOverflow = new Parameter(this, 6, "SaturateOnIntegerOverflow", "off");

        // Add all parameters to parameter list

        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates CompareToConstant block directly from BlockJson DTO
     */
    public CompareToConstant(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.constantValue = new Parameter(this, 1, "Constantvalue", "0");
        this.relationalOperator = new Parameter(this, 2, "Relationaloperator", "0");
        this.logicDataType = new Parameter(this, 3, "Logicdatatype", "0");
        this.sampleTime = new Parameter(this, 4, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 5, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 6, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }



    // === Static Factory Method for JSON Deserialization ===
    public static CompareToConstant fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter constantValue = createConstantValueFromJSON(paramValues, blockName);
            Parameter relationalOperator = createRelationalOperatorFromJSON(paramValues, blockName);
            Parameter logicDataType = createLogicDataTypeFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            CompareToConstant block = new CompareToConstant(constantValue, relationalOperator, logicDataType,
                                                           sampleTime, outDataType, saturateParam,
                                                           blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, constantValue, relationalOperator, logicDataType,
                                     sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create CompareToConstant block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static CompareToConstant create(String name, String path, double constantValue, String relationalOperator, NCSLabModel model) {
        return create(name, path, constantValue, relationalOperator, "boolean", -1.0,
                     "Inherit: Logical (see Configuration Parameters: Optimization)", false, model);
    }

    public static CompareToConstant create(String name, String path, double constantValue, String relationalOperator,
                                          String logicDataType, double sampleTime, String outDataType,
                                          boolean saturateOnOverflow, NCSLabModel model) {
        Parameter constantValueParam = new Parameter(null, 1, "ConstantValue", String.valueOf(constantValue));
        Parameter relationalOperatorParam = new Parameter(null, 2, "RelationalOperator", relationalOperator);
        Parameter logicDataTypeParam = new Parameter(null, 3, "LogicDataType", logicDataType);
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        CompareToConstant block = new CompareToConstant(constantValueParam, relationalOperatorParam, logicDataTypeParam,
                                                       sampleTimeParam, outDataTypeParam, saturateParam,
                                                       name, path, "null", model);

        setParameterBlockReference(block, constantValueParam, relationalOperatorParam, logicDataTypeParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createConstantValueFromJSON(JSONObject paramValues, String blockName) {
        String constantValueStr = paramValues.optString("const", "0.0");
        return new Parameter(null, 1, "ConstantValue", constantValueStr);
    }
    private static Parameter createRelationalOperatorFromJSON(JSONObject paramValues, String blockName) {
        String relopValue = paramValues.optString("relop", "==");
        if ("~=".equals(relopValue)) {
            relopValue = "!=";
        }
        return new Parameter(null, 2, "RelationalOperator", relopValue);
    }
    private static Parameter createLogicDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String logicDataTypeValue = paramValues.optString("LogicDataType", "boolean");
        return new Parameter(null, 3, "LogicDataType", logicDataTypeValue);
    }
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 4, "SampleTime", sampleTimeValue);
    }
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        return new Parameter(null, 5, "OutDataTypeStr", outDataTypeValue);
    }
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateValue);
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
    private static void setParameterBlockReference(CompareToConstant block, Parameter... parameters) {
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
        identity.put("blockType", "CompareToConstant");
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

        // Main output port (has feedthrough)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }
}
