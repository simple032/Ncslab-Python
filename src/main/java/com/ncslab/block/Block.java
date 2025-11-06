package com.ncslab.block;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// External libraries
import lombok.Getter;
import lombok.Setter;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;

// Internal imports - Core
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;

// Internal imports - Block components
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.*;
import com.ncslab.block.lan.CCodeBlock;
import com.ncslab.block.lan.MCodeBlock;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.code.st.CodeStructST;
import com.ncslab.util.TemplateUtils;


/**
 * Base class for all block modules, defining the block framework.
 * Connects various language generator interfaces for multi-language code generation.
 * 
 * This class provides the foundation for all simulation blocks in the NCSLab system,
 * supporting both DTO-native construction and legacy JSON-based construction.
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Block implements MCodeBlock, CCodeBlock {

    // === Block Identity ===
    /** Block type identifier - used in BlockType factory for differentiated handling */
    @Getter
    protected String blockType;
    
    /** Block name identifier */    
    protected String blockName;
    public String getBlockName() {
        return this.blockName.replace("\t", "").replace("\n", "");
    }
    
    /** Unique block ID within the model */
    @Setter
    @Getter
    protected int blockId = 0;
    
    /** 
     * Block canvas position path.
     * Format: modelName for top-level blocks, modelName/subsystem for subsystem blocks
     */
    protected String blockPath;
    public String getBlockPath() {
        return this.blockPath.replace("\t", "").replace("\n", "");
    }
    
    /** Block UUID for unique identification (only BlockCId has true uniqueness) */
    @Getter
    protected String blockUUID = "null";
    
    /** 
     * Block parameters stored in native JSON format.
     * Different blocks have different parameters, so stored as raw JSON for flexibility.
     */
    @Getter
    protected JSONObject paramValues;

    // === Block Interface Lists ===
    /** Input port list for block connections */
    @Getter
    protected List<InputPort> inputPortList = new ArrayList<>();
    
    /** Output port list for block connections */
    @Getter
    protected List<OutputPort> outputPortList = new ArrayList<>();
    
    /** Parameter list for block configuration */
    @Getter
    protected List<Parameter> parameterList = new ArrayList<>();
    
    /** Continuous state list for differential equations */
    @Getter
    protected List<State> stateList = new ArrayList<>();
    
    /** Discrete state list for difference equations */
    @Getter
    protected List<State> dStateList = new ArrayList<>();
    
    /** Real work vector list for intermediate calculations */
    @Getter
    protected List<RWork> rworkList = new ArrayList<>();
    
    /** Global variable list for cross-block communication */
    @Getter
    protected List<GlobalVariable> globalVariableList = new ArrayList<>();
    
    /** Output signal list for code generation */
    protected List<OutputSignal> outputSignalList = new ArrayList<>();

    // === Block State Management ===
    /** 
     * Whether output code has been generated.
     * If true, traversal can reference directly without further processing.
     */
    protected boolean isOutputCodeGenerated = false;
    
    /** Whether dimension scanning has been completed */
    protected boolean isDimScaned = false;
    
    /** Reference to parent model */
    @Getter
    protected NCSLabModel model;
    
    /** Reference to parent system block */
    @Getter
    @Setter
    protected NCSLabSystem parent;
    
    /** Hardware block flag */
    protected boolean isHardware = false;
    
    /** 
     * Number of signals in the block.
     * Signals can be InputPort or OutputPort quantities, determined during code generation.
     */
    @Setter
    @Getter
    protected int signalNum = 0;
    // === Static Block Interface ===
    /** Input port names for template generation */
    @Getter
    public static List<String> inputNames = new ArrayList<>();
    
    /** Output port names for template generation */
    @Getter
    public static List<String> outputNames = new ArrayList<>();
    
    /** Velocity template context for code generation */
    protected final VelocityContext context = new VelocityContext();;
    
    // === Parameter Management ===
    
    /**
     * Retrieves parameter defaults from the derived class's static PARAMETER_DEFAULTS field.
     * Uses reflection to access the static field, allowing each block type to define its own defaults.
     * 
     * @return Map of parameter names to default values, empty map if no defaults found
     */
    protected Map<String, String> getParameterDefaults() {
        try {
            Class<?> clazz = this.getClass();
            java.lang.reflect.Field defaultsField = clazz.getField("PARAMETER_DEFAULTS");
            
            if (Map.class.isAssignableFrom(defaultsField.getType())) {
                @SuppressWarnings("unchecked")
                Map<String, String> defaults = (Map<String, String>) defaultsField.get(null);
                return defaults != null ? defaults : new HashMap<>();
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            // Graceful fallback for blocks without PARAMETER_DEFAULTS
            System.err.println("Cannot retrieve default parameters for " + this.getClass().getName());
        }
        
        return new HashMap<>();
    }

    /**
     * Retrieves a parameter by name instead of index-based access.
     * Provides safer parameter access with meaningful parameter names.
     * 
     * @param name Parameter name to search for
     * @return Parameter object if found, null otherwise
     */
    protected Parameter getParameterByName(String name) {
        return parameterList.stream()
            .filter(p -> p.getLocalName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retrieves input port defaults from the derived class's static INPUT_PORT_DEFAULTS field.
     * Uses reflection to access the static field, allowing each block type to define its own input port configurations.
     * 
     * @return List of input port configurations, empty list if no defaults found
     */
    protected List<Map<String, Object>> getInputPortDefaults() {
        try {
            Class<?> clazz = this.getClass();
            java.lang.reflect.Field defaultsField = clazz.getField("INPUT_PORT_DEFAULTS");
            
            if (List.class.isAssignableFrom(defaultsField.getType())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> defaults = (List<Map<String, Object>>) defaultsField.get(null);
                return defaults != null ? defaults : new ArrayList<>();
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            // Graceful fallback for blocks without INPUT_PORT_DEFAULTS
            System.err.println("Cannot retrieve input port defaults for " + this.getClass().getName());
        }
        
        return new ArrayList<>();
    }

    /**
     * Retrieves output port defaults from the derived class's static OUTPUT_PORT_DEFAULTS field.
     * Uses reflection to access the static field, allowing each block type to define its own output port configurations.
     * 
     * @return List of output port configurations, empty list if no defaults found
     */
    protected List<Map<String, Object>> getOutputPortDefaults() {
        try {
            Class<?> clazz = this.getClass();
            java.lang.reflect.Field defaultsField = clazz.getField("OUTPUT_PORT_DEFAULTS");
            
            if (List.class.isAssignableFrom(defaultsField.getType())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> defaults = (List<Map<String, Object>>) defaultsField.get(null);
                return defaults != null ? defaults : new ArrayList<>();
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            // Graceful fallback for blocks without OUTPUT_PORT_DEFAULTS
            System.err.println("Cannot retrieve output port defaults for " + this.getClass().getName());
        }
        
        return new ArrayList<>();
    }

    private void postConstructionInitialization() {}
    // === Constructors ===
    
    /**
     * DTO-Native Constructor - Creates Block from BlockDto DTO.
     * This constructor provides the foundation for DTO-native block creation,
     * offering improved type safety and validation over legacy JSON construction.
     * 
     * @param blockDto DTO containing block configuration data
     * @param model Parent model reference
     */
    protected Block(BlockDto blockDto, NCSLabModel model) {
        this.blockType = blockDto.getBlockType();
        this.blockName = blockDto.getBlockName();
        this.model = model;
        this.blockPath = blockDto.getBlockPath();
        this.blockUUID = blockDto.getBlockUUID() != null ? blockDto.getBlockUUID() : "null";

        // Initialize paramValues for legacy compatibility
        this.paramValues = new JSONObject();
        if (blockDto.getParamValues() != null) {
            // Convert DTO parameter map to JSONObject for legacy block compatibility
            for (Map.Entry<String, Object> entry : blockDto.getParamValues().entrySet()) {
                this.paramValues.put(entry.getKey(), entry.getValue());
            }
        }

        // Convert DTO's TypedParameter fields to Parameter objects using reflection
        // This enables DTO-based factory methods to work seamlessly
        // com.ncslab.dto.mapper.TypedParameterConverter.convertAndAddParameters(blockDto, this, 1);

        parseParameterList(blockDto);
        // parseInputOutputPorts(blockDto);
        // postConstructionInitialization();
        TemplateUtils.populateAllContext(context, this);
    }

    /**
     * Legacy JSON Constructor - Creates Block from JSONObject.
     * Maintained for backward compatibility with existing JSON-based workflows.
     * 
     * @param blockIn JSON object containing block configuration
     * @param model Parent model reference
     * @deprecated Use DTO-native constructor for new development
     */
    @Deprecated
    protected Block(JSONObject blockIn, NCSLabModel model) {
        this.blockType = blockIn.getString("blockType");
        this.blockName = blockIn.getString("blockName");
        
        if (blockIn.get("paramValues") instanceof JSONObject) {
            this.paramValues = blockIn.getJSONObject("paramValues");
        }
        
        this.model = model;
        this.blockPath = blockIn.getString("blockPath");
        this.blockUUID = blockIn.optString("blockUUID", "null");
        
        parseParameterList();
        // parseInputOutputPorts();
        // postConstructionInitialization();
        TemplateUtils.populateAllContext(context, this);
    }

    public int getStateNum(){        
        return stateList.size();
    }
    
    // === Public Block Interface Methods ===
    
    /**
     * Sets the feedthrough property for the first output port.
     * 
     * @param feedThrough True if the block has direct feedthrough from input to output
     */
    public void setFeedThrough(boolean feedThrough) {
        if (!outputPortList.isEmpty()) {
            this.outputPortList.get(0).setFeedThrough(feedThrough);
        }
    }
    
    /**
     * Checks if this block represents hardware functionality.
     * 
     * @return True if this is a hardware block
     */
    public boolean getIsHardware() {
        return this.isHardware;
    }
    
    /**
     * Determines if this block is a terminal block (sink with no outputs).
     * 
     * @return True if the block has no output ports
     */
    public boolean isTerminalBlock() {
        return outputPortList.isEmpty();
    }
    
    /**
     * Extracts the subsystem name from the block path.
     * 
     * @return Subsystem name (everything after the last '/')
     */
    public String getSubSystemName() {
        int index = this.blockPath.lastIndexOf("/");
        return this.blockPath.substring(index + 1);
    }

    /**
     * Checks if output code generation has been completed.
     * 
     * @return True if output code has been generated
     */
    public boolean getIsOutputCodeGenerated() {
        return this.isOutputCodeGenerated;
    }
    
    /**
     * Checks if dimension scanning has been completed.
     * 
     * @return True if dimensions have been scanned
     */
    public boolean getIsDimScaned() {
        return this.isDimScaned;
    }
    
    /**
     * Gets the variable name for the specified input port.
     * 
     * @param n Input port index
     * @return Variable name for the input port, or null if index is invalid or port not connected
     */
    public String getInputPortVariable(int n) {
        // Check bounds and list state
        if (inputPortList == null || n < 0 || n >= inputPortList.size()) {
            System.err.println("Warning: Input port index " + n + " is out of bounds for block " + 
                             blockName + " (ID: " + blockId + "). InputPort list size: " + 
                             (inputPortList != null ? inputPortList.size() : "null"));
            return null;
        }
        
        InputPort inputPort = inputPortList.get(n);
        if (inputPort == null) {
            System.err.println("Warning: Input port at index " + n + " is null for block " + 
                             blockName + " (ID: " + blockId + ")");
            return null;
        }
        
        if (inputPort.getLinkedLine() == null) {
            System.err.println("Warning: Input port " + n + " has no linked line for block " + 
                             blockName + " (ID: " + blockId + ")");
            return null;
        }
        
        if (inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            System.err.println("Warning: Input port " + n + " linked line has no output port for block " + 
                             blockName + " (ID: " + blockId + ")");
            return null;
        }
        
        if (inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC() == null) {
            System.err.println("Warning: Input port " + n + " output signal is null for block " + 
                             blockName + " (ID: " + blockId + ")");
            return null;
        }
        
        return inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
    }
    
    /**
     * Gets the variable name for the specified output port.
     * 
     * @param n Output port index
     * @return Variable name for the output port, or null if index is invalid or signal not available
     */
    public String getOutputPortVariable(int n) {
        // Check bounds and list state
        if (outputPortList == null || n < 0 || n >= outputPortList.size()) {
            System.err.println("Warning: Output port index " + n + " is out of bounds for block " + 
                             blockName + " (ID: " + blockId + "). OutputPort list size: " + 
                             (outputPortList != null ? outputPortList.size() : "null"));
            return null;
        }
        
        OutputPort outputPort = outputPortList.get(n);
        if (outputPort == null) {
            System.err.println("Warning: Output port at index " + n + " is null for block " + 
                             blockName + " (ID: " + blockId + ")");
            return null;
        }
        
        if (outputPort.getOutputSignalC() == null) {
            System.err.println("Warning: Output port " + n + " signal is null for block " + 
                             blockName + " (ID: " + blockId + ")");
            return null;
        }
        
        return outputPort.getOutputSignalC().getName();
    }
    
    /**
     * Gets the derivative variable name for the specified state.
     * 
     * @param n State index
     * @return Derivative variable name
     */
    public String getDerivativeVariable(int n) {
        return stateList.get(n).getDerivativeName();
    }
    
    /**
     * Gets the state variable name for the specified state.
     * 
     * @param n State index
     * @return State variable name
     */
    public String getStateVariable(int n) {
        return stateList.get(n).getName();
    }
    
    /**
     * Gets the real work variable name for the specified RWork.
     * 
     * @param n RWork index
     * @return RWork variable name
     */
    public String getRWorkVariable(int n) {
        return rworkList.get(n).getName();
    }

    /**
     * Sets the output code generation status and updates all output ports.
     * 
     * @param isOutputCodeGenerated True if output code has been generated
     */
    public void setIsOuputCodeGenerated(boolean isOutputCodeGenerated) {
        this.isOutputCodeGenerated = isOutputCodeGenerated;
        for (OutputPort outputPort : outputPortList) {
            outputPort.setIsCodeGenerated(true);
        }
    }
    
    /**
     * Sets the dimension scanning status and updates all output ports.
     * 
     * @param isDimScaned True if dimension scanning has been completed
     */
    public void setIsDimScaned(boolean isDimScaned) {
        this.isDimScaned = isDimScaned;
        for (OutputPort outputPort : outputPortList) {
            outputPort.setIsDimScaned(true);
        }
    }

	// start: matlab code generation
	//生成M语言的Init代码,不同的Block类型，重载这个方法，生成自己的代码
	@Override
	public void generateInitCodeM(CodeStructM code) {}

	//生成M语言的Init代码，供上一级调用
	@Override
	public void generateBlockInitCodeM(CodeStructM code) {
		for(OutputSignal outputSignal : outputSignalList) {
			code.addOutputSignal(outputSignal);
		}
		for(Parameter parameter : parameterList) {
			code.addParameter(parameter);
		}
		for(State state : stateList) {
			code.addState(state);
		}
		generateInitCodeM(code);
	}

	//生成M语言的Output代码,不同的Block类型，重载这个方法，生成自己的代码
	@Override
	public void  generateOutputCodeM(CodeStructM code) {}

	//生成M语言的Output代码，供上一级调用
	@Override
	public void generateBlockOutputCodeM(CodeStructM code) {
		System.out.format("Generating block output code (%d):%s\n", blockId, blockName);

		generateOutputCodeM(code);

		/*
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}*/
	}

	/**
	 * generate output M code
	 * override this for different block
	 * @param code
	 */
	@Override
	public void generateUpdateCodeM(CodeStructM code) {
		StringBuilder updateCode = new StringBuilder();

		for (State state : stateList) {
			updateCode.append(String.format("%s = %s + %s * stepSize;\n",
				state.getName(), state.getName(), state.getDerivativeName()));
		}

		code.addUpdateCode(updateCode.toString());
	}

	//生成M语言的Update代码，供上一级调用
	@Override
	public void generateBlockUpdateCodeM(CodeStructM code) {
		generateUpdateCodeM(code);
	}

	@Override
	public void generateDerivativeCodeM(CodeStructM code) {}

	@Override
	public void generateBlockDerivativeCodeM(CodeStructM code) {
		generateDerivativeCodeM(code);
	}


	public void  generateArraysCodeM(CodeStructM code) {}

	public void generateBlockArraysCodeM(CodeStructM code) {
		System.out.format("Generating block arrays code (%d):%s\n", blockId, blockName);

		generateArraysCodeM(code);
	}
 	//end


	public void updateBlock() {
        // 更新Parameter的name
        for (Parameter parameter : parameterList) {
            parameter.updateName();
        }

		int i=0;
		//建立模块OutputPort对应的Signal
		for(OutputPort outputPort : outputPortList) {
			OutputSignal outputSignal = new OutputSignal(this, i, outputPort.getNumber(),
				outputPort.getName(), outputPort.getWidth(), outputPort.getHeight());

			outputPort.setOutputSignalC(outputSignal);
			outputSignalList.add(outputSignal);
			i++;
		}
	}


	//start: C code generation
	//生成C语言的Init代码，供上一级调用
	@Override
	public void generateBlockInitCodeC(CodeStructC code) {
		for(OutputSignal outputSignal : outputSignalList) {
			code.addOutputSignal(outputSignal);
		}
		generateInitCodeC(code);
	}

	//生成C语言的Init代码,不同的Block类型，重载这个方法，生成自己的代码
	@Override
	public void generateInitCodeC(CodeStructC code) {
		String initCode="/*Code for initialization of block "+ getBlockType() +":("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
		for(Parameter parameter : parameterList) {
			code.addParameter(parameter);
		}
		for(State state : stateList) {
			code.addState(state);
		}
		// Add global variables into the variableList.
		for (GlobalVariable variable : globalVariableList) {
			code.addGlobalVariable(variable);
		}
        // Populate all standard context variables
        TemplateUtils.populateAllContext(context, this);  
	}

	//生成C语言的Output代码，供上一级调用
	@Override
	public void generateBlockOutputCodeC(CodeStructC code) {
//		System.out.format("Generating block output code (%d):%s\n", blockId, blockName);
		generateOutputCodeC(code);

		/*
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}*/

	}

	//生成C语言的Output代码,不同的Block类型，重载这个方法，生成自己的代码
	@Override
	public void generateOutputCodeC(CodeStructC code) {
        String outputCode="/*Code for output of block "+ getBlockType() +":("+getBlockId()+")"+getBlockName()+"*/\n";
//        code.addOutputCode("/*Code for output of block "+ this.blockType + " :("
//            + getBlockId() + ")" + getBlockPath() + "/" + getBlockName() +"*/\n");
        code.addOutputCode(outputCode);
    }

	public void generateBlockSinkOutputCodeC(CodeStructC code) {
//		System.out.format("Generating block sink output code (%d):%s\n", blockId, blockName);
		generateOutputSinkCodeC(code);
	}

	public void generateOutputSinkCodeC(CodeStructC code) {}

	//生成C语言的Update代码,不同的Block类型，重载这个方法，生成自己的代码
	@Override
	public void generateUpdateCodeC(CodeStructC code) throws MatDimException {
		StringBuilder updateCode = new StringBuilder(String.format(
			"/*Code for update of block %s:(%d)%s*/\n",
			getBlockType(), getBlockId(), getBlockPath()));

		for (State state : stateList) {
			updateCode.append(String.format("%s = %s + %s * model.stepSize;\n",
				state.getName(), state.getName(), state.getDerivativeName()));
		}

		code.addUpdateCode(updateCode.toString());
	}

	//生成C语言的Update代码，供上一级调用
	@Override
	public void generateBlockUpdateCodeC(CodeStructC code) throws MatDimException {
		generateUpdateCodeC(code);
	}

	@Override
	public void generateDiscreteBlockUpdateCodeC(CodeStructC code) throws MatDimException{
		generateDiscreteUpdateCodeC(code);
	}

	@Override
	public void generateDiscreteUpdateCodeC(CodeStructC code) throws MatDimException{}

	@Override
	public void generateBlockDerivativeCodeC(CodeStructC code) {
		generateDerivativeCodeC(code);
	}

	@Override
	public void generateDerivativeCodeC(CodeStructC code) {}

	//define arrays to save data for discrete blocks
	//author:xiazhiqiang
	public void generateBlockArraysCodeC(CodeStructC code) {
//		System.out.format("Generating block arrays code (%d):%s\n", blockId, blockName);
		generateArraysCodeC(code);
	}
	public void generateArraysCodeC(CodeStructC code) {}

	public void generateBlockTerminateCodeC(CodeStructC code) {
		generateTerminateCodeC(code);
	}

	public void generateTerminateCodeC(CodeStructC code) {

	}

	public String getHardwareDefineCodeC() {
		return "";
	}

	public boolean isSFcnBlock() {
		// TODO Auto-generated method stub
		return false;
	}

	public void generateSourceFile() {
		// TODO Auto-generated method stub

	}

	public String getSFcnName() {
		// TODO Auto-generated method stub
		return "";
	}

    public String getFileName(){
        return "";
    }

	public void generateBlockStatementCodeC(CodeStructC code) {
		// TODO Auto-generated method stub
		generateStatementCodeC(code);
	}




	public void generateStatementCodeC(CodeStructC code) {

	}

	public String[] getSFunctionModuleList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateDimension() throws MatDimException{
		// TODO Auto-generated method stub

	}

	/*检查数据宽度是否匹配，可以重载，如果不匹配，可以throw Exception*/
	/*默认检查输入的宽度，默认的宽度为1，如果不为1，需要重载这个函数*/
	public void checkDimension() throws MatDimException{
		for(InputPort input : inputPortList) {
			OutputSignal signal = input.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal.getDataType() != DataType.REAL) {
				MatDimException e = new MatDimException(String.format("Block %s doesn't support Matrix!", this.blockName));
				throw(e);
			}
		}
	}

    /**
     * Generates a unique buffer name for this block.
     * 
     * @return Buffer name string
     */
    protected String getBufferName() {
        return "Block" + getBlockId() + "_buffer";
    }

    public void generateBlockOutputCodeST(CodeStructST code) {
    }

    public void generateBlockInitCodeST(CodeStructST code) {

    }

    public void generateBlockUpdateCodeST(CodeStructST code) {

    }


    /**
     * Parses the parameter list from default values and actual parameter values.
     * Creates Parameter objects with proper indexing and default value fallback.
     */
    private void parseParameterList() {
        Map<String, String> defaults = getParameterDefaults();
        
        int paramIndex = 1;
        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            String paramName = entry.getKey();
            String defaultValue = entry.getValue();
            
            // Safe parameter value extraction with default fallback
            String actualValue = (paramValues != null) ?  paramValues.optString(paramName, defaultValue) : defaultValue;

            parameterList.add(new Parameter(this, paramIndex++, paramName, actualValue));
        }
    }

    /**
     * Parses the parameter list from default values and actual parameter values.
     * Creates Parameter objects with proper indexing and default value fallback.
     * Skips parameters that were already added by TypedParameterConverter.
     */
    private void parseParameterList(BlockDto blockDto) {
        Map<String, String> defaults = getParameterDefaults();

        int paramIndex = parameterList.size() + 1; // Start after existing parameters
        Map<String, Object> paramValues = blockDto.getParamValues();

        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            String paramName = entry.getKey();
            String defaultValue = entry.getValue();

            // Skip if parameter already exists (added by TypedParameterConverter)
            if (getParameterByName(paramName) != null) {
                continue;
            }

            // Safe parameter value extraction with default fallback and type conversion
            String actualValue = defaultValue;
            if (paramValues != null) {
                Object paramValue = paramValues.getOrDefault(paramName, defaultValue);
                actualValue = String.valueOf(paramValue);
            }

            parameterList.add(new Parameter(this, paramIndex++, paramName, actualValue));
        }
    }

    /**
     * Parses the input and output port lists from default values.
     * Creates InputPort and OutputPort objects using centralized configuration.
     */
    private void parseInputOutputPorts() {
        parseInputPorts();
        parseOutputPorts();
    }

    /**
     * Parses the input and output port lists from default values and DTO overrides.
     * Creates InputPort and OutputPort objects using centralized configuration.
     */
    private void parseInputOutputPorts(BlockDto blockDto) {
        parseInputPorts(blockDto);
        parseOutputPorts(blockDto);
    }

    /**
     * Parses input ports from static defaults using legacy JSON constructor.
     */
    private void parseInputPorts() {
        List<Map<String, Object>> inputDefaults = getInputPortDefaults();
        for (int i = 0; i < inputDefaults.size(); i++) {
            Map<String, Object> portConfig = inputDefaults.get(i);
            InputPort inputPort = createInputPort(i + 1, portConfig);
            inputPortList.add(inputPort);
        }
    }

    /**
     * Parses input ports from static defaults with DTO overrides.
     */
    private void parseInputPorts(BlockDto blockDto) {
        List<Map<String, Object>> inputDefaults = getInputPortDefaults();
        for (int i = 0; i < inputDefaults.size(); i++) {
            Map<String, Object> portConfig = inputDefaults.get(i);
            InputPort inputPort = createInputPort(i + 1, portConfig, blockDto);
            inputPortList.add(inputPort);
        }
    }

    /**
     * Parses output ports from static defaults using legacy JSON constructor.
     */
    private void parseOutputPorts() {
        List<Map<String, Object>> outputDefaults = getOutputPortDefaults();
        for (int i = 0; i < outputDefaults.size(); i++) {
            Map<String, Object> portConfig = outputDefaults.get(i);
            OutputPort outputPort = createOutputPort(i + 1, portConfig);
            outputPortList.add(outputPort);
        }
    }

    /**
     * Parses output ports from static defaults with DTO overrides.
     */
    private void parseOutputPorts(BlockDto blockDto) {
        List<Map<String, Object>> outputDefaults = getOutputPortDefaults();
        for (int i = 0; i < outputDefaults.size(); i++) {
            Map<String, Object> portConfig = outputDefaults.get(i);
            OutputPort outputPort = createOutputPort(i + 1, portConfig, blockDto);
            outputPortList.add(outputPort);
        }
    }

    /**
     * Creates an InputPort from configuration for legacy JSON constructor.
     */
    private InputPort createInputPort(int index, Map<String, Object> config) {
        String name = (String) config.getOrDefault("name", "in" + index);
        int width = (Integer) config.getOrDefault("width", 1);
        int height = (Integer) config.getOrDefault("height", 1);
        String dataTypeStr = (String) config.getOrDefault("dataType", "REAL");
        DataType dataType = DataType.valueOf(dataTypeStr);
        
        return new InputPort(this, index, name);
    }

    /**
     * Creates an InputPort from configuration with DTO overrides.
     */
    private InputPort createInputPort(int index, Map<String, Object> config, BlockDto blockDto) {
        // Use defaults first, then override with DTO values if available
        String name = (String) config.getOrDefault("name", "in" + index);
        int width = (Integer) config.getOrDefault("width", 1);
        int height = (Integer) config.getOrDefault("height", 1);
        String dataTypeStr = (String) config.getOrDefault("dataType", "REAL");
        DataType dataType = DataType.valueOf(dataTypeStr);
        
        // TODO: Override with DTO port configuration when available
        
        return new InputPort(this, index, name);
    }

    /**
     * Creates an OutputPort from configuration for legacy JSON constructor.
     */
    private OutputPort createOutputPort(int index, Map<String, Object> config) {
        String name = (String) config.getOrDefault("name", "out" + index);
        int width = (Integer) config.getOrDefault("width", 1);
        int height = (Integer) config.getOrDefault("height", 1);
        String dataTypeStr = (String) config.getOrDefault("dataType", "REAL");
        boolean feedthrough = (Boolean) config.getOrDefault("feedthrough", false);
        DataType dataType = DataType.valueOf(dataTypeStr);
        
        return new OutputPort(this, name, index, feedthrough);
    }

    /**
     * Creates an OutputPort from configuration with DTO overrides.
     */
    private OutputPort createOutputPort(int index, Map<String, Object> config, BlockDto blockDto) {
        // Use defaults first, then override with DTO values if available
        String name = (String) config.getOrDefault("name", "out" + index);
        int width = (Integer) config.getOrDefault("width", 1);
        int height = (Integer) config.getOrDefault("height", 1);
        String dataTypeStr = (String) config.getOrDefault("dataType", "REAL");
        boolean feedthrough = (Boolean) config.getOrDefault("feedthrough", false);
        DataType dataType = DataType.valueOf(dataTypeStr);
        
        // TODO: Override with DTO port configuration when available        
        return new OutputPort(this, name, index, feedthrough);
    }

    // === Utility Methods ===
    
    /**
     * Gets all input port variable names as an array.
     * 
     * @return Array of input port variable names (skips null entries)
     */
    protected String[] getInputPortVariables() {
        List<String> inputPortVariables = new ArrayList<>();
        for (int n = 0; n < inputPortList.size(); n++) {
            String varName = getInputPortVariable(n);
            if (varName != null) {
                inputPortVariables.add(varName);
            }
        }
        return inputPortVariables.toArray(new String[0]);
    }
    
    /**
     * Gets all output port variable names as an array.
     * 
     * @return Array of output port variable names (skips null entries)
     */
    protected String[] getOutputPortVariables() {
        List<String> outputPortVariables = new ArrayList<>();
        for (int n = 0; n < outputPortList.size(); n++) {
            String varName = getOutputPortVariable(n);
            if (varName != null) {
                outputPortVariables.add(varName);
            }
        }
        return outputPortVariables.toArray(new String[0]);
    }
    
    /**
     * Safely gets the variable name for the specified input port with fallback.
     * 
     * @param n Input port index
     * @param fallback Default value to return if input port is not available
     * @return Variable name for the input port, or fallback value if not available
     */
    protected String safeGetInputPortVariable(int n, String fallback) {
        String varName = getInputPortVariable(n);
        return varName != null ? varName : fallback;
    }
    
    /**
     * Safely gets the variable name for the specified output port with fallback.
     * 
     * @param n Output port index
     * @param fallback Default value to return if output port is not available
     * @return Variable name for the output port, or fallback value if not available
     */
    protected String safeGetOutputPortVariable(int n, String fallback) {
        String varName = getOutputPortVariable(n);
        return varName != null ? varName : fallback;
    }

    // === Multi-Language Code Generation Interface ===
    
    /**
     * Generates initialization code for the specified language.
     * 
     * @param code Code structure to append to
     * @param language Target language identifier
     */
    public void generateInitCode(CodeStructC code, String language) {
        // Default implementation - override in derived classes
    }
    
    /**
     * Generates update code for the specified language.
     * 
     * @param code Code structure to append to
     * @param language Target language identifier
     */
    public void generateUpdateCode(CodeStructC code, String language) {
        // Default implementation - override in derived classes
    }
    
    /**
     * Generates derivative code for the specified language.
     * 
     * @param code Code structure to append to
     * @param language Target language identifier
     */
    public void generateDerivativeCode(CodeStructC code, String language) {
        // Default implementation - override in derived classes
    }
    
    /**
     * Generates output code for the specified language.
     * 
     * @param code Code structure to append to
     * @param language Target language identifier
     */
    public void generateOutputCode(CodeStructC code, String language) {
        // Default implementation - override in derived classes
    }

    // === Runtime Simulation Interface ===
    
    /**
     * Calculates block output at the specified time.
     * Override in derived classes to implement block-specific output calculation.
     * 
     * @param t Current simulation time
     */
    public void calculateOutput(double t) {
        // Default implementation - override in derived classes
    }
    
    /**
     * Calculates block derivatives at the specified time.
     * Used for continuous-time blocks with differential equations.
     * 
     * @param t Current simulation time
     */
    public void calculateDerivative(double t) {
        // Default implementation - override in derived classes
    }
    
    /**
     * Performs discrete update calculations at the specified time.
     * Used for discrete-time blocks with difference equations.
     * 
     * @param t Current simulation time
     */
    public void calculateDiscreteUpdate(double t) {
        // Default implementation - override in derived classes
    }
    
    /**
     * Performs block initialization calculations.
     * Called once at the start of simulation.
     */
    public void calculateInit() {
        // Default implementation - override in derived classes
    }
    
    /**
     * Performs block termination cleanup.
     * Called once at the end of simulation.
     * 
     * @param t Final simulation time
     */
    public void calculateTerminate(double t) {
        // Default implementation - override in derived classes
    }
}
