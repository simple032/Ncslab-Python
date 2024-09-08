package com.ncslab.block.testrig;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class xzInvertedPendulumSUST extends Block {


	private String name = "xzInvertedPendulumSUST";
	Parameter Vspeed;
	Parameter ENAOrDIS;
	Parameter POS0Flag;
//	State speedState;
//	State spState;

	public xzInvertedPendulumSUST(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		outputPortList.add(new OutputPort(this,"Real_X",1,false));
//		outputPortList.add(new OutputPort(this,"AngleSpeed",2,false));
		outputPortList.add(new OutputPort(this,"Angle",2,false));
		//outputPortList.add(new OutputPort(this,"Water_Level",2,false));

//		spState=new State(this,1,"SerialPortState");
//		stateList.add(spState);
		Vspeed=new Parameter(this,parameterList.size()+1,"Vspeed",paramValues.getString("Vspeed"));
		parameterList.add(Vspeed);
		ENAOrDIS=new Parameter(this,parameterList.size()+1,"ENAOrDIS",paramValues.getString("ENAOrDIS"));
		parameterList.add(ENAOrDIS);
		POS0Flag=new Parameter(this,parameterList.size()+1,"POS0Flag",paramValues.getString("POS0Flag"));
		parameterList.add(POS0Flag);


		//pumpState=new State(this,1,"pumpState");
		//stateList.add(pumpState);
		//levelState=new State(this,2,"levelState");
		//stateList.add(levelState);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		code.addInitCode(initCode);
	}

	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);

		String derivativeCode="";

		code.addDerivativeCode(derivativeCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

		code.addOutputCode(outputCode);
	}


	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		//1.Open the serial port
		String port = "\"/dev/ttyUSB0\"";
		int baudrate = 115200;
		initCode+="char xzIPSUSTmsg[255];\n";
		initCode+="hCommXzIPSUST = Serialport_Open("+port+", "+baudrate+",xzIPSUSTmsg);\n";


//		initCode+="char initCmd[50]={0};\n";
//
//		initCode+="sprintf((char*)initCmd,\"GZ200DIS\\r\\n\");\n";
//		initCode+="Serialport_Send(hCommIPSUST,initCmd,strlen(initCmd));\n";
//		initCode+="memset(initCmd,0,sizeof(initCmd));\n";
//
//		initCode+="sprintf((char*)initCmd,\"GZ000V0\\r\\n\");\n";
//		initCode+="Serialport_Send(hCommIPSUST,initCmd,strlen(initCmd));\n";
//		initCode+="memset(initCmd,0,sizeof(initCmd));\n";
//
//		initCode+="sprintf((char*)initCmd,\"GZ200SCS0\\r\\n\");\n";
//		initCode+="Serialport_Send(hCommIPSUST,initCmd,strlen(initCmd));\n";
//		initCode+="memset(initCmd,0,sizeof(initCmd));\n";
//
//		initCode+="sprintf((char*)initCmd,\"GZ200ENA\\r\\n\");\n";
//		initCode+="Serialport_Send(hCommIPSUST,initCmd,strlen(initCmd));\n";
//		initCode+="memset(initCmd,0,sizeof(initCmd));\n";
//
//		initCode+="sprintf((char*)initCmd,\"GZ200PO0\\r\\n\");\n";
//		initCode+="Serialport_Send(hCommIPSUST,initCmd,strlen(initCmd));\n";

		initCode+="tcflush(hCommXzIPSUST,TCIOFLUSH);\n";

//		initCode+=" char cmdInit[] = {\"GZ999POS*=0\\r\\n\"};\n"
//				+ "if("+paramValues.getDouble("POS0Flag")+"==1) {Serialport_Send(hCommXzIPSUST,cmdInit,strlen(cmdInit));\n"
//				+ "usleep(3000);}\n";



		initCode+="char cmdInit2[] = {\"GZ200ENA\\r\\n\"};\n"
				+ "char cmdInit4[] = {\"GZ200DIS\\r\\n\"};\n"
				+ "if((int)"+paramValues.getDouble("ENAOrDIS")+"==1) Serialport_Send(hCommXzIPSUST,cmdInit2,strlen(cmdInit2));\n"
				+ "else Serialport_Send(hCommXzIPSUST,cmdInit4,strlen(cmdInit4));\n"
				+ "usleep(15000);\n";
		initCode+=" char cmdInit3[] = {\"GZ000A10\\r\\n\"};\n"
				+ "Serialport_Send(hCommXzIPSUST,cmdInit3,strlen(cmdInit3));\n"
				+ "usleep(5000);\n";
		initCode+=" char cmdInit[] = {\"GZ000V100\\r\\n\"};\n"
				+ "Serialport_Send(hCommXzIPSUST,cmdInit,strlen(cmdInit));\n"
				+ "usleep(1500000);\n";


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

		outputCode+="if(mp->majorStep>0){\n";
		outputCode+="char recvBuffXzIPSUST["+bufLen+"]={0};\n";

		//outputCode+="if(IPSUSTbool==false)tcflush(hCommIPSUST,TCIOFLUSH);\n";
		outputCode+="char sendBuffXzIPSUST[]={\"GZ999POS*=?\\r\\n\"};\n";
		outputCode+="if(xzIPSUSTbool==false) {Serialport_Send(hCommXzIPSUST,sendBuffXzIPSUST,strlen(sendBuffXzIPSUST));\n"


				+" char cmdClearPos[] = {\"GZ999POS*=0\\r\\n\"};\n"
				+ "if((int)"+paramValues.getDouble("POS0Flag")+"==1) {Serialport_Send(hCommXzIPSUST,cmdClearPos,strlen(cmdClearPos));\n"
				+ "usleep(3000);}\n"

				+ "}";
		//outputCode+="printf(\"to send msg:%s\\n\",sendBuffXzIPSUST);\n";

		//outputCode+="Serialport_Flush(hCommIPSUST);\n";

		outputCode+="char xzcmd[50]={0},xzcmd2[50]={0};\n";
		outputCode+="int xztmp = "+this.getInputPortVariable(0) +";\n";
		outputCode+="float xzacctmp = xztmp/9.424776;\n";
		outputCode+="float xzacc = (xztmp>=0)?xzacctmp:-xzacctmp;\n";
		outputCode+="int xzaccInt=(int)xzacc;\n";
		outputCode+="int VspeedValue = (int)"+this.getInputPortVariable(1) +">0?"+this.getInputPortVariable(1)+":-"+this.getInputPortVariable(1)+";\n";


		outputCode+="sprintf((char*)xzcmd,\"GZ100A%d\\r\\n\",xzaccInt);";

		outputCode+="if(xzIPSUSTbool==false)Serialport_Recv(hCommXzIPSUST,recvBuffXzIPSUST,62);\n";
		outputCode+="printf(\"recvBuff=%s\\n\",recvBuffXzIPSUST);\n";

//		outputCode+="if(xzIPSUSTbool==true) {"
//				+ "char cmdInitENA[] = {\"GZ200ENA\\r\\n\"};\n"
//				+ "char cmdInitDIS[] = {\"GZ200DIS\\r\\n\"};\n"
//				+ "if((int)"+paramValues.getDouble("ENAOrDIS")+"==0){"
//					+ "Serialport_Send(hCommXzIPSUST,\"GZ000V000\\r\\n\",strlen(cmdInit));\n"
//					+ "usleep(5000);\n"
//				+ "}else{"
//					+ "if(lastDisOrEna==0) {"
//					+ "Serialport_Send(hCommXzIPSUST,cmdInitENA,strlen(cmdInitENA));\n"
//					+ "usleep(4000);\n"
//					+ "}"
//				+ "}"
//				+ "lastDisOrEna=(int)"+paramValues.getDouble("ENAOrDIS")+";\n"
//				+ "}\n";


		outputCode+="if(xzIPSUSTbool==true) Serialport_Send(hCommXzIPSUST,xzcmd,strlen(xzcmd));\n";
		outputCode+="if(xztmp<0&&(int)"+ENAOrDIS.getName()+"==1) sprintf((char*)xzcmd2,\"GZ000V-%d\\r\\n\",VspeedValue);\n";
		outputCode+="else if(xztmp>0&&(int)"+ENAOrDIS.getName()+"==1) sprintf((char*)xzcmd2,\"GZ000V%d\\r\\n\",VspeedValue);\n";
		outputCode+="else sprintf((char*)xzcmd2,\"GZ000V0\\r\\n\");\n";
		outputCode+="if(xzIPSUSTbool==true)usleep(3000);\n";
		outputCode+="if(xzIPSUSTbool==true) Serialport_Send(hCommXzIPSUST,xzcmd2,strlen(xzcmd2));\n";

//		outputCode+="int pwm = "+this.getInputPortVariable(0) +";\n";
//		outputCode+="pwm = pwm>=30000?30000:pwm;\n";
//		outputCode+="pwm = pwm<=-30000?-30000:pwm;\n";
//		outputCode+="sprintf((char*)cmd,\"GZ000V%d\\r\\n\",pwm);";
//		outputCode+="Serialport_Send(hCommIPSUST,cmd,strlen(cmd));\n";

		//derivativeCode+="printf(\"send over\\n\");\n";

		outputCode+="char xzheadStr[]={\"POS\"};\n";
		outputCode+="int xzpos1=0,xzpos2=0;\n";

//		outputCode+="float xzAngle=0,xzxPOS=0;\n";

		//outputCode+="memset(recvBuffIPSUST,0,sizeof(recvBuffIPSUST));\n";

		//outputCode+="tcflush(hCommIPSUST,TCIFLUSH);\n";

		outputCode+="char *xzkeyStart = strstr(recvBuffXzIPSUST,xzheadStr);\n";
		outputCode+="if(xzkeyStart!=NULL){\n"
				+ "xzkeyStart+=3;\n"
				+ "xzpos1 = strtol(xzkeyStart,&xzkeyStart,10);\n"
				+ "xzpos2 = strtol(xzkeyStart+1,NULL,10);\n";
		outputCode+="xzxPOS = xzpos2*360.0/(16*2000.0);\n";
		outputCode+="xzAngle = (xzpos1%4000)*360.0/4000.0;\n";
		outputCode+="xzAngle = (xzpos1>=0)?(xzAngle-180):(xzAngle+180);\n"
				+ "}\n";



		outputCode+="if(xzIPSUSTbool==false)"+outputPortList.get(0).getOutputSignalC().getName()+"=xzxPOS;\n";
		outputCode+="if(xzIPSUSTbool==false)"+outputPortList.get(1).getOutputSignalC().getName()+"=xzAngle;\n";
		outputCode+="xzIPSUSTbool=!xzIPSUSTbool;\n";
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";


		code.addDerivativeCode(derivativeCode);
	}

	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		statementCode +="HANDLE hCommXzIPSUST;\n";
		statementCode +="bool xzIPSUSTbool=true;\n";
		statementCode +="float xzAngle=0,xzxPOS=0;\n";
		statementCode +="int lastDisOrEna=0;\n";
		code.addStatementCode(statementCode);
	}
}
