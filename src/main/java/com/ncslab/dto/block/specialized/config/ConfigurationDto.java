package com.ncslab.dto.block.specialized.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Configuration")
@MigrationCompatible(originalClass = "com.ncslab.block.config.Configuration")
public class ConfigurationDto extends BlockDto {

    @Override
    public ConfigurationDto copy() {
        ConfigurationDto copy = new ConfigurationDto();
        copyBaseFieldsTo(copy);
        return copy;
    }

    @Override
    public ValidationResult validate() {
        return ValidationResult.success();
    }
}
