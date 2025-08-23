package com.ncslab.dto.block.specialized.driver;

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
 * DTO representation of EtherCATAO block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("EtherCATAO")
@MigrationCompatible(originalClass = "com.ncslab.block.driver.EtherCATAO")
public class EtherCATAODto extends BlockDto {
    
    @Builder.Default
    private TypedParameter slaveID = TypedParameter.of(1);
    @Builder.Default
    private TypedParameter interfaceName = TypedParameter.of("eth0");
    @Builder.Default
    private TypedParameter timeSample = TypedParameter.of(0.001);
    
    public Integer getSlaveIDValue() {
        return slaveID != null ? slaveID.getAsInteger() : 1;
    }
    
    public String getInterfaceNameValue() {
        return interfaceName != null ? interfaceName.getAsString() : "eth0";
    }
    
    public Double getTimeSampleValue() {
        return timeSample != null ? timeSample.getAsDouble() : 0.001;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SlaveID", slaveID)
                .put("Interface", interfaceName)
                .put("timeSample", timeSample)
                .build();
    }
}