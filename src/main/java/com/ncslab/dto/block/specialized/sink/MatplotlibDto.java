package com.ncslab.dto.block.specialized.sink;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of Matplotlib sink block.
 * 
 * The Matplotlib block creates plots and visualizations using matplotlib
 * library for Python-based data analysis and display.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Matplotlib")
@MigrationCompatible(originalClass = "com.ncslab.block.sink.Matplotlib")
public class MatplotlibDto extends BlockDto {
    
    /**
     * Sample time for the matplotlib block
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Save name for plot files
     * Default: "plot"
     */
    @Builder.Default
    private TypedParameter saveName = TypedParameter.of("plot");
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    /**
     * Plot title
     * Default: "Plot"
     */
    @Builder.Default
    private TypedParameter title = TypedParameter.of("Plot");
    
    /**
     * X-axis label
     * Default: "Time"
     */
    @Builder.Default
    private TypedParameter xLabel = TypedParameter.of("Time");
    
    /**
     * Y-axis label
     * Default: "Amplitude"
     */
    @Builder.Default
    private TypedParameter yLabel = TypedParameter.of("Amplitude");
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getSaveNameValue() {
        return saveName != null ? saveName.getAsString() : "plot";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public String getTitleValue() {
        return title != null ? title.getAsString() : "Plot";
    }
    
    public String getXLabelValue() {
        return xLabel != null ? xLabel.getAsString() : "Time";
    }
    
    public String getYLabelValue() {
        return yLabel != null ? yLabel.getAsString() : "Amplitude";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        // Validate save name
        if (saveName == null || saveName.getAsString() == null || saveName.getAsString().trim().isEmpty()) {
            result.addError("Save name cannot be empty");
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }
    
    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }
    
    @Override
    public MatplotlibDto copy() {
        return MatplotlibDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .saveName(saveName != null ? saveName.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .title(title != null ? title.copy() : null)
                .xLabel(xLabel != null ? xLabel.copy() : null)
                .yLabel(yLabel != null ? yLabel.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", sampleTime)
                .put("SaveName", saveName)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("Title", title)
                .put("XLabel", xLabel)
                .put("YLabel", yLabel)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("MatplotlibDto{id=%d, name='%s', type='%s', saveName='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getSaveNameValue());
    }
}