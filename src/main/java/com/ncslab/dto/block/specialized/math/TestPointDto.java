package com.ncslab.dto.block.specialized.math;

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

/**
 * DTO representation of TestPoint block for signal monitoring and testing.
 * 
 * The TestPoint block provides a test point for monitoring signals without
 * affecting the signal flow. It's commonly used for debugging and testing.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TestPoint")
@MigrationCompatible(originalClass = "com.ncslab.block.math.TestPoint")
public class TestPointDto extends BlockDto {
    
    /**
     * Test point tag name for identification
     * Default: "TP"
     */
    @Builder.Default
    private TypedParameter testTag = TypedParameter.of("TP");
    
    /**
     * Enable data logging for this test point
     * Default: true
     */
    @Builder.Default
    private TypedParameter enableLogging = TypedParameter.of(true);
    
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
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getTestTagValue() {
        return testTag != null ? testTag.getAsString() : "TP";
    }
    
    public Boolean getEnableLoggingValue() {
        return enableLogging != null ? enableLogging.getAsBoolean() : true;
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate test tag
        if (testTag == null || testTag.getAsString() == null || testTag.getAsString().trim().isEmpty()) {
            result.addError("Test tag cannot be empty");
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
     * Check if logging is enabled for this test point
     */
    public boolean isLoggingEnabled() {
        return getEnableLoggingValue();
    }
    
    @Override
    public TestPointDto copy() {
        return TestPointDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .testTag(testTag != null ? testTag.copy() : null)
                .enableLogging(enableLogging != null ? enableLogging.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("TestTag", testTag)
                .put("EnableLogging", enableLogging)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("TestPointDto{id=%d, name='%s', type='%s', tag='%s', logging=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getTestTagValue(),
                           isLoggingEnabled());
    }
}