package com.ncslab.dto.block.specialized.signal;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of Probe block for signal property inspection.
 *
 * The Probe block outputs signal properties for introspection and debugging:
 * - "width": signal width (number of elements)
 * - "sampleTime": signal sample time
 * - "dataType": data type as numeric code
 * - "complexity": 0 for real, 1 for complex
 *
 * SIMULINK Equivalent: Probe block
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Probe")
@MigrationCompatible(originalClass = "com.ncslab.block.signal.Probe")
public class ProbeDto extends BlockDto {

    /**
     * Probe type - which property to output
     * Valid values: "width", "sampleTime", "dataType", "complexity"
     * Default: "width"
     */
    @Builder.Default
    private TypedParameter probeType = TypedParameter.of("width");

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification
     * Default: "double" (probe output is always numeric)
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("double");

    // ===== PARAMETER ACCESS HELPERS =====

    public String getProbeTypeValue() {
        return probeType != null ? probeType.getAsString() : "width";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "double";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate probe type
        if (probeType != null) {
            String type = probeType.getAsString();
            if (type != null && !isValidProbeType(type)) {
                result.addError("Probe type must be one of: width, sampleTime, dataType, complexity");
            }
        }

        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }

        return result;
    }

    /**
     * Check if the probe type is valid
     */
    private boolean isValidProbeType(String type) {
        return type.equals("width") || type.equals("sampleTime") ||
               type.equals("dataType") || type.equals("complexity");
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    @Override
    public ProbeDto copy() {
        return ProbeDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .probeType(probeType != null ? probeType.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ProbeType", probeType)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ProbeDto{id=%d, name='%s', type='%s', probeType='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getProbeTypeValue());
    }
}
