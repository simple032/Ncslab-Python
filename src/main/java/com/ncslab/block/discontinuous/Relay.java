package com.ncslab.block.discontinuous;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
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
import java.util.Vector;

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
public class Relay extends Block {
    // Legacy fields for backward compatibility
    Parameter onSwitchValue;
    Parameter offSwitchValue;
    Parameter onOutputValue;
    Parameter offOutputValue;
    private State xState;

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter switchOnPoint;
    @Getter
    private final Parameter switchOffPoint;
    @Getter
    private final Parameter outputWhenOn;
    @Getter
    private final Parameter outputWhenOff;
    @Getter
    private final Parameter sampleTime;
    @Getter
    private final Parameter outDataType;
    @Getter
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();
    
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
    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // Parameter names matching legacy JSON field names
        parameterNames.add("OnSwitchValue"); // Maps to SwitchOnPoint
        parameterNames.add("OffSwitchValue"); // Maps to SwitchOffPoint
        parameterNames.add("OnOutputValue"); // Maps to OutputWhenOn
        parameterNames.add("OffOutputValue"); // Maps to OutputWhenOff
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
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
    }
    // === Static Factory Method for JSON Deserialization ===
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
        context.put("block", this);
        context.put("onSwitchValue", onSwitchValue);
        context.put("offSwitchValue", offSwitchValue);
        context.put("onOutputValue", onOutputValue);
        context.put("offOutputValue", offOutputValue);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Relay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("onSwitchValue", onSwitchValue);
        context.put("offSwitchValue", offSwitchValue);
        context.put("onOutputValue", onOutputValue);
        context.put("offOutputValue", offOutputValue);
        context.put("outputs", getOutputPortVariables());
        context.put("inputs", getInputPortVariables());

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
        if (xState != null) {
            Data output = onOutputValue.getData();
            outputPortList.get(0).getOutputSignalC().setData(output);
        }
    }

    @Override
    public void calculateOutput(double t) {
        Data input = inputPortList.get(0).getData();
        double inputValue = input.getInitValue();
        double onSwitch = onSwitchValue.getData().getInitValue();
        double offSwitch = offSwitchValue.getData().getInitValue();
        
        Data output;
        if (inputValue >= onSwitch) {
            output = onOutputValue.getData();
        } else if (inputValue <= offSwitch) {
            output = offOutputValue.getData();
        } else {
            // Keep previous state - use current output
            output = outputPortList.get(0).getOutputSignalC().getData();
        }
        
        outputPortList.get(0).getOutputSignalC().setData(output);
    }
}
