package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncslab.dto.model.MdlDataDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * DTO for WebSocket Message JSON representation
 * Standardizes real-time communication messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessageDto {
    
    @JsonProperty("msg")
    private String msg; // Message type: "start", "generating", "compiling", "simulating", "error", "result"
    
    @JsonProperty("com")
    private String com; // Command: "start", "stop", "pause", "resume"
    
    @JsonProperty("status")
    private String status; // "success", "error", "in_progress"
    
    @JsonProperty("message")
    private String message; // Human-readable message
    
    @JsonProperty("error")
    private String error; // Error message if any
    
    @JsonProperty("data")
    private Object data; // Additional data payload
    
    @JsonProperty("progress")
    private Integer progress; // Progress percentage (0-100)
    
    @JsonProperty("time")
    private Double time; // Current simulation time
    
    @JsonProperty("timeLength")
    private Double timeLength; // Total simulation time length
    
    @JsonProperty("resultsFile")
    private String resultsFile; // Path to results file
    
    @JsonProperty("modelId")
    private Integer modelId;
    
    @JsonProperty("userId")
    private Integer userId;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    // Model data for start commands - now typed
    @JsonProperty("mdlData")
    private MdlDataDto mdlData;

    /**
     * When true with command {@code start}, the simulation worker prefers CUDA-capable native
     * paths (see {@link com.ncslab.simulation.SimulationBackendContext}). Use this on the
     * <strong>same</strong> WebSocket URL as normal simulation (e.g. {@code /websocketsimulate})
     * so nginx does not need a separate {@code location} for CUDA.
     */
    @JsonProperty("preferCudaSimulation")
    private Boolean preferCudaSimulation;

    // Step control fields
    @JsonProperty("steps")
    private Integer steps; // Number of steps for step_forward/step_backward
    
    @JsonProperty("customStepSize") 
    private Double customStepSize; // Custom step size override
    
    @JsonProperty("targetTime")
    private Double targetTime; // Target time for goto_time or step_backward
    
    @JsonProperty("direction")
    private String direction; // "forward" or "backward"
    
    @JsonProperty("mode")
    private String mode; // "continuous", "step_control"
    
    @JsonProperty("saveCheckpoint")
    private Boolean saveCheckpoint; // Whether to save checkpoint
    
    @JsonProperty("fromCheckpoint")
    private Boolean fromCheckpoint; // Resume from saved checkpoint
    
    @JsonProperty("checkpointId")
    private Integer checkpointId; // Checkpoint identifier
    
    @JsonProperty("capabilities")
    private String[] capabilities; // Available step control capabilities
    
    
    
    /**
     * Check if this is a command message (has 'com' field)
     * @return true if command message
     */
    public boolean isCommand() {
        return com != null;
    }
    
    /**
     * Check if this is a status message (has 'msg' field)
     * @return true if status message
     */
    public boolean isStatusMessage() {
        return msg != null;
    }
    
    /**
     * Check if this is an error message
     * @return true if error message
     */
    public boolean isError() {
        return error != null || "error".equals(msg) || "error".equals(status);
    }
    
    /**
     * Create start command message
     * @param mdlData Model data (typed)
     * @return Start command WebSocketMessageDto
     */
    public static WebSocketMessageDto createStartCommand(MdlDataDto mdlData) {
        return WebSocketMessageDto.builder()
                .com("start")
                .mdlData(mdlData)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create start command message from legacy Map
     * @param mdlDataMap Legacy Map<String, Object> structure
     * @return Start command WebSocketMessageDto
     */
    public static WebSocketMessageDto createStartCommandFromLegacyMap(Map<String, Object> mdlDataMap) {
        MdlDataDto mdlData = MdlDataDto.fromLegacyMap(mdlDataMap);
        return createStartCommand(mdlData);
    }
    
    /**
     * Create status message
     * @param msgType Message type
     * @param message Optional message text
     * @return Status WebSocketMessageDto
     */
    public static WebSocketMessageDto createStatusMessage(String msgType, String message) {
        return WebSocketMessageDto.builder()
                .msg(msgType)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create error message
     * @param error Error text
     * @return Error WebSocketMessageDto
     */
    public static WebSocketMessageDto createErrorMessage(String error) {
        return WebSocketMessageDto.builder()
                .msg("error")
                .status("error")
                .error(error)
                .message(error)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create simulation progress message
     * @param currentTime Current simulation time
     * @param totalTime Total simulation time
     * @return Progress WebSocketMessageDto
     */
    public static WebSocketMessageDto createSimulationProgress(double currentTime, double totalTime) {
        int progressPercent = (int) ((currentTime / totalTime) * 100);
        
        return WebSocketMessageDto.builder()
                .msg("simulating")
                .time(currentTime)
                .timeLength(totalTime)
                .progress(progressPercent)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create result message (file-based - legacy)
     * @param resultsFilePath Path to results file
     * @param userId User ID
     * @param modelId Model ID
     * @return Result WebSocketMessageDto
     */
    public static WebSocketMessageDto createResultMessage(String resultsFilePath, int userId, int modelId) {
        return WebSocketMessageDto.builder()
                .msg("result")
                .status("success")
                .resultsFile(resultsFilePath)
                .userId(userId)
                .modelId(modelId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create streaming result data message
     * @param scopeData Scope data payload
     * @param isChunked Whether this is chunked data (true) or final results (false)
     * @param chunkIndex Index of chunk (0-based) for chunked data
     * @param totalChunks Total number of chunks for chunked data
     * @return Streaming result WebSocketMessageDto
     */
    public static WebSocketMessageDto createStreamingResultMessage(Object scopeData, boolean isChunked, 
                                                                    Integer chunkIndex, Integer totalChunks) {
        WebSocketMessageDto.WebSocketMessageDtoBuilder builder = WebSocketMessageDto.builder()
                .msg("streaming_result")
                .status("success")
                .data(scopeData)
                .timestamp(System.currentTimeMillis());
        
        if (isChunked) {
            // Add chunk metadata for chunked streaming
            java.util.Map<String, Object> metadata = new java.util.HashMap<>();
            metadata.put("isChunked", true);
            metadata.put("chunkIndex", chunkIndex);
            metadata.put("totalChunks", totalChunks);
            
            // Store actual scope data separately
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("metadata", metadata);
            payload.put("scopeData", scopeData);
            builder.data(payload);
        }
        
        return builder.build();
    }
    
    /**
     * Create real-time scope data update message
     * @param blockUUID Block UUID
     * @param scopeData Current scope data
     * @param currentTime Current simulation time
     * @return Real-time scope update WebSocketMessageDto
     */
    public static WebSocketMessageDto createRealTimeScopeUpdate(String blockUUID, Object scopeData, double currentTime) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("blockUUID", blockUUID);
        payload.put("scopeData", scopeData);
        payload.put("time", currentTime);
        
        return WebSocketMessageDto.builder()
                .msg("scope_update")
                .status("success")
                .time(currentTime)
                .data(payload)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create final simulation results message (optimized)
     * @param allScopeData Complete scope data
     * @param userId User ID
     * @param modelId Model ID
     * @return Final results WebSocketMessageDto
     */
    public static WebSocketMessageDto createFinalResultsMessage(Object allScopeData, int userId, int modelId) {
        return WebSocketMessageDto.builder()
                .msg("final_results")
                .status("success")
                .data(allScopeData)
                .userId(userId)
                .modelId(modelId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    // ===== STEP CONTROL COMMAND FACTORY METHODS =====
    
    /**
     * Create step forward command
     * @param steps Number of steps to advance
     * @param customStepSize Custom step size (optional)
     * @return Step forward command WebSocketMessageDto
     */
    public static WebSocketMessageDto createStepForwardCommand(int steps, Double customStepSize) {
        return WebSocketMessageDto.builder()
                .com("step_forward")
                .steps(steps)
                .customStepSize(customStepSize)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create step backward command
     * @param steps Number of steps to go back
     * @param targetTime Specific target time (optional)
     * @return Step backward command WebSocketMessageDto
     */
    public static WebSocketMessageDto createStepBackwardCommand(int steps, Double targetTime) {
        return WebSocketMessageDto.builder()
                .com("step_backward")
                .steps(steps)
                .targetTime(targetTime)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create pause simulation command
     * @param saveCheckpoint Whether to save checkpoint at pause
     * @return Pause command WebSocketMessageDto
     */
    public static WebSocketMessageDto createPauseCommand(boolean saveCheckpoint) {
        return WebSocketMessageDto.builder()
                .com("pause")
                .saveCheckpoint(saveCheckpoint)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create resume simulation command
     * @param fromCheckpoint Whether to resume from saved checkpoint
     * @return Resume command WebSocketMessageDto
     */
    public static WebSocketMessageDto createResumeCommand(boolean fromCheckpoint) {
        return WebSocketMessageDto.builder()
                .com("resume")
                .fromCheckpoint(fromCheckpoint)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create goto time command
     * @param targetTime Target time to jump to
     * @param direction Direction of time travel ("forward" or "backward")
     * @return Goto time command WebSocketMessageDto
     */
    public static WebSocketMessageDto createGotoTimeCommand(double targetTime, String direction) {
        return WebSocketMessageDto.builder()
                .com("goto_time")
                .targetTime(targetTime)
                .direction(direction)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create set mode command
     * @param mode Simulation mode ("continuous" or "step_control")
     * @return Set mode command WebSocketMessageDto
     */
    public static WebSocketMessageDto createSetModeCommand(String mode) {
        return WebSocketMessageDto.builder()
                .com("set_mode")
                .mode(mode)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    // ===== STEP CONTROL RESPONSE FACTORY METHODS =====
    
    /**
     * Create step result message
     * @param currentTime Current simulation time after step
     * @param step Current step number
     * @param totalSteps Total steps being executed
     * @param direction Step direction ("forward" or "backward")
     * @param scopeData Scope data at current time
     * @return Step result WebSocketMessageDto
     */
    public static WebSocketMessageDto createStepResultMessage(double currentTime, int step, 
                                                              int totalSteps, String direction, Object scopeData) {
        return WebSocketMessageDto.builder()
                .msg("step_result")
                .status("success")
                .time(currentTime)
                .progress((int)((double)step / totalSteps * 100))
                .direction(direction)
                .data(scopeData)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create checkpoint saved message
     * @param time Time when checkpoint was saved
     * @param checkpointId Checkpoint identifier
     * @param memoryUsage Memory usage information
     * @return Checkpoint saved WebSocketMessageDto
     */
    public static WebSocketMessageDto createCheckpointSavedMessage(double time, int checkpointId, String memoryUsage) {
        return WebSocketMessageDto.builder()
                .msg("checkpoint_saved")
                .status("success")
                .time(time)
                .checkpointId(checkpointId)
                .message(memoryUsage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create mode changed message
     * @param mode New simulation mode
     * @param capabilities Available capabilities in this mode
     * @return Mode changed WebSocketMessageDto
     */
    public static WebSocketMessageDto createModeChangedMessage(String mode, String[] capabilities) {
        return WebSocketMessageDto.builder()
                .msg("mode_changed")
                .status("success")
                .mode(mode)
                .capabilities(capabilities)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
