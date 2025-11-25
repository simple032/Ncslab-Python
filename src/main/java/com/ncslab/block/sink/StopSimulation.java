package com.ncslab.block.sink;

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
import com.ncslab.dto.block.specialized.sink.StopSimulationDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.SimulationStopException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
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
 * StopSimulation sink block with SIMULINK-compatible parameters.
 *
 * Stops simulation when the input signal becomes nonzero or meets a specified
 * stop condition. Useful for conditional simulation termination.
 *
 * SIMULINK Parameters:
 * - StopCondition: Condition for stopping ("nonzero", ">0", "<0", "==value")
 * - ComparisonValue: Value for "==value" condition (default: 1.0)
 * - StopMessage: Custom message when simulation stops
 * - SampleTime: Sample time for condition checking (-1 for inherited)
 *
 * @author NCSLab Verification Framework
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class StopSimulation extends SinkBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter stopCondition;
    private final Parameter comparisonValue;
    private final Parameter stopMessage;
    private final Parameter sampleTime;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("StopCondition", "nonzero");
        PARAMETER_DEFAULTS.put("ComparisonValue", "1.0");
        PARAMETER_DEFAULTS.put("StopMessage", "Simulation stopped by StopSimulation block");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
    }

    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        inputNames.add("in1");

        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (sink block has no outputs)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
    }

    // === Private Constructor with Typed Parameters ===
    private StopSimulation(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Get parameters by name from the automatically populated parameterList
        this.stopCondition = getParameterByName("StopCondition");
        this.comparisonValue = getParameterByName("ComparisonValue");
        this.stopMessage = getParameterByName("StopMessage");
        this.sampleTime = getParameterByName("SampleTime");

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
        identity.put("blockType", "StopSimulation");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public StopSimulation(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Get parameters by name from the automatically populated parameterList
        this.stopCondition = getParameterByName("StopCondition");
        this.comparisonValue = getParameterByName("ComparisonValue");
        this.stopMessage = getParameterByName("StopMessage");
        this.sampleTime = getParameterByName("SampleTime");

        // Create input port
        inputPortList.add(new InputPort(this, 1));
    }

    /**
     * DTO-NATIVE Constructor - Creates StopSimulation block from BlockDto DTO
     */
    public StopSimulation(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.stopCondition = getParameterByName("StopCondition");
        this.comparisonValue = getParameterByName("ComparisonValue");
        this.stopMessage = getParameterByName("StopMessage");
        this.sampleTime = getParameterByName("SampleTime");

        initializePorts();

        log.info("DTO-NATIVE: StopSimulation block created successfully - {}", blockDto.getBlockName());
    }

    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
    }

    // === Static Factory Method for JSON Deserialization ===
    public static StopSimulation fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");

            return new StopSimulation(blockName, blockPath, blockUUID, model);

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create StopSimulation block from JSON: " + e.getMessage(), e);
        }
    }

    /**
     * DTO Factory Method - Creates StopSimulation block from DTO with validation
     */
    public static StopSimulation createFromDto(StopSimulationDto dto, NCSLabModel model) {
        Objects.requireNonNull(dto, "StopSimulationDto cannot be null");
        Objects.requireNonNull(model, "NCSLabModel cannot be null");

        try {
            return new StopSimulation(dto, model);

        } catch (Exception e) {
            throw new BlockCreationException(
                    String.format("Failed to create StopSimulation block from DTO: %s (block: %s)",
                                e.getMessage(), dto.getBlockName()), e);
        }
    }

    // === Block Calculation ===
    public void calculateOutput() throws MatDimException {
        // Get input signal
        if (inputPortList.isEmpty()) {
            log.warn("StopSimulation block '{}': No input ports", blockName);
            return;
        }

        InputPort inputPort = inputPortList.get(0);
        if (inputPort.getLinkedLine() == null || inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            log.warn("StopSimulation block '{}': No input signal connected", blockName);
            return;
        }

        OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (inputSignal == null) {
            log.warn("StopSimulation block '{}': No input signal connected", blockName);
            return;
        }

        // Check if stop condition is met
        if (meetsStopCondition(inputSignal)) {
            String message = stopMessage != null ? stopMessage.getInitString() : "Simulation stopped";
            String fullMessage = String.format("StopSimulation block '%s': %s", blockName, message);

            log.info(fullMessage);

            // Throw simulation stop exception
            throw new SimulationStopException(fullMessage, blockName, 0.0, true);
        }
    }

    /**
     * Checks if the input signal meets the stop condition
     *
     * @param inputSignal the input signal to check
     * @return true if stop condition is met
     */
    private boolean meetsStopCondition(OutputSignal inputSignal) {
        try {
            Data inputData = inputSignal.getData();
            if (inputData == null) return false;

            // Get stop condition
            String condition = stopCondition != null ? stopCondition.getInitString() : "nonzero";

            // Get input value
            double inputValue = getInputValue(inputData);

            // Check condition
            switch (condition) {
                case "nonzero":
                    return inputValue != 0.0;

                case ">0":
                    return inputValue > 0.0;

                case "<0":
                    return inputValue < 0.0;

                case "==value":
                    double compareValue = comparisonValue != null ?
                        Double.parseDouble(comparisonValue.getInitString()) : 1.0;
                    return Math.abs(inputValue - compareValue) < 1e-10;

                default:
                    return inputValue != 0.0;
            }

        } catch (Exception e) {
            log.error("Error checking stop condition in block '{}': {}", blockName, e.getMessage());
            return false;
        }
    }

    /**
     * Gets the input value from the signal data
     */
    private double getInputValue(Data inputData) {
        // Handle scalar values
        double initValue = inputData.getInitValue();
        if (!Double.isNaN(initValue)) {
            return initValue;
        }

        // Handle matrix values (use first element)
        if (inputData.getMatrix() != null) {
            Matrix m = inputData.getMatrix();
            if (m.getRowDimension() > 0 && m.getColumnDimension() > 0) {
                return m.get(0, 0);
            }
        }

        return 0.0;
    }

    // Code generation methods are handled by templates via TemplateManager
}
