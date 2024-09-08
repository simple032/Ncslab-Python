package com.ncslab.block.driver;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class UDPReceiveBak extends com.ncslab.block.Block{

	private String name = "UDPReceive";

	Parameter LocalIPPort;
	public UDPReceiveBak(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����
		outputPortList.add(new OutputPort(this,1,false));


		LocalIPPort=new Parameter(this,parameterList.size()+1,"LocalIPPort",paramValues.getString("LocalIPPort"));
		parameterList.add(LocalIPPort);

	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="/*Code for initialization of block UDPReceive:("+getBlockId()+")"+getBlockName()+"*/\n";

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

		String initCode="/*Code for initialization of block UDPReceive:("+getBlockId()+")"+getBlockName()+"*/\n";

		//udp receive
		initCode+="sockfd_netrecv"+getBlockId()+" = socket(AF_INET, SOCK_DGRAM|SOCK_NONBLOCK,0);\n"
				+ "if(sockfd_netrecv"+getBlockId()+"==-1){printf(\"can not create socket\\n\"); close(sockfd_netrecv"+getBlockId()+");} \r\n"
				+ "adr_serv_netrecv"+getBlockId()+".sin_family = AF_INET; \r\n"
				+ "adr_serv_netrecv"+getBlockId()+".sin_port =htons("+paramValues.getInt("LocalIPPort")+"); \r\n"
				+ "adr_serv_netrecv"+getBlockId()+".sin_addr.s_addr=INADDR_ANY; \r\n"
				+ "bind(sockfd_netrecv"+getBlockId()+",(struct sockaddr*)&adr_serv_netrecv"+getBlockId()+",sizeof(adr_serv_netrecv"+getBlockId()+"));\r\n";
				;

//		initCode+="sockfd = socket(AF_INET, SOCK_DGRAM|SOCK_NONBLOCK, 0);\n"
//				 + "if(sockfd==-1){printf(\"can not create socket\\n\"); close(sockfd);} \r\n"
//				 + "sock_addr.sin_family = AF_INET; \r\n"
//				 + "sock_addr.sin_port =htons("+paramValues.getInt("LocalIPPort")+"); \r\n"
//				 + "sock_addr.sin_addr.s_addr=INADDR_ANY; \r\n"
//				 + "bind(sockfd,(struct sockaddr*)&sock_addr,sizeof(sock_addr));\r\n";


		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block UDPReceive:("+getBlockId()+")"+getBlockName()+"*/\n";


		outputCode+=
					"int recvbuf = 0;\n"
					+"socklen_t len = sizeof(adr_serv_netrecv"+getBlockId()+");\r\n"
				  +"union data_union data_union_recv"+getBlockId()+";"
				  +"if(model.majorStep==1){\n"
				  +"recvfrom(sockfd_netrecv"+getBlockId()+", &recvbuf, sizeof(recvbuf), 0,(struct sockaddr*)&adr_serv_netrecv"+getBlockId()+",&len);\n"
				  + "data_union_recv"+getBlockId()+".c[3] = recvbuf >> 24;\r\n"
				  + "data_union_recv"+getBlockId()+".c[2] = recvbuf >> 16;\r\n"
				  + "data_union_recv"+getBlockId()+".c[1] = recvbuf >> 8;\r\n"
				  + "data_union_recv"+getBlockId()+".c[0] = recvbuf;\r\n"
				  +outputPortList.get(0).getOutputSignalC().getName()+"=data_union_recv"+getBlockId()+".v;\r\n"
//				  +outputPortList.get(0).getOutputSignalC().getName()+"="+"ADS1256_GetChannalValue("+channel.getName()+")*5.0/0x7fffff;\n"
				  +"}\n";

		code.addOutputCode(outputCode);
	}

	public void generateTerminateCodeC(CodeStructC code) {
		String terminateCode="/*Code for output of block UDPReceive:("+getBlockId()+")"+getBlockName()+"*/\n";


		terminateCode+="close(sockfd_netrecv"+getBlockId()+");\n";

		code.addTerminateCode(terminateCode);
	}

	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		statementCode += "static struct sockaddr_in adr_serv_netrecv"+getBlockId()+";\n";
		statementCode += "static int_T sockfd_netrecv"+getBlockId()+" ;//sockfd_netrecv;\n";

		code.addStatementCode(statementCode);
	}
}
