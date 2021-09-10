package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class NewMotor extends Block {

	
	private String name = "NewMotor";
	
	State speedState;
	State spState;
	
	public NewMotor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Speed",1,false));
		//outputPortList.add(new OutputPort(this,"Water_Level",2,false));
		
		spState=new State(this,1,"SerialPortState");
		stateList.add(spState);
		
		
		
		//pumpState=new State(this,1,"pumpState");
		//stateList.add(pumpState);
		//levelState=new State(this,2,"levelState");
		//stateList.add(levelState);
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
//		initCode+=pumpState.getName()+"=0;\n";
//		initCode+=levelState.getName()+"=0;\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		
		String derivativeCode="";
		
//		derivativeCode+=pumpState.getDerivativeName()+"=("
//				+this.getInputPortVariable(0)
//				+"*"+pumpK+"-"+pumpState.getName()+")"
//				+"*"+(1/pumpT)
//				+";\n";
//		
//		derivativeCode+=levelState.getDerivativeName()+"=("
//				+pumpState.getName()+"*"+waterLevelK+"-"+levelState.getName()+")"
//				+"*"+(1/waterLevelT)
//				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		
//		outputCode+=getOutputPortVariable(0)+"="
//				+pumpState.getName()
//				+";\n";
//		
//		outputCode+=getOutputPortVariable(1)+"="
//				+levelState.getName()
//				+ ";\n";
		
		code.addOutputCode(outputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		initCode+=pumpState.getName()+"="+0+";\n";
//		initCode+=levelState.getName()+"="+0+";\n"; 
		
		//1.Open the serial port
		String port = "\"/dev/ttyUSB0\"";
		int baudrate = 115200;
		initCode+="char msg[255];\n";
		initCode+="hComm = Serialport_Open("+port+", "+baudrate+",msg);\n";
	
		//initCode+="ssSetIWorkValue(0,hComm);\n"		
		
		code.addInitCode(initCode);
	}
	
	public void generateIncludeCodeC(CodeStructC code) {
		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
	}
	
	public void addLine(String originCode, String newLine) {
		
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+pumpState.getName()+";\n";
//		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+levelState.getName()+";\n";
		int bufLen = 255;
		
		outputCode+="char recvBuff["+bufLen+"];\n";
			
		outputCode+="Serialport_Recv(hComm,recvBuff,7);\n";		
		outputCode+="int speed=recvBuff[4]+(recvBuff[5]<<8);\n";			
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=speed;\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		int bufLen = 255;	
		derivativeCode+="unsigned char cmd[]={0xAA,0xAA,0x01,0x01,0x00,0x00,0x00};\n";
		derivativeCode+="int pwm = "+this.getInputPortVariable(0) +";\n";
		derivativeCode+="cmd[4] = (pwm&0xFF);\n";
		derivativeCode+="cmd[5] = (pwm&0xFF00)>>8;\n";
		derivativeCode+="cmd[6] = calcSum(cmd);\n";
		//outputCode+="int hComm=ssGetIWorkValue(0);\n";
	
		derivativeCode+="Serialport_Send(hComm,cmd,7);\n";		
//		derivativeCode+=pumpState.getDerivativeName()+"=("
//				+this.getInputPortVariable(0)
//				+"*"+pumpK+"-"+pumpState.getName()+")"
//				+"*"+(1/pumpT)
//				+";\n";
//		
//		derivativeCode+=levelState.getDerivativeName()+"=("
//				+pumpState.getName()+"*"+waterLevelK+"-"+levelState.getName()+")"
//				+"*"+(1/waterLevelT)
//				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
}
