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
 * DTO representation of TransportDelay block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Transport Delay block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - DelayTime: Delay time (scalar or matrix)
 * - InitialOutput: Initial output value  
 * - BufferSize: Size of the delay buffer for variable step solvers
 * - PadeOrder: Pade approximation order
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
@JsonTypeName("TransportDelay")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.TransportDelay")
public class TransportDelayDto extends BlockDto implements VersionedDto {
    
    // Constructor for Jackson deserialization
    
    // ===== TRANSPORT DELAY SPECIFIC PARAMETERS =====
    
    /**
     * Delay time (scalar or matrix)
     * Default: 1.0
     * Validation: Must be positive
     */
    @Builder.Default
    private TypedParameter delayTime = TypedParameter.of(1.0);
    
    /**
     * Initial output value
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialOutput = TypedParameter.of(0.0);
    
    /**
     * Size of the delay buffer for variable step solvers
     * Default: 1024
     * Validation: Must be positive integer
     */
    @Builder.Default
    private TypedParameter bufferSize = TypedParameter.of(1024);
    
    /**
     * Pade approximation order
     * Default: 0 (no approximation)
     * Validation: Must be non-negative integer
     */
    @Builder.Default
    private TypedParameter padeOrder = TypedParameter.of(0);
    
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
    
    public Double getDelayTimeValue() {
        return delayTime != null ? delayTime.getAsDouble() : 1.0;
    }
    
    public Double getInitialOutputValue() {
        return initialOutput != null ? initialOutput.getAsDouble() : 0.0;
    }
    
    public Integer getBufferSizeValue() {
        return bufferSize != null ? bufferSize.getAsInteger() : 1024;
    }
    
    public Integer getPadeOrderValue() {
        return padeOrder != null ? padeOrder.getAsInteger() : 0;
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
        
        // Validate delay time is positive
        if (delayTime != null) {
            Double dtValue = delayTime.getAsDouble();
            if (dtValue != null && dtValue <= 0.0) {
                result.addError("Delay time must be positive");
            }
        }
        
        // Validate buffer size is positive integer
        if (bufferSize != null) {
            Integer bsValue = bufferSize.getAsInteger();
            if (bsValue != null && bsValue <= 0) {
                result.addError("Buffer size must be positive");
            }
        }
        
        // Validate Pade order is non-negative
        if (padeOrder != null) {
            Integer poValue = padeOrder.getAsInteger();
            if (poValue != null && poValue < 0) {
                result.addError("Pade order must be non-negative");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    @Override
    public TransportDelayDto copy() {
        return TransportDelayDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .delayTime(delayTime != null ? delayTime.copy() : null)
                .initialOutput(initialOutput != null ? initialOutput.copy() : null)
                .bufferSize(bufferSize != null ? bufferSize.copy() : null)
                .padeOrder(padeOrder != null ? padeOrder.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("DelayTime", delayTime)
                .put("InitialOutput", initialOutput)
                .put("BufferSize", bufferSize)
                .put("PadeOrder", padeOrder)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
    
    @Override
    public boolean isValidConfiguration() {
        ValidationResult result = validate();
        return result.isValid() && delayTime != null && getDelayTimeValue() > 0.0;
    }
    
    // ===== VERSIONING SUPPORT =====
    
    @Override
    public VersionInfo getVersionInfo() {
        return VersionInfo.of("1.0", "Initial DTO implementation");
    }
    
    @Override
    public String toString() {
        return String.format("TransportDelayDto{id=%d, name='%s', delayTime=%.3f, bufferSize=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getDelayTimeValue(),
                           getBufferSizeValue());
    }
}