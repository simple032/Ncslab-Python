package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import com.ncslab.dto.block.specialized.subsystem.InDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Input port block for subsystems with SIMULINK-compatible parameters.
 *
 * SIMULINK Parameters:
 * - Port: Port number for subsystem input
 * - PortDimensions: Port dimensions specification
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutputDataTypeStr: Output data type specification
 */
public class In extends Block {

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
        // Port names - In block has no inputs, only output to subsystem
        outputNames.add("out1");
    }
    // === Private Constructor with Typed Parameters ===
    private In(Parameter port, Parameter portDimensions, Parameter sampleTime, Parameter outputDataType,
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

 - In block receives from subsystem input
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public In(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.port = getParameterByName("No");
        this.portDimensions = getParameterByName("PortDimensions");
        this.sampleTime = getParameterByName("SampleTime");
        this.outputDataType = getParameterByName("OutputDataTypeStr");

        // Add all parameters to parameter list if they exist
        if (this.port != null) parameterList.add(this.port);
        if (this.portDimensions != null) parameterList.add(this.portDimensions);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outputDataType != null) parameterList.add(this.outputDataType);

        
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates In block directly from BlockDto DTO
     */
    public In(InDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        // Create SIMULINK parameters with defaults
        this.port = getParameterByName("No");
        this.portDimensions = getParameterByName("PortDimensions");
        this.sampleTime = getParameterByName("SampleTime");
        this.outputDataType = getParameterByName("OutputDataTypeStr");

        // Add all parameters to parameter list if they exist
        if (this.port != null) parameterList.add(this.port);
        if (this.portDimensions != null) parameterList.add(this.portDimensions);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outputDataType != null) parameterList.add(this.outputDataType);

        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static In fromJSON(JSONObject blockJSON, NCSLabModel model) {
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

            In block = new In(port, portDimensions, sampleTime, outputDataType,
                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, port, portDimensions, sampleTime, outputDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create In block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static In create(String name, String path, int portNumber, NCSLabModel model) {
        return create(name, path, portNumber, "-1", -1.0, "Inherit: auto", model);
    }

    public static In create(String name, String path, int portNumber, String portDimensions,
                           double sampleTime, String outputDataType, NCSLabModel model) {
        Parameter portParam = new Parameter(null, 1, "No", String.valueOf(portNumber));
        Parameter portDimensionsParam = new Parameter(null, 2, "PortDimensions", portDimensions);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outputDataTypeParam = new Parameter(null, 4, "OutputDataTypeStr", outputDataType);

        In block = new In(portParam, portDimensionsParam, sampleTimeParam, outputDataTypeParam,
                         name, path, "null", model);

        setParameterBlockReference(block, portParam, portDimensionsParam, sampleTimeParam, outputDataTypeParam);

        return block;
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add In-specific context
        context.put("portNumber", port.getData().getInitValue());
        context.put("subsystem", subsystem);
        
        // Safely handle signal connection chain with null checks
        InputPort inputPort = this.getSubsystem().getInputPortList().get(getPortNumber() - 1);
        if (inputPort != null && inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null) {
            context.put("inputSignal", inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        } else {
            context.put("inputSignal", "0.0"); // Default value when no input connected
        }
        context.put("outputSignal", outputPortList.get(0).getOutputSignalC().getName());

        String outputCode = TemplateManager.renderTemplate("c/subsystem/In/output.vm", context);
        code.addOutputCode(outputCode);
    }
    public void updateDimension() throws MatDimException {
        // In block passes dimensions from subsystem input to internal output
        OutputPort out = outputPortList.get(0);
        if(this.subsystem == null) return;
        InputPort in = this.getSubsystem().getInputPortList().get(getPortNumber() - 1);

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
        // Pass data from subsystem input to internal blocks
        if(this.subsystem == null) return;
        InputPort in = this.getSubsystem().getInputPortList().get(getPortNumber() - 1);
        OutputPort out = outputPortList.get(0);
        out.setData(in.getData());
    }

    @Override
    public void calculateInit() {
        // Initialize data passing
        if(this.subsystem == null) return;
        InputPort in = this.getSubsystem().getInputPortList().get(getPortNumber() - 1);
        OutputPort out = outputPortList.get(0);
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

    private static void setParameterBlockReference(In block, Parameter... parameters) {
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
        identity.put("blockType", "In");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // And provides to inside subsystem
        outputPortList.add(new OutputPort(this, 1, true));
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
