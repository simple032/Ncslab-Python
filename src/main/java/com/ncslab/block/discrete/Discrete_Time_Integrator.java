package com.ncslab.block.discrete;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * Discrete_Time_Integrator block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - Gain: Gain value for integration
 * - InitialCondition: Initial condition of the integrator
 * - IntegratorMethod: Integration method (Forward Euler, Backward Euler, Trapezoidal)
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Discrete_Time_Integrator extends DiscreteBlock {

    // === Internal State ===
    private State xState;

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter gain;
    @Getter
    private final Parameter initialCondition;
    @Getter
    private final Parameter integratorMethod;
    @Getter
    private final Parameter sampleTimeParam;
    @Getter
    private final Parameter outDataType;
    @Getter
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Gain", "1");
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
        PARAMETER_DEFAULTS.put("IntegratorMethod", "Forward Euler");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        parameterNames.add("Gain");
        parameterNames.add("InitialCondition");
        parameterNames.add("IntegratorMethod");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    // === Private Constructor with Typed Parameters ===
    private Discrete_Time_Integrator(Parameter gain, Parameter initialCondition, Parameter integratorMethod,
                                    Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                                    String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(sampleTime);

        // Assign parameters
        this.gain = Objects.requireNonNull(gain, "Gain parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.integratorMethod = Objects.requireNonNull(integratorMethod, "Integrator method parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Discrete_Time_Integrator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.gain = new Parameter(this, 1, "Gain", paramValues.getString("gainval"));
        this.sampleTimeParam = new Parameter(this, 2, "SampleTime", paramValues.getString("SampleTime"));
        this.initialCondition = new Parameter(this, 3, "InitialCondition", paramValues.getString("InitialCondition"));
        this.integratorMethod = new Parameter(this, 4, "IntegratorMethod", paramValues.optString("IntegratorMethod", "Integration: Forward Euler"));

        // Create missing SIMULINK parameters with defaults
        this.outDataType = new Parameter(this, 5, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 6, "SaturateOnIntegerOverflow", "off");

        // Add all parameters to parameter list

        // Initialize ports
        initializePorts();
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Discrete_Time_Integrator fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter gain = createGainFromJSON(paramValues, blockName);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter integratorMethod = createIntegratorMethodFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Discrete_Time_Integrator block = new Discrete_Time_Integrator(gain, initialCondition, integratorMethod,
                                                                          sampleTime, outDataType, saturateParam,
                                                                          blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, gain, initialCondition, integratorMethod,
                                     sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Discrete_Time_Integrator block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Discrete_Time_Integrator create(String name, String path, double gain, double initialCondition, double sampleTime, NCSLabModel model) {
        return create(name, path, gain, initialCondition, "Integration: Forward Euler", sampleTime, "Inherit: Same as input", false, model);
    }

    public static Discrete_Time_Integrator create(String name, String path, double gain, double initialCondition,
                                                  String integratorMethod, double sampleTime, String outDataType,
                                                  boolean saturateOnOverflow, NCSLabModel model) {
        Parameter gainParam = new Parameter(null, 1, "Gain", String.valueOf(gain));
        Parameter initialConditionParam = new Parameter(null, 2, "InitialCondition", String.valueOf(initialCondition));
        Parameter integratorMethodParam = new Parameter(null, 3, "IntegratorMethod", integratorMethod);
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        Discrete_Time_Integrator block = new Discrete_Time_Integrator(gainParam, initialConditionParam, integratorMethodParam,
                                                                      sampleTimeParam, outDataTypeParam, saturateParam,
                                                                      name, path, "null", model);

        setParameterBlockReference(block, gainParam, initialConditionParam, integratorMethodParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be positive and finite");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createGainFromJSON(JSONObject paramValues, String blockName) {
        String gainValue = paramValues.optString("gainval", "1.0");
        return new Parameter(null, 1, "Gain", gainValue);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 2, "InitialCondition", initialConditionValue);
    }

    private static Parameter createIntegratorMethodFromJSON(JSONObject paramValues, String blockName) {
        String integratorMethodValue = paramValues.optString("IntegratorMethod", "Integration: Forward Euler");
        return new Parameter(null, 3, "IntegratorMethod", integratorMethodValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 4, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 5, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateValue);
    }

    // === Utility Methods ===
    private static String requireNonEmptyString(JSONObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing");
        }
        String value = json.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Field '" + key + "' cannot be empty");
        }
        return value;
    }

    private static void setParameterBlockReference(Discrete_Time_Integrator block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null, but should work for basic operations
            }
        }
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Discrete_Time_Integrator");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Main input port
        input = new InputPort(this, 1);
        inputPortList.add(input);

        // Main output port (no feedthrough for integrator)
        output = new OutputPort(this, 1, false);
        outputPortList.add(output);
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        xState.setData(initialCondition.getData());
        out.setData(initialCondition.getData());
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        Data currentState = xState.getData();
        Data inputSignal = in.getData();

        // y(k) = xState(k)
        out.setData(currentState);
    }

    @Override
    public void calculateDiscreteUpdate(double t) {
        InputPort in = inputPortList.get(0);
        Data inputSignal = in.getData();
        Data updatedX;
        String option = integratorMethod.getInitString();

        switch (option) {
            case "Integration: Forward Euler":
            case "Integration: Backward Euler":
                updatedX = xState.getData().plus(gain.getData().times(inputSignal).times(new Data(sampleTime)));
                break;
            case "Integration: Trapezoidal":
                updatedX = xState.getData().plus(gain.getData().times(inputSignal).times(new Data(sampleTime / 2.0)));
                break;
            case "Accumulation: Forward Euler":
            case "Accumulation: Backward Euler":
                updatedX = xState.getData().plus(gain.getData().times(inputSignal));
                break;
            case "Accumulation: Trapezoidal":
                updatedX = xState.getData().plus(gain.getData().times(inputSignal).times(new Data(0.5)));
                break;
            default:
                throw new RuntimeException("Unsupported IntegratorMethod: " + option);
        }
        xState.setData(updatedX);
    }

    public void generateArraysCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal", signal);
        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Time_Integrator/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("blockId", blockId);
        context.put("blockName", blockName);
        context.put("gain", gain);
        context.put("sampleTime", sampleTimeParam);
        context.put("initialCondition", initialCondition);
        context.put("xState", xState);
        
        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Time_Integrator/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("block", this);
        context.put("out", out);
        context.put("ops", ops);
        context.put("signal", signal);
        context.put("xState", xState);
        context.put("gain", gain);
        context.put("sampleTime", sampleTimeParam);
        context.put("option", integratorMethod.getInitString());

        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Time_Integrator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);
        context.put("block", this);
        
        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Time_Integrator/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        context.put("block", this);
        
        String codeStr = TemplateManager.renderTemplate("c/discrete/Discrete_Time_Integrator/update.vm", context);
        code.addUpdateCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal.getDataType() == DataType.REAL) {
            xState = new State(this, 1, "save_data", gain.getHeight(), gain.getWidth());
        } else {
            xState = new State(this, 1, "save_data", signal.getHeight(), signal.getWidth());
        }
        stateList.add(xState);

        if ((sampleTimeParam.getDouble() * 1000000) % (model.getConfig().getFixedStep() * 1000000) > 0.000001) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be an integer multiple of the fixed-step size!\n \n");
            throw(e);
        }

        if (gain.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
            out.setHeight(gain.getHeight());
            out.setWidth(gain.getWidth());
            out.getOutputSignalC().setHeight(gain.getHeight());
            out.getOutputSignalC().setWidth(gain.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else if (gain.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            if (gain.getWidth() != signal.getWidth() || gain.getHeight() != signal.getHeight()) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
                throw(e);
            }
            out.setHeight(gain.getHeight());
            out.setWidth(gain.getWidth());
            out.getOutputSignalC().setHeight(gain.getHeight());
            out.getOutputSignalC().setWidth(gain.getWidth());
            out.getOutputSignalC().setDataType(gain.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        if (sampleTimeParam.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter(sampleTime) of Block " + this.blockName + " must be a real double scalar(period)!\n \n");
            throw(e);
        }
        if (gain.getWidth() != initialCondition.getWidth()
                || gain.getHeight() != initialCondition.getHeight()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match! Gain and initialCondition input dimensions should be same!");
            throw(e);
        }
    }
}

