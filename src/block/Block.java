package block;

import java.util.Vector;
import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import block.io.State;
import code.c.CodeStructC;
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
	
	//生成M语言的Output代码,不同的Block类型，重载这个方法，生成自己的代码
	public String generateOutputCodeM() {
		String code="";
		
		return code;
	}
	
	//生成M语言的Output代码，供上一级调用
	public String generateBlockOutputCodeM() {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		String code=generateOutputCodeM();
		
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}
		
		return code;
	}
	
	//生成M语言的Init代码,不同的Block类型，重载这个方法，生成自己的代码
	public String generateInitCodeM() {
		String code="";
		
		return code;
	}

	//生成M语言的Init代码，供上一级调用
	public String generateBlockInitCodeM() {
		String code=generateInitCodeM();
		return code;
	}
	
	//生成M语言的Update代码,不同的Block类型，重载这个方法，生成自己的代码
	public String generateUpdateCodeM() {
		String code="";
		
		return code;
	}
	
	//生成M语言的Update代码，供上一级调用
	public String generateBlockUpdateCodeM() {
		String code=generateUpdateCodeM();
		return code;
	}
	
	
	//c语言的代码生成方法，与M语言相同
	//生成C语言的Init代码，供上一级调用
	public void generateBlockInitCodeC(CodeStructC code) {
		for(OutputPort outputPort:outputPortList) {
			code.addOutputSignal(this, outputPort);
		}
		generateInitCodeC(code);
	}
	
	//生成C语言的Init代码,不同的Block类型，重载这个方法，生成自己的代码
	public void generateInitCodeC(CodeStructC code) {
		
	}
	
	//生成C语言的Output代码，供上一级调用
	public void generateBlockOutputCodeC(CodeStructC code) {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		generateOutputCodeC(code);
		
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}
		
	}
	
	//生成C语言的Output代码,不同的Block类型，重载这个方法，生成自己的代码
	public void generateOutputCodeC(CodeStructC code) {
		
	}
	
	//生成C语言的Update代码，供上一级调用
	public void generateBlockUpdateCodeC(CodeStructC code) {
		generateUpdateCodeC(code);
	}
	
	//生成C语言的Update代码,不同的Block类型，重载这个方法，生成自己的代码
	public void generateUpdateCodeC(CodeStructC code) {
	}
}
