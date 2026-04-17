package com.ncslab.circuit2.block.multielement;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CircuitBlockMulti;
import com.ncslab.circuit2.block.element.DCVoltageSource;
import com.ncslab.circuit2.block.element.Resistor;
import com.ncslab.block.data.Data;
import com.ncslab.circuit2.block.multielement.base.SwitchBase;
import com.ncslab.circuit2.line.CircuitLine;
import com.ncslab.ncslablink.NCSLabModel;

public class IGBT extends CircuitBlockMulti implements Recalc {

	private String rOnString;
	private String lOnString;
	private String vFString;
	private String rSString;
	private String cSString;
	private SwitchBase sw;
	
	private block.elec.IGBT igbt;
	public IGBT(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		rOnString=Data.parseExpression(paramValues.getString("Ron"));
		lOnString=Data.parseExpression(paramValues.getString("Lon"));
		vFString=Data.parseExpression(paramValues.getString("Vf"));
		rSString=Data.parseExpression(paramValues.getString("Rs"));
		cSString=Data.parseExpression(paramValues.getString("Cs"));
		
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
		igbt=new block.elec.IGBT(addJSON,model,this);
		model.addElectBlock(igbt);
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
		String inputName = igbt.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();

		code+="int "+this.getStatusString()+"=("+v0+"-"+v1+">"+vFString+" && "+inputName+">0);\n";
		
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
		//构建IGBT中的电阻电感
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "SeriesRLCBranch");
		addJSON.put("blockName", this.blockName+"RLon");
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		
		String branchType = "RL";
		if(Double.parseDouble(rOnString)<=0) {
			branchType = "L";
		}
		if(Double.parseDouble(lOnString)<=0) {
			branchType = "R";
		}
		addParamValues.put("Branchtype",branchType);
		addParamValues.put("R", rOnString);
		addParamValues.put("L", lOnString);
		addParamValues.put("C", "0");
		addJSON.put("paramValues", addParamValues);
		
		Resistor rlOn=new Resistor(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(rlOn);
		
		//使用电压源构建IGBT的恒压降
		addJSON=new JSONObject();
		addJSON.put("blockType", "DC Voltage Source");
		addJSON.put("blockName", this.blockName+"Vf");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		
		addParamValues.put("v0", vFString);
		addJSON.put("paramValues", addParamValues);
		
		DCVoltageSource vF=new DCVoltageSource(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(vF);
		
		//构建IGBT中的开关，正向电压大于恒压降，就打通，否则关断
		addJSON=new JSONObject();
		addJSON.put("blockType", "SwitchBase");
		addJSON.put("blockName", this.blockName+"sw");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		
		addJSON.put("paramValues", addParamValues);
		
		sw=new SwitchBase(blockId++,addJSON,model,this);
		this.getSingleCurcuitBlockList().add(sw);
		
		//建立连线，将恒压降与电阻串联起来
		CircuitLine circuitLine=new CircuitLine(rlOn.getCurcuitPortList().get(1),vF.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		//建立连线，将开关与电阻串联起来
		circuitLine=new CircuitLine(sw.getCurcuitPortList().get(1),rlOn.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		//开关-电阻-恒压降串联，用开关的左port和和压降的右port替换igbt的两侧port，与外界相连
		replacePort(this.getCurcuitPortList().get(0),sw.getCurcuitPortList().get(0));
		replacePort(this.getCurcuitPortList().get(1),vF.getCurcuitPortList().get(1));
		
		//用开关的左port和和压降的右port替换igbt的两侧port
		this.getCurcuitPortList().set(0, sw.getCurcuitPortList().get(0));
		this.getCurcuitPortList().set(1, vF.getCurcuitPortList().get(1));

	}

	@Override
	public void setupLogicCode() {
		sw.setSwitchCode(this.getSwtichCode());
	}
	
	@Override
	public void setupLogicCode(int partId) {
		sw.setSwitchCodePart(this.getSwtichCode(partId));
	}

	private String getSwtichCode() {
		String sCode="";
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		String inputName = igbt.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		//计算开关器件的开关逻辑，并记录为oldStatus
		sCode+="int "+this.getOldStatusString()+"=("+v0+"-"+v1+">"+vFString+" && "+inputName+">0);\n";
		
		//根据开关逻辑，控制是否需要合并矩阵
		sCode+="if("+v0+"-"+v1+">"+vFString+" && "+inputName+">0){\n";
		sCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+sw.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+sw.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		sCode+="}\n";
		
		return sCode;
	}
	
	//需要修改
	private String getSwtichCode(int partId) {
		String sCode="";
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		String inputName = igbt.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		//计算开关器件的开关逻辑，并记录为oldStatus
		sCode+="int "+this.getOldStatusString()+"=("+v0+"-"+v1+">"+vFString+" && "+inputName+">0);\n";
		
		//根据开关逻辑，控制是否需要合并矩阵
		sCode+="if("+v0+"-"+v1+">"+vFString+" && "+inputName+">0){\n";
		sCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+sw.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+sw.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		sCode+="}\n";
		
		return sCode;
	}
	
	//需要修改
	public String getCurrentString() {
		return this.getSingleCurcuitBlockList().get(1).getCurrentString();
	}
	
	public String getVoltageString() {
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		String vString="("+v0+"-"+v1+")";
		return vString;
	}
}
