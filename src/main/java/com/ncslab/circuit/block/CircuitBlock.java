package com.ncslab.circuit.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.ncslab.circuit.block.io.*;
import com.ncslab.circuit.CircuitModel;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.communication.CircuitBlockDto;
import com.ncslab.dto.common.TypedParameter;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.InputPort;

import com.ncslab.block.sink.Scope;
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
	
	protected BlockModeType blockModeType=BlockModeType.Anything;
	
	protected BlockMode blockMode;
	
	//等价的Block组合
	protected List<Block> blockList = new ArrayList<>();
	
	//等价的Line组合
	protected List<Line> lineList = new ArrayList<>();

	// 电气端口的列表
	protected List<CircuitPort> circuitPortList = new ArrayList<>();
	
	//输出与外界连接的等价Block列表
	protected List<Block> outputBlockList = new ArrayList<>();
	
	//输入与外界连接的等价Block列表
	//protected List<Block> inputBlockList = new ArrayList<>();
	protected List<InputPort> inputPortList = new ArrayList<>();
	
	//模块的电压方程
	private List<BlockVoltage> voltageList = new ArrayList<>();
	
	//与其他的CircuitBlock生成的Block相连的输出Block
	private Block outputBlock;
	//与其他的CircuitBlock生成的Block相连的输入Block
	private Block inputBlock;
	
	//模块的输出值,测试用
	private Block terminalBlock;
	
	private CircuitModel circuitModel=null;

	/**
	 * Legacy JSON Constructor - Creates circuit block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockIn JSON object containing circuit block configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	protected CircuitBlock(JSONObject blockIn, NCSLabModel model) {
		this.blockType = blockIn.getString("blockType");
		this.blockName = blockIn.getString("blockName");
		this.paramValues = blockIn.getJSONObject("paramValues");
		this.model = model;
		this.blockPath = blockIn.getString("blockPath");

		circuitPortList.add(new CircuitPort(this,"LConn1",CircuitPortType.Left,1));
		circuitPortList.add(new CircuitPort(this,"RConn1",CircuitPortType.Right,2));

		setupBlockModeType();
		//setupBlockList();
	}

	/**
	 * DTO-Native Constructor - Creates circuit block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing circuit block configuration
	 * @param model Parent model reference
	 */
	protected CircuitBlock(CircuitBlockDto dto, NCSLabModel model) {
		this.blockType = dto.getBlockType();
		this.blockName = dto.getBlockName();
		this.blockPath = dto.getBlockPath();
		this.model = model;

		// Create paramValues JSONObject from DTO parameters
		this.paramValues = new JSONObject();
		if (dto.getParameters() != null) {
			for (Map.Entry<String, TypedParameter> entry : dto.getParameters().entrySet()) {
				this.paramValues.put(entry.getKey(), entry.getValue().getAsString());
			}
		}

		// Add circuit-specific parameters
		Map<String, Object> circuitParams = dto.createCircuitParamValues();
		for (Map.Entry<String, Object> entry : circuitParams.entrySet()) {
			if (!this.paramValues.has(entry.getKey())) {
				this.paramValues.put(entry.getKey(), entry.getValue());
			}
		}

		circuitPortList.add(new CircuitPort(this,"LConn1",CircuitPortType.Left,1));
		circuitPortList.add(new CircuitPort(this,"RConn1",CircuitPortType.Right,2));

		setupBlockModeType();
		//setupBlockList();

		System.out.println("DTO-NATIVE: CircuitBlock created successfully - " + dto.getBlockName());
	}
	
	public void setCircuitModel(CircuitModel circuitModel) {
		this.circuitModel=circuitModel;
	}
	
	public CircuitModel getCircuitModel() {
		return this.circuitModel;
	}
	
	public List<Block> getOutputBlockList() {
		return this.outputBlockList;
	}
	
	public List<InputPort> getInputPortList() {
		return this.inputPortList;
	}
	
	public void setupBlocks() {
		setupBlockList();
		//addTerminalBlock(); 
	}
	
	public void setupBlockConnections() {
		setupBlockListConnections();
	}
	
	protected void setupBlockListConnections() {
		if(inputBlock==null) {
			return;
		}
		
		List<InputPort> inputPortList=inputBlock.getInputPortList();
		for(int i=0;i<inputPortList.size();i++) {
			InputPort input=inputPortList.get(i);
			OutputPort output=null;
			switch(blockMode) {
			case Branch:
				PortCurrent current=this.getCurcuitPortList().get(0).getCurrentList().get(i);
				output=current.getCircuitBlock().getOutputPort();
				break;
			case Link:
				BlockVoltage voltage=getVoltageList().get(i);
				output=voltage.getCircuitBlock().getOutputPort();
				break;
			}
			
			List<Block> blocks = new ArrayList<>();
			blocks.add(input.getBlock());
			blocks.add(output.getBlock());
			
			createLine(output.getBlock().getBlockName(), 1, input.getBlock().getBlockName(), i+1,blocks);
		}
		
		
	}
	
	private void addTerminalBlock() {
		JSONObject terminatorJSON=new JSONObject();
		terminatorJSON.put("blockType", "Scope");
		String type=null;
		switch(this.blockMode) {
		case Branch:
			type="_voltage";
			break;
		case Link:
			type="_current";
			break;
		}
		terminatorJSON.put("blockName", this.blockName.replaceAll(" ", "_")+type);
		terminatorJSON.put("blockPath", this.blockPath);
		JSONObject terminatorParamValues=new JSONObject();
		terminatorJSON.put("paramValues", terminatorParamValues);
		
		terminalBlock=new Scope(terminatorJSON,this.model);
		this.blockList.add(terminalBlock);
		
		createLine(outputBlock.getBlockName(), 1, terminalBlock.getBlockName(), 1);
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
	
	public List<Block> getBlockList(){
		/*
		for(Block block:blockList) {
			System.out.println(block.getBlockName());
		}*/
		return this.blockList;
	}
	
	public List<Line> getLineList(){
		return this.lineList;
	}
	
	public void setVoltageList(List<BlockVoltage> voltageList) {
		this.voltageList=voltageList;
	}
	
	public List<BlockVoltage> getVoltageList(){
		return this.voltageList;
	}
	
	//每个模块根据自己的模式,以及电压和电流方程,生成对应的Block
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
	
	public List<CircuitPort> getCurcuitPortList(){
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
	
	/*
	public void searchBlock(List<CircuitBlock> blockPath,CircuitPort port) {
		CircuitNode node=port.getCircuitNode();
		node.getOtherCircuitPortList(port);
	}*/
	
	protected void createLine(String fromBlockName,int fromBlockNum,String toBlockName,int toBlockNum) {
		JSONObject lineObject=new JSONObject();
		lineObject.put("fromBlockName", fromBlockName);
		lineObject.put("fromPortNo", fromBlockNum);
		
		lineObject.put("toBlockName", toBlockName);
		lineObject.put("toPortNo", toBlockNum);
		
		Line line=Line.createLine(lineObject,blockList);
		lineList.add(line);
	}
	
	protected void createLine(String fromBlockName,int fromBlockNum,String toBlockName,int toBlockNum,List<Block> blockList) {
		JSONObject lineObject=new JSONObject();
		lineObject.put("fromBlockName", fromBlockName);
		lineObject.put("fromPortNo", fromBlockNum);
		
		lineObject.put("toBlockName", toBlockName);
		lineObject.put("toPortNo", toBlockNum);
		
		Line line=Line.createLine(lineObject,blockList);
		lineList.add(line);
	}
}
