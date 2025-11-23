package com.ncslab.block.route;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;
import Jama.Matrix;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.route.BusCreatorDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.BusSignal;
import com.ncslab.block.io.BusElement;
import com.ncslab.block.io.BusDefinition;
import com.ncslab.block.io.BusDefinitionRegistry;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * Bus Creator block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Creates a bus signal from multiple input signals by combining them into a structured format.
 * Supports both virtual buses (no memory overhead) and non-virtual buses (C struct generation).
 *
 * SIMULINK Parameters:
 * - NumberOfInputs: Number of input ports (1-100)
 * - ElementNames: Comma-separated names for each bus element
 * - BusOutputName: Name of the output bus
 * - IsVirtual: True for virtual bus, false for non-virtual (C struct)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 *
 * Key Features:
 * - Creates structured bus signals from multiple inputs
 * - Named bus elements for type-safe element access
 * - Virtual bus mode: zero runtime overhead
 * - Non-virtual bus mode: generates C struct
 * - Supports scalar, vector, and matrix signals
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
public class BusCreator extends RouteBlock {
    // === Configuration ===
    /** Number of input ports */
    private int numberOfInputs;

    /** Element names for bus */
    private String[] elementNames;

    /** Bus output name */
    private String busOutputName;

    /** Virtual bus flag */
    private boolean isVirtual;

    /** Feedthrough flag - Bus Creator has direct feedthrough */
    private boolean feedThrough = true;

    // === SIMULINK-Compatible Parameters ===
    /** Number of inputs parameter */
    private final Parameter numberOfInputsParam;

    /** Element names parameter */
    private final Parameter elementNamesParam;

    /** Bus output name parameter */
    private final Parameter busOutputNameParam;

    /** Virtual flag parameter */
    private final Parameter isVirtualParam;

    /** Sample time parameter */
    private final Parameter sampleTime;

    /** Output data type specification parameter */
    private final Parameter outDataType;

    // === Bus Signal ===
    /** Output bus signal containing all elements */
    private BusSignal outputBusSignal;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("NumberOfInputs", "2");
        PARAMETER_DEFAULTS.put("ElementNames", "signal1,signal2");
        PARAMETER_DEFAULTS.put("BusOutputName", "Bus");
        PARAMETER_DEFAULTS.put("IsVirtual", "true");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Bus: <object name>");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names are dynamic based on numberOfInputs
        outputNames.add("out1");
        // Input names are generated dynamically based on element names
    }

    // === Private Constructor with Typed Parameters ===
    private BusCreator(Parameter numberOfInputsParam, Parameter elementNamesParam,
                      Parameter busOutputNameParam, Parameter isVirtualParam,
                      Parameter sampleTime, Parameter outDataType,
                      String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.numberOfInputsParam = Objects.requireNonNull(numberOfInputsParam, "NumberOfInputs parameter cannot be null");
        this.elementNamesParam = Objects.requireNonNull(elementNamesParam, "ElementNames parameter cannot be null");
        this.busOutputNameParam = Objects.requireNonNull(busOutputNameParam, "BusOutputName parameter cannot be null");
        this.isVirtualParam = Objects.requireNonNull(isVirtualParam, "IsVirtual parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.numberOfInputsParam);
        parameterList.add(this.elementNamesParam);
        parameterList.add(this.busOutputNameParam);
        parameterList.add(this.isVirtualParam);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        // Parse parameters
        parseParameters();

        // Create ports
        createPorts();

        // Initialize bus signal (will be created in updateDimension)
        this.outputBusSignal = null;
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public BusCreator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.numberOfInputsParam = getParameterByName("NumberOfInputs");
        this.elementNamesParam = getParameterByName("ElementNames");
        this.busOutputNameParam = getParameterByName("BusOutputName");
        this.isVirtualParam = getParameterByName("IsVirtual");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Parse parameters
        parseParameters();

        // Create ports
        createPorts();

        // Initialize bus signal (will be created in updateDimension)
        this.outputBusSignal = null;
    }

    /**
     * DTO-NATIVE Constructor - Creates BusCreator block directly from BusCreatorDto DTO
     */
    public BusCreator(BusCreatorDto dto, NCSLabModel model) {
        super(dto, model);

        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }

        // Initialize final parameters from BusCreatorDto
        this.numberOfInputsParam = getParameterByName("NumberOfInputs");
        this.elementNamesParam = getParameterByName("ElementNames");
        this.busOutputNameParam = getParameterByName("BusOutputName");
        this.isVirtualParam = getParameterByName("IsVirtual");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Parse parameters
        parseParameters();

        // Create ports
        createPorts();

        // Initialize bus signal (will be created in updateDimension)
        this.outputBusSignal = null;

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static BusCreator fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter numberOfInputsParam = createNumberOfInputsFromJSON(paramValues, blockName);
            Parameter elementNamesParam = createElementNamesFromJSON(paramValues, blockName);
            Parameter busOutputNameParam = createBusOutputNameFromJSON(paramValues, blockName);
            Parameter isVirtualParam = createIsVirtualFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);

            BusCreator block = new BusCreator(numberOfInputsParam, elementNamesParam, busOutputNameParam,
                                             isVirtualParam, sampleTime, outDataType,
                                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, numberOfInputsParam, elementNamesParam, busOutputNameParam,
                                      isVirtualParam, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create BusCreator block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static BusCreator create(String name, String path, int numberOfInputs, String elementNames, NCSLabModel model) {
        return create(name, path, numberOfInputs, elementNames, "Bus", true, -1.0, "Bus: <object name>", model);
    }

    /**
     * Create a BusCreator block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param numberOfInputs Number of input ports
     * @param elementNames Comma-separated element names
     * @param busOutputName Output bus name
     * @param isVirtual Virtual bus flag
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return BusCreator block instance
     */
    public static BusCreator create(String name, String path, int numberOfInputs, String elementNames,
                                   String busOutputName, boolean isVirtual, double sampleTime,
                                   String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        BusCreatorDto dto = BusCreatorDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .numberOfInputs(com.ncslab.dto.common.TypedParameter.of(numberOfInputs))
            .elementNames(com.ncslab.dto.common.TypedParameter.of(elementNames))
            .busOutputName(com.ncslab.dto.common.TypedParameter.of(busOutputName))
            .isVirtual(com.ncslab.dto.common.TypedParameter.of(isVirtual))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid BusCreator parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new BusCreator(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createNumberOfInputsFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("NumberOfInputs", "2");
        return new Parameter(null, 1, "NumberOfInputs", value);
    }

    private static Parameter createElementNamesFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("ElementNames", "signal1,signal2");
        return new Parameter(null, 2, "ElementNames", value);
    }

    private static Parameter createBusOutputNameFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("BusOutputName", "Bus");
        return new Parameter(null, 3, "BusOutputName", value);
    }

    private static Parameter createIsVirtualFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("IsVirtual", "true");
        return new Parameter(null, 4, "IsVirtual", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 5, "SampleTime", value);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("OutDataTypeStr", "Bus: <object name>");
        return new Parameter(null, 6, "OutDataTypeStr", value);
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

    private static void setParameterBlockReference(BusCreator block, Parameter... parameters) {
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
        identity.put("blockType", "BusCreator");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Parameter Parsing ===
    private void parseParameters() {
        // Parse number of inputs
        this.numberOfInputs = Integer.parseInt(numberOfInputsParam.getInitString());

        // Parse element names
        String namesStr = elementNamesParam.getInitString();
        this.elementNames = namesStr.split(",");
        for (int i = 0; i < elementNames.length; i++) {
            elementNames[i] = elementNames[i].trim();
        }

        // Parse bus output name
        this.busOutputName = busOutputNameParam.getInitString().trim();

        // Parse virtual flag
        String virtualStr = isVirtualParam.getInitString().toLowerCase();
        this.isVirtual = "true".equals(virtualStr) || "on".equals(virtualStr) || "1".equals(virtualStr);

        // Validate element names count matches number of inputs
        if (elementNames.length != numberOfInputs) {
            throw new BlockCreationException(
                String.format("BusCreator: Element names count (%d) must match number of inputs (%d)",
                             elementNames.length, numberOfInputs));
        }
    }

    // === Port Creation ===
    private void createPorts() {
        // Create input ports based on number of inputs
        for (int i = 0; i < numberOfInputs; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        // Create single output port (bus signal)
        outputPortList.add(new OutputPort(this, 1, feedThrough));
    }

    // === Code Generation ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add bus-specific context
        context.put("numberOfInputs", numberOfInputs);
        context.put("elementNames", elementNames);
        context.put("busOutputName", busOutputName);
        context.put("isVirtual", isVirtual);

        String codeStr = TemplateManager.renderTemplate("m/route/BusCreator/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add bus-specific context
        context.put("numberOfInputs", numberOfInputs);
        context.put("elementNames", elementNames);
        context.put("busOutputName", busOutputName);
        context.put("isVirtual", isVirtual);

        String codeStr = TemplateManager.renderTemplate("m/route/BusCreator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add bus-specific context
        context.put("numberOfInputs", numberOfInputs);
        context.put("elementNames", elementNames);
        context.put("busOutputName", busOutputName);
        context.put("isVirtual", isVirtual);

        // Add input data types for struct generation
        List<CDataType> inputCDataTypes = new ArrayList<>();
        List<Integer> inputHeights = new ArrayList<>();
        List<Integer> inputWidths = new ArrayList<>();

        for (InputPort inputPort : inputPortList) {
            if (inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                inputCDataTypes.add(signal.getCDataType());
                inputHeights.add(signal.getHeight());
                inputWidths.add(signal.getWidth());
            } else {
                // Default for unconnected ports
                inputCDataTypes.add(CDataType.DOUBLE);
                inputHeights.add(1);
                inputWidths.add(1);
            }
        }

        context.put("inputCDataTypes", inputCDataTypes);
        context.put("inputHeights", inputHeights);
        context.put("inputWidths", inputWidths);

        String codeStr = TemplateManager.renderTemplate("c/route/BusCreator/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add bus-specific context
        context.put("numberOfInputs", numberOfInputs);
        context.put("elementNames", elementNames);
        context.put("busOutputName", busOutputName);
        context.put("isVirtual", isVirtual);

        // Add input signal names
        List<String> inputSignalNames = new ArrayList<>();
        for (int i = 0; i < inputPortList.size(); i++) {
            inputSignalNames.add(getInputPortVariable(i));
        }
        context.put("inputSignalNames", inputSignalNames);

        // Add output signal name
        context.put("outputSignalName", getOutputPortVariable(0));

        String codeStr = TemplateManager.renderTemplate("c/route/BusCreator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/route/BusCreator/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("c/route/BusCreator/update.vm", context);
        code.addUpdateCode(codeStr);
    }

    // === Dimension Propagation ===
    @Override
    public void updateDimension() throws MatDimException {
        // Validate that all inputs are connected
        for (int i = 0; i < inputPortList.size(); i++) {
            InputPort inputPort = inputPortList.get(i);
            if (inputPort.getLinkedLine() == null || inputPort.getLinkedLine().getLinkedOutputPort() == null) {
                throw new MatDimException(String.format("BusCreator '%s': Input port %d (%s) is not connected",
                                                       blockName, i + 1, elementNames[i]));
            }
        }

        // Create BusSignal instance
        OutputPort outputPort = outputPortList.get(0);

        // Lookup bus definition from registry (if available)
        BusDefinition busDefinition = BusDefinitionRegistry.getInstance().lookup(busOutputName);

        // Create bus signal
        this.outputBusSignal = new BusSignal(this, getBlockId(), 1, busOutputName, busDefinition, isVirtual);

        // Add all input signals as bus elements
        for (int i = 0; i < inputPortList.size(); i++) {
            InputPort inputPort = inputPortList.get(i);
            OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            String elementName = elementNames[i];

            // Add element to bus
            outputBusSignal.addElement(elementName, inputSignal);
        }

        // Assign bus signal to output port
        outputPort.setOutputSignalC(outputBusSignal);

        // Set dimensions
        int totalWidth = outputBusSignal.getTotalWidth();
        outputPort.setWidth(totalWidth);
        outputPort.setHeight(1);

        System.out.println("BusCreator updateDimension: " + getBlockName() + " - Created BusSignal with " +
                          numberOfInputs + " elements, totalWidth=" + totalWidth + ", isVirtual=" + isVirtual);
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Dimension checking performed in updateDimension
    }

    // === Runtime Execution ===
    @Override
    public void calculateOutput(double t) {
        // Bus signal already contains references to input signals
        // No need to copy data - BusSignal provides direct access to elements

        // For non-virtual buses, we may need to update the bus signal data
        // but for virtual buses, the element signals are already linked correctly

        if (!isVirtual) {
            // For non-virtual buses, ensure all element data is current
            // (This may be needed for C struct generation)
            for (int i = 0; i < inputPortList.size(); i++) {
                InputPort inputPort = inputPortList.get(i);
                String elementName = elementNames[i];

                // Get current input data
                Data inputData = inputPort.getData();

                // Update bus element (implementation depends on BusSignal's internal structure)
                // For now, the bus signal references are sufficient
            }
        }

        // Output port already has the bus signal assigned, no further action needed
    }

    // === Getters for Template Context ===
    public int getNumberOfInputs() {
        return numberOfInputs;
    }

    public String[] getElementNames() {
        return elementNames;
    }

    public String getBusOutputName() {
        return busOutputName;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    /**
     * Gets the output bus signal.
     * @return Output bus signal containing all elements
     */
    public BusSignal getOutputBusSignal() {
        return outputBusSignal;
    }
}
