package com.ncslab.dto.mapper.exception;

import java.util.List;
import java.util.ArrayList;

/**
 * Exception thrown when batch mapping operations fail.
 * Contains information about multiple mapping failures that occurred during batch processing.
 */
public class BatchMappingException extends MappingException {
    
    private final List<MappingException> failedMappings = new ArrayList<>();
    private final int totalCount;
    private final int failedCount;
    
    public BatchMappingException(String message) {
        super(message);
        this.totalCount = 0;
        this.failedCount = 0;
    }
    
    public BatchMappingException(String message, Throwable cause) {
        super(message, cause);
        this.totalCount = 0;
        this.failedCount = 1;
    }
    
    public BatchMappingException(String message, List<MappingException> failedMappings, 
                               int totalCount) {
        super(message);
        this.failedMappings.addAll(failedMappings);
        this.totalCount = totalCount;
        this.failedCount = failedMappings.size();
    }
    
    public BatchMappingException(String message, MappingException singleFailure, 
                               int totalCount, int failedCount) {
        super(message, singleFailure);
        this.failedMappings.add(singleFailure);
        this.totalCount = totalCount;
        this.failedCount = failedCount;
    }
    
    public List<MappingException> getFailedMappings() {
        return new ArrayList<>(failedMappings);
    }
    
    public void addMappingException(MappingException exception) {
        if (exception != null) {
            failedMappings.add(exception);
        }
    }
    
    public int getTotalCount() { return totalCount; }
    public int getFailedCount() { return failedCount; }
    public int getSuccessfulCount() { return totalCount - failedCount; }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        sb.append(" (").append(failedCount).append("/").append(totalCount).append(" failed)");
        
        if (!failedMappings.isEmpty()) {
            sb.append("\nFirst failure: ").append(failedMappings.get(0).getMessage());
        }
        
        return sb.toString();
    }
}