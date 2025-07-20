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
 * DiscreteStateSpace block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - A: System matrix A
 * - B: Input matrix B
 * - C: Output matrix C
 * - D: Feedthrough matrix D
 * - InitialCondition: Initial condition of state variables
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class DiscreteStateSpace extends DiscreteBlock {
    private String name = "Discrete State Space";

    private boolean feedThrough = false;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter A;
    private final Parameter B;
    private final Parameter C;
    private final Parameter D;
    private final Parameter initialCondition;
    private final Parameter sampleTimeParam;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    private Vector<State> xStateList = new Vector<>();

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("A", "[1]");
        PARAMETER_DEFAULTS.put("B", "[1]");
        PARAMETER_DEFAULTS.put("C", "[1]");
        PARAMETER_DEFAULTS.put("D", "[0]");
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
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
    private DiscreteStateSpace(Parameter A, Parameter B, Parameter C, Parameter D, Parameter initialCondition,
                              Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                              String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.A = Objects.requireNonNull(A, "A matrix parameter cannot be null");
        this.B = Objects.requireNonNull(B, "B matrix parameter cannot be null");
        this.C = Objects.requireNonNull(C, "C matrix parameter cannot be null");
        this.D = Objects.requireNonNull(D, "D matrix parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Determine feedthrough
        if (D.isZero()) {
            feedThrough = false;
        } else {
            feedThrough = true;
        }

        // Initialize state variables
        for (int i = 0; i < A.getWidth(); i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DiscreteStateSpace(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.A = new Parameter(this, 1, "A", paramValues.getString("A"));
        this.B = new Parameter(this, 2, "B", paramValues.getString("B"));
        this.C = new Parameter(this, 3, "C", paramValues.getString("C"));
        this.D = new Parameter(this, 4, "D", paramValues.getString("D"));
        this.initialCondition = new Parameter(this, 5, "InitialCondition", paramValues.getString("InitialCondition"));

        // Create missing SIMULINK parameters with defaults
        this.sampleTimeParam = new Parameter(this, 6, "SampleTime", "1.0");
        this.outDataType = new Parameter(this, 7, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 8, "SaturateOnIntegerOverflow", "off");

        // Add all parameters to parameter list

        // Determine feedthrough
        if (D.isZero()) {
            feedThrough = false;
        } else {
            feedThrough = true;
        }

        // Initialize state variables
        for (int i = 0; i < A.getWidth(); i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }
    }

    // === Static Factory Method for JSON Deserialization ===
    public static DiscreteStateSpace fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter A = createMatrixParameterFromJSON(paramValues, "A", "A", "1");
            Parameter B = createMatrixParameterFromJSON(paramValues, "B", "B", "1");
            Parameter C = createMatrixParameterFromJSON(paramValues, "C", "C", "1");
            Parameter D = createMatrixParameterFromJSON(paramValues, "D", "D", "0");
            Parameter initialCondition = createMatrixParameterFromJSON(paramValues, "InitialCondition", "InitialCondition", "0");
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            DiscreteStateSpace block = new DiscreteStateSpace(A, B, C, D, initialCondition,
                                                             sampleTime, outDataType, saturateParam,
                                                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, A, B, C, D, initialCondition,
                                     sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DiscreteStateSpace block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static DiscreteStateSpace create(String name, String path, String A, String B, String C, String D,
                                           String initialCondition, double sampleTime, NCSLabModel model) {
        return create(name, path, A, B, C, D, initialCondition, sampleTime, "Inherit: Same as input", false, model);
    }

    public static DiscreteStateSpace create(String name, String path, String A, String B, String C, String D,
                                           String initialCondition, double sampleTime, String outDataType,
                                           boolean saturateOnOverflow, NCSLabModel model) {
        Parameter AParam = new Parameter(null, 1, "A", A);
        Parameter BParam = new Parameter(null, 2, "B", B);
        Parameter CParam = new Parameter(null, 3, "C", C);
        Parameter DParam = new Parameter(null, 4, "D", D);
        Parameter initialConditionParam = new Parameter(null, 5, "InitialCondition", initialCondition);
        Parameter sampleTimeParam = new Parameter(null, 6, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 7, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 8, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        DiscreteStateSpace block = new DiscreteStateSpace(AParam, BParam, CParam, DParam, initialConditionParam,
                                                          sampleTimeParam, outDataTypeParam, saturateParam,
                                                          name, path, "null", model);

        setParameterBlockReference(block, AParam, BParam, CParam, DParam, initialConditionParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createMatrixParameterFromJSON(JSONObject paramValues, String jsonKey, String paramName, String defaultValue) {
        String value = paramValues.optString(jsonKey, defaultValue);
        return new Parameter(null, getParameterIndex(paramName), paramName, value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 6, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 7, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 8, "SaturateOnIntegerOverflow", saturateValue);
    }

    private static int getParameterIndex(String paramName) {
        switch (paramName) {
            case "A": return 1;
            case "B": return 2;
            case "C": return 3;
            case "D": return 4;
            case "InitialCondition": return 5;
            default: return 1;
        }
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

    private static void setParameterBlockReference(DiscreteStateSpace block, Parameter... parameters) {
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
        identity.put("blockType", "DiscreteStateSpace");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
}
