package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.block.continuous.Integrator;
import com.ncslab.block.math.Add;
import com.ncslab.block.math.Gain;
import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.circuit2.block.baseelement.*;


public class Inductor extends CircuitBlockSingle implements SwitchBlock{

	private String lString;
	private double lValue;
	
	public Inductor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		//System.out.println(blockJSON);
		lString=paramValues.getString("l");
		lValue=Double.parseDouble(lString);
	}
	
	public Inductor(JSONObject blockJSON,NCSLabModel model) {
		super(0,blockJSON,model);
		//System.out.println(blockJSON);
		lString=paramValues.getString("l");
		lValue=Double.parseDouble(lString);
	}
	
	public String getRString() {
		return "(2.0*("+this.lString+")/"+this.getSimpleTime()+")";
	}
	
	public double getRValue() {
		return 2.0*lValue/this.getSampleTimeValue();
	}
	
	public String getHisString() {
		String hisString;
		hisString="EBlock"+this.getBlockId()+"_His";
		return hisString;
	}
	
	public String getHisUpdateString() {
		String hisUpdateString="";
		
		String hisString=getHisString();
		
		String lv=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String rv=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		hisUpdateString+=hisString+"=("+hisString+"+"+this.getSimpleTime()+"/("+this.lString+")*("+lv+"-"+rv+"))";
		
		return hisUpdateString;
	}
	
	public void updateHis() {
		double lv=this.getCurcuitPortList().get(0).getCircuitNode().getVoltage();
		double rv=this.getCurcuitPortList().get(1).getCircuitNode().getVoltage();
		
		hisValue+=this.getSampleTimeValue()/lValue*(lv-rv);
	}
	
	public String getCurrentCode() {
		String code=super.getCurrentCode();
		
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		code+=this.getCurrentString()+"=("+v0+"-"+v1+")/"+this.getRString()+"+"+this.getHisString()+";\n";
		return code;
	}
	
	public String generateHisStringCode() {
		return "REAL EBlock"+this.getBlockId()+"_His=0;\n";
	}

	@Override
	public String getSwitchCode() {
		// TODO Auto-generated method stub
		String switchCode="/*Switch Code for inductor "+this.getBlockName()+" */\n";
		if(Double.parseDouble(lString)<=0){
			switchCode+="setSwitchStatus(&switchGAA,"+this.getSwitchId()+","+1+");\n";
			switchCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize);\n";
		}
		else {
			switchCode+="setSwitchStatus(&switchGAA,"+this.getSwitchId()+","+0+");\n";
		}
		return switchCode;
	}
	
	
	
	private int switchId=0;
	
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
		// TODO Auto-generated method stub
		String switchCode="/*Switch Code for inductor "+this.getBlockName()+" */\n";
		if(Double.parseDouble(lString)<=0){
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
		return lValue<=0;
	}
	
}
