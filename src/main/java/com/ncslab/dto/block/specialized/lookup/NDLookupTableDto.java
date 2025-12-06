package com.ncslab.dto.block.specialized.lookup;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.common.TypedParameterMap.TypedParameterMapBuilder;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of NDLookupTable block.
 *
 * Provides multi-dimensional table interpolation with support for:
 * - 1-D to n-D lookup tables
 * - Multiple interpolation methods (Linear, Flat, Nearest)
 * - Multiple extrapolation methods (Linear, Clip)
 * - Explicit or evenly-spaced breakpoint specification
 *
 * SIMULINK Parameters:
 * - NumberOfTableDimensions: Number of table dimensions (1 to n), default 1
 * - BreakpointsSpecification: "Explicit values" or "Even spacing", default "Explicit values"
 * - BreakpointsForDimension1: Breakpoints for dimension 1, default "[0 1]"
 * - BreakpointsForDimension2: Breakpoints for dimension 2 (if n>=2)
 * - BreakpointsForDimension3: Breakpoints for dimension 3 (if n>=3)
 * - BreakpointsForDimension4: Breakpoints for dimension 4 (if n>=4)
 * - Table: Table data values (n-dimensional array)
 * - InterpMethod: Interpolation method ("Linear point-slope", "Flat", "Nearest"), default "Linear point-slope"
 * - ExtrapMethod: Extrapolation method ("Linear", "Clip"), default "Clip"
 * - SampleTime: Sample time for discrete operation, default "-1"
 * - OutDataTypeStr: Output data type specification
 *
 * @author NCSLab Team
 * @version 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("NDLookupTable")
@MigrationCompatible(originalClass = "com.ncslab.block.lookup.NDLookupTable")
public class NDLookupTableDto extends BlockDto {

    /** Number of table dimensions */
    @Builder.Default
    private TypedParameter numberOfTableDimensions = TypedParameter.of(1);

    /** Breakpoints specification method */
    @Builder.Default
    private TypedParameter breakpointsSpecification = TypedParameter.of("Explicit values");

    /** Breakpoints for dimension 1 */
    @Builder.Default
    private TypedParameter breakpointsForDimension1 = TypedParameter.of("[0 1]");

    /** Breakpoints for dimension 2 (optional) */
    private TypedParameter breakpointsForDimension2;

    /** Breakpoints for dimension 3 (optional) */
    private TypedParameter breakpointsForDimension3;

    /** Breakpoints for dimension 4 (optional) */
    private TypedParameter breakpointsForDimension4;

    /** Table data */
    @Builder.Default
    private TypedParameter table = TypedParameter.of("[0 1]");

    /** Interpolation method */
    @Builder.Default
    private TypedParameter interpMethod = TypedParameter.of("Linear point-slope");

    /** Extrapolation method */
    @Builder.Default
    private TypedParameter extrapMethod = TypedParameter.of("Clip");

    /** Sample time parameter */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of("-1");

    /** Output data type specification */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as first input");

    // === Convenience Getters ===

    public int getNumberOfTableDimensionsValue() {
        return numberOfTableDimensions != null ? numberOfTableDimensions.getAsInteger() : 1;
    }

    public String getBreakpointsSpecificationValue() {
        return breakpointsSpecification != null ? breakpointsSpecification.getAsString() : "Explicit values";
    }

    public String getBreakpointsForDimension1Value() {
        return breakpointsForDimension1 != null ? breakpointsForDimension1.getAsString() : "[0 1]";
    }

    public String getBreakpointsForDimension2Value() {
        return breakpointsForDimension2 != null ? breakpointsForDimension2.getAsString() : null;
    }

    public String getBreakpointsForDimension3Value() {
        return breakpointsForDimension3 != null ? breakpointsForDimension3.getAsString() : null;
    }

    public String getBreakpointsForDimension4Value() {
        return breakpointsForDimension4 != null ? breakpointsForDimension4.getAsString() : null;
    }

    public String getTableValue() {
        return table != null ? table.getAsString() : "[0 1]";
    }

    public String getInterpMethodValue() {
        return interpMethod != null ? interpMethod.getAsString() : "Linear point-slope";
    }

    public String getExtrapMethodValue() {
        return extrapMethod != null ? extrapMethod.getAsString() : "Clip";
    }

    public String getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsString() : "-1";
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }

    @Override
    public TypedParameterMap toParameterMap() {
        TypedParameterMapBuilder builder = TypedParameterMap.builder()
                .put("NumberOfTableDimensions", numberOfTableDimensions)
                .put("BreakpointsSpecification", breakpointsSpecification)
                .put("BreakpointsForDimension1", breakpointsForDimension1)
                .put("Table", table)
                .put("InterpMethod", interpMethod)
                .put("ExtrapMethod", extrapMethod)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr);

        // Add optional dimensional breakpoints
        if (breakpointsForDimension2 != null) {
            builder.put("BreakpointsForDimension2", breakpointsForDimension2);
        }
        if (breakpointsForDimension3 != null) {
            builder.put("BreakpointsForDimension3", breakpointsForDimension3);
        }
        if (breakpointsForDimension4 != null) {
            builder.put("BreakpointsForDimension4", breakpointsForDimension4);
        }

        return builder.build();
    }
}
