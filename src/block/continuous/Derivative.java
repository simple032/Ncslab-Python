package block.continuous;

import org.json.JSONObject;

import block.Block;
import ncslablink.NCSLabModel;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;

public class Derivative extends Block {
	private State stateIntegral;
	
	//G=s/(Ts+1) T->0
	public Derivative(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,false));
		
		stateIntegral=new State(this,1,"integral");
		stateList.add(stateIntegral);	
		
	
	}
	
	public void generateInitCodeM(CodeStructM code) {
		String initCode="";
		
		super.generateInitCodeM(code);
		
		
		initCode+=stateIntegral.getName()+"=0;\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		String derivativeCode="";
		
		super.generateDerivativeCodeM(code);
		
		derivativeCode+=stateIntegral.getDerivativeName()+"="
				+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		
		String outputCode="";
		
		super.generateOutputCodeM(code);
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()
					+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		initCode+=stateIntegral.getName()+"=0;\n"; 
		
		code.addInitCode(initCode);
	}
	
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()
				+"="
				+ "1/model.stepSize"
				+"*(1-"
				+stateIntegral.getName()
				+ ")"
					+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateDerivativeCodeC(CodeStructC code) {
		
		String derivativeCode="/*Code for Derivative of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		derivativeCode+=stateIntegral.getDerivativeName()+"="
				+"("
				+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
				+"-"
				+stateIntegral.getName()
				+")/model.stepSize"
				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
}
