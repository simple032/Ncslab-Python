package com.ncslab.circuit;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.List;

import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.CircuitBlockType;
import com.ncslab.circuit.line.CircuitLine;
import com.ncslab.circuit.CircuitModel;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.dto.CircuitBlockJson;
import com.ncslab.dto.BlockJson;
import com.ncslab.dto.LineJson;

import com.ncslab.ncslablink.ModelException;

public class CircuitParser {

	private NCSLabModel model;
	private List<CircuitBlock> blockList = new ArrayList<>();
	private List<CircuitLine> lineList = new ArrayList<>();

	@Getter
    private CircuitModel circuitModel;

	private static boolean isCircuitBlock(JSONObject blockJSON) {
		String srcBlock=blockJSON.getString("srcBlock");
		//System.out.println(srcBlock);
		if(srcBlock.startsWith("fl_lib")||srcBlock.startsWith("elec_lib")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * DTO-based method to check if a block is a circuit block
	 * @param blockDto Block DTO
	 * @return true if circuit block
	 */
	private static boolean isCircuitBlock(BlockJson blockDto) {
		String srcBlock = blockDto.getSrcBlock();
		return srcBlock != null && (srcBlock.startsWith("fl_lib") || srcBlock.startsWith("elec_lib"));
	}
	
	/**
	 * Enhanced method that works with both DTO and JSONObject
	 * @param blockDto Block DTO (preferred)
	 * @param blockJSON Block JSONObject (fallback)
	 * @return true if circuit block
	 */
	private static boolean isCircuitBlockEnhanced(BlockJson blockDto, JSONObject blockJSON) {
		if (blockDto != null) {
			return isCircuitBlock(blockDto);
		} else if (blockJSON != null) {
			return isCircuitBlock(blockJSON);
		}
		return false;
	}

	private boolean isCircuitLine(JSONObject lineJSON) {

		String fromBlockName=lineJSON.getString("fromBlockName");
		String toBlockName=lineJSON.getString("toBlockName");

		int i;
		for(i=0;i<blockList.size();i++) {
			CircuitBlock block=blockList.get(i);
			if(block.getBlockName().equals(fromBlockName)) {
				break;
			}
		}
		if(i==blockList.size()) {
			return false;
		}

		for(i=0;i<blockList.size();i++) {
			CircuitBlock block=blockList.get(i);
			if(block.getBlockName().equals(toBlockName)) {
				break;
			}
		}
		if(i==blockList.size()) {
			return false;
		}

		return true;
	}
	
	/**
	 * DTO-based method to check if a line is a circuit line
	 * @param lineDto Line DTO
	 * @return true if circuit line
	 */
	private boolean isCircuitLine(LineJson lineDto) {
		String fromBlockName = lineDto.getFromBlockName();
		String toBlockName = lineDto.getToBlockName();

		// Check if both blocks exist in circuit block list
		boolean foundFromBlock = false;
		boolean foundToBlock = false;
		
		for (CircuitBlock block : blockList) {
			if (block.getBlockName().equals(fromBlockName)) {
				foundFromBlock = true;
			}
			if (block.getBlockName().equals(toBlockName)) {
				foundToBlock = true;
			}
		}
		
		return foundFromBlock && foundToBlock;
	}
	
	/**
	 * Enhanced method that works with both DTO and JSONObject for line checking
	 * @param lineDto Line DTO (preferred)
	 * @param lineJSON Line JSONObject (fallback)
	 * @return true if circuit line
	 */
	private boolean isCircuitLineEnhanced(LineJson lineDto, JSONObject lineJSON) {
		if (lineDto != null) {
			return isCircuitLine(lineDto);
		} else if (lineJSON != null) {
			return isCircuitLine(lineJSON);
		}
		return false;
	}

	private boolean isFromCircuitLine(JSONObject lineJSON) {
		String fromBlockName=lineJSON.getString("fromBlockName");

		int i;
		for(i=0;i<blockList.size();i++) {
			CircuitBlock block=blockList.get(i);
			if(block.getBlockName().equals(fromBlockName)) {
				break;
			}
		}
		if(i==blockList.size()) {
			return false;
		}

		return true;
	}

	public CircuitParser(NCSLabModel model) throws ModelException{
		this.model=model;

		//剥离所有的电路模块和电路连接线,建立电气模块blockList和线路lineList
		parseCircuit();

		//建立电路模型CircuitModel,建立节点模型,产生生成树
		circuitModel=CircuitModel.CreateCircuitModel(model, blockList, lineList);

		//根据生成的Block电压方程和node电流方程,建立等效的M2PLink的block
		circuitModel.setupModel();

		//处理与外部相连的模块的连接线
		parseExternalConnections();
	}


	//修改JSON中原来与CircuitBlock的连接线,变成与CircuitBlock输出端口相连的连接线
	private void parseFromLine(JSONObject lineJSON) {
		String fromBlockName=lineJSON.getString("fromBlockName");

		CircuitBlock fromCircuitBlock=null;
		for(CircuitBlock circuitBlock:blockList) {
			if(circuitBlock.getBlockName().equals(fromBlockName)) {
				fromCircuitBlock=circuitBlock;
			}
		}

		//改变连接线
		Block fromBlock=fromCircuitBlock.getOutputBlockList().get(0);
		//System.out.println(fromBlock.getBlockName()+":"+fromBlock.getOutputPortList().get(0).getNumber());
		lineJSON.put("fromBlockName", fromBlock.getBlockName());
        lineJSON.put("fromBlockUUID", fromBlock.getBlockUUID());
		lineJSON.put("fromPortNo", fromBlock.getOutputPortList().get(0).getNumber());
		lineJSON.put("toPortNo", "1");
	}

	private boolean isToCircuitLine(JSONObject lineJSON) {
		String toBlockName=lineJSON.getString("toBlockName");

		int i;
		for(i=0;i<blockList.size();i++) {
			CircuitBlock block=blockList.get(i);
			if(block.getBlockName().equals(toBlockName)) {
				break;
			}
		}
		if(i==blockList.size()) {
			return false;
		}

		return true;
	}

	//修改JSON中原来与CircuitBlock的连接线,变成与CircuitBlock输出端口相连的连接线
		private void parseToLine(JSONObject lineJSON) {
			String toBlockName=lineJSON.getString("toBlockName");

			CircuitBlock toCircuitBlock=null;
			for(CircuitBlock circuitBlock:blockList) {
				if(circuitBlock.getBlockName().equals(toBlockName)) {
					toCircuitBlock=circuitBlock;
				}
			}


			//改变连接线
			InputPort toPort=toCircuitBlock.getInputPortList().get(0);
			Block toBlock=toPort.getBLock();
			//System.out.println(fromBlock.getBlockName()+":"+fromBlock.getOutputPortList().get(0).getNumber());
			lineJSON.put("toBlockName", toBlock.getBlockName());
            lineJSON.put("toBlockUUID", toBlock.getBlockUUID());
			lineJSON.put("toPortNo", toPort.getNumber());
			lineJSON.put("fromPortNo", "1");
		}


	//处理与外部相连的模块的线路
	private void parseExternalConnections() {
		JSONArray lineJSONList=model.getLinesJSON();
		int i=0;
		while(i<lineJSONList.length()) {
			JSONObject lineJSON=lineJSONList.getJSONObject(i);
			//System.out.println(lineJSON);
			if(isFromCircuitLine(lineJSON)) {
				//修改JSON中原来与CircuitBlock的连接线,变成与CircuitBlock输出端口相连的连接线
				parseFromLine(lineJSON);
			}
			else
			if(isToCircuitLine(lineJSON)) {
				//System.out.println(lineJSON);
				parseToLine(lineJSON);

				//System.out.println(lineJSON);
			}
			i++;
		}
	}

    //剥离所有的电路模块和电路连接线,建立电气模块blockList和线路lineList
	private void parseCircuit() throws ModelException{
		//剥离所有的电气模块
		int id=1;
		JSONArray blockJSONList=model.getBlocksJSON();
		int i=0;
		while(i<blockJSONList.length()) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);
			//System.out.println(blockJSON);
			//如果是电路模块
			if(isCircuitBlock(blockJSON)) {
				//System.out.println(blockJSON);
				//建立电路模块
				CircuitBlock block=CircuitBlockType.createBlock(id++, blockJSON, model);
				blockList.add(block);
				blockJSONList.remove(i);
			}
			else {
				i++;
			}
		}

		//剥离所有的电气连接线
		JSONArray lineJSONList=model.getLinesJSON();
		i=0;
		while(i<lineJSONList.length()) {
			JSONObject lineJSON=lineJSONList.getJSONObject(i);
			//System.out.println(lineJSON);

			if(isCircuitLine(lineJSON)) {
				//System.out.println(lineJSON);

				CircuitLine line=new CircuitLine(lineJSON,blockList);
				lineList.add(line);

				lineJSONList.remove(i);
			}
			else {
				i++;
			}

		}
	}

	public void showBlocks() {
		for(CircuitBlock block:blockList) {
			System.out.println("+++++++++++++++++++++++++++");
			System.out.println("Name: "+block.getBlockName());
			System.out.println("Type: "+block.getBlockType());
			//System.out.println("In: "+block.getInputPortList().size()+" Out:"+block.getOutputPortList().size());
		}
		for(CircuitLine line:lineList) {
			System.out.println("--------------------------");
			System.out.println("FromBlock: "+line.getFromPort().getBlock().getBlockName()+"("+line.getFromPort().getName()+")");
			System.out.println("ToBlock: "+line.getToPort().getBlock().getBlockName()+"("+line.getToPort().getName()+")");
		}
	}
}
