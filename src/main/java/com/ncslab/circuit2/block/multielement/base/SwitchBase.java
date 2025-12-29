package com.ncslab.circuit2.block.multielement.base;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.circuit2.block.baseelement.CircuitBlockMulti;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.circuit2.block.element.SwitchBlock;
import com.ncslab.circuit2.block.io.CircuitPort;

public class SwitchBase extends CircuitBlockSingle implements SwitchBlock{
	private CircuitBlockMulti multiBlock;
	private String switchCode="";
	private String switchCodePart="";
	private int switchId=0;
	public SwitchBase(int id,JSONObject blockJSON,NCSLabModel model,CircuitBlockMulti multiBlock) {
		super(id,blockJSON,model);
		this.blockModeType=BlockModeType.Switch;
		this.multiBlock=multiBlock;
	}
	
	
	public void setSwitchId(int switchId) {
		this.switchId=switchId;
	}
	public int getSwitchId() {
		return this.switchId;
	}
	
	public String getSwitchCode() {
		String switchCode="/*Switch Code for SwitchBase "+this.getBlockName()+" */\n";
		//switchCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+");\n";
		return switchCode+this.switchCode;
	}
	
	public CircuitBlockMulti getMultiBlock() {
		return this.multiBlock;
	}
	
	public void setSwitchCode(String switchCode) {
		this.switchCode=switchCode;
	}
	
	public void setSwitchCodePart(String switchCodePart) {
		this.switchCodePart=switchCodePart;
	}
	
	public String getCurrentCode() {
		String code=super.getCurrentCode();
		
		CircuitPort p0=this.getCurcuitPortList().get(0);
		CircuitPort p1=this.getCurcuitPortList().get(1);
		if(hasOtherSwitch(p0)==false) {
			code+=this.getCurrentString()+"="+this.getCurrentCodeFromPort(p0)+";\n";
		}
		else 
		if(hasOtherSwitch(p0)==false) {
			code+=this.getCurrentString()+"="+this.getCurrentCodeFromPort(p1)+";\n";
		}
		else {
			code+=this.getCurrentString()+"=0;\n";
		}
		
		this.setIsCurrentCodeGen(true);
		
		//for(p0.get)
		
		return code;
	}


	@Override
	public String getSwitchCode(int partId) {
		// TODO Auto-generated method stub
		String switchCode="/*Switch Code for SwitchBase "+this.getBlockName()+" */\n";
		//switchCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+");\n";
		return switchCode+this.switchCodePart;
	}
}
