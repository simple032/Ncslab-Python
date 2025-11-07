package com.ncslab.block.discrete;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.dto.block.specialized.discrete.DiscreteTransferFcnDto;
import com.ncslab.dto.core.BlockDto;
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
    private List<State> xStateList = new ArrayList<>();

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

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

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

        // Add parameters to parameterList for template context population
        parameterList.add(this.numerator);
        parameterList.add(this.denominator);
        parameterList.add(this.initialStates);
        parameterList.add(this.sampleTimeParam);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);


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
        this.numerator = getParameterByName("Numerator");
        this.denominator = getParameterByName("Denominator");
        this.initialStates = getParameterByName("InitialStates");
        this.sampleTimeParam = getParameterByName("SampleTime");

        // Create missing SIMULINK parameters with defaults
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
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
    }    /**
     * DTO-NATIVE Constructor - Creates Discrete_Transfer_Fcn block directly from BlockDto DTO
     */
    public Discrete_Transfer_Fcn(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.numerator = getParameterByName("Numerator");
        this.denominator = getParameterByName("Denominator");
        this.initialStates = getParameterByName("InitialStates");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
        
        // Determine feedthrough
        if (denominator.getWidth() == numerator.getWidth()) {
            feedThrough = true;
        }
        
        // Create state variables
        for (int i = 0; i < denominator.getWidth() - 1; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }
        
        setSampleTime(sampleTimeParam);

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedThrough));
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

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Discrete_Transfer_Fcn create(String name, String path, String numerator, String denominator,
                                               String initialStates, double sampleTime, NCSLabModel model) {
        return create(name, path, numerator, denominator, initialStates, sampleTime, "Inherit: Same as input", false, model);
    }

    /**
     * Create a Discrete_Transfer_Fcn block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param numerator Numerator coefficients of the transfer function
     * @param denominator Denominator coefficients of the transfer function
     * @param initialStates Initial states of the transfer function
     * @param sampleTime Sample time for discrete operation
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Discrete_Transfer_Fcn block instance
     */
    public static Discrete_Transfer_Fcn create(String name, String path, String numerator, String denominator,
                                               String initialStates, double sampleTime, String outDataType,
                                               boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DiscreteTransferFcnDto dto = DiscreteTransferFcnDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .numerator(com.ncslab.dto.common.TypedParameter.of(numerator))
            .denominator(com.ncslab.dto.common.TypedParameter.of(denominator))
            .initialStates(com.ncslab.dto.common.TypedParameter.of(initialStates))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Discrete_Transfer_Fcn parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Discrete_Transfer_Fcn(dto, model);
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

    @Override
    public void calculateOutput(double t) {
        // Discrete transfer function: H(z) = num(z)/den(z)
        // Implemented as direct form II structure
        OutputPort output = outputPortList.get(0);
        InputPort input = inputPortList.get(0);
        
        if (input.getData() == null) {
            output.setData(new Data(0.0));
            return;
        }
        
        Data inputData = input.getData();
        double[] numCoeffs = numerator.getData().getDoubleArray();
        double[] denCoeffs = denominator.getData().getDoubleArray();
        
        if (inputData.getDataType() == DataType.REAL) {
            double inputValue = inputData.getInitValue();
            double outputValue = 0.0;
            
            // Calculate numerator contribution: sum(num[i] * delayed_inputs[i])
            // For direct form II: num[0] * u[k] + num[1] * x1[k] + num[2] * x2[k] + ...
            outputValue += numCoeffs[0] * inputValue;
            
            // Add contributions from internal states (delayed inputs)
            for (int i = 0; i < xStateList.size() && i + 1 < numCoeffs.length; i++) {
                State state = xStateList.get(i);
                if (state.getData() != null) {
                    outputValue += numCoeffs[i + 1] * state.getData().getInitValue();
                }
            }
            
            // Handle edge cases
            if (Double.isNaN(outputValue) || Double.isInfinite(outputValue)) {
                outputValue = 0.0;
            }
            
            output.setData(new Data(outputValue));
            
        } else if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix case - apply transfer function element-wise
            Jama.Matrix inputMatrix = inputData.getMatrix();
            int rows = inputMatrix.getRowDimension();
            int cols = inputMatrix.getColumnDimension();
            Jama.Matrix outputMatrix = new Jama.Matrix(rows, cols);
            
            // For matrices, we apply the transfer function to each element
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    double inputElement = inputMatrix.get(row, col);
                    double outputElement = 0.0;
                    
                    // Calculate transfer function for this element
                    outputElement += numCoeffs[0] * inputElement;
                    
                    // Add state contributions (this is simplified - in practice,
                    // matrix transfer functions would need separate state storage per element)
                    for (int i = 0; i < xStateList.size() && i + 1 < numCoeffs.length; i++) {
                        State state = xStateList.get(i);
                        if (state.getData() != null && state.getData().getDataType() == DataType.MATRIX) {
                            Jama.Matrix stateMatrix = state.getData().getMatrix();
                            if (row < stateMatrix.getRowDimension() && col < stateMatrix.getColumnDimension()) {
                                outputElement += numCoeffs[i + 1] * stateMatrix.get(row, col);
                            }
                        } else if (state.getData() != null) {
                            // Use scalar state for all matrix elements
                            outputElement += numCoeffs[i + 1] * state.getData().getInitValue();
                        }
                    }
                    
                    // Handle edge cases
                    if (Double.isNaN(outputElement) || Double.isInfinite(outputElement)) {
                        outputElement = 0.0;
                    }
                    
                    outputMatrix.set(row, col, outputElement);
                }
            }
            
            output.setData(new Data(outputMatrix));
            
        } else {
            // Unknown data type
            output.setData(new Data(0.0));
        }
    }

    @Override
    public void calculateInit() {
        // Initialize discrete transfer function block
        OutputPort output = outputPortList.get(0);
        
        // Initialize internal states with initial states parameter
        double ic = initialStates.getDouble();
        for (State state : xStateList) {
            if (state.getDataType() == DataType.REAL) {
                state.setData(new Data(ic));
            } else if (state.getDataType() == DataType.MATRIX) {
                int height = state.getHeight();
                int width = state.getWidth();
                Jama.Matrix icMatrix = new Jama.Matrix(height, width);
                
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        icMatrix.set(i, j, ic);
                    }
                }
                state.setData(new Data(icMatrix));
            }
        }
        
        // Initialize output to zero
        output.setData(new Data(0.0));
    }
    
    @Override
    public void calculateUpdate(double t) {
        // Update discrete transfer function states
        // Direct Form II: x[k+1] = shift register of delayed inputs minus feedback
        InputPort input = inputPortList.get(0);
        
        if (input.getData() == null || xStateList.isEmpty()) {
            return;
        }
        
        Data inputData = input.getData();
        double[] denCoeffs = denominator.getData().getDoubleArray();
        
        if (inputData.getDataType() == DataType.REAL) {
            double inputValue = inputData.getInitValue();
            
            // Calculate the intermediate signal w[k] for direct form II
            double wk = inputValue;
            
            // Subtract feedback terms: w[k] = u[k] - sum(den[i+1] * x[i])
            for (int i = 0; i < xStateList.size() && i + 1 < denCoeffs.length; i++) {
                State state = xStateList.get(i);
                if (state.getData() != null) {
                    wk -= denCoeffs[i + 1] * state.getData().getInitValue();
                }
            }
            
            // Shift register: x[k+1] = [w[k], x1[k], x2[k], ...]
            // Update states in reverse order to avoid overwriting
            for (int i = xStateList.size() - 1; i > 0; i--) {
                State currentState = xStateList.get(i);
                State previousState = xStateList.get(i - 1);
                if (previousState.getData() != null) {
                    currentState.setData(new Data(previousState.getData().getInitValue()));
                }
            }
            
            // Set first state to intermediate signal
            if (!xStateList.isEmpty()) {
                xStateList.get(0).setData(new Data(wk));
            }
            
        } else if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix case - similar update but for matrix elements
            Jama.Matrix inputMatrix = inputData.getMatrix();
            
            // For simplicity, we handle matrix case by using scalar approach
            // In practice, you might want separate state storage per matrix element
            double inputScalar = 0.0;
            if (inputMatrix.getRowDimension() > 0 && inputMatrix.getColumnDimension() > 0) {
                inputScalar = inputMatrix.get(0, 0);
            }
            
            double wk = inputScalar;
            for (int i = 0; i < xStateList.size() && i + 1 < denCoeffs.length; i++) {
                State state = xStateList.get(i);
                if (state.getData() != null) {
                    wk -= denCoeffs[i + 1] * state.getData().getInitValue();
                }
            }
            
            // Shift register update
            for (int i = xStateList.size() - 1; i > 0; i--) {
                State currentState = xStateList.get(i);
                State previousState = xStateList.get(i - 1);
                if (previousState.getData() != null) {
                    currentState.setData(new Data(previousState.getData().getInitValue()));
                }
            }
            
            if (!xStateList.isEmpty()) {
                xStateList.get(0).setData(new Data(wk));
            }
        }
    }
}
