package com.ncslab.dto.block.specialized.hardware.stm32;

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
 * DTO representation of UDPSender block for STM32.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("STM32UDPSender")
@MigrationCompatible(originalClass = "com.ncslab.block.hardware.stm32.UDPSender")
public class UDPSenderDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter remoteAddress = TypedParameter.of("192.168.1.100");
    @Builder.Default
    private TypedParameter remotePort = TypedParameter.of(8080);
    @Builder.Default
    private TypedParameter localPort = TypedParameter.of(8081);
    
    public String getRemoteAddressValue() {
        return remoteAddress != null ? remoteAddress.getAsString() : "192.168.1.100";
    }
    
    public Integer getRemotePortValue() {
        return remotePort != null ? remotePort.getAsInteger() : 8080;
    }
    
    public Integer getLocalPortValue() {
        return localPort != null ? localPort.getAsInteger() : 8081;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("RemoteAddress", remoteAddress)
                .put("RemotePort", remotePort)
                .put("LocalPort", localPort)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}