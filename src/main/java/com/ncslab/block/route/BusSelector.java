package com.ncslab.block.route;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.BusElement;
import com.ncslab.block.io.BusSignal;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.dto.block.specialized.route.BusSelectorDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bus Selector block - Extracts specified elements from bus signal.
 *
 * This block extracts individual elements from a bus signal and creates
 * separate output signals for each selected element. Supports both flat
 * and hierarchical element paths.
 *
 * SIMULINK-Compatible Parameters:
 * - NumberOfOutputs: Number of output ports (1-100, default: 1)
 * - SelectedSignals: Comma-separated element paths
 *   - Flat: "signal1,signal2,signal3"
 *   - Hierarchical: "motor.speed,motor.controller.setpoint,sensors.temperature"
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 *
 * Examples:
 * - Flat selection: "temperature,pressure,flow"
 * - Nested selection: "sensors.temperature,sensors.pressure,controller.output"
 * - Mixed: "speed,motor.controller.setpoint"
 *
 * Port Configuration:
 * - Input: Bus signal (single input port)
 * - Outputs: Individual signals (one per selected element)
 *
 * @author NCSLab Bus Architecture Implementation
 * @version 2.0 - Full BusSignal support with hierarchical paths
 * @since 2025-01-16
 */
public class BusSelector extends RouteBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter numberOfOutputs;
    private final Parameter selectedSignals;
    private final Parameter sampleTime;
    private final Parameter outDataTypeStr;

    // === Internal State ===
    private String[] selectedSignalNames;

    // === Static Parameter Definitions ===
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("NumberOfOutputs", "1");
        PARAMETER_DEFAULTS.put("SelectedSignals", "signal1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Inherit via back propagation");
    }

    static {
        // Port names
        inputNames.add("in1");
        // Output names are dynamic based on selected signals
    }

    // === Private Constructor with Typed Parameters ===
    private BusSelector(Parameter numberOfOutputs, Parameter selectedSignals,
                       Parameter sampleTime, Parameter outDataTypeStr,
                       String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.numberOfOutputs = Objects.requireNonNull(numberOfOutputs, "NumberOfOutputs parameter cannot be null");
        this.selectedSignals = Objects.requireNonNull(selectedSignals, "SelectedSignals parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataTypeStr = Objects.requireNonNull(outDataTypeStr, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.numberOfOutputs);
        parameterList.add(this.selectedSignals);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataTypeStr);

        // Parse selected signals
        parseSelectedSignals();

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public BusSelector(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility
        this.numberOfOutputs = getParameterByName("NumberOfOutputs");
        this.selectedSignals = getParameterByName("SelectedSignals");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");

        // Parse selected signals
        parseSelectedSignals();

        // Initialize ports
        initializePorts();
    }

    // === DTO Constructor (Preferred) ===
    /**
     * DTO-based constructor - preferred for new implementations
     * @param dto The DTO containing block configuration
     * @param model The parent model
     */
    public BusSelector(BusSelectorDto dto, NCSLabModel model) {
        super(dto, model);

        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }

        // Initialize from DTO parameters
        this.numberOfOutputs = getParameterByName("NumberOfOutputs");
        this.selectedSignals = getParameterByName("SelectedSignals");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");

        // Parse selected signals
        parseSelectedSignals();

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static BusSelector fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter numberOfOutputs = createNumberOfOutputsFromJSON(paramValues, blockName);
            Parameter selectedSignals = createSelectedSignalsFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataTypeStr = createOutDataTypeFromJSON(paramValues, blockName);

            BusSelector block = new BusSelector(numberOfOutputs, selectedSignals, sampleTime,
                                               outDataTypeStr, blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, numberOfOutputs, selectedSignals, sampleTime, outDataTypeStr);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create BusSelector block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static BusSelector create(String name, String path, String selectedSignals, NCSLabModel model) {
        // Count number of signals
        int numOutputs = selectedSignals.split(",").length;
        return create(name, path, numOutputs, selectedSignals, -1.0,
                     "Inherit: Inherit via back propagation", model);
    }

    /**
     * Create a BusSelector block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param numberOfOutputs Number of output ports
     * @param selectedSignals Comma-separated element names
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return BusSelector block instance
     */
    public static BusSelector create(String name, String path, int numberOfOutputs, String selectedSignals,
                                    double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        BusSelectorDto dto = BusSelectorDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .numberOfOutputs(com.ncslab.dto.common.TypedParameter.of(numberOfOutputs))
            .selectedSignals(com.ncslab.dto.common.TypedParameter.of(selectedSignals))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid BusSelector parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new BusSelector(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createNumberOfOutputsFromJSON(JSONObject paramValues, String blockName) {
        String value = String.valueOf(paramValues.optInt("NumberOfOutputs", 1));
        return new Parameter(null, 1, "NumberOfOutputs", value);
    }

    private static Parameter createSelectedSignalsFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("SelectedSignals", "signal1");
        return new Parameter(null, 2, "SelectedSignals", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", value);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("OutDataTypeStr", "Inherit: Inherit via back propagation");
        return new Parameter(null, 4, "OutDataTypeStr", value);
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

    private static void setParameterBlockReference(BusSelector block, Parameter... parameters) {
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
        identity.put("blockType", "BusSelector");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void parseSelectedSignals() {
        String signalsStr = selectedSignals.getInitString();
        if (signalsStr == null || signalsStr.trim().isEmpty()) {
            selectedSignalNames = new String[]{"signal1"};
        } else {
            selectedSignalNames = signalsStr.split(",");
            for (int i = 0; i < selectedSignalNames.length; i++) {
                selectedSignalNames[i] = selectedSignalNames[i].trim();
            }
        }
    }

    private void initializePorts() {
        // Single input port (bus signal)
        inputPortList.add(new InputPort(this, 1));

        // Multiple output ports (one per selected signal)
        int numOutputs = numberOfOutputs.getData().getIntValue();
        for (int i = 0; i < numOutputs; i++) {
            outputPortList.add(new OutputPort(this, i + 1, true)); // feedthrough enabled
        }
    }

    // === Code Generation Methods ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add selected signals for template
        context.put("selectedSignals", selectedSignalNames);

        String codeStr = TemplateManager.renderTemplate("m/route/BusSelector/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add selected signals for template
        context.put("selectedSignals", selectedSignalNames);

        String codeStr = TemplateManager.renderTemplate("m/route/BusSelector/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/route/BusSelector/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add input signal name for template
        if (!inputPortList.isEmpty()) {
            context.put("inputPort0SignalName", getInputPortVariable(0));
        }

        // Add selected signals for template
        context.put("selectedSignals", selectedSignalNames);

        String codeStr = TemplateManager.renderTemplate("c/route/BusSelector/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add input signal name for template
        if (!inputPortList.isEmpty()) {
            context.put("inputPort0SignalName", getInputPortVariable(0));
        }

        // Add output signal names
        context.put("outputNames", getOutputPortVariables());

        // Add selected signals for template
        context.put("selectedSignals", selectedSignalNames);

        String codeStr = TemplateManager.renderTemplate("c/route/BusSelector/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/route/BusSelector/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/route/BusSelector/update.vm", context);
        code.addUpdateCode(codeStr);
    }

    // === Dimension Propagation ===
    @Override
    public void updateDimension() throws MatDimException {
        // Check input connection
        InputPort busInputPort = inputPortList.get(0);
        if (busInputPort.getLinkedLine() == null ||
            busInputPort.getLinkedLine().getLinkedOutputPort() == null) {
            throw new MatDimException("BusSelector '" + blockName + "': Input bus not connected");
        }

        OutputSignal inputSignal = busInputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // If input is a BusSignal, extract element dimensions
        if (inputSignal instanceof BusSignal) {
            BusSignal busSignal = (BusSignal) inputSignal;

            // Set output dimensions based on selected elements
            for (int i = 0; i < selectedSignalNames.length && i < outputPortList.size(); i++) {
                String signalPath = selectedSignalNames[i];
                OutputPort outputPort = outputPortList.get(i);

                // Get element signal to determine dimensions
                OutputSignal elementSignal;
                if (signalPath.contains(".")) {
                    elementSignal = busSignal.getNestedElement(signalPath);
                } else {
                    elementSignal = busSignal.getElementSignal(signalPath);
                }

                if (elementSignal != null) {
                    outputPort.setWidth(elementSignal.getWidth());
                    outputPort.setHeight(elementSignal.getHeight());

                    // Set output signal dimensions
                    OutputSignal outputSignal = outputPort.getOutputSignalC();
                    outputSignal.setWidth(elementSignal.getWidth());
                    outputSignal.setHeight(elementSignal.getHeight());
                    outputSignal.setDataType(elementSignal.getDataType());
                    outputSignal.setCDataType(elementSignal.getCDataType());
                } else {
                    // Element not found - set default dimensions
                    outputPort.setWidth(1);
                    outputPort.setHeight(1);
                    System.err.println("BusSelector '" + blockName + "': Element '" + signalPath + "' not found during dimension propagation");
                }
            }
        } else {
            // Fallback for non-bus inputs (backward compatibility)
            for (OutputPort outputPort : outputPortList) {
                outputPort.setWidth(inputSignal.getWidth());
                outputPort.setHeight(inputSignal.getHeight());
            }
        }

        System.out.println("BusSelector updateDimension: " + getBlockName() + " - Configured " +
                          selectedSignalNames.length + " outputs");
    }

    @Override
    public void checkDimension() throws MatDimException {
        InputPort inputPort = this.getInputPortList().get(0);

        // Verify input is connected
        if (inputPort.getLinkedLine() == null || inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input not connected!\n" +
                "Bus Selector requires a bus signal input.\n");
            throw(e);
        }

        // Verify number of selected signals matches numberOfOutputs
        if (selectedSignalNames.length != numberOfOutputs.getData().getIntValue()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " configuration error!\n" +
                "Number of selected signals (" + selectedSignalNames.length + ") does not match " +
                "numberOfOutputs parameter (" + numberOfOutputs.getData().getIntValue() + ").\n");
            throw(e);
        }
    }

    // === Runtime Execution ===
    @Override
    public void calculateOutput(double t) {
        InputPort busInputPort = inputPortList.get(0);

        // Check if input is connected
        if (busInputPort.getLinkedLine() == null ||
            busInputPort.getLinkedLine().getLinkedOutputPort() == null) {
            System.err.println("BusSelector '" + getBlockName() + "': Input bus not connected");
            return;
        }

        OutputSignal inputSignal = busInputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Check if input is actually a BusSignal
        if (!(inputSignal instanceof BusSignal)) {
            // Fallback: treat as regular signal (backward compatibility)
            System.err.println("BusSelector '" + getBlockName() + "': Input is not a bus signal, using fallback");

            // For non-bus inputs, just pass through to first output
            if (!outputPortList.isEmpty()) {
                outputPortList.get(0).setOutputSignalC(inputSignal);
            }
            return;
        }

        BusSignal busSignal = (BusSignal) inputSignal;

        // Extract each selected element and assign to corresponding output port
        for (int i = 0; i < selectedSignalNames.length && i < outputPortList.size(); i++) {
            String signalPath = selectedSignalNames[i];
            OutputPort outputPort = outputPortList.get(i);

            // Support hierarchical paths using BusSignal.getNestedElement()
            OutputSignal elementSignal;
            if (signalPath.contains(".")) {
                // Hierarchical path: "sensors.temperature"
                elementSignal = busSignal.getNestedElement(signalPath);
            } else {
                // Flat path: "temperature"
                elementSignal = busSignal.getElementSignal(signalPath);
            }

            if (elementSignal != null) {
                // Assign the signal directly to output port
                outputPort.setOutputSignalC(elementSignal);

                System.out.println("BusSelector '" + getBlockName() + "': Extracted '" +
                                 signalPath + "' from bus '" + busSignal.getBusName() + "'");
            } else {
                System.err.println("BusSelector '" + getBlockName() + "': Signal '" +
                                 signalPath + "' not found in bus '" + busSignal.getBusName() + "'");

                // Create a zero data as fallback
                outputPort.setData(new Data(0.0));
            }
        }
    }

    /**
     * Helper method to get output port variable names for template context.
     * @return Array of output variable names
     */
    @Override
    protected String[] getOutputPortVariables() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < outputPortList.size(); i++) {
            names.add(getOutputPortVariable(i));
        }
        return names.toArray(new String[0]);
    }
}
