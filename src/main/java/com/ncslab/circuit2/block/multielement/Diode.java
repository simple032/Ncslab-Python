package com.ncslab.circuit2.block.multielement;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CircuitBlockMulti;
import com.ncslab.circuit2.block.element.Resistor;
import com.ncslab.circuit2.block.element.DCVoltageSource;
import com.ncslab.circuit2.block.multielement.base.SwitchBase;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.line.CircuitLine;
import com.ncslab.ncslablink.NCSLabModel;


public class Diode extends CircuitBlockMulti implements Recalc{
	private String rOnString;
	private String vFString;
	private String gOffString;
	private SwitchBase sw;
	
	private double vFValue;
	
	private boolean diodeStatus=false;
	
	public Diode(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		System.out.println(blockJSON);
		
		rOnString=paramValues.getString("Ron");
		vFString=paramValues.getString("Vf");
		gOffString=paramValues.getString("Goff");
		
		vFValue=Double.parseDouble(vFString);
		
		//vFString="0";
	}
	
	public Diode(JSONObject blockJSON,NCSLabModel model) {
		super(0,blockJSON,model);
		System.out.println(blockJSON);
		
		rOnString=paramValues.getString("Ron");
		vFString=paramValues.getString("Vf");
		gOffString=paramValues.getString("Goff");
		
		vFValue=Double.parseDouble(vFString);
		
		//vFString="0";
	}
	
	public void setupSubCircuitBlocks(int blockId) {
		//构建二极管中的电阻
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Reisitor");
		addJSON.put("blockName", this.blockName+"Ron");
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		
		addParamValues.put("R", rOnString);
		addJSON.put("paramValues", addParamValues);
		
		Resistor rOn=new Resistor(blockId++,addJSON,model);
		this.getSingleCurcuitBlockList().add(rOn);
		
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
		addJSON.put("blockName", this.blockName+"sw");
		addJSON.put("blockPath", this.blockPath);
		addParamValues=new JSONObject();
		
		addJSON.put("paramValues", addParamValues);
		
		sw=new SwitchBase(blockId++,addJSON,model,this);
		this.getSingleCurcuitBlockList().add(sw);

		// 构建冰点的电阻电阻
		addJSON = new JSONObject();
		addJSON.put("blockType", "Reisitor");
		addJSON.put("blockName", this.blockName + "RPara");
		addJSON.put("blockPath", this.blockPath);
		addParamValues = new JSONObject();

		addParamValues.put("R", ""+(1/Double.parseDouble(gOffString)));
		addJSON.put("paramValues", addParamValues);

		Resistor rPara = new Resistor(blockId++, addJSON, model);
		this.getSingleCurcuitBlockList().add(rPara);

		//建立连线，将恒压降与电阻串联起来
		CircuitLine circuitLine=new CircuitLine(rOn.getCurcuitPortList().get(1),vF.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		//建立连线，将开关与电阻串联起来
		circuitLine=new CircuitLine(sw.getCurcuitPortList().get(1),rOn.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		circuitLine=new CircuitLine(sw.getCurcuitPortList().get(0),rPara.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		circuitLine=new CircuitLine(vF.getCurcuitPortList().get(1),rPara.getCurcuitPortList().get(1));
		this.getCircuitLineList().add(circuitLine);
		
		//开关-电阻-恒压降串联，用开关的左port和和压降的右port替换diode的两侧port，与外界相连
		replacePort(this.getCurcuitPortList().get(0),sw.getCurcuitPortList().get(0));
		replacePort(this.getCurcuitPortList().get(1),vF.getCurcuitPortList().get(1));
		
		//用开关的左port和和压降的右port替换diode的两侧port
		this.getCurcuitPortList().set(0, sw.getCurcuitPortList().get(0));
		this.getCurcuitPortList().set(1, vF.getCurcuitPortList().get(1));
		
		
		
		/*
		CircuitLine circuitLine=new CircuitLine(rOn.getCurcuitPortList().get(1),vF.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		
		
		replacePort(this.getCurcuitPortList().get(0),rOn.getCurcuitPortList().get(0));
		replacePort(this.getCurcuitPortList().get(1),vF.getCurcuitPortList().get(1));
		
		circuitLine=new CircuitLine(rOn.getCurcuitPortList().get(0),sw.getCurcuitPortList().get(0));
		this.getCircuitLineList().add(circuitLine);
		circuitLine=new CircuitLine(vF.getCurcuitPortList().get(1),sw.getCurcuitPortList().get(1));
		this.getCircuitLineList().add(circuitLine);*/
		
		
	}
	
	//设置Diode内部的控制逻辑
	public void setupLogicCode() {
		//设置开关器件的控制逻辑
		sw.setSwitchCode(this.getSwtichCode());
	}
	
	//设置Diode内部的控制逻辑
	public void setupLogicCode(int partId) {
		// 设置开关器件的控制逻辑
		sw.setSwitchCodePart(this.getSwtichCode(partId));
	}
	
	public String getOldStatusString() {
		return "EBlock"+this.getBlockId()+"_"+this.getBlockName()+"_oldStatus";
	}
	
	public String getStatusString() {
		return "EBlock"+this.getBlockId()+"_"+this.getBlockName()+"_status";
	}
	
	private String getSwtichCode() {
		String sCode="/*Switch Code for Diode "+this.getBlockName()+" */\n";
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		//计算开关器件的开关逻辑，并记录为oldStatus
		//sCode+="int "+this.getOldStatusString()+"=("+v0+"-"+v1+">"+vFString+")"+";\n";
		
		//sCode+="static int "+this.getStatusString()+"=0;\n";
			
		
		
		//根据开关逻辑，控制是否需要合并矩阵
		sCode+="if("+this.getStatusString()+"){\n";
		//sCode+="int sw="+sw.getSwtichId()+";\n";
		sCode+="setSwitchStatus(&switchGAA,"+sw.getSwitchId()+","+1+");\n";
		sCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+sw.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+sw.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		sCode+="}\n";
		sCode+="else{\n";
		sCode+="setSwitchStatus(&switchGAA,"+sw.getSwitchId()+","+0+");\n";
		sCode+="}\n";
		
		return sCode;
	}
	
	private String getSwtichCode(int partId) {
		String sCode="/*Switch Code for Diode "+this.getBlockName()+" */\n";
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		//计算开关器件的开关逻辑，并记录为oldStatus
		//sCode+="int "+this.getOldStatusString()+"=("+v0+"-"+v1+">"+vFString+")"+";\n";
		
		//sCode+="static int "+this.getStatusString()+"=0;\n";
		
		//根据开关逻辑，控制是否需要合并矩阵
		sCode+="if("+this.getStatusString()+"){\n";
		//sCode+="int sw="+sw.getSwtichId()+";\n";
		sCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+sw.getSwitchPartId()+","+1+");\n";
		sCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+sw.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+sw.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		sCode+="}\n";
		sCode+="else{\n";
		sCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+sw.getSwitchPartId()+","+0+");\n";
		sCode+="}\n";
		
		return sCode;
	}
	
	//返回是否需要重计算的控制逻辑
	public String getIsRecalcCode() {
		
		String code="";
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		code+="int "+this.getOldStatusString()+"="+this.getStatusString()+";\n";
		code+=this.getStatusString()+"=("+v0+"-"+v1+">"+vFString+")"+";\n";
		
		
		//code+="if("+this.getOldStatusString()+"^"+this.getStatusString()+"){\n";

		//如果计算之后二极管从关闭到打开，就需要重计算
		//似乎二极管关断的时候不需要重新计算,因为二极管被来就有反向恢复的特性,这个还要检测
		code+="if("+this.getOldStatusString()+"==0&&"+this.getStatusString()+"==1&&("+v0+"-"+v1+")>(("+vFString+")*100)){\n";
		//code+="if("+this.getOldStatusString()+"!="+this.getStatusString()+"){\n";
		code+=isRecalcString+"=1;\n";
		code+="}\n";
		
		return code;
		
	}
	
	public String getCircuitStatusDefineCode() {
		String cCode= "/*Circuit status defined code for "+this.getBlockName()+"*/\n";
		
		cCode+="static int "+this.getStatusString()+"=0;\n";
		
		return cCode;
	}
	
	@Override
	public void logicCode(double t) {	
		sw.setSwitchStatus(diodeStatus);
	}
	
	@Override
	public void updateLogic(double t) {
		double v0=this.getCurcuitPortList().get(0).getCircuitNode().getVoltage();
		double v1=this.getCurcuitPortList().get(1).getCircuitNode().getVoltage();
		
		boolean oldDiodeStatus=diodeStatus;		
		diodeStatus=(v0-v1>vFValue);
		//System.out.println("diodeStatus:"+diodeStatus);
		if(oldDiodeStatus==false&&diodeStatus==true) {
			this.model.getCircuitModel().setRecalc(true);
		}
	}
}
