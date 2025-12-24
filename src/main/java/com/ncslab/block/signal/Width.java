package com.ncslab.block.signal;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.signal.WidthDto;

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
 * Width block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * The Width block outputs the width (number of elements) of the input signal.
 * - Scalar input → output 1
 * - Vector input → output N (number of elements)
 * - Matrix input → output total elements (rows × cols)
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification (always numeric)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
public class Width extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");  // Width is always numeric
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

        // Output port defaults (width is always scalar)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);  // Width has feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private Width(Parameter sampleTime, Parameter outDataType,
                  String blockName, String blockPath,
                  String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(sampleTime);

        // Assign parameters
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Width(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        initializePorts();
    }

    /**
     * DTO Constructor - Creates Width block directly from WidthDto DTO
     */
    public Width(WidthDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO with defaults
        String sampleTimeValue = dto.getSampleTime() != null ? (dto.getSampleTime().getAsString()) : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue();

        // Validate sample time
        double sampleTimeDouble = Double.parseDouble(sampleTimeValue);
        if (sampleTimeDouble != -1.0 && sampleTimeDouble < 0.0) {
            throw new IllegalArgumentException("Sample time must be non-negative or -1 (inherited)");
        }

        // Initialize parameters
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        initializePorts();

        System.out.println("DTO-SPECIFIC: Width block created successfully from WidthDto - " + dto.getBlockName());
    }

    /**
     * Factory method to create Width block from WidthDto.
     *
     * @param dto The WidthDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New Width block instance
     * @throws BlockCreationException if block creation fails
     */
    public static Width createFromDto(WidthDto dto, NCSLabModel model) throws BlockCreationException {
        return new Width(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Width fromJSON(JSONObject blockJSON, NCSLabModel model) {
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

            Width block = new Width(sampleTime, outDataType,
                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Width block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a Width block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return Configured Width block instance
     */
    public static Width create(String name, String path, NCSLabModel model) {
        return create(name, path, -1.0, "double", model);
    }

    /**
     * Create a Width block with full parameters using DTO-based construction.
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return Configured Width block instance
     */
    public static Width create(String name, String path, double sampleTime,
                              String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        WidthDto dto = WidthDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Width parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Width(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue < 0.0) {
            throw new IllegalArgumentException("Sample time must be non-negative or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "double");
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

    private static void setParameterBlockReference(Width block, Parameter... parameters) {
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
        identity.put("blockType", "Width");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));  // Feedthrough
    }

    // === Code Generation Methods ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = "";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/signal/Width/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // Width block always outputs a scalar (1x1)
        OutputPort out = outputPortList.get(0);

        out.setHeight(1);
        out.setWidth(1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(1);
        out.getOutputSignalC().setDataType(DataType.REAL);
    }

    public void checkDimension() throws MatDimException {
        // Width block accepts any input dimension
        // No dimension checking needed
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Width block: outputs the width (number of elements) of input signal
        Data inputData = inputPortList.get(0).getData();
        Data outputData = new Data(1, 1);  // Width is always scalar

        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - compute total elements
            Jama.Matrix inputMatrix = inputData.getMatrix();
            int rows = inputMatrix.getRowDimension();
            int cols = inputMatrix.getColumnDimension();
            int totalElements = rows * cols;
            outputData.setInitValue(totalElements);
        } else {
            // Scalar input - width is 1
            outputData.setInitValue(1.0);
        }

        outputPortList.get(0).setData(outputData);
    }
}
