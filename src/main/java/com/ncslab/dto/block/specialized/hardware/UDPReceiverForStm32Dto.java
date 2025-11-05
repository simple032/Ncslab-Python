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
 * DTO for UDP Receiver for STM32 block.
 * Receives UDP packets on STM32 microcontroller.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("UDPReceiverForStm32")
public class UDPReceiverForStm32Dto extends BlockDto {
    
    /**
     * Local port number for receiving UDP packets.
     * Default: 25000
     */
    private TypedParameter localPort;
    
    /**
     * Sample time for packet reception.
     * Default: 0.01
     */
    private TypedParameter sampleTime;
    
    /**
     * Maximum packet size in bytes.
     * Default: 1024
     */
    private TypedParameter maxPacketSize;
    
    /**
     * Timeout for receive operation in milliseconds.
     * Default: 100
     */
    private TypedParameter receiveTimeout;
    
    public UDPReceiverForStm32Dto(String blockName, String blockPath) {
        super(blockName,blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (localPort == null) {
            localPort = TypedParameter.of(25000);
        }
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(0.01);
        }
        if (maxPacketSize == null) {
            maxPacketSize = TypedParameter.of(1024);
        }
        if (receiveTimeout == null) {
            receiveTimeout = TypedParameter.of(100);
        }
    }
}