package com.ncslab.block.source;

import Jama.Matrix;
import com.ncslab.block.data.Data;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.source.RepeatingSequenceDto;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.source.SourceBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * RepeatingSequence block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - TimeValues: Time values for the sequence
 * - OutputValues: Output values corresponding to time values  
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class RepeatingSequence extends SourceBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter timeValues;
    private final Parameter outputValues;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Parameter Definitions ===
    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // Port names
        outputNames.add("out1");
        // No input ports for repeating sequence block
        
        // TODO:现在还使用旧版参数Parameter defaults
        // PARAMETER_DEFAULTS.put("TimeValues", "[0 1]");
        // PARAMETER_DEFAULTS.put("OutputValues", "[0 1]");
        PARAMETER_DEFAULTS.put("rep_seq_t", "[0 1]");
        PARAMETER_DEFAULTS.put("rep_seq_y", "[0 1]");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    // === Private Constructor with Typed Parameters ===
    private RepeatingSequence(Parameter timeValues, Parameter outputValues,
                             Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                             String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(timeValues, outputValues, sampleTime);
        
        // Assign parameters
        this.timeValues = Objects.requireNonNull(timeValues, "Time values parameter cannot be null");
        this.outputValues = Objects.requireNonNull(outputValues, "Output values parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.timeValues);
        parameterList.add(this.outputValues);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);


        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public RepeatingSequence(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Try to get parameters by new names first, then legacy names
        Parameter timeValuesParam = getParameterByName("TimeValues");
        if (timeValuesParam == null) {
            timeValuesParam = getParameterByName("rep_seq_t");
            if (timeValuesParam == null) {
                throw new BlockCreationException("RepeatingSequence block is missing required parameter 'TimeValues' (or legacy 'rep_seq_t')");
            }
        }
        this.timeValues = timeValuesParam;

        Parameter outputValuesParam = getParameterByName("OutputValues");
        if (outputValuesParam == null) {
            outputValuesParam = getParameterByName("rep_seq_y");
            if (outputValuesParam == null) {
                throw new BlockCreationException("RepeatingSequence block is missing required parameter 'OutputValues' (or legacy 'rep_seq_y')");
            }
        }
        this.outputValues = outputValuesParam;

        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates RepeatingSequence block directly from BlockDto DTO
     */
    public RepeatingSequence(RepeatingSequenceDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO with proper values
        String timeValuesStr = blockDto.getTimeValues() != null ? blockDto.getTimeValues().getValue(String.class) : "[0 1]";
        String outputValuesStr = blockDto.getOutputValues() != null ? blockDto.getOutputValues().getValue(String.class) : "[0 1]";
                
        if(getParameterByName("TimeValues") != null){
            this.timeValues = getParameterByName("TimeValues");
        } else if(getParameterByName("rep_seq_t") != null){
            this.timeValues = getParameterByName("rep_seq_t");
        } else{
            throw new BlockCreationException("RepeatingSequence block is missing required parameter 'TimeValues' (or legacy 'rep_seq_t')");
        }
       
        if(getParameterByName("OutputValues") != null){
            this.outputValues = getParameterByName("OutputValues");
        } else if(getParameterByName("rep_seq_y") != null){
            this.outputValues = getParameterByName("rep_seq_y");
        } else{
            throw new BlockCreationException("RepeatingSequence block is missing required parameter 'OutputValues' (or legacy 'rep_seq_y')");
        }

        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName() + 
                          " with timeValues=" + timeValuesStr + " outputValues=" + outputValuesStr);
    }

// === Static Factory Method for JSON Deserialization ===
    public static RepeatingSequence fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter timeValues = createTimeValuesFromJSON(paramValues, blockName);
            Parameter outputValues = createOutputValuesFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            RepeatingSequence block = new RepeatingSequence(timeValues, outputValues, sampleTime, outDataType, saturateParam,
                                                           blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, timeValues, outputValues, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create RepeatingSequence block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static RepeatingSequence create(String name, String path, String timeValues, String outputValues, NCSLabModel model) {
        return create(name, path, timeValues, outputValues, 0.0, "Inherit: Same as parameter", false, model);
    }

    /**
     * Create a RepeatingSequence block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param timeValues Time points for sequence (e.g., "[0 1 2 3]")
     * @param outputValues Output values at each time point (e.g., "[0 1 0 1]")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return RepeatingSequence block instance
     */
    public static RepeatingSequence create(String name, String path, String timeValues, String outputValues,
                                         double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        RepeatingSequenceDto dto = RepeatingSequenceDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .timeValues(com.ncslab.dto.common.TypedParameter.of(timeValues))
            .outputValues(com.ncslab.dto.common.TypedParameter.of(outputValues))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid RepeatingSequence parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new RepeatingSequence(dto, model);
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter timeValues, Parameter outputValues, Parameter sampleTime) {
        // Validate that time and output values have the same dimensions
        if (timeValues.getWidth() != outputValues.getWidth()) {
            throw new IllegalArgumentException("Time values and output values must have the same width");
        }
        
        if (timeValues.getHeight() != 1 || outputValues.getHeight() != 1) {
            throw new IllegalArgumentException("Time values and output values must be row vectors (height = 1)");
        }
        
        if (timeValues.getWidth() <= 1 || outputValues.getWidth() <= 1) {
            throw new IllegalArgumentException("Time values and output values must have more than 1 element");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createTimeValuesFromJSON(JSONObject paramValues, String blockName) {
        // Check new name first, then legacy name, then fail
        String timeValuesStr;
        if (paramValues.has("TimeValues")) {
            timeValuesStr = paramValues.getString("TimeValues");
        } else if (paramValues.has("rep_seq_t")) {
            timeValuesStr = paramValues.getString("rep_seq_t");
        } else {
            throw new BlockCreationException("RepeatingSequence block '" + blockName +
                "' is missing required parameter 'TimeValues' (or legacy 'rep_seq_t')");
        }
        return new Parameter(null, 1, "TimeValues", timeValuesStr);
    }

    private static Parameter createOutputValuesFromJSON(JSONObject paramValues, String blockName) {
        // Check new name first, then legacy name, then fail
        String outputValuesStr;
        if (paramValues.has("OutputValues")) {
            outputValuesStr = paramValues.getString("OutputValues");
        } else if (paramValues.has("rep_seq_y")) {
            outputValuesStr = paramValues.getString("rep_seq_y");
        } else {
            throw new BlockCreationException("RepeatingSequence block '" + blockName +
                "' is missing required parameter 'OutputValues' (or legacy 'rep_seq_y')");
        }
        return new Parameter(null, 2, "OutputValues", outputValuesStr);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as parameter");
        return new Parameter(null, 4, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(RepeatingSequence block, Parameter... parameters) {
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
        identity.put("blockType", "RepeatingSequence");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        outputPortList.get(0).setHeight(1);
        outputPortList.get(0).setWidth(1);
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Note: TemplateUtils.populateAllContext() already adds Parameter objects as *Object variables
        // No need to manually add them - they're already in context

        String codeStr = TemplateManager.renderTemplate("c/source/RepeatingSequence/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Note: TemplateUtils.populateAllContext() already adds:
        // - $TimeValuesObject → Parameter object (has .getInitCodeC() method)
        // - $OutputValuesObject → Parameter object (has .getInitCodeC() method)
        // - $TimeValues → C variable name string
        // - $OutputValues → C variable name string
        // So we don't need to manually add them here.
        
        context.put("TimeValuesObject", timeValues);
        context.put("OutputValuesObject", outputValues);

        context.put("OutputValues", outputValues.getName());
        context.put("TimeValues", timeValues.getName());

        String codeStr = TemplateManager.renderTemplate("c/source/RepeatingSequence/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("TimeValuesObject", timeValues);
        context.put("OutputValuesObject", outputValues);

        context.put("OutputValues", outputValues.getName());
        context.put("TimeValues", timeValues.getName());

        context.put("TimeValuesHeight", timeValues.getHeight());
        context.put("TimeValuesWidth", timeValues.getWidth());

        String codeStr = TemplateManager.renderTemplate("c/source/RepeatingSequence/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        if (timeValues.getWidth() != outputValues.getWidth()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }
        if (timeValues.getHeight() != 1 || outputValues.getHeight() != 1 || timeValues.getWidth() == 1 || outputValues.getWidth() == 1) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions must be 1*n!");
            throw(e);
        }
        // Allow row vectors (1×n matrices) but reject multi-row matrices
        if ((timeValues.getDataType() == DataType.MATRIX && timeValues.getHeight() != 1) || 
            (outputValues.getDataType() == DataType.MATRIX && outputValues.getHeight() != 1)) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input must be row vectors (1×n), not multi-dimensional matrices!");
            throw(e);
        }
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateOutput(double t) {
        double[] times = timeValues.getData().getDoubleArray();
        double[] values = outputValues.getData().getDoubleArray();

        double dt = t;
        while (dt > times[times.length - 1]) {
            dt -= times[times.length - 1];
        }

        double output = values[0]; // Default to first value
        for (int i = 0; i < times.length - 1; i++) {
            if (dt >= times[i] && dt < times[i + 1]) {
                // Linear interpolation between points
                double numerator = (dt - times[i]) * (values[i + 1] - values[i]);
                double denominator = times[i + 1] - times[i];

                output = values[i] + numerator / denominator;
                break;
            }
        }
        outputPortList.get(0).getOutputSignalC().setValue(output);
    }

    @Override
    public void calculateInit() {
        double[] times = timeValues.getData().getDoubleArray();
        double[] values = outputValues.getData().getDoubleArray();
        double initialOutput = values[0];
        outputPortList.get(0).getOutputSignalC().setValue(initialOutput);
    }
}