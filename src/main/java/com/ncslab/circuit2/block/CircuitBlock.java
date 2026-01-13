package com.ncslab.circuit2.block;

import java.util.Vector;

import org.json.JSONObject;

//External libraries
import lombok.Getter;
import lombok.Setter;

import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.block.io.CircuitPortType;
import com.ncslab.circuit.block.io.PortCurrent;
import com.ncslab.circuit.block.io.BlockVoltage;
import com.ncslab.circuit2.block.io.CircuitNode;
import com.ncslab.circuit2.CircuitModel2;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.InputPort;

import com.ncslab.block.sink.Scope;
import com.ncslab.block.source.Constant;
import com.ncslab.line.Line;

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
	
	protected BlockModeType blockModeType=BlockModeType.Nromal;

	// 电气端口的列表
	protected Vector<CircuitPort> circuitPortList = new Vector<CircuitPort>();
	
	//输出与外界连接的等价Block列表
	protected Vector<Block> outputBlockList=new Vector<Block>();
	
	//输入与外界连接的等价Block列表
	//protected Vector<Block> inputBlockList=new Vector<Block>();
	protected Vector<InputPort> inputPortList=new Vector<InputPort>();
	
	//模块的电压方程
	private Vector<BlockVoltage> voltageList=new Vector<BlockVoltage>();
	
	//与其他的CircuitBlock生成的Block相连的输出Block
	private Block outputBlock;
	//与其他的CircuitBlock生成的Block相连的输入Block
	private Block inputBlock;
	
	//模块的输出值,测试用
	private Block terminalBlock;
	
	@Getter
	private String blockUUID="null";
	
	protected static String simpleTime="(model.stepSize)";
	
	private CircuitModel2 circuitModel=null;

	protected CircuitBlock(int id,JSONObject blockIn, NCSLabModel model) {
		this.blockType = blockIn.getString("blockType");
		this.blockName = blockIn.getString("blockName");
		this.paramValues = blockIn.getJSONObject("paramValues");
		this.model = model;
		this.blockPath = blockIn.getString("blockPath");
		this.blockId=id;
		
		if(blockIn.isNull("blockUUID")) {
			this.blockUUID="null";
		}
		else {
			this.blockUUID = blockIn.getString("blockUUID");
		}
		
		
		circuitPortList.add(new CircuitPort(this,"LConn1",CircuitPortType.Left,1));
		circuitPortList.add(new CircuitPort(this,"RConn1",CircuitPortType.Right,2));
		
		//setupBlockModeType();
		//setupBlockList();
	}
	
	protected double getSampleTimeValue() {
		return model.getConfig().getFixedStep();
	}
	
	protected CircuitBlock(JSONObject blockIn, NCSLabModel model) {
		this.blockType = blockIn.getString("blockType");
		this.blockName = blockIn.getString("blockName");
		this.paramValues = blockIn.getJSONObject("paramValues");
		this.model = model;
		this.blockPath = blockIn.getString("blockPath");
		
		circuitPortList.add(new CircuitPort(this,"LConn1",CircuitPortType.Left,1));
		circuitPortList.add(new CircuitPort(this,"RConn1",CircuitPortType.Right,2));
		
		//setupBlockModeType();
		//setupBlockList();
	}
	
	protected String getSimpleTime() {
		return this.simpleTime;
	}
	
	public void setBlockId(int id) {
		this.blockId=id;
	}
	
	public String getBlockPath() {
		return this.blockPath;
	}
	
	public int getBlockId() {
		return this.blockId;
	}
	
	public BlockModeType getBlockModeType() {
		return this.blockModeType;
	}
	
	public void setCircuitModel(CircuitModel2 circuitModel) {
		this.circuitModel=circuitModel;
	}
	
	public CircuitModel2 getCircuitModel() {
		return this.circuitModel;
	}
	
	public Vector<Block> getOutputBlockList() {
		return this.outputBlockList;
	}
	
	public Vector<InputPort> getInputPortList() {
		return this.inputPortList;
	}
	
	public void setupBlocks() {
		//setupBlockList();
		//addTerminalBlock(); 
	}
	
	public void setOutputBlock(Block outputPort) {
		this.outputBlock=outputPort;
	}
	
	public void setupInputBlock(Block inputBlock) {
		this.inputBlock=inputBlock;
	}
	
	public OutputPort getOutputPort() {
		return outputBlock.getOutputPortList().get(0);
	}
	
	public void setVoltageList(Vector<BlockVoltage> voltageList) {
		this.voltageList=voltageList;
	}
	
	public Vector<BlockVoltage> getVoltageList(){
		return this.voltageList;
	}
	
	//注意处理非双端口的模块,目前只有接地和运放.
	public CircuitPort getAnotherCircuitPort(CircuitPort circuitPort) {
		if(blockModeType == BlockModeType.Ground) {
			return circuitPortList.get(0);
		}else
		if(circuitPort==circuitPortList.get(0)) {
			return circuitPortList.get(1);
		}
		else
		if(circuitPort==circuitPortList.get(1)) {
			return circuitPortList.get(0);
		}else
		//目前只有运放
		if(circuitPort==circuitPortList.get(2)){
			return circuitPortList.get(2);
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
	
	public String getCircuitStatusDefineCode() {
		return "";
	}
	
	private int switchPartId=0;

	public void setSwitchPartId(int switchPartId) {
		// TODO Auto-generated method stub
		this.switchPartId=switchPartId;
	}

	public int getSwitchPartId() {
		// TODO Auto-generated method stub
		return this.switchPartId;
	}
}
