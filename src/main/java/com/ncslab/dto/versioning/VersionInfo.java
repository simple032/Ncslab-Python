package com.ncslab.dto.versioning;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Simple version information holder for DTO versioning.
 * Basic implementation for user to extend manually in future.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionInfo {
    
    private String version = "1.0";
    private String description = "";
    
    public static VersionInfo of(String version) {
        return new VersionInfo(version, "");
    }
    
    public static VersionInfo of(String version, String description) {
        return new VersionInfo(version, description);
    }
    
    public boolean isCompatibleWith(String otherVersion) {
        // Basic semantic versioning compatibility check
        String[] thisParts = this.version.split("\\.");
        String[] otherParts = otherVersion.split("\\.");
        
        if (thisParts.length >= 2 && otherParts.length >= 2) {
            return thisParts[0].equals(otherParts[0]); // Same major version
        }
        
        return this.version.equals(otherVersion);
    }
}