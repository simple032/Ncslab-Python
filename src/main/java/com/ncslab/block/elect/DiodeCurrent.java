package com.ncslab.block.elect;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class DiodeCurrent extends Block {
	protected Parameter vf;
	protected Parameter ron;
	protected Parameter goff;

    
    
    
    /**
     * DTO-NATIVE Constructor - Creates DiodeCurrent block directly from BlockJson DTO
     */
    public DiodeCurrent(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: DiodeCurrent block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
        
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Vf", "0.7");    // Forward voltage in volts
        PARAMETER_DEFAULTS.put("Ron", "0.001"); // On-resistance in ohms
        PARAMETER_DEFAULTS.put("Goff", "1e-5"); // Off-conductance in siemens
    }

	public DiodeCurrent(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);InputPort in;
		OutputPort out;

		out = new OutputPort(this,1,true);
		in = new InputPort(this,1);

		outputPortList.add(out);
		inputPortList.add(in);

		vf=new Parameter(this,1,"Vf",paramValues.getString("Vf"));
		ron=new Parameter(this,2,"Ron",paramValues.getString("Ron"));
		goff=new Parameter(this,3,"Goff",paramValues.getString("Goff"));
		System.out.println(paramValues);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		context.put("block", this);

		String initCode = TemplateManager.renderTemplate("c/elect/DiodeCurrent/init.vm", context);
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
	    context.put("block", this);

	    String outputCode = TemplateManager.renderTemplate("c/elect/DiodeCurrent/output.vm", context);
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
}
