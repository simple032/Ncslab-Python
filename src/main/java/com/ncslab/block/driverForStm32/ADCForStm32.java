package com.ncslab.block.driverForStm32;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class ADCForStm32 extends com.ncslab.block.Block{

	Parameter index;
	Parameter channel;
	public ADCForStm32(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		outputPortList.add(new OutputPort(this,1,false));

		index=new Parameter(this,1,"index",paramValues.getString("ADCForStm32Index"));
		channel=new Parameter(this,2,"channel",paramValues.getString("ADCForStm32Channel"));
		parameterList.add(index);
		parameterList.add(channel);
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

		String initConfigCode="/*Code for initialization of block ADCForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";

		initConfigCode+=index.getName()+"="+paramValues.getInt("ADCForStm32Index")+";\n";
		initConfigCode+=channel.getName()+"="+paramValues.getInt("ADCForStm32Channel")+";\n";
		initConfigCode+="MX_ADC_Config("+paramValues.getInt("ADCForStm32Index")+","+paramValues.getInt("ADCForStm32Channel")+");";

		code.addInitConfigCode(initConfigCode);

		String initCode="MX_DMA_Init();\n"
				+ "MX_ADC_Init();\n";
		code.addInitCode(initCode);

	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block PWMForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";

		outputCode+="if(model.majorStep==1){\n"
				+ outputPortList.get(0).getOutputSignalC().getName()+"=(float)(Get_Adc("+paramValues.getInt("ADCForStm32Index")+","+paramValues.getInt("ADCForStm32Channel")+")*3.3/65535);"
				+"}\n";

		code.addOutputCode(outputCode);
	}
}
