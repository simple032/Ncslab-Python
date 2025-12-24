package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for Android simulation response data section.
 * Contains file URLs for simulation results.
 *
 * @author NCSLab
 * @version 1.0
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AndroidDataDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * URL path to the figure/scope file for visualization
     */
    @JsonProperty("figFileUrl")
    private String figFileUrl;

    /**
     * URL path to the data JSON file containing simulation results
     */
    @JsonProperty("dataFileUrl")
    private String dataFileUrl;

    /**
     * URL path to the compiled binary file (for compilation responses)
     */
    @JsonProperty("binaryFileUrl")
    private String binaryFileUrl;

    /**
     * Creates an AndroidDataDto with specified file URLs for simulation
     *
     * @param figFileUrl URL to the figure/scope file
     * @param dataFileUrl URL to the data JSON file
     * @return constructed AndroidDataDto instance
     */
    public static AndroidDataDto of(String figFileUrl, String dataFileUrl) {
        return AndroidDataDto.builder()
                .figFileUrl(figFileUrl)
                .dataFileUrl(dataFileUrl)
                .build();
    }

    /**
     * Creates an AndroidDataDto with binary file URL for compilation
     *
     * @param binaryFileUrl URL to the compiled binary file
     * @return constructed AndroidDataDto instance
     */
    public static AndroidDataDto ofBinary(String binaryFileUrl) {
        return AndroidDataDto.builder()
                .binaryFileUrl(binaryFileUrl)
                .build();
    }

    /**
     * Validates that all required fields are present
     *
     * @return true if both URLs are non-null and non-empty
     */
    public boolean isValid() {
        // Valid if either simulation data (both figFileUrl and dataFileUrl) OR binaryFileUrl is present
        boolean hasSimulationData = figFileUrl != null && !figFileUrl.isEmpty()
            && dataFileUrl != null && !dataFileUrl.isEmpty();
        boolean hasBinaryData = binaryFileUrl != null && !binaryFileUrl.isEmpty();
        return hasSimulationData || hasBinaryData;
    }
}
