package com.ncslab.block.route;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;
import Jama.Matrix;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.route.MuxDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * Mux (Multiplexer) block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * Combines multiple input signals into a single output vector by concatenating the inputs.
 * Supports both scalar and vector input signals with configurable input port ordering.
 * 
 * SIMULINK Parameters:
 * - Inputs: Number of input ports or vector of input port widths
 * - DisplayOrder: Display order of input ports (top-down, bottom-up)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Mux extends RouteBlock {
    // === Configuration ===
    /** Number of input ports */
    private int num;
    
    /** Feedthrough flag - Mux has direct feedthrough */
    private boolean feedThrough = true;
    
    // === SIMULINK-Compatible Parameters ===
    /** Input ports specification parameter */
    private final Parameter inputs;
    
    /** Display order parameter */
    private final Parameter displayOrder;
    
    /** Sample time parameter */
    private final Parameter sampleTime;
    
    /** Output data type specification parameter */
    private final Parameter outDataType;
    
    /** Integer overflow handling parameter */
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    
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

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SIMULINK parameter names
        
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
		this.inputs = getParameterByName("Inputs");
		this.displayOrder = getParameterByName("DisplayOrder");
		this.sampleTime = getParameterByName("SampleTime");
		this.outDataType = getParameterByName("OutDataTypeStr");
		this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
		
		// Add all parameters to parameter list

		this.num = Integer.parseInt(paramValues.getString("Inputs"));

		// Create input ports based on parameter
		for(int i=0; i<num; i++) {
			inputPortList.add(new InputPort(this, i+1));
		}
		outputPortList.add(new OutputPort(this, 1, feedThrough));
	}    /**
     * DTO-NATIVE Constructor - Creates Mux block directly from MuxDto DTO
     */
    public Mux(MuxDto muxDto, NCSLabModel model) {
        super(muxDto, model);

        // Initialize final parameters from MuxDto
        this.inputs = getParameterByName("Inputs");
        this.displayOrder = getParameterByName("DisplayOrder");
        this.sampleTime = getParameterByName("SampleTime");
        String outDataTypeValue = muxDto.getOutDataTypeStr() != null ? muxDto.getOutDataTypeStr().getAsString() : "Inherit: Same as input";
        this.outDataType = getParameterByName("OutDataTypeStr");

        String saturateValue = muxDto.getSaturateOnIntegerOverflow() != null ? muxDto.getSaturateOnIntegerOverflow().getAsString() : "off";
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Parse number of inputs and create ports
        // Read from parameterList (populated by parseParameterList), NOT from DTO TypedParameter
        this.num = Integer.parseInt(this.inputs.getInitString());
        // Create input ports based on parameter
        for(int i=0; i<num; i++) {
            inputPortList.add(new InputPort(this, i+1));
        }
        outputPortList.add(new OutputPort(this, 1, feedThrough));

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + muxDto.getBlockName());
    }
    
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, 1, true));
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
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Mux create(String name, String path, int numberOfInputs, NCSLabModel model) {
        return create(name, path, numberOfInputs, "1:N", -1.0, "Inherit: Inherit via internal rule", false, model);
    }

    /**
     * Create a Mux block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param numberOfInputs Number of input ports (minimum 2)
     * @param displayOrder Display order of input ports (1:N, N:1, or custom)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Mux block instance
     */
    public static Mux create(String name, String path, int numberOfInputs, String displayOrder, double sampleTime,
                            String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        MuxDto dto = MuxDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .inputs(com.ncslab.dto.common.TypedParameter.of(numberOfInputs))
            .displayOrder(com.ncslab.dto.common.TypedParameter.of(displayOrder))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow ? "on" : "off"))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Mux parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Mux(dto, model);
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
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("m/route/Mux/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("m/route/Mux/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("c/route/Mux/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		// Add input dimensions and data types for template
		java.util.List<Integer> inputHeights = new java.util.ArrayList<>();
		java.util.List<Integer> inputWidths = new java.util.ArrayList<>();
		java.util.List<DataType> inputDataTypes = new java.util.ArrayList<>();

		for (InputPort inputPort : inputPortList) {
			inputHeights.add(inputPort.getHeight());
			inputWidths.add(inputPort.getWidth());
			inputDataTypes.add(inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getDataType());
		}

		context.put("inputHeights", inputHeights);
		context.put("inputWidths", inputWidths);
		context.put("inputDataTypes", inputDataTypes);

		String codeStr = TemplateManager.renderTemplate("c/route/Mux/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void updateDimension() throws MatDimException {
		super.updateDimension();
		int size=0;
		CDataType outputCType = CDataType.DOUBLE; // Default
		boolean firstInput = true;

		for(int i=0; i<inputPortList.size(); i++) {
			InputPort inputPort = inputPortList.get(i);

			// Get dimensions from linked output signal, not from input port
			if (inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null) {
				OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

				// Calculate size based on signal dimensions
				int height = inputSignal.getHeight();
				int width = inputSignal.getWidth();
				boolean isScalar = (height == 1 && width == 1);
				boolean isVector = (height == 1 || width == 1);

				if (isScalar || isVector) {
					// Scalar or vector: add vector size
					int vectorSize = Math.max(height, width);
					size += vectorSize;
				} else {
					// Matrix: add all elements
					int elements = height * width;
					size += elements;
					System.out.println("MUX updateDimension: " + getBlockName() + " - Added " + elements + " matrix elements to size (now=" + size + ")");
				}

				// Determine output CDataType
				CDataType inputCType = inputSignal.getCDataType();
				if (firstInput) {
					outputCType = inputCType;
					firstInput = false;
				} else if (outputCType != inputCType) {
					// Mixed types - default to DOUBLE for safety
					outputCType = CDataType.DOUBLE;
				}
			}
		}

		// Ensure size is at least 1 (avoid 0-dimension matrices)
		if (size == 0) {
			throw new MatDimException("Mux block '" + getBlockName() + "': Cannot determine output dimensions. " +
				"Check that all input signals have valid dimensions.");
		}

		OutputPort output=getOutputPortList().get(0);
		output.setHeight(size);
		output.setWidth(1);
		output.getOutputSignalC().setHeight(size);
		output.getOutputSignalC().setWidth(1);
		output.getOutputSignalC().setDataType(DataType.MATRIX);
		// Set output CDataType based on input analysis
		output.getOutputSignalC().setCDataType(outputCType);
		System.out.println("MUX updateDimension: " + getBlockName() + " - Output dimensions set to [" + size + "×1], CDataType: " + outputCType);
	}

	public void checkDimension() throws MatDimException {
		// Implementation can be added if needed
	}

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Calculate total output size dynamically by summing all input vector sizes
        int totalSize = 0;
        for(int i=0; i<num; i++) {
            InputPort in = inputPortList.get(i);
            if(in.isVector() || in.isReal()) {
                totalSize += in.getVectorSize();
            } else {
                // Matrix inputs - add all elements
                totalSize += in.getHeight() * in.getWidth();
            }
        }

        // Create output matrix with correct dimensions [totalSize x 1]
        Matrix matrixResult = new Matrix(totalSize, 1);
        int index = 0;

        // Concatenate all inputs into output vector
        for(int i=0; i<num; i++) {
            InputPort in = inputPortList.get(i);

            if(in.getHeight() > 1) {
                // Column vector input [height x 1]
                for(int j=0; j<in.getHeight(); j++) {
                    matrixResult.set(index++, 0, in.getData().getMatrix().get(j, 0));
                }
            } else if(in.getWidth() > 1) {
                // Row vector input [1 x width]
                for(int j=0; j<in.getWidth(); j++) {
                    matrixResult.set(index++, 0, in.getData().getMatrix().get(0, j));
                }
            } else {
                // Scalar input
                matrixResult.set(index++, 0, in.getData().getInitValue());
            }
        }

        out.setData(new Data(matrixResult));
    }
}
