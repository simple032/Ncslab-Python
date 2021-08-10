package block.source;

import org.json.JSONObject;

import block.io.OutputPort;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class Constant extends block.Block{
	public Constant(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//Ò»¸öÊä³ö
		outputPortList.add(new OutputPort(this,1,false));
	}
	
	public String generateOutputCodeM() {
		String code="Block"+this.getBlockId()+"_Output1="+paramValues.getDouble("Value")+";\n";
		
		return code;
	}
	
	code.c.Parameter value;
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		value=code.addParameter(this,"value");
		parameterList.add(value);
		
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
