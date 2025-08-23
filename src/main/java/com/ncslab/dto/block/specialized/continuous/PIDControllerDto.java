package com.ncslab.dto.block.specialized.continuous;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.versioning.VersionedDto;
import com.ncslab.dto.versioning.VersionInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
/**
 * DTO representation of PIDController block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the PID Controller block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - P: Proportional gain
 * - I: Integral gain  
 * - D: Derivative gain
 * - N: Filter coefficient for derivative term
 * - FormulationType: PID form (parallel, standard)
 * - ExternalReset: External reset mode
 * - InitialConditionForIntegrator: Initial condition for integrator
 * - InitialConditionForFilter: Initial condition for filter
 * - LimitOutput: Whether to limit output values
 * - UpperSaturationLimit: Upper limit for output
 * - LowerSaturationLimit: Lower limit for output
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("PIDController")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.PIDController")
public class PIDControllerDto extends BlockDto implements VersionedDto {

    // ===== PID CONTROLLER SPECIFIC PARAMETERS =====
    
    /**
     * Proportional gain
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter proportionalGain = TypedParameter.of(1.0);
    
    /**
     * Integral gain
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter integralGain = TypedParameter.of(1.0);
    
    /**
     * Derivative gain
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter derivativeGain = TypedParameter.of(0.0);
    
    /**
     * Filter coefficient for derivative term
     * Default: 100.0
     */
    @Builder.Default
    private TypedParameter filterCoefficient = TypedParameter.of(100.0);
    
    /**
     * PID formulation type
     * Default: "Parallel"
     * Options: "Parallel", "Standard"
     */
    @Builder.Default
    private TypedParameter formulationType = TypedParameter.of("Parallel");
    
    /**
     * External reset mode
     * Default: "none"
     * Options: "none", "rising", "falling", "either", "level"
     */
    @Builder.Default
    private TypedParameter externalReset = TypedParameter.of("none");
    
    /**
     * Initial condition for integrator
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialConditionForIntegrator = TypedParameter.of(0.0);
    
    /**
     * Initial condition for filter
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialConditionForFilter = TypedParameter.of(0.0);
    
    /**
     * Whether to limit output values
     * Default: false
     */
    @Builder.Default
    private TypedParameter limitOutput = TypedParameter.of(false);
    
    /**
     * Upper saturation limit
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter upperSaturationLimit = TypedParameter.of(1.0);
    
    /**
     * Lower saturation limit
     * Default: -1.0
     */
    @Builder.Default
    private TypedParameter lowerSaturationLimit = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    /**
     * Handle integer overflow
     * Default: false
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getProportionalGainValue() {
        return proportionalGain != null ? proportionalGain.getAsDouble() : 1.0;
    }
    
    public Double getIntegralGainValue() {
        return integralGain != null ? integralGain.getAsDouble() : 1.0;
    }
    
    public Double getDerivativeGainValue() {
        return derivativeGain != null ? derivativeGain.getAsDouble() : 0.0;
    }
    
    public Double getFilterCoefficientValue() {
        return filterCoefficient != null ? filterCoefficient.getAsDouble() : 100.0;
    }
    
    public String getFormulationTypeValue() {
        return formulationType != null ? formulationType.getAsString() : "Parallel";
    }
    
    public String getExternalResetValue() {
        return externalReset != null ? externalReset.getAsString() : "none";
    }
    
    public Double getInitialConditionForIntegratorValue() {
        return initialConditionForIntegrator != null ? initialConditionForIntegrator.getAsDouble() : 0.0;
    }
    
    public Double getInitialConditionForFilterValue() {
        return initialConditionForFilter != null ? initialConditionForFilter.getAsDouble() : 0.0;
    }
    
    public Boolean getLimitOutputValue() {
        return limitOutput != null ? limitOutput.getAsBoolean() : false;
    }
    
    public Double getUpperSaturationLimitValue() {
        return upperSaturationLimit != null ? upperSaturationLimit.getAsDouble() : 1.0;
    }
    
    public Double getLowerSaturationLimitValue() {
        return lowerSaturationLimit != null ? lowerSaturationLimit.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate gains are finite
        if (proportionalGain != null) {
            Double pValue = proportionalGain.getAsDouble();
            if (pValue != null && (Double.isNaN(pValue) || Double.isInfinite(pValue))) {
                result.addError("Proportional gain must be finite");
            }
        }
        
        if (integralGain != null) {
            Double iValue = integralGain.getAsDouble();
            if (iValue != null && (Double.isNaN(iValue) || Double.isInfinite(iValue))) {
                result.addError("Integral gain must be finite");
            }
        }
        
        if (derivativeGain != null) {
            Double dValue = derivativeGain.getAsDouble();
            if (dValue != null && (Double.isNaN(dValue) || Double.isInfinite(dValue))) {
                result.addError("Derivative gain must be finite");
            }
        }
        
        // Validate filter coefficient is positive
        if (filterCoefficient != null) {
            Double nValue = filterCoefficient.getAsDouble();
            if (nValue != null && nValue <= 0.0) {
                result.addError("Filter coefficient must be positive");
            }
        }
        
        // Validate saturation limits
        if (limitOutput != null && Boolean.TRUE.equals(limitOutput.getAsBoolean())) {
            Double upper = getUpperSaturationLimitValue();
            Double lower = getLowerSaturationLimitValue();
            if (upper != null && lower != null && upper <= lower) {
                result.addError("Upper saturation limit must be greater than lower limit");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    @Override
    public PIDControllerDto copy() {
        return PIDControllerDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .proportionalGain(proportionalGain != null ? proportionalGain.copy() : null)
                .integralGain(integralGain != null ? integralGain.copy() : null)
                .derivativeGain(derivativeGain != null ? derivativeGain.copy() : null)
                .filterCoefficient(filterCoefficient != null ? filterCoefficient.copy() : null)
                .formulationType(formulationType != null ? formulationType.copy() : null)
                .externalReset(externalReset != null ? externalReset.copy() : null)
                .initialConditionForIntegrator(initialConditionForIntegrator != null ? initialConditionForIntegrator.copy() : null)
                .initialConditionForFilter(initialConditionForFilter != null ? initialConditionForFilter.copy() : null)
                .limitOutput(limitOutput != null ? limitOutput.copy() : null)
                .upperSaturationLimit(upperSaturationLimit != null ? upperSaturationLimit.copy() : null)
                .lowerSaturationLimit(lowerSaturationLimit != null ? lowerSaturationLimit.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("P", proportionalGain)
                .put("I", integralGain)
                .put("D", derivativeGain)
                .put("N", filterCoefficient)
                .put("FormulationType", formulationType)
                .put("ExternalReset", externalReset)
                .put("InitialConditionForIntegrator", initialConditionForIntegrator)
                .put("InitialConditionForFilter", initialConditionForFilter)
                .put("LimitOutput", limitOutput)
                .put("UpperSaturationLimit", upperSaturationLimit)
                .put("LowerSaturationLimit", lowerSaturationLimit)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
    
    @Override
    public boolean isValidConfiguration() {
        ValidationResult result = validate();
        return result.isValid() && 
               proportionalGain != null && integralGain != null && derivativeGain != null;
    }
    
    // ===== VERSIONING SUPPORT =====
    
    @Override
    public VersionInfo getVersionInfo() {
        return VersionInfo.of("1.0", "Initial DTO implementation");
    }
    
    @Override
    public String toString() {
        return String.format("PIDControllerDto{id=%d, name='%s', P=%.3f, I=%.3f, D=%.3f, N=%.1f}", 
                           getBlockId(), 
                           getBlockName(), 
                           getProportionalGainValue(),
                           getIntegralGainValue(),
                           getDerivativeGainValue(),
                           getFilterCoefficientValue());
    }
}