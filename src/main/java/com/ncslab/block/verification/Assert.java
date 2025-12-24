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
import com.ncslab.dto.block.specialized.verification.AssertDto;

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
 * Assert verification block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Verifies that an input signal meets a specified condition during simulation.
 * If the assertion fails, logs an error message and optionally stops the simulation.
 *
 * SIMULINK Parameters:
 * - Assertion: Condition to verify (boolean or >0)
 * - ErrorMessage: Custom error message when assertion fails
 * - StopWhenAssertionFail: Stop simulation on failure (default: true)
 * - SampleTime: Sample time for assertion checking (-1 for inherited)
 * - Enabled: Enable/disable assertion checking (default: true)
 *
 * @author NCSLab Verification Framework
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class Assert extends Block {

    // === SIMULINK-Compatible Parameters ===
    /** Error message parameter */
    private final Parameter errorMessage;

    /** Stop when assertion fails parameter */
    private final Parameter stopWhenAssertionFail;

    /** Sample time parameter */
    private final Parameter sampleTime;

    /** Enabled parameter */
    private final Parameter enabled;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("ErrorMessage", "Assertion failed");
        PARAMETER_DEFAULTS.put("StopWhenAssertionFail", "on");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("Enabled", "on");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        inputNames.add("in1");

        // Input port defaults (assertion input - typically boolean or scalar)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (Assert has no outputs - it's a verification block)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
    }

    // === Private Constructor with Typed Parameters ===
    private Assert(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Get parameters by name from the automatically populated parameterList
        this.errorMessage = getParameterByName("ErrorMessage");
        this.stopWhenAssertionFail = getParameterByName("StopWhenAssertionFail");
        this.sampleTime = getParameterByName("SampleTime");
        this.enabled = getParameterByName("Enabled");

        // Create input port
        inputPortList.add(new InputPort(this, 1));
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
        identity.put("blockType", "Assert");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Assert(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Get parameters by name from the automatically populated parameterList
        this.errorMessage = getParameterByName("ErrorMessage");
        this.stopWhenAssertionFail = getParameterByName("StopWhenAssertionFail");
        this.sampleTime = getParameterByName("SampleTime");
        this.enabled = getParameterByName("Enabled");

        // Create input port
        inputPortList.add(new InputPort(this, 1));
    }

    /**
     * DTO-NATIVE Constructor - Creates Assert block directly from BlockDto DTO
     */
    public Assert(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.errorMessage = getParameterByName("ErrorMessage");
        this.stopWhenAssertionFail = getParameterByName("StopWhenAssertionFail");
        this.sampleTime = getParameterByName("SampleTime");
        this.enabled = getParameterByName("Enabled");

        initializePorts();

        log.info("DTO-NATIVE: Assert block created successfully - {}", blockDto.getBlockName());
    }

    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Assert fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");

            return new Assert(blockName, blockPath, blockUUID, model);

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Assert block from JSON: " + e.getMessage(), e);
        }
    }

    /**
     * DTO Factory Method - Creates Assert block from AssertDto DTO with comprehensive validation
     */
    public static Assert createFromDto(AssertDto dto, NCSLabModel model) {
        Objects.requireNonNull(dto, "AssertDto cannot be null");
        Objects.requireNonNull(model, "NCSLabModel cannot be null");

        try {
            // Create block using DTO constructor
            return new Assert(dto, model);

        } catch (Exception e) {
            throw new BlockCreationException(
                    String.format("Failed to create Assert block from DTO: %s (block: %s)",
                                e.getMessage(), dto.getBlockName()), e);
        }
    }

    // === Block Calculation ===
    public void calculateOutput() throws MatDimException {
        // Check if assertions are enabled
        if (enabled != null && "off".equalsIgnoreCase(enabled.getInitString())) {
            return;
        }

        // Get input signal
        if (inputPortList.isEmpty()) {
            log.warn("Assert block '{}': No input ports", blockName);
            return;
        }

        InputPort inputPort = inputPortList.get(0);
        if (inputPort.getLinkedLine() == null || inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            log.warn("Assert block '{}': No input signal connected", blockName);
            return;
        }

        OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (inputSignal == null) {
            log.warn("Assert block '{}': No input signal connected", blockName);
            return;
        }

        // Get assertion condition value
        boolean assertionPassed = evaluateAssertion(inputSignal);

        // If assertion fails, handle error
        if (!assertionPassed) {
            String message = errorMessage != null ? errorMessage.getInitString() : "Assertion failed";
            String fullMessage = String.format("Assert block '%s': %s", blockName, message);

            log.error(fullMessage);

            // Check if we should stop simulation
            boolean shouldStop = stopWhenAssertionFail != null &&
                               "on".equalsIgnoreCase(stopWhenAssertionFail.getInitString());

            if (shouldStop) {
                // Throw exception to stop simulation
                throw new BlockExecutionException(fullMessage, blockName, 0.0);
            }
        }
    }

    /**
     * Evaluates the assertion condition from the input signal.
     * Assertion passes if input is boolean true or numeric value > 0
     *
     * @param inputSignal the input signal to evaluate
     * @return true if assertion passes, false otherwise
     */
    private boolean evaluateAssertion(OutputSignal inputSignal) {
        try {
            Data inputData = inputSignal.getData();
            if (inputData == null) {
                return false;
            }

            // Handle scalar values
            double initValue = inputData.getInitValue();
            if (!Double.isNaN(initValue)) {
                return initValue > 0.0; // Assertion passes if value is positive (nonzero)
            }

            // Handle matrix values (check if any element is positive)
            Matrix m = inputData.getMatrix();
            if (m != null) {
                for (int i = 0; i < m.getRowDimension(); i++) {
                    for (int j = 0; j < m.getColumnDimension(); j++) {
                        if (m.get(i, j) > 0.0) {
                            return true;
                        }
                    }
                }
                return false;
            }

            // Default: no data means assertion fails
            return false;

        } catch (Exception e) {
            log.error("Error evaluating assertion in block '{}': {}", blockName, e.getMessage());
            return false;
        }
    }

    // Code generation methods are handled by templates via TemplateManager
}
