package com.ncslab.block.hardware.stm32;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.hardware.stm32.DACDto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.List;

public class DAC extends com.ncslab.block.Block{

	Parameter index;
	Parameter channel;

    
    
    /**
     * DTO-NATIVE Constructor - Creates DAC block directly from BlockDto DTO
     */
    public DAC(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: DAC block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        inputNames.add("in1");
        
        PARAMETER_DEFAULTS.put("DACForStm32Index", "1");
        PARAMETER_DEFAULTS.put("DACForStm32Channel", "0");
    }
	public DAC(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		inputPortList.add(new InputPort(this,1));

		index=new Parameter(this,1,"index",paramValues.getString("DACForStm32Index"));
		channel=new Parameter(this,2,"channel",paramValues.getString("DACForStm32Channel"));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";

//		initCode+=channel.getName()+"="+paramValues.getInt("PWMForStm32Channel")+";\n";
//		initCode+="TIM3_PWM_Init(2*1000000*STEP_SIZE-1,100-1,"+paramValues.getInt("PWMForStm32Channel")+");";

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

		context.put("block", this);
	
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/stm32/DAC/init.vm", context));
		// Removed unused initCode reference

	}

	public void generateOutputCodeC(CodeStructC code) {
		// Populate all standard template variables first
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/stm32/DAC/output.vm", context));
	}
}
