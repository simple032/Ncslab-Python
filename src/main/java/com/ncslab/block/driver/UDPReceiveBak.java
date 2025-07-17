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

public class UDPReceiveBak extends com.ncslab.block.Block{

	private String name = "UDPReceive";

	Parameter LocalIPPort;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("LocalIPPort", "8080");
    }

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    static {

        outputNames.add("out1");
        parameterNames.add("LocalIPPort");
    }
	public UDPReceiveBak(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����
		outputPortList.add(new OutputPort(this,1,false));
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
		context.put("localIPPort", paramValues.getInt("LocalIPPort"));
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceiveBak/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("name", name);
		context.put("outputSignal", outputPortList.get(0).getOutputSignalC().getName());
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceiveBak/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateTerminateCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("name", name);
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceiveBak/terminate.vm", context);
		code.addTerminateCode(codeStr);
	}

	public void generateStatementCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("name", name);
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceiveBak/statement.vm", context);
		code.addStatementCode(codeStr);
	}
}
