package com.ncslab.dto.block.specialized.sink;

import com.ncslab.dto.block.sink.SinkDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO representation of XYGraph block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the XYGraph block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for data collection (-1 for inherited)
 * - XMin: Minimum X-axis value for plot display
 * - XMax: Maximum X-axis value for plot display
 * - YMin: Minimum Y-axis value for plot display
 * - YMax: Maximum Y-axis value for plot display
 * - SaveName: Variable name to save data (default: "XYGraphData")
 * - BufferSize: Size of data buffer (default: 100000)
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.sink.XYGraph")
public class XYGraphDto extends SinkDto {

    /**
     * Sample time for data collection
     * Default: -1 (inherited)
     */
    private TypedParameter sampleTime;

    /**
     * Minimum X-axis value for plot display
     * Default: -10
     */
    private TypedParameter xMin;

    /**
     * Maximum X-axis value for plot display
     * Default: 10
     */
    private TypedParameter xMax;

    /**
     * Minimum Y-axis value for plot display
     * Default: -10
     */
    private TypedParameter yMin;

    /**
     * Maximum Y-axis value for plot display
     * Default: 10
     */
    private TypedParameter yMax;

    /**
     * Variable name to save XY graph data
     * Default: "XYGraphData"
     */
    private TypedParameter saveName;

    /**
     * Size of data buffer for XY graph
     * Default: 100000
     */
    private TypedParameter bufferSize;

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Double st = sampleTime.getAsDouble();
            if (st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }

        // Validate axis limits
        if (xMin != null && xMax != null) {
            Double min = xMin.getAsDouble();
            Double max = xMax.getAsDouble();
            if (min != null && max != null && min >= max) {
                result.addError("XMin must be less than XMax");
            }
        }

        if (yMin != null && yMax != null) {
            Double min = yMin.getAsDouble();
            Double max = yMax.getAsDouble();
            if (min != null && max != null && min >= max) {
                result.addError("YMin must be less than YMax");
            }
        }

        // Validate buffer size
        if (bufferSize != null && bufferSize.getAsDouble() != null) {
            Double size = bufferSize.getAsDouble();
            if (size <= 0) {
                result.addError("Buffer size must be positive");
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return String.format("XYGraphDto{blockName='%s', sampleTime=%s, xRange=[%s,%s], yRange=[%s,%s], saveName='%s', bufferSize=%s}",
                getBlockName(),
                sampleTime != null ? sampleTime.getAsDouble() : "null",
                xMin != null ? xMin.getAsDouble() : "null",
                xMax != null ? xMax.getAsDouble() : "null",
                yMin != null ? yMin.getAsDouble() : "null",
                yMax != null ? yMax.getAsDouble() : "null",
                saveName != null ? saveName.getAsString() : "null",
                bufferSize != null ? bufferSize.getAsDouble() : "null");
    }
}
