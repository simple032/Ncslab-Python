package com.ncslab.block.source;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.source.SineWaveDto;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.source.SourceBlock;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * SineWave block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Amplitude: Amplitude of sine wave
 * - Bias: DC offset (bias) of the signal
 * - Frequency: Frequency of sine wave in rad/s
 * - Phase: Phase shift in radians
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - Samples: Number of samples per frame
 * - TimeSource: Time source (Use simulation time, Use external signal)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class SineWave extends SourceBlock {
    
    // === SIMULINK-Compatible Parameters ===
    private final Parameter amplitude;
    private final Parameter bias;
    private final Parameter frequency;
    private final Parameter phase;
    private final Parameter sampleTime;
    private final Parameter samples;
    private final Parameter timeSource;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Amplitude", "1");
        PARAMETER_DEFAULTS.put("Bias", "0");
        PARAMETER_DEFAULTS.put("Frequency", "1");  // rad/s
        PARAMETER_DEFAULTS.put("Phase", "0");      // radians
        PARAMETER_DEFAULTS.put("SampleTime", "0"); // Continuous
        PARAMETER_DEFAULTS.put("Samples", "1");
        PARAMETER_DEFAULTS.put("TimeSource", "Use simulation time");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        // No input ports for sine wave block (unless external time source)
    }

    // === Private Constructor with Typed Parameters ===
    private SineWave(Parameter amplitude, Parameter bias, Parameter frequency, Parameter phase,
                    Parameter sampleTime, Parameter samples, Parameter timeSource,
                    Parameter outDataType, Parameter saturateOnIntegerOverflow,
                    String blockName, String blockPath, String blockUUID, 
                    NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(amplitude, bias, frequency, phase, sampleTime);
        
        // Assign parameters
        this.amplitude = Objects.requireNonNull(amplitude, "Amplitude parameter cannot be null");
        this.bias = Objects.requireNonNull(bias, "Bias parameter cannot be null");
        this.frequency = Objects.requireNonNull(frequency, "Frequency parameter cannot be null");
        this.phase = Objects.requireNonNull(phase, "Phase parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.samples = Objects.requireNonNull(samples, "Samples parameter cannot be null");
        this.timeSource = Objects.requireNonNull(timeSource, "Time source parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public SineWave(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility
        this.amplitude = getParameterByName("Amplitude");
        this.bias = getParameterByName("Bias");
        this.frequency = getParameterByName("Frequency");
        this.phase = getParameterByName("Phase");
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime"); // 0 for continuous
        this.samples = getParameterByName("Samples");
        this.timeSource = getParameterByName("TimeSource");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }    
    
    /**
     * DTO-NATIVE Constructor - Creates SineWave block directly from SineWaveDto
     */
    public SineWave(SineWaveDto dto, NCSLabModel model) {
        super(dto, model);

        // Initialize parameters from DTO with null safety and validation
        this.amplitude = getParameterByName("Amplitude");
        this.bias = getParameterByName("Bias");
        this.frequency = getParameterByName("Frequency");
        this.phase = getParameterByName("Phase");
        this.sampleTime = getParameterByName("SampleTime");
        this.samples = getParameterByName("Samples");
        this.timeSource = getParameterByName("TimeSource");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName( "SaturateOnIntegerOverflow");

        initializePorts();
        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }


    void initializePorts(){
        // Initialize ports        
        outputPortList.get(0).setHeight(amplitude.getHeight());
        outputPortList.get(0).setWidth(amplitude.getWidth());

    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static SineWave fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            // Extract and validate JSON fields
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            // Create typed parameters from JSON with defaults
            Parameter amplitude = createAmplitudeFromJSON(paramValues, blockName);
            Parameter bias = createBiasFromJSON(paramValues, blockName);
            Parameter frequency = createFrequencyFromJSON(paramValues, blockName);
            Parameter phase = createPhaseFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter samples = createSamplesFromJSON(paramValues, blockName);
            Parameter timeSource = createTimeSourceFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            SineWave block = new SineWave(amplitude, bias, frequency, phase, sampleTime, samples, 
                                         timeSource, outDataType, saturateParam,
                                         blockName, blockPath, blockUUID, model);
            
            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, amplitude, bias, frequency, phase, sampleTime, 
                                      samples, timeSource, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create SineWave block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static SineWave create(String name, String path, double amplitude, double frequency, NCSLabModel model) {
        return create(name, path, amplitude, 0.0, frequency, 0.0, 0.0, 1, "Use simulation time", 
                     "Inherit: Same as parameter", false, model);
    }
    public static SineWave create(String name, String path, double amplitude, double bias, double frequency, 
                                 double phase, double sampleTime, int samples, String timeSource,
                                 String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Create parameters
        Parameter amplitudeParam = new Parameter(null, 1, "Amplitude", String.valueOf(amplitude));
        Parameter biasParam = new Parameter(null, 2, "Bias", String.valueOf(bias));
        Parameter frequencyParam = new Parameter(null, 3, "Frequency", String.valueOf(frequency));
        Parameter phaseParam = new Parameter(null, 4, "Phase", String.valueOf(phase));
        Parameter sampleTimeParam = new Parameter(null, 5, "SampleTime", String.valueOf(sampleTime));
        Parameter samplesParam = new Parameter(null, 6, "Samples", String.valueOf(samples));
        Parameter timeSourceParam = new Parameter(null, 7, "TimeSource", timeSource);
        Parameter outDataTypeParam = new Parameter(null, 8, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 9, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        SineWave block = new SineWave(amplitudeParam, biasParam, frequencyParam, phaseParam, sampleTimeParam,
                                     samplesParam, timeSourceParam, outDataTypeParam, saturateParam,
                                     name, path, "null", model);
        
        // Set block reference in parameters
        setParameterBlockReference(block, amplitudeParam, biasParam, frequencyParam, phaseParam, sampleTimeParam,
                                  samplesParam, timeSourceParam, outDataTypeParam, saturateParam);
        
        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter amplitude, Parameter bias, Parameter frequency, 
                                          Parameter phase, Parameter sampleTime) {
        // Validate frequency is positive
        double freqValue = frequency.getDouble();
        if (freqValue < 0.0 || Double.isNaN(freqValue) || Double.isInfinite(freqValue)) {
            throw new IllegalArgumentException("Frequency must be positive and finite");
        }
        // Validate amplitude is finite
        double ampValue = amplitude.getDouble();
        if (Double.isNaN(ampValue) || Double.isInfinite(ampValue)) {
            throw new IllegalArgumentException("Amplitude must be finite");
        }
        // Validate bias is finite  
        double biasValue = bias.getDouble();
        if (Double.isNaN(biasValue) || Double.isInfinite(biasValue)) {
            throw new IllegalArgumentException("Bias must be finite");
        }
        // Validate phase is finite
        double phaseValue = phase.getDouble();
        if (Double.isNaN(phaseValue) || Double.isInfinite(phaseValue)) {
            throw new IllegalArgumentException("Phase must be finite");
        }
        // Validate sample time (0 for continuous, >0 for discrete, -1 for inherited)
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
    private static Parameter createBiasFromJSON(JSONObject paramValues, String blockName) {
        String biasValue = paramValues.optString("Bias", "0");
        return new Parameter(null, 2, "Bias", biasValue);
    }
    private static Parameter createFrequencyFromJSON(JSONObject paramValues, String blockName) {
        String frequencyValue = paramValues.optString("Frequency", "1");
        return new Parameter(null, 3, "Frequency", frequencyValue);
    }
    private static Parameter createPhaseFromJSON(JSONObject paramValues, String blockName) {
        String phaseValue = paramValues.optString("Phase", "0");
        return new Parameter(null, 4, "Phase", phaseValue);
    }
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 5, "SampleTime", sampleTimeValue);
    }
    private static Parameter createSamplesFromJSON(JSONObject paramValues, String blockName) {
        String samplesValue = paramValues.optString("Samples", "1");
        return new Parameter(null, 6, "Samples", samplesValue);
    }
    private static Parameter createTimeSourceFromJSON(JSONObject paramValues, String blockName) {
        String timeSourceValue = paramValues.optString("TimeSource", "Use simulation time");
        return new Parameter(null, 7, "TimeSource", timeSourceValue);
    }
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as parameter");
        return new Parameter(null, 8, "OutDataTypeStr", outDataTypeValue);
    }
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 9, "SaturateOnIntegerOverflow", saturateValue);
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
    private static void setParameterBlockReference(SineWave block, Parameter... parameters) {
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
        identity.put("blockType", "SineWave");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    // === Code Generation Methods (preserved from original) ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/source/SineWave/init.vm", context);
        code.addInitCode(codeStr);
    }
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/source/SineWave/output.vm", context);
        code.addOutputCode(codeStr);
    }
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/source/SineWave/init.vm", context);
        code.addInitCode(codeStr);
    }
    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/source/SineWave/output.vm", context);
        code.addOutputCode(codeStr);
    }
    public void updateDimension() throws MatDimException {
        if (amplitude.getWidth() != bias.getWidth()
                || amplitude.getWidth() != frequency.getWidth()
                || amplitude.getWidth() != phase.getWidth()
                || amplitude.getHeight() != bias.getHeight()
                || amplitude.getHeight() != frequency.getHeight()
                || amplitude.getHeight() != phase.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }
    }

    public void checkDimension() throws MatDimException {
        // No additional dimension checks needed for sine wave block
    }

    // === Runtime Simulation API (restored) ===
    @Override
    public void calculateOutput(double t) {
        OutputPort outputPort = outputPortList.get(0);
        Data outputData;

        // Handle both scalar and matrix cases
        if (amplitude.getDataType() == com.ncslab.block.data.DataType.REAL) {
            // Scalar case - SIMULINK-compatible sine wave generation
            double amplitudeValue = amplitude.getData().getInitValue();
            double biasValue = bias.getData().getInitValue();
            double frequencyValue = frequency.getData().getInitValue();
            double phaseValue = phase.getData().getInitValue();

            // Handle edge cases
            if (Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
                outputData = new Data(Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
            } else if (Double.isNaN(frequencyValue) || Double.isInfinite(frequencyValue)) {
                outputData = new Data(Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
            } else if (Double.isNaN(phaseValue) || Double.isInfinite(phaseValue)) {
                outputData = new Data(Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
            } else {
                // Standard sine wave calculation: amplitude * sin(2*pi*frequency*t + phase) + bias
                // Note: SIMULINK uses 2*pi*frequency for Hz, or direct frequency for rad/s
                double argument = frequencyValue * t + phaseValue;
                double sineValue = Math.sin(argument);
                
                // Handle NaN result from sin (shouldn't happen with finite inputs, but defensive)
                if (Double.isNaN(sineValue)) {
                    sineValue = 0.0;
                }
                
                double outputValue = amplitudeValue * sineValue + (Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                outputData = new Data(outputValue);
            }
        } else {
            // Matrix case - element-wise sine wave generation
            int height = amplitude.getHeight();
            int width = amplitude.getWidth();
            outputData = new Data(height, width);
            
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    double amplitudeValue = amplitude.getData().getMatrix().get(i, j);
                    double biasValue = bias.getData().getMatrix().get(i, j);
                    double frequencyValue = frequency.getData().getMatrix().get(i, j);
                    double phaseValue = phase.getData().getMatrix().get(i, j);

                    // Handle edge cases for each matrix element
                    if (Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
                        outputData.getMatrix().set(i, j, Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                    } else if (Double.isNaN(frequencyValue) || Double.isInfinite(frequencyValue)) {
                        outputData.getMatrix().set(i, j, Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                    } else if (Double.isNaN(phaseValue) || Double.isInfinite(phaseValue)) {
                        outputData.getMatrix().set(i, j, Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                    } else {
                        // Standard sine wave calculation
                        double argument = frequencyValue * t + phaseValue;
                        double sineValue = Math.sin(argument);
                        
                        // Handle NaN result from sin
                        if (Double.isNaN(sineValue)) {
                            sineValue = 0.0;
                        }
                        
                        double outputValue = amplitudeValue * sineValue + (Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                        outputData.getMatrix().set(i, j, outputValue);
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

        // Initialize output at t=0: amplitude * sin(phase) + bias
        if (amplitude.getDataType() == com.ncslab.block.data.DataType.REAL) {
            // Scalar case
            double amplitudeValue = amplitude.getData().getInitValue();
            double biasValue = bias.getData().getInitValue();
            double phaseValue = phase.getData().getInitValue();

            // Handle edge cases for parameters
            if (Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
                initialData = new Data(Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
            } else if (Double.isNaN(phaseValue) || Double.isInfinite(phaseValue)) {
                initialData = new Data(Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
            } else {
                // At t=0: amplitude * sin(phase) + bias
                double sineValue = Math.sin(phaseValue);
                
                // Handle NaN result from sin
                if (Double.isNaN(sineValue)) {
                    sineValue = 0.0;
                }
                
                double initialValue = amplitudeValue * sineValue + (Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                initialData = new Data(initialValue);
            }
        } else {
            // Matrix case
            int height = amplitude.getHeight();
            int width = amplitude.getWidth();
            initialData = new Data(height, width);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    double amplitudeValue = amplitude.getData().getMatrix().get(i, j);
                    double biasValue = bias.getData().getMatrix().get(i, j);
                    double phaseValue = phase.getData().getMatrix().get(i, j);

                    // Handle edge cases for each matrix element
                    if (Double.isNaN(amplitudeValue) || Double.isInfinite(amplitudeValue)) {
                        initialData.getMatrix().set(i, j, Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                    } else if (Double.isNaN(phaseValue) || Double.isInfinite(phaseValue)) {
                        initialData.getMatrix().set(i, j, Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                    } else {
                        // At t=0: amplitude * sin(phase) + bias
                        double sineValue = Math.sin(phaseValue);
                        
                        // Handle NaN result from sin
                        if (Double.isNaN(sineValue)) {
                            sineValue = 0.0;
                        }
                        
                        double initialValue = amplitudeValue * sineValue + (Double.isNaN(biasValue) || Double.isInfinite(biasValue) ? 0.0 : biasValue);
                        initialData.getMatrix().set(i, j, initialValue);
                    }
                }
            }
        }

        outputPort.setData(initialData);
    }
}
