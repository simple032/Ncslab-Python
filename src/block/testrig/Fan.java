package block.testrig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class Fan extends Block {
	private int copyNum;
	private String remote_addr;
	private String local_addr;
	private int remote_port;
	private int local_port;
	String hardwareDefineName;
	public Fan(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Speed",1,false));
		this.isHardware=true;
		copyNum=(int)Double.parseDouble(paramValues.getString("copyNum"));
		local_port=(int)Double.parseDouble(paramValues.getString("localPort"));
		remote_port=(int)Double.parseDouble(paramValues.getString("remotePort"));
		remote_addr=paramValues.getString("remoteAddress");
		local_addr=paramValues.getString("localAddress");
		switch(model.getModelMode()) {
		case Simulation:
			/*
			speedState=new State(this,1,"speedState");
			stateList.add(speedState);
			break;
			*/
		case Compilation:
			break;
		}
	}
	public String getHardwareDefineCodeC() {
		String hardwareDefineCode="";
		//add some head files
		hardwareDefineCode+="#include\"ncs_packet.h\"\n";
		hardwareDefineCode+="#include\"ncs_udp.h\"\n";
		hardwareDefineName="Block"+this.getBlockId()+"_Fan";
		hardwareDefineCode+="FAN "+hardwareDefineName+";\n";
		return hardwareDefineCode;
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Fan" + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			break;
		case Compilation:
			hardwareDefineName="Block"+this.getBlockId()+"_Fan";
			remote_addr=remote_addr.replace("'", "");
			local_addr=local_addr.replace("'", "");
			initCode+="char remote_addr[]=\""+remote_addr+"\";\n";
			initCode+="char local_addr[]=\""+local_addr+"\";\n";
			initCode+="initFan(&"+hardwareDefineName+","+copyNum+","+local_port+","+remote_port+",remote_addr,local_addr);\n";
			//initCode+="initFan(&"+hardwareDefineName+","+copyNum+","+local_port+","+remote_port+",\""+remote_addr+"\",\""+local_addr+"\");\n";
			break;
		}
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Fan:("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			break;
		case Compilation:
			hardwareDefineName="Block"+this.getBlockId()+"_Fan";
			outputCode+="if(mp->majorStep>0) {\n";
			outputCode+=hardwareDefineName+".fanCMD=1.0/2900*"+this.getInputPortVariable(0)+";\n";
			outputCode+=hardwareDefineName+".fanCMD="+hardwareDefineName+".fanCMD>1.0?1.0:"+hardwareDefineName+".fanCMD;\n";
			outputCode+=hardwareDefineName+".fanCMD="+hardwareDefineName+".fanCMD<0?0:"+hardwareDefineName+".fanCMD;\n";
			outputCode+=hardwareDefineName+".fanCMD="+hardwareDefineName+".fanCMD*1000;\n";
			outputCode+="outputFan(&"+hardwareDefineName+");\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+hardwareDefineName+".speed_rpm;\n";
			//outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+hardwareDefineName+".speed_rpm;\n";
			outputCode+="}\n";
			break;
		}
		
		code.addOutputCode(outputCode);
	}
	
}
