package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.block.math.Add;
import com.ncslab.block.math.Gain;
import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.circuit2.block.baseelement.*;

public class Resistor extends CircuitBlockSingle implements SwitchBlock{
	
	private String rString;
	private int switchId=0;
	
	private double rValue;
	
	public Resistor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		rString=paramValues.getString("R");
		rValue=Double.parseDouble(rString);
		//System.out.println(blockJSON);
	}
	
	public Resistor(JSONObject blockJSON,NCSLabModel model) {
		super(0,blockJSON,model);
		rString=paramValues.getString("R");
		rValue=Double.parseDouble(rString);
		//System.out.println(blockJSON);
	}
	
	public String getRString() {
		if(Double.parseDouble(rString)<=0) {
			return "1.0";
		}
		else {
			return "("+this.rString+"*1.0)";
		}
	}
	
	public double getRValue() {
		return rValue;
	}
	
	public String getSwitchCode() {
		String switchCode="/*Switch Code for resistor "+this.getBlockName()+" */\n";
		if(Double.parseDouble(rString)<=0){
			switchCode+="setSwitchStatus(&switchGAA,"+this.getSwitchId()+","+1+");\n";
			switchCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize);\n";
		}
		else {
			switchCode+="setSwitchStatus(&switchGAA,"+this.getSwitchId()+","+0+");\n";
		}
		return switchCode;
	}
	
	public String getCurrentCode() {
		String code=super.getCurrentCode();
		
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		code+=this.getCurrentString()+"=("+v0+"-"+v1+")/"+this.getRString()+";\n";
		
		this.setIsCurrentCodeGen(true);
		return code;
	}

	@Override
	public void setSwitchId(int switchId) {
		// TODO Auto-generated method stub
		this.switchId=switchId;
	}

	@Override
	public int getSwitchId() {
		// TODO Auto-generated method stub
		return this.switchId;
	}
	
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String getSwitchCode(int partId) {
		String switchCode="/*Switch Code for resistor "+this.getBlockName()+" */\n";
		if(Double.parseDouble(rString)<=0){
			switchCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+this.getSwitchPartId()+","+1+");\n";
			switchCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize);\n";
		}
		else {
			switchCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+this.getSwitchPartId()+","+0+");\n";
		}
		return switchCode;
	}

	@Override
	public boolean getSwitchStatus() {
		// TODO Auto-generated method stub
		return rValue<=0;
	}
	
}
