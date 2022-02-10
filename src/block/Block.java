package block;

import java.util.Vector;
import org.json.JSONObject;

import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import block.io.State;
import block.io.OutputSignal;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

//各个Block模块的基类，定义了block的框架；如果需要生成各种语言，需要连接各种语言生成器的接口
public class Block implements block.lan.MCodeBlock,block.lan.CCodeBlock{
	
	//Block的类型，需要在BlockType中建立block的时候分别对待
	protected String blockType;
	protected String blockName;
	
	protected int blockId=0;
	
	//Block的参数，因为不同的block有不同的参数，因此以原生的json格式存储
	protected JSONObject paramValues;
	
	//输入与输出端口的列表
	protected Vector<InputPort> inputPortList=new Vector<InputPort>();
	protected Vector<OutputPort> outputPortList=new Vector<OutputPort>();
	
	protected Vector<Parameter> parameterList=new Vector<Parameter>();
	protected Vector<State> stateList=new Vector<State>();
	
	protected Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();
	
	//是否输出的代码已经生成，如果生成，遍历到这个模块的时候，直接引用就行了，就不需要进一步遍历了
	protected boolean isOutputCodeGenerated=false;
	
	//指向上级Model模型的指针
	protected NCSLabModel model;
	
	//Block中Singal中的个数，Signal没有Java的数据结构，Signal可以是InputPort的量，也可以是OutputPort中的量，具体看代码生成时的认定
	protected int signalNum=0;
	
	protected Block(JSONObject blockIn,NCSLabModel model) {
		this.blockType=blockIn.getString("blockType");
		this.blockName=blockIn.getString("blockName");
		this.paramValues=blockIn.getJSONObject("paramValues");
		this.model=model;
		
	}
	
	public void setSignalNum(int signalNum) {
		this.signalNum=signalNum;
	}
	
	public int getSignalNum() {
		return this.signalNum;
	}
	
	public NCSLabModel getModel() {
		return this.model;
	}
	
	public boolean isTerminalBlock() {
		return (outputPortList.size()==0);
	}
	
	public String getBlockName() {
		return blockName;
	}
	
	public String getBlockType() {
		return blockType;
	}
	
	public Vector<InputPort> getInputPortList(){
		return inputPortList;
	}
	
	public Vector<OutputPort> getOutputPortList(){
		return outputPortList;
	}
	
	public Vector<Parameter> getParameterList(){
		return parameterList;
	}
	
	public Vector<State> getStateList(){
		return stateList;
	}
	
	public void setBlockId(int blockId) {
		this.blockId=blockId;
	}
	
	public int getBlockId() {
		return this.blockId;
	}

	public boolean getIsOutputCodeGenerated() {
		return this.isOutputCodeGenerated;
	}
	
	public String getInputPortVariable(int n) {
		return inputPortList.get(n).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}
	
	public String getOutputPortVariable(int n) {
		return outputPortList.get(n).getOutputSignalC().getName();
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
	
	public void generateBlockDerivativeCodeC(CodeStructC code) {
		generateDerivativeCodeC(code);
	}
	public void generateDerivativeCodeC(CodeStructC code) {
		
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
}
