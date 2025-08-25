package com.ncslab.block.driver;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.driver.UDPSendDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class UDPSend extends com.ncslab.block.Block{

	private String name = "UDPSend";

	Parameter RemoteIPAddress;
	Parameter RemoteIPPort;
	Parameter LocalIPPort;

    
    
    /**
     * DTO-NATIVE Constructor - Creates UDPSend block directly from BlockDto DTO
     */
    public UDPSend(UDPSendDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: UDPSend block created successfully - " + blockDto.getBlockName());
    }



    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("RemoteIPAddress", "127.0.0.1");
        PARAMETER_DEFAULTS.put("RemoteIPPort", "8081");
        PARAMETER_DEFAULTS.put("LocalIPPort", "8080");
    }
    public static final List<String> inputNames = new ArrayList<>();

    static {

        inputNames.add("in1");
    }
	public UDPSend(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����
		inputPortList.add(new InputPort(this,1));

		RemoteIPAddress=getParameterByName("RemoteIPAddress");
		RemoteIPPort=getParameterByName("RemoteIPPort");
		LocalIPPort=getParameterByName("LocalIPPort");

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
		context.put("remoteIPPort", paramValues.getInt("RemoteIPPort"));
		context.put("remoteIPAddress", paramValues.getString("RemoteIPAddress"));

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPSend/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		super.generateOutputCodeC(code);

		context.put("block", this);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPSend/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateTerminateCodeC(CodeStructC code) {
		context.put("block", this);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPSend/terminate.vm", context);
		code.addTerminateCode(codeStr);
	}

	public void generateStatementCodeC(CodeStructC code) {
		context.put("block", this);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPSend/statement.vm", context);
		code.addStatementCode(codeStr);
	}
}
