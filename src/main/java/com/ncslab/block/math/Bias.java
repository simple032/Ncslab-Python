package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.BiasDto;
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
 * Bias block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Bias: Bias value to add to input signal
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Bias extends MathBlock {
    
    // === SIMULINK-Compatible Parameters ===
    private final Parameter bias;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Parameter Definitions ===
    
    // Parameter defaults
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Bias", "0");  // Default bias value
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
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
        
        // Output port defaults
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }
    
    // === Private Constructor with Typed Parameters ===
    private Bias(Parameter bias, Parameter sampleTime, Parameter outDataType, 
                Parameter saturateOnIntegerOverflow, String blockName, String blockPath, 
                String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(bias, sampleTime);

        // Assign parameters
        this.bias = Objects.requireNonNull(bias, "Bias parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameter list for TemplateUtils access
        // First update parameter block references and names
        setParameterBlockReference(this, bias, sampleTime, outDataType, saturateOnIntegerOverflow);
        bias.updateName();
        sampleTime.updateName();
        outDataType.updateName();
        saturateOnIntegerOverflow.updateName();

        parameterList.add(bias);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Bias(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Create legacy bias parameter
        this.bias = getParameterByName("Bias");
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }
    /**
     * BiasDto Constructor - Creates Bias block directly from BiasDto
     */
    public Bias(BiasDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Extract parameter values from DTO
        String biasValue = dto.getBias() != null ? dto.getBias().getAsString() : "0";
        String sampleTimeValue = dto.getSampleTime() != null ? dto.getSampleTime().getAsString() : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue() != null ? dto.getOutDataTypeStrValue() : "Inherit: Same as input";
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() != null && dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Initialize parameters with extracted values
        this.bias = getParameterByName("Bias");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Validate parameters
        validateParameters(this.bias, this.sampleTime);
        
        // Initialize ports
        initializePorts();
        
        System.out.println("BiasDto: " + getClass().getSimpleName() + " block created successfully from BiasDto - " + dto.getBlockName());
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Bias fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter bias = createBiasFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Bias block = new Bias(bias, sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, bias, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Bias block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a Bias block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param biasValue Bias value to add to input signal
     * @param model Parent model
     * @return Configured Bias block instance
     */
    public static Bias create(String name, String path, double biasValue, NCSLabModel model) {
        return create(name, path, biasValue, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a Bias block with full parameters using DTO-based construction.
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param biasValue Bias value to add to input signal
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Configured Bias block instance
     */
    public static Bias create(String name, String path, double biasValue, double sampleTime,
                             String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        BiasDto dto = BiasDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .bias(com.ncslab.dto.common.TypedParameter.of(biasValue))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Bias parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Bias(dto, model);
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter bias, Parameter sampleTime) {
        double biasValue = bias.getDouble();
        if (Double.isNaN(biasValue) || Double.isInfinite(biasValue)) {
            throw new IllegalArgumentException("Bias value must be finite");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createBiasFromJSON(JSONObject paramValues, String blockName) {
        String biasValue = paramValues.optString("Bias", "0");
        return new Parameter(null, 1, "Bias", biasValue);
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
    
    private static void setParameterBlockReference(Bias block, Parameter... parameters) {
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
        identity.put("blockType", "Bias");
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // String codeStr = TemplateManager.renderTemplate("c/math/Bias/init.vm", context);
        // code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/Bias/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateInit() {
        // Initialization logic for Bias block
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Bias block: adds bias value to input signal
        Data inputData = inputPortList.get(0).getData();
        double biasValue = bias.getData().getInitValue();
        Data outputData;
        
        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - add bias to each element
            Jama.Matrix inputMatrix = inputData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
            
            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    outputMatrix.set(i, j, value + biasValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input - add bias directly
            double inputValue = inputData.getInitValue();
            outputData = new Data(1, 1);
            outputData.setInitValue(inputValue + biasValue);
        }
        
        outputPortList.get(0).setData(outputData);
    }
}

