package com.ncslab.block.subsystem;

import com.google.protobuf.ByteString.Output;
import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import com.ncslab.dto.block.specialized.subsystem.OutDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Output port block for subsystems with SIMULINK-compatible parameters.
 *
 * SIMULINK Parameters:
 * - Port: Port number for subsystem output
 * - PortDimensions: Port dimensions specification
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutputDataTypeStr: Output data type specification
 */
public class Out extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter port;
    private final Parameter portDimensions;
    private final Parameter sampleTime;
    private final Parameter outputDataType;

    @Setter
    @Getter
    private Subsystem subsystem;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("No", "1");
        PARAMETER_DEFAULTS.put("PortDimensions", "-1");  // Inherit
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutputDataTypeStr", "Inherit: auto");
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names - Out block receives from inside subsystem
        inputNames.add("in1");
    }
    // === Private Constructor with Typed Parameters ===
    private Out(Parameter port, Parameter portDimensions, Parameter sampleTime, Parameter outputDataType,
                String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.port = Objects.requireNonNull(port, "Port parameter cannot be null");
        this.portDimensions = Objects.requireNonNull(portDimensions, "Port dimensions parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outputDataType = Objects.requireNonNull(outputDataType, "Output data type parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.port);
        parameterList.add(this.portDimensions);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outputDataType);


        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Out(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.port = getParameterByName("No");
        this.portDimensions = getParameterByName("PortDimensions");
        this.sampleTime = getParameterByName("SampleTime");
        this.outputDataType = getParameterByName("OutputDataTypeStr");        
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates Out block directly from BlockDto DTO
     */
    public Out(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Create SIMULINK parameters with defaults
        this.port = getParameterByName("No");
        this.portDimensions = getParameterByName("PortDimensions");
        this.sampleTime = getParameterByName("SampleTime");
        this.outputDataType = getParameterByName("OutputDataTypeStr");
        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Out fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter port = createPortFromJSON(paramValues);
            Parameter portDimensions = createPortDimensionsFromJSON(paramValues);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues);
            Parameter outputDataType = createOutputDataTypeFromJSON(paramValues);

            Out block = new Out(port, portDimensions, sampleTime, outputDataType,
                               blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, port, portDimensions, sampleTime, outputDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Out block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Out create(String name, String path, int portNumber, NCSLabModel model) {
        return create(name, path, portNumber, "-1", -1.0, "Inherit: auto", model);
    }

    /**
     * Create an Out block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param portNumber Port number for subsystem output (>= 1)
     * @param portDimensions Port dimensions specification ("-1" for inherited)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outputDataType Output data type specification
     * @param model Parent model
     * @return Out block instance
     */
    public static Out create(String name, String path, int portNumber, String portDimensions,
                            double sampleTime, String outputDataType, NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        OutDto dto = new OutDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(portNumber),
            com.ncslab.dto.common.TypedParameter.of(portDimensions),
            com.ncslab.dto.common.TypedParameter.of(sampleTime),
            com.ncslab.dto.common.TypedParameter.of(outputDataType)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid Out parameters: " + dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Out(dto, model);
    }

    /**
     * Factory method to create Out block from OutDto.
     *
     * @param dto   OutDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created Out block
     */
    public static Out createFromDto(OutDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("OutDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        // Validate DTO before creating block
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid OutDto: " + dto.getValidationErrors());
        }

        return new Out(dto, model);
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add Out-specific context
        context.put("portNumber", port.getData().getInitValue());
        context.put("subsystem", subsystem);
        
        // Safely handle signal connection chain with null checks
        InputPort inputPort = inputPortList.get(0);
        OutputPort outputPort = this.getSubsystem().getOutputPortList().get(getPortNumber() - 1);
        String outputVar = outputPort.getOutputSignalC().getName();
        String inputVar = "0";
        if (inputPort != null && inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null) {
            inputVar = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        }         
        String outputCode = String.format("%s = %s;\n", outputVar, inputVar);
        code.addOutputCode(outputCode);
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Out block passes dimensions from internal input to subsystem output
        if(this.subsystem == null) return;
        OutputPort out = this.subsystem.getOutputPortList().get(getPortNumber()-1);
        InputPort in = inputPortList.get(0);

        if (in.getLinkedLine() != null) {
            OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        // Validation can be added here if needed
    }

    @Override
    public void calculateOutput(double t) {
        // Pass data from internal blocks to subsystem output
        InputPort in = inputPortList.get(0);
        // Check if subsystem is initialized
        if(this.subsystem == null) return;
        OutputPort out = this.subsystem.getOutputPortList().get(getPortNumber()-1);
        out.setData(in.getData());
    }

    @Override
    public void calculateInit() {
        // Initialize data passing
        InputPort in = inputPortList.get(0);
        // Check if subsystem is initialized
        if(this.subsystem == null) return;
        OutputPort out = this.subsystem.getOutputPortList().get(getPortNumber()-1);
        out.setData(in.getData());
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createPortFromJSON(JSONObject paramValues) {
        String portValue = paramValues.optString("No", paramValues.optString("No", "1"));
        return new Parameter(null, 1, "No", portValue);
    }

    private static Parameter createPortDimensionsFromJSON(JSONObject paramValues) {
        String portDimensionsValue = paramValues.optString("PortDimensions", "-1");
        return new Parameter(null, 2, "PortDimensions", portDimensionsValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutputDataTypeFromJSON(JSONObject paramValues) {
        String outputDataTypeValue = paramValues.optString("OutputDataTypeStr", "Inherit: auto");
        return new Parameter(null, 4, "OutputDataTypeStr", outputDataTypeValue);
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

    private static void setParameterBlockReference(Out block, Parameter... parameters) {
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
        identity.put("blockType", "Out");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Out block receives from inside subsystem
        inputPortList.add(new InputPort(this, 1));
    }

    // === Accessor Methods ===
    public Parameter getPortParameter() {
        return port;
    }

    public int getPortNumber() {
        try {
            return port.getData().getIntValue();
        } catch (NumberFormatException e) {
            return 1; // Default port number
        }
    }

}
