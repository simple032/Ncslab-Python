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
    public FromWorkspace(FromWorkspaceDto blockDto, NCSLabModel model) {
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
     * Load workspace data from external source
     * This method would be called during model initialization
     */
    public void loadWorkspaceData(List<Double> time, List<Double> signal) {
        this.timeData = new ArrayList<>(time);
        this.signalData = new ArrayList<>(signal);
        this.currentIndex = 0;
    }
}
