package com.ncslab.dto.block.specialized.testrig;

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
 * DTO representation of DCMotorAngleDirect block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DCMotorAngleDirect")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.DCMotorAngleDirect")
public class DCMotorAngleDirectDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter motorK = TypedParameter.of(106.25);
    @Builder.Default
    private TypedParameter motorT = TypedParameter.of(0.07);
    @Builder.Default
    private TypedParameter port = TypedParameter.of("\"/dev/ttyAMA0\"");
    @Builder.Default
    private TypedParameter baudrate = TypedParameter.of(115200);
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    public Double getMotorKValue() {
        return motorK != null ? motorK.getAsDouble() : 106.25;
    }
    
    public Double getMotorTValue() {
        return motorT != null ? motorT.getAsDouble() : 0.07;
    }
    
    public String getPortValue() {
        return port != null ? port.getAsString() : "\"/dev/ttyAMA0\"";
    }
    
    public Integer getBaudrateValue() {
        return baudrate != null ? baudrate.getAsInteger() : 115200;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("motorK", motorK)
                .put("motorT", motorT)
                .put("port", port)
                .put("baudrate", baudrate)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }
}