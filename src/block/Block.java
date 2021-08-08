package block;

import java.util.Vector;
import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;

import ncslablink.NCSLabModel;

public class Block {
	protected String blockType;
	protected String blockName;
	
	protected int blockId=0;
	
	protected JSONObject paramValues;
	
	protected Vector<InputPort> inputPortList=new Vector<InputPort>();
	protected Vector<OutputPort> outputPortList=new Vector<OutputPort>();
	
	protected boolean isOutputCodeGenerated=false;
	
	protected String initCode;
	
	protected NCSLabModel model;
	
	protected Block(JSONObject blockIn,NCSLabModel model) {
		this.blockType=blockIn.getString("blockType");
		this.blockName=blockIn.getString("blockName");
		this.paramValues=blockIn.getJSONObject("paramValues");
		
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
	
	public void setBlockId(int blockId) {
		this.blockId=blockId;
	}
	
	public int getBlockId() {
		return this.blockId;
	}
	
	public String generateOutputCode() {
		String code="";
		
		return code;
	}
	
	public String generateBlockOutputCode() {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		String code=generateOutputCode();
		
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}
		
		return code;
	}
	
	public boolean getIsOutputCodeGenerated() {
		return this.isOutputCodeGenerated;
	}
	
	public String generateInitCode() {
		String code="";
		
		this.initCode=code;
		return code;
	}
	
	public String generateUpdateCode() {
		String code="";
		
		this.initCode=code;
		return code;
	}
}
