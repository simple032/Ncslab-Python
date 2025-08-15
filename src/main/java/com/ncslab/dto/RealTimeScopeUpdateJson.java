package com.ncslab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * DTO for complete real-time scope update containing all scopes and metadata
 * Used for step control simulation scope streaming
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RealTimeScopeUpdateJson {
    
    /**
     * Version and streaming metadata
     */
    private String version;
    private boolean stepControl;
    private boolean streaming;
    private boolean optimized;
    
    /**
     * Current simulation state
     */
    private double currentTime;
    private int currentStep;
    private double stepSize;
    private boolean isPaused;
    
    /**
     * Scope data
     */
    private List<RealTimeScopeJson> scopes;
    private int scopeCount;
    
    /**
     * Timestamps
     */
    private long timestamp;
    
    /**
     * Create a complete scope update from terminal list
     * @param terminals List of terminals to process
     * @param currentTime Current simulation time
     * @param currentStep Current step count
     * @param stepSize Current step size
     * @param isPaused Whether simulation is paused
     * @return RealTimeScopeUpdateJson instance
     */
    public static RealTimeScopeUpdateJson fromTerminalList(List<com.ncslab.block.io.terminal.Terminal> terminals,
                                                          double currentTime, int currentStep,
                                                          double stepSize, boolean isPaused) {
        List<RealTimeScopeJson> scopeList = new java.util.ArrayList<>();
        
        if (terminals != null) {
            for (com.ncslab.block.io.terminal.Terminal terminal : terminals) {
                if (terminal instanceof com.ncslab.block.io.terminal.ScopeStruct) {
                    com.ncslab.block.io.terminal.ScopeStruct scope = 
                        (com.ncslab.block.io.terminal.ScopeStruct) terminal;
                    
                    RealTimeScopeJson scopeJson = RealTimeScopeJson.fromScopeStruct(
                        scope, currentTime, currentStep, stepSize, isPaused);
                    scopeList.add(scopeJson);
                }
            }
        }
        
        return RealTimeScopeUpdateJson.builder()
                .version("0.3")
                .stepControl(true)
                .streaming(true)
                .optimized(true)
                .currentTime(currentTime)
                .currentStep(currentStep)
                .stepSize(stepSize)
                .isPaused(isPaused)
                .scopes(scopeList)
                .scopeCount(scopeList.size())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Convert to map for WebSocket transmission
     * @return Map representation
     */
    public Map<String, Object> toMap() {
        List<Map<String, Object>> scopeMaps = scopes != null ? 
            scopes.stream().map(RealTimeScopeJson::toMap).toList() : List.of();
        
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("version", version != null ? version : "0.3");
        map.put("step_control", stepControl);
        map.put("streaming", streaming);
        map.put("optimized", optimized);
        map.put("current_time", currentTime);
        map.put("current_step", currentStep);
        map.put("step_size", stepSize);
        map.put("is_paused", isPaused);
        map.put("scopes", scopeMaps);
        map.put("scope_count", scopeCount);
        map.put("timestamp", timestamp);
        return map;
    }
}