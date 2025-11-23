package com.ncslab.block.route;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.route.DemuxDto;
import com.ncslab.block.route.RouteBlock;
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
public class Demux extends RouteBlock {
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
        this.outputs = getParameterByName("Outputs");
        this.displayOrder = getParameterByName("DisplayOrder");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list

		this.feedThrough = true;
		this.num = outputs.getData().getIntValue();

		// Create output ports based on parameter
		for(int i=0; i<num; i++) {
			outputPortList.add(new OutputPort(this, i+1, feedThrough));
		}
		inputPortList.add(new InputPort(this, 1));
	}    /**
     * DTO-NATIVE Constructor - Creates Demux block directly from DemuxDto DTO
     */
    public Demux(DemuxDto demuxDto, NCSLabModel model) {
        super(demuxDto, model);

        // Initialize final parameters from DemuxDto
        this.outputs = getParameterByName("Outputs");
        this.displayOrder = getParameterByName("DisplayOrder");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Parse number of outputs and create ports
        this.feedThrough = true;
        this.num = this.outputs.getData().getIntValue();
        
        // Create output ports based on parameter
        for(int i=0; i<num; i++) {
            outputPortList.add(new OutputPort(this, i+1, feedThrough));
        }
        inputPortList.add(new InputPort(this, 1));

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + demuxDto.getBlockName());
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
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Demux create(String name, String path, int numberOfOutputs, NCSLabModel model) {
        return create(name, path, numberOfOutputs, "1:N", -1.0, "Inherit: Inherit via internal rule", false, model);
    }

    /**
     * Create a Demux block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param numberOfOutputs Number of output ports (minimum 2)
     * @param displayOrder Display order of output ports (1:N, N:1, or custom)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Demux block instance
     */
    public static Demux create(String name, String path, int numberOfOutputs, String displayOrder, double sampleTime,
                              String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DemuxDto dto = DemuxDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .outputs(com.ncslab.dto.common.TypedParameter.of(numberOfOutputs))
            .displayOrder(com.ncslab.dto.common.TypedParameter.of(displayOrder))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow ? "on" : "off"))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Demux parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Demux(dto, model);
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

		// Add input variable name for the template
		if (!inputPortList.isEmpty()) {
			context.put("inputName", getInputPortVariable(0));
		}

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/output.vm", context);
		code.addOutputCode(codeStr);
	}
    
	public void updateDimension() throws MatDimException {
		// Demux splits a vector input into multiple scalar outputs
		// Input: vector of size N
		// Outputs: num outputs, each gets N/num elements (or 1 element if N == num)

		InputPort inputPort = inputPortList.get(0);
		int inputVectorSize = inputPort.getVectorSize();

		// Calculate elements per output port
		// For simple case: input vector size should equal number of outputs
		// Each output gets 1 element
		if (inputVectorSize == num) {
			// Each output port gets a scalar (1x1)
			for (OutputPort outputPort : outputPortList) {
				outputPort.setHeight(1);
				outputPort.setWidth(1);
				outputPort.getOutputSignalC().setHeight(1);
				outputPort.getOutputSignalC().setWidth(1);
				outputPort.getOutputSignalC().setDataType(DataType.REAL);
			}
		} else {
			// General case: distribute elements across outputs
			// This could handle cases where vector size > num or vector size < num
			int elementsPerOutput = inputVectorSize / num;
			int remainingElements = inputVectorSize % num;

			for (int i = 0; i < outputPortList.size(); i++) {
				OutputPort outputPort = outputPortList.get(i);
				int elements = elementsPerOutput + (i < remainingElements ? 1 : 0);

				if (elements == 1) {
					// Scalar output
					outputPort.setHeight(1);
					outputPort.setWidth(1);
					outputPort.getOutputSignalC().setHeight(1);
					outputPort.getOutputSignalC().setWidth(1);
					outputPort.getOutputSignalC().setDataType(DataType.REAL);
				} else {
					// Vector output (column vector)
					outputPort.setHeight(elements);
					outputPort.setWidth(1);
					outputPort.getOutputSignalC().setHeight(elements);
					outputPort.getOutputSignalC().setWidth(1);
					outputPort.getOutputSignalC().setDataType(DataType.MATRIX);
				}
			}
		}
	}
	public void checkDimension() throws MatDimException {
		InputPort inputPort = this.getInputPortList().get(0);

		// Debug: Print Demux input dimensions and connected block info
		if ("Demux1".equals(blockName)) {
			System.out.println("========================================");
			System.out.println("Demux1 checkDimension - Input dimensions:");
			System.out.println("  Height: " + inputPort.getHeight());
			System.out.println("  Width: " + inputPort.getWidth());
			System.out.println("  isVector(): " + inputPort.isVector());
			System.out.println("  isReal(): " + inputPort.isReal());
			System.out.println("  VectorSize: " + inputPort.getVectorSize());

			// Check connected block
			if (inputPort.getLinkedLine() != null) {
				OutputPort sourcePort = inputPort.getLinkedLine().getLinkedOutputPort();
				if (sourcePort != null) {
					System.out.println("  Connected to: " + sourcePort.getBlock().getBlockName());
					System.out.println("  Source output dimensions: [" +
						sourcePort.getHeight() + "×" + sourcePort.getWidth() + "]");
				}
			}
			System.out.println("========================================");
		}

		// Check that input is a vector (not a scalar)
		if(inputPort.isVector()==false || inputPort.isReal()==true) {
			MatDimException e=new MatDimException(
            String.format(
            "Block %s input has invalid dimension [%dx%d]!\n Only a vector is applicable for demux\n", blockName, inputPort.getHeight(), inputPort.getWidth()));
			throw(e);
		}

		int inputVectorSize = inputPort.getVectorSize();

		// SIMULINK behavior: Allow flexible distribution
		// - If inputVectorSize == num: Each output gets 1 element (strict mode)
		// - If inputVectorSize > num: Elements are distributed across outputs (e.g., 6 elements into 2 outputs = 3 elements each)
		// - If inputVectorSize < num: This is an error (can't split N elements into more than N outputs)

		if(inputVectorSize < num) {
			MatDimException e=new MatDimException("Block "+this.blockName+" output dimension error!\n " +
				"The input signal width is "+inputVectorSize+", but the number of outputs is "+num+".\n" +
				"Cannot split "+inputVectorSize+" elements into "+num+" outputs (insufficient elements).\n");
			throw(e);
		}

		// Valid cases:
		// - inputVectorSize == num: Each output gets 1 element
		// - inputVectorSize > num: Elements distributed (e.g., 6 into 2 = 3 each)
		// Both are valid in SIMULINK, no exception needed
	}

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Demux block logic:
        // Takes a vector input and splits it into multiple scalar outputs
        // Each output port gets one element from the input vector
        
        // Get input data (vector)
        Data inputData = inputPortList.get(0).getData();
        
        // Handle different data types
        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - split into scalar/vector outputs
            for (int i = 0; i < num && i < outputPortList.size(); i++) {
                // Create output data for each port
                Data outputData = new Data(1, 1); // Scalar output
                
                // Extract element from matrix
                if (inputData.getMatrix() != null) {
                    // For row vector: extract column i
                    // For column vector: extract row i
                    if (inputData.getHeight() == 1) {
                        // Row vector - extract column i
                        if (i < inputData.getWidth()) {
                            double value = inputData.getMatrix().get(0, i);
                            outputData.setInitValue(value);
                        }
                    } else if (inputData.getWidth() == 1) {
                        // Column vector - extract row i
                        if (i < inputData.getHeight()) {
                            double value = inputData.getMatrix().get(i, 0);
                            outputData.setInitValue(value);
                        }
                    } else {
                        // General matrix - extract elements in row-major order
                        int totalElements = inputData.getHeight() * inputData.getWidth();
                        if (i < totalElements) {
                            int row = i / inputData.getWidth();
                            int col = i % inputData.getWidth();
                            double value = inputData.getMatrix().get(row, col);
                            outputData.setInitValue(value);
                        }
                    }
                }
                
                // Set output port data
                outputPortList.get(i).setData(outputData);
            }
        } else {
            // Scalar input - replicate to all outputs (edge case)
            double scalarValue = inputData.getInitValue();
            for (int i = 0; i < num && i < outputPortList.size(); i++) {
                Data outputData = new Data(1, 1);
                outputData.setInitValue(scalarValue);
                outputPortList.get(i).setData(outputData);
            }
        }
    }
}

