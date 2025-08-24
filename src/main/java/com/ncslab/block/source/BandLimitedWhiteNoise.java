package com.ncslab.block.source;

import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.source.BandLimitedWhiteNoiseDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * BandLimitedWhiteNoise block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Seed: Random seed for noise generation
 * - Cov: Covariance/power spectral density of the noise
 * - Ts: Sample period for discrete operation
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class BandLimitedWhiteNoise extends SourceBlock {

    // === BandLimitedWhiteNoise-Specific SIMULINK Parameters ===
    private final Parameter seed;
    private final Parameter cov;
    private final Parameter samplePeriod;
    
    // === Static Parameter Definitions ===
    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // Port names
        outputNames.add("out1");
        // No input ports for noise block
        
        // Noise-specific defaults
        Map<String, String> noiseDefaults = new HashMap<>();
        noiseDefaults.put("Seed", "0");
        noiseDefaults.put("Cov", "1");
        noiseDefaults.put("Ts", "0.1");
        
        // Merge with common source block defaults
        PARAMETER_DEFAULTS.putAll(mergeWithCommonDefaults(noiseDefaults));
    }
    
    // === Private Constructor with Typed Parameters ===
    private BandLimitedWhiteNoise(Parameter seed, Parameter cov, Parameter samplePeriod,
                                 Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super("BandLimitedWhiteNoise", sampleTime, outDataType, saturateOnIntegerOverflow, blockName, blockPath, blockUUID, model);
        
        // Validate parameters
        validateParameters(seed, cov, samplePeriod, sampleTime);
        
        // Assign BandLimitedWhiteNoise-specific parameters
        this.seed = Objects.requireNonNull(seed, "Seed parameter cannot be null");
        this.cov = Objects.requireNonNull(cov, "Covariance parameter cannot be null");
        this.samplePeriod = Objects.requireNonNull(samplePeriod, "Sample period parameter cannot be null");
        
        // Add noise-specific parameters to parameter list
        parameterList.add(seed);
        parameterList.add(cov);
        parameterList.add(samplePeriod);
        
        // Set port dimensions
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public BandLimitedWhiteNoise(JSONObject blockJSON, NCSLabModel model){
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility  
        this.seed = new Parameter(this, 1, "Seed", String.valueOf(paramValues.getInt("Seed")));
        this.cov = new Parameter(this, 2, "Cov", String.valueOf(paramValues.getDouble("Cov")));
        this.samplePeriod = new Parameter(this, 3, "Ts", String.valueOf(paramValues.getDouble("Ts")));
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates BandLimitedWhiteNoise block directly from BlockDto DTO
     */
    public BandLimitedWhiteNoise(BandLimitedWhiteNoiseDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.seed = new Parameter(this, 1, "Seed", "0");
        this.cov = new Parameter(this, 2, "Cov", "0");
        this.samplePeriod = new Parameter(this, 3, "Sampleperiod", "0");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

// === Static Factory Method for JSON Deserialization ===
    public static BandLimitedWhiteNoise fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter seed = createSeedFromJSON(paramValues, blockName);
            Parameter cov = createCovFromJSON(paramValues, blockName);
            Parameter samplePeriod = createSamplePeriodFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            BandLimitedWhiteNoise block = new BandLimitedWhiteNoise(seed, cov, samplePeriod, sampleTime, outDataType, saturateParam,
                                                                   blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, seed, cov, samplePeriod, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create BandLimitedWhiteNoise block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static BandLimitedWhiteNoise create(String name, String path, int seed, double cov, double samplePeriod, NCSLabModel model) {
        return create(name, path, seed, cov, samplePeriod, 0.0, "double", false, model);
    }
    
    public static BandLimitedWhiteNoise create(String name, String path, int seed, double cov, double samplePeriod,
                                              double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter seedParam = new Parameter(null, 1, "Seed", String.valueOf(seed));
        Parameter covParam = new Parameter(null, 2, "Cov", String.valueOf(cov));
        Parameter samplePeriodParam = new Parameter(null, 3, "Ts", String.valueOf(samplePeriod));
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        BandLimitedWhiteNoise block = new BandLimitedWhiteNoise(seedParam, covParam, samplePeriodParam, sampleTimeParam, outDataTypeParam, saturateParam,
                                                               name, path, "null", model);
        
        setParameterBlockReference(block, seedParam, covParam, samplePeriodParam, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter seed, Parameter cov, Parameter samplePeriod, Parameter sampleTime) {
        int seedValue = (int) seed.getDouble();
        if (seedValue < 0) {
            throw new IllegalArgumentException("Seed must be non-negative");
        }
        
        double covValue = cov.getDouble();
        if (covValue < 0.0 || Double.isNaN(covValue) || Double.isInfinite(covValue)) {
            throw new IllegalArgumentException("Covariance must be non-negative and finite");
        }
        
        double samplePeriodValue = samplePeriod.getDouble();
        if (samplePeriodValue <= 0.0 || Double.isNaN(samplePeriodValue) || Double.isInfinite(samplePeriodValue)) {
            throw new IllegalArgumentException("Sample period must be positive and finite");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSeedFromJSON(JSONObject paramValues, String blockName) {
        String seedValue = paramValues.optString("Seed", "23341");
        return new Parameter(null, 1, "Seed", seedValue);
    }
    
    private static Parameter createCovFromJSON(JSONObject paramValues, String blockName) {
        String covValue = paramValues.optString("Cov", "1.0");
        return new Parameter(null, 2, "Cov", covValue);
    }
    
    private static Parameter createSamplePeriodFromJSON(JSONObject paramValues, String blockName) {
        String samplePeriodValue = paramValues.optString("Ts", "0.1");
        return new Parameter(null, 3, "Ts", samplePeriodValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 4, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "double");
        return new Parameter(null, 5, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(BandLimitedWhiteNoise block, Parameter... parameters) {
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
    
    
    // === Port Dimension Setup ===
    private void initializePorts() {
        // Set port dimensions based on seed parameter
        outputPortList.get(0).setHeight(seed.getHeight());
        outputPortList.get(0).setWidth(seed.getWidth());
    }

    // === Noise Generation Methods ===
    private double generateGaussianNoise(double mean, double stdDev) {
        return mean + stdDev * Math.random();
    }

    // === Code Generation Methods (using Velocity templates) ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        
        context.put("seed", this.seed);
        context.put("cov", this.cov);
        context.put("samplePeriod", this.samplePeriod);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/source/BandLimitedWhiteNoise/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        context.put("block", this);
        context.put("seed", this.seed);
        context.put("cov", this.cov);
        context.put("samplePeriod", this.samplePeriod);
        context.put("sampleTime", this.sampleTime);
        context.put("outDataType", this.outDataType);
        context.put("saturateOnIntegerOverflow", this.saturateOnIntegerOverflow);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/source/BandLimitedWhiteNoise/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        if (seed.getWidth() != cov.getWidth()
                || seed.getHeight() != cov.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw(e);
        }
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateOutput(double t) {
        double covValue = cov.getData().getInitValue();
        double noiseValue = generateGaussianNoise(0.0, Math.sqrt(covValue));
        outputPortList.get(0).getOutputSignalC().setData(new Data(noiseValue));
    }

    @Override
    public void calculateInit() {
        outputPortList.get(0).getOutputSignalC().setData(new Data(0.0));
    }
}