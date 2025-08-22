package com.ncslab.ncslablink;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.json.JSONArray;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.util.JsonUtils;
import com.ncslab.block.Block;
import com.ncslab.block.io.State;
import com.ncslab.block.data.DataType;
import Jama.Matrix;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.nonstiff.*;
import org.apache.commons.math3.ode.MultistepIntegrator;
import org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

/**
 * Enhanced SimulationModel with step control and checkpointing capabilities
 */
public class StepControlSimulation extends SimulationModel {
    
    @Getter
    private List<SimulationCheckpoint> checkpoints = new ArrayList<>();
    
    @Getter @Setter
    private int currentCheckpointIndex = -1;
    
    @Getter @Setter
    private boolean stepMode = false;
    
    @Getter @Setter
    private double customStepSize = 0.0;
    
    @Getter @Setter
    private boolean isPaused = false;
    
    @Getter
    private double currentTime = 0.0;
    
    @Getter @Setter
    private int maxCheckpoints = 1000; // Configurable limit
    
    private AtomicInteger checkpointIdCounter = new AtomicInteger(0);
    
    // Constructors
    public StepControlSimulation(JSONObject jsonIn, ModelMode mode) throws ModelException {
        super(jsonIn, mode);
        logConfigurationParameters();
    }
    
    public StepControlSimulation(ModelDto modelDto, ModelMode mode) throws ModelException {
        super(modelDto, mode);
        logConfigurationParameters();
    }
    
    /**
     * Log configuration parameters for debugging
     */
    private void logConfigurationParameters() {
        Config config = getConfig();
        System.out.println("=== Step Control Simulation Configuration ===");
        System.out.printf("Step Mode: %s%n", config.getStep());
        System.out.printf("Solver: %s%n", config.getSolver());
        System.out.printf("Fixed Step: %.6f%n", config.getFixedStep());
        System.out.printf("Start Time: %.6f%n", config.getStartTime());
        System.out.printf("Stop Time: %.6f%n", config.getStopTime());
        System.out.printf("Max Data Points: %d%n", config.getMaxDataPoints());
        System.out.printf("Max Step: %.6f%n", config.getMaxStep());
        System.out.printf("Min Step: %.6f%n", config.getMinStep());
        System.out.printf("Initial Step: %.6f%n", config.getInitialStep());
        System.out.printf("Relative Tolerance: %.2e%n", config.getRelTol());
        System.out.printf("Absolute Tolerance: %.2e%n", config.getAbsTol());
        System.out.printf("Effective Step Size: %.6f%n", config.getEffectiveStepSize());
        System.out.printf("Is Variable Step: %b%n", config.isVariableStep());
        System.out.println("=============================================");
    }
    
    // Factory methods
    public static StepControlSimulation createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
        return new StepControlSimulation(jsonIn, mode);
    }

    public static StepControlSimulation createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
        return new StepControlSimulation(modelDto, mode);
    }
    
    /**
     * Save current simulation state as checkpoint
     * @return Created checkpoint
     */
    public SimulationCheckpoint saveCheckpoint() {
        int checkpointId = checkpointIdCounter.incrementAndGet();
        
        // Create state snapshot
        double[] stateSnapshot = SimulationCheckpoint.copyStateArray(getStatesArray());
        
        // Create discrete state snapshot
        Map<String, Object> discreteStateSnapshot = captureDiscreteStates();
        
        // Create output snapshot
        JSONObject outputSnapshot = captureOutputs();
        
        SimulationCheckpoint checkpoint = SimulationCheckpoint.builder()
                .timeStamp(System.currentTimeMillis())
                .simulationTime(currentTime)
                .stateSnapshot(stateSnapshot)
                .discreteStateSnapshot(discreteStateSnapshot)
                .outputSnapshot(outputSnapshot)
                .checkpointId(checkpointId)
                .build();
        
        checkpoint.estimateMemoryUsage();
        
        // Add to checkpoint list
        checkpoints.add(checkpoint);
        currentCheckpointIndex = checkpoints.size() - 1;
        
        // Enforce memory limits
        enforceCheckpointLimits();
        
        System.out.printf("Checkpoint saved: %s%n", checkpoint);
        return checkpoint;
    }
    
    /**
     * Restore simulation to a specific checkpoint
     * @param checkpointIndex Index in checkpoint list
     * @return true if restoration successful
     */
    public boolean restoreCheckpoint(int checkpointIndex) {
        if (checkpointIndex < 0 || checkpointIndex >= checkpoints.size()) {
            System.err.printf("Invalid checkpoint index: %d (available: 0-%d)%n", 
                            checkpointIndex, checkpoints.size() - 1);
            return false;
        }
        
        SimulationCheckpoint checkpoint = checkpoints.get(checkpointIndex);
        
        if (!checkpoint.isValid()) {
            System.err.printf("Checkpoint %d is invalid%n", checkpointIndex);
            return false;
        }
        
        // Restore continuous states
        restoreStatesArray(checkpoint.getStateSnapshot());
        
        // Restore discrete states
        restoreDiscreteStates(checkpoint.getDiscreteStateSnapshot());
        
        // Update current time
        this.currentTime = checkpoint.getSimulationTime();
        this.currentCheckpointIndex = checkpointIndex;
        
        System.out.printf("Restored to checkpoint: %s%n", checkpoint);
        return true;
    }
    
    /**
     * Restore to specific simulation time using nearest checkpoint
     * @param targetTime Target simulation time
     * @return true if restoration successful
     */
    public boolean restoreToTime(double targetTime) {
        if (checkpoints.isEmpty()) {
            System.err.println("No checkpoints available for time restoration");
            return false;
        }
        
        // Find nearest checkpoint at or before target time
        int bestIndex = -1;
        double bestTime = -1;
        
        for (int i = 0; i < checkpoints.size(); i++) {
            double checkpointTime = checkpoints.get(i).getSimulationTime();
            if (checkpointTime <= targetTime && checkpointTime > bestTime) {
                bestTime = checkpointTime;
                bestIndex = i;
            }
        }
        
        if (bestIndex == -1) {
            System.err.printf("No checkpoint found before target time %.3f%n", targetTime);
            return false;
        }
        
        return restoreCheckpoint(bestIndex);
    }
    
    /**
     * Get simulation states array (protected accessor)
     * @return Current states array
     */
    protected double[] getStatesArray() {
        // Use reflection to access private states field from parent class
        try {
            java.lang.reflect.Field statesField = SimulationModel.class.getDeclaredField("states");
            statesField.setAccessible(true);
            return (double[]) statesField.get(this);
        } catch (Exception e) {
            System.err.println("Failed to access states array: " + e.getMessage());
            return new double[0];
        }
    }
    
    /**
     * Restore states array
     * @param stateSnapshot State snapshot to restore
     */
    private void restoreStatesArray(double[] stateSnapshot) {
        try {
            java.lang.reflect.Field statesField = SimulationModel.class.getDeclaredField("states");
            statesField.setAccessible(true);
            double[] currentStates = (double[]) statesField.get(this);
            
            if (stateSnapshot != null && currentStates != null) {
                System.arraycopy(stateSnapshot, 0, currentStates, 0, 
                               Math.min(stateSnapshot.length, currentStates.length));
            }
        } catch (Exception e) {
            System.err.println("Failed to restore states array: " + e.getMessage());
        }
    }
    
    /**
     * Capture discrete states from all blocks
     * @return Map of block UUID to discrete state data
     */
    private Map<String, Object> captureDiscreteStates() {
        Map<String, Object> discreteStates = new HashMap<>();
        
        for (Block block : getBlockList()) {
            String blockUUID = block.getBlockUUID();
            
            // Capture discrete states for this block
            Map<String, Object> blockStates = new HashMap<>();
            
            for (State state : block.getStateList()) {
                if (state.getDataType() == DataType.REAL) {
                    blockStates.put(state.getName(), state.getData().getInitValue());
                } else {
                    // Matrix state - create deep copy
                    Matrix matrix = state.getData().getMatrix();
                    if (matrix != null) {
                        blockStates.put(state.getName(), matrix.copy());
                    }
                }
            }
            
            if (!blockStates.isEmpty()) {
                discreteStates.put(blockUUID, blockStates);
            }
        }
        
        return discreteStates;
    }
    
    /**
     * Restore discrete states to all blocks
     * @param discreteStateSnapshot Discrete state snapshot
     */
    private void restoreDiscreteStates(Map<String, Object> discreteStateSnapshot) {
        if (discreteStateSnapshot == null) return;
        
        for (Block block : getBlockList()) {
            String blockUUID = block.getBlockUUID();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> blockStates = (Map<String, Object>) discreteStateSnapshot.get(blockUUID);
            
            if (blockStates != null) {
                for (State state : block.getStateList()) {
                    Object stateValue = blockStates.get(state.getName());
                    
                    if (stateValue != null) {
                        if (state.getDataType() == DataType.REAL && stateValue instanceof Double) {
                            state.getData().setInitValue((Double) stateValue);
                        } else if (stateValue instanceof Matrix) {
                            Matrix restoredMatrix = (Matrix) stateValue;
                            state.getData().setMatrix(restoredMatrix.copy());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Capture current outputs from all blocks
     * @return JSON object with current outputs
     */
    private JSONObject captureOutputs() {
        JSONObject outputs = new JSONObject();
        
        for (Block block : getBlockList()) {
            JSONObject blockOutputs = new JSONObject();
            
            block.getOutputPortList().forEach(outputPort -> {
                if (outputPort.getOutputSignalC() != null) {
                    JSONObject signalData = new JSONObject();
                    signalData.put("value", outputPort.getOutputSignalC().getData().getInitValue());
                    signalData.put("dataType", outputPort.getOutputSignalC().getDataType().toString());
                    blockOutputs.put(outputPort.getOutputSignalC().getName(), signalData);
                }
            });
            
            if (blockOutputs.length() > 0) {
                outputs.put(block.getBlockUUID(), blockOutputs);
            }
        }
        
        return outputs;
    }
    
    /**
     * Enforce checkpoint memory limits
     */
    private void enforceCheckpointLimits() {
        while (checkpoints.size() > maxCheckpoints) {
            SimulationCheckpoint removed = checkpoints.remove(0);
            System.out.printf("Removed old checkpoint: %s%n", removed);
            
            // Update current index
            if (currentCheckpointIndex > 0) {
                currentCheckpointIndex--;
            }
        }
    }
    
    /**
     * Get total memory usage of all checkpoints
     * @return Total memory usage in bytes
     */
    public long getTotalCheckpointMemory() {
        return checkpoints.stream()
                .mapToLong(SimulationCheckpoint::getMemoryUsage)
                .sum();
    }
    
    /**
     * Get checkpoint statistics
     * @return Map with checkpoint statistics
     */
    public Map<String, Object> getCheckpointStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCheckpoints", checkpoints.size());
        stats.put("currentIndex", currentCheckpointIndex);
        stats.put("totalMemory", getTotalCheckpointMemory());
        stats.put("maxCheckpoints", maxCheckpoints);
        stats.put("stepMode", stepMode);
        stats.put("isPaused", isPaused);
        
        if (!checkpoints.isEmpty()) {
            stats.put("earliestTime", checkpoints.get(0).getSimulationTime());
            stats.put("latestTime", checkpoints.get(checkpoints.size() - 1).getSimulationTime());
        }
        
        return stats;
    }
    
    /**
     * Clear all checkpoints
     */
    public void clearCheckpoints() {
        checkpoints.clear();
        currentCheckpointIndex = -1;
        checkpointIdCounter.set(0);
        System.out.println("All checkpoints cleared");
    }
    
    /**
     * Set current simulation time (for step control)
     * @param time New current time
     */
    public void setCurrentTime(double time) {
        this.currentTime = time;
    }
    
    // ===== STEP CONTROL METHODS =====
    
    /**
     * Step forward by specified number of steps
     * @param session WebSocket session for real-time updates
     * @param numSteps Number of steps to advance
     * @param customStep Custom step size (null to use config step size)
     * @throws ModelException if simulation error occurs
     */
    public void stepForward(Session session, int numSteps, Double customStep) throws ModelException {
        if (numSteps <= 0) {
            throw new ModelException("Number of steps must be positive");
        }
        
        double stepSize = (customStep != null) ? customStep : getConfig().getEffectiveStepSize();
        System.out.printf("Stepping forward %d steps with step size %.6f from time %.6f%n", 
                         numSteps, stepSize, currentTime);
        
        try {
            // Create integrator for stepping
            FirstOrderIntegrator integrator = createIntegrator(stepSize);
            FirstOrderDifferentialEquations systemODE = createSystemODE();
            
            for (int i = 0; i < numSteps; i++) {
                // Save checkpoint before step (adaptive checkpointing)
                if (i == 0 || (i + 1) % 10 == 0) { // Save every 10 steps
                    SimulationCheckpoint checkpoint = saveCheckpoint();
                    sendCheckpointMessage(session, checkpoint);
                }
                
                double targetTime = currentTime + stepSize;
                
                // Perform single integration step
                double actualEndTime = integrator.integrate(systemODE, currentTime, 
                                                          getStatesArray(), targetTime, getStatesArray());
                
                // Update current time
                setCurrentTime(actualEndTime);
                
                // Calculate outputs and discrete updates
                calculateOutputs(currentTime);
                calculateDiscreteUpdates(currentTime);
                
                // Send step result to client
                sendStepResult(session, currentTime, i + 1, numSteps, "forward");
                
                System.out.printf("Step %d/%d completed: time=%.6f%n", i + 1, numSteps, currentTime);
            }
            
            System.out.printf("Forward stepping completed: final time=%.6f%n", currentTime);
            
        } catch (Exception e) {
            throw new ModelException("Step forward failed: " + e.getMessage());
        }
    }
    
    /**
     * Step backward by specified number of steps or to target time
     * @param session WebSocket session for real-time updates
     * @param numSteps Number of steps to go back (ignored if targetTime specified)
     * @param targetTime Specific target time to go back to (optional)
     * @throws ModelException if restoration fails
     */
    public void stepBackward(Session session, int numSteps, Double targetTime) throws ModelException {
        if (checkpoints.isEmpty()) {
            throw new ModelException("No checkpoints available for backward stepping");
        }
        
        boolean restored = false;
        
        if (targetTime != null) {
            // Jump to specific time using checkpoints
            System.out.printf("Stepping backward to target time %.6f%n", targetTime);
            restored = restoreToTime(targetTime);
            
            if (restored) {
                sendStepResult(session, currentTime, 1, 1, "backward");
            }
        } else {
            // Step back N steps using checkpoint history
            if (numSteps <= 0) {
                throw new ModelException("Number of backward steps must be positive");
            }
            
            System.out.printf("Stepping backward %d steps from checkpoint index %d%n", 
                             numSteps, currentCheckpointIndex);
            
            int targetIndex = Math.max(0, currentCheckpointIndex - numSteps);
            restored = restoreCheckpoint(targetIndex);
            
            if (restored) {
                sendStepResult(session, currentTime, -numSteps, numSteps, "backward");
            }
        }
        
        if (!restored) {
            throw new ModelException("Backward stepping failed - could not restore to target state");
        }
        
        // Recalculate outputs after restoration
        calculateOutputs(currentTime);
        calculateDiscreteUpdates(currentTime);
        
        System.out.printf("Backward stepping completed: current time=%.6f%n", currentTime);
    }
    
    /**
     * Pause simulation and optionally save checkpoint
     * @param session WebSocket session
     * @param saveCheckpoint Whether to save checkpoint at pause
     * @throws IOException if WebSocket communication fails
     */
    public void pauseSimulation(Session session, boolean saveCheckpoint) throws IOException {
        this.isPaused = true;
        
        if (saveCheckpoint) {
            SimulationCheckpoint checkpoint = saveCheckpoint();
            sendCheckpointMessage(session, checkpoint);
        }
        
        WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage("paused", 
            "Simulation paused at time " + String.format("%.6f", currentTime));
        if (session != null) {
            session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(message));
        }
        
        System.out.printf("Simulation paused at time %.6f%n", currentTime);
    }
    
    /**
     * Resume simulation from current state or checkpoint
     * @param session WebSocket session
     * @param fromCheckpoint Whether to resume from last saved checkpoint
     * @throws IOException if WebSocket communication fails
     */
    public void resumeSimulation(Session session, boolean fromCheckpoint) throws IOException {
        if (fromCheckpoint && currentCheckpointIndex >= 0) {
            restoreCheckpoint(currentCheckpointIndex);
            calculateOutputs(currentTime);
            calculateDiscreteUpdates(currentTime);
        }
        
        this.isPaused = false;
        
        WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage("resumed", 
            "Simulation resumed at time " + String.format("%.6f", currentTime));
        if (session != null) {
            session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(message));
        }
        
        System.out.printf("Simulation resumed at time %.6f%n", currentTime);
    }
    
    /**
     * Switch simulation mode between continuous and step control
     * @param session WebSocket session
     * @param mode New simulation mode ("continuous" or "step_control")
     * @throws IOException if WebSocket communication fails
     */
    public void setSimulationMode(Session session, String mode) throws IOException {
        boolean oldStepMode = this.stepMode;
        
        if ("step_control".equals(mode)) {
            this.stepMode = true;
            if (!oldStepMode) {
                // Switching to step control - save initial checkpoint
                saveCheckpoint();
            }
        } else if ("continuous".equals(mode)) {
            this.stepMode = false;
        } else {
            throw new IllegalArgumentException("Invalid simulation mode: " + mode);
        }
        
        // Define capabilities based on mode
        String[] capabilities;
        if (stepMode) {
            capabilities = new String[]{"step_forward", "step_backward", "goto_time", "pause", "resume"};
        } else {
            capabilities = new String[]{"pause", "resume"};
        }
        
        WebSocketMessageDto message = WebSocketMessageDto.createModeChangedMessage(mode, capabilities);
        if (session != null) {
            session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(message));
        }
        
        System.out.printf("Simulation mode changed to: %s%n", mode);
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Create integrator based on current configuration
     * @param stepSize Step size to use
     * @return Configured integrator
     */
    private FirstOrderIntegrator createIntegrator(double stepSize) {
        Config config = getConfig();
        double absTol = config.getAbsTol();
        double relTol = config.getRelTol();
        double minStep = config.getMinStep();
        double maxStep = config.getMaxStep();
        
        // Use config-specified max step if available, otherwise use step size
        if (maxStep <= 0) {
            maxStep = stepSize;
        }
        
        String solver = config.getSolver();
        System.out.printf("Creating integrator: solver=%s, step=%s, stepSize=%.6f, minStep=%.6f, maxStep=%.6f%n", 
                         solver, config.getStep(), stepSize, minStep, maxStep);
        
        // Choose integrator based on step mode and solver
        if (config.isFixedStep()) {
            // Fixed-step integrators
            switch (solver) {
                case "ode1":
                case "EulerForward":
                    return new EulerIntegrator(stepSize);
                case "ode2":
                case "Heun":
                    return new MidpointIntegrator(stepSize);
                case "ode3":
                case "BogackiShampine23":
                    return new ThreeEighthesIntegrator(stepSize);
                case "ode4":
                case "RungeKutta4":
                    return new ClassicalRungeKuttaIntegrator(stepSize);
                case "FixedStepAuto":
                default:
                    return new ClassicalRungeKuttaIntegrator(stepSize);
            }
        } else {
            // Variable-step integrators
            switch (solver) {
                case "ode45":
                case "DormandPrince":
                    return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
                case "ode23":
                case "BogackiShampine23":
                    return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); // Fallback
                case "ode113":
                case "AdamsBashforthMoulton":
                    return new AdamsBashforthIntegrator(4, minStep, maxStep, absTol, relTol);
                case "VariableStepAuto":
                case "auto":
                default:
                    return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
            }
        }
    }
    
    /**
     * Create system ODE for integration
     * @return FirstOrderDifferentialEquations instance
     */
    private FirstOrderDifferentialEquations createSystemODE() {
        return new FirstOrderDifferentialEquations() {
            @Override
            public int getDimension() {
                return getStatesArray().length;
            }
            
            @Override
            public void computeDerivatives(double t, double[] x, double[] xDot) {
                calculateDerivatives(t, x, xDot);
            }
        };
    }
    
    /**
     * Check if data collection should be limited based on MaxDataPoints
     * @return true if data collection should continue
     */
    private boolean shouldCollectMoreData() {
        int maxPoints = getConfig().getMaxDataPoints();
        if (maxPoints <= 0) return true; // No limit
        
        // Check total data points collected across all scopes
        int totalDataPoints = 0;
        for (var terminal : getTerminalList()) {
            if (terminal instanceof com.ncslab.block.io.terminal.ScopeStruct) {
                com.ncslab.block.io.terminal.ScopeStruct scope = 
                    (com.ncslab.block.io.terminal.ScopeStruct) terminal;
                totalDataPoints += scope.getTimeList().size();
            }
        }
        
        return totalDataPoints < maxPoints;
    }
    
    /**
     * Send step result message to client with streaming scope data
     * @param session WebSocket session
     * @param currentTime Current simulation time
     * @param step Current step number
     * @param totalSteps Total steps being executed
     * @param direction Step direction
     */
    private void sendStepResult(Session session, double currentTime, int step, 
                              int totalSteps, String direction) {
        try {
            if (session != null) {
                // Capture current scope data for streaming (respect MaxDataPoints)
                JSONObject scopeData = captureOutputsForStreaming(currentTime);
                
                // Add configuration info to step result
                JSONObject configInfo = new JSONObject();
                configInfo.put("maxDataPoints", getConfig().getMaxDataPoints());
                configInfo.put("stepMode", getConfig().getStep());
                configInfo.put("solver", getConfig().getSolver());
                scopeData.put("config", configInfo);
                
                WebSocketMessageDto message = WebSocketMessageDto.createStepResultMessage(
                    currentTime, step, totalSteps, direction, scopeData.toMap());
                
                session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(message));
                
                // Also send real-time scope updates for debug visualization
                sendRealTimeScopeUpdate(session, scopeData, currentTime);
            }
        } catch (IOException e) {
            System.err.printf("Failed to send step result: %s%n", e.getMessage());
        }
    }
    
    /**
     * Capture outputs optimized for streaming (debug mode)
     * @param currentTime Current simulation time
     * @return JSON object with streaming-optimized outputs
     */
    private JSONObject captureOutputsForStreaming(double currentTime) {
        JSONObject streamingOutputs = new JSONObject();
        streamingOutputs.put("timestamp", System.currentTimeMillis());
        streamingOutputs.put("simulationTime", currentTime);
        streamingOutputs.put("streaming", true);
        
        JSONArray blocks = new JSONArray();
        
        for (Block block : getBlockList()) {
            JSONObject blockData = new JSONObject();
            blockData.put("blockUUID", block.getBlockUUID());
            blockData.put("blockName", block.getBlockName());
            blockData.put("blockType", block.getBlockType());
            
            JSONArray outputs = new JSONArray();
            block.getOutputPortList().forEach(outputPort -> {
                if (outputPort.getOutputSignalC() != null) {
                    JSONObject signalData = new JSONObject();
                    signalData.put("name", outputPort.getOutputSignalC().getName());
                    signalData.put("value", outputPort.getOutputSignalC().getData().getInitValue());
                    signalData.put("dataType", outputPort.getOutputSignalC().getDataType().toString());
                    signalData.put("portIndex", outputPort.getNumber());
                    outputs.put(signalData);
                }
            });
            
            JSONArray inputs = new JSONArray();
            block.getInputPortList().forEach(inputPort -> {
                if (inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null) {
                    JSONObject inputData = new JSONObject();
                    var outputPort = inputPort.getLinkedLine().getLinkedOutputPort();
                    if (outputPort.getOutputSignalC() != null) {
                        inputData.put("name", outputPort.getOutputSignalC().getName());
                        inputData.put("value", outputPort.getOutputSignalC().getData().getInitValue());
                        inputData.put("dataType", outputPort.getOutputSignalC().getDataType().toString());
                        inputData.put("portIndex", inputPort.getNumber());
                        inputs.put(inputData);
                    }
                }
            });
            
            blockData.put("outputs", outputs);
            blockData.put("inputs", inputs);
            
            if (outputs.length() > 0 || inputs.length() > 0) {
                blocks.put(blockData);
            }
        }
        
        streamingOutputs.put("blocks", blocks);
        return streamingOutputs;
    }
    
    /**
     * Send real-time scope update for debug visualization
     * @param session WebSocket session
     * @param scopeData Scope data
     * @param currentTime Current time
     */
    private void sendRealTimeScopeUpdate(Session session, JSONObject scopeData, double currentTime) {
        try {
            if (session != null) {
                WebSocketMessageDto message = WebSocketMessageDto.createRealTimeScopeUpdate(
                    null, scopeData.toMap(), currentTime);
                
                session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(message));
            }
        } catch (IOException e) {
            System.err.printf("Failed to send real-time scope update: %s%n", e.getMessage());
        }
    }
    
    /**
     * Send checkpoint saved message to client
     * @param session WebSocket session
     * @param checkpoint Saved checkpoint
     */
    private void sendCheckpointMessage(Session session, SimulationCheckpoint checkpoint) {
        try {
            if (session != null) {
                WebSocketMessageDto message = WebSocketMessageDto.createCheckpointSavedMessage(
                    checkpoint.getSimulationTime(), 
                    checkpoint.getCheckpointId(),
                    checkpoint.getMemoryUsageString());
                
                session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(message));
            }
        } catch (IOException e) {
            System.err.printf("Failed to send checkpoint message: %s%n", e.getMessage());
        }
    }
}