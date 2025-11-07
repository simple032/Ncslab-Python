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
    public GPIO(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: GPIO block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();
    
    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        inputNames.add("in1");
        
        PARAMETER_DEFAULTS.put("Bcm", "18");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (GPIO is output-only hardware block)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
    }
	public GPIO(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		// Port initialization is now handled by the centralized parseInputOutputPorts() method in parent constructor

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
		
		// Populate all standard template variables first
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		
		context.put("block", this);
		context.put("Bcm",  Bcm.getData().getIntValue());
		context.put("bcmParameterName", Bcm.getName());
		context.put("paramName", Bcm.getName());
		context.put("BCM", Bcm.getData().getIntValue());
		
		// GPIO-specific ADC output variables
		for (int i = 1; i <= 8; i++) {
			context.put("outputAD" + i, "outputAD" + i);
			context.put("AD" + i, i - 1);
		}
		
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/rasp/GPIO/init.vm", context));
	}

	public void generateOutputCodeC(CodeStructC code) {
		// Populate all standard template variables first
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		
		context.put("block", this);
		context.put("Bcm",  Bcm.getData().getIntValue());
		context.put("bcmParameterName", Bcm.getName());
		context.put("paramName", Bcm.getName());
		context.put("BCM", Bcm.getData().getIntValue());
		context.put("inputSignal",  inputPortList.get(0) .getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
		
		// GPIO-specific ADC output variables (commonly used in GPIO templates)
		for (int i = 1; i <= 8; i++) {
			context.put("outputAD" + i, "outputAD" + i);
			context.put("AD" + i, i - 1); // Zero-based channel indexing
		}
		
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/rasp/GPIO/output.vm", context));
	}

    @Override
    public void calculateOutput(double t) {
        // GPIO block typically controls digital output pins
        // This is a sink block - it takes input and controls hardware GPIO pin
        // For simulation purposes, we don't produce output signals
        
        InputPort input = inputPortList.get(0);
        
        if (input.getData() != null) {
            double inputValue = input.getData().getInitValue();
            int gpioPin = Bcm.getData().getIntValue();
            
            // For real hardware, this would control the actual GPIO pin
            // For simulation, we can log the GPIO state
            boolean gpioState = inputValue > 0.5; // Threshold for digital output
            
            // Log GPIO state for debugging (in real hardware this would set the pin)
            System.out.printf("GPIO Pin %d: %s (input: %.3f)%n", gpioPin, 
                gpioState ? "HIGH" : "LOW", inputValue);
        }
    }

    @Override
    public void calculateInit() {
        // Initialize GPIO block
        // For real hardware, this would configure the GPIO pin as output
        int gpioPin = Bcm.getData().getIntValue();
        
        // Log initialization (in real hardware this would configure the pin)
        System.out.printf("GPIO Pin %d initialized as output%n", gpioPin);
    }
}
