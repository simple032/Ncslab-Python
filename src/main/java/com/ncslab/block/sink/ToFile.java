package com.ncslab.block.sink;

// Java standard imports
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
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.terminal.ScopeStruct;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * To File block with SIMULINK-compatible parameters.
 *
 * Writes signal data to a MAT file during simulation for post-processing and analysis.
 * Supports multiple save formats (Timeseries, Array) and data decimation.
 *
 * SIMULINK Parameters:
 * - FileName: Name of MAT file to write to (default: "output.mat")
 * - MatrixName: Variable name in MAT file (default: "data")
 * - SaveFormat: Data save format ("Timeseries" or "Array")
 * - Decimation: Decimation factor for saving data (default: 1)
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class ToFile extends SinkBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter fileName;
    private final Parameter matrixName;
    private final Parameter saveFormat;
    private final Parameter decimation;
    private final Parameter sampleTime;

    // === Data Storage ===
    private ScopeStruct scopeStruct; // Reuse existing data structure

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

    static {
        PARAMETER_DEFAULTS.put("FileName", "output.mat");
        PARAMETER_DEFAULTS.put("MatrixName", "data");
        PARAMETER_DEFAULTS.put("SaveFormat", "Timeseries");
        PARAMETER_DEFAULTS.put("Decimation", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");

        inputNames.add("in1");
        // No outputs for sink block
    }

    /**
     * DTO-NATIVE Constructor - Creates ToFile block directly from BlockDto DTO
     */
    public ToFile(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.fileName = getParameterByName("FileName");
        this.matrixName = getParameterByName("MatrixName");
        this.saveFormat = getParameterByName("SaveFormat");
        this.decimation = getParameterByName("Decimation");
        this.sampleTime = getParameterByName("SampleTime");

        // Add input port
        inputPortList.add(new InputPort(this, 1));

        // Initialize scope struct for data storage
        String varName = matrixName != null ? matrixName.getInitString() : "data";
        this.scopeStruct = new ScopeStruct(this, 1, varName);

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Legacy Constructor (Deprecated) - For backward compatibility
     */
    @Deprecated
    public ToFile(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Get parameters from legacy JSON
        this.fileName = getParameterByName("FileName");
        this.matrixName = getParameterByName("MatrixName");
        this.saveFormat = getParameterByName("SaveFormat");
        this.decimation = getParameterByName("Decimation");
        this.sampleTime = getParameterByName("SampleTime");

        // Add input port
        inputPortList.add(new InputPort(this, 1));

        // Initialize scope struct for data storage
        String varName = matrixName != null ? matrixName.getInitString() : "data";
        this.scopeStruct = new ScopeStruct(this, 1, varName);
    }

    @Override
    public void calculateInit() {
        // Initialize scope data structure
        if (model.getModelMode() == ModelMode.Simulation) {
            if (scopeStruct != null && !inputPortList.isEmpty() && inputPortList.get(0).getData() != null) {
                scopeStruct.addTimeSeries(0.0, inputPortList.get(0).getData());
            }
        }
    }

    @Override
    public void calculateOutput(double t) {
        // ToFile is a sink block - collect data during simulation
        if (model.getModelMode() == ModelMode.Simulation) {
            if (scopeStruct != null && !inputPortList.isEmpty()) {
                Data inputData = inputPortList.get(0).getData();
                if (inputData != null) {
                    // Check decimation
                    int decValue = decimation != null ? Integer.parseInt(decimation.getInitString()) : 1;
                    int dataPoints = scopeStruct.getTimeList().size();

                    if (dataPoints % decValue == 0) {
                        // Check if we should sample based on sample time
                        double sampleTimeValue = sampleTime != null ? sampleTime.getDouble() : -1.0;
                        boolean shouldSample = false;

                        if (sampleTimeValue <= 0) {
                            // Continuous or inherited sampling
                            shouldSample = true;
                        } else {
                            // Discrete sampling
                            double lastSampleTime = scopeStruct.getTimeList().isEmpty() ?
                                -1.0 : scopeStruct.getTimeList().get(scopeStruct.getTimeList().size() - 1);
                            shouldSample = (t - lastSampleTime) >= sampleTimeValue * 0.99;
                        }

                        if (shouldSample && (scopeStruct.getTimeList().isEmpty() ||
                            t > scopeStruct.getTimeList().get(scopeStruct.getTimeList().size() - 1))) {
                            scopeStruct.addTimeSeries(t, inputData);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && inputPortList.get(0).getLinkedLine() != null) {
            OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

            if (scopeStruct != null) {
                scopeStruct.setDimension(signal.getWidth(), signal.getHeight());
                model.addTerminal(scopeStruct);
            }
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // No dimension updates needed for sink block
    }

    @Override
    public void calculateTerminate(double t) {
        // Only save file in simulation mode
        if (model.getModelMode() == ModelMode.Simulation) {
            saveFileDataToFilesystemAPI();
        }
    }

    /**
     * Save accumulated data to MAT file via Filesystem API (Simulation Mode Only)
     *
     * Process:
     * 1. GET /api/filesystem/current-directory
     * 2. Construct full path: currentDir + fileName
     * 3. Convert scopeStruct data to JSON format
     * 4. PUT /api/filesystem/file/{fullPath} with file content
     */
    private void saveFileDataToFilesystemAPI() {
        try {
            String fileNameStr = fileName.getInitString();
            String varName = matrixName.getInitString();
            String format = saveFormat.getInitString();

            System.out.println("[ToFile] Saving data to file via API: " + fileNameStr);
            System.out.println("[ToFile] Variable name: " + varName);
            System.out.println("[ToFile] Save format: " + format);

            if (scopeStruct == null) {
                System.err.println("[ToFile] No data to save - scopeStruct is null");
                return;
            }

            // Get collected data
            int dataPoints = scopeStruct.getTimeList().size();
            System.out.println("[ToFile] Data points collected: " + dataPoints);

            if (dataPoints == 0) {
                System.err.println("[ToFile] No data points collected during simulation");
                return;
            }

            // Get userId from UserContext or model
            String userId = com.ncslab.util.UserContext.getUserId();
            if (userId == null) {
                userId = String.valueOf(model.getUserId());
            }
            System.out.println("[ToFile] User ID: " + userId);

            // Step 1: Get current directory
            String currentDir = com.ncslab.util.FileSystemApiClient.getCurrentDirectory(userId);
            System.out.println("[ToFile] Current directory: " + currentDir);

            // Step 2: Construct full path
            String fullPath = com.ncslab.util.FileSystemApiClient.constructFilePath(currentDir, fileNameStr);
            System.out.println("[ToFile] Full path: " + fullPath);

            // Step 3: Convert data to JSON format
            String fileContent = convertToJsonFormat(varName, format);

            // Step 4: Write file via API
            org.json.JSONObject response = com.ncslab.util.FileSystemApiClient.writeFile(userId, fullPath, fileContent);

            System.out.println("[ToFile] Write response: " + response.toString());

            // Step 5: Verify file was written successfully by reading it back
            boolean verified = verifyFileWritten(userId, fullPath, fileContent, dataPoints);

            if (verified) {
                System.out.println("[ToFile] ✓ Successfully saved and verified " + dataPoints + " points to " + fileNameStr);
            } else {
                System.err.println("[ToFile] ✗ Warning: File saved but verification failed for " + fileNameStr);
            }

        } catch (Exception e) {
            System.err.println("[ToFile] Error saving file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verify that the file was written successfully by reading it back
     *
     * @param userId User ID for the request
     * @param fullPath Full path to the file
     * @param originalContent Original content that was written
     * @param expectedDataPoints Expected number of data points
     * @return true if verification successful, false otherwise
     */
    private boolean verifyFileWritten(String userId, String fullPath, String originalContent, int expectedDataPoints) {
        try {
            System.out.println("[ToFile] Verifying file write...");

            // Step 1: Read the file back
            String readContent = com.ncslab.util.FileSystemApiClient.readFile(userId, fullPath);

            if (readContent == null || readContent.isEmpty()) {
                System.err.println("[ToFile] Verification failed: File content is empty");
                return false;
            }

            // Step 2: Parse and validate the JSON structure
            org.json.JSONObject readJson = new org.json.JSONObject(readContent);

            // Step 3: Verify data points count
            org.json.JSONObject readData = readJson.getJSONObject("data");

            int readDataPoints = 0;
            if (readData.has("time")) {
                org.json.JSONArray timeArray = readData.getJSONArray("time");
                readDataPoints = timeArray.length();
            }

            if (readDataPoints != expectedDataPoints) {
                System.err.println("[ToFile] Verification failed: Data points mismatch. Expected: "
                    + expectedDataPoints + ", Read: " + readDataPoints);
                return false;
            }

            // Step 4: Verify JSON structure matches
            org.json.JSONObject originalJson = new org.json.JSONObject(originalContent);

            // Check if both have the same keys at the top level
            if (!originalJson.keySet().equals(readJson.keySet())) {
                System.err.println("[ToFile] Verification failed: JSON structure mismatch");
                return false;
            }

            System.out.println("[ToFile] Verification successful: " + readDataPoints + " data points confirmed");
            return true;

        } catch (org.json.JSONException e) {
            System.err.println("[ToFile] Verification failed: JSON parsing error - " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[ToFile] Verification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convert scopeStruct data to JSON format.
     *
     * For Timeseries format: {"variableName": {"time": [...], "signal": [...]}}
     * For Array format: {"time": [...], "signal": [...]}
     *
     * Note: scopeStruct stores scalar values directly in dataList as List<Double>
     * For matrix data, values are flattened in row-major order
     */
    private String convertToJsonFormat(String varName, String format) {
        org.json.JSONObject fileData = new org.json.JSONObject();

        // Get time and signal data from scopeStruct
        List<Double> timeList = scopeStruct.getTimeList();
        List<Double> dataList = scopeStruct.getDataList();

        // Convert to JSON arrays
        org.json.JSONArray timeArray = new org.json.JSONArray();
        org.json.JSONArray signalArray = new org.json.JSONArray();

        int width = scopeStruct.getWidth();
        int height = scopeStruct.getHeight();

        // Add time data
        for (Double time : timeList) {
            timeArray.put(time);
        }

        // Handle scalar or matrix data
        if (width == 1 && height == 1) {
            // Scalar data - simple array
            for (Double value : dataList) {
                signalArray.put(value);
            }
        } else {
            // Matrix data - stored flattened, need to reconstruct
            int matrixSize = width * height;
            int numTimePoints = timeList.size();

            for (int t = 0; t < numTimePoints; t++) {
                org.json.JSONArray matrixArray = new org.json.JSONArray();
                for (int h = 0; h < height; h++) {
                    org.json.JSONArray rowArray = new org.json.JSONArray();
                    for (int w = 0; w < width; w++) {
                        int index = t * matrixSize + h * width + w;
                        rowArray.put(dataList.get(index));
                    }
                    matrixArray.put(rowArray);
                }
                signalArray.put(matrixArray);
            }
        }

        // Format data according to save format
        if ("Timeseries".equals(format)) {
            // Timeseries format: nested structure with variable name
            org.json.JSONObject timeseriesData = new org.json.JSONObject();
            timeseriesData.put("time", timeArray);
            timeseriesData.put("signal", signalArray);
            fileData.put(varName, timeseriesData);
        } else {
            // Array format: simple time and signal arrays
            fileData.put("time", timeArray);
            fileData.put("signal", signalArray);
        }

        return fileData.toString();
    }

    // === Code Generation Methods ===

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        if (model.getModelMode() == ModelMode.Simulation) {
            TemplateUtils.populateAllContext(context, this);

            // Add block-specific context
            context.put("scopeStruct", scopeStruct);
            context.put("fileName", fileName.getInitString());
            context.put("matrixName", matrixName.getInitString());
            context.put("saveFormat", saveFormat.getInitString());

            String initCode = TemplateManager.renderTemplate("c/sink/ToFile/init.vm", context);
            code.addInitCode(initCode);
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            TemplateUtils.populateAllContext(context, this);

            // Add block-specific context
            context.put("scopeStruct", scopeStruct);

            if (!inputPortList.isEmpty() && inputPortList.get(0).getLinkedLine() != null) {
                OutputSignal inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                context.put("inputSignal", inputSignal.getName());
                context.put("inputSignalHeight", inputSignal.getHeight());
                context.put("inputSignalWidth", inputSignal.getWidth());
            }

            String outputCode = TemplateManager.renderTemplate("c/sink/ToFile/output.vm", context);
            code.addSinkOutputCode(outputCode);
        }
    }

    @Override
    public void generateTerminateCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            TemplateUtils.populateAllContext(context, this);

            context.put("fileName", fileName.getInitString());
            context.put("matrixName", matrixName.getInitString());
            context.put("saveFormat", saveFormat.getInitString());
            context.put("scopeStruct", scopeStruct);

            String codeStr = TemplateManager.renderTemplate("c/sink/ToFile/terminate.vm", context);
            code.addTerminateCode(codeStr);
        }
    }

    @Override
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String varName = matrixName.getInitString();
        code.addGlobalDefineCode("global " + varName + ";\n");
        code.addInitCode(varName + " = [];\n");
        code.addInitCode(varName + "_time = [];\n");
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String varName = matrixName.getInitString();
        String fileNameStr = fileName.getInitString();

        String outputCode = "";
        outputCode += "if storeEnable>0\n";
        outputCode += "    " + varName + " = [" + varName
            + " Block" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId()
            + "_Output" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
            + "];\n";
        outputCode += "    " + varName + "_time = [" + varName + "_time t];\n";
        outputCode += "    % Note: File will be saved at end of simulation\n";
        outputCode += "    % save('" + fileNameStr + "', '" + varName + "', '" + varName + "_time');\n";
        outputCode += "end\n";
        code.addOutputCode(outputCode);
    }

    /**
     * Get the file name parameter value
     */
    public String getFileName() {
        return fileName != null ? fileName.getInitString() : "output.mat";
    }

    /**
     * Get the matrix/variable name for the data
     */
    public String getMatrixName() {
        return matrixName != null ? matrixName.getInitString() : "data";
    }

    /**
     * Get the save format
     */
    public String getSaveFormat() {
        return saveFormat != null ? saveFormat.getInitString() : "Timeseries";
    }
}
