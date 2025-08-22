package com.ncslab.dto.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * DTO for real-time scope data in step control simulation
 * Provides structured scope information with time series data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RealTimeScopeDto {
    
    /**
     * Scope metadata
     */
    private String name;
    private String path;
    private String uuid;
    private int width;
    private int height;
    
    /**
     * Step control context
     */
    private boolean stepControl;
    private double currentTime;
    private int currentStep;
    private double stepSize;
    private boolean isPaused;
    
    /**
     * Time series data
     */
    private List<Double> time;
    private List<Double> data;
    private int timePoints;
    private int dataPoints;
    private int length; // For compatibility
    
    /**
     * Real-time streaming metadata
     */
    private boolean realTimeUpdate;
    private long updateTimestamp;
    
    /**
     * Create a RealTimeScopeDto from scope block data
     * @param scope ScopeStruct containing the scope data
     * @param currentTime Current simulation time
     * @param currentStep Current step count
     * @param stepSize Current step size
     * @param isPaused Whether simulation is paused
     * @return RealTimeScopeDto instance
     */
    public static RealTimeScopeDto fromScopeStruct(com.ncslab.block.io.terminal.ScopeStruct scope,
                                                   double currentTime, int currentStep, 
                                                   double stepSize, boolean isPaused) {
        com.ncslab.block.sink.Scope scopeBlock = (com.ncslab.block.sink.Scope) scope.getBlock();
        
        // Extract time and data arrays (non-destructive)
        List<Double> timeList = new java.util.ArrayList<>();
        List<Double> dataList = new java.util.ArrayList<>();
        
        if (scope.getTimeList() != null && scope.getDataList() != null && !scope.getTimeList().isEmpty()) {
            // Create copies for real-time streaming (don't remove from original lists)
            List<Double> originalTimeList = new java.util.ArrayList<>(scope.getTimeList());
            List<Double> originalDataList = new java.util.ArrayList<>(scope.getDataList());
            
            int availablePoints = Math.min(originalTimeList.size(), 
                originalDataList.size() / (scope.getHeight() * scope.getWidth()));
            
            for (int i = 0; i < availablePoints; i++) {
                if (i < originalTimeList.size()) {
                    timeList.add(originalTimeList.get(i));
                }
                
                // Extract data for this time point
                int dataIndex = i * scope.getHeight() * scope.getWidth();
                for (int h = 0; h < scope.getHeight(); h++) {
                    for (int w = 0; w < scope.getWidth(); w++) {
                        int idx = dataIndex + h * scope.getWidth() + w;
                        if (idx < originalDataList.size()) {
                            dataList.add(originalDataList.get(idx));
                        } else {
                            dataList.add(0.0); // Default value
                        }
                    }
                }
            }
        }
        
        return RealTimeScopeDto.builder()
                .name(scopeBlock.getBlockName())
                .path(scopeBlock.getBlockPath())
                .uuid(scopeBlock.getBlockUUID())
                .width(scope.getWidth())
                .height(scope.getHeight())
                .stepControl(true)
                .currentTime(currentTime)
                .currentStep(currentStep)
                .stepSize(stepSize)
                .isPaused(isPaused)
                .time(timeList)
                .data(dataList)
                .timePoints(timeList.size())
                .dataPoints(dataList.size())
                .length(timeList.size()) // For compatibility
                .realTimeUpdate(true)
                .updateTimestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Convert to map for WebSocket transmission
     * @return Map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("name", name != null ? name : "");
        map.put("path", path != null ? path : "");
        map.put("uuid", uuid != null ? uuid : "");
        map.put("width", width);
        map.put("height", height);
        map.put("step_control", stepControl);
        map.put("current_time", currentTime);
        map.put("current_step", currentStep);
        map.put("step_size", stepSize);
        map.put("is_paused", isPaused);
        map.put("time", time != null ? time : List.of());
        map.put("data", data != null ? data : List.of());
        map.put("time_points", timePoints);
        map.put("data_points", dataPoints);
        map.put("length", length);
        map.put("real_time_update", realTimeUpdate);
        map.put("update_timestamp", updateTimestamp);
        return map;
    }
}