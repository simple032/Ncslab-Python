package com.ncslab.dto.block.specialized.route;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Arrays;

/**
 * DTO representation of Switch route block.
 * 
 * The Switch block passes through one of two inputs based on the value of a
 * third control input. The control input is compared to a threshold using
 * the specified criteria. If the condition is true, the first input is passed
 * through; otherwise, the third input is passed through.
 * 
 * Port Configuration:
 * - Input 1: First data input (passed when condition is true)
 * - Input 2: Control signal (compared against threshold)
 * - Input 3: Second data input (passed when condition is false)
 * - Output: Selected input signal
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Switch")
@MigrationCompatible(originalClass = "com.ncslab.block.route.Switch")
public class SwitchDto extends BlockDto {
    
    /**
     * Threshold value for switching condition
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter threshold = TypedParameter.of(0.0);
    
    /**
     * Switching criteria
     * Default: ">="
     * Options: ">=", ">", "~=" (not equal)
     */
    @Builder.Default
    private TypedParameter criteria = TypedParameter.of(">=");
    
    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // Valid switching criteria
    private static final List<String> VALID_CRITERIA = Arrays.asList(">=", ">", "~=");
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getThresholdValue() {
        return threshold != null ? threshold.getAsDouble() : 0.0;
    }
    
    public String getCriteriaValue() {
        return criteria != null ? criteria.getAsString() : ">=";
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // Legacy compatibility method
    public String getRelopValue() {
        return getCriteriaValue();
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate threshold
        if (threshold != null) {
            Double threshVal = threshold.getAsDouble();
            if (threshVal != null && (Double.isNaN(threshVal) || Double.isInfinite(threshVal))) {
                result.addError("Threshold value must be finite");
            }
        }
        
        // Validate criteria
        if (criteria != null) {
            String criteriaStr = criteria.getAsString();
            if (criteriaStr != null && !VALID_CRITERIA.contains(criteriaStr)) {
                result.addError("Switching criteria must be one of: " + VALID_CRITERIA);
            }
        }
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }
    
    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }
    
    /**
     * Get the number of input ports (always 3 for Switch block)
     */
    public int getInputPortCount() {
        return 3; // in1 (data), in2 (control), in3 (data)
    }
    
    /**
     * Get the number of output ports (always 1 for Switch block)
     */
    public int getOutputPortCount() {
        return 1; // out1
    }
    
    /**
     * Check if criteria is greater than or equal
     */
    public boolean isGreaterThanOrEqual() {
        return ">=".equals(getCriteriaValue());
    }
    
    /**
     * Check if criteria is strictly greater than
     */
    public boolean isGreaterThan() {
        return ">".equals(getCriteriaValue());
    }
    
    /**
     * Check if criteria is not equal
     */
    public boolean isNotEqual() {
        return "~=".equals(getCriteriaValue());
    }
    
    /**
     * Evaluate switching condition for given control value
     */
    public boolean evaluateCondition(double controlValue) {
        double thresh = getThresholdValue();
        String crit = getCriteriaValue();
        
        switch (crit) {
            case ">=":
                return controlValue >= thresh;
            case ">":
                return controlValue > thresh;
            case "~=":
                return Math.abs(controlValue - thresh) > 1e-10; // Numerical tolerance for not-equal
            default:
                return false;
        }
    }
    
    /**
     * Determine which input should be selected for given control value
     * @return 1 if first input should be selected, 3 if third input should be selected
     */
    public int getSelectedInputPort(double controlValue) {
        return evaluateCondition(controlValue) ? 1 : 3;
    }
    
    @Override
    public SwitchDto copy() {
        return SwitchDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .threshold(threshold != null ? threshold.copy() : null)
                .criteria(criteria != null ? criteria.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Threshold", threshold)
                .put("Criteria", criteria)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("SwitchDto{id=%d, name='%s', type='%s', threshold=%s, criteria='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getThresholdValue(), 
                           getCriteriaValue());
    }
}