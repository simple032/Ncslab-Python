package com.ncslab.block.route;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// JAMA library
import Jama.Matrix;

// Internal imports - DTO
import com.ncslab.dto.block.specialized.route.IndexVectorDto;
import com.ncslab.dto.core.BlockDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * Index Vector block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Generates index vector for use with Selector and Assignment blocks.
 * Equivalent to MATLAB colon operator for index generation.
 *
 * SIMULINK Parameters:
 * - IndexMode: "Zero-based" or "One-based" (default: "One-based")
 * - IndexParamArray: Index specification (e.g., "1:10", "1:2:20", "[1 3 5 7]")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type (default: "int32")
 *
 * Port Configuration:
 * - Input Ports: 0 (source block)
 * - Output Ports: 1 (vector of indices)
 *
 * Index Notation Formats:
 * - Array notation: [1, 3, 5, 7] - explicit index list
 * - MATLAB range: 1:10 - indices from 1 to 10 with step 1
 * - MATLAB range with step: 1:2:20 - indices from 1 to 20 with step 2
 * - Single value: 5 - single index
 *
 * Index Modes:
 * - One-based: Indices start at 1 (MATLAB convention)
 * - Zero-based: Indices start at 0 (C/Java convention) - subtracts 1 from all indices
 *
 * @author NCSLab Team
 * @version 2025
 */
public class IndexVector extends RouteBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter indexMode;
    private final Parameter indexParamArray;
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // === Computed Index Vector ===
    private int[] indexVector;
    private boolean isZeroBased;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("IndexMode", "One-based");
        PARAMETER_DEFAULTS.put("IndexParamArray", "1:10");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "int32");
    }
    static {
        // Port names
        outputNames.add("out1");
        // No input ports for source block

        // Input port defaults (empty for source block)
        INPUT_PORT_DEFAULTS = new ArrayList<>();

        // Output port defaults
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private IndexVector(Parameter indexMode, Parameter indexParamArray,
                       Parameter sampleTime, Parameter outDataType,
                       String blockName, String blockPath, String blockUUID,
                       NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.indexMode = Objects.requireNonNull(indexMode, "IndexMode parameter cannot be null");
        this.indexParamArray = Objects.requireNonNull(indexParamArray, "IndexParamArray parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.indexMode);
        parameterList.add(this.indexParamArray);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public IndexVector(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility
        this.indexMode = getParameterByName("IndexMode");
        this.indexParamArray = getParameterByName("IndexParamArray");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize ports
        initializePorts();
    }

    // ===== DUAL CONSTRUCTOR PATTERN - MIGRATION SUPPORT =====

    /**
     * Enhanced DTO-based constructor - preferred for new implementations
     * @param dto The DTO containing block configuration
     * @param model The parent model
     */
    public IndexVector(IndexVectorDto dto, NCSLabModel model) {
        super(dto, model);

        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }

        // Initialize from DTO parameters using new Parameter creation
        this.indexMode = getParameterByName("IndexMode");
        this.indexParamArray = getParameterByName("IndexParamArray");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize ports
        initializePorts();

        // Complete initialization
        System.out.println("Enhanced DTO: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }

    /**
     * DTO-NATIVE Constructor - Creates IndexVector block directly from BlockDto DTO
     */
    public IndexVector(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.indexMode = getParameterByName("IndexMode");
        this.indexParamArray = getParameterByName("IndexParamArray");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static IndexVector fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter indexMode = createIndexModeFromJSON(paramValues, blockName);
            Parameter indexParamArray = createIndexParamArrayFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);

            IndexVector block = new IndexVector(indexMode, indexParamArray, sampleTime, outDataType,
                                               blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, indexMode, indexParamArray, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create IndexVector block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static IndexVector create(String name, String path, String indexParamArray, NCSLabModel model) {
        return create(name, path, "One-based", indexParamArray, -1.0, "int32", model);
    }

    /**
     * Create an IndexVector block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param indexMode Index mode ("One-based" or "Zero-based")
     * @param indexParamArray Index specification (e.g., "1:10", "[1, 3, 5]")
     * @param sampleTime Sample time (-1 for inherited, 0 for continuous, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return IndexVector block instance
     */
    public static IndexVector create(String name, String path, String indexMode, String indexParamArray,
                                    double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        IndexVectorDto dto = IndexVectorDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .indexMode(com.ncslab.dto.common.TypedParameter.of(indexMode))
            .indexParamArray(com.ncslab.dto.common.TypedParameter.of(indexParamArray))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid IndexVector parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new IndexVector(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createIndexModeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("IndexMode", "One-based");
        return new Parameter(null, 1, "IndexMode", value);
    }

    private static Parameter createIndexParamArrayFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("IndexParamArray", "1:10");
        return new Parameter(null, 2, "IndexParamArray", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", value);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("OutDataTypeStr", "int32");
        return new Parameter(null, 4, "OutDataTypeStr", value);
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

    private static void setParameterBlockReference(IndexVector block, Parameter... parameters) {
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
        identity.put("blockType", "IndexVector");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Get parameters
        String indexModeStr = this.indexMode.getInitString();
        isZeroBased = "Zero-based".equals(indexModeStr);

        String indexParam = this.indexParamArray.getInitString();

        // Parse index specification
        indexVector = parseIndexSpecification(indexParam);

        // Adjust for zero-based if needed
        if (isZeroBased) {
            for (int i = 0; i < indexVector.length; i++) {
                indexVector[i] = indexVector[i] - 1;
            }
        }

        // Create output port
        OutputPort outputPort = new OutputPort(this, 1);
        outputPortList.add(outputPort);

        // Set port dimensions based on index vector size
        outputPort.setHeight(indexVector.length);
        outputPort.setWidth(1);

        // Set output data type to integer
        outputPort.getOutputSignalC().setDataType(DataType.REAL); // Will be int32 in generated code
    }

    /**
     * Parse index specification string to integer array.
     * Handles both [1, 3, 5] and MATLAB syntax 1:2:10
     * @param spec Index specification string
     * @return Array of indices
     */
    private int[] parseIndexSpecification(String spec) {
        if (spec == null || spec.trim().isEmpty()) {
            return new int[]{1}; // Default to first element
        }

        String trimmed = spec.trim();

        // Array notation: [1, 3, 5]
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String content = trimmed.substring(1, trimmed.length() - 1).trim();
            if (content.isEmpty()) {
                return new int[]{1};
            }
            String[] parts = content.split(",");
            int[] indices = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                indices[i] = Integer.parseInt(parts[i].trim());
            }
            return indices;
        }

        // MATLAB range notation: 1:2:10 (start:step:end) or 1:10 (start:end)
        if (trimmed.contains(":")) {
            String[] parts = trimmed.split(":");
            if (parts.length == 2) {
                // start:end (step = 1)
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                return generateRange(start, 1, end);
            } else if (parts.length == 3) {
                // start:step:end
                int start = Integer.parseInt(parts[0].trim());
                int step = Integer.parseInt(parts[1].trim());
                int end = Integer.parseInt(parts[2].trim());
                return generateRange(start, step, end);
            }
        }

        // Single integer
        try {
            int index = Integer.parseInt(trimmed);
            return new int[]{index};
        } catch (NumberFormatException e) {
            return new int[]{1}; // Default
        }
    }

    /**
     * Generate range array from MATLAB-style start:step:end
     * @param start Starting value
     * @param step Step size
     * @param end Ending value
     * @return Array of indices in the range
     */
    private int[] generateRange(int start, int step, int end) {
        if (step == 0) {
            return new int[]{start};
        }

        List<Integer> indices = new ArrayList<>();
        if (step > 0) {
            for (int i = start; i <= end; i += step) {
                indices.add(i);
            }
        } else {
            for (int i = start; i >= end; i += step) {
                indices.add(i);
            }
        }

        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    // === Code Generation Methods ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // Add index vector data for template
        context.put("indexMode", indexMode);
        context.put("indexParamArray", indexParamArray);
        context.put("IndexVectorParsed", indexVector);
        context.put("IndexVectorSize", indexVector.length);

        String initCode = TemplateManager.renderTemplate("c/route/IndexVector/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        TemplateUtils.populateAllContext(context, this);

        // Add output port signal name
        context.put("outputVar", getOutputPortVariable(0));
        context.put("IndexVectorSize", indexVector.length);

        String outputCode = TemplateManager.renderTemplate("c/route/IndexVector/output.vm", context);
        code.addOutputCode(outputCode);
    }

    @Override
    public void calculateOutput(double t) {
        // Output the pre-computed index vector
        Matrix outputMatrix = new Matrix(indexVector.length, 1);

        for (int i = 0; i < indexVector.length; i++) {
            outputMatrix.set(i, 0, indexVector[i]);
        }

        outputPortList.get(0).setData(new Data(outputMatrix));
    }

    @Override
    public void calculateInit() {
        // Initialize output with index vector
        calculateOutput(0.0);
    }
}
