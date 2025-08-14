package com.ncslab.block.hardware.stm32;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class PWM extends com.ncslab.block.Block{

	Parameter channel;
	Parameter timx;
	Parameter frequency;
    
    
    /**
     * DTO-NATIVE Constructor - Creates PWM block directly from BlockJson DTO
     */
    public PWM(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: PWM block created successfully - " + blockDto.getBlockName());
    }


    public static final Vector<String> inputNames = new Vector<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        inputNames.add("in1");
        
        PARAMETER_DEFAULTS.put("PWMForStm32Channel", "1");
        PARAMETER_DEFAULTS.put("PWMForStm32TIM", "3");
        PARAMETER_DEFAULTS.put("PWMForStm32Frequency", "1000");
    }
	public PWM(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		inputPortList.add(new InputPort(this,1));

		channel=new Parameter(this,1,"channel",paramValues.getString("PWMForStm32Channel"));
		timx=new Parameter(this,2,"timx",paramValues.getString("PWMForStm32TIM"));
		frequency=new Parameter(this,3,"frequency",Integer.toString(paramValues.getInt("PWMForStm32Frequency")));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";

//		initCode+=channel.getName()+"="+paramValues.getInt("PWMForStm32Channel")+";\n";
//
//		initCode+="TIM3_PWM_Init(2*1000000*STEP_SIZE-1,100-1,"+paramValues.getInt("PWMForStm32Channel")+");";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+channel.getName()+";\n";

		// Removed unused outputCode reference
		 code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		// Add PWM-specific variables
		context.put("channel", channel.getData().getIntValue());
		context.put("timx", timx.getData().getIntValue());
		context.put("frequency", frequency.getData().getIntValue());

		code.addInitCode(TemplateManager.renderTemplate("c/hardware/stm32/PWM/init.vm", context));
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		// Add PWM-specific variables
		context.put("channel", channel.getData().getIntValue());
		context.put("timx", timx.getData().getIntValue());
		context.put("frequency", frequency.getData().getIntValue());
		// inputSignal is already populated by TemplateUtils.populatePortVariables()
		
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/stm32/PWM/output.vm", context));
	}
}
