package circuit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;

import ncslablink.NCSLabModel;
import circuit.block.CircuitBlock;
import circuit.block.CircuitBlockType;
import circuit.line.CircuitLine;
import circuit.CircuitModel;

import ncslablink.ModelException;

public class CircuitParser {
	
	private NCSLabModel model;
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	private Vector<CircuitLine> lineList=new Vector<CircuitLine>();
	
	private CircuitModel circuitModel;
	
	private static boolean isCircuitBlock(JSONObject blockJSON) {
		String srcBlock=blockJSON.getString("srcBlock");
		//System.out.println(srcBlock);
		if(srcBlock.startsWith("fl_lib")) {
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
	
	public CircuitParser(NCSLabModel model) throws ModelException{
		this.model=model;
		parseCircuit();
		
		circuitModel=CircuitModel.CreateCircuitModel(model, blockList, lineList);
	}
	
	public CircuitModel getCircuitModel() {
		return this.circuitModel;
	}
	
	public void parseCircuit() throws ModelException{
		
		//剥离所有的电气模块
		int id=1;
		JSONArray blockJSONList=model.getBlocksJSON();
		int i=0;
		while(i<blockJSONList.length()) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);
			if(isCircuitBlock(blockJSON)) {
				//System.out.println(blockJSON);
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
