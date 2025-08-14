package com.ncslab.block.discontinuous;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;
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
import java.util.Vector;

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
public class RateLimiter extends Block {
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

    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
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
     * DTO-NATIVE Constructor - Creates RateLimiter block directly from BlockJson DTO
     */
    public RateLimiter(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.risingSlew = new Parameter(this, 1, "Risingslew", "0");
        this.fallingSlew = new Parameter(this, 2, "Fallingslew", "0");
        this.sampleTime = new Parameter(this, 3, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
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

    // === Static Factory Method for Programmatic Creation ===
    public static RateLimiter create(String name, String path, String risingSlew, String fallingSlew, NCSLabModel model) {
        return create(name, path, risingSlew, fallingSlew, -1.0, "Inherit: Same as input", false, model);
    }

    public static RateLimiter create(String name, String path, String risingSlew, String fallingSlew,
                                    double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Create a JSONObject with parameter values for centralized parsing
        JSONObject paramValues = new JSONObject();
        paramValues.put("UpperLimit", risingSlew);
        paramValues.put("LowerLimit", fallingSlew);
        paramValues.put("SampleTime", String.valueOf(sampleTime));
        paramValues.put("OutDataTypeStr", outDataType);
        paramValues.put("SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        JSONObject blockJSON = createBlockIdentity(name, path, "null");
        blockJSON.put("paramValues", paramValues);
        
        return new RateLimiter(blockJSON, model);
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
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal", signal);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/RateLimiter/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("upperLimit", upperLimit);
        context.put("lowerLimit", lowerLimit);

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/RateLimiter/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("upperLimit", upperLimit);
        context.put("lowerLimit", lowerLimit);
        context.put("outputs", getOutputPortVariables());
        context.put("inputs", getInputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/RateLimiter/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("upperLimit", upperLimit);
        context.put("lowerLimit", lowerLimit);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/RateLimiter/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal outputSignal = outputPortList.get(0).getOutputSignalC();
        
        context.put("block", this);
        context.put("upperLimit", upperLimit);
        context.put("lowerLimit", lowerLimit);
        context.put("signal", signal);
        context.put("outputSignal", outputSignal);
        context.put("outputs", getOutputPortVariables());
        context.put("inputs", getInputPortVariables());

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
