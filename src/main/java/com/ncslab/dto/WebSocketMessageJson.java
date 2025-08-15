package com.ncslab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.json.JSONObject;

import java.util.Map;

/**
 * DTO for WebSocket Message JSON representation
 * Standardizes real-time communication messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessageJson {
    
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
    
    // Model data for start commands
    @JsonProperty("mdlData")
    private Map<String, Object> mdlData;
    
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
     * Create WebSocketMessageJson from legacy JSONObject
     * @param jsonObject Legacy JSONObject
     * @return WebSocketMessageJson DTO or null if conversion fails
     */
    public static WebSocketMessageJson fromLegacyJson(JSONObject jsonObject) {
        try {
            WebSocketMessageJson message = new WebSocketMessageJson();
            
            // Map common fields
            message.setMsg(jsonObject.optString("msg", null));
            message.setCom(jsonObject.optString("com", null));
            message.setStatus(jsonObject.optString("status", null));
            message.setMessage(jsonObject.optString("message", null));
            message.setError(jsonObject.optString("error", null));
            message.setProgress(jsonObject.has("progress") ? jsonObject.getInt("progress") : null);
            message.setTime(jsonObject.has("time") ? jsonObject.getDouble("time") : null);
            message.setTimeLength(jsonObject.has("timeLength") ? jsonObject.getDouble("timeLength") : null);
            message.setResultsFile(jsonObject.optString("resultsFile", null));
            message.setModelId(jsonObject.has("modelId") ? jsonObject.getInt("modelId") : null);
            message.setUserId(jsonObject.has("userId") ? jsonObject.getInt("userId") : null);
            message.setSessionId(jsonObject.optString("sessionId", null));
            
            // Handle step control fields
            message.setSteps(jsonObject.has("steps") ? jsonObject.getInt("steps") : null);
            message.setCustomStepSize(jsonObject.has("customStepSize") ? jsonObject.getDouble("customStepSize") : null);
            message.setTargetTime(jsonObject.has("targetTime") ? jsonObject.getDouble("targetTime") : null);
            message.setDirection(jsonObject.optString("direction", null));
            message.setMode(jsonObject.optString("mode", null));
            message.setSaveCheckpoint(jsonObject.has("saveCheckpoint") ? jsonObject.getBoolean("saveCheckpoint") : null);
            message.setFromCheckpoint(jsonObject.has("fromCheckpoint") ? jsonObject.getBoolean("fromCheckpoint") : null);
            message.setCheckpointId(jsonObject.has("checkpointId") ? jsonObject.getInt("checkpointId") : null);
            
            // Handle capabilities array
            if (jsonObject.has("capabilities")) {
                org.json.JSONArray capArray = jsonObject.getJSONArray("capabilities");
                String[] capabilities = new String[capArray.length()];
                for (int i = 0; i < capArray.length(); i++) {
                    capabilities[i] = capArray.getString(i);
                }
                message.setCapabilities(capabilities);
            }
            
            // Handle data field (can be various types)
            if (jsonObject.has("data")) {
                message.setData(jsonObject.get("data"));
            }
            
            // Handle mdlData field
            if (jsonObject.has("mdlData")) {
                JSONObject mdlDataJson = jsonObject.getJSONObject("mdlData");
                // Convert to map for easier handling
                Map<String, Object> mdlDataMap = mdlDataJson.toMap();
                message.setMdlData(mdlDataMap);
            }
            
            message.setTimestamp(System.currentTimeMillis());
            
            return message;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Convert to legacy JSONObject format
     * @deprecated Use Jackson serialization with JsonUtils.serializeDto() instead
     * @return JSONObject representation
     */
    @Deprecated
    public JSONObject toLegacyJson() {
        JSONObject json = new JSONObject();
        
        if (msg != null) json.put("msg", msg);
        if (com != null) json.put("com", com);
        if (status != null) json.put("status", status);
        if (message != null) json.put("message", message);
        if (error != null) json.put("error", error);
        if (progress != null) json.put("progress", progress);
        if (time != null) json.put("time", time);
        if (timeLength != null) json.put("timeLength", timeLength);
        if (resultsFile != null) json.put("resultsFile", resultsFile);
        if (modelId != null) json.put("modelId", modelId);
        if (userId != null) json.put("userId", userId);
        if (sessionId != null) json.put("sessionId", sessionId);
        if (data != null) json.put("data", data);
        if (mdlData != null) json.put("mdlData", new JSONObject(mdlData));
        
        // Add step control fields
        if (steps != null) json.put("steps", steps);
        if (customStepSize != null) json.put("customStepSize", customStepSize);
        if (targetTime != null) json.put("targetTime", targetTime);
        if (direction != null) json.put("direction", direction);
        if (mode != null) json.put("mode", mode);
        if (saveCheckpoint != null) json.put("saveCheckpoint", saveCheckpoint);
        if (fromCheckpoint != null) json.put("fromCheckpoint", fromCheckpoint);
        if (checkpointId != null) json.put("checkpointId", checkpointId);
        if (capabilities != null) {
            org.json.JSONArray capArray = new org.json.JSONArray();
            for (String cap : capabilities) {
                capArray.put(cap);
            }
            json.put("capabilities", capArray);
        }
        
        return json;
    }
    
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
     * @param mdlData Model data
     * @return Start command WebSocketMessageJson
     */
    public static WebSocketMessageJson createStartCommand(Map<String, Object> mdlData) {
        return WebSocketMessageJson.builder()
                .com("start")
                .mdlData(mdlData)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create status message
     * @param msgType Message type
     * @param message Optional message text
     * @return Status WebSocketMessageJson
     */
    public static WebSocketMessageJson createStatusMessage(String msgType, String message) {
        return WebSocketMessageJson.builder()
                .msg(msgType)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create error message
     * @param error Error text
     * @return Error WebSocketMessageJson
     */
    public static WebSocketMessageJson createErrorMessage(String error) {
        return WebSocketMessageJson.builder()
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
     * @return Progress WebSocketMessageJson
     */
    public static WebSocketMessageJson createSimulationProgress(double currentTime, double totalTime) {
        int progressPercent = (int) ((currentTime / totalTime) * 100);
        
        return WebSocketMessageJson.builder()
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
     * @return Result WebSocketMessageJson
     */
    public static WebSocketMessageJson createResultMessage(String resultsFilePath, int userId, int modelId) {
        return WebSocketMessageJson.builder()
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
     * @return Streaming result WebSocketMessageJson
     */
    public static WebSocketMessageJson createStreamingResultMessage(Object scopeData, boolean isChunked, 
                                                                    Integer chunkIndex, Integer totalChunks) {
        WebSocketMessageJson.WebSocketMessageJsonBuilder builder = WebSocketMessageJson.builder()
                .msg("streaming_result")
                .status("success")
                .data(scopeData)
                .timestamp(System.currentTimeMillis());
        
        if (isChunked) {
            // Add chunk metadata for chunked streaming
            JSONObject metadata = new JSONObject();
            metadata.put("isChunked", true);
            metadata.put("chunkIndex", chunkIndex);
            metadata.put("totalChunks", totalChunks);
            builder.data(metadata.toMap());
            
            // Store actual scope data separately
            JSONObject payload = new JSONObject();
            payload.put("metadata", metadata);
            payload.put("scopeData", scopeData);
            builder.data(payload.toMap());
        }
        
        return builder.build();
    }
    
    /**
     * Create real-time scope data update message
     * @param blockUUID Block UUID
     * @param scopeData Current scope data
     * @param currentTime Current simulation time
     * @return Real-time scope update WebSocketMessageJson
     */
    public static WebSocketMessageJson createRealTimeScopeUpdate(String blockUUID, Object scopeData, double currentTime) {
        JSONObject payload = new JSONObject();
        payload.put("blockUUID", blockUUID);
        payload.put("scopeData", scopeData);
        payload.put("time", currentTime);
        
        return WebSocketMessageJson.builder()
                .msg("scope_update")
                .status("success")
                .time(currentTime)
                .data(payload.toMap())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create final simulation results message (optimized)
     * @param allScopeData Complete scope data
     * @param userId User ID
     * @param modelId Model ID
     * @return Final results WebSocketMessageJson
     */
    public static WebSocketMessageJson createFinalResultsMessage(Object allScopeData, int userId, int modelId) {
        return WebSocketMessageJson.builder()
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
     * @return Step forward command WebSocketMessageJson
     */
    public static WebSocketMessageJson createStepForwardCommand(int steps, Double customStepSize) {
        return WebSocketMessageJson.builder()
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
     * @return Step backward command WebSocketMessageJson
     */
    public static WebSocketMessageJson createStepBackwardCommand(int steps, Double targetTime) {
        return WebSocketMessageJson.builder()
                .com("step_backward")
                .steps(steps)
                .targetTime(targetTime)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create pause simulation command
     * @param saveCheckpoint Whether to save checkpoint at pause
     * @return Pause command WebSocketMessageJson
     */
    public static WebSocketMessageJson createPauseCommand(boolean saveCheckpoint) {
        return WebSocketMessageJson.builder()
                .com("pause")
                .saveCheckpoint(saveCheckpoint)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create resume simulation command
     * @param fromCheckpoint Whether to resume from saved checkpoint
     * @return Resume command WebSocketMessageJson
     */
    public static WebSocketMessageJson createResumeCommand(boolean fromCheckpoint) {
        return WebSocketMessageJson.builder()
                .com("resume")
                .fromCheckpoint(fromCheckpoint)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create goto time command
     * @param targetTime Target time to jump to
     * @param direction Direction of time travel ("forward" or "backward")
     * @return Goto time command WebSocketMessageJson
     */
    public static WebSocketMessageJson createGotoTimeCommand(double targetTime, String direction) {
        return WebSocketMessageJson.builder()
                .com("goto_time")
                .targetTime(targetTime)
                .direction(direction)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create set mode command
     * @param mode Simulation mode ("continuous" or "step_control")
     * @return Set mode command WebSocketMessageJson
     */
    public static WebSocketMessageJson createSetModeCommand(String mode) {
        return WebSocketMessageJson.builder()
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
     * @return Step result WebSocketMessageJson
     */
    public static WebSocketMessageJson createStepResultMessage(double currentTime, int step, 
                                                              int totalSteps, String direction, Object scopeData) {
        return WebSocketMessageJson.builder()
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
     * @return Checkpoint saved WebSocketMessageJson
     */
    public static WebSocketMessageJson createCheckpointSavedMessage(double time, int checkpointId, String memoryUsage) {
        return WebSocketMessageJson.builder()
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
     * @return Mode changed WebSocketMessageJson
     */
    public static WebSocketMessageJson createModeChangedMessage(String mode, String[] capabilities) {
        return WebSocketMessageJson.builder()
                .msg("mode_changed")
                .status("success")
                .mode(mode)
                .capabilities(capabilities)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}