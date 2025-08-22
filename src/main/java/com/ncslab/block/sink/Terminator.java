package com.ncslab.block.sink;

import com.ncslab.ncslablink.MatDimException;
import com.ncslab.dto.core.BlockDto;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Terminator extends SinkBlock{

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    public static final List<String> inputNames = new ArrayList<>();

    static {
        inputNames.add("in1");
    }
	public Terminator(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);

		//һ������
		inputPortList.add(new InputPort(this,1));
	}
	
	/**
	 * DTO-NATIVE Constructor - Creates Terminator block directly from BlockDto DTO
	 */
	public Terminator(BlockDto blockDto, NCSLabModel model) {
		super(blockDto, model);
		inputPortList.add(new InputPort(this,1));
		System.out.println("DTO-NATIVE: Terminator block created successfully - " + blockDto.getBlockName());
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		
		context.put("block", this);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("m/sink/Terminator/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		code.addGlobalDefineCode("global "+getBlockName()+";\n");
		
		context.put("block", this);

		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("m/sink/Terminator/init.vm", context);
		code.addInitCode(codeStr);
	}

    public void checkDimension() throws MatDimException {
    }

}
