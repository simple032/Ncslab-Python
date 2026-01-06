package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;
import com.ncslab.circuit2.block.baseelement.*;

public class VariableResistor extends CircuitBlockSingle implements SwitchBlock,VariableBlock,InterCircuitBlock{
	com.ncslab.block.elec.VariableResistor variableResistorBlock;
	
	private int variableBlockId=0;
	
	private String variableString;
	
	private int switchId=0;
	
	public VariableResistor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		createBlock(null);
		
		variableString="EBlock"+this.blockId+"_R";
	}
	
	public String getVariableString() {
		variableString="EBlock"+this.blockId+"_R";
		return variableString;
	}
	
	public VariableResistor(JSONObject blockJSON,NCSLabModel model,NCSLabSystem targetSystem,java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		super(0,blockJSON,model);
		this.blockModeType=BlockModeType.Nromal;
		//System.out.println(blockJSON);
		//System.out.println(getVoltageString());
		createBlock(blockSeqCounter);
		variableString="EBlock"+this.blockId+"_R";
	}
	
	private void createBlock(java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Variable Reisitor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		variableResistorBlock=new com.ncslab.block.elec.VariableResistor(addJSON,model,this);
		variableResistorBlock.setBlockUUID(this.getBlockUUID());
		variableResistorBlock.setBlockId(blockSeqCounter.incrementAndGet());
		model.addElectBlock(variableResistorBlock);
	}
	
	public String getRString() {
		
		String inputString="("+variableResistorBlock.getInputPortVariable(0)+")";
		
		return "("+inputString+">0?"+inputString+":1)";
	}
	
	public String getSwitchCode() {
		String switchCode="/*Switch Code for Switch "+this.getBlockName()+" */\n";
		

		switchCode+="if ("+variableResistorBlock.getInputPortVariable(0)+"<=0){\n";
		switchCode+="setSwitchStatus(&switchGAA,"+this.getSwitchId()+","+1+");\n";
		//switchCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		switchCode+="}\n";
		switchCode+="else{\n";
		switchCode+="setSwitchStatus(&switchGAA,"+this.getSwitchId()+","+0+");\n";
		switchCode+="}\n";
		
		return switchCode;
	}
	
	public String getCurrentCode() {
		String code=super.getCurrentCode();
		
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		code+=this.getCurrentString()+"=("+v0+"-"+v1+")/"+this.getRString()+";\n";
		return code;
	}

	@Override
	public int getVariableBlockId() {
		// TODO Auto-generated method stub
		return this.variableBlockId;
	}

	@Override
	public void setVariableBlockId(int variableBlockId) {
		// TODO Auto-generated method stub
		this.variableBlockId=variableBlockId;
	}
	
	public String getVariableDefineCode() {
		String code="REAL "+getVariableString()+"=0;\n";
		return code;
	}

	@Override
	public String getVariableChangeCode() {
		// TODO Auto-generated method stub
		String code="/*Variable Change Code for "+this.getBlockName()+"*/\n";
		
		code+="if("+variableString+"!="+variableResistorBlock.getInputPortVariable(0)+"){\n";
		code+=this.isVariableChanged+"=1;\n";
		code+=variableString+"="+variableResistorBlock.getInputPortVariable(0)+";\n";
		code+="};\n";
		
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

	@Override
	public String getSwitchCode(int partId) {
		// TODO Auto-generated method stub
		String switchCode="/*Switch Code for Switch "+this.getBlockName()+" */\n";
		

		switchCode+="if ("+variableResistorBlock.getInputPortVariable(0)+"<=0){\n";
		switchCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+this.getSwitchPartId()+","+1+");\n";
		//switchCode+="//CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		switchCode+="}\n";
		switchCode+="else{\n";
		switchCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+this.getSwitchPartId()+","+0+");\n";
		switchCode+="}\n";
		
		return switchCode;
	}
}
