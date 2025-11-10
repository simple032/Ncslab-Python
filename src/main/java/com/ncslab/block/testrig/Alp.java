package com.ncslab.block.testrig;

import com.ncslab.util.TemplateManager;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.AlpDto;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class Alp extends Block {
	String hardwareDefineName;
	private double num[] = {0.1308,0,0};
	private double den[]= {1,3.091,1.19,0.2};
	private List<State> xStateList = new ArrayList<>();

	// Fan speed dynamics: First-order system with gain and time constant
	private static final double FAN_GAIN = 0.002;  // Fan speed gain (scales PWM to RPM-like units)
	private static final double FAN_TIME_CONSTANT = 0.5;  // Fan response time (seconds)
	private State fanSpeedState;  // State for fan speed dynamics

    
    
    /**
     * DTO-NATIVE Constructor - Creates Alp block directly from BlockDto DTO
     */
    public Alp(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        
        // Initialize ports and states same as JSON constructor
        this.isHardware = true;
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "FanSpeed", 1, false));
        outputPortList.add(new OutputPort(this, "Position", 2, false));
        
        // Initialize states based on model mode
        switch(model.getModelMode()) {
            case Simulation:
                // Position dynamics states
                for(int i = 0; i < 3; i++) {
                    State xState = new State(this, i + 1, "x" + (i + 1));
                    xStateList.add(xState);
                    stateList.add(xState);
                }
                // Fan speed state (separate dynamics)
                fanSpeedState = new State(this, 4, "fanSpeed");
                stateList.add(fanSpeedState);
                break;
            case Compilation:
                break;
        }
        
        System.out.println("DTO-NATIVE: Alp block created successfully - " + blockDto.getBlockName());
    }



    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("FanSpeed");
        outputNames.add("Position");
        inputNames.add("in1");
        
        // Alp block doesn't use configurable parameters - uses hardcoded transfer function coefficients
        // Empty defaults to satisfy Block.java reflection requirement
    }
	public Alp(JSONObject blockJSON,NCSLabModel model) {
        super(blockJSON,model);
		this.isHardware=true;
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"FanSpeed",1,false));
		outputPortList.add(new OutputPort(this,"Position",2,false));
		switch(model.getModelMode()) {
		case Simulation:
			// Position dynamics states
			for(int i=0;i<3;i++) {
				State xState=new State(this,i+1,"x"+(i+1));
				xStateList.add(xState);
				stateList.add(xState);
			}
			// Fan speed state (separate dynamics)
			fanSpeedState = new State(this, 4, "fanSpeed");
			stateList.add(fanSpeedState);
			break;
		case Compilation:
			break;
			}
	}
	public String getHardwareDefineCodeC() {
		hardwareDefineName="Block"+this.getBlockId()+"_Alp";
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		context.put("hardwareDefineName", hardwareDefineName);
		
		return TemplateManager.renderTemplate("c/testrig/Alp/hardware_define.vm", context);
	}
public void generateInitCodeC(CodeStructC code) {
    super.generateInitCodeC(code);

    com.ncslab.util.TemplateUtils.populateAllContext(context, this);

    String codeStr = TemplateManager.renderTemplate("c/testrig/Alp/init.vm", context);
    code.addInitCode(codeStr);
}
	public void generateOutputCodeC(CodeStructC code) {
	        context.put("block", this);
	        context.put("blockStateList", xStateList);
	        context.put("blockOutputPortVariables", Arrays.asList(getOutputPortVariables()));
	        context.put("modelMode", model.getModelMode().name());
	        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
	        context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));

	        // Ensure hardwareDefineName is set
	        if (hardwareDefineName == null) {
	            hardwareDefineName = "Block" + this.getBlockId() + "_Alp";
	        }
	        context.put("hardwareDefineName", hardwareDefineName);

	        String codeStr = TemplateManager.renderTemplate("c/testrig/Alp/output.vm", context);
	        code.addOutputCode(codeStr);
	    }
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of ALP" + ":("+getBlockId()+")"+getBlockName()+"*/\n";

		switch(model.getModelMode()) {
		case Simulation:
			// Fan speed dynamics: fanSpeed' = (FAN_GAIN * input - fanSpeed) / FAN_TIME_CONSTANT
			String inputVar = getInputPortVariable(0);
			if (inputVar != null) {
				derivativeCode += fanSpeedState.getDerivativeName() + " = (" + FAN_GAIN + " * " + inputVar
					+ " - " + fanSpeedState.getName() + ") / " + FAN_TIME_CONSTANT + ";\n";
			} else {
				derivativeCode += fanSpeedState.getDerivativeName() + " = -" + fanSpeedState.getName()
					+ " / " + FAN_TIME_CONSTANT + ";\n";
			}

			// Position dynamics: controller canonical form
			for(int i=0;i<xStateList.size()-1;i++) {
				derivativeCode+=xStateList.get(i).getDerivativeName()+"="
						+xStateList.get(i+1).getName()
						+";\n";
			}
			if (inputVar != null) {
				derivativeCode+=xStateList.get(xStateList.size()-1).getDerivativeName()+"=("+inputVar;
			} else {
				// Fallback for disconnected input port
				derivativeCode+=xStateList.get(xStateList.size()-1).getDerivativeName()+"=(0.0";
			}
			// Subtract den[1]*x1 + den[2]*x2 + den[3]*x3 (skip den[0]=1)
			for(int i = 0; i < xStateList.size(); i++) {
				derivativeCode+="-"+xStateList.get(i).getName()+"*"+den[i+1];
			}
			derivativeCode+=");\n";
		     break;
		case Compilation:
			break;
		}

		code.addDerivativeCode(derivativeCode);
	}

	@Override
	public void calculateInit() {
		// Initialize position dynamics states to zero
		for (State xState : xStateList) {
			xState.setData(new com.ncslab.block.data.Data(0.0));
		}

		// Initialize fan speed state to zero
		if (fanSpeedState != null) {
			fanSpeedState.setData(new com.ncslab.block.data.Data(0.0));
		}

		// Initialize output to zero
		if (outputPortList.size() >= 2) {
			outputPortList.get(0).setData(new com.ncslab.block.data.Data(0.0)); // FanSpeed
			outputPortList.get(1).setData(new com.ncslab.block.data.Data(0.0)); // Position
		}
	}

	@Override
	public void calculateOutput(double t) {
		if (xStateList.isEmpty() || outputPortList.size() < 2 || fanSpeedState == null) {
			return;
		}

		// Calculate position using transfer function: Position = num[0]*x1 + num[1]*x2 + num[2]*x3
		com.ncslab.block.data.Data position = new com.ncslab.block.data.Data(0.0);

		for (int i = 0; i < xStateList.size() && i < num.length; i++) {
			com.ncslab.block.data.Data stateContribution = xStateList.get(i).getData()
				.times(new com.ncslab.block.data.Data(num[i]));
			position = position.plus(stateContribution);
		}

		// FanSpeed: Use fan speed state (with dynamics, scaled by 2.0 like hardware)
		com.ncslab.block.data.Data fanSpeed = fanSpeedState.getData().times(new com.ncslab.block.data.Data(2.0));

		outputPortList.get(0).setData(fanSpeed);  // FanSpeed (from fan dynamics state)
		outputPortList.get(1).setData(position);  // Position (transfer function output)
	}

	@Override
	public void calculateDerivative(double t) {
		if (xStateList.isEmpty() || inputPortList.isEmpty() || fanSpeedState == null) {
			return;
		}

		// Get input value
		com.ncslab.block.data.Data inputData = inputPortList.get(0).getData();

		// Fan speed dynamics: first-order system fanSpeed' = (FAN_GAIN * input - fanSpeed) / FAN_TIME_CONSTANT
		com.ncslab.block.data.Data fanTarget = inputData.times(new com.ncslab.block.data.Data(FAN_GAIN));
		com.ncslab.block.data.Data fanDerivative = fanTarget.minus(fanSpeedState.getData())
			.divide(new com.ncslab.block.data.Data(FAN_TIME_CONSTANT));
		fanSpeedState.setDerivateData(fanDerivative);

		// Position dynamics: Controller canonical form state-space representation
		// x'1 = x2
		// x'2 = x3
		// x'3 = input - den[1]*x1 - den[2]*x2 - den[3]*x3

		// First n-1 states: x'i = x(i+1)
		for (int i = 0; i < xStateList.size() - 1; i++) {
			xStateList.get(i).setDerivateData(xStateList.get(i + 1).getData());
		}

		// Last state: x'n = input - sum(den[i+1] * x[i])
		// For den = [1, 3.091, 1.19, 0.2], we need: input - den[1]*x1 - den[2]*x2 - den[3]*x3
		com.ncslab.block.data.Data lastDerivative = inputData;

		// Subtract den coefficients times states (skip den[0] which is always 1)
		for (int i = 0; i < xStateList.size(); i++) {
			com.ncslab.block.data.Data term = xStateList.get(i).getData()
				.times(new com.ncslab.block.data.Data(den[i + 1]));
			lastDerivative = lastDerivative.minus(term);
		}

		xStateList.get(xStateList.size() - 1).setDerivateData(lastDerivative);
	}
}
