package com.ncslab.dto.block.specialized.hardware.stm32;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of UDPReceiver block for STM32.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("STM32UDPReceiver")
@MigrationCompatible(originalClass = "com.ncslab.block.hardware.stm32.UDPReceiver")
public class UDPReceiverDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter port = TypedParameter.of(8080);
    @Builder.Default
    private TypedParameter bufferSize = TypedParameter.of(1024);
    @Builder.Default
    private TypedParameter timeout = TypedParameter.of(1000);
    
    public Integer getPortValue() {
        return port != null ? port.getAsInteger() : 8080;
    }
    
    public Integer getBufferSizeValue() {
        return bufferSize != null ? bufferSize.getAsInteger() : 1024;
    }
    
    public Integer getTimeoutValue() {
        return timeout != null ? timeout.getAsInteger() : 1000;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Port", port)
                .put("BufferSize", bufferSize)
                .put("Timeout", timeout)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}