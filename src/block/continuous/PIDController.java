package block.continuous;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class PIDController extends block.Block{
	
	Parameter cparaP;
	Parameter cparaI;
	Parameter lowerSaturationLimit=null;
	Parameter upperSaturationLimit=null;
	State stateIntegral;
	public PIDController(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		
		cparaP=new Parameter(this,parameterList.size()+1,"P",paramValues.getString("P"));
		parameterList.add(cparaP);
		cparaI=new Parameter(this,parameterList.size()+1,"I",paramValues.getString("I"));
		parameterList.add(cparaI);
		
		stateIntegral=new State(this,1,"integral");
		stateList.add(stateIntegral);
		
		if(paramValues.getString("LimitOutput").equals("on")) {
			lowerSaturationLimit=new Parameter(this,parameterList.size()+1,"LowerSaturationLimit",paramValues.getString("LowerSaturationLimit"));
			parameterList.add(lowerSaturationLimit);
			upperSaturationLimit=new Parameter(this,parameterList.size()+1,"UpperSaturationLimit",paramValues.getString("UpperSaturationLimit"));
			parameterList.add(upperSaturationLimit);
		}
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
		
		initCode+=cparaP.getName()+"="+paramValues.getDouble("P")+";\n";
		initCode+=cparaI.getName()+"="+paramValues.getDouble("I")+";\n";
		
		initCode+=stateIntegral.getName()+"=0;\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		
		String derivativeCode="";
		
		derivativeCode+=stateIntegral.getDerivativeName()+"="
				+cparaI.getName()+"*"
				+this.getInputPortVariable(0)
				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		
		String outputCode=getOutputPortVariable(0)
				+"="+stateIntegral.getName()             //"=Block"+getBlockId()+"_Integral"
				+"+"+cparaP.getName()+"*"
				+this.getInputPortVariable(0)
				+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		initCode+=cparaP.getName()+"="+paramValues.getDouble("P")+";\n";
		initCode+=cparaI.getName()+"="+paramValues.getDouble("I")+";\n";
		
		if(paramValues.getString("LimitOutput").equals("on")) {
			initCode+=lowerSaturationLimit.getName()+"="+paramValues.getDouble("LowerSaturationLimit")+";\n";
			initCode+=upperSaturationLimit.getName()+"="+paramValues.getDouble("UpperSaturationLimit")+";\n";
		}
		
		initCode+=stateIntegral.getName()+"="+0+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=getOutputPortVariable(0)+"="+cparaP.getName()
					+"*"+getInputPortVariable(0)
					+"+"+stateIntegral.getName()
					+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		derivativeCode+=stateIntegral.getDerivativeName()+"="
				+cparaI.getName()+"*"
				+this.getInputPortVariable(0)
				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
}
