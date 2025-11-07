package com.ncslab.block.discrete;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.DiscreteStateSpaceDto;

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
import java.util.ArrayList;
import java.util.List;

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

    private List<State> xStateList = new ArrayList<>();

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

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

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

        // Add parameters to parameterList for template context population
        parameterList.add(this.A);
        parameterList.add(this.B);
        parameterList.add(this.C);
        parameterList.add(this.D);
        parameterList.add(this.initialCondition);
        parameterList.add(this.sampleTimeParam);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);


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
        this.A = getParameterByName("A");
        this.B = getParameterByName("B");
        this.C = getParameterByName("C");
        this.D = getParameterByName("D");
        this.initialCondition = getParameterByName("InitialCondition");

        // Create missing SIMULINK parameters with defaults
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
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
    }    /**
     * DTO-NATIVE Constructor - Creates DiscreteStateSpace block directly from BlockDto DTO
     */
    public DiscreteStateSpace(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.A = getParameterByName("A");
        this.B = getParameterByName("B");
        this.C = getParameterByName("C");
        this.D = getParameterByName("D");
        this.initialCondition = getParameterByName("Initialcondition");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
        
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
        
        setSampleTime(sampleTimeParam);

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
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

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static DiscreteStateSpace create(String name, String path, String A, String B, String C, String D,
                                           String initialCondition, double sampleTime, NCSLabModel model) {
        return create(name, path, A, B, C, D, initialCondition, sampleTime, "Inherit: Same as input", false, model);
    }

    /**
     * Create a DiscreteStateSpace block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param A System matrix A
     * @param B Input matrix B
     * @param C Output matrix C
     * @param D Feedthrough matrix D
     * @param initialCondition Initial condition of state variables
     * @param sampleTime Sample time for discrete operation
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return DiscreteStateSpace block instance
     */
    public static DiscreteStateSpace create(String name, String path, String A, String B, String C, String D,
                                           String initialCondition, double sampleTime, String outDataType,
                                           boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DiscreteStateSpaceDto dto = DiscreteStateSpaceDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .A(com.ncslab.dto.common.TypedParameter.of(A))
            .B(com.ncslab.dto.common.TypedParameter.of(B))
            .C(com.ncslab.dto.common.TypedParameter.of(C))
            .D(com.ncslab.dto.common.TypedParameter.of(D))
            .initialCondition(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid DiscreteStateSpace parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new DiscreteStateSpace(dto, model);
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

    @Override
    public void calculateOutput(double t) {
        // Discrete state space: y[k] = C*x[k] + D*u[k]
        OutputPort output = outputPortList.get(0);
        InputPort input = inputPortList.get(0);
        
        if (input.getData() == null) {
            output.setData(new Data(0.0));
            return;
        }
        
        Data inputData = input.getData();
        Matrix CMatrix = C.getMatrix();
        Matrix DMatrix = D.getMatrix();
        
        if (inputData.getDataType() == DataType.REAL && !xStateList.isEmpty()) {
            // Scalar case
            double inputValue = inputData.getInitValue();
            
            // Calculate C*x[k] 
            double outputFromStates = 0.0;
            for (int i = 0; i < xStateList.size(); i++) {
                State state = xStateList.get(i);
                double stateValue = state.getData().getInitValue();
                if (i < CMatrix.getColumnDimension()) {
                    outputFromStates += CMatrix.get(0, i) * stateValue;
                }
            }
            
            // Calculate D*u[k]
            double outputFromInput = DMatrix.get(0, 0) * inputValue;
            
            // Total output: y[k] = C*x[k] + D*u[k]
            double totalOutput = outputFromStates + outputFromInput;
            output.setData(new Data(totalOutput));
            
        } else if (inputData.getDataType() == DataType.MATRIX && !xStateList.isEmpty()) {
            // Matrix case
            Matrix inputMatrix = inputData.getMatrix();
            
            // Get current state vector
            int numStates = xStateList.size();
            Matrix stateVector = new Matrix(numStates, 1);
            for (int i = 0; i < numStates; i++) {
                State state = xStateList.get(i);
                if (state.getData().getDataType() == DataType.REAL) {
                    stateVector.set(i, 0, state.getData().getInitValue());
                } else if (state.getData().getDataType() == DataType.MATRIX) {
                    // For matrix states, take the first element
                    Matrix stateMatrix = state.getData().getMatrix();
                    if (stateMatrix.getRowDimension() > 0 && stateMatrix.getColumnDimension() > 0) {
                        stateVector.set(i, 0, stateMatrix.get(0, 0));
                    }
                }
            }
            
            // Calculate C*x[k]
            Matrix outputFromStates = CMatrix.times(stateVector);
            
            // Calculate D*u[k] - handle different input dimensions
            Matrix outputFromInput;
            if (inputMatrix.getRowDimension() == DMatrix.getColumnDimension()) {
                outputFromInput = DMatrix.times(inputMatrix);
            } else {
                // Create compatible input vector
                Matrix inputVector = new Matrix(DMatrix.getColumnDimension(), 1);
                for (int i = 0; i < Math.min(DMatrix.getColumnDimension(), inputMatrix.getRowDimension()); i++) {
                    inputVector.set(i, 0, inputMatrix.get(i, 0));
                }
                outputFromInput = DMatrix.times(inputVector);
            }
            
            // Total output: y[k] = C*x[k] + D*u[k]
            Matrix totalOutput = outputFromStates.plus(outputFromInput);
            output.setData(new Data(totalOutput));
            
        } else {
            // No states or invalid input - use feedthrough only
            if (inputData.getDataType() == DataType.REAL) {
                double inputValue = inputData.getInitValue();
                double outputValue = DMatrix.get(0, 0) * inputValue;
                output.setData(new Data(outputValue));
            } else if (inputData.getDataType() == DataType.MATRIX) {
                Matrix inputMatrix = inputData.getMatrix();
                Matrix outputMatrix = DMatrix.times(inputMatrix);
                output.setData(new Data(outputMatrix));
            } else {
                output.setData(new Data(0.0));
            }
        }
    }

    @Override
    public void calculateInit() {
        // Initialize discrete state space block
        OutputPort output = outputPortList.get(0);
        
        // Initialize states to initial condition
        double ic = initialCondition.getDouble();
        for (State state : xStateList) {
            if (state.getDataType() == DataType.REAL) {
                state.setData(new Data(ic));
            } else if (state.getDataType() == DataType.MATRIX) {
                int height = state.getHeight();
                int width = state.getWidth();
                Matrix icMatrix = new Matrix(height, width);
                
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        icMatrix.set(i, j, ic);
                    }
                }
                state.setData(new Data(icMatrix));
            }
        }
        
        // Initialize output to zero
        Matrix CMatrix = C.getMatrix();
        if (CMatrix.getRowDimension() == 1 && CMatrix.getColumnDimension() == 1) {
            output.setData(new Data(0.0));
        } else {
            Matrix zeroOutput = new Matrix(CMatrix.getRowDimension(), 1);
            output.setData(new Data(zeroOutput));
        }
    }
    
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add computed state list for template
        context.put("stateList", xStateList);

        // Add stateName variable for template
        if (!xStateList.isEmpty()) {
            context.put("stateName", xStateList.get(0).getName());
        }

        String codeStr = TemplateManager.renderTemplate("c/discrete/DiscreteStateSpace/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void calculateUpdate(double t) {
        // Update discrete state: x[k+1] = A*x[k] + B*u[k]
        // This method is called during discrete time steps
        InputPort input = inputPortList.get(0);
        
        if (input.getData() == null || xStateList.isEmpty()) {
            return;
        }
        
        Data inputData = input.getData();
        Matrix AMatrix = A.getMatrix();
        Matrix BMatrix = B.getMatrix();
        
        if (inputData.getDataType() == DataType.REAL) {
            // Scalar case
            double inputValue = inputData.getInitValue();
            
            // Create new state values
            List<Double> newStateValues = new ArrayList<>();
            
            for (int i = 0; i < xStateList.size(); i++) {
                State state = xStateList.get(i);
                double oldStateValue = state.getData().getInitValue();
                
                // Calculate A*x[k] component for this state
                double stateComponent = 0.0;
                for (int j = 0; j < xStateList.size(); j++) {
                    if (j < AMatrix.getColumnDimension()) {
                        double otherStateValue = xStateList.get(j).getData().getInitValue();
                        stateComponent += AMatrix.get(i, j) * otherStateValue;
                    }
                }
                
                // Calculate B*u[k] component
                double inputComponent = 0.0;
                if (i < BMatrix.getRowDimension()) {
                    inputComponent = BMatrix.get(i, 0) * inputValue;
                }
                
                // New state value: x[k+1] = A*x[k] + B*u[k]
                double newStateValue = stateComponent + inputComponent;
                newStateValues.add(newStateValue);
            }
            
            // Update all states with new values
            for (int i = 0; i < xStateList.size(); i++) {
                xStateList.get(i).setData(new Data(newStateValues.get(i)));
            }
            
        } else if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix case - similar implementation for matrices
            Matrix inputMatrix = inputData.getMatrix();
            
            // Get current state vector
            int numStates = xStateList.size();
            Matrix currentStateVector = new Matrix(numStates, 1);
            for (int i = 0; i < numStates; i++) {
                State state = xStateList.get(i);
                if (state.getData().getDataType() == DataType.REAL) {
                    currentStateVector.set(i, 0, state.getData().getInitValue());
                }
            }
            
            // Calculate A*x[k]
            Matrix stateUpdate = AMatrix.times(currentStateVector);
            
            // Calculate B*u[k]
            Matrix inputVector = new Matrix(BMatrix.getColumnDimension(), 1);
            for (int i = 0; i < Math.min(BMatrix.getColumnDimension(), inputMatrix.getRowDimension()); i++) {
                inputVector.set(i, 0, inputMatrix.get(i, 0));
            }
            Matrix inputUpdate = BMatrix.times(inputVector);
            
            // New state: x[k+1] = A*x[k] + B*u[k]
            Matrix newStateVector = stateUpdate.plus(inputUpdate);
            
            // Update states
            for (int i = 0; i < numStates && i < newStateVector.getRowDimension(); i++) {
                xStateList.get(i).setData(new Data(newStateVector.get(i, 0)));
            }
        }
    }
}
