package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.ReshapeDto;

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
 * Reshape block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * The Reshape block reshapes the input signal to specified output dimensions
 * while preserving the total number of elements.
 * Uses column-major ordering (MATLAB convention).
 *
 * Example:
 * - Input: [1 2 3 4 5 6] (1x6 vector)
 * - OutputDimensions: [2, 3]
 * - Output: [[1 3 5], [2 4 6]] (2x3 matrix, column-major ordering)
 *
 * SIMULINK Parameters:
 * - OutputDimensions: Target dimensions as [rows, cols]
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
public class Reshape extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter outputDimensions;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // Cached parsed dimensions
    private int[] parsedDimensions;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("OutputDimensions", "1,1");  // Default to scalar
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
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

        // Input port defaults (accepts matrix)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "MATRIX");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (outputs reshaped matrix)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "MATRIX");
        output1.put("feedthrough", true);  // Reshape has feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private Reshape(Parameter outputDimensions, Parameter sampleTime,
                   Parameter outDataType, Parameter saturateOnIntegerOverflow,
                   String blockName, String blockPath,
                   String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate and parse dimensions
        this.parsedDimensions = parseDimensions(outputDimensions.getInitString());

        // Assign parameters
        this.outputDimensions = Objects.requireNonNull(outputDimensions, "Output dimensions parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.outputDimensions);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Reshape(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.outputDimensions = getParameterByName("OutputDimensions");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Parse dimensions
        this.parsedDimensions = parseDimensions(outputDimensions.getInitString());

        initializePorts();
    }

    /**
     * DTO Constructor - Creates Reshape block directly from ReshapeDto DTO
     */
    public Reshape(ReshapeDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO with defaults
        String outputDimensionsValue = dto.getOutputDimensionsValue();
        String sampleTimeValue = dto.getSampleTime() != null ? (dto.getSampleTime().getAsString()) : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";

        // Parse and validate dimensions
        this.parsedDimensions = parseDimensions(outputDimensionsValue);

        // Validate sample time
        double sampleTimeDouble = Double.parseDouble(sampleTimeValue);
        if (sampleTimeDouble != -1.0 && sampleTimeDouble < 0.0) {
            throw new IllegalArgumentException("Sample time must be non-negative or -1 (inherited)");
        }

        // Initialize parameters
        this.outputDimensions = getParameterByName("OutputDimensions");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        initializePorts();

        System.out.println("DTO-SPECIFIC: Reshape block created successfully from ReshapeDto - " + dto.getBlockName());
    }

    /**
     * Factory method to create Reshape block from ReshapeDto.
     *
     * @param dto The ReshapeDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New Reshape block instance
     * @throws BlockCreationException if block creation fails
     */
    public static Reshape createFromDto(ReshapeDto dto, NCSLabModel model) throws BlockCreationException {
        return new Reshape(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Reshape fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter outputDimensions = createOutputDimensionsFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Reshape block = new Reshape(outputDimensions, sampleTime, outDataType, saturateParam,
                                       blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, outputDimensions, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Reshape block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a Reshape block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param rows Target number of rows
     * @param cols Target number of columns
     * @param model Parent model
     * @return Configured Reshape block instance
     */
    public static Reshape create(String name, String path, int rows, int cols, NCSLabModel model) {
        return create(name, path, rows + "," + cols, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a Reshape block with full parameters using DTO-based construction.
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param outputDimensions Output dimensions as "rows,cols"
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Configured Reshape block instance
     */
    public static Reshape create(String name, String path, String outputDimensions,
                                double sampleTime, String outDataType,
                                boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        ReshapeDto dto = ReshapeDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .outputDimensions(com.ncslab.dto.common.TypedParameter.of(outputDimensions))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Reshape parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Reshape(dto, model);
    }

    // === Dimension Parsing ===
    /**
     * Parse dimensions string "rows,cols" into int array
     */
    private static int[] parseDimensions(String dimensionsStr) {
        String[] parts = dimensionsStr.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Output dimensions must specify at least 2 dimensions (rows,cols)");
        }

        int[] dimensions = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                dimensions[i] = Integer.parseInt(parts[i].trim());
                if (dimensions[i] <= 0) {
                    throw new IllegalArgumentException("All dimensions must be positive integers");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid dimension value: " + parts[i]);
            }
        }

        return dimensions;
    }

    /**
     * Calculate total number of elements from dimensions
     */
    private int getTotalElements() {
        int total = 1;
        for (int dim : parsedDimensions) {
            total *= dim;
        }
        return total;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createOutputDimensionsFromJSON(JSONObject paramValues, String blockName) {
        String dimensionsValue = paramValues.optString("OutputDimensions", "1,1");
        return new Parameter(null, 1, "OutputDimensions", dimensionsValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
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

    private static void setParameterBlockReference(Reshape block, Parameter... parameters) {
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
        identity.put("blockType", "Reshape");
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

        // Add dimension information to context
        context.put("outputRows", parsedDimensions[0]);
        context.put("outputCols", parsedDimensions[1]);

        String codeStr = TemplateManager.renderTemplate("c/matrix/Reshape/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // Get input dimensions
        InputPort in = inputPortList.get(0);
        OutputSignal inSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Calculate input total elements
        int inputTotalElements;
        if (inSignal.getDataType() == DataType.MATRIX) {
            inputTotalElements = inSignal.getHeight() * inSignal.getWidth();
        } else {
            inputTotalElements = 1;  // Scalar
        }

        // Calculate output total elements
        int outputTotalElements = getTotalElements();

        // Verify element count matches
        if (inputTotalElements != outputTotalElements) {
            throw new MatDimException("Reshape: Input has " + inputTotalElements +
                    " elements but output dimensions require " + outputTotalElements + " elements");
        }

        // Set output dimensions
        OutputPort out = outputPortList.get(0);
        out.setHeight(parsedDimensions[0]);
        out.setWidth(parsedDimensions[1]);
        out.getOutputSignalC().setHeight(parsedDimensions[0]);
        out.getOutputSignalC().setWidth(parsedDimensions[1]);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    public void checkDimension() throws MatDimException {
        // Get input port
        InputPort in = inputPortList.get(0);

        // Check that input is connected
        if (in.getLinkedLine() == null) {
            throw new MatDimException("Reshape: Input port not connected");
        }

        // Get input signal
        OutputSignal inSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Calculate input total elements
        int inputTotalElements;
        if (inSignal.getDataType() == DataType.MATRIX) {
            inputTotalElements = inSignal.getHeight() * inSignal.getWidth();
        } else {
            inputTotalElements = 1;  // Scalar
        }

        // Calculate output total elements
        int outputTotalElements = getTotalElements();

        // Verify element count matches
        if (inputTotalElements != outputTotalElements) {
            throw new MatDimException("Block " + this.blockName + " dimension error!\n" +
                    "Input has " + inputTotalElements + " elements (" +
                    inSignal.getHeight() + "x" + inSignal.getWidth() + "), " +
                    "but output dimensions [" + parsedDimensions[0] + "," + parsedDimensions[1] +
                    "] require " + outputTotalElements + " elements.\n" +
                    "Reshape requires input and output to have the same number of elements.");
        }
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Reshape block: reshapes input to specified output dimensions
        // Uses column-major ordering (MATLAB convention)
        Data inputData = inputPortList.get(0).getData();

        // Get input as flat array (column-major order)
        double[] flatArray;
        if (inputData.getDataType() == DataType.MATRIX) {
            Jama.Matrix inputMatrix = inputData.getMatrix();
            int inputRows = inputMatrix.getRowDimension();
            int inputCols = inputMatrix.getColumnDimension();

            flatArray = new double[inputRows * inputCols];
            // Extract in column-major order (MATLAB convention)
            int idx = 0;
            for (int col = 0; col < inputCols; col++) {
                for (int row = 0; row < inputRows; row++) {
                    flatArray[idx++] = inputMatrix.get(row, col);
                }
            }
        } else {
            // Scalar input
            flatArray = new double[] { inputData.getInitValue() };
        }

        // Verify element count
        int outputTotalElements = getTotalElements();
        if (flatArray.length != outputTotalElements) {
            throw new IllegalArgumentException("Reshape: Element count mismatch. Input has " +
                    flatArray.length + " elements but output requires " + outputTotalElements + " elements");
        }

        // Reshape to output dimensions (column-major order)
        int outputRows = parsedDimensions[0];
        int outputCols = parsedDimensions[1];

        Jama.Matrix outputMatrix = new Jama.Matrix(outputRows, outputCols);
        int idx = 0;
        for (int col = 0; col < outputCols; col++) {
            for (int row = 0; row < outputRows; row++) {
                outputMatrix.set(row, col, flatArray[idx++]);
            }
        }

        Data outputData = new Data(outputMatrix);
        outputPortList.get(0).setData(outputData);
    }
}
