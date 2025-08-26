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
    @Getter
    protected String blockName;
    
    /** Unique block ID within the model */
    @Setter
    @Getter
    protected int blockId = 0;
    
    /** 
     * Block canvas position path.
     * Format: modelName for top-level blocks, modelName/subsystem for subsystem blocks
     */
    @Getter
    protected String blockPath;
    
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
    protected VelocityContext context = null;
    
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
        
        // Initialize paramValues from DTO for backward compatibility
        this.paramValues = new JSONObject();
        if (blockDto.getParamValues() != null && !blockDto.getParamValues().isEmpty()) {
            for (Map.Entry<String, Object> entry : blockDto.getParamValues().entrySet()) {
                this.paramValues.put(entry.getKey(), entry.getValue());
            }
        }
        
        parseParameterList();
        initializeTemplateContext();
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
        initializeTemplateContext();
    }

    /**
     * Initializes the Velocity template context with comprehensive block data.
     * Centralizes context setup for consistent template rendering across all blocks.
     */
    private void initializeTemplateContext() {
        context = new VelocityContext();
        TemplateUtils.populateAllContext(context, this);
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
     * @return Variable name for the input port
     */
    public String getInputPortVariable(int n) {
        return inputPortList.get(n).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
    }
    
    /**
     * Gets the variable name for the specified output port.
     * 
     * @param n Output port index
     * @return Variable name for the output port
     */
    public String getOutputPortVariable(int n) {
        return outputPortList.get(n).getOutputSignalC().getName();
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
		String initCode="/*Code for initialization of block Pulse:("+getBlockId()+")"+getBlockName()+"*/\n";
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
//        code.addOutputCode("/*Code for output of block "+ this.blockType + " :("
//            + getBlockId() + ")" + getBlockPath() + "/" + getBlockName() +"*/\n");
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
            String actualValue = (paramValues != null) ? 
                paramValues.optString(paramName, defaultValue) : defaultValue;
            
            parameterList.add(new Parameter(this, paramIndex++, paramName, actualValue));
        }
    }


    // === Utility Methods ===
    
    /**
     * Gets all input port variable names as an array.
     * 
     * @return Array of input port variable names
     */
    protected String[] getInputPortVariables() {
        List<String> inputPortVariables = new ArrayList<>();
        for (int n = 0; n < inputPortList.size(); n++) {
            inputPortVariables.add(getInputPortVariable(n));
        }
        return inputPortVariables.toArray(new String[0]);
    }
    
    /**
     * Gets all output port variable names as an array.
     * 
     * @return Array of output port variable names
     */
    protected String[] getOutputPortVariables() {
        List<String> outputPortVariables = new ArrayList<>();
        for (int n = 0; n < outputPortList.size(); n++) {
            outputPortVariables.add(getOutputPortVariable(n));
        }
        return outputPortVariables.toArray(new String[0]);
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
