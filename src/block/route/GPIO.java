package block.route;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class GPIO extends block.Block{
	
	block.io.Parameter Bcm;
	public GPIO(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//一个输入
		inputPortList.add(new InputPort(this,1));
		
		Bcm=new Parameter(this,1,"Bcm",paramValues.getString("Bcm"));
		parameterList.add(Bcm);
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
	
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Bcm:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=Bcm.getName()+"="+paramValues.getDouble("Bcm")+";\n"; 
		
		initCode+="wiringPiSetupGpio();\n"
				+ "pinMode ("+Bcm.getName()+", OUTPUT);\r\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Bcm:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+="if("+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"){\n";
		outputCode+="digitalWrite("+Bcm.getName()+", HIGH);}\n";
		outputCode+="else{\n";
		outputCode+="digitalWrite("+Bcm.getName()+", LOW);}\n";
		
		
		code.addOutputCode(outputCode);
	}
}