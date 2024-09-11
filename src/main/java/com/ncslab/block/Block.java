package com.ncslab.block;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.ncslab.block.io.*;
import com.ncslab.code.plc.CodeStructPLC;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.lan.CCodeBlock;
import com.ncslab.block.lan.MCodeBlock;
import com.ncslab.block.io.OutputPort;

//各个Block模块的基类，定义了block的框架；如果需要生成各种语言，需要连接各种语言生成器的接口
public class Block implements MCodeBlock, CCodeBlock{

	//Block的类型，需要在BlockType中建立block的时候分别对待
	@Getter
    protected String blockType;
	@Getter
    protected String blockName;

	@Getter
    @Setter
    protected int blockId = 0;
    //xiazhiqiang:获取模块所处子系统的位置两个方法getBlockPath与getSubSystemName
    //Block所在画布的位置，不在子系统时为modelName，存在子系统时为modelName/subsystem
	@Getter
    protected String blockPath;
	//Block的参数，因为不同的block有不同的参数，因此以原生的json格式存储
	@Getter
    protected JSONObject paramValues;

	//输入与输出端口的列表
	@Getter
    protected Vector<InputPort> inputPortList = new Vector<>();
	@Getter
    protected Vector<OutputPort> outputPortList = new Vector<OutputPort>();

	@Getter
    protected Vector<Parameter> parameterList = new Vector<Parameter>();
	@Getter
    protected Vector<State> stateList = new Vector<State>();
    @Getter
    protected Vector<State> dStateList = new Vector<State>();
    @Getter
    protected Vector<RWork> rworkList = new Vector<>();
	@Getter
    protected Vector<GlobalVariable> globalVariableList = new Vector<>(); // global variables

	protected Vector<OutputSignal> outputSignalList = new Vector<OutputSignal>();

	//是否输出的代码已经生成，如果生成，遍历到这个模块的时候，直接引用就行了，就不需要进一步遍历了
	protected boolean isOutputCodeGenerated = false;

	protected boolean isDimScaned = false;

	//指向上级Model模型的指针
	@Getter
    protected NCSLabModel model;

	protected boolean isHardware = false;

	//Block中Singal中的个数，Signal没有Java的数据结构，Signal可以是InputPort的量，也可以是OutputPort中的量，具体看代码生成时的认定
	@Getter
    @Setter
    protected int signalNum = 0;

    @Getter
    public static Vector<String> parameterNames = new Vector<>();

    @Getter
    public static Vector<String> inputNames = new Vector<>();

    @Getter
    public static Vector<String> outputNames = new Vector<>();


    protected Block(JSONObject blockIn, NCSLabModel model) {
		this.blockType=blockIn.getString("blockType");
		this.blockName=blockIn.getString("blockName");
		this.paramValues=blockIn.getJSONObject("paramValues");
		this.model=model;
		this.blockPath=blockIn.getString("blockPath");
	}

    public void setFeedThrough(boolean feedThrough) {
		this.outputPortList.get(0).setFeedThrough(feedThrough);
	}

	public boolean getIsHardware() {
		return this.isHardware;
	}

    public boolean isTerminalBlock() {
		return (outputPortList.size() == 0);
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
		System.out.format("Generating block output code (%d):%s\n", blockId, blockName);
		generateOutputCodeC(code);

		/*
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}*/

	}

	//生成C语言的Output代码,不同的Block类型，重载这个方法，生成自己的代码
	@Override
	public void generateOutputCodeC(CodeStructC code) {}

	public void generateBlockSinkOutputCodeC(CodeStructC code) {
		System.out.format("Generating block sink output code (%d):%s\n", blockId, blockName);
		generateOutputSinkCodeC(code);
	}

	public void generateOutputSinkCodeC(CodeStructC code) {}

	//生成C语言的Update代码,不同的Block类型，重载这个方法，生成自己的代码
	@Override
	public void generateUpdateCodeC(CodeStructC code) throws MatDimException {
		StringBuilder updateCode = new StringBuilder(String.format(
			"/*Code for update of block %s:(%d)%s*/\n",
			getBlockType(), getBlockId(), getBlockName()));

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
		System.out.format("Generating block arrays code (%d):%s\n", blockId, blockName);
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

    protected String M2PCode2C(String content){
        // 创建替换映射
        Map<String, String> replacements = new HashMap<>();

        for(int i=0; i<stateList.size(); i++)
            replacements.put(String.format("<DSTATE%d>", i), getDerivativeVariable(i)) ;

        for(int i=0; i<stateList.size(); i++)
            replacements.put(String.format("<STATE%d>", i), getStateVariable(i)) ;

        for(int i=0; i<inputPortList.size(); i++)
            replacements.put(String.format("<INPUT%d>", i), getInputPortVariable(i)) ;

        for(int i=0; i<outputPortList.size(); i++)
            replacements.put(String.format("<OUTPUT%d>", i), getOutputPortVariable(i)) ;

        for(int i=0; i<rworkList.size(); i++)
            replacements.put(String.format("<RWORK%d>", i), getRWorkVariable(i)) ;

        // 替换模板中的占位符
        String result = content;
        for (Map.Entry<String, String> entry : replacements.entrySet())
            result = result.replace(entry.getKey(), entry.getValue());
        return result;
    }

    public void generateBlockOutputCodePLC(CodeStructPLC code) {
    }

    public void generateBlockInitCodePLC(CodeStructPLC code) {

    }

    public void generateBlockUpdateCodePLC(CodeStructPLC code) {

    }
}
