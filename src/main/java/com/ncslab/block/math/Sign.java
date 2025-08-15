package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Sign block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - ZeroCrossing: Enable zero-crossing detection
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Sign extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter zeroCrossing;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("ZeroCrossing", "on");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    
    // === Private Constructor with Typed Parameters ===
    private Sign(Parameter zeroCrossing, Parameter sampleTime, Parameter outDataType, 
                Parameter saturateOnIntegerOverflow, String blockName, String blockPath, 
                String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(sampleTime);
        
        // Assign parameters
        this.zeroCrossing = Objects.requireNonNull(zeroCrossing, "Zero crossing parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Sign(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Create missing SIMULINK parameters with defaults
        this.zeroCrossing = new Parameter(this, 1, "ZeroCrossing", "on");
        this.sampleTime = new Parameter(this, 2, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates Sign block directly from BlockJson DTO
     */
    public Sign(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.zeroCrossing = new Parameter(this, 1, "ZeroCrossing", "on");
        this.sampleTime = new Parameter(this, 2, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }// === Static Factory Method for JSON Deserialization ===
    public static Sign fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter zeroCrossing = createZeroCrossingFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Sign block = new Sign(zeroCrossing, sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, zeroCrossing, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Sign block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Sign create(String name, String path, NCSLabModel model) {
        return create(name, path, true, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Sign create(String name, String path, boolean zeroCrossing, double sampleTime,
                             String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter zeroCrossingParam = new Parameter(null, 1, "ZeroCrossing", zeroCrossing ? "on" : "off");
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Sign block = new Sign(zeroCrossingParam, sampleTimeParam, outDataTypeParam, saturateParam,
                             name, path, "null", model);
        
        setParameterBlockReference(block, zeroCrossingParam, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createZeroCrossingFromJSON(JSONObject paramValues, String blockName) {
        String zeroCrossingValue = paramValues.optString("ZeroCrossing", "on");
        return new Parameter(null, 1, "ZeroCrossing", zeroCrossingValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 3, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 4, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(Sign block, Parameter... parameters) {
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
        identity.put("blockType", "Sign");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        
        context.put("block", this);
        context.put("inputSignal", ops1.getOutputSignalC());
        context.put("outputSignal", getOutputPortList().get(0).getOutputSignalC());
        context.put("inputHeight", ops1.getHeight());
        context.put("inputWidth", ops1.getWidth());
        
        String codeStr = TemplateManager.renderTemplate("m/math/Sign/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());

        String codeStr = TemplateManager.renderTemplate("c/math/Sign/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateInit() {
        // Initialization logic for Sign block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();
        double value = inputData.getInitValue();
        double signValue = Math.signum(value);
        Data resultData = new Data(signValue);
        out.setData(resultData);
    }
}

