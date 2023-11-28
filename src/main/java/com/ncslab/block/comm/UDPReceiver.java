package com.ncslab.block.comm;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class UDPReceiver extends Block {
	
	private String name = "UDPReceiver";
	
	private String addr;
	private int port;
	
	public UDPReceiver(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ�����룬�������
		
		outputPortList.add(new OutputPort(this,1,false));
		outputPortList.add(new OutputPort(this,2,false));

		//pumpState=new State(this,1,"pumpState");
		//stateList.add(pumpState);
		//levelState=new State(this,2,"levelState");
		//stateList.add(levelState);
		paraseParamValues();
	}
	
	private void paraseParamValues() {
		this.addr = paramValues.getString("RemoteAddr");		
		this.port = paramValues.getInt("LocalPort");
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
		
		initCode+="sockfd_netrecv"+getBlockId()+"[0] = socket(AF_INET, SOCK_DGRAM,0);\n";
		initCode+="if (sockfd_netrecv"+getBlockId()+"[0] == -1)\n"
		+"{\n	printf(\"socket function error!\");\n	exit(-1);\n		}\n"
		+"fcntl(sockfd_netrecv"+getBlockId()+"[0], F_SETFL, O_NONBLOCK);\n";	
		initCode+="adr_serv_netrecv"+getBlockId()+".sin_family = AF_INET;\n";
		initCode+="adr_serv_netrecv"+getBlockId()+".sin_port = htons(port);\n";
		initCode+="adr_serv_netrecv"+getBlockId()+".sin_addr.s_addr = htonl(INADDR_ANY);\n";

		initCode+="bzero(&(adr_serv_netrecv"+getBlockId()+".sin_zero), 8);\n";		
		initCode+="if (bind(sockfd_netrecv"+getBlockId()+"[0],(struct sockaddr *)&adr_serv_netrecv"+getBlockId()+", sizeof(adr_serv_netrecv"+getBlockId()+")) == -1)\n"
		    +"printf(\"bind error!\");\n";		
		initCode+="len=sizeof(sock_buf_size);\n";
		initCode+="setsockopt( sockfd_netrecv"+getBlockId()+"[0], SOL_SOCKET, SO_SNDBUF,(char *)&sock_buf_size, len);\n";
		initCode+="setsockopt( sockfd_netrecv"+getBlockId()+"[0], SOL_SOCKET, SO_RCVBUF,(char *)&sock_buf_size, len);\n";
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
		outputCode += "{\n";
	    outputCode += "int status, n;\n"; 
	    outputCode += "int recvnum=sizeof(real_T)*"+outputPortList.size()+";\n";
		outputCode += "char msg[255];\n" 
		+"int recvlength;\n"
	    +"int buflen;\n"  
	    +"fd_set readfds;\n"
	    +"struct timeval timeout ;\n"
	    +"struct  sockaddr_in their_addr, adr_send_netrecv;\n"    
	    +"timeout.tv_sec =  0;\n"
	    +"timeout.tv_usec = 0;\n"    
		+"recvlength=sizeof(struct sockaddr);\n"
		+"status=inet_aton(\""+this.addr+"\",&adr_send_netrecv.sin_addr);\n"
		+"if (status != 1)\n"
		+"{printf(\"Wrong IP Address.\\n\");}\n"
		+"bzero(&(adr_send_netrecv.sin_zero), 8);\n"
	    +"FD_ZERO(&readfds);\n"
	    +"FD_SET(sockfd_netrecv"+getBlockId()+"[0],&readfds);\n"
	    +"n = select(sockfd_netrecv"+getBlockId()+"[0]+1,&readfds,0,0,&timeout);\n"
	    +"if(n>0 && FD_ISSET(sockfd_netrecv"+getBlockId()+"[0],&readfds))\n"
        +"{\n"
        +"    if((status = recvfrom(sockfd_netrecv"+getBlockId()+"[0],msg,recvnum,0,(struct sockaddr *)&their_addr, &recvlength))>=0)\n"
        +"    {\n"   
        +"        if(their_addr.sin_addr.s_addr!=adr_send_netrecv.sin_addr.s_addr)\n"
        +"        {\n"
        +"            printf(\"ignore data from %s\\n\",inet_ntoa(their_addr.sin_addr));\n"
        +"            return;\n"
        +"}\n"               
        +"char* data = msg;\n"
        +"memcpy((char *)&"+outputPortList.get(0).getOutputSignalC().getName()+",data,sizeof(real_T));\n"
        +"data += sizeof(real_T);\n"
        +"memcpy((char *)&"+outputPortList.get(1).getOutputSignalC().getName()+",data,sizeof(real_T)); \n"
        +"printf(\"receive %d bytes data succesfully from %s !\\n\", status, inet_ntoa(their_addr.sin_addr));\n"      
        +"}else{\n"
        +"printf(\"error recvfrom!\\n\");\n"
        +"}\n"
        +"}\n";    
		outputCode += "}\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

	
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
	
	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		statementCode += "static struct sockaddr_in adr_serv_netrecv"+getBlockId()+";\n";
		statementCode += "static int_T sockfd_netrecv"+getBlockId()+"[1] ;//sockfd_netrecv;\n";		
		code.addStatementCode(statementCode);
	}
}
