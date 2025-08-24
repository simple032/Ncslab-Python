package com.ncslab.block.sink;

import com.ncslab.ncslablink.MatDimException;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.sink.TerminatorDto;
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
	 * Specialized DTO Constructor - Creates Terminator block from TerminatorDto
	 * This constructor provides type-safe access to Terminator-specific parameters
	 */
	public Terminator(TerminatorDto dto, NCSLabModel model) {
		super(dto, model);
		
		// Initialize single input port
		inputPortList.add(new InputPort(this, 1));
		
		// Note: Terminator blocks are simple sink blocks that just terminate signals
		// No additional parameters needed since they have no outputs
		
		System.out.println("DTO-SPECIALIZED: Terminator block created from TerminatorDto - " + dto.getBlockName() );
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
