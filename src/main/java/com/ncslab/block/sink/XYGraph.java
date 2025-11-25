package com.ncslab.block.sink;

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

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.terminal.ScopeStruct;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * XY Graph block with SIMULINK-compatible parameters.
 *
 * Provides phase-plane plotting capabilities by plotting Y values against X values.
 * Used for visualizing system trajectories, limit cycles, and phase portraits.
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for data collection (-1 for inherited, 0 for continuous)
 * - XMin: Minimum X-axis value for plotting
 * - XMax: Maximum X-axis value for plotting
 * - YMin: Minimum Y-axis value for plotting
 * - YMax: Maximum Y-axis value for plotting
 * - SaveName: Variable name to save data
 * - BufferSize: Size of data buffer
 *
 * @author NCSLab Team
 * @version 2025
 */
public class XYGraph extends SinkBlock {

    // === Scope data structures for X and Y data ===
    ScopeStruct xScopeStruct;
    ScopeStruct yScopeStruct;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter sampleTime;
    private final Parameter xMin;
    private final Parameter xMax;
    private final Parameter yMin;
    private final Parameter yMax;
    private final Parameter saveName;
    private final Parameter bufferSize;

    // === Static Parameter Definitions ===
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // X and Y input ports
        inputNames.add("x");
        inputNames.add("y");

        // Input port defaults (X and Y inputs)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> inputX = new HashMap<>();
        inputX.put("name", "x");
        inputX.put("width", 1);
        inputX.put("height", 1);
        inputX.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(inputX);

        Map<String, Object> inputY = new HashMap<>();
        inputY.put("name", "y");
        inputY.put("width", 1);
        inputY.put("height", 1);
        inputY.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(inputY);

        // Output port defaults (sink block has no outputs)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
    }

    // === Parameter Defaults ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("XMin", "-10");
        PARAMETER_DEFAULTS.put("XMax", "10");
        PARAMETER_DEFAULTS.put("YMin", "-10");
        PARAMETER_DEFAULTS.put("YMax", "10");
        PARAMETER_DEFAULTS.put("SaveName", "XYGraphData");
        PARAMETER_DEFAULTS.put("BufferSize", "100000");
    }

    // === Private Constructor with Typed Parameters ===
    private XYGraph(Parameter sampleTime, Parameter xMin, Parameter xMax,
                   Parameter yMin, Parameter yMax, Parameter saveName, Parameter bufferSize,
                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.xMin = Objects.requireNonNull(xMin, "XMin parameter cannot be null");
        this.xMax = Objects.requireNonNull(xMax, "XMax parameter cannot be null");
        this.yMin = Objects.requireNonNull(yMin, "YMin parameter cannot be null");
        this.yMax = Objects.requireNonNull(yMax, "YMax parameter cannot be null");
        this.saveName = Objects.requireNonNull(saveName, "SaveName parameter cannot be null");
        this.bufferSize = Objects.requireNonNull(bufferSize, "BufferSize parameter cannot be null");

        // Add parameters to parameter list
        parameterList.add(this.sampleTime);
        parameterList.add(this.xMin);
        parameterList.add(this.xMax);
        parameterList.add(this.yMin);
        parameterList.add(this.yMax);
        parameterList.add(this.saveName);
        parameterList.add(this.bufferSize);

        // Initialize ports and scope structures
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public XYGraph(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create parameters from JSON with defaults
        this.sampleTime = getParameterByName("SampleTime");
        this.xMin = getParameterByName("XMin");
        this.xMax = getParameterByName("XMax");
        this.yMin = getParameterByName("YMin");
        this.yMax = getParameterByName("YMax");
        this.saveName = getParameterByName("SaveName");
        this.bufferSize = getParameterByName("BufferSize");

        // Initialize ports and scope structures
        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates XYGraph block directly from XYGraphDto DTO
     */
    public XYGraph(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.sampleTime = getParameterByName("SampleTime");
        this.xMin = getParameterByName("XMin");
        this.xMax = getParameterByName("XMax");
        this.yMin = getParameterByName("YMin");
        this.yMax = getParameterByName("YMax");
        this.saveName = getParameterByName("SaveName");
        this.bufferSize = getParameterByName("BufferSize");

        // Initialize ports and scope structures
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create XYGraph from BlockDto.
     *
     * @param dto The BlockDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New XYGraph instance
     * @throws BlockCreationException if block creation fails
     */
    public static XYGraph createFromDto(BlockDto dto, NCSLabModel model) throws BlockCreationException {
        return new XYGraph(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static XYGraph fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter xMin = createXMinFromJSON(paramValues, blockName);
            Parameter xMax = createXMaxFromJSON(paramValues, blockName);
            Parameter yMin = createYMinFromJSON(paramValues, blockName);
            Parameter yMax = createYMaxFromJSON(paramValues, blockName);
            Parameter saveName = createSaveNameFromJSON(paramValues, blockName);
            Parameter bufferSize = createBufferSizeFromJSON(paramValues, blockName);

            XYGraph block = new XYGraph(sampleTime, xMin, xMax, yMin, yMax, saveName, bufferSize,
                                       blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, sampleTime, xMin, xMax, yMin, yMax, saveName, bufferSize);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create XYGraph block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static XYGraph create(String name, String path, NCSLabModel model) {
        return create(name, path, "-1", "-10", "10", "-10", "10", "XYGraphData", "100000", model);
    }

    /**
     * Create an XYGraph block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param sampleTime Sample time (-1 for inherited, 0 for continuous, >0 for discrete)
     * @param xMin Minimum X-axis value for plot display
     * @param xMax Maximum X-axis value for plot display
     * @param yMin Minimum Y-axis value for plot display
     * @param yMax Maximum Y-axis value for plot display
     * @param saveName Variable name to save XY graph data
     * @param bufferSize Data buffer size (positive integer, typically 100000)
     * @param model Parent model
     * @return XYGraph block instance
     */
    public static XYGraph create(String name, String path, String sampleTime,
                                String xMin, String xMax, String yMin, String yMax,
                                String saveName, String bufferSize, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        com.ncslab.dto.block.specialized.sink.XYGraphDto dto = com.ncslab.dto.block.specialized.sink.XYGraphDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .xMin(com.ncslab.dto.common.TypedParameter.of(xMin))
            .xMax(com.ncslab.dto.common.TypedParameter.of(xMax))
            .yMin(com.ncslab.dto.common.TypedParameter.of(yMin))
            .yMax(com.ncslab.dto.common.TypedParameter.of(yMax))
            .saveName(com.ncslab.dto.common.TypedParameter.of(saveName))
            .bufferSize(com.ncslab.dto.common.TypedParameter.of(bufferSize))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid XYGraph parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new XYGraph(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }

    private static Parameter createXMinFromJSON(JSONObject paramValues, String blockName) {
        String xMinValue = paramValues.optString("XMin", "-10");
        return new Parameter(null, 2, "XMin", xMinValue);
    }

    private static Parameter createXMaxFromJSON(JSONObject paramValues, String blockName) {
        String xMaxValue = paramValues.optString("XMax", "10");
        return new Parameter(null, 3, "XMax", xMaxValue);
    }

    private static Parameter createYMinFromJSON(JSONObject paramValues, String blockName) {
        String yMinValue = paramValues.optString("YMin", "-10");
        return new Parameter(null, 4, "YMin", yMinValue);
    }

    private static Parameter createYMaxFromJSON(JSONObject paramValues, String blockName) {
        String yMaxValue = paramValues.optString("YMax", "10");
        return new Parameter(null, 5, "YMax", yMaxValue);
    }

    private static Parameter createSaveNameFromJSON(JSONObject paramValues, String blockName) {
        String saveNameValue = paramValues.optString("SaveName", "XYGraphData");
        return new Parameter(null, 6, "SaveName", saveNameValue);
    }

    private static Parameter createBufferSizeFromJSON(JSONObject paramValues, String blockName) {
        String bufferSizeValue = paramValues.optString("BufferSize", "100000");
        return new Parameter(null, 7, "BufferSize", bufferSizeValue);
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

    private static void setParameterBlockReference(XYGraph block, Parameter... parameters) {
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
        identity.put("blockType", "XYGraph");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Two input ports: X and Y
        inputPortList.add(new InputPort(this, 1)); // X input
        inputPortList.add(new InputPort(this, 2)); // Y input

        // Create scope structures for X and Y data
        xScopeStruct = new ScopeStruct(this, 1, this.blockName + "_X");
        yScopeStruct = new ScopeStruct(this, 2, this.blockName + "_Y");
    }

    // === Code Generation Methods ===
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode = "";
        outputCode += "if storeEnable>0\n";
        outputCode += "    " + getBlockName() + "_X=[" + getBlockName() + "_X"
            + " Block" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId()
            + "_Output" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
            + "];\n";
        outputCode += "    " + getBlockName() + "_Y=[" + getBlockName() + "_Y"
            + " Block" + getInputPortList().get(1).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId()
            + "_Output" + getInputPortList().get(1).getLinkedLine().getLinkedOutputPort().getNumber()
            + "];\n";
        outputCode += "end\n";
        code.addOutputCode(outputCode);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode = "";
        code.addGlobalDefineCode("global " + getBlockName() + "_X " + getBlockName() + "_Y;\n");
        initCode += getBlockName() + "_X=[];\n";
        initCode += getBlockName() + "_Y=[];\n";
        initCode += "XYGraphNum=XYGraphNum+1;\n";
        initCode += "XYGraphList=[XYGraphList; '" + getBlockName() + "'];\n";
        code.addInitCode(initCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        if (model.getModelMode() == ModelMode.Simulation) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            context.put("xScopeStructName", xScopeStruct.getName());
            context.put("yScopeStructName", yScopeStruct.getName());

            String initCode = TemplateManager.renderTemplate("c/sink/XYGraph/init.vm", context);
            code.addInitCode(initCode);
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("xScopeStruct", xScopeStruct);
            context.put("yScopeStruct", yScopeStruct);
            context.put("xScopeStructName", xScopeStruct.getName());
            context.put("yScopeStructName", yScopeStruct.getName());

            String outputCode = TemplateManager.renderTemplate("c/sink/XYGraph/output.vm", context);
            code.addSinkOutputCode(outputCode);
        }
    }

    public void generateTerminateCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("block", this);
            context.put("xScopeStruct", xScopeStruct);
            context.put("yScopeStruct", yScopeStruct);

            String codeStr = TemplateManager.renderTemplate("c/sink/XYGraph/terminate.vm", context);
            code.addTerminateCode(codeStr);
        }
    }

    public void updateDimension() throws MatDimException {
        // XY Graph inputs should be scalar
    }

    public void checkDimension() throws MatDimException {
        // Validate both X and Y inputs are scalar
        OutputSignal signalX = this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signalY = this.inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Set dimensions for X scope struct
        xScopeStruct.setDimension(signalX.getWidth(), signalX.getHeight());
        xScopeStruct.setMaxDataLength(Integer.parseInt(bufferSize.getInitString()));
        model.addTerminal(xScopeStruct);

        // Set dimensions for Y scope struct
        yScopeStruct.setDimension(signalY.getWidth(), signalY.getHeight());
        yScopeStruct.setMaxDataLength(Integer.parseInt(bufferSize.getInitString()));
        model.addTerminal(yScopeStruct);
    }

    @Override
    public void calculateOutput(double t) {
        // XYGraph blocks are sink blocks - they collect data but don't produce output
        // Store X and Y input data in scope structures if in simulation mode
        if (model.getModelMode() == ModelMode.Simulation) {
            if (inputPortList.size() >= 2) {
                Data xData = inputPortList.get(0).getData();
                Data yData = inputPortList.get(1).getData();

                if (xData != null && yData != null) {
                    // Check if we should sample at this time point
                    double sampleTimeValue = sampleTime.getDouble();
                    boolean shouldSample = false;

                    if (sampleTimeValue <= 0) {
                        // Continuous sampling - sample every call
                        shouldSample = true;
                    } else {
                        // Discrete sampling - check if it's time to sample
                        double lastSampleTime = xScopeStruct.getTimeList().isEmpty() ?
                            -1.0 : xScopeStruct.getTimeList().get(xScopeStruct.getTimeList().size() - 1);
                        shouldSample = (t - lastSampleTime) >= sampleTimeValue * 0.99; // Small tolerance
                    }

                    if (shouldSample && (xScopeStruct.getTimeList().isEmpty() ||
                        t > xScopeStruct.getTimeList().get(xScopeStruct.getTimeList().size() - 1))) {
                        xScopeStruct.addTimeSeries(t, xData);
                        yScopeStruct.addTimeSeries(t, yData);
                    }
                }
            }
        }
    }

    @Override
    public void calculateInit() {
        // Initialize XYGraph data structures
        if (model.getModelMode() == ModelMode.Simulation) {
            if (inputPortList.size() >= 2 && inputPortList.get(0).getData() != null && inputPortList.get(1).getData() != null) {
                xScopeStruct.addTimeSeries(0.0, inputPortList.get(0).getData());
                yScopeStruct.addTimeSeries(0.0, inputPortList.get(1).getData());
            }
        }
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        if (model.getModelMode() == ModelMode.Simulation) {
            if (xScopeStruct.getTimeList().isEmpty() || t > xScopeStruct.getTimeList().get(xScopeStruct.getTimeList().size() - 1)) {
                xScopeStruct.addTimeSeries(t, inputPortList.get(0).getData());
                yScopeStruct.addTimeSeries(t, inputPortList.get(1).getData());

                // Debug: Periodic logging (every 1000 data points)
                if (xScopeStruct.getTimeList().size() % 1000 == 0) {
                    System.out.printf("RT Simulation: XYGraph %s collected %d data points (latest t=%.3f)%n",
                        this.getBlockName(), xScopeStruct.getTimeList().size(), t);
                }
            }
        }
    }

    /**
     * Get static input names for block definition
     */
    public static List<String> getInputNames() {
        return inputNames;
    }

    /**
     * Get static output names for block definition
     */
    public static List<String> getOutputNames() {
        return new ArrayList<>(); // Sink block has no outputs
    }
}
