package com.ncslab.block.hardware.stm32;

import lombok.Getter;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class ADC extends com.ncslab.block.Block{

	Parameter index;
	Parameter channel;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    
    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    
    static {
        parameterNames.add("index");
        parameterNames.add("channel");
        outputNames.add("out1");
        
        PARAMETER_DEFAULTS.put("ADCForStm32Index", "1");
        PARAMETER_DEFAULTS.put("ADCForStm32Channel", "0");
    }
	public ADC(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		outputPortList.add(new OutputPort(this,1,false));

		index=new Parameter(this,1,"index",paramValues.getString("ADCForStm32Index"));
		channel=new Parameter(this,2,"channel",paramValues.getString("ADCForStm32Channel"));
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

		context.put("block", this);
	
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/stm32/ADC/init.vm", context));
		// Removed unused initCode reference

	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
	
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/stm32/ADC/output.vm", context));

		// Removed unused outputCode reference
	}
}
