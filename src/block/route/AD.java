package block.route;

import org.json.JSONObject;

import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class AD extends block.Block{
	
	block.io.Parameter channel;
	public AD(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//一个输出
		outputPortList.add(new OutputPort(this,"out",1,false));
		
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
		
		String initCode="/*Code for initialization of block AD:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=channel.getName()+"="+paramValues.getDouble("Channel")+";\n"; 
		
		initCode+="DEV_ModuleInit();\n"
				+"if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block AD:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=ADS1256_GetChannalValue("+channel.getName()+")*5.0/0x7fffff;\n";
		
		code.addOutputCode(outputCode);
	}
}