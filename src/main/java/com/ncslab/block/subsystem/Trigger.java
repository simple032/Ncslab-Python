package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
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
import com.ncslab.dto.block.specialized.subsystem.TriggerDto;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Trigger block for event-driven subsystem execution with SIMULINK-compatible parameters.
 *
 * <p>The Trigger block controls when a subsystem executes based on signal transitions.
 * It detects rising edges, falling edges, or either edge type, and can optionally
 * pass through the trigger signal as an output.</p>
 *
 * <p><b>SIMULINK Parameters:</b></p>
 * <ul>
 *   <li><b>TriggerType</b>: Type of edge detection ("rising", "falling", "either", "function-call")</li>
 *   <li><b>ShowOutputPort</b>: Create optional output port for trigger signal ("on", "off")</li>
 *   <li><b>ZeroCross</b>: Enable zero-crossing detection for precise edges ("on", "off")</li>
 *   <li><b>SampleTime</b>: Inherited sample time (-1)</li>
 *   <li><b>OutputDataType</b>: Data type specification ("auto" or specific type)</li>
 * </ul>
 *
 * <p><b>Edge Detection:</b></p>
 * <ul>
 *   <li><b>Rising Edge</b>: Transition from zero (or negative) to positive value</li>
 *   <li><b>Falling Edge</b>: Transition from positive to zero (or negative) value</li>
 *   <li><b>Either Edge</b>: Any rising or falling edge</li>
 *   <li><b>Function-Call</b>: Explicit function call mechanism</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 input port (trigger signal - scalar double)</li>
 *   <li>0 or 1 output port (optional trigger signal passthrough if ShowOutputPort="on")</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
public class Trigger extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter triggerType;
    private final Parameter showOutputPort;
    private final Parameter zeroCross;
    private final Parameter sampleTime;
    private final Parameter outputDataType;

    // === Subsystem Reference ===
    @Setter
    @Getter
    private Subsystem subsystem;

    // === Edge Detection State ===
    private Data previousTriggerSignal;
    private boolean isTriggered = false;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("TriggerType", "rising");
        PARAMETER_DEFAULTS.put("ShowOutputPort", "off");
        PARAMETER_DEFAULTS.put("ZeroCross", "on");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutputDataType", "auto");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names - Trigger block has one input for trigger signal
        inputNames.add("trigger");
        // Output port conditionally added based on ShowOutputPort parameter
    }

    // === Private Constructor with Typed Parameters ===
    private Trigger(Parameter triggerType, Parameter showOutputPort, Parameter zeroCross,
                   Parameter sampleTime, Parameter outputDataType,
                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.triggerType = Objects.requireNonNull(triggerType, "TriggerType parameter cannot be null");
        this.showOutputPort = Objects.requireNonNull(showOutputPort, "ShowOutputPort parameter cannot be null");
        this.zeroCross = Objects.requireNonNull(zeroCross, "ZeroCross parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outputDataType = Objects.requireNonNull(outputDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.triggerType);
        parameterList.add(this.showOutputPort);
        parameterList.add(this.zeroCross);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outputDataType);

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Trigger(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.triggerType = getParameterByName("TriggerType");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.zeroCross = getParameterByName("ZeroCross");
        this.sampleTime = getParameterByName("SampleTime");
        this.outputDataType = getParameterByName("OutputDataType");

        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates Trigger block directly from TriggerDto DTO
     */
    public Trigger(TriggerDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.triggerType = getParameterByName("TriggerType");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.zeroCross = getParameterByName("ZeroCross");
        this.sampleTime = getParameterByName("SampleTime");
        this.outputDataType = getParameterByName("OutputDataType");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Trigger fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter triggerType = createTriggerTypeFromJSON(paramValues);
            Parameter showOutputPort = createShowOutputPortFromJSON(paramValues);
            Parameter zeroCross = createZeroCrossFromJSON(paramValues);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues);
            Parameter outputDataType = createOutputDataTypeFromJSON(paramValues);

            Trigger block = new Trigger(triggerType, showOutputPort, zeroCross, sampleTime, outputDataType,
                                       blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, triggerType, showOutputPort, zeroCross, sampleTime, outputDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Trigger block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Trigger create(String name, String path, NCSLabModel model) {
        return create(name, path, "rising", false, true, -1.0, "auto", model);
    }

    /**
     * Create a Trigger block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param triggerType Type of trigger detection ("rising", "falling", "either", "function-call")
     * @param showOutputPort Whether to create output port for trigger signal passthrough
     * @param zeroCross Enable zero-crossing detection
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outputDataType Output data type specification
     * @param model Parent model
     * @return Trigger block instance
     */
    public static Trigger create(String name, String path, String triggerType, boolean showOutputPort,
                                boolean zeroCross, double sampleTime, String outputDataType, NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        TriggerDto dto = new TriggerDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(triggerType),
            com.ncslab.dto.common.TypedParameter.of(showOutputPort ? "on" : "off"),
            com.ncslab.dto.common.TypedParameter.of(zeroCross ? "on" : "off"),
            com.ncslab.dto.common.TypedParameter.of(sampleTime),
            com.ncslab.dto.common.TypedParameter.of(outputDataType)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid Trigger parameters: " + dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Trigger(dto, model);
    }

    // === Code Generation Methods ===

    /**
     * Generates array declarations for trigger state variables.
     */
    public void generateArraysCodeC(CodeStructC code) {
        try {
            // Populate all standard context variables
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            String codeStr = TemplateManager.renderTemplate("c/subsystem/Trigger/arrays.vm", context);
            code.addArraysCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates initialization code for trigger state variables.
     */
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        try {
            // Populate all standard context variables
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            String templatePath = "c/subsystem/Trigger/init.vm";
            String codeStr = TemplateManager.renderTemplate(templatePath, context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add Trigger-specific context
        context.put("triggerType", getTriggerTypeValue());
        context.put("hasOutputPort", hasOutputPort());
        context.put("zeroCrossingEnabled", isZeroCrossingEnabled());
        context.put("subsystem", subsystem);

        // Safely handle signal connection chain with null checks
        InputPort inputPort = inputPortList.get(0);
        if (inputPort != null && inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null) {
            context.put("inputSignal", inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        } else {
            context.put("inputSignal", "0.0"); // Default value when no input connected
        }

        if (hasOutputPort() && outputPortList.size() > 0) {
            context.put("outputSignal", outputPortList.get(0).getOutputSignalC().getName());
        } else {
            context.put("outputSignal", null);
        }

        String outputCode = TemplateManager.renderTemplate("c/subsystem/Trigger/output.vm", context);
        code.addOutputCode(outputCode);
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Trigger block passes dimensions from input to optional output
        InputPort in = inputPortList.get(0);

        if (hasOutputPort() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            if (in.getLinkedLine() != null) {
                OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                out.setHeight(signal.getHeight());
                out.setWidth(signal.getWidth());
                out.getOutputSignalC().setHeight(signal.getHeight());
                out.getOutputSignalC().setWidth(signal.getWidth());
                out.getOutputSignalC().setDataType(signal.getDataType());
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Validation can be added here if needed
    }

    @Override
    public void calculateInit() {
        // Initialize previous trigger signal state to zero
        previousTriggerSignal = new Data(0.0);
        isTriggered = false;
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in = inputPortList.get(0);
        Data inputData = in.getData();

        // Detect trigger event based on trigger type
        boolean edgeDetected = false;
        String triggerTypeStr = getTriggerTypeValue();

        switch (triggerTypeStr) {
            case "rising":
                edgeDetected = detectRisingEdge(inputData);
                break;
            case "falling":
                edgeDetected = detectFallingEdge(inputData);
                break;
            case "either":
                edgeDetected = detectRisingEdge(inputData) || detectFallingEdge(inputData);
                break;
            case "function-call":
                // Function-call triggers are handled externally
                edgeDetected = false;
                break;
            default:
                edgeDetected = false;
        }

        isTriggered = edgeDetected;

        // Pass through trigger signal to output if ShowOutputPort is enabled
        if (hasOutputPort() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            out.setData(inputData);
        }

        // Store current value for next comparison
        if (inputData.getDataType() == DataType.REAL) {
            previousTriggerSignal = new Data(inputData.getInitValue());
        } else {
            previousTriggerSignal = new Data(0.0); // Trigger only works with scalar signals
        }
    }

    // === Edge Detection Methods ===

    /**
     * Checks if a trigger event has occurred.
     *
     * @return true if trigger event detected in current time step
     */
    public boolean isTriggered() {
        return isTriggered;
    }

    /**
     * Detects a rising edge (0 to positive transition).
     *
     * @param currentData Current trigger signal value
     * @return true if rising edge detected
     */
    private boolean detectRisingEdge(Data currentData) {
        if (currentData.getDataType() != DataType.REAL) {
            return false; // Trigger only works with scalar signals
        }

        double currentValue = currentData.getInitValue();
        double previousValue = previousTriggerSignal != null ? previousTriggerSignal.getInitValue() : 0.0;

        // Rising edge: previous value <= 0 and current value > 0
        return previousValue <= 0.0 && currentValue > 0.0;
    }

    /**
     * Detects a falling edge (positive to 0 transition).
     *
     * @param currentData Current trigger signal value
     * @return true if falling edge detected
     */
    private boolean detectFallingEdge(Data currentData) {
        if (currentData.getDataType() != DataType.REAL) {
            return false; // Trigger only works with scalar signals
        }

        double currentValue = currentData.getInitValue();
        double previousValue = previousTriggerSignal != null ? previousTriggerSignal.getInitValue() : 0.0;

        // Falling edge: previous value > 0 and current value <= 0
        return previousValue > 0.0 && currentValue <= 0.0;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createTriggerTypeFromJSON(JSONObject paramValues) {
        String triggerTypeValue = paramValues.optString("TriggerType", "rising");
        return new Parameter(null, 1, "TriggerType", triggerTypeValue);
    }

    private static Parameter createShowOutputPortFromJSON(JSONObject paramValues) {
        String showOutputValue = paramValues.optString("ShowOutputPort", "off");
        return new Parameter(null, 2, "ShowOutputPort", showOutputValue);
    }

    private static Parameter createZeroCrossFromJSON(JSONObject paramValues) {
        String zeroCrossValue = paramValues.optString("ZeroCross", "on");
        return new Parameter(null, 3, "ZeroCross", zeroCrossValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 4, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutputDataTypeFromJSON(JSONObject paramValues) {
        String outputDataTypeValue = paramValues.optString("OutputDataType", "auto");
        return new Parameter(null, 5, "OutputDataType", outputDataTypeValue);
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

    private static void setParameterBlockReference(Trigger block, Parameter... parameters) {
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
        identity.put("blockType", "Trigger");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Trigger block always has one input port for trigger signal
        inputPortList.add(new InputPort(this, 1));

        // Output port is optional based on ShowOutputPort parameter
        if (hasOutputPort()) {
            outputPortList.add(new OutputPort(this, 1, true));
            if (!outputNames.contains("trigger")) {
                outputNames.add("trigger");
            }
        }
    }

    // === Accessor Methods ===

    /**
     * Gets the trigger type parameter.
     *
     * @return Trigger type parameter
     */
    public Parameter getTriggerTypeParameter() {
        return triggerType;
    }

    /**
     * Gets the trigger type value.
     *
     * @return Trigger type string ("rising", "falling", "either", "function-call")
     */
    public String getTriggerTypeValue() {
        return triggerType.getInitString();
    }

    /**
     * Checks if output port is enabled.
     *
     * @return true if ShowOutputPort is "on"
     */
    public boolean hasOutputPort() {
        return "on".equals(showOutputPort.getInitString());
    }

    /**
     * Checks if zero-crossing detection is enabled.
     *
     * @return true if ZeroCross is "on"
     */
    public boolean isZeroCrossingEnabled() {
        return "on".equals(zeroCross.getInitString());
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for discrete operation
     */
    public double getSampleTimeValue() {
        try {
            return sampleTime.getData().getInitValue();
        } catch (NumberFormatException e) {
            return -1.0; // Default inherited
        }
    }
}
