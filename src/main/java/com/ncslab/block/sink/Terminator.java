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

    // Port defaults for centralized initialization (sink block has no outputs)
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        inputNames.add("in1");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (terminator has no outputs)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
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

    public void generateOutputCodeC(com.ncslab.code.c.CodeStructC code) {
        super.generateOutputCodeC(code);
        
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add Terminator-specific context variables
        context.put("block", this);
        
        // Since Terminator is a sink block with no outputs, provide empty outputSignal
        context.put("outputSignal", "");
        context.put("outputSignalName", "");
        
        // Terminator blocks don't generate any C code - they just terminate signals
        // No template rendering needed since terminators don't produce output
    }

}
