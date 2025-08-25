package com.ncslab.block.driver;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.driver.UDPReceiveDto;

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

public class UDPReceive extends com.ncslab.block.Block{

	private String name = "UDPReceive";

    
    
    /**
     * DTO-NATIVE Constructor - Creates UDPReceive block directly from BlockDto DTO
     */
    public UDPReceive(UDPReceiveDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: UDPReceive block created successfully - " + blockDto.getBlockName());
    }



    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("LocalIPPort", "8080");
        PARAMETER_DEFAULTS.put("address", "127.0.0.1");
        PARAMETER_DEFAULTS.put("port", "8080");
    }

    public static final List<String> outputNames = new ArrayList<>();
    static {

        outputNames.add("out1");
    }
	Parameter LocalIPPort;
	public UDPReceive(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����
		outputPortList.add(new OutputPort(this,1,false));

        String ipPort = "";
        if(paramValues.has("LocalIPPort")) ipPort = paramValues.getString("LocalIPPort");
        else{
            ipPort = paramValues.getString("address") + ":" + paramValues.getInt("port");
        }

        LocalIPPort=getParameterByName("LocalIPPort");

	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
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

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceive/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		super.generateOutputCodeC(code);

		context.put("block", this);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceive/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateTerminateCodeC(CodeStructC code) {
		context.put("block", this);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceive/terminate.vm", context);
		code.addTerminateCode(codeStr);
	}

	public void generateStatementCodeC(CodeStructC code) {
		context.put("block", this);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceive/statement.vm", context);
		code.addStatementCode(codeStr);
	}
}
