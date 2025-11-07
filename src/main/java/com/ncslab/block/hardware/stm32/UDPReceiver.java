package com.ncslab.block.hardware.stm32;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.comm.UDPReceiverDto;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class UDPReceiver extends com.ncslab.block.Block{

	Parameter localPort;

    
    
    /**
     * DTO-NATIVE Constructor - Creates UDPReceiver block directly from BlockDto DTO
     */
    public UDPReceiver(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: UDPReceiver block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("localPort", "8080");
        PARAMETER_DEFAULTS.put("UdpReceiverLocalPortForStm32", "8080");

    }

    public static final List<String> outputNames = new ArrayList<>();
    static {
        outputNames.add("out1");
    }
	public UDPReceiver(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		outputPortList.add(new OutputPort(this,1,false));

		localPort=new Parameter(this,1,"localPort",paramValues.getString("UdpReceiverLocalPortForStm32"));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";

//		initCode+=channel.getName()+"="+paramValues.getInt("PWMForStm32Channel")+";\n";
//
//		initCode+="TIM3_PWM_Init(2*1000000*STEP_SIZE-1,100-1,"+paramValues.getInt("PWMForStm32Channel")+");";
//
		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+channel.getName()+";\n";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		// Add UDPReceiver-specific variables
		context.put("localPort", localPort.getData().getIntValue());
	
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/stm32/UDPReceiver/init.vm", context));
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		// Add UDPReceiver-specific variables
		context.put("outputSignal", getOutputPortList().get(0).getOutputSignalC().getName());
		context.put("localPort", localPort.getData().getIntValue());
		
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/stm32/UDPReceiver/output.vm", context));
	}
}
