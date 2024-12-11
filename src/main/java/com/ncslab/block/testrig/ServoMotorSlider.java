package com.ncslab.block.testrig;

import java.util.Vector;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class ServoMotorSlider extends Block {


	private String name = "ServoMotorSlider";


	String hardwareDefineName;
	String realName;
	private Vector<State> xStateList=new Vector<State>();
	//simulation parameter
	private double num[] = {0,17.41,123.4};
	private double den[] = {1,2.01,38.86,49.06};


    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("Position");
        inputNames.add("in1");
    }

	public ServoMotorSlider(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);


		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Position",1,false));
		this.isHardware=true;

		switch(model.getModelMode()) {
		case Simulation:
			for(int i=0;i<3;i++) {
				State xState=new State(this,i+1,"x"+(i+1));
				xStateList.add(xState);
				stateList.add(xState);
			}
			break;

		case Compilation:
			break;
		}
	}

	public String getHardwareDefineCodeC() {
		String hardwareDefineCode="";
		//add some head files
		hardwareDefineCode+="#include\"DEV_Config.h\"\n";
		hardwareDefineCode+="#include\"DAC8532.h\"\n";
		hardwareDefineCode+="HANDLE hComm;\n";
		return hardwareDefineCode;
	}
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block ServoMotorSlider:("+getBlockId()+")"+getBlockName()+"*/\n";
		 arraysCode+="double servodata=0;\n";
		 arraysCode+="double servodata1=0;\n";
		 code.addArraysCode(arraysCode);
	 }

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

		switch(model.getModelMode()) {
		case Simulation:
			for(State xState:xStateList) {
				initCode+=xState.getName()+
						"=0;\n";
			}
			break;
		case Compilation:
			initCode+="DEV_ModuleInit();\n";
			//1.Open the serial port
			String port = "\"/dev/ttyUSB0\"";
			int baudrate = 115200;
			initCode+="char msg[255];\n";
			initCode+="hComm = Serialport_Open((char *)"+port+", "+baudrate+",(char *)msg);\n";
			break;
		}

		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			outputCode+=getOutputPortVariable(0)+"=0";
			int i=num.length-1;
			for(State xState:xStateList) {
				outputCode+="+"+xState.getName()+"*"+num[i];
				i--;
			}
			outputCode+=";\n";
			break;
		case Compilation:

			int bufLen = 1024;
			outputCode+="char recvBuff["+bufLen+"]={0};\n";
			hardwareDefineName="Block"+this.getBlockId()+"_ServoMotorSlider";
			outputCode+="uint32_t chns=channel_A;\n";

			outputCode+="if(mp->majorStep>0){\n";
			//DA_Channel
			outputCode+="double da="+this.getInputPortVariable(0)+";\n";
			outputCode+="da=da>2.5?2.5:da;\n";
			outputCode+="da=da<-2.5?-2.5:da;\n";
			//Limit input less than 80 to protect
			outputCode+="if((da>0&&servodata1>80)||(da<0&&servodata1<-80)){\n";
			outputCode+="da=0;}\n";
			outputCode+="da=da + 2.5;\n";
			outputCode+="printf(\"da=%f\",da);\n";
			outputCode+="DAC8532_Out_Voltage(chns,da);\n";

			//position return
			outputCode+="unsigned char cmd[]={0x01,0x04,0x04,0x00,0x00,0x02,0x70,0xFB};\n";
			outputCode+="Serialport_Send(hComm,cmd,8);\n";
			outputCode+="int k=Serialport_Recv(hComm,(uint8_t*)recvBuff,sizeof(recvBuff));\n";
			//position parameter
			outputCode+="int16_t Read=(recvBuff[4]+(recvBuff[3]<<8));\n";
			outputCode+="for(int i=0; i<k; i++){printf(\"%d=%d \", i, recvBuff[i]);} printf(\"\\n\");\n";
			outputCode+="if((Read>0.5)||(Read<-0.5)){\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=(-0.0039*Read);\n";
			outputCode+="printf(\"out=%f\","+outputPortList.get(0).getOutputSignalC().getName()+");\n";
			outputCode+="servodata=Read;\n";
			outputCode+="servodata1=(-0.0039*Read);\n";
			outputCode+="}else{\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=(-0.0039)*servodata;}\n";
			outputCode+="servodata1=(-0.0039*servodata);\n";
			outputCode+="}\n";
			break;
		}

		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

		switch(model.getModelMode()) {
		case Simulation:
			for(int i=0;i<xStateList.size()-1;i++) {
				derivativeCode+=xStateList.get(i).getDerivativeName()+"="
						+xStateList.get(i+1).getName()
						+";\n";
			}
			derivativeCode+=xStateList.get(xStateList.size()-1).getDerivativeName()+"=("+getInputPortVariable(0);
			int i=den.length-1;
			for(State xState:xStateList) {
				derivativeCode+="-"+xState.getName()+"*"+den[i];
				i--;
			}
			derivativeCode+=");\n";
		     break;
		case Compilation:
			break;
		}

		code.addDerivativeCode(derivativeCode);
	}

}


