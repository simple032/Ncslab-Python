package com.ncslab.block.signal;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.signal.SignalSpecificationDto;

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
 * Signal Specification block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * The Signal Specification block specifies and enforces signal properties (dimensions,
 * data type, sample time, complexity). Acts as signal contract enforcement:
 * - Validates input signal against specified properties
 * - If enforcement enabled and mismatch detected: log warning or error
 * - Always passes signal through (non-blocking)
 * - Used for documentation and early error detection
 *
 * SIMULINK Parameters:
 * - Dimensions: Expected signal dimensions (e.g., "1", "[3]", "[2 3]") - default: "-1" (inherit)
 * - DimensionsMode: "Inherit" or "Fixed" (default: "Inherit")
 * - DataType: Expected data type (default: "Inherit: Same as input")
 * - SampleTime: Expected sample time (default: -1 for inherited)
 * - Complexity: Expected complexity ("real", "complex", "auto") - default: "auto"
 * - EnforceDimensions: Enforce dimension check (default: true)
 * - EnforceDataType: Enforce data type check (default: false)
 * - EnforceSampleTime: Enforce sample time check (default: false)
 * - EnforceComplexity: Enforce complexity check (default: false)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class SignalSpecification extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter dimensions;
    private final Parameter dimensionsMode;
    private final Parameter dataType;
    private final Parameter sampleTime;
    private final Parameter complexity;
    private final Parameter enforceDimensions;
    private final Parameter enforceDataType;
    private final Parameter enforceSampleTime;
    private final Parameter enforceComplexity;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Dimensions", "-1");  // Inherit
        PARAMETER_DEFAULTS.put("DimensionsMode", "Inherit");  // Inherit or Fixed
        PARAMETER_DEFAULTS.put("DataType", "Inherit: Same as input");  // Data type
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("Complexity", "auto");  // real, complex, auto
        PARAMETER_DEFAULTS.put("EnforceDimensions", "on");  // Enforce dimension check
        PARAMETER_DEFAULTS.put("EnforceDataType", "off");  // Enforce data type check
        PARAMETER_DEFAULTS.put("EnforceSampleTime", "off");  // Enforce sample time check
        PARAMETER_DEFAULTS.put("EnforceComplexity", "off");  // Enforce complexity check
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

        // Output port defaults (pass-through)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);  // Signal specification has feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private SignalSpecification(Parameter dimensions, Parameter dimensionsMode,
                               Parameter dataType, Parameter sampleTime,
                               Parameter complexity, Parameter enforceDimensions,
                               Parameter enforceDataType, Parameter enforceSampleTime,
                               Parameter enforceComplexity,
                               String blockName, String blockPath,
                               String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(dimensions, dimensionsMode, complexity);

        // Assign parameters
        this.dimensions = Objects.requireNonNull(dimensions, "Dimensions parameter cannot be null");
        this.dimensionsMode = Objects.requireNonNull(dimensionsMode, "Dimensions mode parameter cannot be null");
        this.dataType = Objects.requireNonNull(dataType, "Data type parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.complexity = Objects.requireNonNull(complexity, "Complexity parameter cannot be null");
        this.enforceDimensions = Objects.requireNonNull(enforceDimensions, "Enforce dimensions parameter cannot be null");
        this.enforceDataType = Objects.requireNonNull(enforceDataType, "Enforce data type parameter cannot be null");
        this.enforceSampleTime = Objects.requireNonNull(enforceSampleTime, "Enforce sample time parameter cannot be null");
        this.enforceComplexity = Objects.requireNonNull(enforceComplexity, "Enforce complexity parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.dimensions);
        parameterList.add(this.dimensionsMode);
        parameterList.add(this.dataType);
        parameterList.add(this.sampleTime);
        parameterList.add(this.complexity);
        parameterList.add(this.enforceDimensions);
        parameterList.add(this.enforceDataType);
        parameterList.add(this.enforceSampleTime);
        parameterList.add(this.enforceComplexity);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public SignalSpecification(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.dimensions = getParameterByName("Dimensions");
        this.dimensionsMode = getParameterByName("DimensionsMode");
        this.dataType = getParameterByName("DataType");
        this.sampleTime = getParameterByName("SampleTime");
        this.complexity = getParameterByName("Complexity");
        this.enforceDimensions = getParameterByName("EnforceDimensions");
        this.enforceDataType = getParameterByName("EnforceDataType");
        this.enforceSampleTime = getParameterByName("EnforceSampleTime");
        this.enforceComplexity = getParameterByName("EnforceComplexity");

        initializePorts();
    }

    /**
     * DTO Constructor - Creates SignalSpecification block directly from SignalSpecificationDto DTO
     */
    public SignalSpecification(SignalSpecificationDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO with defaults
        String dimensionsValue = dto.getDimensionsValue();
        String dimensionsModeValue = dto.getDimensionsModeValue();
        String dataTypeValue = dto.getDataTypeValue();
        String sampleTimeValue = dto.getSampleTime() != null ? (dto.getSampleTime().getAsString()) : "-1";
        String complexityValue = dto.getComplexityValue();
        String enforceDimensionsValue = dto.getEnforceDimensionsValue();
        String enforceDataTypeValue = dto.getEnforceDataTypeValue();
        String enforceSampleTimeValue = dto.getEnforceSampleTimeValue();
        String enforceComplexityValue = dto.getEnforceComplexityValue();

        // Validate dimensions mode
        if (!isValidDimensionsMode(dimensionsModeValue)) {
            throw new IllegalArgumentException("Invalid dimensions mode: " + dimensionsModeValue +
                    ". Must be one of: Inherit, Fixed");
        }

        // Validate complexity
        if (!isValidComplexity(complexityValue)) {
            throw new IllegalArgumentException("Invalid complexity: " + complexityValue +
                    ". Must be one of: real, complex, auto");
        }

        // Validate sample time
        double sampleTimeDouble = Double.parseDouble(sampleTimeValue);
        if (sampleTimeDouble != -1.0 && sampleTimeDouble < 0.0) {
            throw new IllegalArgumentException("Sample time must be non-negative or -1 (inherited)");
        }

        // Initialize parameters
        this.dimensions = getParameterByName("Dimensions");
        this.dimensionsMode = getParameterByName("DimensionsMode");
        this.dataType = getParameterByName("DataType");
        this.sampleTime = getParameterByName("SampleTime");
        this.complexity = getParameterByName("Complexity");
        this.enforceDimensions = getParameterByName("EnforceDimensions");
        this.enforceDataType = getParameterByName("EnforceDataType");
        this.enforceSampleTime = getParameterByName("EnforceSampleTime");
        this.enforceComplexity = getParameterByName("EnforceComplexity");

        initializePorts();

        System.out.println("DTO-SPECIFIC: SignalSpecification block created successfully from SignalSpecificationDto - " + dto.getBlockName());
    }

    /**
     * Factory method to create SignalSpecification block from SignalSpecificationDto.
     *
     * @param dto The SignalSpecificationDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New SignalSpecification block instance
     * @throws BlockCreationException if block creation fails
     */
    public static SignalSpecification createFromDto(SignalSpecificationDto dto, NCSLabModel model) throws BlockCreationException {
        return new SignalSpecification(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static SignalSpecification fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter dimensions = createDimensionsFromJSON(paramValues, blockName);
            Parameter dimensionsMode = createDimensionsModeFromJSON(paramValues, blockName);
            Parameter dataType = createDataTypeFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter complexity = createComplexityFromJSON(paramValues, blockName);
            Parameter enforceDimensions = createEnforceDimensionsFromJSON(paramValues, blockName);
            Parameter enforceDataType = createEnforceDataTypeFromJSON(paramValues, blockName);
            Parameter enforceSampleTime = createEnforceSampleTimeFromJSON(paramValues, blockName);
            Parameter enforceComplexity = createEnforceComplexityFromJSON(paramValues, blockName);

            SignalSpecification block = new SignalSpecification(dimensions, dimensionsMode, dataType,
                                                               sampleTime, complexity, enforceDimensions,
                                                               enforceDataType, enforceSampleTime,
                                                               enforceComplexity,
                                                               blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, dimensions, dimensionsMode, dataType, sampleTime,
                                      complexity, enforceDimensions, enforceDataType,
                                      enforceSampleTime, enforceComplexity);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create SignalSpecification block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a SignalSpecification block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return Configured SignalSpecification block instance
     */
    public static SignalSpecification create(String name, String path, NCSLabModel model) {
        return create(name, path, "-1", "Inherit", "Inherit: Same as input",
                     -1.0, "auto", true, false, false, false, model);
    }

    /**
     * Create a SignalSpecification block with full parameters using DTO-based construction.
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param dimensions Expected signal dimensions
     * @param dimensionsMode "Inherit" or "Fixed"
     * @param dataType Expected data type
     * @param sampleTime Expected sample time (-1 for inherited)
     * @param complexity Expected complexity ("real", "complex", "auto")
     * @param enforceDimensions Enforce dimension check
     * @param enforceDataType Enforce data type check
     * @param enforceSampleTime Enforce sample time check
     * @param enforceComplexity Enforce complexity check
     * @param model Parent model
     * @return Configured SignalSpecification block instance
     */
    public static SignalSpecification create(String name, String path, String dimensions,
                                            String dimensionsMode, String dataType,
                                            double sampleTime, String complexity,
                                            boolean enforceDimensions, boolean enforceDataType,
                                            boolean enforceSampleTime, boolean enforceComplexity,
                                            NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        SignalSpecificationDto dto = SignalSpecificationDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .dimensions(com.ncslab.dto.common.TypedParameter.of(dimensions))
            .dimensionsMode(com.ncslab.dto.common.TypedParameter.of(dimensionsMode))
            .dataType(com.ncslab.dto.common.TypedParameter.of(dataType))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .complexity(com.ncslab.dto.common.TypedParameter.of(complexity))
            .enforceDimensions(com.ncslab.dto.common.TypedParameter.of(enforceDimensions ? "on" : "off"))
            .enforceDataType(com.ncslab.dto.common.TypedParameter.of(enforceDataType ? "on" : "off"))
            .enforceSampleTime(com.ncslab.dto.common.TypedParameter.of(enforceSampleTime ? "on" : "off"))
            .enforceComplexity(com.ncslab.dto.common.TypedParameter.of(enforceComplexity ? "on" : "off"))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid SignalSpecification parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new SignalSpecification(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter dimensions, Parameter dimensionsMode,
                                          Parameter complexity) {
        String dimensionsModeValue = dimensionsMode.getInitString();
        if (!isValidDimensionsMode(dimensionsModeValue)) {
            throw new IllegalArgumentException("Invalid dimensions mode: " + dimensionsModeValue +
                    ". Must be one of: Inherit, Fixed");
        }

        String complexityValue = complexity.getInitString();
        if (!isValidComplexity(complexityValue)) {
            throw new IllegalArgumentException("Invalid complexity: " + complexityValue +
                    ". Must be one of: real, complex, auto");
        }
    }

    private static boolean isValidDimensionsMode(String mode) {
        return mode.equals("Inherit") || mode.equals("Fixed");
    }

    private static boolean isValidComplexity(String complexity) {
        return complexity.equals("real") || complexity.equals("complex") ||
               complexity.equals("auto");
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createDimensionsFromJSON(JSONObject paramValues, String blockName) {
        String dimensionsValue = paramValues.optString("Dimensions", "-1");
        return new Parameter(null, 1, "Dimensions", dimensionsValue);
    }

    private static Parameter createDimensionsModeFromJSON(JSONObject paramValues, String blockName) {
        String dimensionsModeValue = paramValues.optString("DimensionsMode", "Inherit");
        return new Parameter(null, 2, "DimensionsMode", dimensionsModeValue);
    }

    private static Parameter createDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String dataTypeValue = paramValues.optString("DataType", "Inherit: Same as input");
        return new Parameter(null, 3, "DataType", dataTypeValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 4, "SampleTime", sampleTimeValue);
    }

    private static Parameter createComplexityFromJSON(JSONObject paramValues, String blockName) {
        String complexityValue = paramValues.optString("Complexity", "auto");
        return new Parameter(null, 5, "Complexity", complexityValue);
    }

    private static Parameter createEnforceDimensionsFromJSON(JSONObject paramValues, String blockName) {
        String enforceValue = paramValues.optString("EnforceDimensions", "on");
        return new Parameter(null, 6, "EnforceDimensions", enforceValue);
    }

    private static Parameter createEnforceDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String enforceValue = paramValues.optString("EnforceDataType", "off");
        return new Parameter(null, 7, "EnforceDataType", enforceValue);
    }

    private static Parameter createEnforceSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String enforceValue = paramValues.optString("EnforceSampleTime", "off");
        return new Parameter(null, 8, "EnforceSampleTime", enforceValue);
    }

    private static Parameter createEnforceComplexityFromJSON(JSONObject paramValues, String blockName) {
        String enforceValue = paramValues.optString("EnforceComplexity", "off");
        return new Parameter(null, 9, "EnforceComplexity", enforceValue);
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

    private static void setParameterBlockReference(SignalSpecification block, Parameter... parameters) {
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
        identity.put("blockType", "SignalSpecification");
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

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        String initCode = TemplateManager.renderTemplate("c/signal/SignalSpecification/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/signal/SignalSpecification/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // Signal specification passes through input signal dimensions
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);

        if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
            OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

            out.setHeight(inputSignal.getHeight());
            out.setWidth(inputSignal.getWidth());
            out.getOutputSignalC().setHeight(inputSignal.getHeight());
            out.getOutputSignalC().setWidth(inputSignal.getWidth());
            out.getOutputSignalC().setDataType(inputSignal.getDataType());
        } else {
            // Default dimensions if no input connected
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        }
    }

    public void checkDimension() throws MatDimException {
        // Signal specification passes through signal - dimension checking happens in validateSignalProperties
        // No dimension mismatch error thrown here
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Signal Specification block: validates signal properties and passes through
        InputPort inputPort = inputPortList.get(0);

        if (inputPort.getLinkedLine() == null ||
            inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            log.warn("SignalSpecification block '{}': No input signal", blockName);
            return;
        }

        OutputSignal inputSignal = inputPort.getLinkedLine()
            .getLinkedOutputPort().getOutputSignalC();

        // Validate signal properties if enforcement is enabled
        validateSignalProperties(inputSignal);

        // Pass through signal (always non-blocking)
        Data inputData = inputPort.getData();
        outputPortList.get(0).setData(inputData);
    }

    /**
     * Validates signal properties based on enforcement settings.
     * Logs warnings or errors if mismatches detected.
     */
    private void validateSignalProperties(OutputSignal signal) {
        // Check dimensions if enforced
        if (enforceDimensions != null &&
            "on".equalsIgnoreCase(enforceDimensions.getInitString())) {
            validateDimensions(signal);
        }

        // Check data type if enforced
        if (enforceDataType != null &&
            "on".equalsIgnoreCase(enforceDataType.getInitString())) {
            validateDataType(signal);
        }

        // Check sample time if enforced
        if (enforceSampleTime != null &&
            "on".equalsIgnoreCase(enforceSampleTime.getInitString())) {
            validateSampleTime();
        }

        // Check complexity if enforced
        if (enforceComplexity != null &&
            "on".equalsIgnoreCase(enforceComplexity.getInitString())) {
            validateComplexity(signal);
        }
    }

    /**
     * Validates signal dimensions against specified dimensions.
     */
    private void validateDimensions(OutputSignal signal) {
        String expectedDims = dimensions.getInitString();

        // Skip validation if dimensions are inherited (-1)
        if ("-1".equals(expectedDims)) {
            return;
        }

        int actualHeight = signal.getHeight();
        int actualWidth = signal.getWidth();

        // Parse expected dimensions
        String[] expectedDimsParts = parseDimensions(expectedDims);
        if (expectedDimsParts != null) {
            int expectedHeight = Integer.parseInt(expectedDimsParts[0]);
            int expectedWidth = expectedDimsParts.length > 1 ?
                Integer.parseInt(expectedDimsParts[1]) : 1;

            if (actualHeight != expectedHeight || actualWidth != expectedWidth) {
                log.warn("SignalSpecification block '{}': Dimension mismatch. " +
                        "Expected: [{}x{}], Actual: [{}x{}]",
                        blockName, expectedHeight, expectedWidth, actualHeight, actualWidth);
            }
        }
    }

    /**
     * Parses dimension string (e.g., "3", "[3]", "[2 3]") into height and width.
     */
    private String[] parseDimensions(String dims) {
        if (dims == null || dims.isEmpty() || "-1".equals(dims)) {
            return null;
        }

        // Remove brackets and split
        String cleanDims = dims.replaceAll("[\\[\\]]", "").trim();
        String[] parts = cleanDims.split("\\s+");

        return parts;
    }

    /**
     * Validates signal data type against specified data type.
     */
    private void validateDataType(OutputSignal signal) {
        String expectedType = dataType.getInitString();

        // Skip validation if data type is inherited
        if (expectedType.contains("Inherit")) {
            return;
        }

        DataType actualType = signal.getDataType();

        // Simple validation (can be enhanced with more data type mappings)
        if ("double".equalsIgnoreCase(expectedType) && actualType != DataType.REAL) {
            log.warn("SignalSpecification block '{}': Data type mismatch. " +
                    "Expected: {}, Actual: {}",
                    blockName, expectedType, actualType);
        }
    }

    /**
     * Validates sample time (placeholder for future implementation).
     */
    private void validateSampleTime() {
        String expectedSampleTime = sampleTime.getInitString();

        // Skip validation if sample time is inherited
        if ("-1".equals(expectedSampleTime)) {
            return;
        }

        // Sample time validation would require access to block's actual sample time
        // This is a placeholder for future enhancement
        log.debug("SignalSpecification block '{}': Sample time validation not yet implemented",
                blockName);
    }

    /**
     * Validates signal complexity against specified complexity.
     */
    private void validateComplexity(OutputSignal signal) {
        String expectedComplexity = complexity.getInitString();

        // Skip validation if complexity is auto
        if ("auto".equalsIgnoreCase(expectedComplexity)) {
            return;
        }

        DataType actualType = signal.getDataType();
        boolean isActuallyReal = (actualType == DataType.REAL || actualType == DataType.MATRIX);

        if ("real".equalsIgnoreCase(expectedComplexity) && !isActuallyReal) {
            log.warn("SignalSpecification block '{}': Complexity mismatch. " +
                    "Expected: real, Actual: complex",
                    blockName);
        } else if ("complex".equalsIgnoreCase(expectedComplexity) && isActuallyReal) {
            log.warn("SignalSpecification block '{}': Complexity mismatch. " +
                    "Expected: complex, Actual: real",
                    blockName);
        }
    }
}
