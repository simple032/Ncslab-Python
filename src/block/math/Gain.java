package block.math;

import org.json.JSONObject;

import block.Block;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import block.io.InputPort;
import ncslablink.NCSLabModel;

public class Gain extends Block{
	block.io.Parameter gain;
	public Gain(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//一个输出
		outputPortList.add(new OutputPort(this,1,true));
		
		//一个输入
		inputPortList.add(new InputPort(this,1));
		
		gain=new Parameter(this,1,"value");
		parameterList.add(gain);
	}
	
	public String generateOutputCodeM() {
		//String code="Block"+this.getBlockId()+"_Output1="+paramValues.getDouble("Gain")+"*Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()+"_Output"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()+";\n";
		String code=outputPortList.get(0).getOutputSignalC().getName()+"="+paramValues.getDouble("Gain")+"*"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
		
		return code;
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Gain:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=gain.getName()+"="+paramValues.getDouble("Gain")+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+gain.getName()+"*"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
		
		code.addOutputCode(outputCode);
	}
}
