package com.ncslab.circuit2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;

import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.CircuitBlockType;
import com.ncslab.circuit2.line.CircuitLine;
import com.ncslab.circuit2.CircuitModel2;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;

import com.ncslab.ncslablink.ModelException;

public class CircuitParser2 {
	
	private NCSLabModel model;
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	private Vector<CircuitLine> lineList=new Vector<CircuitLine>();
	
	private CircuitModel2 circuitModel;
	
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
	
	public CircuitParser2(NCSLabModel model) throws ModelException{
		this.model=model;
		
		//剥离所有的电路模块和电路连接线,建立电气模块blockList和线路lineList
		//parseCircuit();
		
		Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
		blockList.addAll(model.getRootSystem().getCircuitBlocks());
		
		Vector<CircuitLine> lineList=new Vector<CircuitLine>();
		lineList.addAll(model.getRootSystem().getCircuitLines());
		
		//建立电路模型CircuitModel,建立节点模型,产生生成树
		circuitModel=CircuitModel2.CreateCircuitModel(model,blockList,lineList);
	
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
			Block toBlock=toPort.getBlock();
			//System.out.println(fromBlock.getBlockName()+":"+fromBlock.getOutputPortList().get(0).getNumber());
			lineJSON.put("toBlockName", toBlock.getBlockName());
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
	
	public CircuitModel2 getCircuitModel() {
		return this.circuitModel;
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
