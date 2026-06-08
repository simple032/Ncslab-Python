package com.ncslab.dto.block.specialized.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Configuration Block (组态模块)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Configuration")
@MigrationCompatible(originalClass = "com.ncslab.block.config.Configuration")
public class ConfigurationDto extends BlockDto {

    @Override
    public ConfigurationDto copy() {
        return ConfigurationDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .build();
    }

    @Override
    public ValidationResult validate() {
        return new ValidationResult();
    }
}
