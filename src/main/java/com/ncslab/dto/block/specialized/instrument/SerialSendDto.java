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
 * DTO representation of SerialSend block (refactored version following SIMULINK R2024b).
 *
 * Sends data over serial port configured by a SerialConfiguration block.
 * Multiple SerialSend blocks can share the same SerialConfiguration port.
 *
 * Parameters:
 * - Port: Name of SerialConfiguration block to use
 * - Header: Optional header bytes (comma-separated hex values, e.g., "0xFF,0xFE")
 * - Terminator: Optional terminator bytes (comma-separated hex values)
 * - Blocking: Blocking mode ("on" = wait for completion, "off" = non-blocking)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SerialSend")
@MigrationCompatible(originalClass = "com.ncslab.block.comm.SerialSend")
public class SerialSendDto extends BlockDto {

    @Builder.Default
    private TypedParameter port = TypedParameter.of("SerialConfig1");

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
        return "SerialSend";
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Port", port)
                .put("Header", header)
                .put("Terminator", terminator)
                .put("Blocking", blocking)
                .build();
    }
}
