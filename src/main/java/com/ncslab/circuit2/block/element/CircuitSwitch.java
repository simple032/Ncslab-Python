package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.block.io.CircuitPortType;
import com.ncslab.circuit2.block.io.CircuitNode;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;
import com.ncslab.circuit2.block.baseelement.*;

public class CircuitSwitch extends CircuitBlockSingle implements SwitchBlock,InterCircuitBlock{
	private com.ncslab.block.elec.CircuitSwitch circuitSwitchBlock;
	private int switchId=0;
	public CircuitSwitch(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		this.blockModeType=BlockModeType.Switch;
		createBlock(null);
	}
	
	public CircuitSwitch(JSONObject blockJSON,NCSLabModel model,NCSLabSystem targetSystem,java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		super(0,blockJSON,model);
		this.blockModeType=BlockModeType.Switch;
		//System.out.println(blockJSON);
		//System.out.println(getVoltageString());
		createBlock(blockSeqCounter);
	}
	
	public void setSwitchId(int switchId) {
		this.switchId=switchId;
	}
	public int getSwitchId() {
		return this.switchId;
	}
	
	private void createBlock(java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Circuit Switch");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		circuitSwitchBlock=new com.ncslab.block.elec.CircuitSwitch(addJSON, model, this);
		circuitSwitchBlock.setBlockUUID(this.getBlockUUID());
		circuitSwitchBlock.setBlockId(blockSeqCounter.incrementAndGet());
		model.addElectBlock(circuitSwitchBlock);
	}
	
	public String getSwitchCode() {
		String switchCode="/*Switch Code for Switch "+this.getBlockName()+" */\n";
		
		switchCode+="if ("+circuitSwitchBlock.getInputPortVariable(0)+">0){\n";
		switchCode+="setSwitchStatus(&switchGAA,"+getSwitchId()+","+1+");\n";
		switchCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		switchCode+="}\n";
		switchCode+="else{\n";
		switchCode+="setSwitchStatus(&switchGAA,"+getSwitchId()+","+0+");\n";
		switchCode+="}\n";
		
		return switchCode;
	}
	
	public String getSwitchCode(int partId) {
		String switchCode="/*Switch Code for Switch "+this.getBlockName()+" */\n";
		
		switchCode+="if ("+circuitSwitchBlock.getInputPortVariable(0)+">0){\n";
		switchCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+getSwitchPartId()+","+1+");\n";
		switchCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		switchCode+="}\n";
		switchCode+="else{\n";
		switchCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+getSwitchPartId()+","+0+");\n";
		switchCode+="}\n";
		
		return switchCode;
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
	
}
