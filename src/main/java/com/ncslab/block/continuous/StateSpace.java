package com.ncslab.block.continuous;

import Jama.Matrix;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.continuous.StateSpaceDto;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.ncslab.block.continuous.ContinuousBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

/**
 * StateSpace block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - A: State matrix (n x n)
 * - B: Input matrix (n x m)
 * - C: Output matrix (p x n)
 * - D: Feedthrough matrix (p x m)
 * - X0: Initial state vector (n x 1)
 * - AbsoluteTolerance: Absolute tolerance for simulation
 * - ContinuousStateAttributes: Attributes for continuous states
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class StateSpace extends ContinuousBlock {
    // === Internal Implementation ===
    private boolean feedThrough = false;
    private State xState;
    private List<State> xStateList = new ArrayList<>();
    
    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter stateMatrix;
    private final Parameter inputMatrix;
    private final Parameter outputMatrix;
    private final Parameter feedthroughMatrix;
    private final Parameter initialState;
    private final Parameter absoluteTolerance;
    private final Parameter continuousStateAttributes;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Port References ===
    private OutputPort output;
    private InputPort input;
    
    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        
        // Parameter defaults for SISO state space systems
        PARAMETER_DEFAULTS.put("A", "1");
        PARAMETER_DEFAULTS.put("B", "1");
        PARAMETER_DEFAULTS.put("C", "1");
        PARAMETER_DEFAULTS.put("D", "0");
        PARAMETER_DEFAULTS.put("X0", "0");
        PARAMETER_DEFAULTS.put("AbsoluteTolerance", "auto");
        PARAMETER_DEFAULTS.put("ContinuousStateAttributes", "'''");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (feedthrough will be determined dynamically from D matrix)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false); // Default, will be updated based on D matrix
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private StateSpace(Parameter stateMatrix, Parameter inputMatrix, Parameter outputMatrix,
                      Parameter feedthroughMatrix, Parameter initialState, Parameter absoluteTolerance,
                      Parameter continuousStateAttributes, Parameter sampleTime, Parameter outDataType,
                      Parameter saturateOnIntegerOverflow, String blockName, String blockPath,
                      String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(stateMatrix, inputMatrix, outputMatrix, feedthroughMatrix, initialState, sampleTime);
        
        // Assign parameters
        this.stateMatrix = Objects.requireNonNull(stateMatrix, "State matrix parameter cannot be null");
        this.inputMatrix = Objects.requireNonNull(inputMatrix, "Input matrix parameter cannot be null");
        this.outputMatrix = Objects.requireNonNull(outputMatrix, "Output matrix parameter cannot be null");
        this.feedthroughMatrix = Objects.requireNonNull(feedthroughMatrix, "Feedthrough matrix parameter cannot be null");
        this.initialState = Objects.requireNonNull(initialState, "Initial state parameter cannot be null");
        this.absoluteTolerance = Objects.requireNonNull(absoluteTolerance, "Absolute tolerance parameter cannot be null");
        this.continuousStateAttributes = Objects.requireNonNull(continuousStateAttributes, "Continuous state attributes parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.stateMatrix);
        parameterList.add(this.inputMatrix);
        parameterList.add(this.outputMatrix);
        parameterList.add(this.feedthroughMatrix);
        parameterList.add(this.initialState);
        parameterList.add(this.absoluteTolerance);
        parameterList.add(this.continuousStateAttributes);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        // Determine feedthrough
        this.feedThrough = !feedthroughMatrix.isZero();
        
        // Initialize states and ports
        initializeStates();
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public StateSpace(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.stateMatrix = getParameterByName("A");
        this.inputMatrix = getParameterByName("B");
        this.outputMatrix = getParameterByName("C");
        this.feedthroughMatrix = getParameterByName("D");
        this.initialState = getParameterByName("X0");
        
        // Create missing SIMULINK parameters with defaults
        this.absoluteTolerance = getParameterByName("AbsoluteTolerance");
        this.continuousStateAttributes = getParameterByName("ContinuousStateAttributes");
        this.sampleTime = getParameterByName("SampleTime"); // 0 for continuous state space
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list if they exist
        if (this.stateMatrix != null) parameterList.add(this.stateMatrix);
        if (this.inputMatrix != null) parameterList.add(this.inputMatrix);
        if (this.outputMatrix != null) parameterList.add(this.outputMatrix);
        if (this.feedthroughMatrix != null) parameterList.add(this.feedthroughMatrix);
        if (this.initialState != null) parameterList.add(this.initialState);
        if (this.absoluteTolerance != null) parameterList.add(this.absoluteTolerance);
        if (this.continuousStateAttributes != null) parameterList.add(this.continuousStateAttributes);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        // Determine feedthrough
        if (feedthroughMatrix.isZero()) {
            feedThrough = false;
        } else {
            feedThrough = true;
        }
        
        // Initialize state
        xState = new State(this, 1, "x", stateMatrix.getWidth(), 1);
        xStateList.add(xState);
        stateList.add(xState);

        // Initialize ports
        input = new InputPort(this, 1);
        inputPortList.add(input);
        output = new OutputPort(this, 1, feedThrough);
        output.setHeight(outputMatrix.getHeight());
        outputPortList.add(output);
    }    /**
     * DTO-NATIVE Constructor - Creates StateSpace block directly from BlockDto DTO
     */
    public StateSpace(StateSpaceDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.stateMatrix = getParameterByName("A");
        this.inputMatrix = getParameterByName("B");
        this.outputMatrix = getParameterByName("C");
        this.feedthroughMatrix = getParameterByName("D");
        this.initialState = getParameterByName("X0");
        this.absoluteTolerance = getParameterByName("AbsoluteTolerance");
        this.continuousStateAttributes = getParameterByName("ContinuousStateAttributes");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.stateMatrix != null) parameterList.add(this.stateMatrix);
        if (this.inputMatrix != null) parameterList.add(this.inputMatrix);
        if (this.outputMatrix != null) parameterList.add(this.outputMatrix);
        if (this.feedthroughMatrix != null) parameterList.add(this.feedthroughMatrix);
        if (this.initialState != null) parameterList.add(this.initialState);
        if (this.absoluteTolerance != null) parameterList.add(this.absoluteTolerance);
        if (this.continuousStateAttributes != null) parameterList.add(this.continuousStateAttributes);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        // Initialize states and ports
        initializeStates();
        initializePorts();
        
        System.out.println("DTO-NATIVE: StateSpace block created successfully - " + blockDto.getBlockName());
    }

    // ===== DUAL CONSTRUCTOR PATTERN - MIGRATION SUPPORT =====
    // This pattern maintains backward compatibility while enabling DTO migration

    /**
     * Enhanced DTO-based constructor - preferred for new implementations
     * @param dto The DTO containing block configuration
     * @param model The parent model
     */

    
    // === Static Factory Method for JSON Deserialization ===
    public static StateSpace fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter stateMatrix = createStateMatrixFromJSON(paramValues, blockName);
            Parameter inputMatrix = createInputMatrixFromJSON(paramValues, blockName);
            Parameter outputMatrix = createOutputMatrixFromJSON(paramValues, blockName);
            Parameter feedthroughMatrix = createFeedthroughMatrixFromJSON(paramValues, blockName);
            Parameter initialState = createInitialStateFromJSON(paramValues, blockName);
            Parameter absoluteTolerance = createAbsoluteToleranceFromJSON(paramValues, blockName);
            Parameter continuousStateAttributes = createContinuousStateAttributesFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            StateSpace block = new StateSpace(stateMatrix, inputMatrix, outputMatrix,
                                             feedthroughMatrix, initialState, absoluteTolerance,
                                             continuousStateAttributes, sampleTime, outDataType,
                                             saturateParam, blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, stateMatrix, inputMatrix, outputMatrix,
                                     feedthroughMatrix, initialState, absoluteTolerance,
                                     continuousStateAttributes, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create StateSpace block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static StateSpace create(String name, String path, String A, String B, String C, String D, String X0, NCSLabModel model) {
        return create(name, path, A, B, C, D, X0, "auto", "'''", 0.0, "Inherit: Same as input", false, model);
    }
    
    public static StateSpace create(String name, String path, String A, String B, String C, String D, String X0,
                                   String absoluteTolerance, String continuousStateAttributes,
                                   double sampleTime, String outDataType, boolean saturateOnOverflow,
                                   NCSLabModel model) {
        Parameter stateMatrixParam = new Parameter(null, 1, "A", A);
        Parameter inputMatrixParam = new Parameter(null, 2, "B", B);
        Parameter outputMatrixParam = new Parameter(null, 3, "C", C);
        Parameter feedthroughMatrixParam = new Parameter(null, 4, "D", D);
        Parameter initialStateParam = new Parameter(null, 5, "X0", X0);
        Parameter absoluteToleranceParam = new Parameter(null, 6, "AbsoluteTolerance", absoluteTolerance);
        Parameter continuousStateAttributesParam = new Parameter(null, 7, "ContinuousStateAttributes", continuousStateAttributes);
        Parameter sampleTimeParam = new Parameter(null, 8, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 9, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 10, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        StateSpace block = new StateSpace(stateMatrixParam, inputMatrixParam, outputMatrixParam,
                                         feedthroughMatrixParam, initialStateParam, absoluteToleranceParam,
                                         continuousStateAttributesParam, sampleTimeParam, outDataTypeParam,
                                         saturateParam, name, path, "null", model);
        
        setParameterBlockReference(block, stateMatrixParam, inputMatrixParam, outputMatrixParam,
                                 feedthroughMatrixParam, initialStateParam, absoluteToleranceParam,
                                 continuousStateAttributesParam, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter stateMatrix, Parameter inputMatrix, Parameter outputMatrix,
                                         Parameter feedthroughMatrix, Parameter initialState, Parameter sampleTime) {
        // Validate that state matrix A is square
        if (stateMatrix.getWidth() != stateMatrix.getHeight()) {
            throw new IllegalArgumentException("State matrix A must be square");
        }
        
        // Validate dimensions compatibility
        int n = stateMatrix.getWidth(); // number of states
        int m = inputMatrix.getWidth();  // number of inputs  
        int p = outputMatrix.getHeight(); // number of outputs
        
        if (inputMatrix.getHeight() != n) {
            throw new IllegalArgumentException("Input matrix B height must match state matrix A size");
        }
        
        if (outputMatrix.getWidth() != n) {
            throw new IllegalArgumentException("Output matrix C width must match state matrix A size");
        }
        
        if (!feedthroughMatrix.isZero()) {
            if (feedthroughMatrix.getHeight() != p || feedthroughMatrix.getWidth() != m) {
                throw new IllegalArgumentException("Feedthrough matrix D dimensions must be " + p + "x" + m);
            }
        }
        
        if (initialState.getHeight() != n || initialState.getWidth() != 1) {
            throw new IllegalArgumentException("Initial state X0 must be " + n + "x1 vector");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createStateMatrixFromJSON(JSONObject paramValues, String blockName) {
        String stateMatrixValue = paramValues.optString("A", "[1]");
        return new Parameter(null, 1, "A", stateMatrixValue);
    }
    
    private static Parameter createInputMatrixFromJSON(JSONObject paramValues, String blockName) {
        String inputMatrixValue = paramValues.optString("B", "[1]");
        return new Parameter(null, 2, "B", inputMatrixValue);
    }
    
    private static Parameter createOutputMatrixFromJSON(JSONObject paramValues, String blockName) {
        String outputMatrixValue = paramValues.optString("C", "[1]");
        return new Parameter(null, 3, "C", outputMatrixValue);
    }
    
    private static Parameter createFeedthroughMatrixFromJSON(JSONObject paramValues, String blockName) {
        String feedthroughMatrixValue = paramValues.optString("D", "[0]");
        return new Parameter(null, 4, "D", feedthroughMatrixValue);
    }
    
    private static Parameter createInitialStateFromJSON(JSONObject paramValues, String blockName) {
        String initialStateValue = paramValues.optString("X0", "[0]");
        return new Parameter(null, 5, "X0", initialStateValue);
    }
    
    private static Parameter createAbsoluteToleranceFromJSON(JSONObject paramValues, String blockName) {
        String absoluteToleranceValue = paramValues.optString("AbsoluteTolerance", "auto");
        return new Parameter(null, 6, "AbsoluteTolerance", absoluteToleranceValue);
    }
    
    private static Parameter createContinuousStateAttributesFromJSON(JSONObject paramValues, String blockName) {
        String continuousStateAttributesValue = paramValues.optString("ContinuousStateAttributes", "'''");
        return new Parameter(null, 7, "ContinuousStateAttributes", continuousStateAttributesValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 8, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 9, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 10, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(StateSpace block, Parameter... parameters) {
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
        identity.put("blockType", "StateSpace");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        // Main input port
        input = new InputPort(this, 1);
        inputPortList.add(input);
        
        // Main output port (feedthrough depends on D matrix)
        output = new OutputPort(this, 1, feedThrough);
        output.setHeight(outputMatrix.getHeight());
        outputPortList.add(output);
    }
    
    // === State Initialization ===
    private void initializeStates() {
        xState = new State(this, 1, "x", stateMatrix.getWidth(), 1);
        xStateList.add(xState);
        stateList.add(xState);
    }
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		context.put("block", this);
		context.put("A", stateMatrix);
		context.put("B", inputMatrix);
		context.put("C", outputMatrix);
		context.put("D", feedthroughMatrix);
		context.put("X0", initialState);
		context.put("xState", xState);

		String codeStr = TemplateManager.renderTemplate("m/continuous/StateSpace/init.vm", context);
		code.addInitCode(codeStr);
	}
   public void generateOutputCodeM(CodeStructM code) {

		super.generateOutputCodeM(code);
		context.put("block", this);
		context.put("C", outputMatrix);
		context.put("D", feedthroughMatrix);
		context.put("xState", xState);
		context.put("feedThrough", feedThrough);

		String codeStr = TemplateManager.renderTemplate("m/continuous/StateSpace/output.vm", context);
		code.addOutputCode(codeStr);
	}
   public void generateDerivativeCodeM(CodeStructM code) {
	    super.generateDerivativeCodeM(code);
		context.put("block", this);
		context.put("A", stateMatrix);
		context.put("B", inputMatrix);
		context.put("xState", xState);
		context.put("inputVariable", this.getInputPortVariable(0));

		String codeStr = TemplateManager.renderTemplate("m/continuous/StateSpace/derivative.vm", context);
		code.addDerivativeCode(codeStr);
   }

   public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		context.put("block", this);
		context.put("A", stateMatrix);
		context.put("B", inputMatrix);
		context.put("C", outputMatrix);
		context.put("D", feedthroughMatrix);
		context.put("X0", initialState);
		context.put("xState", xState);
		
		// Add state name for template variable
		if (xState != null) {
			context.put("stateName", context.get(xState.getLocalName())); // Use state local name mapped by TemplateUtils // C variable name
		}

		String codeStr = TemplateManager.renderTemplate("c/continuous/StateSpace/init.vm", context);
		code.addInitCode(codeStr);
   }

   public void generateOutputCodeC(CodeStructC code) {
		super.generateOutputCodeC(code);
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		context.put("block", this);
		context.put("C", outputMatrix);
		context.put("D", feedthroughMatrix);
		context.put("xState", xState);
		context.put("feedThrough", feedThrough);
		
		// Add state name for template variable
		if (xState != null) {
			context.put("stateName", context.get(xState.getLocalName())); // Use state local name mapped by TemplateUtils // C variable name
		}

		String codeStr = TemplateManager.renderTemplate("c/continuous/StateSpace/output.vm", context);
		code.addOutputCode(codeStr);
   }

   public void generateDerivativeCodeC(CodeStructC code) {
   		super.generateDerivativeCodeC(code);
		context.put("block", this);
		context.put("A", stateMatrix);
		context.put("B", inputMatrix);
		context.put("xState", xState);

		String codeStr = TemplateManager.renderTemplate("c/continuous/StateSpace/derivative.vm", context);
		code.addDerivativeCode(codeStr);
   }

   public void updateDimension() throws MatDimException{
	    OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

		if(this.feedThrough) {
			if(stateMatrix.getWidth()!=stateMatrix.getHeight() // A must be square
					||stateMatrix.getHeight()!=inputMatrix.getHeight() // A and B must match
					||stateMatrix.getWidth()!=outputMatrix.getWidth() // A and C must match
					||stateMatrix.getWidth()!=xState.getHeight() // A and state must match
					||feedthroughMatrix.getWidth()!=inputMatrix.getWidth() // B and D must match
					||feedthroughMatrix.getHeight()!=outputMatrix.getHeight() // D and C must match
					||inputMatrix.getWidth()!=in.getHeight() // input and B must match
					||in.getWidth()!=1 // input must be column vector
					||initialState.getHeight()!=stateMatrix.getHeight()
					||initialState.getWidth()!=1
					) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!");
				throw(e);

			}
		}
		else {
			// If no D matrix, don't check D dimensions
			if(stateMatrix.getWidth()!=stateMatrix.getHeight()
					||stateMatrix.getHeight()!=inputMatrix.getHeight()
					||stateMatrix.getWidth()!=outputMatrix.getWidth()
					||stateMatrix.getWidth()!=xState.getHeight()
					//||inputMatrix.getWidth()!=in.getHeight()
					||in.getWidth()!=1
					||initialState.getHeight()!=stateMatrix.getHeight()
					||initialState.getWidth()!=1
					) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!");
				throw(e);

			}
		}

		// Set output dimensions based on parameters
		out.setHeight(outputMatrix.getHeight());
		out.setWidth(1);
		out.getOutputSignalC().setHeight(outputMatrix.getHeight());
		out.getOutputSignalC().setWidth(1);
		if(feedthroughMatrix.getHeight()>1) {
			out.getOutputSignalC().setDataType(DataType.MATRIX);
		}
		else {
			// TODO: if there is no D but the height of C > 1, the output should be a matrix
			out.getOutputSignalC().setDataType(DataType.REAL);
		}
   }

   public void checkDimension() throws MatDimException{
	   InputPort in  = inputPortList.get(0);
	   if(inputMatrix.getWidth()!=in.getHeight()) { // input and B must match
		   MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!");
		   throw(e);
	   }
   }

   @Override
    public void calculateDerivative(double t){
        Data data = stateMatrix.getData().times(xState.getData()).plus(inputMatrix.getData().times(input.getData()));
        xState.setDerivateData(data);
   }

    @Override
    public void calculateOutput(double t){
        Data data = outputMatrix.getData().times(xState.getData());
        if(feedThrough) {
            data = data.plus(feedthroughMatrix.getData().times(input.getData()));
        }
        output.setData(data);
    }

   @Override
    public void calculateInit(){
        if(xState.getData().getDataType()==DataType.REAL && stateMatrix.getHeight() > 1 && xState.getData().getInitValue()==0) {
            xState.setData(new Data(stateMatrix.getHeight(),1));
            xState.setDerivateData(new Data(stateMatrix.getHeight(),1));
        }else{
            xState.setData(initialState.getData());
            if(stateMatrix.getHeight()>1) {
                xState.setDerivateData(new Data(stateMatrix.getHeight(),1));
            }else{
                xState.setDerivateData(new Data(0));
            }
        }
   }
}
