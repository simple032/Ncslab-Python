package com.ncslab.block.source;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all source blocks with common SIMULINK-compatible parameters.
 * 
 * Common SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public abstract class SourceBlock extends Block {

    // === Common SIMULINK-Compatible Parameters ===
    @Getter
    protected final Parameter sampleTime;
    @Getter
    protected final Parameter outDataType;
    @Getter
    protected final Parameter saturateOnIntegerOverflow;

    // === Common Parameter Defaults ===
    public static final Map<String, String> COMMON_PARAMETER_DEFAULTS;
    static {
        COMMON_PARAMETER_DEFAULTS = new HashMap<>();
        COMMON_PARAMETER_DEFAULTS.put("SampleTime", "0");  // Continuous by default
        COMMON_PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        COMMON_PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // All source blocks have one output port
        outputNames.add("out1");
        // Source blocks typically have no input ports
        
        // Input port defaults (source blocks have no inputs)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        
        // Output port defaults (source blocks have one output)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    /**
     * Protected constructor for source blocks with common parameters
     */
    protected SourceBlock(String blockType, Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                         String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockType, blockName, blockPath, blockUUID), model);
        
        // Assign common parameters
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "SaturateOnIntegerOverflow parameter cannot be null");
        
        // Add common parameters to the block's parameter list
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
        // Create standard output port
        outputPortList.add(new OutputPort(this, 1));
    }

    /**
     * DTO-NATIVE Constructor - Creates SourceBlock directly from BlockDto DTO
     */
    protected SourceBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        
        // Get common parameters from already-parsed parameterList (from Block constructor)
        sampleTime = getParameterByName("SampleTime");
        outDataType = getParameterByName("OutDataTypeStr");
        saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add parameters if they weren't already added by parseParameterList        
        
        // Create standard output port
        outputPortList.add(new OutputPort(this, 1));        
        
        System.out.println("DTO-NATIVE: SourceBlock created successfully - " + blockDto.getBlockName());
    }

    /**
     * Legacy constructor for compatibility with existing JSON-based instantiation
     */
    protected SourceBlock(JSONObject blockJson, NCSLabModel model) {
        super(blockJson, model);
        
        // Get common parameters from already-parsed parameterList (from Block constructor)
        Parameter sampleTimeParam = getParameterByName("SampleTime");
        Parameter outDataTypeParam = getParameterByName("OutDataTypeStr");
        Parameter saturateParam = getParameterByName("SaturateOnIntegerOverflow");
        
        // If any common parameters are missing, create them with defaults
        if (sampleTimeParam == null) {
            sampleTimeParam = new Parameter(this, getNextParameterId(), "SampleTime", 
                COMMON_PARAMETER_DEFAULTS.get("SampleTime"));
            parameterList.add(sampleTimeParam);
        }
        if (outDataTypeParam == null) {
            outDataTypeParam = new Parameter(this, getNextParameterId(), "OutDataTypeStr", 
                COMMON_PARAMETER_DEFAULTS.get("OutDataTypeStr"));
            parameterList.add(outDataTypeParam);
        }
        if (saturateParam == null) {
            saturateParam = new Parameter(this, getNextParameterId(), "SaturateOnIntegerOverflow", 
                COMMON_PARAMETER_DEFAULTS.get("SaturateOnIntegerOverflow"));
            parameterList.add(saturateParam);
        }
        
        // Assign to final fields
        this.sampleTime = sampleTimeParam;
        this.outDataType = outDataTypeParam;
        this.saturateOnIntegerOverflow = saturateParam;
        
        // Create standard output port
        outputPortList.add(new OutputPort(this, 1));
    }
    
    /**
     * Helper method to get next available parameter ID
     */
    private int getNextParameterId() {
        return parameterList.size() + 1;
    }  

    /**
     * Helper method to merge common defaults with block-specific defaults
     */
    private static Map<String, String> mergeWithCommonDefaults(Map<String, String> blockSpecificDefaults) {
        Map<String, String> merged = new HashMap<>(COMMON_PARAMETER_DEFAULTS);
        
        for (Map.Entry<String, String> entry : blockSpecificDefaults.entrySet()) {
            merged.put(entry.getKey(), entry.getValue());
        }
        return merged;
    }


    /**
     * Get the sample time value as double
     */
    public double getSampleTimeValue() {
        try {
            return sampleTime.getValue();
        } catch (NumberFormatException e) {
            return 0.0; // Default to continuous
        }
    }

    /**
     * Check if this is a continuous time source block
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Check if this is a discrete time source block
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    /**
     * Check if sample time is inherited
     */
    public boolean isInheritedSampleTime() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Helper method to create block identity JSON for source blocks
     */
    protected static JSONObject createBlockIdentity(String blockType, String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", blockType);
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }
}