package com.ncslab.dto.block.specialized.advancedControl;

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
 * DTO representation of LQRController block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LQRController")
@MigrationCompatible(originalClass = "com.ncslab.block.advancedControl.LQRController")
public class LQRControllerDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter A = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter B = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter Q = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter R = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter N = TypedParameter.of("[0]");
    
    public String getAValue() {
        return A != null ? A.getAsString() : "[1]";
    }
    
    public String getBValue() {
        return B != null ? B.getAsString() : "[1]";
    }
    
    public String getQValue() {
        return Q != null ? Q.getAsString() : "[1]";
    }
    
    public String getRValue() {
        return R != null ? R.getAsString() : "[1]";
    }
    
    public String getNValue() {
        return N != null ? N.getAsString() : "[0]";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("A", A)
                .put("B", B)
                .put("Q", Q)
                .put("R", R)
                .put("N", N)
                .build();
    }
}