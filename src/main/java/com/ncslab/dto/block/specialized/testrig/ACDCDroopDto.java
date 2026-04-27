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
 * DTO representation of ACDCDroop block (three-phase AC/DC rectifier with droop control).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ACDCDroop")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.ACDCDroop")
public class ACDCDroopDto extends BlockDto {

    @Builder.Default
    private TypedParameter mp = TypedParameter.of(0.0001);
    @Builder.Default
    private TypedParameter mq = TypedParameter.of(0.0016);
    @Builder.Default
    private TypedParameter fn = TypedParameter.of(50.0);
    @Builder.Default
    private TypedParameter vn = TypedParameter.of(311.0);
    @Builder.Default
    private TypedParameter vdc_ref = TypedParameter.of(700.0);
    @Builder.Default
    private TypedParameter lf = TypedParameter.of(0.001732);
    @Builder.Default
    private TypedParameter rf = TypedParameter.of(0.01);
    @Builder.Default
    private TypedParameter cdc = TypedParameter.of(0.0022);
    @Builder.Default
    private TypedParameter kp_v = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter ki_v = TypedParameter.of(2.5);
    @Builder.Default
    private TypedParameter kp_i = TypedParameter.of(2.5);
    @Builder.Default
    private TypedParameter ki_i = TypedParameter.of(1.0);

    public Double getMpValue() {
        return mp != null ? mp.getAsDouble() : 0.0001;
    }

    public Double getMqValue() {
        return mq != null ? mq.getAsDouble() : 0.0016;
    }

    public Double getFnValue() {
        return fn != null ? fn.getAsDouble() : 50.0;
    }

    public Double getVnValue() {
        return vn != null ? vn.getAsDouble() : 311.0;
    }

    public Double getVdcRefValue() {
        return vdc_ref != null ? vdc_ref.getAsDouble() : 700.0;
    }

    public Double getLfValue() {
        return lf != null ? lf.getAsDouble() : 0.001732;
    }

    public Double getRfValue() {
        return rf != null ? rf.getAsDouble() : 0.01;
    }

    public Double getCdcValue() {
        return cdc != null ? cdc.getAsDouble() : 0.0022;
    }

    public Double getKpVValue() {
        return kp_v != null ? kp_v.getAsDouble() : 0.5;
    }

    public Double getKiVValue() {
        return ki_v != null ? ki_v.getAsDouble() : 2.5;
    }

    public Double getKpIValue() {
        return kp_i != null ? kp_i.getAsDouble() : 2.5;
    }

    public Double getKiIValue() {
        return ki_i != null ? ki_i.getAsDouble() : 1.0;
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("mp", mp)
                .put("mq", mq)
                .put("fn", fn)
                .put("Vn", vn)
                .put("Vdc_ref", vdc_ref)
                .put("Lf", lf)
                .put("Rf", rf)
                .put("Cdc", cdc)
                .put("Kp_v", kp_v)
                .put("Ki_v", ki_v)
                .put("Kp_i", kp_i)
                .put("Ki_i", ki_i)
                .build();
    }
}
