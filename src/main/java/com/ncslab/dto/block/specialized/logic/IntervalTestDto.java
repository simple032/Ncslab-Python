package com.ncslab.dto.block.specialized.logic;

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
 * DTO representation of IntervalTest block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("IntervalTest")
@MigrationCompatible(originalClass = "com.ncslab.block.logicAndBit.IntervalTest")
public class IntervalTestDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter lowerBound = TypedParameter.of(-1.0);
    @Builder.Default
    private TypedParameter upperBound = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter intervalClosing = TypedParameter.of("[]");
    
    public Double getLowerBoundValue() {
        return lowerBound != null ? lowerBound.getAsDouble() : -1.0;
    }
    
    public Double getUpperBoundValue() {
        return upperBound != null ? upperBound.getAsDouble() : 1.0;
    }
    
    public String getIntervalClosingValue() {
        return intervalClosing != null ? intervalClosing.getAsString() : "[]";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("LowerBound", lowerBound)
                .put("UpperBound", upperBound)
                .put("IntervalClosing", intervalClosing)
                .build();
    }
}