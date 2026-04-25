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
 * DTO representation of HGGenerator12 block (12-phase rectifier generator).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("HGGenerator12")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.HGGenerator12")
public class HGGenerator12Dto extends BlockDto {

    @Builder.Default
    private TypedParameter xd = TypedParameter.of(1.5);
    @Builder.Default
    private TypedParameter xdd = TypedParameter.of(0.3);
    @Builder.Default
    private TypedParameter xddd = TypedParameter.of(0.2);
    @Builder.Default
    private TypedParameter xq = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter xqq = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter xqqq = TypedParameter.of(0.25);
    @Builder.Default
    private TypedParameter xl = TypedParameter.of(0.15);
    @Builder.Default
    private TypedParameter td0 = TypedParameter.of(5.0);
    @Builder.Default
    private TypedParameter td000 = TypedParameter.of(0.1);
    @Builder.Default
    private TypedParameter tq0 = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter tq000 = TypedParameter.of(0.05);
    @Builder.Default
    private TypedParameter rs = TypedParameter.of(0.005);
    @Builder.Default
    private TypedParameter fn = TypedParameter.of(50.0);
    @Builder.Default
    private TypedParameter th = TypedParameter.of(0.0);
    @Builder.Default
    private TypedParameter pha = TypedParameter.of(0.0);

    public Double getXdValue() {
        return xd != null ? xd.getAsDouble() : 1.5;
    }

    public Double getXddValue() {
        return xdd != null ? xdd.getAsDouble() : 0.3;
    }

    public Double getXdddValue() {
        return xddd != null ? xddd.getAsDouble() : 0.2;
    }

    public Double getXqValue() {
        return xq != null ? xq.getAsDouble() : 1.0;
    }

    public Double getXqqValue() {
        return xqq != null ? xqq.getAsDouble() : 0.5;
    }

    public Double getXqqqValue() {
        return xqqq != null ? xqqq.getAsDouble() : 0.25;
    }

    public Double getXlValue() {
        return xl != null ? xl.getAsDouble() : 0.15;
    }

    public Double getTd0Value() {
        return td0 != null ? td0.getAsDouble() : 5.0;
    }

    public Double getTd000Value() {
        return td000 != null ? td000.getAsDouble() : 0.1;
    }

    public Double getTq0Value() {
        return tq0 != null ? tq0.getAsDouble() : 1.0;
    }

    public Double getTq000Value() {
        return tq000 != null ? tq000.getAsDouble() : 0.05;
    }

    public Double getRsValue() {
        return rs != null ? rs.getAsDouble() : 0.005;
    }

    public Double getFnValue() {
        return fn != null ? fn.getAsDouble() : 50.0;
    }

    public Double getThValue() {
        return th != null ? th.getAsDouble() : 0.0;
    }

    public Double getPhaValue() {
        return pha != null ? pha.getAsDouble() : 0.0;
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Xd", xd)
                .put("Xdd", xdd)
                .put("Xddd", xddd)
                .put("Xq", xq)
                .put("Xqq", xqq)
                .put("Xqqq", xqqq)
                .put("Xl", xl)
                .put("Td0", td0)
                .put("Td000", td000)
                .put("Tq0", tq0)
                .put("Tq000", tq000)
                .put("Rs", rs)
                .put("fn", fn)
                .put("th", th)
                .put("pha", pha)
                .build();
    }
}
