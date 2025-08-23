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
 * DTO representation of Observer block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Observer")
@MigrationCompatible(originalClass = "com.ncslab.block.driver.Observer")
public class ObserverDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter A = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter B = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter C = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter L = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter initialCondition = TypedParameter.of("[0]");
    
    public String getAValue() {
        return A != null ? A.getAsString() : "[1]";
    }
    
    public String getBValue() {
        return B != null ? B.getAsString() : "[1]";
    }
    
    public String getCValue() {
        return C != null ? C.getAsString() : "[1]";
    }
    
    public String getLValue() {
        return L != null ? L.getAsString() : "[1]";
    }
    
    public String getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getAsString() : "[0]";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("A", A)
                .put("B", B)
                .put("C", C)
                .put("L", L)
                .put("InitialCondition", initialCondition)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}