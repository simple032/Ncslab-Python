package com.ncslab.block.function;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class SFunction extends Block {
	
	private String name = "S-Function";
	
	private String fcnName = "";
	
	State speedState;
	
	public SFunction(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//TODO:Dynamic port allocation
		//һ�����룬�������
		inputPortList.add(new InputPort(this, 1));
		outputPortList.add(new OutputPort(this,1,false));
		
		//System.out.print(blockJSON);
		parseParamValues();
		
		//inputPortList = getInputPortFromSFcn("");
	}
	
	private void parseParamValues() {
		this.fcnName = paramValues.getString("FunctionName");	
	}
	
	public Vector<InputPort> getInputPortFromSFcn(String filename) {
		Vector<InputPort> lip = new Vector<InputPort>();
		int num = 1;
		for(int i=0; i<num; i++)
		{
			lip.add(new InputPort(this, i+1));
		}
		return lip;
	}
	
	public void setOutputPortNum(int num) {
		for(int i=0; i<num; i++) {
			outputPortList.add(new OutputPort(this, i+1));
		}
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
//		initCode+=pumpState.getName()+"=0;\n";
//		initCode+=levelState.getName()+"=0;\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		
		String derivativeCode="";
		
//		derivativeCode+=pumpState.getDerivativeName()+"=("
//				+this.getInputPortVariable(0)
//				+"*"+pumpK+"-"+pumpState.getName()+")"
//				+"*"+(1/pumpT)
//				+";\n";
//		
//		derivativeCode+=levelState.getDerivativeName()+"=("
//				+pumpState.getName()+"*"+waterLevelK+"-"+levelState.getName()+")"
//				+"*"+(1/waterLevelT)
//				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		
//		outputCode+=getOutputPortVariable(0)+"="
//				+pumpState.getName()
//				+";\n";
//		
//		outputCode+=getOutputPortVariable(1)+"="
//				+levelState.getName()
//				+ ";\n";
		
		code.addOutputCode(outputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		initCode+=pumpState.getName()+"="+0+";\n";
//		initCode+=levelState.getName()+"="+0+";\n"; 
		initCode += fcnName+"_"+getBlockId()+"(&sfcnStruc"+getBlockId()+");\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateIncludeCodeC(CodeStructC code) {
//		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
	}
	
	public boolean isSFcnBlock() {
		return true;
	}
	
	public String getSFcnName() {
		return this.fcnName;
	}	

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+pumpState.getName()+";\n";
//		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+levelState.getName()+";\n";
		outputCode +=  "sfcnOutputs(sfcnStruc" + getBlockId() + ",0);\n";
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		derivativeCode +=  "sfcnDerivatives(sfcnStruc" + getBlockId() + ");\n";
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		statementCode += "void "+ this.fcnName + "_" + getBlockId() +"(SimStruct* rts);\n";
		code.addStatementCode(statementCode);
	}
}
