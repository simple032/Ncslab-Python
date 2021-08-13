package block;

import java.util.Vector;
import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;
import code.c.Parameter;
import code.c.State;

public class Block implements block.lan.MCodeBlock,block.lan.CCodeBlock{
	protected String blockType;
	protected String blockName;
	
	protected int blockId=0;
	
	protected JSONObject paramValues;
	
	protected Vector<InputPort> inputPortList=new Vector<InputPort>();
	protected Vector<OutputPort> outputPortList=new Vector<OutputPort>();
	
	protected Vector<Parameter> parameterList=new Vector<Parameter>();
	protected Vector<State> stateList=new Vector<State>();
	
	protected boolean isOutputCodeGenerated=false;
	
	protected NCSLabModel model;
	
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
	
	public String generateOutputCodeM() {
		String code="";
		
		return code;
	}
	
	public String generateBlockOutputCodeM() {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		String code=generateOutputCodeM();
		
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}
		
		return code;
	}
	
	public boolean getIsOutputCodeGenerated() {
		return this.isOutputCodeGenerated;
	}
	
	public String generateBlockInitCodeM() {
		String code=generateInitCodeM();
		return code;
	}
	
	public String generateInitCodeM() {
		String code="";
		
		return code;
	}
	
	public String generateBlockUpdateCodeM() {
		String code=generateUpdateCodeM();
		return code;
	}
	
	public String generateUpdateCodeM() {
		String code="";
		
		return code;
	}
	
	public void generateBlockInitCodeC(CodeStructC code) {
		for(OutputPort outputPort:outputPortList) {
			code.addOutputSignal(this, outputPort);
		}
		generateInitCodeC(code);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		
	}
	
	public void generateBlockOutputCodeC(CodeStructC code) {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		generateOutputCodeC(code);
		
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}
		
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		
	}
	
	public void generateBlockUpdateCodeC(CodeStructC code) {
		generateUpdateCodeC(code);
	}
	
	public void generateUpdateCodeC(CodeStructC code) {
	}
}
