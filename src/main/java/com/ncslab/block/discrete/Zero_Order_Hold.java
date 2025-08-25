package com.ncslab.block.discrete;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.ZeroOrderHoldDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
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
 * Zero_Order_Hold block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Zero_Order_Hold extends DiscreteBlock {
    private State stateOutput;
    private final boolean feedthrough = false; // Zero-order hold has no feedthrough

    // === SIMULINK-Compatible Parameters ===
    private final Parameter sampleTimeParam;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Private Constructor with Typed Parameters ===
    private Zero_Order_Hold(Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                           String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Zero_Order_Hold(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.sampleTimeParam = getParameterByName("SampleTime");
        
        // Create missing SIMULINK parameters with defaults
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
    }
    
    /**
     * DTO-NATIVE Constructor - Creates Zero_Order_Hold block directly from ZeroOrderHoldDto DTO
     */
    public Zero_Order_Hold(ZeroOrderHoldDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Create parameters from DTO
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));
        
        setSampleTime(sampleTimeParam);
        System.out.println("DTO-NATIVE: Zero_Order_Hold block created successfully - " + dto.getBlockName());
    }
    
    /**
     * Generic DTO Constructor for factory compatibility
     */
    public Zero_Order_Hold(BlockDto blockDto, NCSLabModel model) {
        this((ZeroOrderHoldDto) blockDto, model);
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedthrough));
    }
    // === Static Factory Method for JSON Deserialization ===
    public static Zero_Order_Hold fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Zero_Order_Hold block = new Zero_Order_Hold(sampleTime, outDataType, saturateParam,
                                                       blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Zero_Order_Hold block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Zero_Order_Hold create(String name, String path, double sampleTime, NCSLabModel model) {
        return create(name, path, sampleTime, "Inherit: Same as input", false, model);
    }
    public static Zero_Order_Hold create(String name, String path, double sampleTime, String outDataType,
                                        boolean saturateOnOverflow, NCSLabModel model) {
        Parameter sampleTimeParam = new Parameter(null, 1, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 2, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 3, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Zero_Order_Hold block = new Zero_Order_Hold(sampleTimeParam, outDataTypeParam, saturateParam,
                                                   name, path, "null", model);
        
        setParameterBlockReference(block, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 2, "OutDataTypeStr", outDataTypeValue);
    }
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 3, "SaturateOnIntegerOverflow", saturateValue);
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
    private static void setParameterBlockReference(Zero_Order_Hold block, Parameter... parameters) {
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
        identity.put("blockType", "Zero_Order_Hold");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("sampleTime", sampleTimeParam);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Zero_Order_Hold/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputPort ops = outputPortList.get(0);
        context.put("block", this);
        context.put("outputPortList", getOutputPortList());
        context.put("sampleTime", sampleTimeParam);
        context.put("signal", signal);
        context.put("ops", ops);
        context.put("optHeightIndex", ops.getHeight()-1);
        context.put("optWidthIndex", ops.getWidth()-1);
        context.put("outputs", getOutputPortVariables());


        String codeStr = TemplateManager.renderTemplate("c/discrete/Zero_Order_Hold/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDiscreteUpdateCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("inputPortList", getInputPortList());
        context.put("stateOutput", stateOutput);
        context.put("signal", signal);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discrete/Zero_Order_Hold/discrete_update.vm", context);
        code.addDiscreteUpdateCode(codeStr);
    }

    public void updateDimension() throws MatDimException {super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if ((Double.parseDouble(paramValues.getString("SampleTime").trim()) * 1000000) % (model.getConfig().getFixedStep() * 1000000) > 0.000001) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        if (sampleTimeParam.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());

        switch (signal.getDataType()) {
            case REAL:
                stateOutput = new State(this, 1, "stateOutput", 1, 1);
                break;
            case MATRIX:
                stateOutput = new State(this, 1, "stateOutput", signal.getHeight(), signal.getWidth());
                break;
        }
        stateList.add(stateOutput);
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            OutputSignal inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            OutputPort outputPort = outputPortList.get(0);
            
            outputPort.setHeight(inputSignal.getHeight());
            outputPort.setWidth(inputSignal.getWidth());
            outputPort.getOutputSignalC().setHeight(inputSignal.getHeight());
            outputPort.getOutputSignalC().setWidth(inputSignal.getWidth());
            outputPort.getOutputSignalC().setDataType(inputSignal.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        // No additional dimension checks needed for zero-order hold
    }
}