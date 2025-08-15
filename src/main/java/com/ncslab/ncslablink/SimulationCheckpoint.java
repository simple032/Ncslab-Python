package com.ncslab.ncslablink;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import Jama.Matrix;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * Simulation checkpoint for step control functionality
 * Stores complete simulation state at a specific time point
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationCheckpoint implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Timestamp when this checkpoint was created
     */
    private double timeStamp;
    
    /**
     * Simulation time at this checkpoint
     */
    private double simulationTime;
    
    /**
     * Snapshot of continuous state variables
     */
    private double[] stateSnapshot;
    
    /**
     * Snapshot of discrete state variables (block UUID -> state data)
     */
    private Map<String, Object> discreteStateSnapshot;
    
    /**
     * Snapshot of block output values at this time
     */
    private JSONObject outputSnapshot;
    
    /**
     * Checkpoint ID for identification
     */
    private int checkpointId;
    
    /**
     * Memory usage estimate in bytes
     */
    private long memoryUsage;
    
    /**
     * Create deep copy of state array
     * @param original Original state array
     * @return Deep copy of the array
     */
    public static double[] copyStateArray(double[] original) {
        if (original == null) return null;
        return Arrays.copyOf(original, original.length);
    }
    
    /**
     * Create deep copy of discrete state map
     * @param original Original discrete state map
     * @return Deep copy of the map
     */
    public static Map<String, Object> copyDiscreteStateMap(Map<String, Object> original) {
        if (original == null) return new HashMap<>();
        
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Matrix) {
                Matrix matrix = (Matrix) value;
                copy.put(entry.getKey(), matrix.copy());
            } else if (value instanceof double[]) {
                copy.put(entry.getKey(), Arrays.copyOf((double[]) value, ((double[]) value).length));
            } else {
                // For primitive types and immutable objects
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }
    
    /**
     * Estimate memory usage of this checkpoint
     * @return Estimated memory usage in bytes
     */
    public long estimateMemoryUsage() {
        long usage = 0;
        
        // Base object overhead
        usage += 48; // Object header + fields
        
        // State array
        if (stateSnapshot != null) {
            usage += 24 + (stateSnapshot.length * 8); // Array header + double values
        }
        
        // Discrete state map
        if (discreteStateSnapshot != null) {
            usage += 48; // HashMap overhead
            for (Map.Entry<String, Object> entry : discreteStateSnapshot.entrySet()) {
                usage += 24; // Entry overhead
                usage += entry.getKey().length() * 2; // String key
                
                Object value = entry.getValue();
                if (value instanceof Matrix) {
                    Matrix matrix = (Matrix) value;
                    usage += 32 + (matrix.getRowDimension() * matrix.getColumnDimension() * 8);
                } else if (value instanceof double[]) {
                    usage += 24 + (((double[]) value).length * 8);
                } else {
                    usage += 16; // Rough estimate for other objects
                }
            }
        }
        
        // Output snapshot (JSON)
        if (outputSnapshot != null) {
            usage += outputSnapshot.toString().length() * 2; // Rough estimate
        }
        
        this.memoryUsage = usage;
        return usage;
    }
    
    /**
     * Get human-readable memory usage string
     * @return Memory usage formatted as string
     */
    public String getMemoryUsageString() {
        long bytes = getMemoryUsage();
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Validate checkpoint integrity
     * @return true if checkpoint is valid
     */
    public boolean isValid() {
        return simulationTime >= 0 && 
               stateSnapshot != null && 
               discreteStateSnapshot != null &&
               checkpointId >= 0;
    }
    
    @Override
    public String toString() {
        return String.format("Checkpoint[id=%d, time=%.3f, states=%d, memory=%s]", 
                           checkpointId, simulationTime, 
                           stateSnapshot != null ? stateSnapshot.length : 0,
                           getMemoryUsageString());
    }
}