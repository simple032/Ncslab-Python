package com.ncslab.block.testrig;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class NewMotor extends Block {

	
	private String name = "NewMotor";
	
	private State speedState;
	
	private double motorK=0.01;
	private double motorT=0.09;
	
	public NewMotor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Speed",1,false));
	
		this.isHardware=true;
		
		switch(model.getModelMode()) {
		case Simulation:
			speedState=new State(this,1,"speedState");
			stateList.add(speedState);
			break;
		case Compilation:
			break;
		}
	}
	
	public String getHardwareDefineCodeC() {
		String hardwareDefineCode="";
		//hardwareDefineName="Block"+this.getBlockId()+"_WaterLevel";
		hardwareDefineCode+="HANDLE hComm;\n";
		hardwareDefineCode+="HANDLE hComm1;\n";
		return hardwareDefineCode;
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
		
		switch(model.getModelMode()) {
		case Simulation:
			initCode+=speedState.getName()+"="+0+";\n"; 
			break;
		case Compilation:
			//1.Open the serial port
			String port = "\"/dev/ttyUSB0\"";
			String port1 = "\"/dev/ttyUSB1\"";
			int baudrate = 115200;
			initCode+="char msg[255];\n";
			initCode+="char msg1[255];\n";
			initCode+="hComm = Serialport_Open((char *)"+port+", "+baudrate+",(char *)msg);\n";
			initCode+="hComm1 = Serialport_Open((char *)"+port1+", "+baudrate+",(char *)msg1);\n";
			break;
		}
		
		code.addInitCode(initCode);
	}
	
	public void generateIncludeCodeC(CodeStructC code) {
		//String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
	}
	
	public void addLine(String originCode, String newLine) {
		
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+pumpState.getName()+";\n";
//		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+levelState.getName()+";\n";
		
		
		switch(model.getModelMode()) {
		case Simulation:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=10000*"+speedState.getName()+";\n";
			break;
		case Compilation:
			int bufLen = 255;
			outputCode+="int sendLength=0;\n";
			outputCode+="if(mp->majorStep>0){\n";
			outputCode+="char recvBuff["+bufLen+"]={0};\n";
				
			outputCode+="Serialport_Recv(hComm,(uint8_t*)recvBuff,7);\n";		
			outputCode+="int speed=recvBuff[4]+(recvBuff[5]<<8)+(recvBuff[5]<<16)+(recvBuff[5]<<24);\n";	
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=speed;\n";
			
			outputCode+="unsigned char cmd[]={0xAA,0xAA,0x01,0x01,0x00,0x00,0x00};\n";
			outputCode+="int pwm = "+this.getInputPortVariable(0) +"*10000;\n";
//			outputCode+="printf(\"speed is %d,pwm is %d\\n\",speed,pwm);\n";
			outputCode+="pwm = pwm>=10000?10000:pwm;\n";
			outputCode+="pwm = pwm<=-10000?-10000:pwm;\n";
			outputCode+="cmd[4] = (pwm&0xFF);\n";
			outputCode+="cmd[5] = (pwm&0xFF00)>>8;\n";
			outputCode+="cmd[6] = calcSum(cmd);\n";
			outputCode+="Serialport_Send(hComm,cmd,7);\n";	
			//数码管显示
			outputCode+="char sendData[1024];\n";
            outputCode+="sprintf(sendData,\"$001,%02d#\",speed);\n";
			outputCode+="sendLength=strlen(sendData);\n";
			outputCode+="Serialport_Send(hComm1,sendData,sendLength);\n";	
			outputCode+="}\n";
			break;
		}
		
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		switch(model.getModelMode()) {
		case Simulation:
		derivativeCode+=speedState.getDerivativeName()+"=("
				+this.getInputPortVariable(0)
				+"*"+motorK+"-"+speedState.getName()+")"
				+"*"+(1/motorT)
				+";\n";
		
		case Compilation:
			break;
		}
		
		code.addDerivativeCode(derivativeCode);
	}
}
