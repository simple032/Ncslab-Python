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
 * DTO representation of Data Store Write block for writing to global variables.
 *
 * <p>The Data Store Write block writes a value to a named data store
 * defined by a Data Store Memory block. This allows updating shared data
 * throughout the model without signal line connections.</p>
 *
 * <p>Block characteristics:</p>
 * <ul>
 *   <li>One input port (no output ports)</li>
 *   <li>Writes input value to named data store</li>
 *   <li>Updates data store at each time step</li>
 *   <li>Sink-like behavior (no output propagation)</li>
 * </ul>
 *
 * <h3>SIMULINK Parameters:</h3>
 * <ul>
 *   <li><b>DataStoreName</b>: Name of data store to write to (must match DataStoreMemory)</li>
 *   <li><b>SampleTime</b>: Sample time for writing (-1 for inherited)</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DataStoreWrite")
@MigrationCompatible(originalClass = "com.ncslab.block.signal.DataStoreWrite")
public class DataStoreWriteDto extends BlockDto {

    /**
     * Name of the data store to write to.
     * Must match a DataStoreMemory block's name.
     * Default: "A"
     */
    @Builder.Default
    private TypedParameter dataStoreName = TypedParameter.of("A");

    /**
     * Sample time for the block operation.
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    // ===== PARAMETER ACCESS HELPERS =====

    /**
     * Gets the data store name to write to.
     *
     * @return Data store name, default "A"
     */
    public String getDataStoreNameValue() {
        return dataStoreName != null ? dataStoreName.getAsString() : "A";
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time, default -1.0
     */
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate data store name
        if (dataStoreName != null) {
            String name = dataStoreName.getAsString();
            if (name == null || name.trim().isEmpty()) {
                result.addError("Data store name cannot be empty");
            } else if (!isValidIdentifier(name)) {
                result.addError("Data store name must be a valid identifier (alphanumeric and underscore only)");
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
     * Validates if a string is a valid identifier (alphanumeric and underscore).
     */
    private boolean isValidIdentifier(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        // Must start with letter or underscore, followed by alphanumeric or underscore
        return name.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    // ===== UTILITY METHODS =====

    /**
     * Checks if this block operates in continuous time.
     *
     * @return true if sample time is 0.0
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if this block inherits its sample time.
     *
     * @return true if sample time is -1.0
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the data store name is valid.
     *
     * @return true if valid identifier format
     */
    public boolean isValidDataStoreName() {
        return isValidIdentifier(getDataStoreNameValue());
    }

    @Override
    public DataStoreWriteDto copy() {
        return DataStoreWriteDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .dataStoreName(dataStoreName != null ? dataStoreName.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("DataStoreName", dataStoreName)
                .put("SampleTime", sampleTime)
                .build();
    }

    @Override
    public String toString() {
        return String.format("DataStoreWriteDto{id=%d, name='%s', dataStoreName='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getDataStoreNameValue());
    }
}
