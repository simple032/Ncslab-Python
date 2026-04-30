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

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("AverageRectifier")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.AverageRectifier")
public class AverageRectifierDto extends BlockDto {

    /* DC link */
    @Builder.Default
    private TypedParameter vdc_ref = TypedParameter.of(700.0);
    @Builder.Default
    private TypedParameter cdc = TypedParameter.of(0.022);

    /* Vdc controller */
    @Builder.Default
    private TypedParameter kp_v = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter ki_v = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter mq = TypedParameter.of(0.0016);

    /* V-P droop control */
    @Builder.Default
    private TypedParameter k_p = TypedParameter.of(0.0);
    @Builder.Default
    private TypedParameter p_ref = TypedParameter.of(0.0);

    /* Power / current scaling */
    @Builder.Default
    private TypedParameter s_base = TypedParameter.of(9800.0);
    @Builder.Default
    private TypedParameter tau_i = TypedParameter.of(0.001);
    @Builder.Default
    private TypedParameter iq_max = TypedParameter.of(2.0);

    /* Soft-start */
    @Builder.Default
    private TypedParameter t_precharge = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter t_ramp = TypedParameter.of(2.0);

    public Double getVdcRefValue() { return vdc_ref != null ? vdc_ref.getAsDouble() : 700.0; }
    public Double getCdcValue() { return cdc != null ? cdc.getAsDouble() : 0.022; }

    public Double getKpVValue() { return kp_v != null ? kp_v.getAsDouble() : 0.5; }
    public Double getKiVValue() { return ki_v != null ? ki_v.getAsDouble() : 0.5; }
    public Double getMqValue() { return mq != null ? mq.getAsDouble() : 0.0016; }

    public Double getKpValue() { return k_p != null ? k_p.getAsDouble() : 0.0; }
    public Double getPRefValue() { return p_ref != null ? p_ref.getAsDouble() : 0.0; }

    public Double getSBaseValue() { return s_base != null ? s_base.getAsDouble() : 9800.0; }
    public Double getTauIValue() { return tau_i != null ? tau_i.getAsDouble() : 0.001; }
    public Double getIqMaxValue() { return iq_max != null ? iq_max.getAsDouble() : 2.0; }

    public Double getTPrechargeValue() { return t_precharge != null ? t_precharge.getAsDouble() : 1.0; }
    public Double getTRampValue() { return t_ramp != null ? t_ramp.getAsDouble() : 2.0; }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Vdc_ref", vdc_ref)
                .put("Cdc", cdc)
                .put("Kp_v", kp_v)
                .put("Ki_v", ki_v)
                .put("mq", mq)
                .put("k_p", k_p)
                .put("P_ref", p_ref)
                .put("S_base", s_base)
                .put("tau_i", tau_i)
                .put("Iq_max", iq_max)
                .put("t_precharge", t_precharge)
                .put("t_ramp", t_ramp)
                .build();
    }
}
