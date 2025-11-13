package com.ncslab.block.source;

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
import com.ncslab.dto.block.specialized.source.FromWorkspaceDto;

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
import com.ncslab.util.UserContext;

// Internal imports - MFCalc client
import com.ncslab.code.m.MfcalcClientManager;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.dto.communication.MfcalcVariableDto;

/**
 * From Workspace block with SIMULINK-compatible parameters.
 *
 * Reads signal data from a workspace variable and outputs it during simulation.
 * Supports interpolation and various behaviors after final value.
 *
 * SIMULINK Parameters:
 * - VariableName: Name of workspace variable to read from (default: "simin")
 * - SampleTime: Sample time for output (default: 0 for continuous)
 * - Interpolate: Whether to interpolate between data points (default: true)
 * - ZeroCross: Enable zero-crossing detection (default: true)
 * - OutputAfterFinalValue: Behavior after final value
 *
 * @author NCSLab Team
 * @version 2025
 */
public class FromWorkspace extends SourceBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter variableName;
    private final Parameter sampleTime;
    private final Parameter interpolate;
    private final Parameter zeroCross;
    private final Parameter outputAfterFinalValue;
    private final Parameter outDataTypeStr;
    private final Parameter formOutput;

    // === Workspace Data ===
    private List<Double> timeData;      // Time vector from workspace
    private List<Double> signalData;    // Signal data from workspace
    private int currentIndex = 0;       // Current position in data

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

    static {
        PARAMETER_DEFAULTS.put("VariableName", "simin");
        PARAMETER_DEFAULTS.put("SampleTime", "0");
        PARAMETER_DEFAULTS.put("Interpolate", "on");
        PARAMETER_DEFAULTS.put("ZeroCross", "on");
        PARAMETER_DEFAULTS.put("OutputAfterFinalValue", "Holding final value");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: auto");
        PARAMETER_DEFAULTS.put("FormOutput", "Array");

        // No inputs for source block
        outputNames.add("out1");
    }

    /**
     * DTO-NATIVE Constructor - Creates FromWorkspace block directly from BlockDto DTO
     */
    public FromWorkspace(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.variableName = getParameterByName("VariableName");
        this.sampleTime = getParameterByName("SampleTime");
        this.interpolate = getParameterByName("Interpolate");
        this.zeroCross = getParameterByName("ZeroCross");
        this.outputAfterFinalValue = getParameterByName("OutputAfterFinalValue");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");
        this.formOutput = getParameterByName("FormOutput");

        // Initialize output port
        outputPortList.add(new OutputPort(this, 1, false));

        // Initialize workspace data lists
        this.timeData = new ArrayList<>();
        this.signalData = new ArrayList<>();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Legacy Constructor (Deprecated) - For backward compatibility
     */
    @Deprecated
    public FromWorkspace(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Get parameters from legacy JSON
        this.variableName = getParameterByName("VariableName");
        this.sampleTime = getParameterByName("SampleTime");
        this.interpolate = getParameterByName("Interpolate");
        this.zeroCross = getParameterByName("ZeroCross");
        this.outputAfterFinalValue = getParameterByName("OutputAfterFinalValue");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");
        this.formOutput = getParameterByName("FormOutput");

        // Initialize output port
        outputPortList.add(new OutputPort(this, 1, false));

        // Initialize workspace data lists
        this.timeData = new ArrayList<>();
        this.signalData = new ArrayList<>();
    }

    @Override
    public void calculateInit() {
        // Load workspace data from MFCalc in simulation mode
        if (model.getModelMode() == com.ncslab.ncslablink.ModelMode.Simulation) {
            loadWorkspaceDataFromMfcalc();
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
        if (t <= timeData.get(0)) {
            // Before first time point - use first value
            outputValue = signalData.get(0);
        } else if (t >= timeData.get(timeData.size() - 1)) {
            // After last time point - apply OutputAfterFinalValue behavior
            outputValue = handleAfterFinalValue(t);
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
     * Handle output after final value based on OutputAfterFinalValue parameter
     */
    private double handleAfterFinalValue(double t) {
        String behavior = outputAfterFinalValue != null ?
            outputAfterFinalValue.getInitString() : "Holding final value";

        switch (behavior) {
            case "Setting to zero":
                return 0.0;

            case "Cyclic repetition":
                // Repeat the entire signal cyclically
                double totalTime = timeData.get(timeData.size() - 1) - timeData.get(0);
                if (totalTime > 0) {
                    double relativeTime = (t - timeData.get(0)) % totalTime;
                    return getInterpolatedValue(timeData.get(0) + relativeTime);
                }
                return signalData.get(0);

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

            case "Holding final value":
            default:
                return signalData.get(signalData.size() - 1);
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // FromWorkspace outputs scalar by default
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("variableName", variableName.getInitString());

        String initCode = TemplateManager.renderTemplate("c/source/FromWorkspace/init.vm", context);
        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/source/FromWorkspace/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String varName = variableName.getInitString();
        code.addGlobalDefineCode("global " + varName + ";\n");
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        context.put("block", this);
        context.put("variableName", variableName.getInitString());
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/source/FromWorkspace/output.vm", context);
        code.addOutputCode(codeStr);
    }

    /**
     * Load workspace data from MFCalc workspace (Simulation Mode Only)
     *
     * Process:
     * 1. Get userId from UserContext or model
     * 2. Call MfcalcClientManager.getVariableForUser(userId, variableName)
     * 3. Parse the response and extract time/signal data
     * 4. Expected format: structure with "time" and "signals" arrays
     */
    private void loadWorkspaceDataFromMfcalc() {
        try {
            String varName = variableName != null ? variableName.getInitString() : "simin";
            System.out.println("[FromWorkspace] Loading workspace variable via MFCalc: " + varName);

            // Get userId from UserContext or model
            String userId = UserContext.getUserId();
            if (userId == null) {
                userId = String.valueOf(model.getUserId());
            }
            System.out.println("[FromWorkspace] User ID: " + userId);

            // Get variable from MFCalc workspace
            MfcalcResponseDto response = MfcalcClientManager.getInstance().getVariableForUser(userId, varName);

            if (response == null || response.isError()) {
                String error = response != null ? response.getError() : "Null response";
                System.err.println("[FromWorkspace] Error getting variable from MFCalc: " + error);
                // Fall back to placeholder data
                usePlaceholderData();
                return;
            }

            // Parse the variable data
            parseWorkspaceData(response.getData());

            System.out.println("[FromWorkspace] Successfully loaded " + timeData.size() + " data points from workspace");

        } catch (Exception e) {
            System.err.println("[FromWorkspace] Error loading workspace data: " + e.getMessage());
            e.printStackTrace();

            // Fallback to placeholder data for testing
            System.out.println("[FromWorkspace] Using placeholder sine wave data");
            usePlaceholderData();
        }
    }

    /**
     * Parse workspace variable data from MFCalc response.
     *
     * Supports multiple MFCalc formats:
     *
     * 1. Timeseries format:
     * {
     *   "type": "timeseries",
     *   "value": {
     *     "time": {"type": "matrix", "value": "[...]"},
     *     "data": {"type": "matrix", "value": "[...]"},
     *     "name": "varname"
     *   }
     * }
     *
     * 2. Struct format:
     * {
     *   "type": "struct",
     *   "value": {
     *     "fields": {
     *       "time": {"type": "matrix", "value": "[...]"},
     *       "signals": {"type": "matrix", "value": "[...]"}
     *     }
     *   }
     * }
     *
     * Also supports legacy formats: {time: [...], signals: [...]}
     */
    @SuppressWarnings("unchecked")
    private void parseWorkspaceData(Object data) throws Exception {
        timeData.clear();
        signalData.clear();

        if (data == null) {
            throw new Exception("Workspace variable data is null");
        }

        // Handle MFCalc formats
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;

            String type = (String) dataMap.get("type");

            if ("timeseries".equals(type)) {
                // MFCalc Timeseries format
                parseMfcalcTimeseries(dataMap);
            } else if ("struct".equals(type)) {
                // MFCalc Struct format
                parseMfcalcStruct(dataMap);
            } else {
                // Legacy simple map format: {time: [...], signals: [...]}
                parseLegacyMapFormat(dataMap);
            }
        }
        // Handle JSONObject format (legacy)
        else if (data instanceof org.json.JSONObject) {
            parseLegacyJsonFormat((org.json.JSONObject) data);
        }

        // Validate data
        if (timeData.isEmpty() || signalData.isEmpty()) {
            throw new Exception("No time or signal data found in workspace variable");
        }

        if (timeData.size() != signalData.size()) {
            throw new Exception("Time and signal arrays have different lengths: time=" +
                timeData.size() + ", signals=" + signalData.size());
        }
    }

    /**
     * Parse MFCalc timeseries format:
     * {"type": "timeseries", "value": {"time": {...}, "data": {...}, "name": "..."}}
     */
    @SuppressWarnings("unchecked")
    private void parseMfcalcTimeseries(Map<String, Object> timeseriesData) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) timeseriesData.get("value");
        if (value == null) {
            throw new Exception("Timeseries value is null");
        }

        // Parse time field
        @SuppressWarnings("unchecked")
        Map<String, Object> timeField = (Map<String, Object>) value.get("time");
        if (timeField != null) {
            parseMatrixField(timeField, timeData);
        }

        // Parse data field
        @SuppressWarnings("unchecked")
        Map<String, Object> dataField = (Map<String, Object>) value.get("data");
        if (dataField != null) {
            parseMatrixField(dataField, signalData);
        }

        // Optional: log the name if present
        String name = (String) value.get("name");
        if (name != null) {
            System.out.println("[FromWorkspace] Loaded timeseries: " + name);
        }
    }

    /**
     * Parse MFCalc struct format:
     * {"type": "struct", "value": {"fields": {"time": {...}, "signals": {...}}}}
     */
    @SuppressWarnings("unchecked")
    private void parseMfcalcStruct(Map<String, Object> structData) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) structData.get("value");
        if (value == null) {
            throw new Exception("Struct value is null");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) value.get("fields");
        if (fields == null) {
            throw new Exception("Struct fields is null");
        }

        // Parse time field
        @SuppressWarnings("unchecked")
        Map<String, Object> timeField = (Map<String, Object>) fields.get("time");
        if (timeField != null) {
            parseMatrixField(timeField, timeData);
        }

        // Parse signals field (try "signals" first, then "values")
        @SuppressWarnings("unchecked")
        Map<String, Object> signalsField = (Map<String, Object>) fields.get("signals");
        if (signalsField == null) {
            signalsField = (Map<String, Object>) fields.get("values");
        }
        if (signalsField != null) {
            parseMatrixField(signalsField, signalData);
        }
    }

    /**
     * Parse MFCalc matrix field: {"type": "matrix", "value": "[1,2;3,4]"}
     */
    @SuppressWarnings("unchecked")
    private void parseMatrixField(Map<String, Object> field, List<Double> targetList) throws Exception {
        String type = (String) field.get("type");
        Object value = field.get("value");

        if ("matrix".equals(type) && value instanceof String) {
            // Parse matrix string format: "[1,2;3,4]"
            String matrixStr = (String) value;
            parseMatrixString(matrixStr, targetList);
        } else if (value instanceof List) {
            // Handle list format
            @SuppressWarnings("unchecked")
            List<?> list = (List<?>) value;
            for (Object item : list) {
                targetList.add(convertToDouble(item));
            }
        }
    }

    /**
     * Parse MFCalc matrix string: "[1,2,3;4,5,6]" -> [1, 2, 3, 4, 5, 6]
     */
    private void parseMatrixString(String matrixStr, List<Double> targetList) {
        // Remove brackets
        String content = matrixStr.trim();
        if (content.startsWith("[")) {
            content = content.substring(1);
        }
        if (content.endsWith("]")) {
            content = content.substring(0, content.length() - 1);
        }

        // Split by semicolon (rows) and comma (columns)
        String[] rows = content.split(";");
        for (String row : rows) {
            String[] values = row.split(",");
            for (String val : values) {
                try {
                    targetList.add(Double.parseDouble(val.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            }
        }
    }

    /**
     * Parse legacy map format: {time: [...], signals: [...]}
     */
    @SuppressWarnings("unchecked")
    private void parseLegacyMapFormat(Map<String, Object> dataMap) {
        // Parse time array
        Object timeObj = dataMap.get("time");
        if (timeObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<?> timeList = (List<?>) timeObj;
            for (Object t : timeList) {
                timeData.add(convertToDouble(t));
            }
        }

        // Parse signal array (try "signals" first, then "values")
        Object signalObj = dataMap.get("signals");
        if (signalObj == null) {
            signalObj = dataMap.get("values");
        }
        if (signalObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<?> signalList = (List<?>) signalObj;
            for (Object s : signalList) {
                signalData.add(convertToDouble(s));
            }
        }
    }

    /**
     * Parse legacy JSONObject format
     */
    private void parseLegacyJsonFormat(org.json.JSONObject json) {
        // Parse time array
        if (json.has("time")) {
            org.json.JSONArray timeArray = json.getJSONArray("time");
            for (int i = 0; i < timeArray.length(); i++) {
                timeData.add(timeArray.getDouble(i));
            }
        }

        // Parse signal array
        String signalKey = json.has("signals") ? "signals" : "values";
        if (json.has(signalKey)) {
            org.json.JSONArray signalArray = json.getJSONArray(signalKey);
            for (int i = 0; i < signalArray.length(); i++) {
                signalData.add(signalArray.getDouble(i));
            }
        }
    }

    /**
     * Convert Object to Double (handles various numeric types from MFCalc)
     */
    private double convertToDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            return Double.parseDouble((String) obj);
        }
        return 0.0;
    }

    /**
     * Use placeholder sine wave data when workspace variable is unavailable
     */
    private void usePlaceholderData() {
        timeData.clear();
        signalData.clear();
        for (int i = 0; i < 100; i++) {
            timeData.add(i * 0.1);
            signalData.add(Math.sin(i * 0.1));
        }
    }

    /**
     * Load workspace data from external source
     * This method would be called during model initialization
     */
    public void loadWorkspaceData(List<Double> time, List<Double> signal) {
        this.timeData = new ArrayList<>(time);
        this.signalData = new ArrayList<>(signal);
        this.currentIndex = 0;
    }
}
