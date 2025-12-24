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
 * DTO representation of Data Store Memory block for global variable storage.
 *
 * <p>The Data Store Memory block defines and initializes a global data store
 * that can be accessed by Data Store Read and Data Store Write blocks throughout
 * the model without signal line connections.</p>
 *
 * <p>This block has no input/output ports - it only defines storage.</p>
 *
 * <h3>SIMULINK Parameters:</h3>
 * <ul>
 *   <li><b>DataStoreName</b>: Unique identifier for the data store (e.g., "GlobalCounter")</li>
 *   <li><b>InitialValue</b>: Initial value or matrix for the data store</li>
 *   <li><b>Dimensions</b>: Signal dimensions [rows, cols], default [1, 1]</li>
 *   <li><b>DataType</b>: Data type specification (e.g., "double", "int32")</li>
 *   <li><b>SampleTime</b>: Sample time (typically -1 for inherited)</li>
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
@JsonTypeName("DataStoreMemory")
@MigrationCompatible(originalClass = "com.ncslab.block.signal.DataStoreMemory")
public class DataStoreMemoryDto extends BlockDto {

    /**
     * Unique name identifier for the data store.
     * This name is used by Data Store Read and Write blocks to access the storage.
     * Default: "A"
     */
    @Builder.Default
    private TypedParameter dataStoreName = TypedParameter.of("A");

    /**
     * Initial value for the data store (scalar or matrix).
     * Default: 0
     */
    @Builder.Default
    private TypedParameter initialValue = TypedParameter.of(0.0);

    /**
     * Signal dimensions in format "[rows, cols]" or "[rows cols]".
     * Default: "[1, 1]" (scalar)
     */
    @Builder.Default
    private TypedParameter dimensions = TypedParameter.of("[1, 1]");

    /**
     * Data type specification.
     * Default: "double"
     */
    @Builder.Default
    private TypedParameter dataType = TypedParameter.of("double");

    /**
     * Sample time for the block operation.
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    // ===== PARAMETER ACCESS HELPERS =====

    /**
     * Gets the data store name as a string.
     *
     * @return Data store name, default "A"
     */
    public String getDataStoreNameValue() {
        return dataStoreName != null ? dataStoreName.getAsString() : "A";
    }

    /**
     * Gets the initial value as a string (supports expressions and matrices).
     *
     * @return Initial value string
     */
    public String getInitialValueAsString() {
        return initialValue != null ? initialValue.getAsString() : "0";
    }

    /**
     * Gets the initial value as a double (for scalar values).
     *
     * @return Initial value as double, default 0.0
     */
    public Double getInitialValueAsDouble() {
        return initialValue != null ? initialValue.getAsDouble() : 0.0;
    }

    /**
     * Gets the dimensions as a string.
     *
     * @return Dimensions string, default "[1, 1]"
     */
    public String getDimensionsValue() {
        return dimensions != null ? dimensions.getAsString() : "[1, 1]";
    }

    /**
     * Gets the data type specification.
     *
     * @return Data type string, default "double"
     */
    public String getDataTypeValue() {
        return dataType != null ? dataType.getAsString() : "double";
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

        // Validate initial value exists
        if (initialValue == null) {
            result.addError("Initial value must be specified");
        }

        // Validate dimensions format
        if (dimensions != null) {
            String dims = dimensions.getAsString();
            if (dims != null && !isValidDimensionsFormat(dims)) {
                result.addError("Dimensions must be in format '[rows, cols]' or '[rows cols]'");
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

    /**
     * Validates dimensions format (e.g., "[1, 1]" or "[3 2]").
     */
    private boolean isValidDimensionsFormat(String dims) {
        if (dims == null) {
            return false;
        }
        // Match formats: [1, 1] or [1 1] or [rows, cols] or [rows cols]
        return dims.matches("\\[\\s*\\d+\\s*[,\\s]+\\s*\\d+\\s*\\]");
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

    @Override
    public DataStoreMemoryDto copy() {
        return DataStoreMemoryDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .dataStoreName(dataStoreName != null ? dataStoreName.copy() : null)
                .initialValue(initialValue != null ? initialValue.copy() : null)
                .dimensions(dimensions != null ? dimensions.copy() : null)
                .dataType(dataType != null ? dataType.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("DataStoreName", dataStoreName)
                .put("InitialValue", initialValue)
                .put("Dimensions", dimensions)
                .put("DataType", dataType)
                .put("SampleTime", sampleTime)
                .build();
    }

    @Override
    public String toString() {
        return String.format("DataStoreMemoryDto{id=%d, name='%s', dataStoreName='%s', initialValue='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getDataStoreNameValue(),
                           getInitialValueAsString());
    }
}
