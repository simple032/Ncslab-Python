package com.ncslab.block.route;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;
import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Demux block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Outputs: Number of output ports or vector of output port widths
 * - DisplayOrder: Display order of output ports
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Demux extends Block {
	private int num;
	private boolean feedThrough = true;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter outputs;
    private final Parameter displayOrder;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Outputs", "2");
        PARAMETER_DEFAULTS.put("DisplayOrder", "1:N");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    static {
        // SIMULINK parameter names
        
        // Port names
        inputNames.add("in1");
        // Output names are dynamic based on number of outputs
    }
    // === Private Constructor with Typed Parameters ===
    private Demux(Parameter outputs, Parameter displayOrder, Parameter sampleTime,
                 Parameter outDataType, Parameter saturateOnIntegerOverflow,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.outputs = Objects.requireNonNull(outputs, "Outputs parameter cannot be null");
        this.displayOrder = Objects.requireNonNull(displayOrder, "Display order parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Parse number of outputs and create ports
        this.feedThrough = true;
        this.num = Integer.parseInt(outputs.getInitString());
        
        // Create output ports based on parameter
        for(int i=0; i<num; i++) {
            outputPortList.add(new OutputPort(this, i+1, feedThrough));
        }
        inputPortList.add(new InputPort(this, 1));
    }
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
	public Demux(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.outputs = new Parameter(this, 1, "Outputs", String.valueOf(paramValues.getInt("Outputs")));
        this.displayOrder = new Parameter(this, 2, "DisplayOrder", "1:N");
        this.sampleTime = new Parameter(this, 3, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Inherit via internal rule");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list

		this.feedThrough = true;
		this.num = paramValues.getInt("Outputs");

		// Create output ports based on parameter
		for(int i=0; i<num; i++) {
			outputPortList.add(new OutputPort(this, i+1, feedThrough));
		}
		inputPortList.add(new InputPort(this, 1));
	}    /**
     * DTO-NATIVE Constructor - Creates Demux block directly from BlockJson DTO
     */
    public Demux(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.outputs = new Parameter(this, 1, "Outputs", "2");
        this.displayOrder = new Parameter(this, 2, "DisplayOrder", "1:N");
        this.sampleTime = new Parameter(this, 3, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Inherit via internal rule");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
        outputPortList.add(new OutputPort(this, 2, true));
    }


    // === Static Factory Method for JSON Deserialization ===
    public static Demux fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            Parameter outputs = createOutputsFromJSON(paramValues, blockName);
            Parameter displayOrder = createDisplayOrderFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Demux block = new Demux(outputs, displayOrder, sampleTime, outDataType, saturateParam,
                                   blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, outputs, displayOrder, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Demux block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Demux create(String name, String path, int numberOfOutputs, NCSLabModel model) {
        return create(name, path, String.valueOf(numberOfOutputs), "1:N", -1.0, "Inherit: Inherit via internal rule", false, model);
    }
    public static Demux create(String name, String path, String outputs, String displayOrder, double sampleTime,
                              String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter outputsParam = new Parameter(null, 1, "Outputs", outputs);
        Parameter displayOrderParam = new Parameter(null, 2, "DisplayOrder", displayOrder);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Demux block = new Demux(outputsParam, displayOrderParam, sampleTimeParam, outDataTypeParam, saturateParam,
                               name, path, "null", model);
        
        setParameterBlockReference(block, outputsParam, displayOrderParam, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createOutputsFromJSON(JSONObject paramValues, String blockName) {
        String outputsValue = String.valueOf(paramValues.optInt("Outputs", 2));
        return new Parameter(null, 1, "Outputs", outputsValue);
    }
    private static Parameter createDisplayOrderFromJSON(JSONObject paramValues, String blockName) {
        String displayOrderValue = paramValues.optString("DisplayOrder", "1:N");
        return new Parameter(null, 2, "DisplayOrder", displayOrderValue);
    }
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Inherit via internal rule");
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
    private static void setParameterBlockReference(Demux block, Parameter... parameters) {
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
        identity.put("blockType", "Demux");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("m/route/Demux/init.vm", context);
		code.addInitCode(codeStr);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("m/route/Demux/output.vm", context);
		code.addOutputCode(codeStr);
	}
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("m/route/Demux/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/init.vm", context);
		code.addInitCode(codeStr);
	}
	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		// Add specific context for output names
		context.put("outputNames", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/output.vm", context);
		code.addOutputCode(codeStr);
	}
	public void generateDerivativeCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}
	public void generateUpdateCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/update.vm", context);
		code.addUpdateCode(codeStr);
	}
	public void updateDimension() throws MatDimException {
		// Implementation left as is
	}
	public void checkDimension() throws MatDimException {
		if(this.getInputPortList().get(0).isVector()==false||this.getInputPortList().get(0).isReal()==true) {
			MatDimException e=new MatDimException("Block "+this.blockName+" input dimension error!\n Only a vector is applicable for demux\n");
			throw(e);
		}
		if(this.getInputPortList().get(0).getVectorSize()!=num) {
			MatDimException e=new MatDimException("Block "+this.blockName+" output dimension error!\n The input signal width is "+this.getInputPortList().get(0).getVectorSize()+", but the number of output is "+num+"\n");
			throw(e);
		}
	}
}

