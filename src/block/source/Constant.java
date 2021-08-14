package block.source;

import org.json.JSONObject;

import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class Constant extends block.Block{
	
	block.io.Parameter value;
	public Constant(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//Ò»¸öÊä³ö
		outputPortList.add(new OutputPort(this,1,false));
		
		value=new Parameter(this,1,"value");
		parameterList.add(value);
		
		updateBlock();
	}
	
	public String generateOutputCodeM() {
		String code="Block"+this.getBlockId()+"_Output1="+paramValues.getDouble("Value")+";\n";
		
		return code;
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		code.addParameter(value);
		
		String initCode="/*Code for initialization of block Contant:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=value.getName()+"="+paramValues.getDouble("Value")+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+value.getName()+";\n";
		
		code.addOutputCode(outputCode);
	}
}
