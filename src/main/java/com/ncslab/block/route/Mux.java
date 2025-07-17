package com.ncslab.block.route;

import Jama.Matrix;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * Mux block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Inputs: Number of input ports or vector of input port widths
 * - DisplayOrder: Display order of input ports
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Mux extends Block {
	private int num;

	private boolean feedThrough = true;

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter inputs;
    @Getter
    private final Parameter displayOrder;
    @Getter
    private final Parameter sampleTime;
    @Getter
    private final Parameter outDataType;
    @Getter
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "2");  // Number of inputs
        PARAMETER_DEFAULTS.put("DisplayOrder", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        parameterNames.add("Inputs");
        parameterNames.add("DisplayOrder");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
        // Port names
        outputNames.add("out1");
        // Input names are dynamic based on number of inputs
    }

    // === Private Constructor with Typed Parameters ===
    private Mux(Parameter inputs, Parameter displayOrder, Parameter sampleTime,
               Parameter outDataType, Parameter saturateOnIntegerOverflow,
               String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.inputs = Objects.requireNonNull(inputs, "Inputs parameter cannot be null");
        this.displayOrder = Objects.requireNonNull(displayOrder, "Display order parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Parse number of inputs and create ports
        this.num = Integer.parseInt(inputs.getInitString());
        
        // Create input ports based on parameter
        for(int i=0; i<num; i++) {
            inputPortList.add(new InputPort(this, i+1));
        }
        outputPortList.add(new OutputPort(this, 1, feedThrough));
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
	public Mux(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		// Create legacy parameters for backward compatibility
		this.inputs = new Parameter(this, 1, "Inputs", paramValues.getString("Inputs"));
		this.displayOrder = new Parameter(this, 2, "DisplayOrder", "1:N");
		this.sampleTime = new Parameter(this, 3, "SampleTime", "-1");
		this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Inherit via internal rule");
		this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");
		
		// Add all parameters to parameter list

		this.num = Integer.parseInt(paramValues.getString("Inputs"));

		// Create input ports based on parameter
		for(int i=0; i<num; i++) {
			inputPortList.add(new InputPort(this, i+1));
		}
		outputPortList.add(new OutputPort(this, 1, feedThrough));
	}
	
    // === Static Factory Method for JSON Deserialization ===
    public static Mux fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter inputs = createInputsFromJSON(paramValues, blockName);
            Parameter displayOrder = createDisplayOrderFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Mux block = new Mux(inputs, displayOrder, sampleTime, outDataType, saturateParam,
                               blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, inputs, displayOrder, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Mux block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Mux create(String name, String path, int numberOfInputs, NCSLabModel model) {
        return create(name, path, String.valueOf(numberOfInputs), "1:N", -1.0, "Inherit: Inherit via internal rule", false, model);
    }
    
    public static Mux create(String name, String path, String inputs, String displayOrder, double sampleTime,
                            String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter inputsParam = new Parameter(null, 1, "Inputs", inputs);
        Parameter displayOrderParam = new Parameter(null, 2, "DisplayOrder", displayOrder);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Mux block = new Mux(inputsParam, displayOrderParam, sampleTimeParam, outDataTypeParam, saturateParam,
                           name, path, "null", model);
        
        setParameterBlockReference(block, inputsParam, displayOrderParam, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInputsFromJSON(JSONObject paramValues, String blockName) {
        String inputsValue = paramValues.optString("Inputs", "2");
        return new Parameter(null, 1, "Inputs", inputsValue);
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
    
    private static void setParameterBlockReference(Mux block, Parameter... parameters) {
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
        identity.put("blockType", "Mux");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		context.put("block", this);
		context.put("outputs", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("m/route/Mux/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		context.put("block", this);
		context.put("inputs", getInputPortVariables());
		context.put("outputs", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("m/route/Mux/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		context.put("block", this);

		String codeStr = TemplateManager.renderTemplate("c/route/Mux/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("inputs", inputPortList);
		context.put("outputs", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("c/route/Mux/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		context.put("block", this);

		String codeStr = TemplateManager.renderTemplate("c/route/Mux/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}

	public void generateUpdateCodeC(CodeStructC code) {
		context.put("block", this);

		String codeStr = TemplateManager.renderTemplate("c/route/Mux/update.vm", context);
		code.addUpdateCode(codeStr);
	}

	public void updateDimension() throws MatDimException {
		//super.updateDimension();
		int size=0;
		for(InputPort inputPort:inputPortList) {
			if(inputPort.isVector()==true||inputPort.isReal()==true) {
				size+=inputPort.getVectorSize();
			}
			else {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension error!\n Matrix is not applicable for demux\n");
				throw(e);
			}
		}

		OutputPort output=getOutputPortList().get(0);
		output.setHeight(size);
		output.setWidth(1);
		output.getOutputSignalC().setHeight(size);
		output.getOutputSignalC().setWidth(1);
		output.getOutputSignalC().setDataType(DataType.MATRIX);
	}

	public void checkDimension() throws MatDimException {
		// Implementation can be added if needed
	}

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Matrix matrixResult = new Matrix(out.getHeight(), out.getWidth());
        int index = 0;
        for(int i=0; i<num; i++) {
            InputPort in = inputPortList.get(i);
            if(in.getHeight() > 1){
                for(int j=0; j<in.getVectorSize(); j++) {
                    matrixResult.set(index++, 0, in.getData().getMatrix().get(j, 0));
                }
            }else if(in.getWidth() > 1){
                for(int j=0; j<in.getVectorSize(); j++) {
                    matrixResult.set(index++, 0, in.getData().getMatrix().get(0, j));
                }
            }else {
                matrixResult.set(index++, 0, in.getData().getInitValue());
            }
        }
        out.setData(new Data(matrixResult));
    }
}
