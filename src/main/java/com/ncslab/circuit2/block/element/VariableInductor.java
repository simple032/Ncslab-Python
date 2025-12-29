package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.ncslablink.NCSLabModel;

public class VariableInductor extends CircuitBlockSingle implements SwitchBlock,VariableBlock{
	com.ncslab.block.elec.VariableInductor variableInductorBlock;
	
	private String variableString;
	
	public VariableInductor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		createBlock();
		
		variableString="EBlock"+this.blockId+"_L";
	}
	
	private void createBlock() {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Variable Inductor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		variableInductorBlock=new com.ncslab.block.elec.VariableInductor(addJSON,model,this);
		model.addElectBlock(variableInductorBlock);
	}
	
	public String getHisString() {
		String hisString;
		hisString="EBlock"+this.getBlockId()+"_His";
		return hisString;
	}
	
	public String getRString() {
		
		//String inputString="("+variableResistorBlock.getInputPortVariable(0)+")";
		
		//return "("+inputString+">0?"+inputString+":1)";
		
		return "(2.0*("+variableInductorBlock.getInputPortVariable(0)+")/"+this.getSimpleTime()+")";
	}
	
	public String getHisUpdateString() {
		String hisUpdateString="";
		
		String hisString=getHisString();
		
		String lv=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String rv=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		hisUpdateString+=hisString+"=("+hisString+"+"+this.getSimpleTime()+"/("+variableInductorBlock.getInputPortVariable(0)+")*("+lv+"-"+rv+"))";
		
		return hisUpdateString;
	}
	
	public String getSwitchCode() {
		String switchCode="/*Switch Code for Switch "+this.getBlockName()+" */\n";
		

		switchCode+="if ("+variableInductorBlock.getInputPortVariable(0)+"<=0){\n";
		switchCode+="setSwitchStatus(&switchGAA,"+this.getSwitchId()+","+1+");\n";
		//switchCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
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
		
		code+=this.getCurrentString()+"=("+v0+"-"+v1+")/"+this.getRString()+"+"+this.getHisString()+";\n";
		return code;
	}
	
	public String generateHisStringCode() {
		return "REAL EBlock"+this.getBlockId()+"_His=0;\n";
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
	
	private int variableBlockId=0;
	
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

	@Override
	public String getVariableDefineCode() {
		// TODO Auto-generated method stub
		
		String code="REAL "+variableString+"=0;\n";
		return code;
	}

	@Override
	public String getVariableChangeCode() {
		// TODO Auto-generated method stub
		String code="/*Variable Change Code for "+this.getBlockName()+"*/\n";
		
		code+="if("+variableString+"!="+variableInductorBlock.getInputPortVariable(0)+"){\n";
		code+=this.isVariableChanged+"=1;\n";
		code+=variableString+"="+variableInductorBlock.getInputPortVariable(0)+";\n";
		code+="};\n";
		
		return code;
	}

	@Override
	public String getSwitchCode(int partId) {
		String switchCode="/*Switch Code for Switch "+this.getBlockName()+" */\n";
		

		switchCode+="if ("+variableInductorBlock.getInputPortVariable(0)+"<=0){\n";
		switchCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+this.getSwitchPartId()+","+1+");\n";
		//switchCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
		switchCode+="}\n";
		switchCode+="else{\n";
		switchCode+="setSwitchStatus(partitioner.partitions["+partId+"].pSwitchGaa,"+this.getSwitchPartId()+","+0+");\n";
		switchCode+="}\n";
		
		return switchCode;
	}
}
