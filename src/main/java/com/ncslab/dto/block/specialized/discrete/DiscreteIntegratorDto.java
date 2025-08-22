package com.ncslab.dto.block.specialized.discrete;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * DTO representation of Discrete_Time_Integrator block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Discrete_Time_Integrator block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - Gain: Gain value for integration (default: 1)
 * - InitialCondition: Initial condition of the integrator (default: 0)
 * - IntegratorMethod: Integration method ("Forward Euler", "Backward Euler", "Trapezoidal")
 * - SampleTime: Sample time for discrete operation (must be positive or -1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * Discrete Integration Methods:
 * - Forward Euler: y[k] = y[k-1] + T*K*u[k-1]
 * - Backward Euler: y[k] = y[k-1] + T*K*u[k]
 * - Trapezoidal: y[k] = y[k-1] + T*K*(u[k] + u[k-1])/2
 * 
 * where T is sample time, K is gain, u is input, y is output
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 8
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.Discrete_Time_Integrator")
public class DiscreteIntegratorDto extends BlockDto {
    
    // ===== DISCRETE INTEGRATOR SPECIFIC PARAMETERS =====
    
    /**
     * Gain value for integration
     * Default: 1
     * Validation: Must be finite number
     */
    private TypedParameter gain;
    
    /**
     * Initial condition of the integrator
     * Default: 0
     * Validation: Must be finite number
     */
    private TypedParameter initialCondition;
    
    /**
     * Integration method
     * Default: "Forward Euler"
     * Options: "Forward Euler", "Backward Euler", "Trapezoidal"
     */
    private TypedParameter integratorMethod;
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;
    
    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    // ===== INTEGRATION METHOD CONSTANTS =====
    
    public static final String FORWARD_EULER = "Forward Euler";
    public static final String BACKWARD_EULER = "Backward Euler";
    public static final String TRAPEZOIDAL = "Trapezoidal";
    
    private static final List<String> VALID_METHODS = Arrays.asList(
        FORWARD_EULER, BACKWARD_EULER, TRAPEZOIDAL
    );
    
    // ===== FACTORY METHODS =====
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static DiscreteIntegratorDto fromLegacyParameters(Map<String, String> params) {
        DiscreteIntegratorDto.DiscreteIntegratorDtoBuilder builder = DiscreteIntegratorDto.builder()
                .blockName(params.getOrDefault("blockName", "DiscreteIntegrator"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("Discrete_Time_Integrator");
        
        DiscreteIntegratorDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("Gain")) {
            try {
                double gainVal = Double.parseDouble(params.get("Gain"));
                dto.gain = TypedParameter.of(gainVal);
            } catch (NumberFormatException e) {
                dto.gain = TypedParameter.of(params.get("Gain"));
            }
        }
        
        if (params.containsKey("InitialCondition")) {
            try {
                double ic = Double.parseDouble(params.get("InitialCondition"));
                dto.initialCondition = TypedParameter.of(ic);
            } catch (NumberFormatException e) {
                dto.initialCondition = TypedParameter.of(params.get("InitialCondition"));
            }
        }
        
        if (params.containsKey("IntegratorMethod")) {
            dto.integratorMethod = TypedParameter.of(params.get("IntegratorMethod"));
        }
        
        if (params.containsKey("SampleTime")) {
            try {
                double st = Double.parseDouble(params.get("SampleTime"));
                dto.setSampleTime(st);
            } catch (NumberFormatException e) {
                // Keep as string for special values like "auto"
            }
        }
        
        if (params.containsKey("OutDataTypeStr")) {
            dto.outDataTypeStr = TypedParameter.of(params.get("OutDataTypeStr"));
        }
        
        if (params.containsKey("SaturateOnIntegerOverflow")) {
            dto.saturateOnIntegerOverflow = TypedParameter.of("on".equals(params.get("SaturateOnIntegerOverflow")));
        }
        
        return dto;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static DiscreteIntegratorDtoBuilder builderWithDefaults() {
        DiscreteIntegratorDto dto = DiscreteIntegratorDto.builder()
                .blockType("Discrete_Time_Integrator")
                .sampleTime(-1.0) // Inherited by default
                .build();
        
        // Set default typed parameters
        dto.gain = TypedParameter.of(1.0);
        dto.initialCondition = TypedParameter.of(0.0);
        dto.integratorMethod = TypedParameter.of(FORWARD_EULER);
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        
        return DiscreteIntegratorDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .gain(dto.gain)
                .initialCondition(dto.initialCondition)
                .integratorMethod(dto.integratorMethod)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getGainValue() {
        return gain != null ? gain.getValue(Double.class) : 1.0;
    }
    
    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }
    
    public String getIntegratorMethodValue() {
        return integratorMethod != null ? integratorMethod.getValue(String.class) : FORWARD_EULER;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getValue(String.class) : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }
    
    // ===== DISCRETE-TIME VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate gain
        if (gain != null) {
            try {
                Double gainVal = gain.getValue(Double.class);
                if (gainVal != null && (Double.isNaN(gainVal) || Double.isInfinite(gainVal))) {
                    result.addError("Gain must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid gain format: " + e.getMessage());
            }
        }
        
        // Validate initial condition
        if (initialCondition != null) {
            try {
                Double icVal = initialCondition.getValue(Double.class);
                if (icVal != null && (Double.isNaN(icVal) || Double.isInfinite(icVal))) {
                    result.addError("Initial condition must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid initial condition format: " + e.getMessage());
            }
        }
        
        // Validate integration method
        if (integratorMethod != null) {
            String method = integratorMethod.getValue(String.class);
            if (method != null && !VALID_METHODS.contains(method)) {
                result.addError("Integration method must be one of: " + VALID_METHODS);
            }
        }
        
        // Validate discrete sample time
        if (getSampleTime() != null) {
            double sampleTime = getSampleTime();
            if (sampleTime != -1.0 && sampleTime <= 0.0) {
                result.addError("Sample time must be positive or -1 (inherited)");
            }
            if (Double.isNaN(sampleTime) || Double.isInfinite(sampleTime)) {
                result.addError("Sample time must be finite");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if sample time is inherited (-1)
     */
    public boolean isInheritedSampleTime() {
        return getSampleTime() != null && getSampleTime() == -1.0;
    }
    
    /**
     * Check if sample time is discrete (positive value)
     */
    public boolean isDiscreteSampleTime() {
        return getSampleTime() != null && getSampleTime() > 0.0;
    }
    
    /**
     * Check if using forward Euler method
     */
    public boolean isForwardEuler() {
        return FORWARD_EULER.equals(getIntegratorMethodValue());
    }
    
    /**
     * Check if using backward Euler method
     */
    public boolean isBackwardEuler() {
        return BACKWARD_EULER.equals(getIntegratorMethodValue());
    }
    
    /**
     * Check if using trapezoidal method
     */
    public boolean isTrapezoidal() {
        return TRAPEZOIDAL.equals(getIntegratorMethodValue());
    }
    
    /**
     * Get integration coefficient (T * K) where T is sample time and K is gain
     */
    public double getIntegrationCoefficient() {
        if (isDiscreteSampleTime()) {
            return getSampleTime() * getGainValue();
        }
        return Double.NaN; // Cannot determine without knowing inherited sample time
    }
    
    /**
     * Create discrete integrator with specific parameters
     */
    public static DiscreteIntegratorDto create(String name, String path, double gain, 
                                             double initialCondition, String method, double sampleTime) {
        DiscreteIntegratorDto dto = new DiscreteIntegratorDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setBlockType("Discrete_Time_Integrator");
        dto.setSampleTime(sampleTime);
        
        // Set default parameters
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setSaturateOnIntegerOverflow(TypedParameter.of(false));
        
        // Set specific parameters
        dto.setGain(TypedParameter.of(gain));
        dto.setInitialCondition(TypedParameter.of(initialCondition));
        dto.setIntegratorMethod(TypedParameter.of(method));
        
        return dto;
    }
    
    /**
     * Create forward Euler integrator
     */
    public static DiscreteIntegratorDto createForwardEuler(String name, String path, double gain, 
                                                          double initialCondition, double sampleTime) {
        return create(name, path, gain, initialCondition, FORWARD_EULER, sampleTime);
    }
    
    /**
     * Create backward Euler integrator
     */
    public static DiscreteIntegratorDto createBackwardEuler(String name, String path, double gain, 
                                                           double initialCondition, double sampleTime) {
        return create(name, path, gain, initialCondition, BACKWARD_EULER, sampleTime);
    }
    
    /**
     * Create trapezoidal integrator
     */
    public static DiscreteIntegratorDto createTrapezoidal(String name, String path, double gain, 
                                                         double initialCondition, double sampleTime) {
        return create(name, path, gain, initialCondition, TRAPEZOIDAL, sampleTime);
    }
    
    @Override
    public DiscreteIntegratorDto copy() {
        return DiscreteIntegratorDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .gain(gain != null ? gain.copy() : null)
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .integratorMethod(integratorMethod != null ? integratorMethod.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (gain != null) {
            params.put("Gain", String.valueOf(gain.getValue()));
        }
        if (initialCondition != null) {
            params.put("InitialCondition", String.valueOf(initialCondition.getValue()));
        }
        if (integratorMethod != null) {
            params.put("IntegratorMethod", String.valueOf(integratorMethod.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", String.valueOf(getSampleTime()));
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getValue(Boolean.class);
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }
        
        return params;
    }
    
    @Override
    public String toString() {
        return String.format("DiscreteIntegratorDto{id=%d, name='%s', type='%s', gain=%s, method='%s', sampleTime=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getGainValue(),
                           getIntegratorMethodValue(),
                           getSampleTime());
    }
}