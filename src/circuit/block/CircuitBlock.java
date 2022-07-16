package circuit.block;

import java.util.Vector;

import org.json.JSONObject;

import circuit.block.io.CircuitPort;
import ncslablink.NCSLabModel;

import block.Block;

import line.Line;

abstract public class CircuitBlock {
	protected String blockType;
	protected String blockName;

	protected int blockId = 0;

	// Block所在画布的位置，不在子系统时为modelName，存在子系统时为modelName/subsystem
	protected String blockPath;
	// Block的参数，因为不同的block有不同的参数，因此以原生的json格式存储
	protected JSONObject paramValues;
	// 指向上级Model模型的指针
	protected NCSLabModel model;
	
	protected BlockModeType blockModeType=BlockModeType.Anything;
	
	protected BlockMode blockMode;
	
	//等价的Block组合
	protected Vector<Block> blockList=new Vector<Block>();
	
	//等价的Line组合
	protected Vector<Line> lineList=new Vector<Line>();

	// 电气端口的列表
	protected Vector<CircuitPort> circuitPortList = new Vector<CircuitPort>();

	protected CircuitBlock(JSONObject blockIn, NCSLabModel model) {
		this.blockType = blockIn.getString("blockType");
		this.blockName = blockIn.getString("blockName");
		this.paramValues = blockIn.getJSONObject("paramValues");
		this.model = model;
		this.blockPath = blockIn.getString("blockPath");
		
		circuitPortList.add(new CircuitPort(this,"LConn1",1));
		circuitPortList.add(new CircuitPort(this,"RConn1",2));
		
		setupBlockModeType();
		//setupBlockList();
	}
	
	public void setupBlocks() {
		setupBlockList();
	}
	
	protected abstract void setupBlockList();
	
	protected abstract void setupBlockModeType();
	
	public CircuitPort getAnotherCircuitPort(CircuitPort circuitPort) {
		if(circuitPort==circuitPortList.get(0)) {
			return circuitPortList.get(1);
		}
		else
		if(circuitPort==circuitPortList.get(1)) {
			return circuitPortList.get(0);
		}
		else {
			return null;
		}
	}
	
	public String getBlockName() {
		return this.blockName;
	}
	
	public Vector<CircuitPort> getCurcuitPortList(){
		return this.circuitPortList;
	}
	
	public String getBlockType() {
		return this.blockType;
	}
	
	public BlockMode getBlockMode() {
		return this.blockMode;
	}
	
	public void setBlockMode(BlockMode blockMode) {
		this.blockMode=blockMode;
	}
	
	public boolean isBranchPossible() {
		if(blockModeType==BlockModeType.Anything||blockModeType==BlockModeType.BranchOnly) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isBranchOnly() {
		if(blockModeType==BlockModeType.BranchOnly) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isAnything() {
		if(blockModeType==BlockModeType.Anything) {
			return true;
		}
		else {
			return false;
		}
	}
	
	protected void createLine(String fromBlockName,int fromBlockNum,String toBlockName,int toBlockNum) {
		JSONObject lineObject=new JSONObject();
		lineObject.put("fromBlockName", fromBlockName);
		lineObject.put("fromPortNo", fromBlockNum);
		
		lineObject.put("toBlockName", toBlockName);
		lineObject.put("toPortNo", toBlockNum);
		
		Line line=Line.createLine(lineObject,blockList);
		lineList.add(line);
	}
}
