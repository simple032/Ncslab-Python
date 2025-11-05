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
import com.ncslab.dto.block.specialized.string.SubstringDto;

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
 * Substring extraction block with SIMULINK-compatible parameters.
 *
 * Extracts a substring from input string based on starting index and length.
 * This block is part of the SIMULINK string manipulation blocks suite.
 *
 * SIMULINK Parameters:
 * - StartIndex: Starting character index, 1-based MATLAB convention (default: 1, range: 1-∞)
 * - Length: Number of characters to extract (default: 1, range: 1-∞)
 *
 * Behavior:
 * - Uses 1-based indexing (MATLAB/Simulink convention): StartIndex=1 means first character
 * - Extracts substring starting at StartIndex for Length characters
 * - If StartIndex + Length exceeds string length, extract to end of string
 * - If StartIndex > string length, return empty string
 *
 * Examples:
 * - Input="HelloWorld", StartIndex=6, Length=5 → Output="World"
 * - Input="Test", StartIndex=2, Length=10 → Output="est" (extracts to end)
 * - Input="Hello", StartIndex=10, Length=5 → Output="" (empty string)
 *
 * Character Set: ISO/IEC 8859-1 (first 256 Unicode code points)
 * Encoding: UTF-8 for C code generation
 * C Representation: Null-terminated uint8 vector (std::string)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class Substring extends Block {

    // === Substring-Specific SIMULINK Parameters ===
    /** Starting character index (1-based MATLAB convention) */
    @Getter
    private final Parameter startIndex;

    /** Number of characters to extract */
    @Getter
    private final Parameter length;

    // === Static Parameter Definitions ===
    // Parameter defaults matching SIMULINK Substring block
    public static final Map<String, Object> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("StartIndex", 1);
        PARAMETER_DEFAULTS.put("Length", 1);
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        inputNames.add("str");
        outputNames.add("substr");
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Substring");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Private Constructor with Typed Parameters ===
    private Substring(Parameter startIndex, Parameter length,
                      String blockName, String blockPath, String blockUUID,
                      NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(startIndex, length);

        // Assign Substring-specific parameters
        this.startIndex = Objects.requireNonNull(startIndex, "StartIndex parameter cannot be null");
        this.length = Objects.requireNonNull(length, "Length parameter cannot be null");

        // Add Substring-specific parameters to parameter list
        parameterList.add(startIndex);
        parameterList.add(length);

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Substring(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.startIndex = getParameterByName("StartIndex");
        this.length = getParameterByName("Length");

        // Verify parameters are properly initialized
        if (this.startIndex == null || this.length == null) {
            throw new IllegalStateException("Substring parameters not properly initialized");
        }

        // Initialize ports
        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates Substring block directly from BlockDto DTO
     */
    public Substring(SubstringDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.startIndex = getParameterByName("StartIndex");
        this.length = getParameterByName("Length");

        // Verify parameters are properly initialized
        if (this.startIndex == null || this.length == null) {
            throw new IllegalStateException("Substring parameters not properly initialized from DTO");
        }

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Substring fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter startIndexParam = createStartIndexFromJSON(paramValues, blockName);
            Parameter lengthParam = createLengthFromJSON(paramValues, blockName);

            Substring block = new Substring(startIndexParam, lengthParam,
                                           blockName, blockPath, blockUUID, model);

            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, startIndexParam, lengthParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Substring block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Substring create(String name, String path, NCSLabModel model) {
        return create(name, path, 1, 1, model);
    }

    /**
     * Create a Substring block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param startIndex Starting character index (1-based MATLAB convention, >= 1)
     * @param length Number of characters to extract (>= 1)
     * @param model Parent model
     * @return Substring block instance
     */
    public static Substring create(String name, String path, int startIndex, int length, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        SubstringDto dto = SubstringDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .startIndex(startIndex)
            .length(length)
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Substring parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Substring(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter startIndex, Parameter length) {
        // Validate StartIndex (>= 1)
        int startIdx = (int) startIndex.getValue();
        if (startIdx < 1) {
            throw new IllegalArgumentException("StartIndex must be >= 1 (1-based MATLAB indexing), got: " + startIdx);
        }

        // Validate Length (>= 1)
        int len = (int) length.getValue();
        if (len < 1) {
            throw new IllegalArgumentException("Length must be >= 1, got: " + len);
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createStartIndexFromJSON(JSONObject paramValues, String blockName) {
        int defaultStartIndex = (Integer) PARAMETER_DEFAULTS.get("StartIndex");
        String startIndexStr = paramValues.optString("StartIndex", String.valueOf(defaultStartIndex));
        return new Parameter(null, 1, "StartIndex", startIndexStr);
    }

    private static Parameter createLengthFromJSON(JSONObject paramValues, String blockName) {
        int defaultLength = (Integer) PARAMETER_DEFAULTS.get("Length");
        String lengthStr = paramValues.optString("Length", String.valueOf(defaultLength));
        return new Parameter(null, 2, "Length", lengthStr);
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

    private static void setParameterBlockReference(Substring block, Parameter... parameters) {
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
        // Create single input port (string)
        inputPortList.add(new InputPort(this, 1));

        // Create single output port (string) with feedthrough
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough

        System.out.println("Substring '" + blockName + "' initialized");
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
                System.out.println("Substring '" + blockName + "' output CDataType set to: STRING");
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Verify input port has string data type (scalar 1x1)
        if (!inputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

                // Check if input is string type
                if (inputSignal.getCDataType() != com.ncslab.block.data.CDataType.STRING) {
                    throw new MatDimException("Substring block '" + blockName +
                                             "' requires string input, but got: " + inputSignal.getCDataType());
                }
            }
        }
    }

    // === Runtime Simulation Methods ===

    /**
     * Initializes the substring output.
     * Sets the output signal to empty string initially.
     */
    @Override
    public void calculateInit() {
        // Initialize output with empty string
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            // Create StringData with empty string
            StringData stringData = new StringData("");
            out.getOutputSignalC().setData(stringData);

            System.out.println("Substring '" + blockName + "' initialized with empty string");
        }
    }

    /**
     * Calculates the output of the substring block.
     * Extracts substring based on StartIndex and Length parameters.
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            // Get input string
            String inputString = getStringFromData(in.getData());

            // Get parameters (1-based MATLAB indexing)
            int startIdx = (int) startIndex.getValue(); // 1-based
            int len = (int) length.getValue();

            // Extract substring
            String result = extractSubstring(inputString, startIdx, len);

            // Create StringData with the result
            StringData stringData = new StringData(result);
            out.setData(stringData);

            System.out.println("Substring '" + blockName + "' extracted: \"" + result +
                             "\" from \"" + inputString + "\" (start=" + startIdx + ", len=" + len + ")");
        }
    }

    // === Code Generation Methods ===

    /**
     * Generates MATLAB initialization code for Substring block.
     *
     * @param code Code structure to append to
     */
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("startIndex", startIndex);
        context.put("length", length);

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/Substring/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            // If template not found, generate inline initialization
            String outputVar = getOutputPortVariablesInternal().get(0);
            code.addInitCode(String.format("%% Initialize Substring output\n"));
            code.addInitCode(String.format("%s = '';\n", outputVar));
        }
    }

    /**
     * Generates MATLAB output code for Substring block.
     *
     * @param code Code structure to append to
     */
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        context.put("block", this);
        context.put("startIndex", startIndex);
        context.put("length", length);
        context.put("StartIndex", (int) startIndex.getValue());
        context.put("Length", (int) length.getValue());
        context.put("outputs", getOutputPortVariablesInternal());
        context.put("inputs", getInputPortVariablesInternal());

        String codeStr = TemplateManager.renderTemplate("m/string/Substring/output.vm", context);
        code.addOutputCode(codeStr);
    }

    /**
     * Generates C initialization code for Substring block.
     *
     * @param code Code structure to append to
     */
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        context.put("block", this);
        context.put("startIndex", startIndex);
        context.put("length", length);

        String codeStr = TemplateManager.renderTemplate("c/string/Substring/init.vm", context);
        code.addInitCode(codeStr);
    }

    /**
     * Generates C output code for Substring block.
     * Uses std::string for string representation in C++ code.
     *
     * @param code Code structure to append to
     */
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        context.put("block", this);
        context.put("startIndex", startIndex);
        context.put("length", length);
        context.put("StartIndex", (int) startIndex.getValue());
        context.put("Length", (int) length.getValue());
        context.put("outputs", getOutputPortVariablesInternal());
        context.put("inputs", getInputPortVariablesInternal());

        String codeStr = TemplateManager.renderTemplate("c/string/Substring/output.vm", context);
        code.addOutputCode(codeStr);
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

    /**
     * Extracts substring using 1-based MATLAB indexing convention.
     * Handles edge cases where index or length exceeds string bounds.
     *
     * @param input Input string
     * @param startIndex 1-based starting index (MATLAB convention)
     * @param length Number of characters to extract
     * @return Extracted substring (empty string if startIndex out of bounds)
     */
    private String extractSubstring(String input, int startIndex, int length) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Convert 1-based MATLAB index to 0-based Java index
        int javaStartIndex = startIndex - 1;

        // Check if start index is out of bounds
        if (javaStartIndex < 0 || javaStartIndex >= input.length()) {
            return ""; // Return empty string if index out of bounds
        }

        // Calculate end index
        int endIndex = Math.min(javaStartIndex + length, input.length());

        // Extract substring
        return input.substring(javaStartIndex, endIndex);
    }

    /**
     * Get list of input port variable names for template context.
     *
     * @return List of input signal variable names
     */
    private List<String> getInputPortVariablesInternal() {
        List<String> inputVars = new ArrayList<>();
        for (InputPort in : inputPortList) {
            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                inputVars.add(inputSignal.getName());
            } else {
                inputVars.add(""); // Placeholder for unconnected inputs
            }
        }
        return inputVars;
    }

    /**
     * Get list of output port variable names for template context.
     *
     * @return List of output signal variable names
     */
    private List<String> getOutputPortVariablesInternal() {
        List<String> outputVars = new ArrayList<>();
        for (OutputPort out : outputPortList) {
            outputVars.add(out.getOutputSignalC().getName());
        }
        return outputVars;
    }

    // === Accessor Methods ===

    /**
     * Gets the start index from the parameter (1-based MATLAB convention).
     *
     * @return The starting character index (1-based)
     */
    public int getStartIndexValue() {
        return (int) startIndex.getValue();
    }

    /**
     * Gets the length from the parameter.
     *
     * @return The number of characters to extract
     */
    public int getLengthValue() {
        return (int) length.getValue();
    }
}
