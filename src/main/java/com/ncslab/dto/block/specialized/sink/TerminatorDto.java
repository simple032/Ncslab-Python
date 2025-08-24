package com.ncslab.dto.block.specialized.sink;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.sink.SinkDto;
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
 * DTO representation of Terminator sink block.
 * 
 * The Terminator block terminates signals without affecting simulation.
 * It's used to eliminate unconnected output ports that would otherwise
 * cause warnings or errors.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Terminator")
@MigrationCompatible(originalClass = "com.ncslab.block.sink.Terminator")
public class TerminatorDto extends SinkDto {
    
    // Terminator blocks only terminate signals - no additional parameters needed
    // sampleTime is inherited from BlockDto (via SinkDto)
    
    // ===== PARAMETER ACCESS HELPERS =====
    // All parameter access methods are inherited from SinkDto
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
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
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }
    
    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }
    
    @Override
    public TerminatorDto copy() {
        return TerminatorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", sampleTime)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("TerminatorDto{id=%d, name='%s', type='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType());
    }
}