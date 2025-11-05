package com.ncslab.block.string;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.StringConcatenateDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.StringData;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * String Concatenate block with SIMULINK-compatible parameters.
 *
 * Concatenates multiple input strings into one output string.
 * This block is part of the SIMULINK string manipulation blocks suite.
 *
 * SIMULINK Parameters:
 * - NumberOfInputs: Number of strings to concatenate (default: 2, range: 1-32)
 * - MaximumLength: Maximum length of output in bytes including null-terminator (default: 0 = sum of inputs, unlimited)
 * - OutputDimensionsMode: "Fixed-size" or "Variable-size" (default: "Fixed-size")
 *
 * Behavior:
 * - Concatenates inputs in port order (Input1 + Input2 + Input3 + ...)
 * - Does NOT insert separators (spaces, etc.) between strings
 * - If MaximumLength > 0 and result exceeds it, truncate to max length
 * - If MaximumLength = 0, output dimension = sum of input dimensions
 *
 * Examples:
 * - Input1="Hello", Input2=" ", Input3="World" → Output="Hello World"
 * - Input1="Test", Input2="123" with MaximumLength=5 → Output="Test1"
 *
 * Character Set: ISO/IEC 8859-1 (first 256 Unicode code points)
 * Encoding: UTF-8 for C code generation
 * C Representation: Null-terminated uint8 vector (std::string)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class StringConcatenate extends Block {

    // === String Concatenate-Specific SIMULINK Parameters ===
    /** Number of input strings to concatenate (1-32) */
    @Getter
    private final Parameter numberOfInputs;

    /** Maximum length of output string in bytes (0 = unlimited) */
    @Getter
    private final Parameter maximumLength;

    /** Output dimensions mode: "Fixed-size" or "Variable-size" */
    @Getter
    private final Parameter outputDimensionsMode;

    // === Static Parameter Definitions ===
    // Parameter defaults matching SIMULINK String Concatenate block
    public static final Map<String, Object> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("NumberOfInputs", 2);
        PARAMETER_DEFAULTS.put("MaximumLength", 0);  // 0 = unlimited (sum of inputs)
        PARAMETER_DEFAULTS.put("OutputDimensionsMode", "Fixed-size");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names - dynamically populated based on NumberOfInputs
        // Inputs will be: in1, in2, in3, ..., inN
        outputNames.add("out1");  // Single output port for concatenated string
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "StringConcatenate");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Private Constructor with Typed Parameters ===
    private StringConcatenate(Parameter numberOfInputs, Parameter maximumLength,
                             Parameter outputDimensionsMode,
                             String blockName, String blockPath, String blockUUID,
                             NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(numberOfInputs, maximumLength, outputDimensionsMode);

        // Assign String Concatenate-specific parameters
        this.numberOfInputs = Objects.requireNonNull(numberOfInputs, "NumberOfInputs parameter cannot be null");
        this.maximumLength = Objects.requireNonNull(maximumLength, "MaximumLength parameter cannot be null");
        this.outputDimensionsMode = Objects.requireNonNull(outputDimensionsMode, "OutputDimensionsMode parameter cannot be null");

        // Add String Concatenate-specific parameters to parameter list
        parameterList.add(numberOfInputs);
        parameterList.add(maximumLength);
        parameterList.add(outputDimensionsMode);

        // Initialize ports based on NumberOfInputs parameter
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public StringConcatenate(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.numberOfInputs = getParameterByName("NumberOfInputs");
        this.maximumLength = getParameterByName("MaximumLength");
        this.outputDimensionsMode = getParameterByName("OutputDimensionsMode");

        // Verify parameters are properly initialized
        if (this.numberOfInputs == null || this.maximumLength == null || this.outputDimensionsMode == null) {
            throw new IllegalStateException("StringConcatenate parameters not properly initialized");
        }

        // Initialize ports based on NumberOfInputs parameter
        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates StringConcatenate block directly from BlockDto DTO
     */
    public StringConcatenate(StringConcatenateDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.numberOfInputs = getParameterByName("NumberOfInputs");
        this.maximumLength = getParameterByName("MaximumLength");
        this.outputDimensionsMode = getParameterByName("OutputDimensionsMode");

        // Verify parameters are properly initialized
        if (this.numberOfInputs == null || this.maximumLength == null || this.outputDimensionsMode == null) {
            throw new IllegalStateException("StringConcatenate parameters not properly initialized from DTO");
        }

        // Initialize ports based on NumberOfInputs parameter
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static StringConcatenate fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter numberOfInputsParam = createNumberOfInputsFromJSON(paramValues, blockName);
            Parameter maxLengthParam = createMaximumLengthFromJSON(paramValues, blockName);
            Parameter outputModeParam = createOutputDimensionsModeFromJSON(paramValues, blockName);

            StringConcatenate block = new StringConcatenate(numberOfInputsParam, maxLengthParam, outputModeParam,
                                                           blockName, blockPath, blockUUID, model);

            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, numberOfInputsParam, maxLengthParam, outputModeParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create StringConcatenate block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static StringConcatenate create(String name, String path, int numberOfInputs, NCSLabModel model) {
        return create(name, path, numberOfInputs, 0, "Fixed-size", model);
    }

    /**
     * Create a StringConcatenate block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param numberOfInputs Number of input strings to concatenate (1-32)
     * @param maximumLength Maximum output length in bytes (0 = unlimited)
     * @param outputDimensionsMode Output dimensions mode ("Fixed-size" or "Variable-size")
     * @param model Parent model
     * @return StringConcatenate block instance
     */
    public static StringConcatenate create(String name, String path, int numberOfInputs,
                                          int maximumLength, String outputDimensionsMode,
                                          NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        StringConcatenateDto dto = StringConcatenateDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .numberOfInputs(numberOfInputs)
            .maximumLength(maximumLength)
            .outputDimensionsMode(outputDimensionsMode)
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid StringConcatenate parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new StringConcatenate(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter numberOfInputs, Parameter maximumLength,
                                          Parameter outputDimensionsMode) {
        // Validate NumberOfInputs (1-32)
        int numInputs = Integer.parseInt(numberOfInputs.getInitString());
        if (numInputs < 1 || numInputs > 32) {
            throw new IllegalArgumentException("NumberOfInputs must be between 1 and 32, got: " + numInputs);
        }

        // Validate MaximumLength (>= 0)
        int maxLength = Integer.parseInt(maximumLength.getInitString());
        if (maxLength < 0) {
            throw new IllegalArgumentException("MaximumLength must be >= 0, got: " + maxLength);
        }

        // Validate OutputDimensionsMode
        String mode = outputDimensionsMode.getInitString();
        if (mode == null || (!mode.equals("Fixed-size") && !mode.equals("Variable-size"))) {
            throw new IllegalArgumentException("OutputDimensionsMode must be 'Fixed-size' or 'Variable-size', got: " + mode);
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createNumberOfInputsFromJSON(JSONObject paramValues, String blockName) {
        int defaultNumInputs = (Integer) PARAMETER_DEFAULTS.get("NumberOfInputs");
        String numInputsStr = paramValues.optString("NumberOfInputs", String.valueOf(defaultNumInputs));
        return new Parameter(null, 1, "NumberOfInputs", numInputsStr);
    }

    private static Parameter createMaximumLengthFromJSON(JSONObject paramValues, String blockName) {
        int defaultMaxLength = (Integer) PARAMETER_DEFAULTS.get("MaximumLength");
        String maxLengthStr = paramValues.optString("MaximumLength", String.valueOf(defaultMaxLength));
        return new Parameter(null, 2, "MaximumLength", maxLengthStr);
    }

    private static Parameter createOutputDimensionsModeFromJSON(JSONObject paramValues, String blockName) {
        String defaultMode = (String) PARAMETER_DEFAULTS.get("OutputDimensionsMode");
        String modeStr = paramValues.optString("OutputDimensionsMode", defaultMode);
        return new Parameter(null, 3, "OutputDimensionsMode", modeStr);
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

    private static void setParameterBlockReference(StringConcatenate block, Parameter... parameters) {
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

    // === Port Initialization ===
    private void initializePorts() {
        int numInputs = Integer.parseInt(numberOfInputs.getInitString());

        // Create dynamic number of input ports based on NumberOfInputs parameter
        for (int i = 1; i <= numInputs; i++) {
            inputPortList.add(new InputPort(this, i));
        }

        // Create single output port (string)
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough

        System.out.println("StringConcatenate '" + blockName + "' initialized with " + numInputs + " input ports");
    }

    // === Dimension Management ===
    @Override
    public void updateDimension() throws MatDimException {
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            // String signals are scalar (1x1) with string data type
            out.setHeight(1);
            out.setWidth(1);

            // Set output to string data type (only if OutputSignal is initialized)
            if (out.getOutputSignalC() != null) {
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setCDataType(com.ncslab.block.data.CDataType.STRING);
                out.getOutputSignalC().setDataType(DataType.STRING);
                System.out.println("StringConcatenate '" + blockName + "' output CDataType set to: STRING");
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Verify all input ports have string data type (scalar 1x1)
        for (InputPort in : inputPortList) {
            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

                // Check if input is string type
                if (inputSignal.getCDataType() != com.ncslab.block.data.CDataType.STRING) {
                    throw new MatDimException("StringConcatenate block '" + blockName +
                                             "' requires string inputs, but got: " + inputSignal.getCDataType());
                }
            }
        }
    }

    // === Runtime Simulation Methods ===

    /**
     * Initializes the string concatenate output.
     * Sets the output signal to empty string initially.
     */
    @Override
    public void calculateInit() {
        // Initialize output with concatenation of all input strings
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            // Get maximum length parameter
            int maxLength = Integer.parseInt(maximumLength.getInitString());

            // Concatenate all input strings
            StringBuilder concatenated = new StringBuilder();
            for (InputPort in : inputPortList) {
                if (in.getData() != null) {
                    String inputString = getStringFromData(in.getData());
                    concatenated.append(inputString);
                }
            }

            // Apply maximum length truncation if specified
            String result = concatenated.toString();
            if (maxLength > 0 && result.length() > maxLength) {
                result = result.substring(0, maxLength);
            }

            // Create StringData with the concatenated result
            StringData stringData = new StringData(result, maxLength);
            out.getOutputSignalC().setData(stringData);

            System.out.println("StringConcatenate '" + blockName + "' initialized with value: \"" + result + "\"");
        }
    }

    /**
     * Calculates the output of the string concatenate block.
     * Concatenates all input strings and applies maximum length constraint.
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            // Get maximum length parameter
            int maxLength = Integer.parseInt(maximumLength.getInitString());

            // Concatenate all input strings
            StringBuilder concatenated = new StringBuilder();
            for (InputPort in : inputPortList) {
                if (in.getData() != null) {
                    String inputString = getStringFromData(in.getData());
                    concatenated.append(inputString);
                }
            }

            // Apply maximum length truncation if specified
            String result = concatenated.toString();
            if (maxLength > 0 && result.length() > maxLength) {
                result = result.substring(0, maxLength);
            }

            // Create StringData with the concatenated result
            StringData stringData = new StringData(result, maxLength);
            out.setData(stringData);
        }
    }


    // === Code Generation Methods ===

    /**
     * Generates MATLAB initialization code for String Concatenate block.
     *
     * @param code Code structure to append to
     */
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/string/StringConcatenate/init.vm", context);
        code.addInitCode(codeStr);
    }

    /**
     * Generates MATLAB output code for String Concatenate block.
     *
     * @param code Code structure to append to
     */
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        // Add special computed values
        context.put("MaximumLength", Integer.parseInt(maximumLength.getInitString()));

        String codeStr = TemplateManager.renderTemplate("m/string/StringConcatenate/output.vm", context);
        code.addOutputCode(codeStr);
    }

    /**
     * Generates C initialization code for String Concatenate block.
     *
     * @param code Code structure to append to
     */
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/string/StringConcatenate/init.vm", context);
        code.addInitCode(codeStr);
    }

    /**
     * Generates C output code for String Concatenate block.
     * Uses std::string for string representation in C++ code.
     *
     * @param code Code structure to append to
     */
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // Add special computed values
        context.put("MaximumLength", Integer.parseInt(maximumLength.getInitString()));

        String codeStr = TemplateManager.renderTemplate("c/string/StringConcatenate/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateUpdateCodePLC(CodeStructC code) {
        // No update code needed for String Concatenate
    }

    public void generateOutputCodePLC(CodeStructC code) {
        // No PLC code generation for String Concatenate
    }

    // === Helper Methods ===

    /**
     * Extract string value from Data object.
     * Handles StringData, initString, and dataString fields.
     *
     * @param data Data object containing string
     * @return String value (empty string if null)
     */
    private String getStringFromData(Data data) {
        if (data == null) {
            return "";
        }

        // Check if it's a StringData object
        if (data instanceof StringData) {
            return ((StringData) data).getStringValue();
        }

        // Try to get string from initString field first
        String str = data.getInitString();
        if (str != null && !str.isEmpty()) {
            return str;
        }

        // Fall back to dataString field
        str = data.getDataString();
        if (str != null && !str.isEmpty()) {
            return str;
        }

        return "";
    }

    // === Accessor Methods ===

    /**
     * Gets the number of inputs from the parameter.
     *
     * @return The number of input ports
     */
    public int getNumberOfInputsValue() {
        return Integer.parseInt(numberOfInputs.getInitString());
    }

    /**
     * Gets the maximum length from the parameter.
     *
     * @return The maximum output length (0 = unlimited)
     */
    public int getMaximumLengthValue() {
        return Integer.parseInt(maximumLength.getInitString());
    }

    /**
     * Gets the output dimensions mode from the parameter.
     *
     * @return The output dimensions mode ("Fixed-size" or "Variable-size")
     */
    public String getOutputDimensionsModeValue() {
        return outputDimensionsMode.getInitString();
    }
}
