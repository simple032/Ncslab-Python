package com.ncslab.block.discontinuous;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
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
 * DeadZone block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - StartOfDeadZone: Start value of the dead zone
 * - EndOfDeadZone: End value of the dead zone
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class DeadZone extends Block {
    // Legacy fields for backward compatibility
    Parameter lowerValue;
    Parameter upperValue;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter startOfDeadZone;
    private final Parameter endOfDeadZone;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("LowerValue", "-0.5");       // StartOfDeadZone
        PARAMETER_DEFAULTS.put("UpperValue", "0.5");        // EndOfDeadZone
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Add a method to calculate lower value indices
    private int[] calculateLowerValueIndices(int height, int width) {
        int[] indices = new int[height * width];
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                indices[index++] = i * width + j;
            }
        }
        return indices;
    }

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    // === Private Constructor with Typed Parameters ===
    private DeadZone(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Get parameters by name from the automatically populated parameterList (via parseParameterList())
        this.startOfDeadZone = getParameterByName("LowerValue"); // LowerValue -> StartOfDeadZone
        this.endOfDeadZone = getParameterByName("UpperValue"); // UpperValue -> EndOfDeadZone
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Legacy field mapping for backward compatibility
        this.lowerValue = this.startOfDeadZone;
        this.upperValue = this.endOfDeadZone;
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DeadZone(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically
        
        // Get parameters by name from the automatically populated parameterList
        this.startOfDeadZone = getParameterByName("LowerValue"); // LowerValue -> StartOfDeadZone
        this.endOfDeadZone = getParameterByName("UpperValue"); // UpperValue -> EndOfDeadZone
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Legacy field mapping for backward compatibility
        this.lowerValue = this.startOfDeadZone;
        this.upperValue = this.endOfDeadZone;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates DeadZone block directly from BlockDto DTO
     */
    public DeadZone(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.startOfDeadZone = new Parameter(this, 1, "Startofdeadzone", "0");
        this.endOfDeadZone = new Parameter(this, 2, "Endofdeadzone", "0");
        this.sampleTime = new Parameter(this, 3, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
        initializePorts();
        
        // Legacy field mapping for backward compatibility
        this.lowerValue = this.startOfDeadZone;
        this.upperValue = this.endOfDeadZone;

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * DTO Constructor - Creates DeadZone block from DeadZoneDto with proper parameter mapping
     */
    public DeadZone(com.ncslab.dto.block.specialized.discontinuous.DeadZoneDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO
        this.startOfDeadZone = new Parameter(this, 1, "LowerValue", String.valueOf(dto.getStartOfDeadZoneValue()));
        this.endOfDeadZone = new Parameter(this, 2, "UpperValue", String.valueOf(dto.getEndOfDeadZoneValue()));
        this.sampleTime = new Parameter(this, 3, "SampleTime", String.valueOf(dto.getSampleTime() != null ? dto.getSampleTime() : -1.0));
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", dto.getOutDataTypeStrValue());
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", dto.getSaturateOnIntegerOverflowValue() ? "on" : "off");

        // Legacy field mapping for backward compatibility
        this.lowerValue = this.startOfDeadZone;
        this.upperValue = this.endOfDeadZone;

        // Initialize ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));

        System.out.println("DTO: " + getClass().getSimpleName() + " block created from DeadZoneDto - " + dto.getBlockName());
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static DeadZone fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            
            return new DeadZone(blockName, blockPath, blockUUID, model);
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DeadZone block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static DeadZone create(String name, String path, String startOfDeadZone, String endOfDeadZone, NCSLabModel model) {
        return create(name, path, startOfDeadZone, endOfDeadZone, -1.0, "Inherit: Same as input", false, model);
    }

    public static DeadZone create(String name, String path, String startOfDeadZone, String endOfDeadZone,
                                 double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Create a JSONObject with parameter values for centralized parsing
        JSONObject paramValues = new JSONObject();
        paramValues.put("LowerValue", startOfDeadZone);
        paramValues.put("UpperValue", endOfDeadZone);
        paramValues.put("SampleTime", String.valueOf(sampleTime));
        paramValues.put("OutDataTypeStr", outDataType);
        paramValues.put("SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        JSONObject blockJSON = createBlockIdentity(name, path, "null");
        blockJSON.put("paramValues", paramValues);
        
        return new DeadZone(blockJSON, model);
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
        identity.put("blockType", "DeadZone");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
}
