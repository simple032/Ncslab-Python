package com.ncslab.dto.block.specialized.elect;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for DiodeElect block - Diode with electrical circuit algebraic loop support.
 * Extends DiodeDto to inherit standard diode block parameters.
 *
 * This block extends the standard Diode block to support algebraic loop resolution
 * in electrical circuit simulation. The electLoopString contains the resolved
 * algebraic equation when this block is identified as part of an algebraic loop.
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DiodeElect")
@MigrationCompatible(originalClass = "com.ncslab.circuit.block.electblock.DiodeElect")
public class DiodeElectDto extends DiodeDto {

    /**
     * Electrical loop resolution string for algebraic loop handling.
     * Contains the resolved algebraic equation when this block is part of a loop.
     */
    private TypedParameter electLoopString;

    /**
     * List of related block IDs in the algebraic loop.
     * Populated during circuit analysis to identify loop dependencies.
     */
    @lombok.Builder.Default
    private List<Integer> relatedBlockIds = new ArrayList<>();

    @Override
    public TypedParameterMap toParameterMap() {
        TypedParameterMap params = super.toParameterMap();

        if (electLoopString != null) {
            params.put("electLoopString", electLoopString);
        }

        if (relatedBlockIds != null && !relatedBlockIds.isEmpty()) {
            params.put("relatedBlockIds", TypedParameter.of(relatedBlockIds));
        }

        return params;
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // ElectBlock-specific validation
        // electLoopString is optional (only set during algebraic loop resolution)
        // relatedBlockIds is optional (populated during circuit analysis)

        return result;
    }

    @Override
    public DiodeElectDto copy() {
        DiodeElectDto copy = DiodeElectDto.builder()
            .blockId(this.getBlockId())
            .blockName(this.getBlockName())
            .blockPath(this.getBlockPath())
            .blockUUID(this.getBlockUUID())
            .forwardVoltage(this.getForwardVoltage())
            .onResistance(this.getOnResistance())
            .offConductance(this.getOffConductance())
            .electLoopString(this.electLoopString != null ? this.electLoopString.copy() : null)
            .relatedBlockIds(this.relatedBlockIds != null ? new ArrayList<>(this.relatedBlockIds) : null)
            .build();

        // Copy other inherited fields
        if (this.getParameters() != null) {
            copy.setParameters(new TypedParameterMap(this.getParameters()));
        }

        return copy;
    }

    @Override
    public String toString() {
        return String.format("DiodeElectDto{id=%d, name='%s', Vf=%s, Ron=%s, isLoopPoint=%s}",
                           getBlockId(),
                           getBlockName(),
                           getForwardVoltageValue(),
                           getOnResistanceValue(),
                           isLoopPoint());
    }

    /**
     * Check if this block is part of an algebraic loop
     */
    public boolean isLoopPoint() {
        return relatedBlockIds != null && !relatedBlockIds.isEmpty();
    }
}
