package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of Substring block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Substring block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The Substring block extracts a substring from input string based on starting
 * index and length.
 *
 * SIMULINK Parameters:
 * - StartIndex: Starting character index, 1-based MATLAB convention (default: 1, range: 1-∞)
 * - Length: Number of characters to extract (default: 1, range: 1-∞)
 *
 * Behavior:
 * - Uses 1-based indexing (MATLAB/Simulink convention): StartIndex=1 means first character
 * - Extracts substring starting at StartIndex for Length characters
 * - If StartIndex + Length exceeds string length, extract to end of string
 * - If StartIndex > string length, return empty string
 *
 * Examples:
 * - Input="HelloWorld", StartIndex=6, Length=5 → Output="World"
 * - Input="Test", StartIndex=2, Length=10 → Output="est" (extracts to end)
 * - Input="Hello", StartIndex=10, Length=5 → Output="" (empty string)
 *
 * Character Set: ISO/IEC 8859-1 (first 256 Unicode code points)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Substring")
@MigrationCompatible(originalClass = "com.ncslab.block.string.Substring")
public class SubstringDto extends BlockDto {

    // ===== SUBSTRING SPECIFIC PARAMETERS =====

    /**
     * Starting character index (1-based MATLAB convention).
     * Valid range: 1-∞
     * Default: 1
     */
    private Integer startIndex;

    /**
     * Number of characters to extract.
     * Valid range: 1-∞
     * Default: 1
     */
    private Integer length;

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate startIndex (>= 1, 1-based MATLAB indexing)
        if (startIndex == null) {
            result.addWarning("startIndex", "Start index is null - using default value 1");
        } else if (startIndex < 1) {
            result.addError("startIndex", "Start index must be >= 1 (1-based MATLAB indexing), got: " + startIndex);
        }

        // Validate length (>= 1)
        if (length == null) {
            result.addWarning("length", "Length is null - using default value 1");
        } else if (length < 1) {
            result.addError("length", "Length must be >= 1, got: " + length);
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    @Override
    public SubstringDto copy() {
        return SubstringDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .startIndex(this.startIndex)
                .length(this.length)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();

        if (startIndex != null) {
            params.put("StartIndex", startIndex.toString());
        }

        if (length != null) {
            params.put("Length", length.toString());
        }

        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("SubstringDto{id=%d, name='%s', type='%s', startIndex=%d, length=%d}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           startIndex,
                           length);
    }

    // ===== HELPER METHODS =====

    /**
     * Get the start index with a default value if null.
     *
     * @return startIndex or 1 if null
     */
    public int getStartIndexOrDefault() {
        return startIndex != null ? startIndex : 1;
    }

    /**
     * Get the length with a default value if null.
     *
     * @return length or 1 if null
     */
    public int getLengthOrDefault() {
        return length != null ? length : 1;
    }

    /**
     * Calculate the end index (1-based MATLAB convention) for the substring extraction.
     * This is the last character index that would be included in the substring.
     *
     * @return end index (1-based)
     */
    public int getEndIndex() {
        int start = getStartIndexOrDefault();
        int len = getLengthOrDefault();
        return start + len - 1;
    }

    /**
     * Check if the substring parameters would extract to the end of a string
     * of the given length.
     *
     * @param stringLength The length of the string being extracted from
     * @return true if extraction would go to the end, false otherwise
     */
    public boolean extractsToEnd(int stringLength) {
        return getEndIndex() >= stringLength;
    }

    /**
     * Check if the start index is beyond the given string length
     * (would result in empty string extraction).
     *
     * @param stringLength The length of the string being extracted from
     * @return true if start index is out of bounds, false otherwise
     */
    public boolean isStartIndexOutOfBounds(int stringLength) {
        return getStartIndexOrDefault() > stringLength;
    }

    /**
     * Get the actual extraction length that would be used for a string
     * of the given length (handles boundary conditions).
     *
     * @param stringLength The length of the string being extracted from
     * @return actual number of characters that would be extracted
     */
    public int getActualExtractionLength(int stringLength) {
        if (isStartIndexOutOfBounds(stringLength)) {
            return 0; // Empty string
        }

        int start = getStartIndexOrDefault();
        int len = getLengthOrDefault();
        int remainingChars = stringLength - start + 1;

        return Math.min(len, remainingChars);
    }
}
