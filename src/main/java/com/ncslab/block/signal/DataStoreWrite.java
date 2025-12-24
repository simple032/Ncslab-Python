package com.ncslab.block.signal;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.signal.DataStoreWriteDto;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.BlockExecutionException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Store Write block - writes value to a named global data store.
 *
 * <p>This block writes the input value to a data store defined by a
 * Data Store Memory block. This allows updating shared data throughout
 * the model without signal line connections.</p>
 *
 * <p>The block has one input port and no output ports (sink-like behavior).
 * It updates the named data store with the input value at each time step.</p>
 *
 * <h3>Block Characteristics:</h3>
 * <ul>
 *   <li>One input port (no output ports)</li>
 *   <li>Writes to named data store</li>
 *   <li>Throws error if data store not found</li>
 *   <li>Sink-like behavior (no output propagation)</li>
 * </ul>
 *
 * <h3>SIMULINK Parameters:</h3>
 * <ul>
 *   <li><b>DataStoreName</b>: Name of data store to write to</li>
 *   <li><b>SampleTime</b>: Sample time (-1 for inherited)</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class DataStoreWrite extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter dataStoreName;
    private final Parameter sampleTime;

    // === Static Parameter Definitions ===

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("DataStoreName", "A");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
    }

    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

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
    private DataStoreWrite(Parameter dataStoreName, Parameter sampleTime,
                          String blockName, String blockPath,
                          String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(dataStoreName);

        // Assign parameters
        this.dataStoreName = Objects.requireNonNull(dataStoreName, "Data store name parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.dataStoreName);
        parameterList.add(this.sampleTime);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DataStoreWrite(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.dataStoreName = getParameterByName("DataStoreName");
        this.sampleTime = getParameterByName("SampleTime");

        initializePorts();
    }

    /**
     * DTO Constructor - Creates DataStoreWrite block directly from DataStoreWriteDto DTO.
     */
    public DataStoreWrite(DataStoreWriteDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO with defaults
        String dataStoreNameValue = dto.getDataStoreNameValue();
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
        this.sampleTime = getParameterByName("SampleTime");

        initializePorts();

        log.info("DTO-SPECIFIC: DataStoreWrite block created successfully from DataStoreWriteDto - {} (store: {})",
                 dto.getBlockName(), dataStoreNameValue);
    }

    /**
     * Factory method to create DataStoreWrite block from DataStoreWriteDto.
     *
     * @param dto The DataStoreWriteDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New DataStoreWrite block instance
     * @throws BlockCreationException if block creation fails
     */
    public static DataStoreWrite createFromDto(DataStoreWriteDto dto, NCSLabModel model) throws BlockCreationException {
        return new DataStoreWrite(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static DataStoreWrite fromJSON(JSONObject blockJSON, NCSLabModel model) {
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

            DataStoreWrite block = new DataStoreWrite(dataStoreName, sampleTime,
                                                     blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, dataStoreName, sampleTime);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DataStoreWrite block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a DataStoreWrite block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param dataStoreName Name of the data store to write to
     * @param model Parent model
     * @return Configured DataStoreWrite block instance
     */
    public static DataStoreWrite create(String name, String path, String dataStoreName, NCSLabModel model) {
        return create(name, path, dataStoreName, -1.0, model);
    }

    /**
     * Create a DataStoreWrite block with full parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param dataStoreName Data store name to write to
     * @param sampleTime Sample time (0 for continuous, -1 for inherited)
     * @param model Parent model
     * @return Configured DataStoreWrite block instance
     */
    public static DataStoreWrite create(String name, String path, String dataStoreName,
                                       double sampleTime, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DataStoreWriteDto dto = DataStoreWriteDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .dataStoreName(com.ncslab.dto.common.TypedParameter.of(dataStoreName))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid DataStoreWrite parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new DataStoreWrite(dto, model);
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

    private static void setParameterBlockReference(DataStoreWrite block, Parameter... parameters) {
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
        identity.put("blockType", "DataStoreWrite");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
    }

    // === Code Generation Methods ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        // No initialization code needed for write operations
        String initCode = "";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/signal/DataStoreWrite/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // No output ports to update (sink block)
        // Input dimensions are inherited from the connected signal
    }

    public void checkDimension() throws MatDimException {
        // Verify data store exists and dimensions are compatible
        String modelUUID = model.getModelUUID();
        String storeName = dataStoreName.getInitString();

        if (!DataStoreRegistry.dataStoreExists(modelUUID, storeName)) {
            log.warn("DataStoreWrite '{}': Data store '{}' not found during dimension check. " +
                    "Will be validated during execution.", blockName, storeName);
        }
    }

    /**
     * Writes the input value to the named data store.
     *
     * @param t Current simulation time
     * @throws BlockExecutionException if data store is not found or write fails
     */
    @Override
    public void calculateOutput(double t) {
        // Verify we have an input port
        if (inputPortList.isEmpty()) {
            log.warn("DataStoreWrite '{}': No input ports configured", blockName);
            return;
        }

        // Get input port
        InputPort inputPort = inputPortList.get(0);
        if (inputPort.getLinkedLine() == null || inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            log.warn("DataStoreWrite '{}': No input signal connected", blockName);
            return;
        }

        // Get input signal
        OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (inputSignal == null) {
            log.warn("DataStoreWrite '{}': Input signal is null", blockName);
            return;
        }

        Data inputData = inputSignal.getData();
        if (inputData == null) {
            log.warn("DataStoreWrite '{}': Input data is null, skipping write", blockName);
            return;
        }

        // Write to data store
        String modelUUID = model.getModelUUID();
        String storeName = dataStoreName.getInitString();

        try {
            DataStoreRegistry.writeDataStore(modelUUID, storeName, inputData);
            log.trace("DataStoreWrite '{}': Wrote to data store '{}': {}",
                     blockName, storeName, inputData.getDataString());

        } catch (IllegalArgumentException e) {
            String errorMsg = String.format(
                "DataStoreWrite '%s': Failed to write to data store '%s'. " +
                "Ensure a DataStoreMemory block with this name exists. Error: %s",
                blockName, storeName, e.getMessage()
            );
            log.error(errorMsg);
            throw new BlockExecutionException(errorMsg, blockName, t);
        } catch (Exception e) {
            String errorMsg = String.format(
                "DataStoreWrite '%s': Unexpected error writing to data store '%s': %s",
                blockName, storeName, e.getMessage()
            );
            log.error(errorMsg, e);
            throw new BlockExecutionException(errorMsg, blockName, t);
        }
    }

    /**
     * Gets the name of the data store this block writes to.
     *
     * @return Data store name
     */
    public String getDataStoreNameValue() {
        return dataStoreName.getInitString();
    }
}
