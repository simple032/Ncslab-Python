package com.ncslab.dto.block.specialized.comm;

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
 * DTO representation of SerialReceive block (refactored version following SIMULINK R2024b).
 *
 * Receives data over serial port configured by a SerialConfiguration block.
 * Multiple SerialReceive blocks can share the same SerialConfiguration port.
 *
 * Parameters:
 * - Port: Name of SerialConfiguration block to use
 * - SampleTime: Sample time for receiving data (seconds)
 * - DataSize: Number of double values to receive (1 = scalar, >1 = vector)
 * - Header: Optional header bytes to skip (comma-separated hex values)
 * - Terminator: Optional terminator bytes to skip (comma-separated hex values)
 * - Blocking: Blocking mode ("on" = wait for data, "off" = non-blocking)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SerialReceive")
@MigrationCompatible(originalClass = "com.ncslab.block.comm.SerialReceive")
public class SerialReceiveDto extends BlockDto {

    @Builder.Default
    private TypedParameter port = TypedParameter.of("SerialConfig1");

    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(0.1);

    @Builder.Default
    private TypedParameter dataSize = TypedParameter.of(1);

    @Builder.Default
    private TypedParameter header = TypedParameter.of("");

    @Builder.Default
    private TypedParameter terminator = TypedParameter.of("");

    @Builder.Default
    private TypedParameter blocking = TypedParameter.of("on");

    /**
     * Get port reference value
     */
    public String getPortValue() {
        return port != null ? port.getAsString() : "SerialConfig1";
    }

    /**
     * Get sample time value
     */
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : 0.1;
    }

    /**
     * Get data size value
     */
    public Integer getDataSizeValue() {
        return dataSize != null ? dataSize.getAsInteger() : 1;
    }

    /**
     * Get header bytes value
     */
    public String getHeaderValue() {
        return header != null ? header.getAsString() : "";
    }

    /**
     * Get terminator bytes value
     */
    public String getTerminatorValue() {
        return terminator != null ? terminator.getAsString() : "";
    }

    /**
     * Get blocking mode value
     */
    public String getBlockingValue() {
        return blocking != null ? blocking.getAsString() : "on";
    }

    @Override
    public String getBlockType() {
        return "SerialReceive";
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Port", port)
                .put("SampleTime", sampleTime)
                .put("DataSize", dataSize)
                .put("Header", header)
                .put("Terminator", terminator)
                .put("Blocking", blocking)
                .build();
    }
}
