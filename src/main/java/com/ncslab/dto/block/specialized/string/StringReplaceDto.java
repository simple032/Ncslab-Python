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
 * DTO representation of StringReplace block with SIMULINK-compatible parameters.
 *
 * Replaces all occurrences of a substring with another substring.
 *
 * SIMULINK Parameters:
 * - OldSubstring: Substring to find (default: "old")
 * - NewSubstring: Substring to replace with (default: "new")
 *
 * Behavior:
 * - Case-sensitive matching
 * - Replaces ALL occurrences (not just first)
 * - If OldSubstring not found, output equals input unchanged
 *
 * Examples:
 * - Input="Hello World", OldSubstring="World", NewSubstring="Java" → Output="Hello Java"
 * - Input="test test", OldSubstring="test", NewSubstring="pass" → Output="pass pass"
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 4 String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringReplace")
@MigrationCompatible(originalClass = "com.ncslab.block.string.StringReplace")
public class StringReplaceDto extends BlockDto {

    /**
     * Substring to find and replace.
     * Default: "old"
     */
    private String oldSubstring;

    /**
     * Substring to replace with.
     * Default: "new"
     */
    private String newSubstring;

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        if (oldSubstring == null) {
            result.addWarning("oldSubstring", "Old substring is null - using default 'old'");
        }
        if (newSubstring == null) {
            result.addWarning("newSubstring", "New substring is null - using default 'new'");
        }

        return result;
    }

    @Override
    public StringReplaceDto copy() {
        return StringReplaceDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .oldSubstring(this.oldSubstring)
                .newSubstring(this.newSubstring)
                .build();
    }

    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();

        if (oldSubstring != null) {
            params.put("OldSubstring", oldSubstring);
        }
        if (newSubstring != null) {
            params.put("NewSubstring", newSubstring);
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    public String getOldSubstringOrDefault() {
        return oldSubstring != null ? oldSubstring : "old";
    }

    public String getNewSubstringOrDefault() {
        return newSubstring != null ? newSubstring : "new";
    }

    @Override
    public String getBlockType() {
        return "StringReplace";
    }
}
