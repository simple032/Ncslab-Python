package com.ncslab.block.hardware.stm32;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.comm.UDPSenderDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class UDPSender extends com.ncslab.block.Block{

	Parameter remoteIp,remotePort;

    
    
    /**
     * DTO-NATIVE Constructor - Creates UDPSender block directly from BlockDto DTO
     */
    public UDPSender(UDPSenderDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: UDPSender block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("remoteIp", "192.168.1.100");
        PARAMETER_DEFAULTS.put("remotePort", "8081");
        PARAMETER_DEFAULTS.put("UdpSenderRemoteIpForStm32", "192.168.1.100");
        PARAMETER_DEFAULTS.put("UdpSenderRemotePortForStm32", "8081");

    }

    public static final List<String> inputNames = new ArrayList<>();

    static {
        inputNames.add("in1");
    }
	public UDPSender(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		inputPortList.add(new InputPort(this,1));

		remoteIp=new Parameter(this,1,"remoteIp",paramValues.getString("UdpSenderRemoteIpForStm32"));
		remotePort=new Parameter(this,2,"remotePort",paramValues.getString("UdpSenderRemotePortForStm32"));
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

		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		// Add computed signal name
		context.put("inputSignal", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());

		code.addInitCode(TemplateManager.renderTemplate("c/hardware/stm32/UDPSender/init.vm", context));
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		// Add computed signal name
		context.put("inputSignal", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());

		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/stm32/UDPSender/output.vm", context));
	}
}
