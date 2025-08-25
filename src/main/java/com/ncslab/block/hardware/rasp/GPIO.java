package com.ncslab.block.hardware.rasp;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.hardware.rasp.GPIODto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.hardware.HardwareBlock;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.List;

public class GPIO extends HardwareBlock{

	Parameter Bcm;

    
    
    /**
     * DTO-NATIVE Constructor - Creates GPIO block directly from BlockDto DTO
     */
    public GPIO(GPIODto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: GPIO block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        inputNames.add("in1");
        
        PARAMETER_DEFAULTS.put("Bcm", "18");
    }
	public GPIO(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//一个输入
		inputPortList.add(new InputPort(this,1));

		Bcm=new Parameter(this,1,"Bcm",paramValues.getString("Bcm"));
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
		context.put("block", this);
		context.put("Bcm",  Bcm.getData().getIntValue());
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/rasp/GPIO/init.vm", context));
	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("Bcm",  Bcm.getData().getIntValue());
		context.put("inputSignal",  inputPortList.get(0) .getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/rasp/GPIO/output.vm", context));
	}
}
