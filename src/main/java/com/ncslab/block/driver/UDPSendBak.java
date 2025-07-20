package com.ncslab.block.driver;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class UDPSendBak extends com.ncslab.block.Block{

	private String name = "UDPSend";

	Parameter RemoteIPAddress;
	Parameter RemoteIPPort;
	Parameter LocalIPPort;


    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("RemoteIPAddress", "127.0.0.1");
        PARAMETER_DEFAULTS.put("RemoteIPPort", "8081");
        PARAMETER_DEFAULTS.put("LocalIPPort", "8080");
    }
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        inputNames.add("in1");
    }
	public UDPSendBak(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����
		inputPortList.add(new InputPort(this,1));

		RemoteIPAddress=new Parameter(this,parameterList.size()+1,"RemoteIPAddress",paramValues.getString("RemoteIPAddress"));
		RemoteIPPort=new Parameter(this,parameterList.size()+1,"RemoteIPPort",paramValues.getString("RemoteIPPort"));
		LocalIPPort=new Parameter(this,parameterList.size()+1,"LocalIPPort",paramValues.getString("LocalIPPort"));

	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";

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

		context.put("block", this);
		context.put("remoteIPAddress", paramValues.getString("RemoteIPAddress"));
		context.put("remoteIPPort", paramValues.getInt("RemoteIPPort"));
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPSendBak/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("name", name);
		context.put("inputSignal", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPSendBak/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateTerminateCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("name", name);
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPSendBak/terminate.vm", context);
		code.addTerminateCode(codeStr);
	}

	public void generateStatementCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("name", name);
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPSendBak/statement.vm", context);
		code.addStatementCode(codeStr);
	}
}
