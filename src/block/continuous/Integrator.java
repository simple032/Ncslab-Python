package block.continuous;

import org.json.JSONObject;

import block.Block;
import ncslablink.NCSLabModel;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import block.io.Parameter;
import code.c.CodeStructC;

public class Integrator extends Block {
	private State stateIntegral;
	private Parameter initialCondition;
	public Integrator(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,false));
		
		stateIntegral=new State(this,1,"integral");
		stateList.add(stateIntegral);
		
		initialCondition=new Parameter(this,1,"InitialCondition");
		parameterList.add(initialCondition);
	}
	
	public String generateInitCodeM() {
		String code=super.generateInitCodeM();
		code+=stateIntegral.getName()+"="+paramValues.getDouble("InitialCondition")+";\n"; 
		
		return code;
	}
	
	public String generateUpdateCodeM() {
		String code=super.generateUpdateCodeM();
		
		code+=stateIntegral.getName()+"="
				+stateIntegral.getName()+"+"
				+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
				+"*"
				+getModel().getConfig().getFixedStep()
				+";\n";
		
		return code;
	}
	
	public String generateOutputCodeM() {
		String code=outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()
					+";\n";
		
		return code;
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		initCode+=initialCondition.getName()+"="+paramValues.getDouble("InitialCondition")+";\n";
		
		initCode+=stateIntegral.getName()+"="+initialCondition.getName()+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateUpdateCodeC(CodeStructC code) {
		super.generateUpdateCodeC(code);
		
		String updateCode="/*Code for update of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		updateCode+=stateIntegral.getName()+"="
				+stateIntegral.getName()+"+"
				+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
				+"*"
				+getModel().getConfig().getFixedStep()
				+";\n";
		
		code.addUpdateCode(updateCode); 
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()
					+";\n";
		
		code.addOutputCode(outputCode);
	}
}
