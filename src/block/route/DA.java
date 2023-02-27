package block.route;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class DA extends block.Block{
	
	block.io.Parameter channel;
	public DA(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//一个输入
		inputPortList.add(new InputPort(this,1));
		
		channel=new Parameter(this,1,"channel",paramValues.getString("Channel"));
		parameterList.add(channel);
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
		
		String initCode="/*Code for initialization of block DA:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=channel.getName()+"="+paramValues.getDouble("Channel")+";\n"; 
		
		initCode+="DEV_ModuleInit();\n"
				+ "printf(\"\\r\\n Program start \\r\\n\");\r\n"
				+ "DAC8532_Out_Voltage(channel_A, 0);\r\n"
				+ "DAC8532_Out_Voltage(channel_B, 0);\r\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block DA:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="if("+channel.getName()+"){\n";
		outputCode+="DAC8532_Out_Voltage(channel_B,"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+");}\n";
		outputCode+="else{\n";
		outputCode+="DAC8532_Out_Voltage(channel_A,"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+");}\n";
		code.addOutputCode(outputCode);
	}
}
