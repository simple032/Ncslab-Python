package com.ncslab.circuit2.block.baseelement;

import org.json.JSONObject;
import lombok.Getter;
import lombok.Setter;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.block.io.CircuitNode;
import com.ncslab.ncslablink.NCSLabModel;

public abstract class VoltageSource extends CircuitBlockSingle {
	private int vsId;
	
	private int partVsId;
	
	private int currentId=-1;
	
	private int partCurrentId=-1;
	
	@Getter
	@Setter
	private double currentValue;
	
	public VoltageSource(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		this.blockModeType=BlockModeType.VoltageSource;
	}
	
	public void setVsId(int vsId) {
		this.vsId=vsId;
	}
	
	public int getVsId() {
		return vsId;
	}
	
	public void setPartVsId(int partVsId) {
		this.partVsId=partVsId;
	}
	
	public int getPartVsId() {
		return partVsId;
	}
	
	public String getVString() {
		return null;
	}
	
	abstract public double getVValue(double t);
	
	public void setCurrentId(int currentId) {
		this.currentId=currentId;
	}
	
	public int getCurrentId() {
		return this.currentId;
	}
	
	public void setPartCurrentId(int partCurrentId) {
		this.partCurrentId=partCurrentId;
	}
	
	public int getPartCurrentId() {
		return this.partCurrentId;
	}
	
	public String getCurrentCode() {
		return "";
	}
	
	public String getCurrentString() {
		//String code="0";
		/*
		CircuitPort thisPort=this.getCurcuitPortList().get(0);
		CircuitNode node=thisPort.getCircuitNode();
		for(CircuitPort port:node.getCircuitPortList()) {
			if(port)
		}*/
		return "EBlock"+this.getBlockId()+"_"+this.blockName.replaceAll(" ", "_")+"_current";
	}

}
