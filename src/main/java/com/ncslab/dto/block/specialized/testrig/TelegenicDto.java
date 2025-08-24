package com.ncslab.dto.block.specialized.testrig;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO representation of Telegenic block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Telegenic")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.Telegenic")
public class TelegenicDto extends BlockDto {
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}