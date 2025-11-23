package com.ncslab.block.elect;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.elect.LimitingDto;

import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.Data;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Limiting extends Block{
	protected Parameter rmin;

    
    
    
    /**
     * DTO-NATIVE Constructor - Creates Limiting block directly from BlockDto DTO
     */
    public Limiting(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Limiting block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
        
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Rmin", "0.001"); // Minimum resistance in ohms
    }
	public Limiting(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		InputPort in;
		OutputPort out;
		out = new OutputPort(this,1,true);
		in = new InputPort(this,1);

		outputPortList.add(out);
		inputPortList.add(in);
		rmin=getParameterByName("Rmin");
		System.out.println(paramValues);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		context.put("block", this);
		
		String initCode = TemplateManager.renderTemplate("c/elect/Limiting/init.vm", context);
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
	    context.put("block", this);

	    String outputCode = TemplateManager.renderTemplate("c/elect/Limiting/output.vm", context);
	    code.addOutputCode(outputCode);
	}

	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		//switch(signal.getDataType()) {
		//case MATRIX:
			out.setWidth(signal.getWidth());
			out.setHeight(signal.getHeight());
			out.getOutputSignalC().setHeight(signal.getWidth());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
			//break;
		//}
	}

	/**
	 * Calculate output for Limiting block at the specified time.
	 *
	 * Implements minimum resistance limiting:
	 * - If input >= Rmin: output = input (pass through)
	 * - If input < Rmin: output = Rmin (limit to minimum)
	 *
	 * @param t Current simulation time
	 */
	@Override
	public void calculateOutput(double t) {
		// Validate required ports exist
		if (inputPortList == null || inputPortList.isEmpty()) {
			throw new IllegalStateException("Limiting block cannot calculate output: no input ports configured");
		}
		if (outputPortList == null || outputPortList.isEmpty()) {
			throw new IllegalStateException("Limiting block cannot calculate output: no output ports configured");
		}

		InputPort inputPort = inputPortList.get(0);
		OutputPort outputPort = outputPortList.get(0);

		// Validate input data
		if (inputPort == null || inputPort.getData() == null) {
			throw new IllegalStateException("Limiting block cannot calculate output: input data is null");
		}

		// Get parameter value
		double rminValue = rmin.getData().getInitValue();

		// Get input value
		double input = inputPort.getData().getInitValue();

		// Calculate output with minimum limiting
		double output;
		if (input >= rminValue) {
			// Pass through: output = input
			output = input;
		} else {
			// Limit to minimum: output = Rmin
			output = rminValue;
		}

		// Set output data
		Data outputData = new Data(output);
		outputPort.setData(outputData);
	}
}
