package com.ncslab.block.driver;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Da extends com.ncslab.block.Block{

	Parameter channel;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();


    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        parameterNames.add("channel");
        inputNames.add("in1");
    }
	public Da(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		inputPortList.add(new InputPort(this,1));

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

		String initCode="/*Code for initialization of block DA:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=channel.getName()+"="+paramValues.getDouble("Channel")+";\n";

		initCode+="if(DA_init_Flag==0){\r\n"
				+"DEV_ModuleInit1();\n"
				+"DAC8532_Out_Voltage(channel_A, 0);\n"
				+"DAC8532_Out_Voltage(channel_B, 0);\n"
				+"DA_init_Flag=1;\n"
				+"}\n"
				+"\n";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block DA:("+getBlockId()+")"+getBlockName()+"*/\n";


		outputCode+="if(model.majorStep==1){\n"
				  +"float da_voltage = 0;\n"
				  +"da_voltage = "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + "<=3.3?" + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ":3.3;\n"
				  +"da_voltage = da_voltage>=0?da_voltage:0;\n"
				  +"if("+paramValues.getDouble("Channel")+"){//1����channel_B /\n"
				  +"DAC8532_Out_Voltage(channel_B, da_voltage);\n"
				  +"}\n"
				  +"else{//0����channel_A /\n"
				  +"DAC8532_Out_Voltage(channel_A, da_voltage);\n"
				  +"}\n"
				  +"}\n";

		code.addOutputCode(outputCode);
	}
}
