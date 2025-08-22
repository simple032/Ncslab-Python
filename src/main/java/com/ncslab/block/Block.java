package com.ncslab.block;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import com.ncslab.block.io.*;
import com.ncslab.code.st.CodeStructST;
import com.ncslab.ncslablink.ModelMode;
import lombok.Getter;
import lombok.Setter;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

import com.ncslab.block.data.DataType;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.lan.CCodeBlock;
import com.ncslab.block.lan.MCodeBlock;
import com.ncslab.util.TemplateUtils;


//各个Block模块的基类，定义了block的框架；如果需要生成各种语言，需要连接各种语言生成器的接口
public class Block implements MCodeBlock, CCodeBlock{

	//Block的类型，需要在BlockType中建立block的时候分别对待
    @Getter
    protected String blockType;
    @Getter
    protected String blockName;

    @Setter
    @Getter
    protected int blockId = 0;
    //xiazhiqiang:获取模块所处子系统的位置两个方法getBlockPath与getSubSystemName
    //Block所在画布的位置，不在子系统时为modelName，存在子系统时为modelName/subsystem
    @Getter
    protected String blockPath;
	//Block的参数，因为不同的block有不同的参数，因此以原生的json格式存储

    @Getter
    protected String blockUUID = "null"; // 只有BlockCId才具有唯一性

    @Getter
    protected JSONObject paramValues;

	//输入与输出端口的列表
    @Getter
    protected List<InputPort> inputPortList = new ArrayList<>();
    @Getter
    protected List<OutputPort> outputPortList = new ArrayList<>();
    @Getter
    protected List<Parameter> parameterList = new ArrayList<>();
    @Getter
    protected List<State> stateList = new ArrayList<>();
    @Getter
    protected List<State> dStateList = new ArrayList<>();
    @Getter
    protected List<RWork> rworkList = new ArrayList<>();
    @Getter
    protected List<GlobalVariable> globalVariableList = new ArrayList<>(); // global variables

	protected List<OutputSignal> outputSignalList = new ArrayList<>();

	//是否输出的代码已经生成，如果生成，遍历到这个模块的时候，直接引用就行了，就不需要进一步遍历了
	protected boolean isOutputCodeGenerated = false;

	protected boolean isDimScaned = false;

	//指向上级Model模型的指针
    @Getter
    protected NCSLabModel model;

	protected boolean isHardware = false;

	//Block中Singal中的个数，Signal没有Java的数据结构，Signal可以是InputPort的量，也可以是OutputPort中的量，具体看代码生成时的认定
    @Setter
    @Getter
    protected int signalNum = 0;


    // Get parameter defaults from the derived class's static PARAMETER_DEFAULTS field
    protected Map<String, String> getParameterDefaults() {
        try {
            // Get the actual runtime class of this instance
            Class<?> clazz = this.getClass();

            // Try to get the static PARAMETER_DEFAULTS field
            java.lang.reflect.Field defaultsField = clazz.getField("PARAMETER_DEFAULTS");

            // Ensure it's a Map type
            if (Map.class.isAssignableFrom(defaultsField.getType())) {
                @SuppressWarnings("unchecked")
                Map<String, String> defaults = (Map<String, String>) defaultsField.get(null);
                return defaults != null ? defaults : new HashMap<>();
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            // If the field doesn't exist or can't be accessed, fall back to empty map
            // This allows blocks without PARAMETER_DEFAULTS to still work
            System.err.println("Can't get default parameters for " + this.getClass().getName());
        }

        // Fallback to empty map if no PARAMETER_DEFAULTS field found
        return new HashMap<>();
    }

    // Helper method to get parameter by name instead of index
    protected Parameter getParameterByName(String name) {
        return parameterList.stream()
            .filter(p -> p.getLocalName().equals(name))
            .findFirst()
            .orElse(null);
    }

    @Getter
    public static List<String> inputNames = new ArrayList<>();

    @Getter
    public static List<String> outputNames = new ArrayList<>();

    protected VelocityContext context = null;

    /**
     * DTO-NATIVE Constructor - Creates Block from BlockDto DTO
     * This constructor provides the foundation for DTO-native block creation
     */
    protected Block(BlockDto blockDto, NCSLabModel model) {
        this.blockType = blockDto.getBlockType();
        this.blockName = blockDto.getBlockName();
        if (blockDto.getParamValues() != null) {
            this.paramValues = new JSONObject(blockDto.getParamValues());
        }
        this.model = model;
        this.blockPath = blockDto.getBlockPath();
        this.blockUUID = blockDto.getBlockUUID() != null ? blockDto.getBlockUUID() : "null";
        parseParameterList();
        context = new VelocityContext();
        
        // Use enhanced template utilities for comprehensive context population
        TemplateUtils.populateAllContext(context, this);
    }

    protected Block(JSONObject blockIn, NCSLabModel model) {
		this.blockType=blockIn.getString("blockType");
		this.blockName=blockIn.getString("blockName");
        if (blockIn.get("paramValues") instanceof JSONObject) {
            this.paramValues=blockIn.getJSONObject("paramValues");
        }
		this.model=model;
		this.blockPath=blockIn.getString("blockPath");
        this.blockUUID=blockIn.optString("blockUUID", "null");
        parseParameterList();
        context = new VelocityContext();
        
        // Use enhanced template utilities for comprehensive context population
        TemplateUtils.populateAllContext(context, this);
	}

    public void setFeedThrough(boolean feedThrough) {
		this.outputPortList.get(0).setFeedThrough(feedThrough);
	}

	public boolean getIsHardware() {
		return this.isHardware;
	}

    public boolean isTerminalBlock() {
		return (outputPortList.isEmpty());
	}

    public String getSubSystemName() {
		int index = this.blockPath.lastIndexOf("/");
        return this.blockPath.substring(index+1);
	}

    public boolean getIsOutputCodeGenerated() {
		return this.isOutputCodeGenerated;
	}

	public boolean getIsDimScaned() {
		return this.isDimScaned;
	}

	public String getInputPortVariable(int n) {
		return inputPortList.get(n).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}

	public String getOutputPortVariable(int n) {
		return outputPortList.get(n).getOutputSignalC().getName();
	}

    public String getDerivativeVariable(int n) { return stateList.get(n).getDerivativeName(); }
    public String getStateVariable(int n) { return stateList.get(n).getName(); }
    public String getRWorkVariable(int n) { return rworkList.get(n).getName(); }

	public void setIsOuputCodeGenerated(boolean isOutputCodeGenerated) {
		this.isOutputCodeGenerated = isOutputCodeGenerated;
		for(OutputPort outputPort : outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}
	}

	public void setIsDimScaned(boolean isDimScaned) {
		this.isDimScaned = isDimScaned;
		for(OutputPort outputPort: outputPortList) {
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

    protected String getBufferName(){
        return "Block"+getBlockId()+"_buffer";
    }

    public void generateBlockOutputCodeST(CodeStructST code) {
    }

    public void generateBlockInitCodeST(CodeStructST code) {

    }

    public void generateBlockUpdateCodeST(CodeStructST code) {

    }


    private void parseParameterList() {
        Map<String, String> defaults = getParameterDefaults();

        int paramIndex = 1;
        for(Map.Entry<String, String> entry : defaults.entrySet()) {
            String paramName = entry.getKey();
            String defaultValue = entry.getValue();

            // Use optString to safely get parameter value with fallback to default
            String actualValue = (paramValues != null) ?
                paramValues.optString(paramName, defaultValue) : defaultValue;

            parameterList.add(new Parameter(this, paramIndex++, paramName, actualValue));
        }
    }

	protected String[] getInputPortVariables(){
		List<String> inputPortVariables = new ArrayList<>();
		for(int n=0; n<inputPortList.size();n++){
			inputPortVariables.add(getInputPortVariable(n));
		}
		return inputPortVariables.toArray(new String[0]);
	}

	protected String[] getOutputPortVariables(){
		List<String> outputPortVariables = new ArrayList<>();
		for(int n=0; n<outputPortList.size();n++){
			outputPortVariables.add(getOutputPortVariable(n));
		}
		return outputPortVariables.toArray(new String[0]);
	}

//    protected String[] SUPPORT_LANGUAGES = {"M", "C"};
//    boolean containsKey = Arrays.stream(SUPPORT_LANGUAGES).anyMatch(item -> item.equals(language));
//    if(!containsKey) {
//        System.error.println("Language not supported");
//        return;
//    }

    public void generateInitCode(CodeStructC code, String language) {}
    public void generateUpdateCode(CodeStructC code, String language){}
    public void generateDerivativeCode(CodeStructC code, String language){}
    public void generateOutputCode(CodeStructC code, String language){}

    public void calculateOutput(double t) {
//        System.err.println("TODO:" + blockType + " to be override(calculateOutput)");
    }
    public void calculateDerivative(double t) {}
    public void calculateDiscreteUpdate(double t) {}
    public void calculateInit() {}
    public void calculateTerminate(double t) {}
}
