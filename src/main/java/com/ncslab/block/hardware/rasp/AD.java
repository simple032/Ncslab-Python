package com.ncslab.block.hardware.rasp;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.hardware.rasp.ADDto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.hardware.HardwareBlock;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.List;

public class AD extends HardwareBlock{

	Parameter channel;

    
    
    /**
     * DTO-NATIVE Constructor - Creates AD block directly from BlockDto DTO
     */
    public AD(ADDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: AD block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    
    static {
        outputNames.add("out1");
        
        PARAMETER_DEFAULTS.put("Channel", "0");
    }
	public AD(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//一个输出
		outputPortList.add(new OutputPort(this,"out",1,false));

		channel=new Parameter(this,1,"channel",paramValues.getString("Channel"));

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
	
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/rasp/AD/init.vm", context));
	}

	public void generateOutputCodeC(CodeStructC code) {
		// Populate all standard template variables first
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/rasp/AD/output.vm", context));
	}
}
