package com.ncslab.dto.versioning;

/**
 * Interface for DTOs that support versioning.
 * Minimal interface for user to extend manually in future.
 */
public interface VersionedDto {
    
    /**
     * Get version information for this DTO
     */
    VersionInfo getVersionInfo();
    
    /**
     * Check if this DTO can be migrated from the specified version
     */
    default boolean canMigrateFrom(String fromVersion) {
        return getVersionInfo().isCompatibleWith(fromVersion);
    }
    
    /**
     * Get supported versions that this DTO can handle
     */
    default String[] getSupportedVersions() {
        return new String[]{getVersionInfo().getVersion()};
    }
}