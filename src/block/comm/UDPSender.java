package block.comm;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class UDPSender extends Block {
	
	private String name = "UDPSender";
	
	private String addr;
	private int port;
	
	public UDPSender(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		//outputPortList.add(new OutputPort(this,"Water_Level",2,false));

		//pumpState=new State(this,1,"pumpState");
		//stateList.add(pumpState);
		//levelState=new State(this,2,"levelState");
		//stateList.add(levelState);
		paraseParamValues();
	}
	
	private void paraseParamValues() {
		this.addr = paramValues.getString("RemoteAddr");		
		this.port = paramValues.getInt("RemotePort");
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
		initCode+="{\n";
		initCode+="int buflen,status;\n";			
		initCode+="int  len,sock_buf_size=1024;\n";    
		initCode+="char buffer[] = \"" + this.addr + "\";\n";
		initCode+="int port = " + this.port + ";\n";
		initCode+="adr_serv_netsend"+getBlockId()+".sin_family = AF_INET;\n";
		initCode+="adr_serv_netsend"+getBlockId()+".sin_port = htons(port);\n";
		initCode+="status=inet_aton(buffer,&adr_serv_netsend"+getBlockId()+".sin_addr);\n";
		initCode+="if(status != 1)\n{\nprintf(\"Wrong IP Address.\\n\");\n}\n";
		initCode+="bzero(&(adr_serv_netsend"+getBlockId()+".sin_zero), 8);\n";		
		initCode+="sockfd_netsend"+getBlockId()+"[0] = socket(AF_INET, SOCK_DGRAM, 0);\n";
		initCode+="if (sockfd_netsend"+getBlockId()+"[0] == -1)\n"
				+"{\n"+"printf(\"Error in socket.\\n\");\nexit(-1);"+"\n}\n";		
		initCode+="len=sizeof(sock_buf_size);\n";
		initCode+="setsockopt( sockfd_netsend"+getBlockId()+"[0], SOL_SOCKET, SO_SNDBUF,(char *)&sock_buf_size, len);\n";
		initCode+="setsockopt( sockfd_netsend"+getBlockId()+"[0], SOL_SOCKET, SO_RCVBUF,(char *)&sock_buf_size, len);\n";
		initCode+="}\n";
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
		
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		int bufLen = 255;	
	
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
		derivativeCode += "{\n";
		derivativeCode+="real_T u0 = "+this.getInputPortVariable(0) +";\n";
		derivativeCode+="real_T u1 = "+this.getInputPortVariable(1) +";\n";
		
		derivativeCode+="if(sfcnIsMajorStep()){\n"
		    +"bzero(&(adr_serv_netsend"+getBlockId()+".sin_zero), 8);\n"
		    +"unsigned char msg["+bufLen+"];\n"
		    +"memcpy(msg, &u0, sizeof(u0));\n"    
		    +"memcpy(msg+sizeof(u0), &u1, sizeof(u1));\n"    
		    +"int sendnum = sizeof(real_T)*"+inputPortList.size()+";\n"
			+"int status = sendto(sockfd_netsend"+getBlockId()+"[0],msg, sendnum, 0,"
			                 +"(struct sockaddr *)&adr_serv_netsend"+getBlockId()+","
			                 +"sizeof(adr_serv_netsend"+getBlockId()+"));\n"
			+"if(status == -1)\n"
			+"printf(\"Error in sendto.\\n\");\n"
			+"}\n";
		derivativeCode += "}\n";
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		statementCode += "static struct sockaddr_in adr_serv_netsend"+getBlockId()+";\n";
		statementCode += "static int_T sockfd_netsend"+getBlockId()+"[1] ;//sockfd_netrecv;\n";		
		code.addStatementCode(statementCode);
	}
}
