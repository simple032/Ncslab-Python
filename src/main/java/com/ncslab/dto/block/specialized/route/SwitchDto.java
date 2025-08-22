package com.ncslab.dto.block.specialized.route;

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

/**
 * DTO representation of Switch block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Switch block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - Threshold: Threshold value for switching condition
 * - Criteria: Switching criteria (>=, >, ~=)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * Block Logic:
 * - Input 1: First data input (used when condition is false)
 * - Input 2: Control signal (compared against threshold)
 * - Input 3: Second data input (used when condition is true)
 * - Output: Input 1 or Input 3 based on (Input 2 criteria Threshold)
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.route.Switch")
public class SwitchDto extends BlockDto {
    
    // ===== SWITCH BLOCK SPECIFIC PARAMETERS =====
    
    /**
     * Threshold value for switching condition
     * Default: 0
     * Validation: Must be finite
     */
    private TypedParameter threshold;
    
    /**
     * Switching criteria
     * Default: ">="
     * Options: ">=", ">", "~="
     */
    private TypedParameter criteria;
    
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
    
    // ===== FACTORY METHODS =====
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static SwitchDto fromLegacyParameters(Map<String, String> params) {
        SwitchDto.SwitchDtoBuilder builder = SwitchDto.builder()
                .blockName(params.getOrDefault("blockName", "Switch"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("Switch");
        
        SwitchDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("Threshold")) {
            dto.threshold = TypedParameter.of(Double.parseDouble(params.get("Threshold")));
        }
        
        if (params.containsKey("Criteria")) {
            dto.criteria = TypedParameter.of(params.get("Criteria"));
        } else if (params.containsKey("Relop")) {
            // Legacy compatibility - 'Relop' parameter
            dto.criteria = TypedParameter.of(params.get("Relop"));
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
    public static SwitchDtoBuilder builderWithDefaults() {
        SwitchDto dto = SwitchDto.builder()
                .blockType("Switch")
                .sampleTime(-1.0)
                .build();
        
        // Set default typed parameters
        dto.threshold = TypedParameter.of(0.0);
        dto.criteria = TypedParameter.of(">=");
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        
        return SwitchDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .threshold(dto.threshold)
                .criteria(dto.criteria)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getThresholdValue() {
        return threshold != null ? threshold.getValue(Double.class) : 0.0;
    }
    
    public String getCriteriaValue() {
        return criteria != null ? criteria.getValue(String.class) : ">=";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getValue(String.class) : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate threshold
        if (threshold != null) {
            Double threshVal = threshold.getValue(Double.class);
            if (threshVal != null && (Double.isNaN(threshVal) || Double.isInfinite(threshVal))) {
                result.addError("Threshold value must be finite");
            }
        }
        
        // Validate criteria
        if (criteria != null) {
            String criteriaStr = criteria.getValue(String.class);
            if (criteriaStr != null) {
                List<String> validCriteria = List.of(">=", ">", "~=");
                if (!validCriteria.contains(criteriaStr)) {
                    result.addError("Switching criteria must be one of: " + validCriteria);
                }
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
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
    
    @Override
    public SwitchDto copy() {
        return SwitchDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .threshold(threshold != null ? threshold.copy() : null)
                .criteria(criteria != null ? criteria.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (threshold != null) {
            params.put("Threshold", String.valueOf(threshold.getValue()));
        }
        if (criteria != null) {
            params.put("Criteria", String.valueOf(criteria.getValue()));
            params.put("Relop", String.valueOf(criteria.getValue())); // Legacy compatibility
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
        return String.format("SwitchDto{id=%d, name='%s', type='%s', threshold=%s, criteria='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getThresholdValue(), 
                           getCriteriaValue());
    }
}