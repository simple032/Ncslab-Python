package block.driver;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class UDPSendBak extends block.Block{
	
	private String name = "UDPSend";
	
	Parameter RemoteIPAddress;
	Parameter RemoteIPPort;
	Parameter LocalIPPort;
	public UDPSendBak(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ�����
		inputPortList.add(new InputPort(this,1));
		
		RemoteIPAddress=new Parameter(this,parameterList.size()+1,"RemoteIPAddress",paramValues.getString("RemoteIPAddress"));
		parameterList.add(RemoteIPAddress);
		RemoteIPPort=new Parameter(this,parameterList.size()+1,"RemoteIPPort",paramValues.getString("RemoteIPPort"));
		parameterList.add(RemoteIPPort);
		LocalIPPort=new Parameter(this,parameterList.size()+1,"LocalIPPort",paramValues.getString("LocalIPPort"));
		parameterList.add(LocalIPPort);
		
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="/*Code for initialization of block UDPSend:("+getBlockId()+")"+getBlockName()+"*/\n";
		
//		initCode+=channel.getName()+"="+paramValues.getDouble("Channel")+";\n"; 
//		
//		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
				
//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+channel.getName()+";\n";
		
//		code.addOutputCode(outputCode);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block UDPSend:("+getBlockId()+")"+getBlockName()+"*/\n";
		//udp send
		initCode+="sockfd_netsend"+getBlockId()+" = socket(AF_INET, SOCK_DGRAM,0);\n"
				+ "if(sockfd_netsend"+getBlockId()+"==-1){printf(\"can not create socket\\n\"); close(sockfd_netsend"+getBlockId()+");} \r\n"
				+ "adr_serv_netsend"+getBlockId()+".sin_family = AF_INET; \r\n"
				+ "adr_serv_netsend"+getBlockId()+".sin_port =htons("+paramValues.getInt("RemoteIPPort")+"); \r\n"
				+ "adr_serv_netsend"+getBlockId()+".sin_addr.s_addr=inet_addr(\""+paramValues.getString("RemoteIPAddress")+"\"); \r\n"
				+ "int ret =connect(sockfd_netsend"+getBlockId()+",(struct sockaddr*)&adr_serv_netsend"+getBlockId()+",sizeof(adr_serv_netsend"+getBlockId()+"));\r\n"
				+ "if(0>ret){printf(\"connect error\\n\");close(sockfd_netsend"+getBlockId()+");}\r\n";
		
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block UDPSend:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		
		outputCode+="union data_union data_union_send"+getBlockId()+";"
				  +"if(model.majorStep==1){\n"
				  +"data_union_send"+getBlockId()+".v = "+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n"
				  +"sendto(sockfd_netsend"+getBlockId()+",&data_union_send"+getBlockId()+",sizeof(data_union_send"+getBlockId()+"),0,(struct sockaddr*)&adr_serv_netsend"+getBlockId()+",sizeof(adr_serv_netsend"+getBlockId()+"));\n"
//				  +outputPortList.get(0).getOutputSignalC().getName()+"="+"ADS1256_GetChannalValue("+channel.getName()+")*5.0/0x7fffff;\n"
				  +"}\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateTerminateCodeC(CodeStructC code) {
		String terminateCode="/*Code for output of block UDPSend:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		
		terminateCode+="close(sockfd_netsend"+getBlockId()+");\n";
		
		code.addTerminateCode(terminateCode);
	}
	
	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		statementCode += "static struct sockaddr_in adr_serv_netsend"+getBlockId()+";\n";
		statementCode += "static int_T sockfd_netsend"+getBlockId()+" ;//sockfd_netrecv;\n";		
		code.addStatementCode(statementCode);
	}
}