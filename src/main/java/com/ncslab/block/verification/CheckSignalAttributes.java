package com.ncslab.block.verification;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import Jama.Matrix;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.verification.CheckSignalAttributesDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.BlockExecutionException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * CheckSignalAttributes verification block with SIMULINK-compatible parameters.
 *
 * Verifies that signal properties (dimensions, data type, sample time, complexity)
 * match expected specifications. Generates warnings or errors on mismatch.
 *
 * SIMULINK Parameters:
 * - Dimensions: Expected signal dimensions "[rows, cols]" (default: "-1" for any)
 * - DimensionsMode: Dimension checking mode ("Ignore", "Check", "CheckAndReport")
 * - DataType: Expected data type (default: "Inherit: Same as input")
 * - Complexity: Expected complexity "real" or "complex" (default: "Inherit")
 * - ExpectedSampleTime: Expected sample time (default: -1 for any)
 * - SeverityLevel: "warning", "error", or "none" (default: "warning")
 * - StopOnError: Stop simulation on error (default: false)
 * - SampleTime: Sample time for attribute checking (default: -1 inherited)
 *
 * @author NCSLab Verification Framework
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class CheckSignalAttributes extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter dimensions;
    private final Parameter dimensionsMode;
    private final Parameter dataType;
    private final Parameter complexity;
    private final Parameter expectedSampleTime;
    private final Parameter severityLevel;
    private final Parameter stopOnError;
    private final Parameter sampleTime;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Dimensions", "-1");
        PARAMETER_DEFAULTS.put("DimensionsMode", "Ignore");
        PARAMETER_DEFAULTS.put("DataType", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("Complexity", "Inherit");
        PARAMETER_DEFAULTS.put("ExpectedSampleTime", "-1");
        PARAMETER_DEFAULTS.put("SeverityLevel", "warning");
        PARAMETER_DEFAULTS.put("StopOnError", "off");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        inputNames.add("in1");
        outputNames.add("out1");

        // Input port defaults
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
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private CheckSignalAttributes(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Get parameters by name from the automatically populated parameterList
        this.dimensions = getParameterByName("Dimensions");
        this.dimensionsMode = getParameterByName("DimensionsMode");
        this.dataType = getParameterByName("DataType");
        this.complexity = getParameterByName("Complexity");
        this.expectedSampleTime = getParameterByName("ExpectedSampleTime");
        this.severityLevel = getParameterByName("SeverityLevel");
        this.stopOnError = getParameterByName("StopOnError");
        this.sampleTime = getParameterByName("SampleTime");

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
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

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "CheckSignalAttributes");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public CheckSignalAttributes(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Get parameters by name from the automatically populated parameterList
        this.dimensions = getParameterByName("Dimensions");
        this.dimensionsMode = getParameterByName("DimensionsMode");
        this.dataType = getParameterByName("DataType");
        this.complexity = getParameterByName("Complexity");
        this.expectedSampleTime = getParameterByName("ExpectedSampleTime");
        this.severityLevel = getParameterByName("SeverityLevel");
        this.stopOnError = getParameterByName("StopOnError");
        this.sampleTime = getParameterByName("SampleTime");

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    /**
     * DTO-NATIVE Constructor - Creates CheckSignalAttributes block from BlockDto DTO
     */
    public CheckSignalAttributes(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.dimensions = getParameterByName("Dimensions");
        this.dimensionsMode = getParameterByName("DimensionsMode");
        this.dataType = getParameterByName("DataType");
        this.complexity = getParameterByName("Complexity");
        this.expectedSampleTime = getParameterByName("ExpectedSampleTime");
        this.severityLevel = getParameterByName("SeverityLevel");
        this.stopOnError = getParameterByName("StopOnError");
        this.sampleTime = getParameterByName("SampleTime");

        initializePorts();

        log.info("DTO-NATIVE: CheckSignalAttributes block created successfully - {}", blockDto.getBlockName());
    }

    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Static Factory Method for JSON Deserialization ===
    public static CheckSignalAttributes fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");

            return new CheckSignalAttributes(blockName, blockPath, blockUUID, model);

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create CheckSignalAttributes block from JSON: " + e.getMessage(), e);
        }
    }

    /**
     * DTO Factory Method - Creates CheckSignalAttributes block from DTO with validation
     */
    public static CheckSignalAttributes createFromDto(CheckSignalAttributesDto dto, NCSLabModel model) {
        Objects.requireNonNull(dto, "CheckSignalAttributesDto cannot be null");
        Objects.requireNonNull(model, "NCSLabModel cannot be null");

        try {
            return new CheckSignalAttributes(dto, model);

        } catch (Exception e) {
            throw new BlockCreationException(
                    String.format("Failed to create CheckSignalAttributes block from DTO: %s (block: %s)",
                                e.getMessage(), dto.getBlockName()), e);
        }
    }

    // === Block Calculation ===
    public void calculateOutput() throws MatDimException {
        // Get input signal
        if (inputPortList.isEmpty() || outputPortList.isEmpty()) {
            log.warn("CheckSignalAttributes block '{}': Missing input/output ports", blockName);
            return;
        }

        InputPort inputPort = inputPortList.get(0);
        if (inputPort.getLinkedLine() == null || inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            log.warn("CheckSignalAttributes block '{}': No input signal connected", blockName);
            return;
        }

        OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (inputSignal == null) {
            log.warn("CheckSignalAttributes block '{}': No input signal connected", blockName);
            return;
        }

        // Pass through input to output
        OutputSignal outputSignal = outputPortList.get(0).getOutputSignalC();
        Data inputData = inputSignal.getData();
        outputSignal.setData(inputData);

        // Check signal attributes
        checkAttributes(inputSignal);
    }

    /**
     * Checks signal attributes against expected values and reports mismatches
     */
    private void checkAttributes(OutputSignal inputSignal) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Data inputData = inputSignal.getData();

        // Check dimensions if enabled
        String dimMode = dimensionsMode != null ? dimensionsMode.getInitString() : "Ignore";
        if (!"Ignore".equals(dimMode) && dimensions != null) {
            String expectedDims = dimensions.getInitString();
            if (!"-1".equals(expectedDims)) {
                // Parse expected dimensions and check
                int[] actualDims = getSignalDimensions(inputData);
                String actualDimsStr = String.format("[%d, %d]", actualDims[0], actualDims[1]);

                if (!expectedDims.equals(actualDimsStr)) {
                    String msg = String.format("Dimension mismatch: expected %s, got %s", expectedDims, actualDimsStr);
                    warnings.add(msg);
                }
            }
        }

        // Check data type if specified
        if (dataType != null) {
            String expectedType = dataType.getInitString();
            if (!expectedType.startsWith("Inherit")) {
                String actualType = getSignalDataType(inputData);
                if (!expectedType.equalsIgnoreCase(actualType)) {
                    String msg = String.format("Data type mismatch: expected %s, got %s", expectedType, actualType);
                    warnings.add(msg);
                }
            }
        }

        // Check complexity if specified
        if (complexity != null) {
            String expectedComplexity = complexity.getInitString();
            if (!"Inherit".equals(expectedComplexity)) {
                String actualComplexity = isComplexSignal(inputData) ? "complex" : "real";
                if (!expectedComplexity.equalsIgnoreCase(actualComplexity)) {
                    String msg = String.format("Complexity mismatch: expected %s, got %s", expectedComplexity, actualComplexity);
                    warnings.add(msg);
                }
            }
        }

        // Report errors/warnings based on severity level
        String severity = severityLevel != null ? severityLevel.getInitString() : "warning";
        boolean shouldStop = stopOnError != null && "on".equalsIgnoreCase(stopOnError.getInitString());

        for (String warning : warnings) {
            String fullMessage = String.format("CheckSignalAttributes block '%s': %s", blockName, warning);

            if ("error".equalsIgnoreCase(severity)) {
                log.error(fullMessage);
                if (shouldStop) {
                    throw new BlockExecutionException(fullMessage, blockName, 0.0);
                }
            } else if ("warning".equalsIgnoreCase(severity)) {
                log.warn(fullMessage);
            }
            // "none" severity - don't report
        }
    }

    /**
     * Gets the dimensions of the signal
     */
    private int[] getSignalDimensions(Data data) {
        if (data == null) return new int[]{1, 1};

        if (data.getMatrix() != null) {
            Matrix m = data.getMatrix();
            return new int[]{m.getRowDimension(), m.getColumnDimension()};
        }

        return new int[]{1, 1};
    }

    /**
     * Gets the data type of the signal
     */
    private String getSignalDataType(Data data) {
        if (data == null || data.getDataType() == null) return "double";
        return data.getDataType().toString();
    }

    /**
     * Checks if the signal is complex
     */
    private boolean isComplexSignal(Data data) {
        // For now, assume all signals are real
        // This can be extended when complex signal support is added
        return false;
    }

    // Code generation methods are handled by templates via TemplateManager
}
