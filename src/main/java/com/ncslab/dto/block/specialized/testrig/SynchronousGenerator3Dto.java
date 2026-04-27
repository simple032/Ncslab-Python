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
@JsonTypeName("SynchronousGenerator3")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.SynchronousGenerator3")
public class SynchronousGenerator3Dto extends BlockDto {

    /* Generator electrical (3-order model) */
    @Builder.Default
    private TypedParameter omega0 = TypedParameter.of(314.15926);
    @Builder.Default
    private TypedParameter xd = TypedParameter.of(1.346);
    @Builder.Default
    private TypedParameter xq = TypedParameter.of(0.94);
    @Builder.Default
    private TypedParameter xdd = TypedParameter.of(0.446);
    @Builder.Default
    private TypedParameter td0 = TypedParameter.of(1.66);
    @Builder.Default
    private TypedParameter ra = TypedParameter.of(0.006);
    @Builder.Default
    private TypedParameter h = TypedParameter.of(1.2);
    @Builder.Default
    private TypedParameter d = TypedParameter.of(10.0);

    /* AVR / excitation */
    @Builder.Default
    private TypedParameter vt_ref = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter kp_avr = TypedParameter.of(50.0);
    @Builder.Default
    private TypedParameter ki_avr = TypedParameter.of(10.0);
    @Builder.Default
    private TypedParameter efd0 = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter efd_min = TypedParameter.of(0.0);
    @Builder.Default
    private TypedParameter efd_max = TypedParameter.of(5.0);

    /* Governor / speed */
    @Builder.Default
    private TypedParameter omega_ref = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter kp_gov = TypedParameter.of(20.0);
    @Builder.Default
    private TypedParameter ki_gov = TypedParameter.of(5.0);
    @Builder.Default
    private TypedParameter pm0 = TypedParameter.of(0.05);
    @Builder.Default
    private TypedParameter pm_min = TypedParameter.of(0.0);
    @Builder.Default
    private TypedParameter pm_max = TypedParameter.of(0.8);

    public Double getOmega0Value() { return omega0 != null ? omega0.getAsDouble() : 314.15926; }
    public Double getXdValue() { return xd != null ? xd.getAsDouble() : 1.346; }
    public Double getXqValue() { return xq != null ? xq.getAsDouble() : 0.94; }
    public Double getXddValue() { return xdd != null ? xdd.getAsDouble() : 0.446; }
    public Double getTd0Value() { return td0 != null ? td0.getAsDouble() : 1.66; }
    public Double getRaValue() { return ra != null ? ra.getAsDouble() : 0.006; }
    public Double getHValue() { return h != null ? h.getAsDouble() : 1.2; }
    public Double getDValue() { return d != null ? d.getAsDouble() : 10.0; }

    public Double getVtRefValue() { return vt_ref != null ? vt_ref.getAsDouble() : 1.0; }
    public Double getKpAvrValue() { return kp_avr != null ? kp_avr.getAsDouble() : 50.0; }
    public Double getKiAvrValue() { return ki_avr != null ? ki_avr.getAsDouble() : 10.0; }
    public Double getEfd0Value() { return efd0 != null ? efd0.getAsDouble() : 1.0; }
    public Double getEfdMinValue() { return efd_min != null ? efd_min.getAsDouble() : 0.0; }
    public Double getEfdMaxValue() { return efd_max != null ? efd_max.getAsDouble() : 5.0; }

    public Double getOmegaRefValue() { return omega_ref != null ? omega_ref.getAsDouble() : 1.0; }
    public Double getKpGovValue() { return kp_gov != null ? kp_gov.getAsDouble() : 20.0; }
    public Double getKiGovValue() { return ki_gov != null ? ki_gov.getAsDouble() : 5.0; }
    public Double getPm0Value() { return pm0 != null ? pm0.getAsDouble() : 0.05; }
    public Double getPmMinValue() { return pm_min != null ? pm_min.getAsDouble() : 0.0; }
    public Double getPmMaxValue() { return pm_max != null ? pm_max.getAsDouble() : 0.8; }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Omega0", omega0)
                .put("Xd", xd)
                .put("Xq", xq)
                .put("Xdd", xdd)
                .put("Td0", td0)
                .put("Ra", ra)
                .put("H", h)
                .put("D", d)
                .put("Vt_ref", vt_ref)
                .put("Kp_avr", kp_avr)
                .put("Ki_avr", ki_avr)
                .put("Efd0", efd0)
                .put("Efd_min", efd_min)
                .put("Efd_max", efd_max)
                .put("omega_ref", omega_ref)
                .put("Kp_gov", kp_gov)
                .put("Ki_gov", ki_gov)
                .put("Pm0", pm0)
                .put("Pm_min", pm_min)
                .put("Pm_max", pm_max)
                .build();
    }
}
