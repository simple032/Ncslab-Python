package com.ncslab.dto.block.specialized.hardware;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for UDP Sender for STM32 block.
 * Sends UDP packets from STM32 microcontroller.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("UDPSenderForStm32")
public class UDPSenderForStm32Dto extends BlockDto {
    
    /**
     * Remote host IP address.
     * Default: "192.168.1.100"
     */
    private TypedParameter remoteIPAddress;
    
    /**
     * Remote port number.
     * Default: 25001
     */
    private TypedParameter remotePort;
    
    /**
     * Local port number for sending.
     * Default: 25000
     */
    private TypedParameter localPort;
    
    /**
     * Sample time for packet transmission.
     * Default: 0.01
     */
    private TypedParameter sampleTime;
    
    /**
     * Maximum packet size in bytes.
     * Default: 1024
     */
    private TypedParameter maxPacketSize;
    
    public UDPSenderForStm32Dto(String blockName, String blockPath) {
        super(blockName,blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (remoteIPAddress == null) {
            remoteIPAddress = TypedParameter.of("192.168.1.100");
        }
        if (remotePort == null) {
            remotePort = TypedParameter.of(25001);
        }
        if (localPort == null) {
            localPort = TypedParameter.of(25000);
        }
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(0.01);
        }
        if (maxPacketSize == null) {
            maxPacketSize = TypedParameter.of(1024);
        }
    }
}