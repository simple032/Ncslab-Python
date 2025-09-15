package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.source.PulseDto;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.source.SourceBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

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
public class Pulse extends SourceBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter amplitude;
    private final Parameter period;
    private final Parameter pulseWidth;
    private final Parameter phaseDelay;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Parameter Definitions ===
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

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization (source block has no inputs)
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");
        // No input ports for pulse block
        
        // Input port defaults (source block has no inputs)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        
        // Output port defaults
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false); // Source blocks don't have feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
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

        // Add parameters to parameterList for template context population
        parameterList.add(this.amplitude);
        parameterList.add(this.period);
        parameterList.add(this.pulseWidth);
        parameterList.add(this.phaseDelay);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);


        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Pulse(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility
        this.amplitude = getParameterByName("Amplitude");
        this.period = getParameterByName("Period");
        this.pulseWidth = getParameterByName("PulseWidth");
        this.phaseDelay = getParameterByName("PhaseDelay");
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime"); // 0 for continuous pulse
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list if they exist
        if (this.amplitude != null) parameterList.add(this.amplitude);
        if (this.period != null) parameterList.add(this.period);
        if (this.pulseWidth != null) parameterList.add(this.pulseWidth);
        if (this.phaseDelay != null) parameterList.add(this.phaseDelay);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates Pulse block directly from PulseDto DTO
     */
    public Pulse(PulseDto pulseDto, NCSLabModel model) {
        super(pulseDto, model);

        // Initialize final parameters from PulseDto
        this.amplitude = getParameterByName("Amplitude");
        this.period = getParameterByName("Period");
        this.pulseWidth = getParameterByName("PulseWidth");
        this.phaseDelay = getParameterByName("PhaseDelay");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.amplitude != null) parameterList.add(this.amplitude);
        if (this.period != null) parameterList.add(this.period);
        if (this.pulseWidth != null) parameterList.add(this.pulseWidth);
        if (this.phaseDelay != null) parameterList.add(this.phaseDelay);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + pulseDto.getBlockName());
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
		// Use TemplateUtils for comprehensive context population
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		
		// Add Pulse-specific context variables
		context.put("block", this);
		context.put("outputs", getOutputPortVariables());
		context.put("amplitude", amplitude);
		context.put("period", period);
		context.put("pulseWidth", pulseWidth);
		context.put("phaseDelay", phaseDelay);
		
		// Add parameter names for template ${...Name} variables - use local names to avoid double prefixing
		context.put("amplitudeName", context.get(amplitude.getLocalName())); // Use parameter local name mapped by TemplateUtils
		context.put("periodName", context.get(period.getLocalName())); // Use parameter local name mapped by TemplateUtils
		context.put("pulseWidthName", context.get(pulseWidth.getLocalName())); // Use parameter local name mapped by TemplateUtils
		context.put("phaseDelayName", context.get(phaseDelay.getLocalName())); // Use parameter local name mapped by TemplateUtils
		
		// Add dimension variables needed by template
		context.put("amplitudeHeight", amplitude.getHeight());
		context.put("amplitudeWidth", amplitude.getWidth());

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
		OutputPort outputPort = outputPortList.get(0);
		Data outputData;

		// Handle both scalar and matrix cases
		if (amplitude.getDataType() == com.ncslab.block.data.DataType.REAL) {
			// Scalar case - SIMULINK-compatible pulse generation
			double amplitudeValue = amplitude.getData().getInitValue();
			double periodValue = period.getData().getInitValue();
			double pulseWidthValue = pulseWidth.getData().getInitValue();
			double phaseDelayValue = phaseDelay.getData().getInitValue();

			// Handle edge cases
			if (periodValue <= 0.0 || Double.isNaN(periodValue) || Double.isInfinite(periodValue)) {
				outputData = new Data(0.0); // Safe fallback
			} else if (Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
				outputData = new Data(0.0); // Safe fallback
			} else {
				// Apply phase delay
				double adjustedTime = t - phaseDelayValue;
				
				// Handle negative time (before pulse starts)
				if (adjustedTime < 0.0) {
					outputData = new Data(0.0);
				} else {
					// Calculate position within period
					double timeInPeriod = adjustedTime % periodValue;
					
					// Convert pulse width to time units
					// If pulseWidth > 100, treat as absolute time, otherwise as percentage
					double pulseWidthTime;
					if (pulseWidthValue > 100.0) {
						pulseWidthTime = pulseWidthValue / 100.0 * periodValue; // Still percentage if > 100
					} else {
						pulseWidthTime = pulseWidthValue / 100.0 * periodValue; // Percentage of period
					}
					
					// Clamp pulse width to period
					pulseWidthTime = Math.min(pulseWidthTime, periodValue);
					
					// Generate pulse output
					double outputValue = (timeInPeriod < pulseWidthTime) ? amplitudeValue : 0.0;
					outputData = new Data(outputValue);
				}
			}
		} else {
			// Matrix case - element-wise pulse generation
			int height = amplitude.getHeight();
			int width = amplitude.getWidth();
			outputData = new Data(height, width);
			
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					double amplitudeValue = amplitude.getData().getMatrix().get(i, j);
					double periodValue = period.getData().getMatrix().get(i, j);
					double pulseWidthValue = pulseWidth.getData().getMatrix().get(i, j);
					double phaseDelayValue = phaseDelay.getData().getMatrix().get(i, j);

					// Handle edge cases
					if (periodValue <= 0.0 || Double.isNaN(periodValue) || Double.isInfinite(periodValue)) {
						outputData.getMatrix().set(i, j, 0.0);
					} else if (Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
						outputData.getMatrix().set(i, j, 0.0);
					} else {
						// Apply phase delay
						double adjustedTime = t - phaseDelayValue;
						
						if (adjustedTime < 0.0) {
							outputData.getMatrix().set(i, j, 0.0);
						} else {
							// Calculate position within period
							double timeInPeriod = adjustedTime % periodValue;
							
							// Convert pulse width to time units
							double pulseWidthTime;
							if (pulseWidthValue > 100.0) {
								pulseWidthTime = pulseWidthValue / 100.0 * periodValue;
							} else {
								pulseWidthTime = pulseWidthValue / 100.0 * periodValue;
							}
							
							// Clamp pulse width to period
							pulseWidthTime = Math.min(pulseWidthTime, periodValue);
							
							// Generate pulse output
							double outputValue = (timeInPeriod < pulseWidthTime) ? amplitudeValue : 0.0;
							outputData.getMatrix().set(i, j, outputValue);
						}
					}
				}
			}
		}

		outputPort.setData(outputData);
	}

	@Override
	public void calculateInit() {
		OutputPort outputPort = outputPortList.get(0);
		Data initialData;

		// Initialize output based on phase delay and initial conditions
		if (amplitude.getDataType() == com.ncslab.block.data.DataType.REAL) {
			// Scalar case
			double phaseDelayValue = phaseDelay.getData().getInitValue();
			double amplitudeValue = amplitude.getData().getInitValue();
			double periodValue = period.getData().getInitValue();
			double pulseWidthValue = pulseWidth.getData().getInitValue();

			// Handle edge cases for parameters
			if (periodValue <= 0.0 || Double.isNaN(periodValue) || Double.isInfinite(periodValue) ||
				Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
				initialData = new Data(0.0); // Safe fallback
			} else {
				// If phase delay is 0 or negative, pulse starts at t=0
				if (phaseDelayValue <= 0.0) {
					double pulseWidthTime = (pulseWidthValue > 100.0) ? 
						pulseWidthValue / 100.0 * periodValue : pulseWidthValue / 100.0 * periodValue;
					pulseWidthTime = Math.min(pulseWidthTime, periodValue);
					
					// At t=0, if pulse width > 0, output should be amplitude
					initialData = new Data((pulseWidthTime > 0.0) ? amplitudeValue : 0.0);
				} else {
					// Pulse hasn't started yet due to phase delay
					initialData = new Data(0.0);
				}
			}
		} else {
			// Matrix case
			int height = amplitude.getHeight();
			int width = amplitude.getWidth();
			initialData = new Data(height, width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					double phaseDelayValue = phaseDelay.getData().getMatrix().get(i, j);
					double amplitudeValue = amplitude.getData().getMatrix().get(i, j);
					double periodValue = period.getData().getMatrix().get(i, j);
					double pulseWidthValue = pulseWidth.getData().getMatrix().get(i, j);

					// Handle edge cases
					if (periodValue <= 0.0 || Double.isNaN(periodValue) || Double.isInfinite(periodValue) ||
						Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
						initialData.getMatrix().set(i, j, 0.0);
					} else {
						// If phase delay is 0 or negative, pulse starts at t=0
						if (phaseDelayValue <= 0.0) {
							double pulseWidthTime = (pulseWidthValue > 100.0) ? 
								pulseWidthValue / 100.0 * periodValue : pulseWidthValue / 100.0 * periodValue;
							pulseWidthTime = Math.min(pulseWidthTime, periodValue);
							
							// At t=0, if pulse width > 0, output should be amplitude
							initialData.getMatrix().set(i, j, (pulseWidthTime > 0.0) ? amplitudeValue : 0.0);
						} else {
							// Pulse hasn't started yet due to phase delay
							initialData.getMatrix().set(i, j, 0.0);
						}
					}
				}
			}
		}

		outputPort.setData(initialData);
	}
}
// Removed extra closing brace if present
