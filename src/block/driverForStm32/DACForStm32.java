package block.driverForStm32;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class DACForStm32 extends block.Block{
	
	block.io.Parameter index;
	block.io.Parameter channel;
	public DACForStm32(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ������
		inputPortList.add(new InputPort(this,1));
		
		index=new Parameter(this,1,"index",paramValues.getString("DACForStm32Index"));
		channel=new Parameter(this,2,"channel",paramValues.getString("DACForStm32Channel"));
		parameterList.add(index);
		parameterList.add(channel);
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
		
//		initCode+=channel.getName()+"="+paramValues.getInt("PWMForStm32Channel")+";\n"; 
//		initCode+="TIM3_PWM_Init(2*1000000*STEP_SIZE-1,100-1,"+paramValues.getInt("PWMForStm32Channel")+");";
		
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
		
		String initConfigCode="/*Code for initialization of block DACForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		initConfigCode+=index.getName()+"="+paramValues.getInt("DACForStm32Index")+";\n"; 
		initConfigCode+=channel.getName()+"="+paramValues.getInt("DACForStm32Channel")+";\n"; 
		initConfigCode+="MX_DAC_Config("+paramValues.getInt("DACForStm32Index")+","+paramValues.getInt("DACForStm32Channel")+");";
		
		code.addInitConfigCode(initConfigCode);
		
		String initCode="DAC_Init();\n"
				+ "DAC_Set_Vol("+paramValues.getInt("DACForStm32Index")+","+paramValues.getInt("DACForStm32Channel")+",0);\n";
		code.addInitCode(initCode);
		
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block PWMForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+="if(model.majorStep==1){\n"
				+ "DAC_Set_Vol("+paramValues.getInt("DACForStm32Index")+","+paramValues.getInt("DACForStm32Channel")+","+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+");\n"
				  +"}\n";
		
		code.addOutputCode(outputCode);
	}
}