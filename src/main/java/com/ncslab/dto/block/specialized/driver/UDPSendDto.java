package com.ncslab.dto.block.specialized.driver;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for UDP Send block.
 * Sends UDP packets to a remote host.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UDPSendDto extends BlockDto {
    
    /**
     * Remote host IP address.
     * Default: "127.0.0.1"
     */
    private TypedParameter remoteIPAddress;
    
    /**
     * Remote port number.
     * Default: 25000
     */
    private TypedParameter remotePort;
    
    /**
     * Local port number for sending.
     * Default: 25001
     */
    private TypedParameter localPort;
    
    /**
     * Sample time for packet transmission.
     * Default: 0.1
     */
    private TypedParameter sampleTime;
    
    public UDPSendDto(String blockName, String blockPath) {
        super("UDPSend", blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (remoteIPAddress == null) {
            remoteIPAddress = TypedParameter.of("127.0.0.1");
        }
        if (remotePort == null) {
            remotePort = TypedParameter.of(25000);
        }
        if (localPort == null) {
            localPort = TypedParameter.of(25001);
        }
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(0.1);
        }
    }
}