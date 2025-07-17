package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * Pulse block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Amplitude: Pulse amplitude
 * - Period: Period of the pulse train
 * - PulseWidth: Width of the pulse (% of period or absolute time)
 * - PhaseDelay: Phase delay (time offset)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Pulse extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter amplitude;
    @Getter
    private final Parameter period;
    @Getter
    private final Parameter pulseWidth;
    @Getter
    private final Parameter phaseDelay;
    @Getter
    private final Parameter sampleTime;
    @Getter
    private final Parameter outDataType;
    @Getter
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Parameter Definitions ===
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Amplitude", "1");
        PARAMETER_DEFAULTS.put("Period", "1");
        PARAMETER_DEFAULTS.put("PulseWidth", "50");  // 50% duty cycle
        PARAMETER_DEFAULTS.put("PhaseDelay", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "0");   // Continuous
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        parameterNames.add("Amplitude");
        parameterNames.add("Period");
        parameterNames.add("PulseWidth");
        parameterNames.add("PhaseDelay");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
        // Port names
        outputNames.add("out1");
        // No input ports for pulse block
    }
    // === Private Constructor with Typed Parameters ===
    private Pulse(Parameter amplitude, Parameter period, Parameter pulseWidth, Parameter phaseDelay,
                 Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(amplitude, period, pulseWidth, sampleTime);
        
        // Assign parameters
        this.amplitude = Objects.requireNonNull(amplitude, "Amplitude parameter cannot be null");
        this.period = Objects.requireNonNull(period, "Period parameter cannot be null");
        this.pulseWidth = Objects.requireNonNull(pulseWidth, "Pulse width parameter cannot be null");
        this.phaseDelay = Objects.requireNonNull(phaseDelay, "Phase delay parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Pulse(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility
        this.amplitude = new Parameter(this, 1, "Amplitude", paramValues.getString("Amplitude"));
        this.period = new Parameter(this, 2, "Period", paramValues.getString("Period"));
        this.pulseWidth = new Parameter(this, 3, "PulseWidth", paramValues.getString("PulseWidth"));
        this.phaseDelay = new Parameter(this, 4, "PhaseDelay", paramValues.getString("PhaseDelay"));
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = new Parameter(this, 5, "SampleTime", "0"); // 0 for continuous pulse
        this.outDataType = new Parameter(this, 6, "OutDataTypeStr", "Inherit: Same as parameter");
        this.saturateOnIntegerOverflow = new Parameter(this, 7, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Pulse fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter amplitude = createAmplitudeFromJSON(paramValues, blockName);
            Parameter period = createPeriodFromJSON(paramValues, blockName);
            Parameter pulseWidth = createPulseWidthFromJSON(paramValues, blockName);
            Parameter phaseDelay = createPhaseDelayFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Pulse block = new Pulse(amplitude, period, pulseWidth, phaseDelay, sampleTime, outDataType, saturateParam,
                                   blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, amplitude, period, pulseWidth, phaseDelay, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Pulse block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Pulse create(String name, String path, double amplitude, double period, double pulseWidth, NCSLabModel model) {
        return create(name, path, amplitude, period, pulseWidth, 0.0, 0.0, "Inherit: Same as parameter", false, model);
    }
    
    public static Pulse create(String name, String path, double amplitude, double period, double pulseWidth,
                              double phaseDelay, double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter amplitudeParam = new Parameter(null, 1, "Amplitude", String.valueOf(amplitude));
        Parameter periodParam = new Parameter(null, 2, "Period", String.valueOf(period));
        Parameter pulseWidthParam = new Parameter(null, 3, "PulseWidth", String.valueOf(pulseWidth));
        Parameter phaseDelayParam = new Parameter(null, 4, "PhaseDelay", String.valueOf(phaseDelay));
        Parameter sampleTimeParam = new Parameter(null, 5, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 6, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 7, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Pulse block = new Pulse(amplitudeParam, periodParam, pulseWidthParam, phaseDelayParam, sampleTimeParam, outDataTypeParam, saturateParam,
                               name, path, "null", model);
        
        setParameterBlockReference(block, amplitudeParam, periodParam, pulseWidthParam, phaseDelayParam, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter amplitude, Parameter period, Parameter pulseWidth, Parameter sampleTime) {
        double amplitudeValue = amplitude.getDouble();
        if (Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
            throw new IllegalArgumentException("Amplitude must be finite");
        }
        
        double periodValue = period.getDouble();
        if (periodValue <= 0.0 || Double.isNaN(periodValue) || Double.isInfinite(periodValue)) {
            throw new IllegalArgumentException("Period must be positive and finite");
        }
        
        double pulseWidthValue = pulseWidth.getDouble();
        if (pulseWidthValue < 0.0 || Double.isNaN(pulseWidthValue) || Double.isInfinite(pulseWidthValue)) {
            throw new IllegalArgumentException("Pulse width must be non-negative and finite");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createAmplitudeFromJSON(JSONObject paramValues, String blockName) {
        String amplitudeValue = paramValues.optString("Amplitude", "1");
        return new Parameter(null, 1, "Amplitude", amplitudeValue);
    }
    
    private static Parameter createPeriodFromJSON(JSONObject paramValues, String blockName) {
        String periodValue = paramValues.optString("Period", "2");
        return new Parameter(null, 2, "Period", periodValue);
    }
    
    private static Parameter createPulseWidthFromJSON(JSONObject paramValues, String blockName) {
        String pulseWidthValue = paramValues.optString("PulseWidth", "50");
        return new Parameter(null, 3, "PulseWidth", pulseWidthValue);
    }
    
    private static Parameter createPhaseDelayFromJSON(JSONObject paramValues, String blockName) {
        String phaseDelayValue = paramValues.optString("PhaseDelay", "0");
        return new Parameter(null, 4, "PhaseDelay", phaseDelayValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 5, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as parameter");
        return new Parameter(null, 6, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 7, "SaturateOnIntegerOverflow", saturateValue);
    }
    
    // === Utility Methods ===
    private static String requireNonEmptyString(JSONObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing");
        }
        String value = json.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Field '" + key + "' cannot be empty");
        }
        return value;
    }
    
    private static void setParameterBlockReference(Pulse block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null, but should work for basic operations
            }
        }
    }
    
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Pulse");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        outputPortList.add(new OutputPort(this, 1, false));
        outputPortList.get(0).setHeight(amplitude.getHeight());
        outputPortList.get(0).setWidth(amplitude.getWidth());
    }

    // === Code Generation Methods (preserved from original) ===
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		context.put("block", this);
		context.put("amplitude", amplitude);
		context.put("period", period);
		context.put("pulseWidth", pulseWidth);
		context.put("phaseDelay", phaseDelay);
		
		String codeStr = TemplateManager.renderTemplate("m/source/Pulse/init.vm", context);
		code.addInitCode(codeStr);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		context.put("block", this);
		context.put("outputs", getOutputPortVariables());
		context.put("amplitude", amplitude);
		context.put("period", period);
		context.put("pulseWidth", pulseWidth);
		context.put("phaseDelay", phaseDelay);

		String codeStr = TemplateManager.renderTemplate("m/source/Pulse/output.vm", context);
		code.addOutputCode(codeStr);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		context.put("block", this);
		context.put("amplitude", amplitude);
		context.put("period", period);
		context.put("pulseWidth", pulseWidth);
		context.put("phaseDelay", phaseDelay);
		
		String codeStr = TemplateManager.renderTemplate("c/source/Pulse/init.vm", context);
		code.addInitCode(codeStr);
	}
	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
		context.put("outputs", getOutputPortVariables());
		context.put("amplitude", amplitude);
		context.put("period", period);
		context.put("pulseWidth", pulseWidth);
		context.put("phaseDelay", phaseDelay);

		String codeStr = TemplateManager.renderTemplate("c/source/Pulse/output.vm", context);
		code.addOutputCode(codeStr);
	}
	 public void updateDimension() throws MatDimException{
	    	if(amplitude.getWidth()!=period.getWidth()
	    			||amplitude.getWidth()!=pulseWidth.getWidth()
	    			||amplitude.getWidth()!=phaseDelay.getWidth()
	    			||amplitude.getHeight()!=period.getHeight()
	    			||amplitude.getHeight()!=pulseWidth.getHeight()
	    			||amplitude.getHeight()!=phaseDelay.getHeight()) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
	    	}
	    }
	@Override
	public void calculateOutput(double t) {
		// 实现具体的输出计算逻辑
		double amplitudeValue = amplitude.getData().getInitValue();
		double periodValue = period.getData().getInitValue();
		double pulseWidthValue = pulseWidth.getData().getInitValue();

		double output = amplitudeValue * (t % periodValue < pulseWidthValue ? 1 : 0);
		outputPortList.get(0).getOutputSignalC().setValue(output);
	}

	@Override
	public void calculateInit() {
		// 初始化逻辑
		outputPortList.get(0).getOutputSignalC().setValue(0.0);
	}
}
// Removed extra closing brace if present
