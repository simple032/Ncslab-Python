package com.ncslab.block.testrig;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class DCMotorAngle extends Block {


	private String name = "NewMotor";

	private State speedState;

	private double motorK=106.25;
	private double motorT=0.07;

	public DCMotorAngle(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);


		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Speed",1,false));
		outputPortList.add(new OutputPort(this,"Angle",2,false));
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
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block DCMotorAngle:("+getBlockId()+")"+getBlockName()+"*/\n";
		 arraysCode+="double angledata=0;\n";
		 arraysCode+="double angledata1=0;\n";
		 arraysCode+="int angle_N=0;\n";
		 code.addArraysCode(arraysCode);
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
			String port1="\"/dev/ttyUSB1\"";
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
		switch(model.getModelMode()) {
		case Simulation:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=10000*"+speedState.getName()+";\n";
			break;
		case Compilation:

			int bufLen = 255;
			outputCode+="if(mp->majorStep>0){\n";
			//speed control
			outputCode+="unsigned char cmd[]={0xAA,0xAA,0x01,0x01,0x00,0x00,0x00};\n";
			outputCode+="int pwm =(int)("+this.getInputPortVariable(0) +"*5000);\n";
			outputCode+="pwm = pwm>=5000?5000:pwm;\n";
			outputCode+="pwm = pwm<=-5000?-5000:pwm;\n";
			outputCode+="cmd[4] = (pwm&0xFF);\n";
			outputCode+="cmd[5] = (pwm&0xFF00)>>8;\n";
			outputCode+="cmd[6] = calcSum(cmd);\n";
			outputCode+="Serialport_Send(hComm,cmd,7);\n";
			outputCode+="char recvBuff["+bufLen+"]={0};\n";
			outputCode+="Serialport_Recv(hComm,(uint8_t*)recvBuff,7);\n";
			outputCode+="int speed=recvBuff[4]+(recvBuff[5]<<8);\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=5.79582*speed;\n";

			//angle control
			outputCode+="unsigned char cmdangle[]={0x01,0x04,0x00,0x00,0x00,0x04,0xf1,0xc9};\n";
			outputCode+="Serialport_Send(hComm1,cmdangle,8);\n";
			outputCode+="char recvBuff1["+bufLen+"]={0};\n";
			outputCode+="Serialport_Recv(hComm1,(uint8_t*)recvBuff1,13);\n";
			outputCode+="double Angle=360-0.01098633*(recvBuff1[6]+(recvBuff1[5]<<8));\n";
			outputCode+="if(angle_N==0){\n";
			outputCode+="angledata=Angle;\n";
			outputCode+="angle_N=angle_N+1;}\n";
			outputCode+="if(angledata-Angle>=180){\n";
			outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=(1+angledata1)*360+Angle;\n";
			outputCode+="if((int)(model.time*100)%20==0){;\n";
			outputCode+="angledata1=1+angledata1;\n";
			outputCode+="}}else if(angledata-Angle<=-180){\n";
			outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=(-1+angledata1)*360+Angle;\n";
			outputCode+="if((int)(model.time*100)%20==0){;\n";
			outputCode+="angledata1=-1+angledata1;\n";
			outputCode+="}}else{\n";
			outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=(0+angledata1)*360+Angle;}\n";
			outputCode+="if((int)(model.time*100)%20==0){;\n";
	        outputCode+="angledata=Angle;}\n";
			//outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=Angle;\n";
			//outputCode+="for(int i=0; i<13; i++){printf(\"%d=%d \", i, recvBuff1[i]);} printf(\"\\n\");\n";
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

