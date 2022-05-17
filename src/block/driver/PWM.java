package block.driver;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class PWM extends block.Block{
	
	block.io.Parameter port;
	public PWM(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ������
		inputPortList.add(new InputPort(this,1));
		
		port=new Parameter(this,1,"port",paramValues.getString("port"));
		parameterList.add(port);
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
		
		initCode+=port.getName()+"="+paramValues.getDouble("port")+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
				
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+port.getName()+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block PWM:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=port.getName()+"="+paramValues.getDouble("port")+";\n"; 
		
		initCode+="wiringPiSetupGpio();\n"
				+ "pinMode("+ paramValues.getDouble("port") +", PWM_OUTPUT);\r\n"
				+ "pwmSetMode (PWM_MODE_MS) ;	\r\n"
				+ "pwmSetClock(3);\r\n"
				+ "pwmSetRange(1000);";
				
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block PWM:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+="if(model.majorStep==1){\n"
				  +"int pwmduty = 0;\n"
				  +"pwmduty = " + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + "<=1000?" + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ":1000;\n"
				  +"pwmduty = pwmduty>=0?pwmduty:0;\n"
				  +"pwmWrite("+ paramValues.getDouble("port") +",pwmduty);\n"
				  //+"pwmWrite("+ paramValues.getDouble("port") +","+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() +");\n"
				  +"}\n";
		
		code.addOutputCode(outputCode);
	}
}