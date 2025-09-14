package com.ncslab.block.discontinuous;

import com.ncslab.block.discontinuous.DiscontinuousBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discontinuous.RelayDto;
import java.util.HashMap;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Relay block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - SwitchOnPoint: Input value at which the relay switches on
 * - SwitchOffPoint: Input value at which the relay switches off
 * - OutputWhenOn: Output value when relay is on
 * - OutputWhenOff: Output value when relay is off
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Relay extends DiscontinuousBlock {
    // Legacy fields for backward compatibility
    Parameter onSwitchValue;
    Parameter offSwitchValue;
    Parameter onOutputValue;
    Parameter offOutputValue;
    private State xState;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter switchOnPoint;
    private final Parameter switchOffPoint;
    private final Parameter outputWhenOn;
    private final Parameter outputWhenOff;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("OnSwitchValue", "1.0");     // SwitchOnPoint
        PARAMETER_DEFAULTS.put("OffSwitchValue", "0.0");    // SwitchOffPoint
        PARAMETER_DEFAULTS.put("OnOutputValue", "1.0");     // OutputWhenOn
        PARAMETER_DEFAULTS.put("OffOutputValue", "0.0");    // OutputWhenOff
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (relay has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // Add a method to calculate state indices
    private int[] calculateStateIndices(int height, int width) {
        int[] indices = new int[height * width];
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                indices[index++] = i * width + j;
            }
        }
        return indices;
    }
    // === Private Constructor with Typed Parameters ===
    private Relay(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Get parameters by name from the automatically populated parameterList (via parseParameterList())
        this.switchOnPoint = getParameterByName("OnSwitchValue"); // OnSwitchValue -> SwitchOnPoint
        this.switchOffPoint = getParameterByName("OffSwitchValue"); // OffSwitchValue -> SwitchOffPoint
        this.outputWhenOn = getParameterByName("OnOutputValue"); // OnOutputValue -> OutputWhenOn
        this.outputWhenOff = getParameterByName("OffOutputValue"); // OffOutputValue -> OutputWhenOff
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Legacy field mapping for backward compatibility
        this.onSwitchValue = this.switchOnPoint;
        this.offSwitchValue = this.switchOffPoint;
        this.onOutputValue = this.outputWhenOn;
        this.offOutputValue = this.outputWhenOff;
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Relay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically
        
        // Get parameters by name from the automatically populated parameterList
        this.switchOnPoint = getParameterByName("OnSwitchValue"); // OnSwitchValue -> SwitchOnPoint
        this.switchOffPoint = getParameterByName("OffSwitchValue"); // OffSwitchValue -> SwitchOffPoint
        this.outputWhenOn = getParameterByName("OnOutputValue"); // OnOutputValue -> OutputWhenOn
        this.outputWhenOff = getParameterByName("OffOutputValue"); // OffOutputValue -> OutputWhenOff
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Legacy field mapping for backward compatibility
        this.onSwitchValue = this.switchOnPoint;
        this.offSwitchValue = this.switchOffPoint;
        this.onOutputValue = this.outputWhenOn;
        this.offOutputValue = this.outputWhenOff;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates Relay block directly from BlockDto DTO
     */
    public Relay(RelayDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO - use correct parameter names matching JSON constructor
        this.switchOnPoint = getParameterByName("OnSwitchValue");
        this.switchOffPoint = getParameterByName("OffSwitchValue");
        this.outputWhenOn = getParameterByName("OnOutputValue");
        this.outputWhenOff = getParameterByName("OffOutputValue");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.switchOnPoint != null) parameterList.add(this.switchOnPoint);
        if (this.switchOffPoint != null) parameterList.add(this.switchOffPoint);
        if (this.outputWhenOn != null) parameterList.add(this.outputWhenOn);
        if (this.outputWhenOff != null) parameterList.add(this.outputWhenOff);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        
        initializePorts();
        
        // Legacy field mapping for backward compatibility
        this.onSwitchValue = this.switchOnPoint;
        this.offSwitchValue = this.switchOffPoint;
        this.onOutputValue = this.outputWhenOn;
        this.offOutputValue = this.outputWhenOff;

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }// === Static Factory Method for JSON Deserialization ===
    public static Relay fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            
            return new Relay(blockName, blockPath, blockUUID, model);
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Relay block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Relay create(String name, String path, String switchOnPoint, String switchOffPoint,
                              String outputWhenOn, String outputWhenOff, NCSLabModel model) {
        return create(name, path, switchOnPoint, switchOffPoint, outputWhenOn, outputWhenOff,
                     -1.0, "Inherit: Same as input", false, model);
    }
    public static Relay create(String name, String path, String switchOnPoint, String switchOffPoint,
                              String outputWhenOn, String outputWhenOff, double sampleTime, 
                              String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Create a JSONObject with parameter values for centralized parsing
        JSONObject paramValues = new JSONObject();
        paramValues.put("OnSwitchValue", switchOnPoint);
        paramValues.put("OffSwitchValue", switchOffPoint);
        paramValues.put("OnOutputValue", outputWhenOn);
        paramValues.put("OffOutputValue", outputWhenOff);
        paramValues.put("SampleTime", String.valueOf(sampleTime));
        paramValues.put("OutDataTypeStr", outDataType);
        paramValues.put("SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        JSONObject blockJSON = createBlockIdentity(name, path, "null");
        blockJSON.put("paramValues", paramValues);
        
        return new Relay(blockJSON, model);
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
    
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Relay");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Code Generation Methods ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("onSwitchValue", onSwitchValue);
        context.put("offSwitchValue", offSwitchValue);
        context.put("onOutputValue", onOutputValue);
        context.put("offOutputValue", offOutputValue);

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Relay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("onSwitchValue", onSwitchValue);
        context.put("offSwitchValue", offSwitchValue);
        context.put("onOutputValue", onOutputValue);
        context.put("offOutputValue", offOutputValue);
        context.put("outputs", getOutputPortVariables());
        context.put("inputs", getInputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Relay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Fail fast - validate required parameters exist
        if (switchOnPoint == null) {
            throw new BlockCreationException("Relay block requires switch on point parameter for initialization");
        }
        if (switchOffPoint == null) {
            throw new BlockCreationException("Relay block requires switch off point parameter for initialization");
        }
        if (outputWhenOn == null) {
            throw new BlockCreationException("Relay block requires output when on parameter for initialization");
        }
        if (outputWhenOff == null) {
            throw new BlockCreationException("Relay block requires output when off parameter for initialization");
        }
        
        // Use legacy fields if available, otherwise use SIMULINK parameters
        Parameter onSwitchParam = (onSwitchValue != null) ? onSwitchValue : switchOnPoint;
        Parameter offSwitchParam = (offSwitchValue != null) ? offSwitchValue : switchOffPoint;
        Parameter onOutputParam = (onOutputValue != null) ? onOutputValue : outputWhenOn;
        Parameter offOutputParam = (offOutputValue != null) ? offOutputValue : outputWhenOff;
        
        context.put("onSwitchValue", onSwitchParam);
        // onSwitchValueName is already set by TemplateUtils.populateAllContext() with correct prefix
        context.put("offSwitchValue", offSwitchParam);
        // offSwitchValueName is already set by TemplateUtils.populateAllContext() with correct prefix
        context.put("onOutputValue", onOutputParam);
        // onOutputValueName is already set by TemplateUtils.populateAllContext() with correct prefix
        context.put("offOutputValue", offOutputParam);
        // offOutputValueName is already set by TemplateUtils.populateAllContext() with correct prefix

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Relay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Fail fast - validate required parameters exist
        if (switchOnPoint == null) {
            throw new BlockCreationException("Relay block requires switch on point parameter for code generation");
        }
        if (switchOffPoint == null) {
            throw new BlockCreationException("Relay block requires switch off point parameter for code generation");
        }
        if (outputWhenOn == null) {
            throw new BlockCreationException("Relay block requires output when on parameter for code generation");
        }
        if (outputWhenOff == null) {
            throw new BlockCreationException("Relay block requires output when off parameter for code generation");
        }
        
        // Use legacy fields if available, otherwise use SIMULINK parameters
        Parameter onSwitchParam = (onSwitchValue != null) ? onSwitchValue : switchOnPoint;
        Parameter offSwitchParam = (offSwitchValue != null) ? offSwitchValue : switchOffPoint;
        Parameter onOutputParam = (onOutputValue != null) ? onOutputValue : outputWhenOn;
        Parameter offOutputParam = (offOutputValue != null) ? offOutputValue : outputWhenOff;
        
        context.put("onSwitchValue", onSwitchParam);
        // onSwitchValueName is already set by TemplateUtils.populateAllContext() with correct prefix
        context.put("offSwitchValue", offSwitchParam);
        // offSwitchValueName is already set by TemplateUtils.populateAllContext() with correct prefix
        context.put("onOutputValue", onOutputParam);
        // onOutputValueName is already set by TemplateUtils.populateAllContext() with correct prefix
        context.put("offOutputValue", offOutputParam);
        // offOutputValueName is already set by TemplateUtils.populateAllContext() with correct prefix
        
        // Add input/output signal names
        context.put("inputs", getInputPortVariables());
        context.put("outputs", getOutputPortVariables());
        
        // Add the missing output1 variable
        context.put("output1", getOutputPortVariable(0));

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Relay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        super.updateDimension();
    }

    @Override
    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }

    @Override
    public void calculateInit() {
        // Initialize state based on initial condition
        
        // Fail fast - validate critical components exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("Relay block cannot initialize: no output ports configured");
        }
        
        OutputPort outputPort = outputPortList.get(0);
        if (outputPort == null) {
            throw new IllegalStateException("Relay block cannot initialize: output port is null");
        }
        
        if (outputWhenOff == null) {
            throw new IllegalStateException("Relay block cannot initialize: output when off parameter is missing");
        }
        
        // Initialize with off state by default
        Parameter offOutputParam = (offOutputValue != null) ? offOutputValue : outputWhenOff;
        Data output = offOutputParam.getData();
        outputPort.getOutputSignalC().setData(output);
    }

    @Override
    public void calculateOutput(double t) {
        // Fail fast - validate required ports and parameters exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new IllegalStateException("Relay block cannot calculate output: no input ports configured");
        }
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("Relay block cannot calculate output: no output ports configured");
        }
        
        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null || inputPort.getData() == null) {
            throw new IllegalStateException("Relay block cannot calculate output: input data is null");
        }
        
        OutputPort outputPort = outputPortList.get(0);
        if (outputPort == null || outputPort.getOutputSignalC() == null) {
            throw new IllegalStateException("Relay block cannot calculate output: output port or signal is null");
        }
        
        if (switchOnPoint == null || switchOnPoint.getData() == null) {
            throw new IllegalStateException("Relay block cannot calculate output: switch on point parameter is missing");
        }
        if (switchOffPoint == null || switchOffPoint.getData() == null) {
            throw new IllegalStateException("Relay block cannot calculate output: switch off point parameter is missing");
        }
        if (outputWhenOn == null || outputWhenOff == null) {
            throw new IllegalStateException("Relay block cannot calculate output: output parameters are missing");
        }
        
        Data input = inputPort.getData();
        double inputValue = input.getInitValue();
        
        // Use legacy fields if available, otherwise use SIMULINK parameters
        Parameter onSwitchParam = (onSwitchValue != null) ? onSwitchValue : switchOnPoint;
        Parameter offSwitchParam = (offSwitchValue != null) ? offSwitchValue : switchOffPoint;
        Parameter onOutputParam = (onOutputValue != null) ? onOutputValue : outputWhenOn;
        Parameter offOutputParam = (offOutputValue != null) ? offOutputValue : outputWhenOff;
        
        double onSwitch = onSwitchParam.getData().getInitValue();
        double offSwitch = offSwitchParam.getData().getInitValue();
        
        Data output;
        if (inputValue >= onSwitch) {
            output = onOutputParam.getData();
        } else if (inputValue <= offSwitch) {
            output = offOutputParam.getData();
        } else {
            // Keep previous state - use current output
            output = outputPort.getOutputSignalC().getData();
        }
        
        outputPort.getOutputSignalC().setData(output);
    }
}
