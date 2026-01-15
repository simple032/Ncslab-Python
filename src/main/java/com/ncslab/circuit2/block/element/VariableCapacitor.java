package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;

public class VariableCapacitor extends CircuitBlockSingle implements VariableBlock,InterCircuitBlock{
	com.ncslab.block.elec.VariableCapacitor variableCapacitorBlock;
	
	private String variableString;
	private double oldCValue=0;
	
	public VariableCapacitor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		createBlock(null);
		
		variableString="EBlock"+this.blockId+"_C";
	}
	
	public String getVariableString() {
		variableString="EBlock"+this.blockId+"_C";
		return variableString;
	}
	
	public VariableCapacitor(JSONObject blockJSON,NCSLabModel model,NCSLabSystem targetSystem,java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		super(0,blockJSON,model);
		this.blockModeType=BlockModeType.Nromal;
		//System.out.println(blockJSON);
		//System.out.println(getVoltageString());
		createBlock(blockSeqCounter);
		variableString="EBlock"+this.blockId+"_C";
	}
	
	private void createBlock(java.util.concurrent.atomic.AtomicInteger blockSeqCounter) {
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Variable Capacitor");
		addJSON.put("blockName", this.blockName);
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		addJSON.put("paramValues", addParamValues);
		//System.out.println(addJSON);
		variableCapacitorBlock=new com.ncslab.block.elec.VariableCapacitor(addJSON,model,this);
		variableCapacitorBlock.setBlockUUID(this.getBlockUUID());
		variableCapacitorBlock.setBlockId(blockSeqCounter.incrementAndGet());
		model.addElectBlock(variableCapacitorBlock);
	}
	
	public String getHisString() {
		String hisString;
		hisString="EBlock"+this.getBlockId()+"_His";
		return hisString;
	}
	
	public String getRString() {
		
		//String inputString="("+variableResistorBlock.getInputPortVariable(0)+")";
		
		//return "("+inputString+">0?"+inputString+":1)";
		
		//return "(2.0*("+variableInductorBlock.getInputPortVariable(0)+")/"+this.getSimpleTime()+")";
		
		String inputString="("+variableCapacitorBlock.getInputPortVariable(0)+")";
		
		return "("+this.getSimpleTime()+"/2.0/("+inputString+"))";
	}
	
	public double getRValue() {
		com.ncslab.block.data.Data data=variableCapacitorBlock.getInputPortList().get(0).getData();
		double value=data.getInitValue();
		//return value;
		return this.getSampleTimeValue()/2.0/value;
	}
	
	public String getHisUpdateString() {
		String hisUpdateString="";
		
		String hisString=getHisString();
		
		String lv=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String rv=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		//hisUpdateString+=hisString+"=("+hisString+"+"+this.getSimpleTime()+"/("+variableInductorBlock.getInputPortVariable(0)+")*("+lv+"-"+rv+"))";
		
		hisUpdateString+=hisString+"=-"+hisString+"-(4.0*"+variableCapacitorBlock.getInputPortVariable(0)+")/("+this.getSimpleTime()+")*("+lv+"-"+rv+")";
		
		return hisUpdateString;
	}
	
	
	//public String getSwitchCode() {
	//	String switchCode="/*Switch Code for Switch "+this.getBlockName()+" */\n";
		

	//	switchCode+="if ("+variableInductorBlock.getInputPortVariable(0)+"<=0){\n";
	//	switchCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+this.getCurcuitPortList().get(0).getCircuitNode().getNodeId()+","+this.getCurcuitPortList().get(1).getCircuitNode().getNodeId()+",oldSize,refSize);\n";
	//	switchCode+="}\n";
		
	//	return switchCode;
	//}
	
	public String getCurrentCode() {
		String code=super.getCurrentCode();
		
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		//code+=this.getCurrentString()+"=("+v0+"-"+v1+")/"+this.getRString()+"+"+this.getHisString()+";\n";
		code+=this.getCurrentString()+"=("+v0+"-"+v1+")/"+this.getRString()+"+"+this.getHisString()+";\n";
		return code;
	}
	
	public String generateHisStringCode() {
		return "REAL EBlock"+this.getBlockId()+"_His=0;\n";
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
		
		String code="REAL "+getVariableString()+"=0;\n";
		return code;
	}

	@Override
	public String getVariableChangeCode() {
		// TODO Auto-generated method stub
		String code="/*Variable Change Code for "+this.getBlockName()+"*/\n";
		
		code+="if("+variableString+"!="+variableCapacitorBlock.getInputPortVariable(0)+"){\n";
		code+=VariableBlock.isVariableChanged+"=1;\n";
		code+=variableString+"="+variableCapacitorBlock.getInputPortVariable(0)+";\n";
		code+="};\n";
		
		return code;
	}

	@Override
	public boolean isVariableChanged() {
		// TODO Auto-generated method stub
		com.ncslab.block.data.Data data=variableCapacitorBlock.getInputPortList().get(0).getData();
		double newCValue=data.getInitValue();
		if(oldCValue!=newCValue) {
			oldCValue=newCValue;
			return true;
		}
		else {
			return false;
		}
	}
	
	public void updateHis() {
		com.ncslab.block.data.Data data=variableCapacitorBlock.getInputPortList().get(0).getData();
		double value=data.getInitValue();
		double lv=this.getCurcuitPortList().get(0).getCircuitNode().getVoltage();
		double rv=this.getCurcuitPortList().get(1).getCircuitNode().getVoltage();
		
		hisValue=-hisValue-4.0*value/this.getSampleTimeValue()*(lv-rv);
	}
}
