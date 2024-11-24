package com.ncslab.block.driverForStm32;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class ConfigForStm32 extends com.ncslab.block.Block{

	Parameter ip;
	Parameter netmask;
	Parameter gateway;
	Parameter port;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {
        parameterNames.add("ip");
        parameterNames.add("netmask");
        parameterNames.add("gateway");
        parameterNames.add("port");
        outputNames.add("out1");

    }
	public ConfigForStm32(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		outputPortList.add(new OutputPort(this,1,false));

		ip=new Parameter(this,1,"ip",paramValues.getString("ip_Stm32"));
		netmask=new Parameter(this,2,"netmask",paramValues.getString("netmask_Stm32"));
		gateway=new Parameter(this,3,"gateway",paramValues.getString("gateway_Stm32"));
		port=new Parameter(this,4,"port",paramValues.getString("monitorPort_Stm32"));
		parameterList.add(ip);
		parameterList.add(netmask);
		parameterList.add(gateway);
		parameterList.add(port);
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

		String initConfigCode="/*Code for initialization of block Config For Stm32:("+getBlockId()+")"+getBlockName()+"*/\n";

//		initConfigCode+=index.getName()+"="+paramValues.getInt("ADCForStm32Index")+";\n";
//		initConfigCode+=channel.getName()+"="+paramValues.getInt("ADCForStm32Channel")+";\n";
//		initConfigCode+="MX_ADC_Config("+paramValues.getInt("ADCForStm32Index")+","+paramValues.getInt("ADCForStm32Channel")+");";
//
//		code.addInitConfigCode(initConfigCode);
//
//		String initCode="MX_DMA_Init();\n"
//				+ "MX_ADC_Init();\n";
//		code.addInitCode(initCode);

	}

	public void generateOutputCodeC(CodeStructC code) {
//		String outputCode="/*Code for output of block PWMForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";

//		outputCode+="if(model.majorStep==1){\n"
//				+ outputPortList.get(0).getOutputSignalC().getName()+"=(float)(Get_Adc("+paramValues.getInt("ADCForStm32Index")+","+paramValues.getInt("ADCForStm32Channel")+")*3.3/65535);"
//				+"}\n";

//		code.addOutputCode(outputCode);
	}
}
