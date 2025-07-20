package com.ncslab.block.discrete;

import com.ncslab.block.io.Parameter;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * Discrete_Transfer_Fcnz block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Discrete_Transfer_Fcnz extends DiscreteBlock{

    // === SIMULINK-Compatible Parameters ===
    private final Parameter sampleTimeParam;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Internal state ===
    private final boolean feedthrough = true; // Transfer function blocks have feedthrough

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
    }

    // === Private Constructor with Typed Parameters ===
    private Discrete_Transfer_Fcnz(Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Create ports
        inputPortList.add(new InputPort(this, 1));  // Input signal
        inputPortList.add(new InputPort(this, 2));  // Numerator coefficients
        inputPortList.add(new InputPort(this, 3));  // Denominator coefficients
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
	public Discrete_Transfer_Fcnz(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);
		
        // Create legacy parameters for backward compatibility
        this.sampleTimeParam = new Parameter(this, 1, "SampleTime", String.valueOf(paramValues.optDouble("SampleTime", -1)));
        
        // Create missing SIMULINK parameters with defaults
        this.outDataType = new Parameter(this, 2, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 3, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list
        
        // Create ports
		inputPortList.add(new InputPort(this, 1));  // Input signal
		inputPortList.add(new InputPort(this, 2));  // Numerator coefficients
		inputPortList.add(new InputPort(this, 3));  // Denominator coefficients
		outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
    }
    // === Static Factory Method for JSON Deserialization ===
    public static Discrete_Transfer_Fcnz fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            
            Discrete_Transfer_Fcnz block = new Discrete_Transfer_Fcnz(sampleTime, outDataType, saturateParam,
                                                                     blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Discrete_Transfer_Fcnz block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Discrete_Transfer_Fcnz create(String name, String path, double sampleTime, NCSLabModel model) {
        return create(name, path, sampleTime, "Inherit: Same as input", false, model);
    }
    public static Discrete_Transfer_Fcnz create(String name, String path, double sampleTime, String outDataType,
                                               boolean saturateOnOverflow, NCSLabModel model) {
        Parameter sampleTimeParam = new Parameter(null, 1, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 2, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 3, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Discrete_Transfer_Fcnz block = new Discrete_Transfer_Fcnz(sampleTimeParam, outDataTypeParam, saturateParam,
                                                                 name, path, "null", model);
        
        setParameterBlockReference(block, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = String.valueOf(paramValues.optDouble("SampleTime", -1.0));
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

    private static void setParameterBlockReference(Discrete_Transfer_Fcnz block, Parameter... parameters) {
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
        identity.put("blockType", "Discrete_Transfer_Fcnz");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
	//define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal3 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal1", signal1);
        context.put("signal3", signal3);

        String arraysCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("sampleTime", sampleTimeParam);

        String initCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal3 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("inputPortList", inputPortList);
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("signal1", signal1);
        context.put("signal2", signal2);
        context.put("signal3", signal3);

        String outputCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/output.vm", context);
        code.addOutputCode(outputCode);
    }

	 public void updateDimension() throws MatDimException{
		 super.updateDimension();
			OutputPort out  = outputPortList.get(0);
			OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal3=inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal2.getHeight()!=1||signal3.getHeight()!=1) {
				 MatDimException e=new MatDimException("The input port2 signal and input port3 signal of"+this.blockName+"must be Matrix(1*n)!\n");
				 throw(e);
			}
			if(signal2.getWidth()>signal3.getWidth()) {
				MatDimException e=new MatDimException("The order of the denominator must be greater than or equal to the order of the numerator.\n");
				 throw(e);
			}
			out.setHeight(signal1.getHeight());
			out.setWidth(signal1.getWidth());
			out.getOutputSignalC().setHeight(signal1.getHeight());
			out.getOutputSignalC().setWidth(signal1.getWidth());
			out.getOutputSignalC().setDataType(signal1.getDataType());
	}
	 public void checkDimension() throws MatDimException{
		 // No additional dimension checks needed for discrete transfer function
	}
}
