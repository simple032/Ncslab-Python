package com.ncslab.block.route;

import com.ncslab.block.route.RouteBlock;
import com.ncslab.block.data.Data;
import lombok.Getter;

import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.route.ManualSwitchDto;

import com.ncslab.block.data.DataType;
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
 * Manual Switch block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * The Manual Switch block provides user-controlled routing between two inputs.
 * Unlike automatic switches controlled by signals, this block's switching state
 * is manually set through a parameter that can be changed interactively.
 *
 * SIMULINK Parameters:
 * - SwitchControl: Control state ("0" or "1") determining which input is routed
 * - SampleTime: Sample time for block operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 *
 * Port Configuration:
 * - Input 0: First data input (routed when SwitchControl = "0")
 * - Input 1: Second data input (routed when SwitchControl = "1")
 * - Output: Selected input signal
 *
 * Selection Logic:
 * - When SwitchControl = "0": Output = Input 0
 * - When SwitchControl = "1": Output = Input 1
 * - Control can be changed during simulation (interactive control)
 *
 * Key Features:
 * - Manual, user-controlled switching
 * - Two input ports with single output
 * - Feedthrough behavior
 * - Supports scalar and matrix signals
 *
 * @author NCSLab
 * @version 1.0
 * @since Manual Switch Implementation 2025
 */
public class ManualSwitch extends RouteBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter switchControl;
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // === Port References ===
    private OutputPort output;
    private List<InputPort> inputs;

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
        PARAMETER_DEFAULTS.put("SwitchControl", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Inherit via internal rule");
    }

    static {
        // Port names (2 inputs, 1 output)
        outputNames.add("out1");
        inputNames.add("in0");
        inputNames.add("in1");

        // Input port defaults (2 inputs)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input0 = new HashMap<>();
        input0.put("name", "in0");
        input0.put("width", 1);
        input0.put("height", 1);
        input0.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input0);

        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (Manual Switch has feedthrough)
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
    private ManualSwitch(Parameter switchControl, Parameter sampleTime, Parameter outDataType,
                        String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.switchControl = Objects.requireNonNull(switchControl, "Switch control parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.switchControl);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public ManualSwitch(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Get parameters by name from the automatically populated parameterList
        this.switchControl = getParameterByName("SwitchControl");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize ports
        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates Manual Switch block directly from ManualSwitchDto
     */
    public ManualSwitch(ManualSwitchDto dto, NCSLabModel model) {
        super(dto, model);

        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }

        // Get parameters by name from the automatically populated parameterList
        this.switchControl = getParameterByName("SwitchControl");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static ManualSwitch fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter switchControl = createSwitchControlFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);

            ManualSwitch block = new ManualSwitch(switchControl, sampleTime, outDataType,
                                                 blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, switchControl, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Manual Switch block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static ManualSwitch create(String name, String path, NCSLabModel model) {
        return create(name, path, "0", -1.0, "Inherit: Inherit via internal rule", model);
    }

    /**
     * Create a Manual Switch block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param switchControl Switch control state ("0" or "1")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return Manual Switch block instance
     */
    public static ManualSwitch create(String name, String path, String switchControl,
                                     double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        ManualSwitchDto dto = ManualSwitchDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .switchControl(com.ncslab.dto.common.TypedParameter.of(switchControl))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Manual Switch parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new ManualSwitch(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSwitchControlFromJSON(JSONObject paramValues, String blockName) {
        String switchControlValue = paramValues.optString("SwitchControl", "0");
        return new Parameter(null, 1, "SwitchControl", switchControlValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Inherit via internal rule");
        return new Parameter(null, 3, "OutDataTypeStr", outDataTypeValue);
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

    private static void setParameterBlockReference(ManualSwitch block, Parameter... parameters) {
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
        identity.put("blockType", "Manual Switch");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        inputs = new ArrayList<>();

        // Create 2 input ports (input 0 and input 1)
        for (int i = 0; i < 2; i++) {
            InputPort inputPort = new InputPort(this, i + 1);
            inputPortList.add(inputPort);
            inputs.add(inputPort);
        }

        // Main output port (has feedthrough)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add switchControl parameter for template
        context.put("switchControl", switchControl);
        context.put("switchControlValue", switchControl.getInitString());

        String initCode = TemplateManager.renderTemplate("c/route/ManualSwitch/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add switch control parameter
        context.put("switchControlValue", switchControl.getInitString());

        // Add input signal names
        context.put("inputVar0", getInputPortVariable(0));
        context.put("inputVar1", getInputPortVariable(1));

        // Add output signal name
        context.put("outputVar", getOutputPortVariable(0));

        String outputCode = TemplateManager.renderTemplate("c/route/ManualSwitch/output.vm", context);
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException {
        // Both inputs must have the same dimension
        OutputPort out = outputPortList.get(0);

        if (inputPortList.isEmpty() || inputPortList.size() < 2) {
            throw new MatDimException("Manual Switch block " + this.blockName + " must have exactly 2 input ports!");
        }

        // Get first connected input to determine dimension
        InputPort firstInput = null;
        OutputSignal firstSignal = null;

        for (InputPort input : inputPortList) {
            if (input.getLinkedLine() != null && input.getLinkedLine().getLinkedOutputPort() != null) {
                firstInput = input;
                firstSignal = input.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                break;
            }
        }

        if (firstSignal == null) {
            throw new MatDimException("Manual Switch block " + this.blockName + " has no connected inputs!");
        }

        // Verify all connected inputs have the same dimension
        for (InputPort input : inputPortList) {
            if (input.getLinkedLine() != null && input.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal signal = input.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                if (signal.getHeight() != firstSignal.getHeight() || signal.getWidth() != firstSignal.getWidth()) {
                    throw new MatDimException("Block " + this.blockName + " input dimensions don't match! All inputs must have the same dimension.\n");
                }
            }
        }

        // Set output dimension based on first input
        out.setHeight(firstSignal.getHeight());
        out.setWidth(firstSignal.getWidth());
        out.getOutputSignalC().setHeight(firstSignal.getHeight());
        out.getOutputSignalC().setWidth(firstSignal.getWidth());
        out.getOutputSignalC().setDataType(firstSignal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // No additional dimension checks needed for Manual Switch block
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Manual Switch block logic:
        // - Read SwitchControl parameter ("0" or "1")
        // - Route corresponding input to output
        // - Default to input 0 if control value is invalid

        // Get switch control parameter value
        String control = switchControl != null ? switchControl.getInitString() : "0";

        // Determine which input to select (default to 0)
        int selectedInput = "1".equals(control) ? 1 : 0;

        // Get the selected input port
        InputPort inputPort = inputPortList.get(selectedInput);

        // Check if input is connected
        if (inputPort.getLinkedLine() != null &&
            inputPort.getLinkedLine().getLinkedOutputPort() != null) {

            // Route selected input to output
            OutputSignal inputSignal = inputPort.getLinkedLine()
                .getLinkedOutputPort().getOutputSignalC();

            if (inputSignal != null && inputSignal.getData() != null) {
                output.setData(inputSignal.getData());
            }
        } else {
            // Input not connected, set output to zero
            output.setData(new Data(0.0));
        }
    }

    @Override
    public void calculateUpdate(double t) {
        // Manual Switch has no internal state to update
    }
}
