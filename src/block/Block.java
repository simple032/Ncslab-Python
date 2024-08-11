package block;

import block.data.DataType;
import block.io.*;
import code.c.CodeStructC;
import code.m.CodeStructM;
import code.plc.CodeStructPLC;
import lombok.Getter;
import lombok.Setter;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

//各个Block模块的基类，定义了block的框架；如果需要生成各种语言，需要连接各种语言生成器的接口
public class Block implements block.lan.MCodeBlock,block.lan.CCodeBlock{
	
	//Block的类型，需要在BlockType中建立block的时候分别对待
	@Getter
    protected String blockType;
	@Getter
    protected String blockName;
	
	@Setter
    @Getter
    protected int blockId=0;

    //xiazhiqiang:获取模块所处子系统的位置两个方法getBlockPath与getSubSystemName
    //Block所在画布的位置，不在子系统时为modelName，存在子系统时为modelName/subsystem
	@Getter
    protected String blockPath;
	//Block的参数，因为不同的block有不同的参数，因此以原生的json格式存储
	@Getter
    protected JSONObject paramValues;
	
	//输入与输出端口的列表
	@Getter
    protected Vector<InputPort> inputPortList=new Vector<InputPort>();
	@Getter
    protected Vector<OutputPort> outputPortList=new Vector<OutputPort>();
	
	@Getter
    protected Vector<Parameter> parameterList=new Vector<Parameter>();
	@Getter
    protected Vector<State> stateList=new Vector<State>();

	protected Vector<RWork> rworkList=new Vector<RWork>();
	
	protected Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();
	
	//是否输出的代码已经生成，如果生成，遍历到这个模块的时候，直接引用就行了，就不需要进一步遍历了
	protected boolean isOutputCodeGenerated=false;
	
	protected boolean isDimScaned=false;
	
	//指向上级Model模型的指针
	@Getter
    protected NCSLabModel model;
	
	//Block中Singal中的个数，Signal没有Java的数据结构，Signal可以是InputPort的量，也可以是OutputPort中的量，具体看代码生成时的认定
	@Setter
    @Getter
    protected int signalNum=0;
	
	protected Block(JSONObject blockIn,NCSLabModel model) {
		this.blockType=blockIn.getString("blockType");
		this.blockName=blockIn.getString("blockName");
		this.paramValues=blockIn.getJSONObject("paramValues");
		this.model=model;
		this.blockPath=blockIn.getString("blockPath");
		
	}

    public boolean isTerminalBlock() {
		return (outputPortList.size()==0);
	}

    public String getSubSystemName() {
		int index=this.blockPath.lastIndexOf("/");
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

	public String getStateVariable(int n) {
		return stateList.get(n).getName();
	}

	public String getDerivativeVariable(int n) { return stateList.get(n).getDerivativeName(); }

	public String getRWorkVariable(int n) {
		return rworkList.get(n).getName();
	}
	
	
	//生成M语言的Output代码,不同的Block类型，重载这个方法，生成自己的代码
	public void  generateOutputCodeM(CodeStructM code) {
	
	}
	
	public void setIsOuputCodeGenerated(boolean isOutputCodeGenerated) {
		this.isOutputCodeGenerated=isOutputCodeGenerated;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}
	}
	
	public void setIsDimScaned(boolean isDimScaned) {
		this.isDimScaned=isDimScaned;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsDimScaned(true);
		}
	}
	
	//生成M语言的Output代码，供上一级调用
	public void generateBlockOutputCodeM(CodeStructM code) {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		generateOutputCodeM(code);
		
		/*
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}*/
	}
	
	//生成M语言的Init代码,不同的Block类型，重载这个方法，生成自己的代码
	public void generateInitCodeM(CodeStructM code) {
	}
	
	//Author:xiazhiqiang
	public void  generateArraysCodeM(CodeStructM code) {
		
	}
	public void generateBlockArraysCodeM(CodeStructM code) {
		System.out.println("Generating block arrays code ("+blockId+"):"+blockName);
		
		generateArraysCodeM(code);
	}
 //end

	//生成M语言的Init代码，供上一级调用
	public void generateBlockInitCodeM(CodeStructM code) {
		for(OutputSignal outputSignal:outputSignalList) {
			code.addOutputSignal(outputSignal);
		}
		for(Parameter parameter:parameterList) {
			code.addParameter(parameter);
		}
		for(State state:stateList) {
			code.addState(state);
		}
		generateInitCodeM(code);
	}
	
	//生成M语言的Update代码,不同的Block类型，重载这个方法，生成自己的代码
	public void generateUpdateCodeM(CodeStructM code) {
		String updateCode="";
		
		for(State state:stateList) {
			updateCode+=state.getName()+"="
					+state.getName()+"+"
					+state.getDerivativeName()
					+"*"
					+"stepSize"
					+";\n";
		}
		
		code.addUpdateCode(updateCode);
	}
	
	//生成M语言的Update代码，供上一级调用
	public void generateBlockUpdateCodeM(CodeStructM code) {
		generateUpdateCodeM(code);
	}
	
	public void generateBlockDerivativeCodeM(CodeStructM code) {
		generateDerivativeCodeM(code);
	}
	public void generateDerivativeCodeM(CodeStructM code) {
		
	}
	
	public void updateBlock() {
		int i=0;
		for(OutputPort outputPort:outputPortList) {
			OutputSignal outputSignal=new OutputSignal(this,i,outputPort.getNumber(),outputPort.getName(),outputPort.getWidth(),outputPort.getHeight());
			outputPort.setOutputSignalC(outputSignal);
			outputSignalList.add(outputSignal);
			i++;
		}
	}
	
	
	//c语言的代码生成方法，与M语言相同
	//生成C语言的Init代码，供上一级调用
	public void generateBlockInitCodeC(CodeStructC code) {
		for(OutputSignal outputSignal:outputSignalList) {
			code.addOutputSignal(outputSignal);
		}
		generateInitCodeC(code);
	}
	
	//生成C语言的Init代码,不同的Block类型，重载这个方法，生成自己的代码
	public void generateInitCodeC(CodeStructC code) {
		for(Parameter parameter:parameterList) {
			code.addParameter(parameter);
		}
		for(State state:stateList) {
			code.addState(state);
		}
	}
	
	//生成C语言的Output代码，供上一级调用
	public void generateBlockOutputCodeC(CodeStructC code) {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		generateOutputCodeC(code);
		
		/*
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true); 
		}*/
		
	}
	
	//生成C语言的Output代码,不同的Block类型，重载这个方法，生成自己的代码
	public void generateOutputCodeC(CodeStructC code) {
		
	}
	
	//生成C语言的Update代码，供上一级调用
	public void generateBlockUpdateCodeC(CodeStructC code) throws MatDimException {
		generateUpdateCodeC(code);
	}
	
	//define arrays to save data for discrete blocks
	//author:xiazhiqiang
	public void generateBlockArraysCodeC(CodeStructC code) {
		System.out.println("Generating block arrays code ("+blockId+"):"+blockName);
		generateArraysCodeC(code);
	}
	public void generateArraysCodeC(CodeStructC code) {
		
	}
	//end	
	
	//生成C语言的Update代码,不同的Block类型，重载这个方法，生成自己的代码
	public void generateUpdateCodeC(CodeStructC code) throws MatDimException {
		String updateCode="/*Code for update of block "+getBlockType()+":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		for(State state:stateList) {
			updateCode+=state.getName()+"="
					+state.getName()+"+"
					+state.getDerivativeName()
					+"*"
					+"model.stepSize"
					+";\n";
		}
		
		code.addUpdateCode(updateCode);
	}
	
	public void generateDiscreteBlockUpdateCodeC(CodeStructC code) throws MatDimException{
		generateDiscreteUpdateCodeC(code);
	}
	public void generateDiscreteUpdateCodeC(CodeStructC code) throws MatDimException{
		
	}
	
	public void generateBlockDerivativeCodeC(CodeStructC code) {
		generateDerivativeCodeC(code);
	}
	public void generateDerivativeCodeC(CodeStructC code) {
		
	}
	
	public void generateBlockTerminateCodeC(CodeStructC code) {
		generateTerminateCodeC(code);
	}
	
	public void generateTerminateCodeC(CodeStructC code) {
		
	}

	public void generateBlockInitCodePLC(CodeStructPLC code){
		generateInitCodePLC(code);
	}

	public void generateInitCodePLC(CodeStructPLC code){

	}

	public void generateBlockUpdateCodePLC(CodeStructPLC code){
		generateUpdateCodePLC(code);
	}

	public void generateUpdateCodePLC(CodeStructPLC code){

	}

	public void generateBlockOutputCodePLC(CodeStructPLC code){
		generateOutputCodePLC(code);
	}

	public void generateOutputCodePLC(CodeStructPLC code){

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
		for(InputPort input:inputPortList) {
			OutputSignal signal=input.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal.getDataType()!=DataType.REAL) {
				MatDimException e=new MatDimException("Block "+this.blockName+" doesn't support Matrix!");
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

}
