package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.greenpineyu.fel.parser.FelParser.integerLiteral_return;

import com.ncslab.block.io.Parameter;
import com.ncslab.circuit2.block.baseelement.CircuitBlockMulti;
import com.ncslab.circuit2.block.multielement.base.SwitchBase;
import com.ncslab.circuit2.line.CircuitLine;
import com.ncslab.ncslablink.NCSLabModel;

public class SeriesRLCBranch extends CircuitBlockMulti {
	
	private String typeString;
	private String rString;
	private String lString;
	private String cString;
	
	public SeriesRLCBranch(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		typeString = paramValues.getString("Branchtype");

		rString = paramValues.getString("R");
		lString = paramValues.getString("L");
		cString = paramValues.getString("C");
		
	}
	
	public SeriesRLCBranch(JSONObject blockJSON, NCSLabModel model) {
		super(0, blockJSON, model);
		// TODO Auto-generated constructor stub
		typeString = paramValues.getString("Branchtype");

		rString = paramValues.getString("R");
		lString = paramValues.getString("L");
		cString = paramValues.getString("C");
		
	}

	@Override
	public void setupSubCircuitBlocks(int blockId) {
		// TODO Auto-generated method stub
		
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Reisitor");
		addJSON.put("blockName", this.blockName+"R");
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		
		addParamValues.put("R", rString);
		addJSON.put("paramValues", addParamValues);
		
		Resistor rBlock=new Resistor(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(rBlock);
		
		addJSON=new JSONObject();
		addJSON.put("blockType", "Inductor");
		addJSON.put("blockName", this.blockName+"L");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		
		addParamValues.put("l", lString);
		addJSON.put("paramValues", addParamValues);
		
		Inductor lBlock=new Inductor(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(lBlock);
		
		addJSON=new JSONObject();
		addJSON.put("blockType", "Capacitor");
		addJSON.put("blockName", this.blockName+"C");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		
		addParamValues.put("c", cString);
		addJSON.put("paramValues", addParamValues);
		
		Capacitor cBlock=new Capacitor(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(cBlock);
	
		//建立连线，将恒压降与电阻串联起来
		CircuitLine circuitLine=new CircuitLine(rBlock.getCurcuitPortList().get(1),lBlock.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		//建立连线，将开关与电阻串联起来
		circuitLine=new CircuitLine(lBlock.getCurcuitPortList().get(1),cBlock.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		
		//开关-电阻-恒压降串联，用开关的左port和和压降的右port替换diode的两侧port，与外界相连
		replacePort(this.getCurcuitPortList().get(0),rBlock.getCurcuitPortList().get(0));
		replacePort(this.getCurcuitPortList().get(1),cBlock.getCurcuitPortList().get(1));
		
		//用开关的左port和和压降的右port替换diode的两侧port
		this.getCurcuitPortList().set(0, rBlock.getCurcuitPortList().get(0));
		this.getCurcuitPortList().set(1, cBlock.getCurcuitPortList().get(1));
		
	}

	@Override
	public void setupLogicCode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupLogicCode(int partId) {
		// TODO Auto-generated method stub
		
	}

}
