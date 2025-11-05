package com.ncslab.block.discontinuous;

import com.ncslab.block.discontinuous.DiscontinuousBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discontinuous.RateLimiterDto;
import java.util.HashMap;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
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
 * RateLimiter block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - RisingSlew: Rising slew rate limit
 * - FallingSlew: Falling slew rate limit
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class RateLimiter extends DiscontinuousBlock {
    // Legacy fields for backward compatibility
    Parameter lowerLimit;
    Parameter upperLimit;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter risingSlew;
    private final Parameter fallingSlew;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("UpperLimit", "1");         // RisingSlew
        PARAMETER_DEFAULTS.put("LowerLimit", "-1");       // FallingSlew
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
        
        // Output port defaults (rate limiter has feedthrough)
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
    private RateLimiter(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Get parameters by name from the automatically populated parameterList (via parseParameterList())
        this.risingSlew = getParameterByName("UpperLimit"); // UpperLimit -> RisingSlew
        this.fallingSlew = getParameterByName("LowerLimit"); // LowerLimit -> FallingSlew
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Legacy field mapping for backward compatibility
        this.upperLimit = this.risingSlew;
        this.lowerLimit = this.fallingSlew;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public RateLimiter(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically
        
        // Get parameters by name from the automatically populated parameterList
        this.risingSlew = getParameterByName("UpperLimit"); // UpperLimit -> RisingSlew
        this.fallingSlew = getParameterByName("LowerLimit"); // LowerLimit -> FallingSlew
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Legacy field mapping for backward compatibility
        this.upperLimit = this.risingSlew;
        this.lowerLimit = this.fallingSlew;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates RateLimiter block directly from BlockDto DTO
     */
    public RateLimiter(RateLimiterDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.risingSlew = getParameterByName("Risingslew");
        this.fallingSlew = getParameterByName("Fallingslew");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
        
        // Legacy field mapping for backward compatibility
        this.upperLimit = this.risingSlew;
        this.lowerLimit = this.fallingSlew;

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }// === Static Factory Method for JSON Deserialization ===
    public static RateLimiter fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            
            return new RateLimiter(blockName, blockPath, blockUUID, model);
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create RateLimiter block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static RateLimiter create(String name, String path, String risingSlew, String fallingSlew, NCSLabModel model) {
        return create(name, path, risingSlew, fallingSlew, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a RateLimiter block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param risingSlew Rising slew rate limit (units per second)
     * @param fallingSlew Falling slew rate limit (units per second, negative value)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return RateLimiter block instance
     */
    public static RateLimiter create(String name, String path, String risingSlew, String fallingSlew,
                                    double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        RateLimiterDto dto = RateLimiterDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .upperLimit(com.ncslab.dto.common.TypedParameter.of(risingSlew))
            .lowerLimit(com.ncslab.dto.common.TypedParameter.of(fallingSlew))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid RateLimiter parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new RateLimiter(dto, model);
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
        identity.put("blockType", "RateLimiter");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // Define arrays to save state
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("signal", signal);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/RateLimiter/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/RateLimiter/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/RateLimiter/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/RateLimiter/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add computed signal objects for template
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal outputSignal = outputPortList.get(0).getOutputSignalC();
        context.put("signal", signal);
        context.put("outputSignal", outputSignal);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/RateLimiter/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    @Override
    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }

    @Override
    public void calculateInit() {
        // Initialize output to input
        Data input = inputPortList.get(0).getData();
        outputPortList.get(0).getOutputSignalC().setData(input);
    }

    @Override
    public void calculateOutput(double t) {
        Data input = inputPortList.get(0).getData();
        Data previousOutput = outputPortList.get(0).getOutputSignalC().getData();
        
        double inputValue = input.getInitValue();
        double previousValue = previousOutput.getInitValue();
        double upperLimitValue = upperLimit.getData().getInitValue();
        double lowerLimitValue = lowerLimit.getData().getInitValue();
        
        // Apply rate limiting
        double difference = inputValue - previousValue;
        double limitedDifference;
        
        if (difference > upperLimitValue) {
            limitedDifference = upperLimitValue;
        } else if (difference < lowerLimitValue) {
            limitedDifference = lowerLimitValue;
        } else {
            limitedDifference = difference;
        }
        
        Data output = new Data(previousValue + limitedDifference);
        outputPortList.get(0).getOutputSignalC().setData(output);
    }
}
