package com.ncslab.block.hardware.rasp;

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
import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class DA extends Block{

	Parameter channel;

    
    
    /**
     * DTO-NATIVE Constructor - Creates DA block directly from BlockJson DTO
     */
    public DA(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: DA block created successfully - " + blockDto.getBlockName());
    }


    public static final Vector<String> inputNames = new Vector<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        inputNames.add("in1");
        
        PARAMETER_DEFAULTS.put("Channel", "0");
    }
	public DA(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//一个输入
		inputPortList.add(new InputPort(this,1));

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
	
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/rasp/DA/init.vm", context));

		// Removed unused initCode reference
	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
	
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/rasp/DA/output.vm", context));
		// Removed unused outputCode reference
	}
}
