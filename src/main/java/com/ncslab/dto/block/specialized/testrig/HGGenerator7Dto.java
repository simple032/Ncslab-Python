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
 * DTO representation of HGGenerator7 block (7th-order synchronous generator, per-unit system).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("HGGenerator7")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.HGGenerator7")
public class HGGenerator7Dto extends BlockDto {

    @Builder.Default
    private TypedParameter omega0 = TypedParameter.of(314.15926);
    @Builder.Default
    private TypedParameter xd = TypedParameter.of(1.346);
    @Builder.Default
    private TypedParameter xq = TypedParameter.of(0.940);
    @Builder.Default
    private TypedParameter xdd = TypedParameter.of(0.446);
    @Builder.Default
    private TypedParameter xddd = TypedParameter.of(0.330);
    @Builder.Default
    private TypedParameter xqqq = TypedParameter.of(0.370);
    @Builder.Default
    private TypedParameter x1 = TypedParameter.of(0.243);
    @Builder.Default
    private TypedParameter td0 = TypedParameter.of(1.660);
    @Builder.Default
    private TypedParameter td000 = TypedParameter.of(0.118);
    @Builder.Default
    private TypedParameter tq000 = TypedParameter.of(0.035);
    @Builder.Default
    private TypedParameter h = TypedParameter.of(1.2);
    @Builder.Default
    private TypedParameter r = TypedParameter.of(0.006);

    public Double getOmega0Value() {
        return omega0 != null ? omega0.getAsDouble() : 314.15926;
    }

    public Double getXdValue() {
        return xd != null ? xd.getAsDouble() : 1.346;
    }

    public Double getXqValue() {
        return xq != null ? xq.getAsDouble() : 0.940;
    }

    public Double getXddValue() {
        return xdd != null ? xdd.getAsDouble() : 0.446;
    }

    public Double getXdddValue() {
        return xddd != null ? xddd.getAsDouble() : 0.330;
    }

    public Double getXqqqValue() {
        return xqqq != null ? xqqq.getAsDouble() : 0.370;
    }

    public Double getX1Value() {
        return x1 != null ? x1.getAsDouble() : 0.243;
    }

    public Double getTd0Value() {
        return td0 != null ? td0.getAsDouble() : 1.660;
    }

    public Double getTd000Value() {
        return td000 != null ? td000.getAsDouble() : 0.118;
    }

    public Double getTq000Value() {
        return tq000 != null ? tq000.getAsDouble() : 0.035;
    }

    public Double getHValue() {
        return h != null ? h.getAsDouble() : 1.2;
    }

    public Double getRValue() {
        return r != null ? r.getAsDouble() : 0.006;
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Omega0", omega0)
                .put("Xd", xd)
                .put("Xq", xq)
                .put("Xdd", xdd)
                .put("Xddd", xddd)
                .put("Xqqq", xqqq)
                .put("X1", x1)
                .put("Td0", td0)
                .put("Td000", td000)
                .put("Tq000", tq000)
                .put("H", h)
                .put("R", r)
                .build();
    }
}
