package com.ncslab.block.discrete;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.State;
import com.ncslab.util.TemplateManager;

/**
 * Discrete_Transfer_Fcn block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - Numerator: Numerator coefficients of the transfer function
 * - Denominator: Denominator coefficients of the transfer function
 * - InitialStates: Initial states of the transfer function
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Discrete_Transfer_Fcn extends DiscreteBlock {
    private boolean feedThrough = false;
    private Vector<State> xStateList = new Vector<>();

    // === SIMULINK-Compatible Parameters ===
    private final Parameter numerator;
    private final Parameter denominator;
    private final Parameter initialStates;
    private final Parameter sampleTimeParam;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Numerator", "[1]");
        PARAMETER_DEFAULTS.put("Denominator", "[1 -1]");  // z-1 in denominator
        PARAMETER_DEFAULTS.put("InitialStates", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    // === Private Constructor with Typed Parameters ===
    private Discrete_Transfer_Fcn(Parameter numerator, Parameter denominator, Parameter initialStates,
                                  Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                                  String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.numerator = Objects.requireNonNull(numerator, "Numerator parameter cannot be null");
        this.denominator = Objects.requireNonNull(denominator, "Denominator parameter cannot be null");
        this.initialStates = Objects.requireNonNull(initialStates, "Initial states parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Determine feedthrough
        if (denominator.getWidth() == numerator.getWidth()) {
            feedThrough = true;
        }

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedThrough));

        // Create state variables
        for (int i = 0; i < denominator.getWidth() - 1; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }

        setSampleTime(sampleTimeParam);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Discrete_Transfer_Fcn(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.numerator = new Parameter(this, 1, "Numerator", paramValues.getString("Numerator"));
        this.denominator = new Parameter(this, 2, "Denominator", paramValues.getString("Denominator"));
        this.initialStates = new Parameter(this, 3, "InitialStates", paramValues.getString("InitialStates"));
        this.sampleTimeParam = new Parameter(this, 4, "SampleTime", paramValues.getString("SampleTime"));

        // Create missing SIMULINK parameters with defaults
        this.outDataType = new Parameter(this, 5, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 6, "SaturateOnIntegerOverflow", "off");

        // Add all parameters to parameter list

        // Determine feedthrough
        if (denominator.getWidth() == numerator.getWidth()) {
            feedThrough = true;
        }

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedThrough));

        // Create state variables
        for (int i = 0; i < denominator.getWidth() - 1; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }

        setSampleTime(sampleTimeParam);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Discrete_Transfer_Fcn fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter numerator = createNumeratorFromJSON(paramValues, blockName);
            Parameter denominator = createDenominatorFromJSON(paramValues, blockName);
            Parameter initialStates = createInitialStatesFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Discrete_Transfer_Fcn block = new Discrete_Transfer_Fcn(numerator, denominator, initialStates,
                sampleTime, outDataType, saturateParam,
                blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, numerator, denominator, initialStates,
                sampleTime, outDataType, saturateParam);

            return block;
        } catch(Exception e){
            throw new BlockCreationException("Failed to create Discrete_Transfer_Fcn block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Discrete_Transfer_Fcn create(String name, String path, String numerator, String denominator,
                                               String initialStates, double sampleTime, NCSLabModel model) {
        return create(name, path, numerator, denominator, initialStates, sampleTime, "Inherit: Same as input", false, model);
    }
    public static Discrete_Transfer_Fcn create(String name, String path, String numerator, String denominator,
                                               String initialStates, double sampleTime, String outDataType,
                                               boolean saturateOnOverflow, NCSLabModel model) {
        Parameter numeratorParam = new Parameter(null, 1, "Numerator", numerator);
        Parameter denominatorParam = new Parameter(null, 2, "Denominator", denominator);
        Parameter initialStatesParam = new Parameter(null, 3, "InitialStates", initialStates);
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        Discrete_Transfer_Fcn block = new Discrete_Transfer_Fcn(numeratorParam, denominatorParam, initialStatesParam,
            sampleTimeParam, outDataTypeParam, saturateParam,
            name, path, "null", model);

        setParameterBlockReference(block, numeratorParam, denominatorParam, initialStatesParam,
            sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createNumeratorFromJSON(JSONObject paramValues, String blockName) {
        String numeratorValue = paramValues.optString("Numerator", "[1]");
        return new Parameter(null, 1, "Numerator", numeratorValue);
    }

    private static Parameter createDenominatorFromJSON(JSONObject paramValues, String blockName) {
        String denominatorValue = paramValues.optString("Denominator", "[1 1]");
        return new Parameter(null, 2, "Denominator", denominatorValue);
    }

    private static Parameter createInitialStatesFromJSON(JSONObject paramValues, String blockName) {
        String initialStatesValue = paramValues.optString("InitialStates", "0");
        return new Parameter(null, 3, "InitialStates", initialStatesValue);
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

    private static void setParameterBlockReference(Discrete_Transfer_Fcn block, Parameter... parameters) {
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
        identity.put("blockType", "Discrete_Transfer_Fcn");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
}
