package com.ncslab.block.elect;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.elect.DiodeDto;

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

public class Diode extends Block {
	protected Parameter vf;
	protected Parameter ron;
	protected Parameter goff;

    
    
    
    /**
     * DTO-NATIVE Constructor - Creates Diode block directly from BlockDto DTO
     */
    public Diode(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Diode block created successfully - " + blockDto.getBlockName());
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

	public Diode(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);InputPort in;
		OutputPort out;

		out = new OutputPort(this,1,true);
		in = new InputPort(this,1);

		outputPortList.add(out);
		inputPortList.add(in);

		vf=getParameterByName("Vf");
		ron=getParameterByName("Ron");
		goff=getParameterByName("Goff");
		System.out.println(paramValues);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		context.put("block", this);

		String initCode = TemplateManager.renderTemplate("c/elect/Diode/init.vm", context);
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
	    context.put("block", this);

	    String outputCode = TemplateManager.renderTemplate("c/elect/Diode/output.vm", context);
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
	 * Calculate output for Diode block at the specified time.
	 *
	 * Implements diode voltage-to-current characteristic:
	 * - If voltage > Vf: current = (voltage - Vf) / Ron (forward conduction)
	 * - If 0 < voltage <= Vf: current = 0 (threshold region)
	 * - If voltage < 0: current = voltage * Goff (reverse leakage)
	 *
	 * @param t Current simulation time
	 */
	@Override
	public void calculateOutput(double t) {
		// Validate required ports exist
		if (inputPortList == null || inputPortList.isEmpty()) {
			throw new IllegalStateException("Diode block cannot calculate output: no input ports configured");
		}
		if (outputPortList == null || outputPortList.isEmpty()) {
			throw new IllegalStateException("Diode block cannot calculate output: no output ports configured");
		}

		InputPort inputPort = inputPortList.get(0);
		OutputPort outputPort = outputPortList.get(0);

		// Validate input data
		if (inputPort == null || inputPort.getData() == null) {
			throw new IllegalStateException("Diode block cannot calculate output: input data is null");
		}

		// Get parameter values
		double vfValue = vf.getData().getInitValue();
		double ronValue = ron.getData().getInitValue();
		double goffValue = goff.getData().getInitValue();

		// Get input voltage
		double voltage = inputPort.getData().getInitValue();

		// Calculate output current based on diode characteristic
		double current;
		if (voltage > vfValue) {
			// Forward conduction: current = (voltage - Vf) / Ron
			current = (voltage - vfValue) / ronValue;
		} else if (voltage > 0) {
			// Threshold region: no current flow
			current = 0.0;
		} else {
			// Reverse leakage: current = voltage * Goff
			current = voltage * goffValue;
		}

		// Set output data
		Data outputData = new Data(current);
		outputPort.setData(outputData);
	}
}
