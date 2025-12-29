package com.ncslab.circuit2.block.baseelement;

import org.json.JSONObject;

import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.circuit2.block.element.SwitchBlock;
import com.ncslab.circuit2.block.io.CircuitNode;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.block.io.CircuitPortType;

abstract public class CircuitBlockSingle extends CircuitBlock {
	
	private boolean isCurrentCodeGen=false;
	
	public CircuitBlockSingle(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
	}
	
	public String getHisString() {
		return "0";
	}
	
	public String getHisUpdateString() {
		return null;
	}
	
	public String getRString() {
		return null;
	}
	
	public String getCurrentString() {
		//return "(("+this.getCurcuitPortList().get(0).getCircuitNode().getNodeString()+"-"+this.getCurcuitPortList().get(1).getCircuitNode().getNodeString()+")/"+this.getRString()+"+"+this.getHisString()+")";
		
		
		//return this.blockName.replaceAll(" ", "_").replace("\t", "_")+"_current";
		return "EBlock"+this.getBlockId()+"_current";
	}
	
	public void setIsCurrentCodeGen(boolean isCurrentCodeGen) {
		this.isCurrentCodeGen=isCurrentCodeGen;
	}
	
	public boolean getIsCurrentCodeGen() {
		return this.isCurrentCodeGen;
	}
	
	public String getCurrentCode() {
		return "/*Current calculation code for "+this.getBlockName()+"*/\n";
	}
	
	protected boolean hasOtherSwitch(CircuitPort port) {
		boolean result=false;
		CircuitNode node=port.getCircuitNode();
		
		for(CircuitPort nodePort:node.getCircuitPortList()) {
			if(nodePort!=port&&(nodePort.getBlock().getIsCurrentCodeGen()==false)) {
				result=true;
				break;
			}
		}
		
		return result;
	}
	
	protected String getCurrentCodeFromPort(CircuitPort port) {
		String code="(0";
		
		CircuitNode node=port.getCircuitNode();
		for(CircuitPort nodePort:node.getCircuitPortList()) {
			if(nodePort!=port) {
				if(nodePort.getCircuitPortType()==CircuitPortType.Left) {
					code+="+"+nodePort.getBlock().getCurrentString();
				}
				else {
					code+="-"+nodePort.getBlock().getCurrentString();
				}
				
			}
		}
		
		code+=")";
		
		return code;
	}
	
	public String generateHisStringCode(String prefix) {
		String code=this.generateHisStringCode();
		
		return code.replaceAll("EBlock", prefix+"_EBlock");
	}
	
	public String generateHisStringCode() {
		return "";
	}
}
