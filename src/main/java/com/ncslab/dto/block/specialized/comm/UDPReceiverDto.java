package com.ncslab.dto.block.specialized.comm;

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
 * DTO representation of UDPReceiver block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("UDPReceiver")
@MigrationCompatible(originalClass = "com.ncslab.block.comm.UDPReceiver")
public class UDPReceiverDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter address = TypedParameter.of("127.0.0.1");
    @Builder.Default
    private TypedParameter port = TypedParameter.of(8080);
    
    public String getAddressValue() {
        return address != null ? address.getAsString() : "127.0.0.1";
    }
    
    public Integer getPortValue() {
        return port != null ? port.getAsInteger() : 8080;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Address", address)
                .put("Port", port)
                .build();
    }
}