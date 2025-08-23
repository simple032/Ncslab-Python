package com.ncslab.dto.block.specialized.hardware.stm32;

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
 * DTO representation of Config block for STM32.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Config")
@MigrationCompatible(originalClass = "com.ncslab.block.hardware.stm32.Config")
public class ConfigDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter clockFrequency = TypedParameter.of(72000000);
    @Builder.Default
    private TypedParameter boardType = TypedParameter.of("STM32F103");
    @Builder.Default
    private TypedParameter debugMode = TypedParameter.of(false);
    @Builder.Default
    private TypedParameter optimizationLevel = TypedParameter.of("O2");
    
    public Integer getClockFrequencyValue() {
        return clockFrequency != null ? clockFrequency.getAsInteger() : 72000000;
    }
    
    public String getBoardTypeValue() {
        return boardType != null ? boardType.getAsString() : "STM32F103";
    }
    
    public Boolean getDebugModeValue() {
        return debugMode != null ? debugMode.getAsBoolean() : false;
    }
    
    public String getOptimizationLevelValue() {
        return optimizationLevel != null ? optimizationLevel.getAsString() : "O2";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ClockFrequency", clockFrequency)
                .put("BoardType", boardType)
                .put("DebugMode", debugMode)
                .put("OptimizationLevel", optimizationLevel)
                .build();
    }
}