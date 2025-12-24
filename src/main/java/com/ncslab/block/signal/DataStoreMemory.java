package com.ncslab.block.signal;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.signal.DataStoreMemoryDto;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.Data;
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
 * Data Store Memory block - defines and initializes a global data store.
 *
 * <p>This block acts as a storage location for shared data that can be accessed
 * by Data Store Read and Data Store Write blocks throughout the model without
 * signal line connections. This is useful for:</p>
 * <ul>
 *   <li>Sharing data between subsystems without connections</li>
 *   <li>Implementing global state variables</li>
 *   <li>Breaking algebraic loops</li>
 *   <li>Reducing visual complexity in models</li>
 * </ul>
 *
 * <p><b>Important:</b> This block has no input/output ports - it only defines
 * the storage location and initial value.</p>
 *
 * <h3>SIMULINK Parameters:</h3>
 * <ul>
 *   <li><b>DataStoreName</b>: Unique identifier (e.g., "GlobalCounter")</li>
 *   <li><b>InitialValue</b>: Initial value or matrix</li>
 *   <li><b>Dimensions</b>: Signal dimensions [rows, cols]</li>
 *   <li><b>DataType</b>: Data type specification</li>
 *   <li><b>SampleTime</b>: Sample time (typically -1 for inherited)</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
public class DataStoreMemory extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter dataStoreName;
    private final Parameter initialValue;
    private final Parameter dimensions;
    private final Parameter dataType;
    private final Parameter sampleTime;

    // === Data Storage ===
    @Getter
    private Data storedData;

    // === Static Parameter Definitions ===

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("DataStoreName", "A");
        PARAMETER_DEFAULTS.put("InitialValue", "0");
        PARAMETER_DEFAULTS.put("Dimensions", "[1, 1]");
        PARAMETER_DEFAULTS.put("DataType", "double");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // DataStoreMemory has no ports - it's a definition block only
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS = new ArrayList<>();
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS = new ArrayList<>();

    // === Private Constructor with Typed Parameters ===
    private DataStoreMemory(Parameter dataStoreName, Parameter initialValue, Parameter dimensions,
                           Parameter dataType, Parameter sampleTime,
                           String blockName, String blockPath,
                           String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(dataStoreName);

        // Assign parameters
        this.dataStoreName = Objects.requireNonNull(dataStoreName, "Data store name parameter cannot be null");
        this.initialValue = Objects.requireNonNull(initialValue, "Initial value parameter cannot be null");
        this.dimensions = Objects.requireNonNull(dimensions, "Dimensions parameter cannot be null");
        this.dataType = Objects.requireNonNull(dataType, "Data type parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.dataStoreName);
        parameterList.add(this.initialValue);
        parameterList.add(this.dimensions);
        parameterList.add(this.dataType);
        parameterList.add(this.sampleTime);

        initializeDataStore(model);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DataStoreMemory(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.dataStoreName = getParameterByName("DataStoreName");
        this.initialValue = getParameterByName("InitialValue");
        this.dimensions = getParameterByName("Dimensions");
        this.dataType = getParameterByName("DataType");
        this.sampleTime = getParameterByName("SampleTime");

        initializeDataStore(model);
    }

    /**
     * DTO Constructor - Creates DataStoreMemory block directly from DataStoreMemoryDto DTO.
     */
    public DataStoreMemory(DataStoreMemoryDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO with defaults
        String dataStoreNameValue = dto.getDataStoreNameValue();
        String initialValueValue = dto.getInitialValueAsString();
        String dimensionsValue = dto.getDimensionsValue();
        String dataTypeValue = dto.getDataTypeValue();
        String sampleTimeValue = dto.getSampleTimeValue() != null ?
                                  dto.getSampleTimeValue().toString() : "-1";

        // Validate data store name
        if (dataStoreNameValue == null || dataStoreNameValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Data store name cannot be empty");
        }
        if (!isValidIdentifier(dataStoreNameValue)) {
            throw new IllegalArgumentException("Data store name must be a valid identifier: " + dataStoreNameValue);
        }

        // Initialize parameters
        this.dataStoreName = getParameterByName("DataStoreName");
        this.initialValue = getParameterByName("InitialValue");
        this.dimensions = getParameterByName("Dimensions");
        this.dataType = getParameterByName("DataType");
        this.sampleTime = getParameterByName("SampleTime");

        initializeDataStore(model);

        System.out.println("DTO-SPECIFIC: DataStoreMemory block created successfully from DataStoreMemoryDto - " +
                          dto.getBlockName() + " (store: " + dataStoreNameValue + ")");
    }

    /**
     * Factory method to create DataStoreMemory block from DataStoreMemoryDto.
     *
     * @param dto The DataStoreMemoryDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New DataStoreMemory block instance
     * @throws BlockCreationException if block creation fails
     */
    public static DataStoreMemory createFromDto(DataStoreMemoryDto dto, NCSLabModel model) throws BlockCreationException {
        return new DataStoreMemory(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static DataStoreMemory fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter dataStoreName = createDataStoreNameFromJSON(paramValues, blockName);
            Parameter initialValue = createInitialValueFromJSON(paramValues, blockName);
            Parameter dimensions = createDimensionsFromJSON(paramValues, blockName);
            Parameter dataType = createDataTypeFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);

            DataStoreMemory block = new DataStoreMemory(dataStoreName, initialValue, dimensions,
                                                       dataType, sampleTime,
                                                       blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, dataStoreName, initialValue, dimensions, dataType, sampleTime);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DataStoreMemory block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a DataStoreMemory block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param dataStoreName Name of the data store
     * @param model Parent model
     * @return Configured DataStoreMemory block instance
     */
    public static DataStoreMemory create(String name, String path, String dataStoreName, NCSLabModel model) {
        return create(name, path, dataStoreName, 0.0, "[1, 1]", "double", -1.0, model);
    }

    /**
     * Create a DataStoreMemory block with full parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param dataStoreName Unique data store identifier
     * @param initialValue Initial value for the data store
     * @param dimensions Signal dimensions (e.g., "[1, 1]")
     * @param dataType Data type specification (e.g., "double")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited)
     * @param model Parent model
     * @return Configured DataStoreMemory block instance
     */
    public static DataStoreMemory create(String name, String path, String dataStoreName,
                                        double initialValue, String dimensions, String dataType,
                                        double sampleTime, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DataStoreMemoryDto dto = DataStoreMemoryDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .dataStoreName(com.ncslab.dto.common.TypedParameter.of(dataStoreName))
            .initialValue(com.ncslab.dto.common.TypedParameter.of(initialValue))
            .dimensions(com.ncslab.dto.common.TypedParameter.of(dimensions))
            .dataType(com.ncslab.dto.common.TypedParameter.of(dataType))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid DataStoreMemory parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new DataStoreMemory(dto, model);
    }

    // === Data Store Initialization ===
    private void initializeDataStore(NCSLabModel model) {
        // Parse initial value
        String initialValueStr = initialValue.getInitString();
        this.storedData = new Data(initialValueStr);

        // Register in global registry
        String modelUUID = model.getModelUUID();
        String storeName = dataStoreName.getInitString();

        DataStoreRegistry.registerDataStore(modelUUID, storeName, this.storedData);

        System.out.println("Initialized data store '" + storeName + "' in model '" +
                          modelUUID + "' with value: " + initialValueStr);
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

    private static Parameter createInitialValueFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("InitialValue", "0");
        return new Parameter(null, 2, "InitialValue", value);
    }

    private static Parameter createDimensionsFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("Dimensions", "[1, 1]");
        return new Parameter(null, 3, "Dimensions", value);
    }

    private static Parameter createDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("DataType", "double");
        return new Parameter(null, 4, "DataType", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 5, "SampleTime", value);
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

    private static void setParameterBlockReference(DataStoreMemory block, Parameter... parameters) {
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
        identity.put("blockType", "DataStoreMemory");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Code Generation Methods ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("storedData", storedData);

        String initCode = TemplateManager.renderTemplate("c/signal/DataStoreMemory/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        // DataStoreMemory has no output code (definition block only)
    }

    public void updateDimension() throws MatDimException {
        // No ports to update
    }

    public void checkDimension() throws MatDimException {
        // No ports to check
    }

    /**
     * DataStoreMemory block has no calculateOutput - it only defines storage.
     * The actual data is accessed by DataStoreRead and DataStoreWrite blocks.
     */
    @Override
    public void calculateOutput(double t) {
        // No calculation needed - this is a definition block
    }

    /**
     * Gets the name of this data store.
     *
     * @return Data store name
     */
    public String getDataStoreNameValue() {
        return dataStoreName.getInitString();
    }
}
