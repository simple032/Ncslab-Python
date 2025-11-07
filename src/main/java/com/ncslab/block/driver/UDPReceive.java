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
    public UDPReceive(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        initializePorts();
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
	
	private void initializePorts() {
        outputPortList.add(new OutputPort(this,1,false));
        LocalIPPort = getParameterByName("LocalIPPort");
    }
	
	@Deprecated
	public UDPReceive(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

        initializePorts();

        // Safe parameter access with null checks
        String ipPort = "";
        if (paramValues != null) {
            if(paramValues.has("LocalIPPort")) {
                ipPort = paramValues.getString("LocalIPPort");
            } else {
                ipPort = paramValues.getString("address") + ":" + paramValues.getInt("port");
            }
        } else {
            // Use default if paramValues is null
            ipPort = "127.0.0.1:8080";
        }
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
		// Safe parameter access with null checks
		int localIPPort = 8080; // default value
		if (paramValues != null && paramValues.has("LocalIPPort")) {
		    localIPPort = paramValues.getInt("LocalIPPort");
		} else if (LocalIPPort != null) {
		    // Fallback to Parameter object
		    localIPPort = (int) LocalIPPort.getData().getInitValue();
		}
		context.put("localIPPort", localIPPort);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/UDPReceive/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		super.generateOutputCodeC(code);

		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

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
