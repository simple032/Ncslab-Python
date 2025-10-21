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
 * DTO representation of SerialSender block.
 * Sends data over serial communication (RS232/UART).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SerialSender")
@MigrationCompatible(originalClass = "com.ncslab.block.comm.SerialSender")
public class SerialSenderDto extends BlockDto {

    @Builder.Default
    private TypedParameter portName = TypedParameter.of("COM1");

    @Builder.Default
    private TypedParameter baudRate = TypedParameter.of(9600);

    @Builder.Default
    private TypedParameter dataBits = TypedParameter.of(8);

    @Builder.Default
    private TypedParameter stopBits = TypedParameter.of(1);

    @Builder.Default
    private TypedParameter parity = TypedParameter.of("None");

    public String getPortNameValue() {
        return portName != null ? portName.getAsString() : "COM1";
    }

    public Integer getBaudRateValue() {
        return baudRate != null ? baudRate.getAsInteger() : 9600;
    }

    public Integer getDataBitsValue() {
        return dataBits != null ? dataBits.getAsInteger() : 8;
    }

    public Integer getStopBitsValue() {
        return stopBits != null ? stopBits.getAsInteger() : 1;
    }

    public String getParityValue() {
        return parity != null ? parity.getAsString() : "None";
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("PortName", portName)
                .put("BaudRate", baudRate)
                .put("DataBits", dataBits)
                .put("StopBits", stopBits)
                .put("Parity", parity)
                .build();
    }
}
