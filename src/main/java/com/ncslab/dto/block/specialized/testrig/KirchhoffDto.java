package com.ncslab.dto.block.specialized.testrig;

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
 * DTO representation of Kirchhoff block.
 * Contains BCM parameter and AD1-AD7 analog output parameters.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Kirchhoff")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.Kirchhoff")
public class KirchhoffDto extends BlockDto {
    
    @Builder.Default
    private String bcm = "18";
    
    @Builder.Default
    private String ad1 = "0";
    
    @Builder.Default
    private String ad2 = "1";
    
    @Builder.Default
    private String ad3 = "2";
    
    @Builder.Default
    private String ad4 = "3";
    
    @Builder.Default
    private String ad5 = "4";
    
    @Builder.Default
    private String ad6 = "5";
    
    @Builder.Default
    private String ad7 = "6";
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("BCM", bcm != null ? TypedParameter.of(bcm) : TypedParameter.of("18"))
                .put("AD1", ad1 != null ? TypedParameter.of(ad1) : TypedParameter.of("0"))
                .put("AD2", ad2 != null ? TypedParameter.of(ad2) : TypedParameter.of("1"))
                .put("AD3", ad3 != null ? TypedParameter.of(ad3) : TypedParameter.of("2"))
                .put("AD4", ad4 != null ? TypedParameter.of(ad4) : TypedParameter.of("3"))
                .put("AD5", ad5 != null ? TypedParameter.of(ad5) : TypedParameter.of("4"))
                .put("AD6", ad6 != null ? TypedParameter.of(ad6) : TypedParameter.of("5"))
                .put("AD7", ad7 != null ? TypedParameter.of(ad7) : TypedParameter.of("6"))
                .build();
    }
}