package com.ncslab.block.source;

// Java standard imports
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;

// Internal imports - Core
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * From File block with SIMULINK-compatible parameters.
 *
 * Reads signal data from a MAT file and outputs it during simulation.
 * Supports interpolation, extrapolation, and various data formats.
 *
 * SIMULINK Parameters:
 * - FileName: Name of MAT file to read from (default: "data.mat")
 * - SampleTime: Sample time for output (default: 0 for continuous)
 * - Interpolate: Whether to interpolate between data points (default: true)
 * - ExtrapolationBeforeFirstDataPoint: Behavior before first data point
 * - ExtrapolationAfterLastDataPoint: Behavior after last data point
 * - ZeroCross: Enable zero-crossing detection (default: true)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class FromFile extends SourceBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter fileName;
    private final Parameter sampleTime;
    private final Parameter interpolate;
    private final Parameter extrapolationBeforeFirstDataPoint;
    private final Parameter extrapolationAfterLastDataPoint;
    private final Parameter zeroCross;
    private final Parameter outDataTypeStr;

    // === File Data ===
    private List<Double> timeData;      // Time vector from file
    private List<Double> signalData;    // Signal data from file
    private int currentIndex = 0;       // Current position in data
    private boolean fileLoaded = false; // Whether file has been loaded

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

    static {
        PARAMETER_DEFAULTS.put("FileName", "data.mat");
        PARAMETER_DEFAULTS.put("SampleTime", "0");
        PARAMETER_DEFAULTS.put("Interpolate", "on");
        PARAMETER_DEFAULTS.put("ExtrapolationBeforeFirstDataPoint", "Hold first value");
        PARAMETER_DEFAULTS.put("ExtrapolationAfterLastDataPoint", "Hold last value");
        PARAMETER_DEFAULTS.put("ZeroCross", "on");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: auto");

        // No inputs for source block
        outputNames.add("out1");
    }

    /**
     * DTO-NATIVE Constructor - Creates FromFile block directly from BlockDto DTO
     */
    public FromFile(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.fileName = getParameterByName("FileName");
        this.sampleTime = getParameterByName("SampleTime");
        this.interpolate = getParameterByName("Interpolate");
        this.extrapolationBeforeFirstDataPoint = getParameterByName("ExtrapolationBeforeFirstDataPoint");
        this.extrapolationAfterLastDataPoint = getParameterByName("ExtrapolationAfterLastDataPoint");
        this.zeroCross = getParameterByName("ZeroCross");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");

        // Initialize output port
        outputPortList.add(new OutputPort(this, 1, false));

        // Initialize file data lists
        this.timeData = new ArrayList<>();
        this.signalData = new ArrayList<>();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Legacy Constructor (Deprecated) - For backward compatibility
     */
    @Deprecated
    public FromFile(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Get parameters from legacy JSON
        this.fileName = getParameterByName("FileName");
        this.sampleTime = getParameterByName("SampleTime");
        this.interpolate = getParameterByName("Interpolate");
        this.extrapolationBeforeFirstDataPoint = getParameterByName("ExtrapolationBeforeFirstDataPoint");
        this.extrapolationAfterLastDataPoint = getParameterByName("ExtrapolationAfterLastDataPoint");
        this.zeroCross = getParameterByName("ZeroCross");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");

        // Initialize output port
        outputPortList.add(new OutputPort(this, 1, false));

        // Initialize file data lists
        this.timeData = new ArrayList<>();
        this.signalData = new ArrayList<>();
    }

    @Override
    public void calculateInit() {
        // Only load file data in simulation mode
        if (model.getModelMode() == com.ncslab.ncslablink.ModelMode.Simulation) {
            if (!fileLoaded && fileName != null) {
                loadFileDataFromFilesystemAPI();
            }
        }

        // Initialize with first data point if available
        if (!signalData.isEmpty()) {
            Data outputData = new Data(signalData.get(0));
            outputPortList.get(0).getOutputSignalC().setData(outputData);
            currentIndex = 0;
        } else {
            // Default to zero if no data available
            Data outputData = new Data(0.0);
            outputPortList.get(0).getOutputSignalC().setData(outputData);
        }
    }

    @Override
    public void calculateOutput(double t) {
        if (timeData.isEmpty() || signalData.isEmpty()) {
            // No data available - output zero
            outputPortList.get(0).getOutputSignalC().setData(new Data(0.0));
            return;
        }

        double outputValue;

        // Find appropriate data point(s) for current time
        if (t < timeData.get(0)) {
            // Before first time point - apply extrapolation behavior
            outputValue = handleBeforeFirstValue(t);
        } else if (t > timeData.get(timeData.size() - 1)) {
            // After last time point - apply extrapolation behavior
            outputValue = handleAfterLastValue(t);
        } else {
            // Between time points - interpolate if enabled
            outputValue = getInterpolatedValue(t);
        }

        outputPortList.get(0).getOutputSignalC().setData(new Data(outputValue));
    }

    /**
     * Get interpolated value at time t
     */
    private double getInterpolatedValue(double t) {
        // Find time points surrounding t
        for (int i = 0; i < timeData.size() - 1; i++) {
            if (t >= timeData.get(i) && t <= timeData.get(i + 1)) {
                currentIndex = i;

                boolean doInterpolate = interpolate != null &&
                    (interpolate.getInitString().equals("on") || interpolate.getInitString().equals("true"));

                if (doInterpolate) {
                    // Linear interpolation
                    double t0 = timeData.get(i);
                    double t1 = timeData.get(i + 1);
                    double y0 = signalData.get(i);
                    double y1 = signalData.get(i + 1);

                    if (t1 > t0) {
                        double alpha = (t - t0) / (t1 - t0);
                        return y0 + alpha * (y1 - y0);
                    } else {
                        return y0;
                    }
                } else {
                    // Zero-order hold (no interpolation)
                    return signalData.get(i);
                }
            }
        }

        // Fallback to last value
        return signalData.get(signalData.size() - 1);
    }

    /**
     * Handle output before first value based on extrapolation parameter
     */
    private double handleBeforeFirstValue(double t) {
        String behavior = extrapolationBeforeFirstDataPoint != null ?
            extrapolationBeforeFirstDataPoint.getInitString() : "Hold first value";

        switch (behavior) {
            case "Set to zero":
                return 0.0;

            case "Extrapolation":
                // Linear extrapolation using first two points
                if (timeData.size() >= 2) {
                    double t0 = timeData.get(0);
                    double t1 = timeData.get(1);
                    double y0 = signalData.get(0);
                    double y1 = signalData.get(1);

                    if (t1 > t0) {
                        double slope = (y1 - y0) / (t1 - t0);
                        return y0 + slope * (t - t0);
                    }
                }
                return signalData.get(0);

            case "Hold first value":
            default:
                return signalData.get(0);
        }
    }

    /**
     * Handle output after last value based on extrapolation parameter
     */
    private double handleAfterLastValue(double t) {
        String behavior = extrapolationAfterLastDataPoint != null ?
            extrapolationAfterLastDataPoint.getInitString() : "Hold last value";

        switch (behavior) {
            case "Set to zero":
                return 0.0;

            case "Extrapolation":
                // Linear extrapolation using last two points
                if (timeData.size() >= 2) {
                    int n = timeData.size();
                    double t0 = timeData.get(n - 2);
                    double t1 = timeData.get(n - 1);
                    double y0 = signalData.get(n - 2);
                    double y1 = signalData.get(n - 1);

                    if (t1 > t0) {
                        double slope = (y1 - y0) / (t1 - t0);
                        return y1 + slope * (t - t1);
                    }
                }
                return signalData.get(signalData.size() - 1);

            case "Hold last value":
            default:
                return signalData.get(signalData.size() - 1);
        }
    }

    /**
     * Load data from MAT file via Filesystem API (Simulation Mode Only)
     *
     * Process:
     * 1. GET /api/filesystem/current-directory
     * 2. Construct full path: currentDir + fileName
     * 3. GET /api/filesystem/file/{fullPath}
     * 4. Parse MAT file content (expects JSON format with time and signal arrays)
     */
    private void loadFileDataFromFilesystemAPI() {
        try {
            String fileNameStr = fileName.getInitString();
            System.out.println("[FromFile] Loading file via API: " + fileNameStr);

            // Get userId from UserContext or model
            String userId = com.ncslab.util.UserContext.getUserId();
            if (userId == null) {
                userId = String.valueOf(model.getUserId());
            }
            System.out.println("[FromFile] User ID: " + userId);

            // Step 1: Get current directory
            String currentDir = com.ncslab.util.FileSystemApiClient.getCurrentDirectory(userId);
            System.out.println("[FromFile] Current directory: " + currentDir);

            // Step 2: Construct full path
            String fullPath = com.ncslab.util.FileSystemApiClient.constructFilePath(currentDir, fileNameStr);
            System.out.println("[FromFile] Full path: " + fullPath);

            // Step 3: Read file content
            String fileContent = com.ncslab.util.FileSystemApiClient.readFile(userId, fullPath);

            // Step 4: Parse file data
            parseFileData(fileContent);

            fileLoaded = true;
            System.out.println("[FromFile] Successfully loaded " + timeData.size() + " data points from " + fileNameStr);

        } catch (Exception e) {
            System.err.println("[FromFile] Error loading file: " + e.getMessage());
            e.printStackTrace();
            fileLoaded = false;

            // Fallback to placeholder data for testing
            System.out.println("[FromFile] Using placeholder sine wave data");
            timeData.clear();
            signalData.clear();
            for (int i = 0; i < 100; i++) {
                timeData.add(i * 0.1);
                signalData.add(Math.sin(i * 0.1));
            }
            fileLoaded = true;
        }
    }

    /**
     * Parse file data from JSON format.
     * Expected format: {"time": [0, 0.1, 0.2, ...], "signal": [0, 0.1, 0.2, ...]}
     * or MAT file format (to be implemented)
     */
    private void parseFileData(String fileContent) throws Exception {
        timeData.clear();
        signalData.clear();

        // Try to parse as JSON first
        try {
            org.json.JSONObject json = new org.json.JSONObject(fileContent);

            // Parse time array
            if (json.has("time")) {
                org.json.JSONArray timeArray = json.getJSONArray("time");
                for (int i = 0; i < timeArray.length(); i++) {
                    timeData.add(timeArray.getDouble(i));
                }
            }

            // Parse signal array
            if (json.has("signal")) {
                org.json.JSONArray signalArray = json.getJSONArray("signal");
                for (int i = 0; i < signalArray.length(); i++) {
                    signalData.add(signalArray.getDouble(i));
                }
            }

            // Validate data
            if (timeData.isEmpty() || signalData.isEmpty()) {
                throw new Exception("No time or signal data found in file");
            }

            if (timeData.size() != signalData.size()) {
                throw new Exception("Time and signal arrays have different lengths");
            }

        } catch (org.json.JSONException e) {
            // If JSON parsing fails, try MAT file format (to be implemented)
            throw new Exception("Failed to parse file data: " + e.getMessage());
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // FromFile outputs scalar by default
        outputPortList.get(0).setHeight(1);
        outputPortList.get(0).setWidth(1);
        outputPortList.get(0).getOutputSignalC().setHeight(1);
        outputPortList.get(0).getOutputSignalC().setWidth(1);
        outputPortList.get(0).getOutputSignalC().setDataType(DataType.REAL);
    }

    @Override
    public void checkDimension() throws MatDimException {
        // No dimension checks needed for source block
    }

    // === Code Generation Methods ===

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("fileName", fileName.getInitString());

        String initCode = TemplateManager.renderTemplate("c/source/FromFile/init.vm", context);
        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/source/FromFile/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String fileNameStr = fileName.getInitString();
        code.addInitCode("% Load data from file: " + fileNameStr + "\n");
        code.addInitCode("load('" + fileNameStr + "');\n");
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        context.put("block", this);
        context.put("fileName", fileName.getInitString());
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/source/FromFile/output.vm", context);
        code.addOutputCode(codeStr);
    }

    /**
     * Get the file name parameter value
     */
    public String getFileName() {
        return fileName != null ? fileName.getInitString() : "data.mat";
    }

    /**
     * Check if file has been successfully loaded
     */
    public boolean isFileLoaded() {
        return fileLoaded;
    }
}
