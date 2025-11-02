package com.ncslab.block.continuous;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.continuous.VariableTransportDelayDto;

import com.ncslab.block.continuous.ContinuousBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

/**
 * VariableTransportDelay block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - DelayType: Type of variable delay
 * - MaximumDelayTime: Maximum delay time
 * - InitialOutput: Initial output value before delay takes effect
 * - InitialBufferSize: Initial size of the delay buffer
 * - PadeOrder: Order of Pade approximation (for approximation methods)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class VariableTransportDelay extends ContinuousBlock {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter delayType;
    @Getter
    private final Parameter maximumDelayTime;
    // Note: No @Getter on initialOutput to avoid conflict with ContinuousBlock.getInitialOutput()
    private final Parameter initialOutput;
    @Getter
    private final Parameter initialBufferSize;
    @Getter
    private final Parameter padeOrder;
    @Getter
    private final Parameter sampleTime;
    @Getter
    private final Parameter outDataType;
    @Getter
    private final Parameter saturateOnIntegerOverflow;
    
    // === Port References ===
    private OutputPort output;
    private InputPort inputSignal;
    private InputPort inputDelay;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1"); // Signal input
        inputNames.add("in2"); // Delay time input
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("DelayType", "Variable");
        PARAMETER_DEFAULTS.put("MaximumDelayTime", "1.0");
        PARAMETER_DEFAULTS.put("InitialOutput", "0.0");
        PARAMETER_DEFAULTS.put("InitialBufferSize", "1024");
        PARAMETER_DEFAULTS.put("PadeOrder", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "0");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    // === Private Constructor with Typed Parameters ===
    private VariableTransportDelay(Parameter delayType, Parameter maximumDelayTime, Parameter initialOutput,
                                  Parameter initialBufferSize, Parameter padeOrder, Parameter sampleTime,
                                  Parameter outDataType, Parameter saturateOnIntegerOverflow,
                                  String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(maximumDelayTime, initialBufferSize, sampleTime);
        
        // Assign parameters
        this.delayType = Objects.requireNonNull(delayType, "Delay type parameter cannot be null");
        this.maximumDelayTime = Objects.requireNonNull(maximumDelayTime, "Maximum delay time parameter cannot be null");
        this.initialOutput = Objects.requireNonNull(initialOutput, "Initial output parameter cannot be null");
        this.initialBufferSize = Objects.requireNonNull(initialBufferSize, "Initial buffer size parameter cannot be null");
        this.padeOrder = Objects.requireNonNull(padeOrder, "Pade order parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.delayType);
        parameterList.add(this.maximumDelayTime);
        parameterList.add(this.initialOutput);
        parameterList.add(this.initialBufferSize);
        parameterList.add(this.padeOrder);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);


        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public VariableTransportDelay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.delayType = getParameterByName("DelayType");
        this.maximumDelayTime = getParameterByName("MaximumDelayTime");
        this.initialOutput = getParameterByName("InitialOutput");
        this.initialBufferSize = getParameterByName("InitialBufferSize");
        this.padeOrder = getParameterByName("PadeOrder");
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime"); // 0 for continuous delay
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates VariableTransportDelay block directly from BlockDto DTO
     */
    public VariableTransportDelay(VariableTransportDelayDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.delayType = getParameterByName("DelayType");
        this.maximumDelayTime = getParameterByName("MaximumDelayTime");
        this.initialOutput = getParameterByName("InitialOutput");
        this.initialBufferSize = getParameterByName("InitialBufferSize");
        this.padeOrder = getParameterByName("PadeOrder");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static VariableTransportDelay fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter delayType = createDelayTypeFromJSON(paramValues, blockName);
            Parameter maximumDelayTime = createMaximumDelayTimeFromJSON(paramValues, blockName);
            Parameter initialOutput = createInitialOutputFromJSON(paramValues, blockName);
            Parameter initialBufferSize = createInitialBufferSizeFromJSON(paramValues, blockName);
            Parameter padeOrder = createPadeOrderFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            VariableTransportDelay block = new VariableTransportDelay(delayType, maximumDelayTime, initialOutput,
                                                                      initialBufferSize, padeOrder, sampleTime,
                                                                      outDataType, saturateParam,
                                                                      blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, delayType, maximumDelayTime, initialOutput,
                                     initialBufferSize, padeOrder, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create VariableTransportDelay block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static VariableTransportDelay create(String name, String path, String delayType, double maxDelayTime,
                                               double initialOutput, int initialBufferSize, NCSLabModel model) {
        return create(name, path, delayType, maxDelayTime, initialOutput, initialBufferSize, 0,
                     0.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a VariableTransportDelay block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param delayType Type of variable delay
     * @param maxDelayTime Maximum delay time
     * @param initialOutput Initial output value before delay takes effect
     * @param initialBufferSize Initial size of the delay buffer
     * @param padeOrder Order of Pade approximation (for approximation methods)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return VariableTransportDelay block instance
     */
    public static VariableTransportDelay create(String name, String path, String delayType, double maxDelayTime,
                                               double initialOutput, int initialBufferSize, int padeOrder,
                                               double sampleTime, String outDataType, boolean saturateOnOverflow,
                                               NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        VariableTransportDelayDto dto = VariableTransportDelayDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .maximumDelay(com.ncslab.dto.common.TypedParameter.of(maxDelayTime))
            .initialInput(com.ncslab.dto.common.TypedParameter.of(initialOutput))
            .bufferSize(com.ncslab.dto.common.TypedParameter.of(initialBufferSize))
            .padeOrder(com.ncslab.dto.common.TypedParameter.of(padeOrder))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid VariableTransportDelay parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new VariableTransportDelay(dto, model);
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter maximumDelayTime, Parameter initialBufferSize, Parameter sampleTime) {
        double maxDelayTimeValue = maximumDelayTime.getDouble();
        if (maxDelayTimeValue <= 0.0 || maxDelayTimeValue == Double.NaN || maxDelayTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Maximum delay time must be positive and finite");
        }
        
        int bufferSizeValue = (int) initialBufferSize.getDouble();
        if (bufferSizeValue <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be positive");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createDelayTypeFromJSON(JSONObject paramValues, String blockName) {
        String delayTypeValue = paramValues.optString("VariableDelayType", "Variable");
        return new Parameter(null, 1, "DelayType", delayTypeValue);
    }
    
    private static Parameter createMaximumDelayTimeFromJSON(JSONObject paramValues, String blockName) {
        String maxDelayTimeValue = paramValues.optString("MaxDelayTime", "1.0");
        return new Parameter(null, 2, "MaximumDelayTime", maxDelayTimeValue);
    }
    
    private static Parameter createInitialOutputFromJSON(JSONObject paramValues, String blockName) {
        String initialOutputValue = paramValues.optString("InitialOutput", "0.0");
        return new Parameter(null, 3, "InitialOutput", initialOutputValue);
    }
    
    private static Parameter createInitialBufferSizeFromJSON(JSONObject paramValues, String blockName) {
        String initialBufferSizeValue = paramValues.optString("InitialBuffsize", "1024");
        return new Parameter(null, 4, "InitialBufferSize", initialBufferSizeValue);
    }
    
    private static Parameter createPadeOrderFromJSON(JSONObject paramValues, String blockName) {
        String padeOrderValue = paramValues.optString("PadeOrder", "0");
        return new Parameter(null, 5, "PadeOrder", padeOrderValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 6, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 7, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 8, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(VariableTransportDelay block, Parameter... parameters) {
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
        identity.put("blockType", "VariableTransportDelay");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        // Signal input port
        inputSignal = new InputPort(this, 1);
        inputPortList.add(inputSignal);
        
        // Delay time input port
        inputDelay = new InputPort(this, 2);
        inputPortList.add(inputDelay);
        
        // Main output port (no feedthrough for variable transport delay)
        output = new OutputPort(this, 1, false);
        outputPortList.add(output);
    }

    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        // Now using standardized $MaximumDelayTimeValue and $InitialBufferSizeValue from populateAllContext

        String arraysCode = TemplateManager.renderTemplate("c/continuous/VariableTransportDelay/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // The parameter objects are now available as $MaximumDelayTimeObject, $InitialOutputObject, etc.
        // through populateAllContext, but keep legacy names temporarily for compatibility
        String codeStr = TemplateManager.renderTemplate("c/continuous/VariableTransportDelay/init.vm", context);
        code.addInitCode(codeStr);
    }

    /**
     * Get the current index variable name for the circular buffer.
     * Made public for Velocity template access.
     */
    public String getCurrentIndexName(){
        return "currentIndex_VariableTransportDelay" + getBlockId();
    }

    public void generateOutputCodeC(CodeStructC code) {
        // Fail fast - validate required signal connections exist
        if (inputPortList == null || inputPortList.size() < 2) {
            throw new BlockCreationException("VariableTransportDelay block cannot generate output code: requires exactly 2 input ports");
        }
        
        InputPort inputPort1 = inputPortList.get(0);
        InputPort inputPort2 = inputPortList.get(1);
        
        if (inputPort1 == null || inputPort1.getLinkedLine() == null || 
            inputPort1.getLinkedLine().getLinkedOutputPort() == null) {
            throw new BlockCreationException("VariableTransportDelay block cannot generate output code: signal input port not properly connected");
        }
        
        if (inputPort2 == null || inputPort2.getLinkedLine() == null || 
            inputPort2.getLinkedLine().getLinkedOutputPort() == null) {
            throw new BlockCreationException("VariableTransportDelay block cannot generate output code: delay input port not properly connected");
        }
        
        OutputSignal signal1 = inputPort1.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPort2.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        
        if (signal1 == null) {
            throw new BlockCreationException("VariableTransportDelay block cannot generate output code: signal input is null");
        }
        if (signal2 == null) {
            throw new BlockCreationException("VariableTransportDelay block cannot generate output code: delay input is null");
        }
        
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add block-specific context
        context.put("signal", signal1);
        context.put("signal2", signal2);
        context.put("MaximumDelayTime", maximumDelayTime);
        context.put("InitialOutput", initialOutput);
        String codeStr = TemplateManager.renderTemplate("c/continuous/VariableTransportDelay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // Fail fast - validate required ports and connections exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new MatDimException("VariableTransportDelay block cannot update dimensions: no output ports configured");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new MatDimException("VariableTransportDelay block cannot update dimensions: no input ports configured");
        }
        
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        
        if (out == null) {
            throw new MatDimException("VariableTransportDelay block cannot update dimensions: output port is null");
        }
        if (in == null || in.getLinkedLine() == null || in.getLinkedLine().getLinkedOutputPort() == null) {
            throw new MatDimException("VariableTransportDelay block cannot update dimensions: input port not properly connected");
        }
        
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new MatDimException("VariableTransportDelay block cannot update dimensions: input signal is null");
        }

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // Fail fast - validate required parameters exist
        if (maximumDelayTime == null) {
            throw new MatDimException("VariableTransportDelay block cannot check dimensions: maximum delay time parameter is null");
        }
        if (initialOutput == null) {
            throw new MatDimException("VariableTransportDelay block cannot check dimensions: initial output parameter is null");
        }
        if (initialBufferSize == null) {
            throw new MatDimException("VariableTransportDelay block cannot check dimensions: initial buffer size parameter is null");
        }
        if (padeOrder == null) {
            throw new MatDimException("VariableTransportDelay block cannot check dimensions: pade order parameter is null");
        }
        
        if (maximumDelayTime.getDataType() != DataType.REAL || initialOutput.getDataType() != DataType.REAL || 
            initialBufferSize.getDataType() != DataType.REAL || padeOrder.getDataType() != DataType.REAL) {
            throw new MatDimException("Parameter of Block " + this.blockName + " can't be Matrix!\n \n");
        }
    }
}
