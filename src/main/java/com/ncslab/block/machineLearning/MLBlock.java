package com.ncslab.block.machineLearning;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Base class for all machine learning blocks (CNN, A2C, LinearRegression, etc.).
 * Provides common implementations for ML model operations and agent communication.
 */
public abstract class MLBlock extends Block {

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // ML model state
    protected boolean modelLoaded = false;
    protected String modelPath = "";
    protected String modelType = "";
    protected CompletableFuture<Data> asyncInference;
    
    // Performance tracking
    protected long lastInferenceTime = 0;
    protected int inferenceCount = 0;
    protected double averageInferenceTime = 0.0;

    /**
     * DTO-NATIVE Constructor
     */
    protected MLBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    /**
     * Legacy JSON Constructor
     */
    protected MLBlock(JSONObject blockJson, NCSLabModel model) {
        super(blockJson, model);
    }

        public void calculateInit() {
        // Initialize ML model and agent connections
        if (loadMLModel()) {
            modelLoaded = true;
            initializeMLSpecificParameters();
        } else {
            System.err.println("Warning: ML model loading failed for block " + blockName);
            modelLoaded = false;
        }
        
        // Set initial output
        setInitialMLOutput();
    }

        public void calculateOutput(double t) {
        // Default implementation - ML blocks should override this method
        // with their specific machine learning inference logic
    }

        public void calculateDerivative(double t) {
        // ML blocks are typically discrete - no derivatives
        // Override if the ML block models continuous dynamics
    }

        public void calculateUpdate(double t) {
        // Default implementation - ML blocks can override if needed
    }

        public void calculateDiscreteUpdate(double t) {
        // Default implementation - ML blocks can override if needed
    }

        public void calculateTerminate(double t) {
        // Clean up ML resources and connections
        cleanupMLResources();
        modelLoaded = false;
    }

    /**
     * Load ML model (default implementation)
     * @return true if model loaded successfully, false otherwise
     */
    protected boolean loadMLModel() {
        // Default implementation - ML blocks should override this method
        return true;
    }

    /**
     * Perform ML inference (default implementation)
     * @param input Input data for inference
     * @param t Current simulation time
     * @return Inference result
     */
    protected Data performMLInference(Data input, double t) {
        // Default implementation - ML blocks should override this method
        return new Data(0.0);
    }

    /**
     * Clean up ML resources (default implementation)
     */
    protected void cleanupMLResources() {
        // Default implementation - ML blocks can override if needed
    }

    /**
     * Initialize ML-specific parameters (default implementation)
     */
    protected void initializeMLSpecificParameters() {
        // Default: no special parameter initialization needed
    }

    /**
     * Set initial ML output values (default implementation)
     */
    protected void setInitialMLOutput() {
        // Default: set output to zero
        if (!outputPortList.isEmpty()) {
            outputPortList.get(0).setData(new Data(0.0));
        }
    }

    /**
     * Provide safe output when model is not loaded (default implementation)
     * @param t Current simulation time
     */
    protected void provideSafeMLOutput(double t) {
        // Default: maintain zero output
        if (!outputPortList.isEmpty()) {
            outputPortList.get(0).setData(new Data(0.0));
        }
    }

    /**
     * Update ML state (default implementation)
     * @param t Current simulation time
     */
    protected void updateMLState(double t) {
        // Default: no state updates needed
    }

    /**
     * Perform discrete ML updates (default implementation)
     * @param t Current simulation time
     */
    protected void performDiscreteMLUpdate(double t) {
        // Default: no discrete updates needed
    }

    /**
     * Handle ML errors (default implementation)
     * @param e Exception that occurred
     * @param t Current simulation time
     */
    protected void handleMLError(Exception e, double t) {
        System.err.println("ML error in block " + blockName + " at time " + t + ": " + e.getMessage());
        // Provide safe fallback output
        provideSafeMLOutput(t);
    }

    /**
     * Perform asynchronous ML inference
     * @param input Input data
     * @param t Current simulation time
     * @return Future containing inference result
     */
    protected CompletableFuture<Data> performAsyncInference(Data input, double t) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return performMLInference(input, t);
            } catch (Exception e) {
                handleMLError(e, t);
                return new Data(0.0); // Safe fallback
            }
        });
    }

    /**
     * Get inference result with timeout
     * @param future Future containing inference result
     * @param timeoutMs Timeout in milliseconds
     * @return Inference result or safe default
     */
    protected Data getInferenceResult(CompletableFuture<Data> future, long timeoutMs) {
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("ML inference timeout or error in block " + blockName + ": " + e.getMessage());
            return new Data(0.0); // Safe fallback
        }
    }

    /**
     * Update performance metrics
     */
    private void updatePerformanceMetrics() {
        inferenceCount++;
        double currentInferenceMs = lastInferenceTime / 1e6;
        averageInferenceTime = ((averageInferenceTime * (inferenceCount - 1)) + currentInferenceMs) / inferenceCount;
    }

    /**
     * Check if ML model is loaded and operational
     * @return true if model is loaded
     */
    public boolean isModelLoaded() {
        return modelLoaded;
    }

    /**
     * Get last inference time in milliseconds
     * @return Last inference time
     */
    public double getLastInferenceTimeMs() {
        return lastInferenceTime / 1e6;
    }

    /**
     * Get average inference time in milliseconds
     * @return Average inference time
     */
    public double getAverageInferenceTimeMs() {
        return averageInferenceTime;
    }

    /**
     * Get total number of inferences performed
     * @return Inference count
     */
    public int getInferenceCount() {
        return inferenceCount;
    }

    /**
     * Set model path and type
     * @param modelPath Path to model file
     * @param modelType Type of model (pytorch, tensorflow, etc.)
     */
    public void setModelInfo(String modelPath, String modelType) {
        this.modelPath = modelPath;
        this.modelType = modelType;
    }
}