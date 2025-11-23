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
import com.ncslab.dto.block.specialized.string.StringConstantDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.source.SourceBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.StringData;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * String Constant block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Outputs a constant string signal specified by the String parameter.
 * This block is part of the SIMULINK string manipulation blocks suite.
 *
 * SIMULINK Parameters:
 * - String: The constant string to output (default: "string")
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 *
 * Based on MATLAB/Simulink R2024b String Constant block specification.
 *
 * Character Set: ISO/IEC 8859-1 (first 256 Unicode code points)
 * Restriction: Does not support char(0) "NULL" character
 * Encoding: UTF-8 for C code generation
 * C Representation: Null-terminated uint8 vector (std::string)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class StringConstant extends SourceBlock {

    // === String Constant-Specific SIMULINK Parameters ===
    /** String value parameter - the constant string to output */
    @Getter
    private final Parameter value;

    // === Static Parameter Definitions ===
    // Parameter defaults matching SIMULINK String Constant block
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        // String Constant-specific defaults
        Map<String, String> stringConstantDefaults = new HashMap<>();
        stringConstantDefaults.put("String", "string");  // SIMULINK default
        stringConstantDefaults.put("SampleTime", "-1");   // Inherited sample time

        // Merge with common source block defaults
        PARAMETER_DEFAULTS = stringConstantDefaults;
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");  // Single output port for string signal
        // No input ports for constant block
    }

    // === Private Constructor with Typed Parameters ===
    private StringConstant(Parameter value, Parameter sampleTime,
                          Parameter outDataType, Parameter saturateOnIntegerOverflow,
                          String blockName, String blockPath, String blockUUID,
                          NCSLabModel model) {
        super("StringConstant", sampleTime, outDataType, saturateOnIntegerOverflow,
              blockName, blockPath, blockUUID, model);

        // Validate parameters
        validateParameters(value, sampleTime);

        // Assign String Constant-specific parameters
        this.value = Objects.requireNonNull(value, "String parameter cannot be null");

        // Add String Constant-specific parameters to parameter list
        parameterList.add(value);

        // Set port dimensions and data type
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public StringConstant(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.value = getParameterByName("String");

        // Verify SourceBlock parameters are properly inherited
        if (getSampleTime() == null || getOutDataType() == null || getSaturateOnIntegerOverflow() == null) {
            throw new IllegalStateException("SourceBlock common parameters not properly initialized");
        }

        // Initialize ports
        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates StringConstant block directly from BlockDto DTO
     */
    public StringConstant(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.value = getParameterByName("String");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create StringConstant from StringConstantDto.
     *
     * @param dto The StringConstantDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New StringConstant instance
     * @throws BlockCreationException if block creation fails
     */
    public static StringConstant createFromDto(StringConstantDto dto, NCSLabModel model) throws BlockCreationException {
        return new StringConstant(dto, model);
    }


    // === Static Factory Method for JSON Deserialization ===
    public static StringConstant fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter stringParam = createStringFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            StringConstant block = new StringConstant(stringParam, sampleTime, outDataType, saturateParam,
                                                     blockName, blockPath, blockUUID, model);

            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, stringParam, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create StringConstant block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static StringConstant create(String name, String path, String value, NCSLabModel model) {
        return create(name, path, value, -1.0, "Inherit: Same as parameter", false, model);
    }

    /**
     * Create a StringConstant block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param value Constant string value to output
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return StringConstant block instance
     */
    public static StringConstant create(String name, String path, String value,
                                       double sampleTime, String outDataType,
                                       boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        StringConstantDto dto = StringConstantDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .stringValue(value)
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataType(outDataType)
            .saturate(saturateOnOverflow)
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid StringConstant parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new StringConstant(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter value, Parameter sampleTime) {
        // Validate string value is not null
        String str = value.getInitString();
        if (str == null) {
            throw new IllegalArgumentException("String value cannot be null");
        }

        // Validate string does not contain null character (char 0)
        if (str.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("String value cannot contain NULL character (char 0)");
        }

        // Validate sample time (0 for continuous, >0 for discrete, -1 for inherited)
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createStringFromJSON(JSONObject paramValues, String blockName) {
        String stringStr = paramValues.optString("String", "string");
        return new Parameter(null, 1, "String", stringStr);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as parameter");
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

    private static void setParameterBlockReference(StringConstant block, Parameter... parameters) {
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


    // === Port Dimension Setup ===
    private void initializePorts() {
        // String signals are scalar (1x1) with string data type
        outputPortList.get(0).setHeight(1);
        outputPortList.get(0).setWidth(1);

        // Set output to string data type (only if OutputSignal is already initialized)
        // Note: OutputSignal might not be created yet during construction
        if (outputPortList.get(0).getOutputSignalC() != null) {
            outputPortList.get(0).getOutputSignalC().setCDataType(com.ncslab.block.data.CDataType.STRING);
            System.out.println("StringConstant '" + blockName + "' output CDataType set to: STRING");
        }
    }

    // === Runtime Simulation Methods ===

    /**
     * Initializes the string constant output.
     * Sets the output signal to the constant string value.
     */
    @Override
    public void calculateInit() {
        // Set the constant value on first initialization
        setConstantOutput();
    }

    /**
     * Calculates the output of the string constant block.
     * For a constant block, the output never changes - it's set in calculateInit().
     * However, for testing purposes, we also set it here to ensure output is available.
     *
     * @param t Current simulation time (unused for constant blocks)
     */
    @Override
    public void calculateOutput(double t) {
        // For constant blocks, output is the same at all times
        setConstantOutput();
    }

    /**
     * Helper method to set the constant string output
     */
    private void setConstantOutput() {
        if (!outputPortList.isEmpty() && outputPortList.get(0).getOutputSignalC() != null) {
            // Create StringData with the constant string value
            String str = value.getInitString();
            StringData stringData = new StringData(str);

            // Set the output signal data
            outputPortList.get(0).getOutputSignalC().setData(stringData);

            System.out.println("StringConstant '" + blockName + "' set output value: \"" + str + "\"");
        }
    }

    // === Code Generation Methods ===

    /**
     * Generates MATLAB initialization code for String Constant block.
     *
     * @param code Code structure to append to
     */
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("stringValue", value);

        String codeStr = TemplateManager.renderTemplate("m/string/StringConstant/init.vm", context);
        code.addInitCode(codeStr);
    }

    /**
     * Generates MATLAB output code for String Constant block.
     *
     * @param code Code structure to append to
     */
    public void generateOutputCodeM(CodeStructM code) {
        context.put("block", this);
        context.put("stringValue", value);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/string/StringConstant/output.vm", context);
        code.addOutputCode(codeStr);
    }

    /**
     * Generates C initialization code for String Constant block.
     *
     * @param code Code structure to append to
     */
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        String codeStr = TemplateManager.renderTemplate("c/string/StringConstant/init.vm", context);
        code.addInitCode(codeStr);
    }

    /**
     * Generates C output code for String Constant block.
     * Uses std::string for string representation in C++ code.
     *
     * @param code Code structure to append to
     */
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        // Use the string constant template
        String codeStr = TemplateManager.renderTemplate("c/string/StringConstant/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateUpdateCodePLC(CodeStructC code) {
        // No update code needed for String Constant
    }

    public void generateOutputCodePLC(CodeStructC code) {
        context.put("block", this);
        context.put("stringValue", value);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/string/StringConstant/output_plc.vm", context);
        code.addOutputCode(codeStr);
    }

    // === Accessor Methods ===

    /**
     * Gets the string value from the parameter.
     *
     * @return The constant string value
     */
    public String getStringValue() {
        return value.getInitString();
    }

    /**
     * Gets the string parameter object.
     *
     * @return The string parameter
     */
    public Parameter getStringParameter() {
        return value;
    }
}
