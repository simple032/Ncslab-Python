package block.continuous;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import block.io.State;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class PIDController extends block.Block{
	
	Parameter cparaP;
	Parameter cparaI;
	Parameter lowerSaturationLimit=null;
	Parameter upperSaturationLimit=null;
	State stateIntegral;
	public PIDController(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		
		cparaP=new Parameter(this,parameterList.size()+1,"P");
		parameterList.add(cparaP);
		cparaI=new Parameter(this,parameterList.size()+1,"I");
		parameterList.add(cparaI);
		
		stateIntegral=new State(this,1,"integral");
		stateList.add(stateIntegral);
		
		if(paramValues.getString("LimitOutput").equals("on")) {
			lowerSaturationLimit=new Parameter(this,parameterList.size()+1,"LowerSaturationLimit");
			parameterList.add(lowerSaturationLimit);
			upperSaturationLimit=new Parameter(this,parameterList.size()+1,"UpperSaturationLimit");
			parameterList.add(upperSaturationLimit);
		}
	}
	
	public String generateInitCodeM() {
		String code=super.generateInitCodeM();
		code+="Block"+getBlockId()+"_Integral=0;\n";
		
		return code;
	}
	
	public String generateUpdateCodeM() {
		String code=super.generateUpdateCodeM();
		
		code+="Block"+getBlockId()+"_Integral="
				+"Block"+getBlockId()+"_Integral+"
				+paramValues.getDouble("I")+"*"
				+"Block"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()+"_"
				+"Output"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+"*"
				+getModel().getConfig().getFixedStep()
				+";\n";
		
		return code;
	}
	
	public String generateOutputCodeM() {
		String code="Block"+this.getBlockId()+"_Output1=Block"+getBlockId()+"_Integral"
				+"+"+paramValues.getDouble("P")+"*"
				+"Block"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()+"_"
				+"Output"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+";\n";
		
		return code;
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
	
	public void generateUpdateCodeC(CodeStructC code) {
		super.generateUpdateCodeC(code);
		
		String updateCode="/*Code for update of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		updateCode+=stateIntegral.getName()+"="
				+stateIntegral.getName()
				+"+"+getInputPortVariable(0)
				+"*"+cparaI.getName()
				+"*"+getModel().getConfig().getFixedStep()
				+";\n";
		
		code.addUpdateCode(updateCode); 
	}
}
