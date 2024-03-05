package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class BallPlateSUST extends Block {

	
	private String name = "BallPlateSUST";
	
//	State speedState;
//	State spState;
	
	public BallPlateSUST(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		outputPortList.add(new OutputPort(this,"Real_X",1,false));
//		outputPortList.add(new OutputPort(this,"AngleSpeed",2,false));
		outputPortList.add(new OutputPort(this,"Real_Y",2,false));
		outputPortList.add(new OutputPort(this,"MotorXPos",3,false));
		outputPortList.add(new OutputPort(this,"MotorYPos",4,false));
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
		
		String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		//1.Open the serial port
		String port = "\"/dev/ttyUSB0\"";
		int baudrate = 115200;
		initCode+="char BPSUSTmsg[255];\n";
//		initCode+="hCommBPSUST = Serialport_Open("+port+", "+baudrate+",BPSUSTmsg);\n";
		
		
		initCode+="	struct termios options;\n"
				+ "    hCommBPSUST = open(\"/dev/ttyUSB0\", O_RDWR | O_NOCTTY);\n"
				+ "    if (hCommBPSUST == -1) {\n"
				+ "        perror(\"无法打开串口设备\");\n"
				+ "        // exit(EXIT_FAILURE);\n"
				+ "    }\n"
				+ "    tcgetattr(hCommBPSUST, &options);\n"
				+ "    cfsetispeed(&options, B115200);\n"
				+ "    cfsetospeed(&options, B115200);\n"
				+ "    options.c_cflag &= ~CSIZE; // 清除数据位设置\n"
				+ "    options.c_cflag |= CS8; // 设置数据位为8位\n"
				+ "    options.c_cflag &= ~PARENB; // 禁用校验位\n"
				+ "    options.c_cflag &= ~CSTOPB; // 设置停止位为1位\n"
				+ "    tcsetattr(hCommBPSUST, TCSANOW, &options);"
				+ "\n"
				+ " char cmdInit[] = {\"GZ999POS*=0\\r\\n\"};\n"
				+ "Serialport_Send(hCommBPSUST,cmdInit,strlen(cmdInit));\n"
				+ "usleep(3000);\n";
		//initCode+="tcflush(hCommBPSUST,TCIOFLUSH);\n";
		

		//2.Open touch board
		initCode+="FILE *devices_file = fopen(\"/proc/bus/input/devices\", \"r\");\n"
				+ "    if (devices_file == NULL) {\n"
				+ "        perror(\"Failed to open devices file\");\n"
				+ "    }\n"
				+ "const char *command = \"xinput disable \\\"Touch p303\\\"\"; \n"
				+ "int status = system(command);\n"
				+ "if (status == -1) {\n"
				+ "   perror(\"Failed to execute the command\");\n"
				+ "}\n"
				+ "char line[256];\n"
				+ "int touchpad_event_number = 1;\n"
				+ "while (fgets(line, sizeof(line), devices_file)) {\n"
				+ "		if (strstr(line, TOUCHPAD_NAME)) {\n"
				+ "			for(int j=0;j<4;j++){\n"
				+ "				fgets(line, sizeof(line), devices_file);\n"
				+ "				char *event_start = strstr(line, \"event\");\n"
				+ "				if (event_start) {\n"
				+ "					touchpad_event_number = atoi(event_start + strlen(\"event\"));\n"
				+ "					break;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "    }\n"
				+ "fclose(devices_file);\n"
				+ "char event_path[20];\n"
				+ "    snprintf(event_path, sizeof(event_path), \"/dev/input/event%d\", touchpad_event_number);\n"
				+ "BPfd = open(event_path, O_RDONLY | O_NONBLOCK);\n"
				+ "    if (BPfd == -1) {\n"
				+ "        perror(\"Failed to open event device\");\n"
				+ "    }\n"
				+ ""
				+ "    int bpflags = fcntl(BPfd, F_GETFL, 0);\n"
				+ "    fcntl(BPfd, F_SETFL, bpflags | O_NONBLOCK);\n";
				
		
		
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
		outputCode+="char recvBuffBPSUST["+bufLen+"]={0};\n";
		
		outputCode+="char bpcmd[50]={0},bpcmd2[50]={0};char bpcmd3[50]={0},bpcmd4[50]={0};double accTmp;\n";
		outputCode+="accTmp=fabs("+this.getInputPortVariable(0)+")+0.8;\n"
				+ "int bptmp = "+this.getInputPortVariable(0) +";\n";
		outputCode+="int bptmp2 = "+this.getInputPortVariable(1) +";\n";

		
		outputCode+="char BPposcmd[]={\"GZ999POS*\\r\\n\"};\n";

		
//		outputCode+="if(bptmp<0) sprintf((char*)bpcmd2,\"GZ000V-200\\r\\n\");\n";
//		outputCode+="else if(bptmp>0) sprintf((char*)bpcmd2,\"GZ000V200\\r\\n\");\n";
//		outputCode+="else sprintf((char*)bpcmd2,\"GZ000V0\\r\\n\");\n";
//		
//		outputCode+="if(bptmp2<0) sprintf((char*)bpcmd4,\"GZ001V-200\\r\\n\");\n";
//		outputCode+="else if(bptmp2>0) sprintf((char*)bpcmd4,\"GZ001V200\\r\\n\");\n";
//		outputCode+="else sprintf((char*)bpcmd4,\"GZ001V0\\r\\n\");\n";
//		
//		outputCode+="bptmp=(bptmp>=0)?bptmp:-bptmp;"
//				+ "bptmp2=(bptmp2>=0)?bptmp2:-bptmp2;\n";
//		outputCode+="sprintf((char*)bpcmd,\"GZ100A%d\\r\\n\",bptmp);\n"
//				+ "sprintf((char*)bpcmd3,\"GZ101A%d\\r\\n\",bptmp2);\n";
		
		
		outputCode+="if("+this.getInputPortVariable(0)+">0&&((int)accTmp)>0) {sprintf((char*)bpcmd2,\"GZ000V200\\r\\n\");sprintf((char*)bpcmd,\"GZ100A%d\\r\\n\",(int)accTmp);\n}\n";
		outputCode+="else if("+this.getInputPortVariable(0)+"<0&&((int)accTmp)>0) {sprintf((char*)bpcmd2,\"GZ000V-200\\r\\n\");sprintf((char*)bpcmd,\"GZ100A%d\\r\\n\",(int)accTmp);}\n";
		outputCode+="else {sprintf((char*)bpcmd2,\"GZ000V0\\r\\n\");sprintf((char*)bpcmd,\"GZ100A0\\r\\n\");}\n";
		
		outputCode+="accTmp=fabs("+this.getInputPortVariable(1)+")+0.8;\n";
		outputCode+="if("+this.getInputPortVariable(1)+">0&&((int)accTmp)>0) {sprintf((char*)bpcmd4,\"GZ001V200\\r\\n\");sprintf((char*)bpcmd3,\"GZ101A%d\\r\\n\",(int)accTmp);\n}\n";
		outputCode+="else if("+this.getInputPortVariable(1)+"<0&&((int)accTmp)>0) {sprintf((char*)bpcmd4,\"GZ001V-200\\r\\n\");sprintf((char*)bpcmd3,\"GZ101A%d\\r\\n\",(int)accTmp);}\n";
		outputCode+="else {sprintf((char*)bpcmd4,\"GZ001V0\\r\\n\");sprintf((char*)bpcmd3,\"GZ101A0\\r\\n\");}\n";
		
		
		
//		
//		outputCode+="if(bptmp2<0) sprintf((char*)bpcmd4,\"GZ001V-200\\r\\n\");\n";
//		outputCode+="else if(bptmp2>0) sprintf((char*)bpcmd4,\"GZ001V200\\r\\n\");\n";
//		outputCode+="else sprintf((char*)bpcmd4,\"GZ001V0\\r\\n\");\n";
//		
//		outputCode+="bptmp=(bptmp>=0)?bptmp:-bptmp;"
//				+ "bptmp2=(bptmp2>=0)?bptmp2:-bptmp2;\n";
//		outputCode+="sprintf((char*)bpcmd,\"GZ100A%d\\r\\n\",bptmp);\n"
//				+ "sprintf((char*)bpcmd3,\"GZ101A%d\\r\\n\",bptmp2);\n";
		
		
		outputCode+="BPSUSTbool=!BPSUSTbool;\n";
		
		outputCode+="char headStrBP[]={\"POS\"};\n";
		
		outputCode+="maxfdBP=hCommBPSUST>BPfd?hCommBPSUST:BPfd;\n";
		
		outputCode+="	   FD_ZERO(&read_bpfds);\n"
				+ "		   FD_ZERO(&write_bpfds);"
				+ "        FD_SET(BPfd, &read_bpfds);\n"
				+ "		   FD_SET(hCommBPSUST, &read_bpfds);"
				+ "		   FD_SET(hCommBPSUST, &write_bpfds);\n"
				+ "			bptimeout.tv_sec = 0;\n"
				+ "        bptimeout.tv_usec = 10000;\n"
				+ "			int bpselectresult = select(maxfdBP + 1, &read_bpfds, &write_bpfds, NULL, &bptimeout);\n"
				+ "        if (bpselectresult == -1) {\n"
				+ "            perror(\"select\");\n"
				+ "        } else if (bpselectresult == 0) {\n"
				+ "            //printf(\"No input within 0.001 seconds.\\n\");\n"
				+ "        } else {\n"
				+ "				if(FD_ISSET(hCommBPSUST, &write_bpfds)){"
//				+ "					tcflush(hCommBPSUST,TCOFLUSH);\n"
				+ "					write(hCommBPSUST,BPposcmd,strlen(BPposcmd));"
//				+ "					if(BPSUSTbool==true){"
//				+ "						if(bptmp!=0) write(hCommBPSUST,bpcmd,strlen(bpcmd));"
//				+ "						usleep(4000);"
//				+ "						write(hCommBPSUST,bpcmd2,strlen(bpcmd2));"
//				+ "					}"
//				+ "					else{"
//				+ "						if(bptmp2!=0) write(hCommBPSUST,bpcmd3,strlen(bpcmd3));"
//				+ "						usleep(4000);"
//				+ "						write(hCommBPSUST,bpcmd4,sizeof(bpcmd4));"
//				+ "					}"
				+ "					usleep(4000);"
//				+ "					if(bptmp!=0) "
				+ "					write(hCommBPSUST,bpcmd,strlen(bpcmd));"
//				+ "					if(bptmp2!=0) "
				+ "					write(hCommBPSUST,bpcmd3,strlen(bpcmd3));"
				+ "					usleep(2200);"
				+ "					write(hCommBPSUST,bpcmd2,strlen(bpcmd2));"
				+ "					write(hCommBPSUST,bpcmd4,strlen(bpcmd4));"
				+ ""
				+ "				}"
				+ "				if(FD_ISSET(hCommBPSUST, &read_bpfds)){\n"
				+ "					Serialport_Recv(hCommBPSUST,recvBuffBPSUST,62);\n"
				+ "					printf(\"recvBuffBPSUST=%s\\n\",recvBuffBPSUST);\n"
				+ "					char *keyStartBP = strstr(recvBuffBPSUST,headStrBP);\n"
				+ "					if(keyStartBP!=NULL){\n"
//				+ "						tcflush(hCommBPSUST,TCIFLUSH);\n"
				+ "						keyStartBP+=3;\n"
				+ "						int BPpostmp = strtol(keyStartBP,&keyStartBP,10);\n"
				+ "						BPpos1 = strtol(keyStartBP+1,&keyStartBP,10);\n"
				+ "						BPpos2 = strtol(keyStartBP+1,NULL,10);\n"
				+ "					}\n"
				+ ""
				+ "				}\n"
				+ "				if (FD_ISSET(BPfd, &read_bpfds)) {\n"
				+"					while(read(BPfd, &ev, sizeof(struct input_event)) == sizeof(struct input_event)) {\n"
				+ "						if(ev.type==EV_ABS)\n"
				+ "						{\n"
				+ "							if(ev.code==ABS_X)\n"
				+ "								t_x=ev.value;\n"
				+ "							if(ev.code==ABS_Y)\n"
				+ "								t_y=ev.value;\n"
				+ "							//printf(\"(%d,%d)\\n\",t_x,t_y);\n"
				+ "						}\n"
				+ "					}"
				+ "				}"
				+ "}\n";
		
//		outputCode+="tcflush(hCommBPSUST,TCIOFLUSH);\n";
//				+ "tcflush(BPfd,TCIOFLUSH);\n";
		
		
		
//		outputCode+="char sendBuffBPSUST[]={\"GZ999POS*=?\\r\\n\"};\n";
//		outputCode+="if(BPSUSTbool==false)Serialport_Send(hCommBPSUST,sendBuffBPSUST,strlen(sendBuffBPSUST));\n";	


		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=(-t_x+16088)/80.4;\n";
		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=(t_y-15968)/107.47	;\n";
		outputCode+=outputPortList.get(2).getOutputSignalC().getName()+"=BPpos1;\n";
		outputCode+=outputPortList.get(3).getOutputSignalC().getName()+"=BPpos2;\n";

		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		statementCode +="#define TOUCHPAD_NAME \"Name=\\\"Touch p303\\\"\"\n";
		statementCode +="int hCommBPSUST;\n";
		statementCode +="int BPfd;\n";
		statementCode +="bool BPSUSTbool=true;\n";
		statementCode +="struct input_event ev;\n";
		statementCode +="int t_x,t_y;\n"
				+ "int BPpos1=0,BPpos2=0;\n";	
		statementCode +="fd_set read_bpfds,write_bpfds;\n"
				+ "struct timeval bptimeout;\n"
				+ "int maxfdBP=0;\n";
		code.addStatementCode(statementCode);
	}
}
