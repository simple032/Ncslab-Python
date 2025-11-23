package com.ncslab.block.route;

import com.ncslab.block.route.RouteBlock;
import lombok.Getter;

import org.apache.yetus.audience.InterfaceAudience.Public;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.route.SelectorDto;

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

import Jama.Matrix;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Selector block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Extracts specified elements from input signal (vector or matrix) based on
 * configured index selection mode.
 *
 * SIMULINK Parameters:
 * - IndexMode: Selection mode ("Index vector", "Starting index", "Index option")
 * - IndexVector: Indices to select (MATLAB syntax: [1, 3, 5] or 1:2:10)
 * - IndexOptions: Options for Index option mode ("All", "Rows", "Columns")
 * - NumberOfDimensions: Input dimensions (1 for vector, 2 for matrix)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 *
 * Selection Modes:
 * 1. Index Vector: Select specific elements by index array or MATLAB range
 *    - Example: [1, 3, 5] selects 1st, 3rd, and 5th elements
 *    - Example: 1:2:10 selects elements 1, 3, 5, 7, 9
 * 2. Starting Index: Select elements from starting index to end
 * 3. Index Option: Select all elements, specific rows, or specific columns
 *
 * Note: Uses MATLAB-style 1-based indexing (first element is index 1, not 0)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class Selector extends RouteBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter indexMode;
    private final Parameter indexVector;
    private final Parameter indexOptions;
    private final Parameter numberOfDimensions;
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

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
        PARAMETER_DEFAULTS.put("IndexMode", "Index vector");
        PARAMETER_DEFAULTS.put("IndexVector", "[1]");
        PARAMETER_DEFAULTS.put("IndexOptions", "All");
        PARAMETER_DEFAULTS.put("NumberOfDimensions", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }
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

        // Output port defaults (selector has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private Selector(Parameter indexMode, Parameter indexVector, Parameter indexOptions,
                    Parameter numberOfDimensions, Parameter sampleTime, Parameter outDataType,
                    String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.indexMode = Objects.requireNonNull(indexMode, "IndexMode parameter cannot be null");
        this.indexVector = Objects.requireNonNull(indexVector, "IndexVector parameter cannot be null");
        this.indexOptions = Objects.requireNonNull(indexOptions, "IndexOptions parameter cannot be null");
        this.numberOfDimensions = Objects.requireNonNull(numberOfDimensions, "NumberOfDimensions parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.indexMode);
        parameterList.add(this.indexVector);
        parameterList.add(this.indexOptions);
        parameterList.add(this.numberOfDimensions);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Selector(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility
        this.indexMode = getParameterByName("IndexMode");
        this.indexVector = getParameterByName("IndexVector");
        this.indexOptions = getParameterByName("IndexOptions");
        this.numberOfDimensions = getParameterByName("NumberOfDimensions");
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
    public Selector(SelectorDto dto, NCSLabModel model) {
        super(dto, model);

        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }

        // Initialize from DTO parameters using new Parameter creation
        this.indexMode = getParameterByName("IndexMode");
        this.indexVector = getParameterByName("IndexVector");
        this.indexOptions = getParameterByName("IndexOptions");
        this.numberOfDimensions = getParameterByName("NumberOfDimensions");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize ports
        initializePorts();

        // Complete initialization
        System.out.println("Enhanced DTO: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Selector fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter indexMode = createIndexModeFromJSON(paramValues, blockName);
            Parameter indexVector = createIndexVectorFromJSON(paramValues, blockName);
            Parameter indexOptions = createIndexOptionsFromJSON(paramValues, blockName);
            Parameter numberOfDimensions = createNumberOfDimensionsFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);

            Selector block = new Selector(indexMode, indexVector, indexOptions, numberOfDimensions,
                                         sampleTime, outDataType, blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, indexMode, indexVector, indexOptions,
                                      numberOfDimensions, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Selector block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Selector create(String name, String path, String indexVector, NCSLabModel model) {
        return create(name, path, "Index vector", indexVector, "All", 1, -1.0,
                     "Inherit: Inherit via internal rule", model);
    }

    /**
     * Create a Selector block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param indexMode Index mode ("Index vector", "Starting index", "Index option")
     * @param indexVector Index specification (e.g., "[1, 3, 5]" or "1:2:10")
     * @param indexOptions Index options ("All", "Rows", "Columns")
     * @param numberOfDimensions Number of dimensions (1 for vector, 2 for matrix)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return Selector block instance
     */
    public static Selector create(String name, String path, String indexMode, String indexVector,
                                 String indexOptions, int numberOfDimensions, double sampleTime,
                                 String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        SelectorDto dto = SelectorDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .indexMode(com.ncslab.dto.common.TypedParameter.of(indexMode))
            .indexVector(com.ncslab.dto.common.TypedParameter.of(indexVector))
            .indexOptions(com.ncslab.dto.common.TypedParameter.of(indexOptions))
            .numberOfDimensions(com.ncslab.dto.common.TypedParameter.of(numberOfDimensions))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Selector parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new Selector(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createIndexModeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("IndexMode", "Index vector");
        return new Parameter(null, 1, "IndexMode", value);
    }

    private static Parameter createIndexVectorFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("IndexVector", "[1]");
        return new Parameter(null, 2, "IndexVector", value);
    }

    private static Parameter createIndexOptionsFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("IndexOptions", "All");
        return new Parameter(null, 3, "IndexOptions", value);
    }

    private static Parameter createNumberOfDimensionsFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("NumberOfDimensions", "1");
        return new Parameter(null, 4, "NumberOfDimensions", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 5, "SampleTime", value);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("OutDataTypeStr", "Inherit: Inherit via internal rule");
        return new Parameter(null, 6, "OutDataTypeStr", value);
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

    private static void setParameterBlockReference(Selector block, Parameter... parameters) {
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
        identity.put("blockType", "Selector");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Single input port
        input = new InputPort(this, 1);
        inputPortList.add(input);

        // Single output port (has feedthrough)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add index parameters for template
        context.put("indexMode", indexMode);
        context.put("indexVector", indexVector);
        context.put("indexOptions", indexOptions);

        String initCode = TemplateManager.renderTemplate("c/route/Selector/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add input/output port signal names
        context.put("inputPort0SignalName", getInputPortVariable(0));
        context.put("outputPort0SignalName", getOutputPortVariable(0));

        // Add index parameters
        context.put("indexMode", indexMode);
        context.put("indexVector", indexVector);
        context.put("indexOptions", indexOptions);
        context.put("numberOfDimensions", numberOfDimensions);

        String outputCode = TemplateManager.renderTemplate("c/route/Selector/output.vm", context);
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);

        OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Parse index vector to determine output size
        int[] indices = parseIndexVector(indexVector.getInitString());
        int outputSize = indices.length;

        // Set output dimensions based on number of selected indices
        if (outputSize == 1) {
            // Single element - scalar output
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        } else {
            // Multiple elements - vector output (column vector)
            out.setHeight(outputSize);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(outputSize);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }

        // Copy data type from input
        out.getOutputSignalC().setCDataType(inputSignal.getCDataType());
    }

    public void checkDimension() throws MatDimException {
        InputPort inputPort = this.getInputPortList().get(0);

        // Validate that input has sufficient elements for selection
        int inputSize = inputPort.getVectorSize();
        int[] indices = parseIndexVector(indexVector.getInitString());

        // Check that all indices are within bounds (MATLAB 1-based indexing)
        for (int idx : indices) {
            if (idx < 1 || idx > inputSize) {
                MatDimException e = new MatDimException(
                    "Block " + this.blockName + " index out of bounds!\n" +
                    "Index " + idx + " is invalid for input vector of size " + inputSize + "\n" +
                    "Note: Indices are 1-based (MATLAB convention)\n"
                );
                throw(e);
            }
        }
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Selector block logic:
        // Extract specified elements from input signal based on index specification

        // Get input data
        Data inputData = inputPortList.get(0).getData();

        // Parse index vector (1-based MATLAB indexing)
        int[] indices = parseIndexVector(indexVector.getInitString());

        // Create output data
        if (indices.length == 1) {
            // Single element - scalar output
            Data outputData = new Data(1, 1);
            double value = extractElement(inputData, indices[0]);
            outputData.setInitValue(value);
            outputPortList.get(0).setData(outputData);
        } else {
            // Multiple elements - vector output (column vector)
            Matrix outputMatrix = new Matrix(indices.length, 1);
            for (int i = 0; i < indices.length; i++) {
                double value = extractElement(inputData, indices[i]);
                outputMatrix.set(i, 0, value);
            }
            Data outputData = new Data(outputMatrix);
            outputPortList.get(0).setData(outputData);
        }
    }

    /**
     * Extract element from input data using 1-based MATLAB indexing
     * @param inputData Input data (scalar, vector, or matrix)
     * @param index 1-based index (MATLAB convention)
     * @return Extracted element value
     */
    private double extractElement(Data inputData, int index) {
        // Convert 1-based MATLAB index to 0-based Java index
        int zeroBasedIndex = index - 1;

        if (inputData.getDataType() == DataType.MATRIX) {
            Matrix matrix = inputData.getMatrix();
            if (matrix != null) {
                int height = inputData.getHeight();
                int width = inputData.getWidth();

                if (height == 1) {
                    // Row vector - extract column
                    if (zeroBasedIndex < width) {
                        return matrix.get(0, zeroBasedIndex);
                    }
                } else if (width == 1) {
                    // Column vector - extract row
                    if (zeroBasedIndex < height) {
                        return matrix.get(zeroBasedIndex, 0);
                    }
                } else {
                    // General matrix - extract in row-major order
                    int totalElements = height * width;
                    if (zeroBasedIndex < totalElements) {
                        int row = zeroBasedIndex / width;
                        int col = zeroBasedIndex % width;
                        return matrix.get(row, col);
                    }
                }
            }
        } else {
            // Scalar input
            if (zeroBasedIndex == 0) {
                return inputData.getInitValue();
            }
        }

        // Out of bounds - return 0
        return 0.0;
    }

    /**
     * Parse index vector string to integer array
     * Handles MATLAB syntax: [1, 3, 5], 1:2:10, etc.
     */
    private int[] parseIndexVector(String vector) {
        if (vector == null || vector.trim().isEmpty()) {
            return new int[]{1}; // Default to first element
        }

        String trimmed = vector.trim();

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
}
