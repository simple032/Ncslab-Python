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
import com.ncslab.dto.block.specialized.sink.ToWorkspaceDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
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
import com.ncslab.util.UserContext;

// Internal imports - MFCalc client
import com.ncslab.code.m.MfcalcClientManager;
import com.ncslab.dto.communication.MfcalcResponseDto;

/**
 * To Workspace block with SIMULINK-compatible parameters.
 *
 * Logs signal data to a workspace variable during simulation for post-processing and analysis.
 * Data can be saved in multiple formats (Array, Structure, StructureWithTime).
 *
 * SIMULINK Parameters:
 * - VariableName: Name of workspace variable to save to (default: "simout")
 * - MaxDataPoints: Maximum number of data points to save (default: "inf")
 * - Decimation: Decimation factor for saving data (default: 1)
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - SaveFormat: Data save format ("Array", "Structure", "StructureWithTime")
 *
 * @author NCSLab Team
 * @version 2025
 */
public class ToWorkspace extends SinkBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter variableName;
    private final Parameter maxDataPoints;
    private final Parameter decimation;
    private final Parameter sampleTime;
    private final Parameter saveFormat;
    private final Parameter fixptAsFi;

    // === Data Storage ===
    private ScopeStruct scopeStruct; // Reuse existing data structure

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

    static {
        PARAMETER_DEFAULTS.put("VariableName", "simout");
        PARAMETER_DEFAULTS.put("MaxDataPoints", "inf");
        PARAMETER_DEFAULTS.put("Decimation", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("SaveFormat", "Array");
        PARAMETER_DEFAULTS.put("FixptAsFi", "off");

        inputNames.add("in1");
        // No outputs for sink block
    }

    /**
     * DTO-NATIVE Constructor - Creates ToWorkspace block directly from BlockDto DTO
     */
    public ToWorkspace(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.variableName = getParameterByName("VariableName");
        this.maxDataPoints = getParameterByName("MaxDataPoints");
        this.decimation = getParameterByName("Decimation");
        this.sampleTime = getParameterByName("SampleTime");
        this.saveFormat = getParameterByName("SaveFormat");
        this.fixptAsFi = getParameterByName("FixptAsFi");

        // Add input port
        inputPortList.add(new InputPort(this, 1));

        // Initialize scope struct for data storage
        this.scopeStruct = new ScopeStruct(this, 1, variableName.getInitString());

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create ToWorkspace from ToWorkspaceDto.
     *
     * @param dto The ToWorkspaceDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New ToWorkspace instance
     * @throws BlockCreationException if block creation fails
     */
    public static ToWorkspace createFromDto(ToWorkspaceDto dto, NCSLabModel model) throws BlockCreationException {
        return new ToWorkspace(dto, model);
    }

    /**
     * Legacy Constructor (Deprecated) - For backward compatibility
     */
    @Deprecated
    public ToWorkspace(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Get parameters from legacy JSON
        this.variableName = getParameterByName("VariableName");
        this.maxDataPoints = getParameterByName("MaxDataPoints");
        this.decimation = getParameterByName("Decimation");
        this.sampleTime = getParameterByName("SampleTime");
        this.saveFormat = getParameterByName("SaveFormat");
        this.fixptAsFi = getParameterByName("FixptAsFi");

        // Add input port
        inputPortList.add(new InputPort(this, 1));

        // Initialize scope struct for data storage
        String varName = variableName != null ? variableName.getInitString() : "simout";
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
        // ToWorkspace is a sink block - collect data during simulation
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
            
            scopeStruct.setDimension(signal.getWidth(), signal.getHeight());

            // Set max data points
            String maxPoints = maxDataPoints != null ? maxDataPoints.getInitString() : "inf";
            if (!maxPoints.equals("inf")) {
                try {
                    scopeStruct.setMaxDataLength(Integer.parseInt(maxPoints));
                } catch (NumberFormatException e) {
                    scopeStruct.setMaxDataLength(100000); // Default fallback
                }
            }

            model.addTerminal(scopeStruct);            
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // No dimension updates needed for sink block
    }

    @Override
    public void calculateTerminate(double t) {
        // Only save workspace data in simulation mode
        if (model.getModelMode() == ModelMode.Simulation) {
            saveWorkspaceDataToMfcalc();
        }
    }

    /**
     * Save accumulated data to MFCalc workspace (Simulation Mode Only)
     *
     * Process:
     * 1. Get userId from UserContext or model
     * 2. Convert scopeStruct data to structure format
     * 3. Call MfcalcClientManager.setVariableForUser(userId, variableName, data)
     * 4. Format: {time: [...], signals: [...]}
     */
    private void saveWorkspaceDataToMfcalc() {
        try {
            String varName = variableName != null ? variableName.getInitString() : "simout";
            System.out.println("[ToWorkspace] Saving data to workspace variable via MFCalc: " + varName);

            if (scopeStruct == null) {
                System.err.println("[ToWorkspace] No data to save - scopeStruct is null");
                return;
            }

            // Get collected data
            int dataPoints = scopeStruct.getTimeList().size();
            System.out.println("[ToWorkspace] Data points collected: " + dataPoints);

            if (dataPoints == 0) {
                System.err.println("[ToWorkspace] No data points collected during simulation");
                return;
            }

            // Get userId from UserContext or model
            String userId = UserContext.getUserId();
            if (userId == null) {
                userId = String.valueOf(model.getUserId());
            }
            System.out.println("[ToWorkspace] User ID: " + userId);

            // Convert data to workspace format
            Map<String, Object> workspaceData = convertToWorkspaceFormat();

            // Send variable to MFCalc workspace
            MfcalcResponseDto response = MfcalcClientManager.getInstance().setVariableForUser(userId, varName, workspaceData);

            if (response == null || response.isError()) {
                String error = response != null ? response.getError() : "Null response";
                System.err.println("[ToWorkspace] Error sending variable to MFCalc: " + error);
                return;
            }

            System.out.println("[ToWorkspace] Successfully saved " + dataPoints + " points to workspace variable '" + varName + "'");

        } catch (Exception e) {
            System.err.println("[ToWorkspace] Error saving workspace data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convert scopeStruct data to MFCalc workspace format.
     *
     * Supports two formats based on SaveFormat parameter:
     *
     * 1. Timeseries format (when SaveFormat = "Timeseries"):
     * {
     *   "type": "timeseries",
     *   "value": {
     *     "time": {"type": "matrix", "value": "[...]"},
     *     "data": {"type": "matrix", "value": "[...]"},
     *     "name": "variableName"
     *   }
     * }
     *
     * 2. Struct format (when SaveFormat = "Array", "Structure", or "StructureWithTime"):
     * {
     *   "type": "struct",
     *   "value": {
     *     "numFields": 2,
     *     "fields": {
     *       "time": {"type": "matrix", "value": "[...]"},
     *       "signals": {"type": "matrix", "value": "[...]"}
     *     }
     *   }
     * }
     *
     * Note: scopeStruct stores scalar values directly in dataList as List<Double>
     * For matrix data, values are flattened in row-major order
     */
    private Map<String, Object> convertToWorkspaceFormat() {
        // Get time and signal data from scopeStruct
        List<Double> timeList = scopeStruct.getTimeList();
        List<Double> dataList = scopeStruct.getDataList();

        int width = scopeStruct.getWidth();
        int height = scopeStruct.getHeight();

        // Get save format
        String format = saveFormat != null ? saveFormat.getInitString() : "Array";
        String varName = variableName != null ? variableName.getInitString() : "simout";

        // Check if Timeseries format is requested
        if ("Timeseries".equals(format)) {
            return convertToTimeseriesFormat(timeList, dataList, width, height, varName);
        } else {
            return convertToStructFormat(timeList, dataList, width, height);
        }
    }

    /**
     * Convert to MFCalc Timeseries format
     */
    private Map<String, Object> convertToTimeseriesFormat(List<Double> timeList, List<Double> dataList,
                                                           int width, int height, String name) {
        Map<String, Object> timeseriesValue = new HashMap<>();

        // Time field - always a column vector in MFCalc
        Map<String, Object> timeField = new HashMap<>();
        timeField.put("type", "matrix");
        timeField.put("value", formatMatrixString(timeList, timeList.size(), 1));
        timeseriesValue.put("time", timeField);

        // Data field
        Map<String, Object> dataField = new HashMap<>();
        if (width == 1 && height == 1) {
            // Scalar data - column vector [n x 1]
            dataField.put("type", "matrix");
            dataField.put("value", formatMatrixString(dataList, dataList.size(), 1));
        } else {
            // Matrix data - reconstruct as time series of matrices [time x (height*width)]
            int matrixSize = width * height;
            int numTimePoints = timeList.size();

            // Format as matrix with rows = numTimePoints, cols = matrixSize
            List<Double> flatData = new ArrayList<>(dataList);
            dataField.put("type", "matrix");
            dataField.put("value", formatMatrixString(flatData, numTimePoints, matrixSize));
        }
        timeseriesValue.put("data", dataField);

        Map<String, Object> workspaceData = new HashMap<>();
        workspaceData.put("type", "timeseries");
        workspaceData.put("value", timeseriesValue);

        return workspaceData;
    }

    /**
     * Convert to MFCalc Struct format
     */
    private Map<String, Object> convertToStructFormat(List<Double> timeList, List<Double> dataList,
                                                       int width, int height) {
        Map<String, Object> structValue = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();

        // Time field - always a column vector in MFCalc
        Map<String, Object> timeField = new HashMap<>();
        timeField.put("type", "matrix");
        timeField.put("value", formatMatrixString(timeList, timeList.size(), 1));
        fields.put("time", timeField);

        // Signals field
        Map<String, Object> signalsField = new HashMap<>();
        if (width == 1 && height == 1) {
            // Scalar data - column vector [n x 1]
            signalsField.put("type", "matrix");
            signalsField.put("value", formatMatrixString(dataList, dataList.size(), 1));
        } else {
            // Matrix data - reconstruct as time series of matrices [time x (height*width)]
            int matrixSize = width * height;
            int numTimePoints = timeList.size();

            // Format as matrix with rows = numTimePoints, cols = matrixSize
            List<Double> flatData = new ArrayList<>(dataList);
            signalsField.put("type", "matrix");
            signalsField.put("value", formatMatrixString(flatData, numTimePoints, matrixSize));
        }
        fields.put("signals", signalsField);

        structValue.put("numFields", fields.size());
        structValue.put("fields", fields);

        Map<String, Object> workspaceData = new HashMap<>();
        workspaceData.put("type", "struct");
        workspaceData.put("value", structValue);

        return workspaceData;
    }

    /**
     * Format data as MFCalc matrix string: "[row1;row2;...]"
     * @param data Flat list of values
     * @param rows Number of rows
     * @param cols Number of columns
     * @return Matrix string in MFCalc format
     */
    private String formatMatrixString(List<Double> data, int rows, int cols) {
        StringBuilder sb = new StringBuilder("[");

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int index = r * cols + c;
                if (index < data.size()) {
                    sb.append(data.get(index));
                } else {
                    sb.append("0");
                }

                if (c < cols - 1) {
                    sb.append(",");
                }
            }

            if (r < rows - 1) {
                sb.append(";");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    // === Code Generation Methods ===

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        if (model.getModelMode() == ModelMode.Simulation) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            // Add block-specific computed context
            context.put("scopeStruct", scopeStruct);

            String initCode = TemplateManager.renderTemplate("c/sink/ToWorkspace/init.vm", context);
            code.addInitCode(initCode);
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            // Add block-specific computed context
            context.put("scopeStruct", scopeStruct);

            if (!inputPortList.isEmpty() && inputPortList.get(0).getLinkedLine() != null) {
                OutputSignal inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                context.put("inputSignal", inputSignal.getName());
                context.put("inputSignalHeight", inputSignal.getHeight());
                context.put("inputSignalWidth", inputSignal.getWidth());
            }

            String outputCode = TemplateManager.renderTemplate("c/sink/ToWorkspace/output.vm", context);
            code.addSinkOutputCode(outputCode);
        }
    }

    @Override
    public void generateTerminateCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("block", this);
            context.put("variableName", variableName.getInitString());

            String codeStr = TemplateManager.renderTemplate("c/sink/ToWorkspace/terminate.vm", context);
            code.addTerminateCode(codeStr);
        }
    }

    @Override
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode = "";
        String varName = variableName.getInitString();
        code.addGlobalDefineCode("global " + varName + ";\n");
        initCode += varName + "=[];\n";
        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode = "";
        String varName = variableName.getInitString();
        outputCode += "if storeEnable>0\n";
        outputCode += varName + "=[" + varName
            + " Block" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId()
            + "_Output" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
            + "]"
            + ";\n";
        outputCode += "end\n";
        code.addOutputCode(outputCode);
    }
}
