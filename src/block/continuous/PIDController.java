package block.continuous;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class PIDController extends block.Block{
	public PIDController(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
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
	
	code.c.Parameter cparaP;
	code.c.Parameter cparaI;
	code.c.State stateIntegral;
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		cparaP=code.addParameter(this,"P");
		cparaI=code.addParameter(this,"I");
		
		stateIntegral=code.addState(this, "integral");
		
		String initCode="/*Code for initialization of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		initCode+=cparaP.getName()+"="+paramValues.getDouble("P")+";\n";
		initCode+=cparaI.getName()+"="+paramValues.getDouble("I")+";\n";
		
		initCode+=stateIntegral.getName()+"="+0+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()
					+"*"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
					+"+"+stateIntegral.getName()
					+";\n";
		
		code.addOutputCode(outputCode);
	}
}
