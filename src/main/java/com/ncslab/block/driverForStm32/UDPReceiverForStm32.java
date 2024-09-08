package com.ncslab.block.driverForStm32;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class UDPReceiverForStm32 extends com.ncslab.block.Block{

	Parameter localPort;
	public UDPReceiverForStm32(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		outputPortList.add(new OutputPort(this,1,false));

		localPort=new Parameter(this,1,"localPort",paramValues.getString("UdpReceiverLocalPortForStm32"));
		parameterList.add(localPort);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";

//		initCode+=channel.getName()+"="+paramValues.getInt("PWMForStm32Channel")+";\n";
//
//		initCode+="TIM3_PWM_Init(2*1000000*STEP_SIZE-1,100-1,"+paramValues.getInt("PWMForStm32Channel")+");";
//
		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+channel.getName()+";\n";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initConfigCode="/*Code for initialization of block UDPReceiverForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";

		initConfigCode+=localPort.getName()+"="+paramValues.getInt("UdpReceiverLocalPortForStm32")+";\n";
		initConfigCode+="UDP_Receive_Config("+paramValues.getInt("UdpReceiverLocalPortForStm32")+");\n";

		code.addInitConfigCode(initConfigCode);

		String initCode="UDP_Server_Init();\n";
		code.addInitCode(initCode);

	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block UDPReceiverForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";

		outputCode+="if(model.majorStep==1){\n"
				+ outputPortList.get(0).getOutputSignalC().getName()+"=UDP_Receive_double("+paramValues.getInt("UdpReceiverLocalPortForStm32")+");"
				+"}\n";

		code.addOutputCode(outputCode);
	}
}
