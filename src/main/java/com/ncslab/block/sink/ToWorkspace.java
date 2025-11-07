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

            if (scopeStruct != null) {
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
    }

    @Override
    public void updateDimension() throws MatDimException {
        // No dimension updates needed for sink block
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
