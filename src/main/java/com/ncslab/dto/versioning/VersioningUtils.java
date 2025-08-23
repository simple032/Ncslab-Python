package com.ncslab.dto.versioning;

import com.ncslab.dto.core.BlockDto;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for DTO versioning support.
 * Provides basic version tracking and compatibility checking.
 */
public class VersioningUtils {
    
    /**
     * Get version information from a DTO if it supports versioning
     */
    public static VersionInfo getVersionInfo(BlockDto dto) {
        if (dto instanceof VersionedDto) {
            return ((VersionedDto) dto).getVersionInfo();
        }
        return VersionInfo.of("1.0", "Legacy DTO without versioning");
    }
    
    /**
     * Check if a DTO supports versioning
     */
    public static boolean isVersioned(BlockDto dto) {
        return dto instanceof VersionedDto;
    }
    
    /**
     * Get version summary for a collection of DTOs
     */
    public static Map<String, String> getVersionSummary(Iterable<BlockDto> dtos) {
        Map<String, String> summary = new HashMap<>();
        
        for (BlockDto dto : dtos) {
            String blockType = dto.getBlockType();
            VersionInfo versionInfo = getVersionInfo(dto);
            summary.put(blockType, versionInfo.getVersion());
        }
        
        return summary;
    }
    
    /**
     * Simple validation that version is supported
     */
    public static boolean isSupportedVersion(String version) {
        if (version == null) return false;
        return version.matches("\\d+\\.\\d+(\\.\\d+)?");
    }
}