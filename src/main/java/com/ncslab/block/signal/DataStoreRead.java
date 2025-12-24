package com.ncslab.block.signal;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.signal.DataStoreReadDto;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
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
 * Data Store Read block - reads value from a named global data store.
 *
 * <p>This block reads the current value from a data store defined by a
 * Data Store Memory block. This allows access to shared data throughout
 * the model without signal line connections.</p>
 *
 * <p>The block has one output port that provides the current value of
 * the named data store at each time step.</p>
 *
 * <h3>Block Characteristics:</h3>
 * <ul>
 *   <li>One output port (no input ports)</li>
 *   <li>Reads from named data store</li>
 *   <li>Throws error if data store not found</li>
 *   <li>Direct feedthrough (output depends on current data store value)</li>
 * </ul>
 *
 * <h3>SIMULINK Parameters:</h3>
 * <ul>
 *   <li><b>DataStoreName</b>: Name of data store to read from</li>
 *   <li><b>SampleTime</b>: Sample time (-1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
public class DataStoreRead extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter dataStoreName;
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // === Static Parameter Definitions ===

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("DataStoreName", "A");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS = new ArrayList<>();
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");

        // Output port defaults (inherits dimensions from data store)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);  // Direct feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private DataStoreRead(Parameter dataStoreName, Parameter sampleTime, Parameter outDataType,
                         String blockName, String blockPath,
                         String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(dataStoreName);

        // Assign parameters
        this.dataStoreName = Objects.requireNonNull(dataStoreName, "Data store name parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.dataStoreName);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DataStoreRead(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.dataStoreName = getParameterByName("DataStoreName");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        initializePorts();
    }

    /**
     * DTO Constructor - Creates DataStoreRead block directly from DataStoreReadDto DTO.
     */
    public DataStoreRead(DataStoreReadDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO with defaults
        String dataStoreNameValue = dto.getDataStoreNameValue();
        String sampleTimeValue = dto.getSampleTimeValue() != null ?
                                  dto.getSampleTimeValue().toString() : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue();

        // Validate data store name
        if (dataStoreNameValue == null || dataStoreNameValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Data store name cannot be empty");
        }
        if (!isValidIdentifier(dataStoreNameValue)) {
            throw new IllegalArgumentException("Data store name must be a valid identifier: " + dataStoreNameValue);
        }

        // Initialize parameters
        this.dataStoreName = getParameterByName("DataStoreName");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        initializePorts();

        System.out.println("DTO-SPECIFIC: DataStoreRead block created successfully from DataStoreReadDto - " +
                          dto.getBlockName() + " (store: " + dataStoreNameValue + ")");
    }

    /**
     * Factory method to create DataStoreRead block from DataStoreReadDto.
     *
     * @param dto The DataStoreReadDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New DataStoreRead block instance
     * @throws BlockCreationException if block creation fails
     */
    public static DataStoreRead createFromDto(DataStoreReadDto dto, NCSLabModel model) throws BlockCreationException {
        return new DataStoreRead(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static DataStoreRead fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter dataStoreName = createDataStoreNameFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);

            DataStoreRead block = new DataStoreRead(dataStoreName, sampleTime, outDataType,
                                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, dataStoreName, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DataStoreRead block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a DataStoreRead block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param dataStoreName Name of the data store to read from
     * @param model Parent model
     * @return Configured DataStoreRead block instance
     */
    public static DataStoreRead create(String name, String path, String dataStoreName, NCSLabModel model) {
        return create(name, path, dataStoreName, -1.0, "Inherit", model);
    }

    /**
     * Create a DataStoreRead block with full parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param dataStoreName Data store name to read from
     * @param sampleTime Sample time (0 for continuous, -1 for inherited)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return Configured DataStoreRead block instance
     */
    public static DataStoreRead create(String name, String path, String dataStoreName,
                                      double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DataStoreReadDto dto = DataStoreReadDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .dataStoreName(com.ncslab.dto.common.TypedParameter.of(dataStoreName))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid DataStoreRead parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new DataStoreRead(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter dataStoreName) {
        String nameValue = dataStoreName.getInitString();
        if (nameValue == null || nameValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Data store name cannot be empty");
        }
        if (!isValidIdentifier(nameValue)) {
            throw new IllegalArgumentException("Data store name must be a valid identifier: " + nameValue);
        }
    }

    private static boolean isValidIdentifier(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return name.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createDataStoreNameFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("DataStoreName", "A");
        return new Parameter(null, 1, "DataStoreName", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", value);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("OutDataTypeStr", "Inherit");
        return new Parameter(null, 3, "OutDataTypeStr", value);
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

    private static void setParameterBlockReference(DataStoreRead block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null
            }
        }
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "DataStoreRead");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        outputPortList.add(new OutputPort(this, 1, true));  // Feedthrough
    }

    // === Code Generation Methods ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = "";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/signal/DataStoreRead/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // Read data store to determine dimensions
        String modelUUID = model.getModelUUID();
        String storeName = dataStoreName.getInitString();

        if (DataStoreRegistry.dataStoreExists(modelUUID, storeName)) {
            Data storeData = DataStoreRegistry.readDataStore(modelUUID, storeName);
            OutputPort out = outputPortList.get(0);

            out.setHeight(storeData.getHeight());
            out.setWidth(storeData.getWidth());
            out.getOutputSignalC().setHeight(storeData.getHeight());
            out.getOutputSignalC().setWidth(storeData.getWidth());
            out.getOutputSignalC().setDataType(storeData.getDataType());
        } else {
            // Default to scalar if data store not found yet (will error during calculateOutput)
            OutputPort out = outputPortList.get(0);
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        }
    }

    public void checkDimension() throws MatDimException {
        // No dimension checking needed - output dimensions match data store
    }

    /**
     * Reads the current value from the named data store and sets it as output.
     *
     * @param t Current simulation time
     * @throws RuntimeException if data store is not found
     */
    @Override
    public void calculateOutput(double t) {
        String modelUUID = model.getModelUUID();
        String storeName = dataStoreName.getInitString();

        // Check if data store exists
        if (!DataStoreRegistry.dataStoreExists(modelUUID, storeName)) {
            throw new RuntimeException(
                String.format("Data store '%s' not found in model '%s'. " +
                            "Ensure a DataStoreMemory block with this name exists.",
                            storeName, modelUUID)
            );
        }

        // Read current value from data store
        Data storeData = DataStoreRegistry.readDataStore(modelUUID, storeName);

        // Set output to data store value
        outputPortList.get(0).setData(storeData);
    }

    /**
     * Gets the name of the data store this block reads from.
     *
     * @return Data store name
     */
    public String getDataStoreNameValue() {
        return dataStoreName.getInitString();
    }
}
