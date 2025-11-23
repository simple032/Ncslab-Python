package com.ncslab.block.route;

import com.ncslab.block.route.RouteBlock;
import com.ncslab.block.data.Data;
import lombok.Getter;

import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.route.MultiportSwitchDto;

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
 * Multiport Switch block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * The Multiport Switch block routes one of many inputs to the output based on
 * a control signal. This is essential for multi-way conditional routing in
 * complex control systems.
 *
 * SIMULINK Parameters:
 * - NumberOfDataInputs: Number of data input ports (excluding control input)
 * - DataPortOrder: Data port indexing ("Zero-based contiguous" or "One-based contiguous")
 * - DefaultOutput: Default output value when control is out of range
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 *
 * Port Configuration:
 * - Input 0: Control signal (scalar integer)
 * - Inputs 1 to N: Data inputs (numberOfDataInputs)
 * - Output: Selected data input
 *
 * Selection Logic:
 * - Control signal is converted to integer
 * - Zero-based: control=0 → input 1, control=1 → input 2, etc.
 * - One-based: control=1 → input 1, control=2 → input 2, etc.
 * - Out of range or invalid control → defaultOutput
 */
public class MultiportSwitch extends RouteBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter numberOfDataInputs;
    private final Parameter dataPortOrder;
    private final Parameter defaultOutput;
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // === Port References ===
    private OutputPort output;
    private InputPort controlInput; // Control signal input (port 0)
    private List<InputPort> dataInputs; // Data inputs (ports 1 to N)

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
        PARAMETER_DEFAULTS.put("NumberOfDataInputs", "3");
        PARAMETER_DEFAULTS.put("DataPortOrder", "Zero-based contiguous");
        PARAMETER_DEFAULTS.put("DefaultOutput", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    static {
        // Port names (dynamic based on numberOfDataInputs, these are defaults)
        outputNames.add("out1");
        inputNames.add("control"); // Control input
        inputNames.add("in1"); // First data input
        inputNames.add("in2"); // Second data input
        inputNames.add("in3"); // Third data input

        // Input port defaults (1 control + 3 data inputs by default)
        INPUT_PORT_DEFAULTS = new ArrayList<>();

        // Control input (port 0)
        Map<String, Object> controlInput = new HashMap<>();
        controlInput.put("name", "control");
        controlInput.put("width", 1);
        controlInput.put("height", 1);
        controlInput.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(controlInput);

        // Data inputs (ports 1-3)
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> dataInput = new HashMap<>();
            dataInput.put("name", "in" + i);
            dataInput.put("width", 1);
            dataInput.put("height", 1);
            dataInput.put("dataType", "REAL");
            INPUT_PORT_DEFAULTS.add(dataInput);
        }

        // Output port defaults (Multiport Switch has feedthrough)
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
    private MultiportSwitch(Parameter numberOfDataInputs, Parameter dataPortOrder,
                           Parameter defaultOutput, Parameter sampleTime, Parameter outDataType,
                           String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.numberOfDataInputs = Objects.requireNonNull(numberOfDataInputs, "Number of data inputs parameter cannot be null");
        this.dataPortOrder = Objects.requireNonNull(dataPortOrder, "Data port order parameter cannot be null");
        this.defaultOutput = Objects.requireNonNull(defaultOutput, "Default output parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.numberOfDataInputs);
        parameterList.add(this.dataPortOrder);
        parameterList.add(this.defaultOutput);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public MultiportSwitch(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Get parameters by name from the automatically populated parameterList
        this.numberOfDataInputs = getParameterByName("NumberOfDataInputs");
        this.dataPortOrder = getParameterByName("DataPortOrder");
        this.defaultOutput = getParameterByName("DefaultOutput");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize ports
        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates Multiport Switch block directly from MultiportSwitchDto
     */
    public MultiportSwitch(MultiportSwitchDto dto, NCSLabModel model) {
        super(dto, model);

        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }

        // Get parameters by name from the automatically populated parameterList
        this.numberOfDataInputs = getParameterByName("NumberOfDataInputs");
        this.dataPortOrder = getParameterByName("DataPortOrder");
        this.defaultOutput = getParameterByName("DefaultOutput");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static MultiportSwitch fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter numberOfDataInputs = createNumberOfDataInputsFromJSON(paramValues, blockName);
            Parameter dataPortOrder = createDataPortOrderFromJSON(paramValues, blockName);
            Parameter defaultOutput = createDefaultOutputFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);

            MultiportSwitch block = new MultiportSwitch(numberOfDataInputs, dataPortOrder, defaultOutput,
                                                       sampleTime, outDataType,
                                                       blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, numberOfDataInputs, dataPortOrder, defaultOutput, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Multiport Switch block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static MultiportSwitch create(String name, String path, int numberOfDataInputs, NCSLabModel model) {
        return create(name, path, numberOfDataInputs, "Zero-based contiguous", 0.0, -1.0, "Inherit: Same as input", model);
    }

    /**
     * Create a Multiport Switch block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param numberOfDataInputs Number of data input ports (excluding control input)
     * @param dataPortOrder Data port indexing order
     * @param defaultOutput Default output value when control is out of range
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return Multiport Switch block instance
     */
    public static MultiportSwitch create(String name, String path, int numberOfDataInputs, String dataPortOrder,
                                        double defaultOutput, double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        MultiportSwitchDto dto = MultiportSwitchDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .numberOfDataInputs(com.ncslab.dto.common.TypedParameter.of(numberOfDataInputs))
            .dataPortOrder(com.ncslab.dto.common.TypedParameter.of(dataPortOrder))
            .defaultOutput(com.ncslab.dto.common.TypedParameter.of(defaultOutput))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Multiport Switch parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new MultiportSwitch(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createNumberOfDataInputsFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("NumberOfDataInputs", "3");
        return new Parameter(null, 1, "NumberOfDataInputs", value);
    }

    private static Parameter createDataPortOrderFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("DataPortOrder", "Zero-based contiguous");
        return new Parameter(null, 2, "DataPortOrder", value);
    }

    private static Parameter createDefaultOutputFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("DefaultOutput", "0.0");
        return new Parameter(null, 3, "DefaultOutput", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 4, "SampleTime", value);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 5, "OutDataTypeStr", value);
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

    private static void setParameterBlockReference(MultiportSwitch block, Parameter... parameters) {
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
        identity.put("blockType", "Multiport Switch");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        dataInputs = new ArrayList<>();
        int numDataInputs = (int) numberOfDataInputs.getDouble();

        // Control input (port 0)
        controlInput = new InputPort(this, 1);
        inputPortList.add(controlInput);

        // Data inputs (ports 1 to N)
        for (int i = 0; i < numDataInputs; i++) {
            InputPort dataInput = new InputPort(this, i + 2); // Port numbers start at 2
            inputPortList.add(dataInput);
            dataInputs.add(dataInput);
        }

        // Main output port (has feedthrough)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add numberOfDataInputs parameter for template
        context.put("numberOfDataInputs", numberOfDataInputs);
        context.put("numberOfDataInputsValue", (int) numberOfDataInputs.getDouble());
        context.put("dataPortOrder", dataPortOrder);
        context.put("dataPortOrderValue", dataPortOrder.getInitString());
        context.put("defaultOutput", defaultOutput);
        context.put("defaultOutputInitCodeC", defaultOutput.getInitCodeC());

        String initCode = TemplateManager.renderTemplate("c/route/MultiportSwitch/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add port counts and parameters
        context.put("inputPortListSize", inputPortList.size());
        context.put("numberOfDataInputsValue", (int) numberOfDataInputs.getDouble());
        context.put("dataPortOrderValue", dataPortOrder.getInitString());

        // Add control input signal name
        context.put("controlInputSignalName", getInputPortVariable(0));

        // Add data input signal names
        List<String> dataInputSignalNames = new ArrayList<>();
        for (int i = 0; i < dataInputs.size(); i++) {
            dataInputSignalNames.add(getInputPortVariable(i + 1)); // Skip control input (port 0)
        }
        context.put("dataInputSignalNames", dataInputSignalNames);

        // Add output signal name
        context.put("outputPort0SignalName", getOutputPortVariable(0));

        // Add default output parameter
        context.put("defaultOutput", defaultOutput);
        context.put("defaultOutputName", context.get(defaultOutput.getLocalName()));

        String outputCode = TemplateManager.renderTemplate("c/route/MultiportSwitch/output.vm", context);
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException {
        // All data inputs must have the same dimension
        OutputPort out = outputPortList.get(0);

        if (dataInputs.isEmpty()) {
            throw new MatDimException("Multiport Switch block " + this.blockName + " has no data input ports!");
        }

        // Get first connected data input to determine dimension
        InputPort firstDataInput = null;
        OutputSignal firstSignal = null;

        for (InputPort dataInput : dataInputs) {
            if (dataInput.getLinkedLine() != null && dataInput.getLinkedLine().getLinkedOutputPort() != null) {
                firstDataInput = dataInput;
                firstSignal = dataInput.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                break;
            }
        }

        if (firstSignal == null) {
            throw new MatDimException("Multiport Switch block " + this.blockName + " has no connected data inputs!");
        }

        // Verify all connected data inputs have the same dimension
        for (InputPort dataInput : dataInputs) {
            if (dataInput.getLinkedLine() != null && dataInput.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal signal = dataInput.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                if (signal.getHeight() != firstSignal.getHeight() || signal.getWidth() != firstSignal.getWidth()) {
                    throw new MatDimException("Block " + this.blockName + " data input dimensions don't match! All data inputs must have the same dimension.\n");
                }
            }
        }

        // Set output dimension based on first data input
        out.setHeight(firstSignal.getHeight());
        out.setWidth(firstSignal.getWidth());
        out.getOutputSignalC().setHeight(firstSignal.getHeight());
        out.getOutputSignalC().setWidth(firstSignal.getWidth());
        out.getOutputSignalC().setDataType(firstSignal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // No additional dimension checks needed for Multiport Switch block
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Multiport Switch block logic:
        // - Read control signal from input 0
        // - Convert to integer (round to nearest)
        // - Determine which data input to route based on control value and dataPortOrder
        // - If control is out of range or invalid, use defaultOutput

        // Get control signal value
        Data controlData = inputPortList.get(0).getData(); // Control input is at port 0
        double controlValue = controlData.getInitValue();

        // Handle NaN and Inf
        if (Double.isNaN(controlValue) || Double.isInfinite(controlValue)) {
            // Invalid control signal, use default output
            output.setData(new Data(defaultOutput.getDouble()));
            return;
        }

        // Convert to integer (round to nearest)
        int controlInt = (int) Math.round(controlValue);

        // Determine selected data input based on dataPortOrder
        int selectedDataInputIndex; // Index in dataInputs list (0-based)
        String portOrder = dataPortOrder.getInitString();

        if ("Zero-based contiguous".equals(portOrder)) {
            // Zero-based: control=0 → dataInputs[0], control=1 → dataInputs[1], etc.
            selectedDataInputIndex = controlInt;
        } else {
            // One-based: control=1 → dataInputs[0], control=2 → dataInputs[1], etc.
            selectedDataInputIndex = controlInt - 1;
        }

        // Validate range
        if (selectedDataInputIndex < 0 || selectedDataInputIndex >= dataInputs.size()) {
            // Out of range, use default output
            output.setData(new Data(defaultOutput.getDouble()));
            return;
        }

        // Get the selected data input (port index is selectedDataInputIndex + 1 because control is at port 0)
        InputPort selectedInput = inputPortList.get(selectedDataInputIndex + 1);

        // Check if input is connected
        if (selectedInput.getLinkedLine() == null || selectedInput.getLinkedLine().getLinkedOutputPort() == null) {
            // Input not connected, use default output
            output.setData(new Data(defaultOutput.getDouble()));
            return;
        }

        // Route selected input to output
        Data selectedData = selectedInput.getData();
        output.setData(selectedData);
    }
}
