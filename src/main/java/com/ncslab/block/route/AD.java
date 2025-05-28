package com.ncslab.block.route;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

import java.util.Vector;

public class AD extends Block{

	Parameter channel;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");
        parameterNames.add("Channel");

    }
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

		String initCode="";
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
