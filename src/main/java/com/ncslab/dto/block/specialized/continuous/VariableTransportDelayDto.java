package com.ncslab.dto.block.specialized.continuous;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of VariableTransportDelay block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("VariableTransportDelay")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.VariableTransportDelay")
public class VariableTransportDelayDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter maximumDelay = TypedParameter.of(10.0);
    @Builder.Default
    private TypedParameter initialInput = TypedParameter.of(0.0);
    @Builder.Default
    private TypedParameter bufferSize = TypedParameter.of(1024);
    @Builder.Default
    private TypedParameter padeOrder = TypedParameter.of(0);
    
    public Double getMaximumDelayValue() {
        return maximumDelay != null ? maximumDelay.getAsDouble() : 10.0;
    }
    
    public Double getInitialInputValue() {
        return initialInput != null ? initialInput.getAsDouble() : 0.0;
    }
    
    public Integer getBufferSizeValue() {
        return bufferSize != null ? bufferSize.getAsInteger() : 1024;
    }
    
    public Integer getPadeOrderValue() {
        return padeOrder != null ? padeOrder.getAsInteger() : 0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("MaximumDelay", maximumDelay)
                .put("InitialInput", initialInput)
                .put("BufferSize", bufferSize)
                .put("PadeOrder", padeOrder)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}