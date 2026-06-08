package com.ncslab.circuit2.block.multielement;

import org.json.JSONObject;

import com.ncslab.circuit2.CircuitModel2;
import com.ncslab.circuit2.block.baseelement.CircuitBlockMulti;
import com.ncslab.circuit2.block.element.DCVoltageSource;
import com.ncslab.circuit2.block.element.Resistor;
import com.ncslab.circuit2.block.multielement.base.SwitchBase;
import com.ncslab.circuit2.line.CircuitLine;
import com.ncslab.ncslablink.NCSLabModel;

public class Mosfet extends CircuitBlockMulti implements Recalc {
	
	private String rOnString;
	private String rDString;
	private String vFString;
	private String rSString;
	private String cSString;
	private SwitchBase sw1;
	private SwitchBase sw2;
	
	private com.ncslab.block.elec.Mosfet mosfet;
	public Mosfet(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		rOnString=paramValues.getString("Ron");
		rDString=paramValues.getString("Rd");
		vFString=paramValues.getString("Vf");
		rSString=paramValues.getString("Rs");
		cSString=paramValues.getString("Cs");
		createBlock();
	}

	private void createBlock() {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "IGBT");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		mosfet=new com.ncslab.block.elec.Mosfet(addJSON,model,this);
		model.addElectBlock(mosfet);
	}
	
	@Override
	public String getOldStatusString() {
		return this.getBlockName()+"_oldStatus";
	}

	@Override
	public String getStatusString() {
		return this.getBlockName()+"_status";
	}

	@Override
	public String getIsRecalcCode() {
		String code="";
		
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		//注意二极管是反接的
		code+="int "+this.getStatusString()+"=("+v1+"-"+v0+">"+vFString+")"+";\n";
		
		//code+="if("+this.getOldStatusString()+"^"+this.getStatusString()+"){\n";

		//如果计算之后二极管从关闭到打开，就需要重计算
		//似乎二极管关断的时候不需要重新计算,因为二极管被来就有反向恢复的特性,这个还要检测
		code+="if("+this.getOldStatusString()+"==0&&"+this.getStatusString()+"==1){\n";
		code+=isRecalcString+"=1;\n";
		code+="}\n";
		
		return code;
	}

	@Override
	public void setupSubCircuitBlocks(int blockId) {
		
		//构建MOSFET中的Ron电阻,因为需要测电流,使用SeriesRLCBranch
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "SeriesRLCBranch");
		addJSON.put("blockName", this.blockName+"Ron");
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addParamValues.put("Branchtype", "R");
		addParamValues.put("R", rOnString);
		addParamValues.put("L", "");
		addParamValues.put("C", "");
		addJSON.put("paramValues", addParamValues);
		
		Resistor rOn=new Resistor(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(rOn);
		
		//构建MOSFET中的开关，栅极输入>0就打通，否则关断
		addJSON=new JSONObject();
		addJSON.put("blockType", "SwitchBase");
		addJSON.put("blockName", this.blockName+"sw1");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		
		sw1=new SwitchBase(blockId++,addJSON,model,this);
		this.getSingleCurcuitBlockList().add(sw1);
		
		//建立连线，将开关与电阻串联起来
		CircuitLine circuitLine=new CircuitLine(sw1.getCurcuitPortList().get(1),rOn.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		//开关-电阻串联，用开关的左port和和压降的右port替换igbt的两侧port，与外界相连
		replacePort(this.getCurcuitPortList().get(0),sw1.getCurcuitPortList().get(0));
		replacePort(this.getCurcuitPortList().get(1),rOn.getCurcuitPortList().get(1));
		
		//用开关的左port和和压降的右port替换igbt的两侧port
		this.getCurcuitPortList().set(0, sw1.getCurcuitPortList().get(0));
		this.getCurcuitPortList().set(1, rOn.getCurcuitPortList().get(1));
		
		//构建MOSFET中的二极管，与开关-电阻并联
		//构建二极管中的电阻
		addJSON=new JSONObject();
		addJSON.put("blockType", "Reisitor");
		addJSON.put("blockName", this.blockName+"Rd");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		
		addParamValues.put("R", rDString);
		addJSON.put("paramValues", addParamValues);
		
		Resistor rD=new Resistor(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(rD);
		
		//使用电压源构建二极管的恒压降
		addJSON=new JSONObject();
		addJSON.put("blockType", "DC Voltage Source");
		addJSON.put("blockName", this.blockName+"Vf");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		
		addParamValues.put("v0", vFString);
		addJSON.put("paramValues", addParamValues);
		
		DCVoltageSource vF=new DCVoltageSource(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(vF);
		
		//构建二极管中的开关，正向电压大于恒压降，就打通，否则关断
		addJSON=new JSONObject();
		addJSON.put("blockType", "SwitchBase");
		addJSON.put("blockName", this.blockName+"sw2");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		
		addJSON.put("paramValues", addParamValues);
		
		sw2=new SwitchBase(blockId++,addJSON,model,this);
		this.getSingleCurcuitBlockList().add(sw2);
		
		//建立连线，将恒压降与Rd电阻串联起来
		circuitLine=new CircuitLine(rD.getCurcuitPortList().get(1),vF.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		//建立连线，将开关与Rd电阻串联起来
		circuitLine=new CircuitLine(sw2.getCurcuitPortList().get(1),rD.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		//建立连线，将恒压降与电阻串联起来
		circuitLine=new CircuitLine(this.getCurcuitPortList().get(1),sw2.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		//建立连线，将开关与电阻串联起来
		circuitLine=new CircuitLine(vF.getCurcuitPortList().get(1),this.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
	}

	@Override
	public void setupLogicCode() {
		sw1.setSwitchCode(this.getSwtichCode1());
		sw2.setSwitchCode(this.getSwtichCode2());
	}
	
	@Override
	public void setupLogicCode(int partId) {
		sw1.setSwitchCodePart(this.getSwtichCode1(partId));
		sw2.setSwitchCodePart(this.getSwtichCode2(partId));
	}
	
	//需要修改
	private String getSwtichCode1() {
		String sCode="";
		String inputName = "0.0";
		if(mosfet.getInputPortList().get(0).getLinkedLine() != null) {
			inputName = mosfet.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		}
		
		//根据开关逻辑，控制是否需要合并矩阵
		sCode+="if("+inputName+"> 0"+"){\n";
		sCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+sw1.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+sw1.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		sCode+="}\n";
		
		return sCode;
	}
	
	private String getSwtichCode2() {
		String sCode="";
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		//计算开关器件的开关逻辑，并记录为oldStatus
		sCode+="int "+this.getOldStatusString()+"=("+v1+"-"+v0+">"+vFString+")"+";\n";
		
		//根据开关逻辑，控制是否需要合并矩阵
		sCode+="if("+v1+"-"+v0+">"+vFString+"){\n";
		sCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+sw2.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+sw2.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		sCode+="}\n";
		
		return sCode;
	}
	
	private String getSwtichCode1(int pratId) {
		String sCode="";
		String inputName = "0.0";
		if(mosfet.getInputPortList().get(0).getLinkedLine() != null) {
			inputName = mosfet.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		}
		
		//根据开关逻辑，控制是否需要合并矩阵
		sCode+="if("+inputName+"> 0"+"){\n";
		sCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+sw1.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+sw1.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		sCode+="}\n";
		
		return sCode;
	}
	
	private String getSwtichCode2(int pratId) {
		String sCode="";
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		//计算开关器件的开关逻辑，并记录为oldStatus
		sCode+="int "+this.getOldStatusString()+"=("+v1+"-"+v0+">"+vFString+")"+";\n";
		
		//根据开关逻辑，控制是否需要合并矩阵
		sCode+="if("+v1+"-"+v0+">"+vFString+"){\n";
		sCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+sw2.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+sw2.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		sCode+="}\n";
		
		return sCode;
	}
	
	public String getCurrentString() {
		return "("+this.getSingleCurcuitBlockList().get(0).getCurrentString()+"-"+this.getSingleCurcuitBlockList().get(3).getCurrentString()+")";
	}
	
	public String getVoltageString() {
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		String vString="("+v0+"-"+v1+")";
		return vString;
	}
}
