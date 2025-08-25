package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.AbsDto;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
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
 * Abs block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Abs extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter sampleTime;
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
        
        // SIMULINK parameter names
    }

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Private Constructor with Typed Parameters ===
    private Abs(Parameter sampleTime, Parameter outDataType, 
               Parameter saturateOnIntegerOverflow, String blockName, String blockPath, 
               String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(sampleTime);
        
        // Assign parameters
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Abs(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Use name-based parameter access instead of index-based
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Initialize ports
        initializePorts();
    }

    /**
     * DTO Constructor - Creates Abs block directly from AbsDto DTO
     */
    public Abs(AbsDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Extract parameters from DTO with defaults
        String sampleTimeValue = dto.getSampleTime() != null ? (dto.getSampleTime().getAsString()) : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Validate sample time
        double sampleTimeDouble = Double.parseDouble(sampleTimeValue);
        if (sampleTimeDouble != -1.0 && sampleTimeDouble <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
        
        // Initialize parameters
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Initialize ports
        initializePorts();
        
        System.out.println("DTO-SPECIFIC: Abs block created successfully from AbsDto - " + dto.getBlockName());
    }

// === Static Factory Method for JSON Deserialization ===
    public static Abs fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            
            Abs block = new Abs(sampleTime, outDataType, saturateParam,
                               blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Abs block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Abs create(String name, String path, NCSLabModel model) {
        return create(name, path, -1.0, "Inherit: Same as input", false, model);
    }

    public static Abs create(String name, String path, double sampleTime,
                            String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter sampleTimeParam = new Parameter(null, 1, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 2, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 3, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Abs block = new Abs(sampleTimeParam, outDataTypeParam, saturateParam,
                           name, path, "null", model);
        
        setParameterBlockReference(block, sampleTimeParam, outDataTypeParam, saturateParam);
        
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
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
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

    private static void setParameterBlockReference(Abs block, Parameter... parameters) {
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
        identity.put("blockType", "Abs");
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
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode="";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());

        String codeStr = TemplateManager.renderTemplate("c/math/Abs/output.vm", context);
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
        // Check dimensions if needed
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Abs block: computes absolute value of input
        Data inputData = inputPortList.get(0).getData();
        Data outputData;
        
        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply abs element-wise
            Jama.Matrix inputMatrix = inputData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
            
            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    outputMatrix.set(i, j, Math.abs(value));
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input - apply abs directly
            double inputValue = inputData.getInitValue();
            outputData = new Data(1, 1);
            outputData.setInitValue(Math.abs(inputValue));
        }
        
        outputPortList.get(0).setData(outputData);
    }
}