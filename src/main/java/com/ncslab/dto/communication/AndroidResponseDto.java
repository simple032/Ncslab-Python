package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for Android simulation API responses.
 * Provides a standardized response structure for Android client applications.
 *
 * Response structure:
 * {
 *   "code": 2000,
 *   "message": "SUCCESS",
 *   "modelName": "s51520",
 *   "data": {
 *     "figFileUrl": "/result/35/s51520/scope",
 *     "dataFileUrl": "/result/35/s51520/data.json"
 *   }
 * }
 *
 * @author NCSLab
 * @version 1.0
 * @since 2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AndroidResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Response code indicating the result status
     * 2000 - Success
     * 400 - Error
     */
    @JsonProperty("code")
    private int code;

    /**
     * Human-readable message describing the response status
     */
    @JsonProperty("message")
    private String message;

    /**
     * Name of the model being processed
     */
    @JsonProperty("modelName")
    private String modelName;

    /**
     * Response data containing file URLs
     */
    @JsonProperty("data")
    private AndroidDataDto data;

    @JsonProperty("NetConIPAddress")
    private String NetConIPAddress;

    /**
     * Creates a successful response with simulation results
     *
     * @param modelName name of the model
     * @param figFileUrl URL to the figure/scope file
     * @param dataFileUrl URL to the data JSON file
     * @return constructed success response
     */
    public static AndroidResponseDto success(String modelName, String figFileUrl, String dataFileUrl) {
        return AndroidResponseDto.builder()
                .code(2000)
                .message("SUCCESS")
                .modelName(modelName)
                .data(AndroidDataDto.of(figFileUrl, dataFileUrl))
                .build();
    }

    /**
     * Creates an error response with error message
     *
     * @param modelName name of the model (can be null)
     * @param errorMessage error description
     * @return constructed error response
     */
    public static AndroidResponseDto error(String modelName, String errorMessage) {
        return AndroidResponseDto.builder()
                .code(400)
                .message(errorMessage)
                .modelName(modelName)
                .data(null)
                .build();
    }

    /**
     * Creates an error response with just error message (no model name)
     *
     * @param errorMessage error description
     * @return constructed error response
     */
    public static AndroidResponseDto error(String errorMessage) {
        return error(null, errorMessage);
    }

    /**
     * Creates a successful compilation response with binary file URL
     *
     * @param modelName name of the compiled model
     * @param binaryFileUrl URL to the compiled binary file
     * @return constructed success response for compilation
     */
    public static AndroidResponseDto compilationSuccess(String modelName, String binaryFileUrl) {
        return AndroidResponseDto.builder()
                .code(2000)
                .message("SUCCESS")
                .modelName(modelName)
                .data(AndroidDataDto.ofBinary(binaryFileUrl))
                .build();
    }

    /**
     * Creates a compilation error response with error code 4000
     *
     * @param modelName name of the model (can be null)
     * @param errorMessage error description
     * @return constructed error response for compilation
     */
    public static AndroidResponseDto compilationError(String modelName, String errorMessage) {
        return AndroidResponseDto.builder()
                .code(4000)
                .message(errorMessage)
                .modelName(modelName)
                .data(null)
                .build();
    }

    /**
     * Creates a compilation error response with just error message (no model name)
     *
     * @param errorMessage error description
     * @return constructed error response for compilation
     */
    public static AndroidResponseDto compilationError(String errorMessage) {
        return compilationError(null, errorMessage);
    }

    /**
     * Validates the response structure
     *
     * @return true if response has valid structure
     */
    public boolean isValid() {
        if (code == 2000) {
            // Success response must have modelName and valid data
            return modelName != null && !modelName.isEmpty()
                && data != null && data.isValid();
        } else {
            // Error response must have a message
            return message != null && !message.isEmpty();
        }
    }

    /**
     * Checks if this is a success response
     *
     * @return true if code is 2000
     */
    public boolean isSuccess() {
        return code == 2000;
    }

    /**
     * Checks if this is an error response
     *
     * @return true if code is not 2000
     */
    public boolean isError() {
        return code != 2000;
    }
}
