package com.ncslab.dto.block.specialized.instrument;

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
 * DTO representation of SerialConfiguration block.
 * Centralized serial port hardware resource management following SIMULINK R2024b architecture.
 *
 * This block configures a serial port that can be shared by multiple SerialSend and
 * SerialReceive blocks, preventing port conflicts through centralized resource management.
 *
 * Parameters:
 * - Port: Serial port identifier (e.g., "COM1", "/dev/ttyUSB0")
 * - BaudRate: Communication speed (110-921600 bps, default 9600)
 * - DataBits: Number of data bits (5-8, default 8)
 * - Parity: Parity checking ("none", "odd", "even", "mark", "space")
 * - StopBits: Stop bits ("1", "1.5", "2")
 * - ByteOrder: Byte order ("LittleEndian", "BigEndian")
 * - FlowControl: Flow control ("none", "hardware", "software")
 * - Timeout: Read timeout in seconds (-1 for blocking, 0 for non-blocking, >0 for timeout)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SerialConfiguration")
@MigrationCompatible(originalClass = "com.ncslab.block.comm.SerialConfiguration")
public class SerialConfigurationDto extends BlockDto {

    @Builder.Default
    private TypedParameter port = TypedParameter.of("COM1");

    @Builder.Default
    private TypedParameter baudRate = TypedParameter.of(9600);

    @Builder.Default
    private TypedParameter dataBits = TypedParameter.of(8);

    @Builder.Default
    private TypedParameter parity = TypedParameter.of("none");

    @Builder.Default
    private TypedParameter stopBits = TypedParameter.of("1");

    @Builder.Default
    private TypedParameter byteOrder = TypedParameter.of("LittleEndian");

    @Builder.Default
    private TypedParameter flowControl = TypedParameter.of("none");

    @Builder.Default
    private TypedParameter timeout = TypedParameter.of(10.0);

    /**
     * Get port identifier value
     */
    public String getPortValue() {
        return port != null ? port.getAsString() : "COM1";
    }

    /**
     * Get baud rate value
     */
    public Integer getBaudRateValue() {
        return baudRate != null ? baudRate.getAsInteger() : 9600;
    }

    /**
     * Get data bits value
     */
    public Integer getDataBitsValue() {
        return dataBits != null ? dataBits.getAsInteger() : 8;
    }

    /**
     * Get parity value
     */
    public String getParityValue() {
        return parity != null ? parity.getAsString() : "none";
    }

    /**
     * Get stop bits value
     */
    public String getStopBitsValue() {
        return stopBits != null ? stopBits.getAsString() : "1";
    }

    /**
     * Get byte order value
     */
    public String getByteOrderValue() {
        return byteOrder != null ? byteOrder.getAsString() : "LittleEndian";
    }

    /**
     * Get flow control value
     */
    public String getFlowControlValue() {
        return flowControl != null ? flowControl.getAsString() : "none";
    }

    /**
     * Get timeout value
     */
    public Double getTimeoutValue() {
        return timeout != null ? timeout.getAsDouble() : 10.0;
    }

    @Override
    public String getBlockType() {
        return "SerialConfiguration";
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Port", port)
                .put("BaudRate", baudRate)
                .put("DataBits", dataBits)
                .put("Parity", parity)
                .put("StopBits", stopBits)
                .put("ByteOrder", byteOrder)
                .put("FlowControl", flowControl)
                .put("Timeout", timeout)
                .build();
    }
}
