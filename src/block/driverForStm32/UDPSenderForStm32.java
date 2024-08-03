package block.driverForStm32;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class UDPSenderForStm32 extends block.Block{
	
	block.io.Parameter remoteIp,remotePort;
	public UDPSenderForStm32(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ������
		inputPortList.add(new InputPort(this,1));
		
		remoteIp=new Parameter(this,1,"remoteIp",paramValues.getString("UdpSenderRemoteIpForStm32"));
		parameterList.add(remoteIp);
		remotePort=new Parameter(this,2,"remotePort",paramValues.getString("UdpSenderRemotePortForStm32"));
		parameterList.add(remotePort);
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
			
		code.addOutputCode(outputCode);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initConfigCode="/*Code for initialization of block UDPSenderForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";
		
//		initConfigCode+=remoteIp.getName()+"=\""+paramValues.getString("UdpSenderRemoteIpForStm32")+"\";\n"; 
		initConfigCode+=remotePort.getName()+"="+paramValues.getInt("UdpSenderRemotePortForStm32")+";\n"; 
		
		initConfigCode+="UDP_Send_Config(\""+paramValues.getString("UdpSenderRemoteIpForStm32")+"\","+paramValues.getInt("UdpSenderRemotePortForStm32")+");";
		
		code.addInitConfigCode(initConfigCode);
		
		String initCode="UDP_Client_Init();\n";
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block UDPSenderForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+="if(model.majorStep==1){\n"
				+ "UDP_Send_double(\""+paramValues.getString("UdpSenderRemoteIpForStm32")+"\","+paramValues.getInt("UdpSenderRemotePortForStm32")+""
						+ ","+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+");\n"
				+ ""
				  +"}\n";
		
		code.addOutputCode(outputCode);
	}
}