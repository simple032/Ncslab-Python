package com.ncslab.block.driver;

import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class Ad extends com.ncslab.block.Block{

	Parameter channel;
	public Ad(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����
		outputPortList.add(new OutputPort(this,1,false));

		channel=new Parameter(this,1,"channel",paramValues.getString("Channel"));
		parameterList.add(channel);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";

		initCode+=channel.getName()+"="+paramValues.getDouble("Channel")+";\n";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+channel.getName()+";\n";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block AD:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=channel.getName()+"="+paramValues.getDouble("Channel")+";\n";

		initCode+="if(AD_init_Flag==0){\r\n"
				+"DEV_ModuleInit();\n"
				+"if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n"
				+"AD_init_Flag=1;\r\n"
				+ "    }\n";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block AD:("+getBlockId()+")"+getBlockName()+"*/\n";


		outputCode+="if(model.majorStep==1){\n"
				  +outputPortList.get(0).getOutputSignalC().getName()+"="+"ADS1256_GetChannalValue("+channel.getName()+")*5.0/0x7fffff;\n"
				  +"}\n";

		code.addOutputCode(outputCode);
	}
}
