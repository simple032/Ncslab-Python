package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.ProductDto;
import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.util.TemplateManager;

import Jama.Matrix;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Product block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - Inputs: String sequence defining input operations (e.g., "**", "", "/")
 * - Multiplication: Element-wise or Matrix multiplication mode
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - InputSameDT: Require inputs to have same data type
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Product extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter inputs;
    private final Parameter multiplication;
    private final Parameter sampleTime;
    private final Parameter inputSameDT;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Operational Settings ===
    @Getter
    private final String inputSequence;
    @Getter
    private final boolean matrixMultiplication;

    // === Static Parameter Definitions ===

    // Parameter defaults
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "**");  // Two multiplication inputs
        PARAMETER_DEFAULTS.put("Multiplication", "Element-wise(.*)");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("InputSameDT", "on");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        // Input names are dynamic based on sequence length
    }

    // === Private Constructor with Typed Parameters ===
    private Product(Parameter inputs, Parameter multiplication, Parameter sampleTime,
                   Parameter inputSameDT, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                   String blockName, String blockPath, String blockUUID,
                   NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Extract input sequence and multiplication mode from parameters
        this.inputSequence = inputs.getInitString();
        this.matrixMultiplication = "Matrix(*)".equals(multiplication.getInitString());

        // Validate parameters
        validateParameters(inputs, sampleTime);

        // Assign parameters
        this.inputs = Objects.requireNonNull(inputs, "Inputs parameter cannot be null");
        this.multiplication = Objects.requireNonNull(multiplication, "Multiplication parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.inputSameDT = Objects.requireNonNull(inputSameDT, "InputSameDT parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.inputs);
        parameterList.add(this.multiplication);
        parameterList.add(this.sampleTime);
        parameterList.add(this.inputSameDT);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        //based on input sequence
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Product(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Extract input sequence and multiplication mode for backward compatibility
        this.inputSequence = paramValues.getString("Inputs");
        this.matrixMultiplication = "Matrix(*)".equals(paramValues.getString("Multiplication"));

        // Create legacy parameters
        this.inputs = getParameterByName("Inputs");
        this.multiplication = getParameterByName("Multiplication");

        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates Product block directly from BlockDto DTO
     */
    public Product(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.inputs = getParameterByName("Inputs");
        this.inputSequence = this.inputs.getInitString(); // Initialize final field from parameter
        this.multiplication = getParameterByName("Multiplication");
        this.matrixMultiplication = "Matrix(*)".equals(this.multiplication.getInitString());
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create Product block from ProductDto.
     *
     * @param dto The ProductDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New Product block instance
     * @throws BlockCreationException if block creation fails
     */
    public static Product createFromDto(ProductDto dto, NCSLabModel model) throws BlockCreationException {
        return new Product(dto, model);
    }
    // === Static Factory Method for JSON Deserialization ===
    public static Product fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            // Extract and validate JSON fields
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            // Create typed parameters from JSON with defaults
            Parameter inputs = createInputsFromJSON(paramValues, blockName);
            Parameter multiplication = createMultiplicationFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter inputSameDT = createInputSameDTFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Product block = new Product(inputs, multiplication, sampleTime, inputSameDT,
                                       outDataType, saturateParam, blockName, blockPath, blockUUID, model);

            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, inputs, multiplication, sampleTime, inputSameDT, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Product block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a Product block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param inputSequence Input sequence defining operations
     * @param model Parent model
     * @return Configured Product block instance
     */
    public static Product create(String name, String path, String inputSequence, NCSLabModel model) {
        return create(name, path, inputSequence, "Element-wise(*)", -1.0, true,
                     "Inherit: Same as input", false, model);
    }

    /**
     * Create a Product block with full parameters using DTO-based construction.
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param inputSequence Input sequence defining operations
     * @param multiplicationMode Element-wise or Matrix multiplication mode
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, greater than 0 for discrete)
     * @param inputSameDT Require inputs to have same data type
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Configured Product block instance
     */
    public static Product create(String name, String path, String inputSequence, String multiplicationMode,
                                double sampleTime, boolean inputSameDT, String outDataType,
                                boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        ProductDto dto = ProductDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .inputs(com.ncslab.dto.common.TypedParameter.of(inputSequence))
            .multiplication(com.ncslab.dto.common.TypedParameter.of(multiplicationMode))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .inputSameDT(com.ncslab.dto.common.TypedParameter.of(inputSameDT))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Product parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Product(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter inputs, Parameter sampleTime) {
        // Validate input sequence
        String sequence = inputs.getInitString();
        if (sequence == null || sequence.trim().isEmpty()) {
            throw new IllegalArgumentException("Input sequence cannot be empty");
        }

        // Validate sequence contains only * and / characters
        for (char c : sequence.toCharArray()) {
            if (c != '*' && c != '/') {
                throw new IllegalArgumentException("Input sequence must contain only '*' and '/' characters");
            }
        }

        if (sequence.isEmpty()) {
            throw new IllegalArgumentException("Input sequence must have at least one input");
        }

        // Validate sample time
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInputsFromJSON(JSONObject paramValues, String blockName) {
        String inputsValue = paramValues.optString("Inputs", "**");
        return new Parameter(null, 1, "Inputs", inputsValue);
    }

    private static Parameter createMultiplicationFromJSON(JSONObject paramValues, String blockName) {
        String multiplicationValue = paramValues.optString("Multiplication", "Element-wise(*)");
        return new Parameter(null, 2, "Multiplication", multiplicationValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createInputSameDTFromJSON(JSONObject paramValues, String blockName) {
        String inputSameDTValue = paramValues.optString("InputSameDT", "on");
        return new Parameter(null, 4, "InputSameDT", inputSameDTValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
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

    private static void setParameterBlockReference(Product block, Parameter... parameters) {
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
        identity.put("blockType", "Product");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Create output port with feedthrough (product is instantaneous)
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        // Create input ports based on sequence length
        for (int i = 0; i < inputSequence.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        // Update input names for static reference
        inputNames.clear();
        for (int i = 0; i < inputSequence.length(); i++) {
            inputNames.add("in" + (i + 1));
        }
    }

    // === Legacy Compatibility Methods ===
    @Deprecated
    private String getSequence() {
        return inputSequence;
    }

    @Deprecated
    private boolean isMultiplication() {
        return matrixMultiplication;
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add port data type context for scalar expansion handling
        // This provides inputHeights, inputWidths, inputIsMatrix arrays
        com.ncslab.util.TemplateUtils.populatePortDataTypeContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/Product/output.vm", context);
        code.addOutputCode(codeStr);
    }


    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] signal = new OutputSignal[inputSequence.length()];
        int[] m = new int[inputSequence.length()];
        int[] n = new int[inputSequence.length()];

        for (int i = 0; i < inputSequence.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            m[i] = signal[i].getHeight();
            n[i] = signal[i].getWidth();
        }

        System.out.println("updateDimension (" + blockName + ") - Input count: " + signal.length);
        for (int i = 0; i < signal.length; i++) {
            System.out.println("  Input " + i + ": [" + signal[i].getHeight() + "x" + signal[i].getWidth() + "]");
        }    

        if (!isMatrixMultiplication()) {
            // Element-wise multiplication mode with SIMULINK-compatible scalar expansion
            // Supported operations:
            //   Scalar × Scalar = Scalar
            //   Scalar × Matrix = Matrix (scalar expanded)
            //   Matrix × Scalar = Matrix (scalar expanded)
            //   Matrix × Matrix (same dimensions) = Matrix (element-wise)

            // Find the maximum dimensions (non-scalar dimension if present)
            int maxHeight = 1;
            int maxWidth = 1;
            boolean hasMatrix = false;

            for (int i = 0; i < signal.length; i++) {
                boolean isScalar = (m[i] == 1 && n[i] == 1);
                if (!isScalar) {
                    hasMatrix = true;
                    if (maxHeight == 1 && maxWidth == 1) {
                        // First non-scalar sets the reference dimensions
                        maxHeight = m[i];
                        maxWidth = n[i];
                    } else {
                        // Verify all non-scalar inputs have the same dimensions
                        if (m[i] != maxHeight || n[i] != maxWidth) {
                            throw new MatDimException(
                                String.format("Block %s: Non-scalar input dimensions must match. " +
                                    "Found [%d×%d] and [%d×%d]",
                                    blockName, maxHeight, maxWidth, m[i], n[i]));
                        }
                    }
                }
            }

            // Set output dimensions
            out.setHeight(maxHeight);
            out.setWidth(maxWidth);
            out.getOutputSignalC().setHeight(maxHeight);
            out.getOutputSignalC().setWidth(maxWidth);
            out.getOutputSignalC().setDataType(hasMatrix ? DataType.MATRIX : DataType.REAL);
            // Debug: Print output dimensions
            System.out.println("updateDimension (" + blockName + ") - Output: [" + maxHeight + "x" + maxWidth + "]");
        } else {
            // Matrix multiplication mode with scalar expansion and fallback support
            // Rules:
            //   Scalar * Scalar = Scalar
            //   Scalar * Matrix = Matrix (scalar expansion, element-wise)
            //   Matrix * Scalar = Matrix (scalar expansion, element-wise)
            //   Matrix * Matrix (compatible dims) = Matrix multiplication [m×n] * [n×p] = [m×p]
            //   Matrix * Matrix (same dims, incompatible for matmul) = Element-wise multiplication

            // CRITICAL FIX: For feedback loops with Delay blocks initialized to scalar,
            // we need to infer proper dimensions for matrix multiplication rather than
            // treating [1×1] as scalar, which can lead to wrong equilibrium dimensions.

            // Check if we have exactly one non-scalar and one [1×1] that could need dimension inference
            int nonScalarCount = 0;
            int scalarIndex = -1;
            int nonScalarIndex = -1;

            for (int i = 0; i < signal.length; i++) {
                boolean isScalar = (m[i] == 1 && n[i] == 1);
                if (!isScalar) {
                    nonScalarCount++;
                    nonScalarIndex = i;
                } else {
                    scalarIndex = i;
                }
            }

            // Special handling for 2-input matrix multiplication with one [1×1]
            // If we have [1×n] × [1×1], infer that [1×1] should be [n×1] for proper matrix multiplication
            // This prevents wrong equilibrium in feedback loops with Delay blocks
            if (signal.length == 2 && nonScalarCount == 1 && scalarIndex >= 0) {
                int nonScalarHeight = m[nonScalarIndex];
                int nonScalarWidth = n[nonScalarIndex];

                // Check if this is a row vector × scalar case that should be row vector × column vector
                if ((nonScalarHeight == 1 && nonScalarWidth > 1) || (nonScalarHeight > 1 && nonScalarWidth == 1)) {
                    // Get the source block for the scalar input
                    OutputPort scalarSource = inputPortList.get(scalarIndex).getLinkedLine().getLinkedOutputPort();
                    Block sourceBlock = scalarSource.getBlock();

                    // If source is a Delay block with scalar IC in a feedback loop,
                    // infer the required dimension for matrix multiplication
                    if (sourceBlock instanceof com.ncslab.block.discrete.Delay) {
                        System.out.println("DEBUG " + blockName + ": Detected Delay block with [1×1] in matrix multiplication");
                        System.out.println("  Non-scalar input: [" + nonScalarHeight + "×" + nonScalarWidth + "]");

                        // For [1×n] × [?×?] matrix multiplication, we need [?×?] = [n×k] to produce [1×k]
                        // For [m×1] × [?×?] matrix multiplication, we need [?×?] = [1×k] to produce [m×k]
                        int inferredHeight, inferredWidth;

                        if (nonScalarIndex == 0) {
                            // Pattern: [1×n] × [1×1] → should be [1×n] × [n×k]
                            // Infer [1×1] should be [n×1] to produce [1×1] (most restrictive)
                            if (nonScalarHeight == 1 && nonScalarWidth > 1) {
                                inferredHeight = nonScalarWidth;
                                inferredWidth = 1;
                                System.out.println("  Inferred Delay dimension for matmul: [" + inferredHeight + "×" + inferredWidth + "]");

                                // Update the scalar signal dimensions
                                m[scalarIndex] = inferredHeight;
                                n[scalarIndex] = inferredWidth;
                                signal[scalarIndex].setHeight(inferredHeight);
                                signal[scalarIndex].setWidth(inferredWidth);
                                signal[scalarIndex].setDataType(DataType.MATRIX);

                                // Update source port dimensions to propagate back
                                scalarSource.setHeight(inferredHeight);
                                scalarSource.setWidth(inferredWidth);
                                scalarSource.getOutputSignalC().setHeight(inferredHeight);
                                scalarSource.getOutputSignalC().setWidth(inferredWidth);
                                scalarSource.getOutputSignalC().setDataType(DataType.MATRIX);
                            }
                        }
                    }
                }
            }

            // Find non-scalar dimensions and determine output dimensions
            int outHeight = 1;
            int outWidth = 1;
            boolean hasMatrix = false;

            // First pass: identify output dimensions
            for (int i = 0; i < signal.length; i++) {
                boolean isScalar = (m[i] == 1 && n[i] == 1);
                if (!isScalar) {
                    if (!hasMatrix) {
                        // First non-scalar matrix sets initial dimensions
                        outHeight = m[i];
                        outWidth = n[i];
                        hasMatrix = true;
                    } else {
                        // Subsequent non-scalar matrices
                        // Check if matrix multiplication is possible
                        if (outWidth == m[i]) {
                            // Compatible for matrix multiplication: [outHeight × outWidth] * [m[i] × n[i]]
                            // Result dimensions: [outHeight × n[i]]
                            outWidth = n[i];
                        } else if (outHeight == m[i] && outWidth == n[i]) {
                            // Same dimensions but not compatible for matrix multiplication
                            // Will fall back to element-wise multiplication
                            // Output dimensions remain [outHeight × outWidth]
                        } else {
                            throw new MatDimException(
                                String.format("Block %s: Matrix dimensions [%d×%d] and [%d×%d] are incompatible " +
                                    "for both matrix multiplication and element-wise multiplication",
                                    blockName, outHeight, outWidth, m[i], n[i]));
                        }
                    }
                }
                // Scalars don't affect output dimensions
            }

            out.setHeight(outHeight);
            out.setWidth(outWidth);
            out.getOutputSignalC().setHeight(outHeight);
            out.getOutputSignalC().setWidth(outWidth);
            out.getOutputSignalC().setDataType(hasMatrix ? DataType.MATRIX : DataType.REAL);
            // Debug: Print output dimensions
            System.out.println("updateDimension (" + blockName + ") - Output: [" + outHeight + "x" + outWidth + "]");
        }

    }

    public void checkDimension() throws MatDimException {
        // Validate all input dimensions are non-zero
        for (int i = 0; i < inputPortList.size(); i++) {
            OutputSignal signal = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            int height = signal.getHeight();
            int width = signal.getWidth();

            if (height == 0 || width == 0) {
                throw new MatDimException(
                    String.format("Product block '%s': Input %d has invalid dimensions [%d×%d]. " +
                        "All input dimensions must be at least [1×1].",
                        blockName, i+1, height, width));
            }
        }
    }

    @Override
    public void calculateInit() {
        // Initialization logic for Product block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // IMPORTANT: For matrix multiplication, initialize result from FIRST INPUT, not identity matrix
        // This ensures correct multiplication order: input[0] * input[1] * ... * input[n]
        // Initialize with first input's data
        Data firstInput = inputPortList.get(0).getData();
        Data resultData;
        if (firstInput.getDataType() == DataType.MATRIX) {
            resultData = new Data(firstInput.getMatrix().copy());
        } else {
            resultData = new Data(firstInput.getInitValue());
        }

        // Check if inputSequence is numeric (count) or operator string
        boolean isNumericInput = inputSequence.length() < inputPortList.size();

        if (!isMatrixMultiplication()) {
            // Element-wise operations (with scalar expansion support)
            // Start from i=1 since we already initialized with input[0]
            for (int i = 1; i < inputPortList.size(); i++) {
                char operation;

                if (isNumericInput) {
                    // Numeric input: use default '*' operation
                    operation = '*';
                } else {
                    // String input: get operation from sequence
                    operation = inputSequence.charAt(i);
                }

                if (operation == '*') {
                    // Use arrayTimes for element-wise multiplication (supports scalar expansion)
                    resultData = resultData.arrayTimes(inputPortList.get(i).getData());
                } else if (operation == '/') {
                    resultData = resultData.divide(inputPortList.get(i).getData());
                }
            }
        } else {
            // Matrix multiplication mode with scalar expansion support
            // Start from i=1 since we already initialized with input[0]
            for (int i = 1; i < inputPortList.size(); i++) {
                char operation;

                if (isNumericInput) {
                    // Numeric input: use default '*' operation
                    operation = '*';
                } else {
                    // String input: get operation from sequence
                    operation = inputSequence.charAt(i);
                }

                Data inputData = inputPortList.get(i).getData();

                if (operation == '*') {
                    // Check if either operand is scalar
                    boolean resultIsScalar = (resultData.getDataType() == DataType.REAL);
                    boolean inputIsScalar = (inputData.getDataType() == DataType.REAL);

                    if (resultIsScalar || inputIsScalar) {
                        // Scalar expansion: use arrayTimes for element-wise multiplication
                        resultData = resultData.arrayTimes(inputData);
                    } else {
                        // Both are matrices: check if dimensions are compatible for matrix multiplication
                        resultData = resultData.times(inputData);
                    }
                } else if (operation == '/')  {
                    resultData = resultData.divide(inputData);
                }
            }
        }

        out.setData(resultData);
    }
}

