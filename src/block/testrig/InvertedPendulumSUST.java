package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class InvertedPendulumSUST extends Block {

	
	private String name = "InvertedPendulumSUST";
	block.io.Parameter Vspeed;
	block.io.Parameter ENAOrDIS;
	
//	State speedState;
//	State spState;
	
	public InvertedPendulumSUST(JSONObject blockJSON,NCSLabModel model) {
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
		initCode+="char IPSUSTmsg[255];\n";
		initCode+="hCommIPSUST = Serialport_Open("+port+", "+baudrate+",IPSUSTmsg);\n";
		
		
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
	
		initCode+="tcflush(hCommIPSUST,TCIOFLUSH);\n";
		
		initCode+=" char cmdInit[] = {\"GZ200ENA\\r\\n\"};\n"
		+ "Serialport_Send(hCommIPSUST,cmdInit,strlen(cmdInit));\n"
		+ "usleep(3000);\n";
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
		outputCode+="char recvBuffIPSUST["+bufLen+"]={0};\n";
		
		//outputCode+="if(IPSUSTbool==false)tcflush(hCommIPSUST,TCIOFLUSH);\n";
		outputCode+="char sendBuffIPSUST[]={\"GZ999POS*=?\\r\\n\"};\n"
				+ "char sendLimitBuffSUST[]={\"GZ999POSLIM*=?\\r\\n\"};\n";
		outputCode+="if(IPSUSTbool==false){"
//				+ "Serialport_Send(hCommIPSUST,sendBuffIPSUST,strlen(sendBuffIPSUST));\n"
//				+ "usleep(2500);\n"
				+ "Serialport_Send(hCommIPSUST,sendLimitBuffSUST,strlen(sendLimitBuffSUST));\n"
				+ "usleep(2500);\n"
				+ "}";	
		//outputCode+="printf(\"to send msg:%s\\n\",sendBuffIPSUST);\n";
		
		//outputCode+="Serialport_Flush(hCommIPSUST);\n";
			
		outputCode+="char cmd[50]={0},cmd2[50]={0};\n";
		outputCode+="int tmp = "+this.getInputPortVariable(0) +";\n";
		outputCode+="float acctmp = tmp/9.424776;\n";
		outputCode+="float ipacc = (tmp>=0)?acctmp:-acctmp;\n";
		outputCode+="int accInt=(int)ipacc;\n";
//		outputCode+="int VspeedVaule = (int)"+ paramValues.getDouble("Vspeed") +";\n";
		outputCode+="int VspeedVaule = (int)"+this.getInputPortVariable(1) +">0?"+this.getInputPortVariable(1)+":-"+this.getInputPortVariable(1)+";\n";
		
		
		outputCode+="sprintf((char*)cmd,\"GZ100A%d\\r\\n\",accInt);";
		
		outputCode+="if(IPSUSTbool==false){Serialport_Recv(hCommIPSUST,recvBuffIPSUST,74);\n";
		outputCode+="printf(\"recvBuff=%s\\n\",recvBuffIPSUST);}\n";
		
		
		outputCode+="if(IPSUSTbool==true) Serialport_Send(hCommIPSUST,cmd,strlen(cmd));\n";
		outputCode+="if(tmp<0&&(int)"+ENAOrDIS.getName()+"==1) sprintf((char*)cmd2,\"GZ000V-%d\\r\\n\",VspeedVaule);\n";
		outputCode+="else if(tmp>0&&(int)"+ENAOrDIS.getName()+"==1) sprintf((char*)cmd2,\"GZ000V%d\\r\\n\",VspeedVaule);\n";
		outputCode+="else sprintf((char*)cmd2,\"GZ000V0\\r\\n\");\n";
		outputCode+="if(IPSUSTbool==true)usleep(2500);\n";
		outputCode+="if(IPSUSTbool==true)Serialport_Send(hCommIPSUST,cmd2,strlen(cmd2));\n";
		
//		outputCode+="int pwm = "+this.getInputPortVariable(0) +";\n";
//		outputCode+="pwm = pwm>=30000?30000:pwm;\n";
//		outputCode+="pwm = pwm<=-30000?-30000:pwm;\n";
//		outputCode+="sprintf((char*)cmd,\"GZ000V%d\\r\\n\",pwm);";
//		outputCode+="Serialport_Send(hCommIPSUST,cmd,strlen(cmd));\n";
			
		//derivativeCode+="printf(\"send over\\n\");\n";
		
		outputCode+="char headStrPOS[]={\"POS\"},headStrLIM[]={\"LIM\"};\n";
		outputCode+="int pos1=0,pos2=0,leftLim=0,rightLim=0;\n";
		
//		outputCode+="float Angle=0,xPOS=0;\n";
		
		//outputCode+="memset(recvBuffIPSUST,0,sizeof(recvBuffIPSUST));\n";
		
		//outputCode+="tcflush(hCommIPSUST,TCIFLUSH);\n";
		
		outputCode+="char *keyStart = strstr(recvBuffIPSUST,headStrPOS);\n";
		outputCode+="if(keyStart!=NULL){\n"
				+ "keyStart+=3;\n"
				+ "pos1 = strtol(keyStart,&keyStart,10);\n"
				+ "pos2 = strtol(keyStart+1,NULL,10);\n"
				+ "xPOSIP = pos2*9.424776/4000;\n"
				+ "AngleIP = (pos1%4000)*360.0/4000.0;\n"
				+ "AngleIP = (pos1>=0)?(AngleIP-180):(AngleIP+180);\n"
				+ "}\n";
		
		outputCode+="char *keyStartLIM = strstr(recvBuffIPSUST,headStrLIM);\n";
		outputCode+="if(keyStartLIM!=NULL){\n"
				+ "keyStartLIM+=3;\n"
				+ "leftLim = strtol(keyStartLIM,&keyStartLIM,10);\n"
				+ "rightLim = strtol(keyStartLIM+1,NULL,10);\n"
				+ "}";
		
		
		
				
		outputCode+="if(IPSUSTbool==false)"+outputPortList.get(0).getOutputSignalC().getName()+"=xPOSIP;\n";
		outputCode+="if(IPSUSTbool==false)"+outputPortList.get(1).getOutputSignalC().getName()+"=AngleIP;\n";
		outputCode+="IPSUSTbool=!IPSUSTbool;\n";
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		statementCode +="HANDLE hCommIPSUST;\n";
		statementCode +="bool IPSUSTbool=true;\n";
		statementCode +="float AngleIP=0,xPOSIP=0;\n";
		statementCode +="int SwingUpFlag=0;\n";
		code.addStatementCode(statementCode);
	}
}
