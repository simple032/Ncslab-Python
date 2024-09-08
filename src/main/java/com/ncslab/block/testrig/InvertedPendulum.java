package com.ncslab.block.testrig;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class InvertedPendulum extends Block {


	private String name = "InvertedPendulum";

	State speedState;
	State spState;

	public InvertedPendulum(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Angle",1,false));
		outputPortList.add(new OutputPort(this,"Set_X",2,false));
		outputPortList.add(new OutputPort(this,"Real_X",3,false));
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
		String port2 = "\"/dev/ttyUSB1\"";
		int baudrate = 115200;
		initCode+="char msg[255];\n";
		initCode+="int tmpHCommon1 = -1,tmpHCommon2 = -1;\n";

		//initCode+="hComm = Serialport_Open("+port+", "+baudrate+",msg);\n";
		//initCode+="if(hComm==-1){hComm = Serialport_Open("+port2+", "+baudrate+",msg);};\n";

		initCode+="while((tmpHCommon1=Serialport_Open("+port+", "+baudrate+",msg))<0&&(tmpHCommon2=Serialport_Open("+port2+", "+baudrate+",msg))<0){}\n";
		//initCode+="	fprintf (stderr, "Unable to open serial device: %s\n", strerror (errno)) ;\n";
		initCode+="hComm = tmpHCommon1>0?tmpHCommon1:tmpHCommon2;\n";

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

		outputCode+="union float_data\n";
		outputCode+="{\n";
		outputCode+="float f_data;\n";
		outputCode+="uint8_t byte[4];\n";
		outputCode+="}tx_float_data,rx_Angle_float_data,rx_xSet_float_data,rx_x_float_data;\n";

		outputCode+="if(mp->majorStep>0){\n";
		outputCode+="char recvBuff["+bufLen+"]={0};\n";

		outputCode+="Serialport_Recv(hComm,recvBuff,17);\n";

		outputCode+="for(int i=0;i<17-12;i++){\n";
		outputCode+="if(recvBuff[i]==0x05&&recvBuff[i+1]==0x03&&recvBuff[i+2]==0xE1){\n";
		outputCode+="rx_Angle_float_data.byte[0]=recvBuff[i+3];\n";
		outputCode+="rx_Angle_float_data.byte[1]=recvBuff[i+4];\n";
		outputCode+="rx_Angle_float_data.byte[2]=recvBuff[i+5];\n";
		outputCode+="rx_Angle_float_data.byte[3]=recvBuff[i+6];\n";

		outputCode+="rx_xSet_float_data.byte[0]=recvBuff[i+7];\n";
		outputCode+="rx_xSet_float_data.byte[1]=recvBuff[i+8];\n";
		outputCode+="rx_xSet_float_data.byte[2]=recvBuff[i+9];\n";
		outputCode+="rx_xSet_float_data.byte[3]=recvBuff[i+10];\n";

		outputCode+="rx_x_float_data.byte[0]=recvBuff[i+11];\n";
		outputCode+="rx_x_float_data.byte[1]=recvBuff[i+12];\n";
		outputCode+="rx_x_float_data.byte[2]=recvBuff[i+13];\n";
		outputCode+="rx_x_float_data.byte[3]=recvBuff[i+14];\n";
		outputCode+="break;\n";
		outputCode+="}}\n";
		outputCode+="Serialport_Flush(hComm);\n";

		outputCode+="float Angle=rx_Angle_float_data.f_data;\n";
		outputCode+="float xSet=rx_xSet_float_data.f_data;\n";
		outputCode+="float x=rx_x_float_data.f_data;\n";

		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=Angle;\n";
		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=xSet;\n";
		outputCode+=outputPortList.get(2).getOutputSignalC().getName()+"=x;\n";


		outputCode+="unsigned char cmd[]={0x00,0x00,0x00,0x00,0x00,0x0D,0x0A};\n";
		outputCode+="float xSet2 = "+this.getInputPortVariable(0) +";\n";

		outputCode+="tx_float_data.f_data = xSet2;\n";
		outputCode+="cmd[1] = tx_float_data.byte[0];\n";
		outputCode+="cmd[2] = tx_float_data.byte[1];\n";
		outputCode+="cmd[3] = tx_float_data.byte[2];\n";
		outputCode+="cmd[4] = tx_float_data.byte[3];\n";
		outputCode+="cmd[0] = calcSum(cmd);\n";

		outputCode+="Serialport_Send(hComm,cmd,7);\n";

		outputCode+="}\n";

		code.addOutputCode(outputCode);
	}
}
