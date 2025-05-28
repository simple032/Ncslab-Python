package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class xzInvertedPendulumSUSTbak extends Block {


	private String name = "xzInvertedPendulumSUST";

//	State speedState;
//	State spState;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("Real_X");
        outputNames.add("Angle");
        inputNames.add("in1");
    }

	public xzInvertedPendulumSUSTbak(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Real_X",1,false));
//		outputPortList.add(new OutputPort(this,"AngleSpeed",2,false));
		outputPortList.add(new OutputPort(this,"Angle",2,false));
		//outputPortList.add(new OutputPort(this,"Water_Level",2,false));

//		spState=new State(this,1,"SerialPortState");
//		stateList.add(spState);



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

		String initCode="";
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
		outputCode+="if(xzIPSUSTbool==false)Serialport_Send(hCommXzIPSUST,sendBuffXzIPSUST,strlen(sendBuffXzIPSUST));\n";
		//outputCode+="printf(\"to send msg:%s\\n\",sendBuffXzIPSUST);\n";

		//outputCode+="Serialport_Flush(hCommIPSUST);\n";

		outputCode+="char xzcmd[50]={0},xzcmd2[50]={0};\n";
		outputCode+="int xztmp = "+this.getInputPortVariable(0) +";\n";
		outputCode+="float xzacctmp = xztmp/9.424776;\n";
		outputCode+="float xzacc = (xztmp>=0)?xzacctmp:-xzacctmp;\n";
		outputCode+="int xzaccInt=(int)xzacc;\n";

		outputCode+="sprintf((char*)xzcmd,\"GZ100A%d\\r\\n\",xzaccInt);";

		outputCode+="if(xzIPSUSTbool==false)Serialport_Recv(hCommXzIPSUST,recvBuffXzIPSUST,62);\n";
		outputCode+="printf(\"recvBuff=%s\\n\",recvBuffXzIPSUST);\n";


		outputCode+="if(xzIPSUSTbool==true) Serialport_Send(hCommXzIPSUST,xzcmd,strlen(xzcmd));\n";
		outputCode+="if(xztmp<0) sprintf((char*)xzcmd2,\"GZ000V-500\\r\\n\");\n";
		outputCode+="else if(xztmp>0) sprintf((char*)xzcmd2,\"GZ000V500\\r\\n\");\n";
		outputCode+="else sprintf((char*)xzcmd2,\"GZ000V0\\r\\n\");\n";
		outputCode+="if(xzIPSUSTbool==true)usleep(5000);\n";
		outputCode+="if(xzIPSUSTbool==true)Serialport_Send(hCommXzIPSUST,xzcmd2,strlen(xzcmd2));\n";

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
		code.addStatementCode(statementCode);
	}
}
