package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.logic.DetectDecreaseDto;

import com.ncslab.block.logicAndBit.LogicBlock;
import Jama.Matrix;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.ncslab.util.TemplateManager;

/**
 * DetectDecrease block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - VinWhenRising: Output value when rising edge is detected
 * - VinWhenFalling: Output value when falling edge is detected
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * - InitialState: Initial value for edge detection
 */
public class DetectDecrease extends LogicBlock {

    private Data previousData;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter vinWhenRising;
    private final Parameter vinWhenFalling;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    private final Parameter initialState;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");

        // Parameter defaults
        PARAMETER_DEFAULTS.put("VinWhenRising", "0");
        PARAMETER_DEFAULTS.put("VinWhenFalling", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        PARAMETER_DEFAULTS.put("InitialState", "0");
    }

    // === Private Constructor with Typed Parameters ===
    private DetectDecrease(Parameter vinWhenRising, Parameter vinWhenFalling, Parameter sampleTime,
                          Parameter outDataType, Parameter saturateOnIntegerOverflow, Parameter initialState,
                          String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.vinWhenRising = Objects.requireNonNull(vinWhenRising, "VinWhenRising parameter cannot be null");
        this.vinWhenFalling = Objects.requireNonNull(vinWhenFalling, "VinWhenFalling parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        this.initialState = Objects.requireNonNull(initialState, "Initial state parameter cannot be null");
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DetectDecrease(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.vinWhenRising = getParameterByName("VinWhenRising");
        this.vinWhenFalling = getParameterByName("VinWhenFalling");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        this.initialState = getParameterByName("InitialState");

        // Add all parameters to parameter list

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates DetectDecrease block directly from BlockDto DTO
     */
    public DetectDecrease(DetectDecreaseDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.vinWhenRising = getParameterByName("VinWhenRising");
        this.vinWhenFalling = getParameterByName("VinWhenFalling");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        this.initialState = getParameterByName("InitialState");
        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static DetectDecrease fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter vinWhenRising = createVinWhenRisingFromJSON(paramValues, blockName);
            Parameter vinWhenFalling = createVinWhenFallingFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            Parameter initialState = createInitialStateFromJSON(paramValues, blockName);

            DetectDecrease block = new DetectDecrease(vinWhenRising, vinWhenFalling, sampleTime,
                                                     outDataType, saturateParam, initialState,
                                                     blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, vinWhenRising, vinWhenFalling, sampleTime,
                                     outDataType, saturateParam, initialState);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DetectDecrease block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (Simple Overload) ===
    public static DetectDecrease create(String name, String path, NCSLabModel model) {
        return create(name, path, "0", "1", -1.0, "Inherit: Logical (see Configuration Parameters: Optimization)", false, "0", model);
    }

    /**
     * Create a DetectDecrease block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * NOTE: DetectDecreaseDto currently only supports initialValue field.
     * The additional parameters (vinWhenRising, vinWhenFalling, sampleTime, outDataType, saturateOnOverflow)
     * are stored in the base BlockDto parameter map for backward compatibility.
     *
     * @param name Block name
     * @param path Block path
     * @param vinWhenRising Output value when rising edge is detected (no decrease)
     * @param vinWhenFalling Output value when falling edge is detected (decrease detected)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param initialState Initial value for edge detection
     * @param model Parent model
     * @return DetectDecrease block instance
     */
    public static DetectDecrease create(String name, String path, String vinWhenRising, String vinWhenFalling,
                                       double sampleTime, String outDataType, boolean saturateOnOverflow,
                                       String initialState, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        // NOTE: DetectDecreaseDto is missing most fields, so we only set initialValue
        DetectDecreaseDto dto = DetectDecreaseDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .initialValue(com.ncslab.dto.common.TypedParameter.of(Double.parseDouble(initialState)))
            .build();

        // Add missing parameters to the DTO's parameter map (workaround for incomplete DTO)
        if (dto.getParameters() == null) {
            dto.setParameters(new com.ncslab.dto.common.TypedParameterMap());
        }
        dto.getParameters().put("VinWhenRising", com.ncslab.dto.common.TypedParameter.of(vinWhenRising));
        dto.getParameters().put("VinWhenFalling", com.ncslab.dto.common.TypedParameter.of(vinWhenFalling));
        dto.getParameters().put("SampleTime", com.ncslab.dto.common.TypedParameter.of(sampleTime));
        dto.getParameters().put("OutDataTypeStr", com.ncslab.dto.common.TypedParameter.of(outDataType));
        dto.getParameters().put("SaturateOnIntegerOverflow", com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow));
        dto.getParameters().put("InitialState", com.ncslab.dto.common.TypedParameter.of(initialState));

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid DetectDecrease parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new DetectDecrease(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createVinWhenRisingFromJSON(JSONObject paramValues, String blockName) {
        String vinValue = paramValues.optString("VinWhenRising", "0");
        return new Parameter(null, 1, "VinWhenRising", vinValue);
    }

    private static Parameter createVinWhenFallingFromJSON(JSONObject paramValues, String blockName) {
        String vinValue = paramValues.optString("VinWhenFalling", "1");
        return new Parameter(null, 2, "VinWhenFalling", vinValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        return new Parameter(null, 4, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateValue);
    }

    private static Parameter createInitialStateFromJSON(JSONObject paramValues, String blockName) {
        String initialStateValue = paramValues.optString("InitialState", "0");
        return new Parameter(null, 6, "InitialState", initialStateValue);
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

    private static void setParameterBlockReference(DetectDecrease block, Parameter... parameters) {
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
        identity.put("blockType", "DetectDecrease");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    @Override
    public void calculateInit() {
        // Initialize with initial state parameter
        double initialValue = Double.parseDouble(initialState.getInitString());
        previousData = new Data(initialValue);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        double risingValue = Double.parseDouble(vinWhenRising.getInitString());
        double fallingValue = Double.parseDouble(vinWhenFalling.getInitString());

        Data resultData;
        if (previousData == null) {
            // First run - no previous value to compare, always output risingValue
            resultData = new Data(risingValue); // For decrease detection, no decrease on first run
        } else {
            switch (inputData.getDataType()) {
                case REAL:
                    double currentValue = inputData.getInitValue();
                    double previousValue = previousData.getDataType() == DataType.REAL ? previousData.getInitValue() : Double.parseDouble(initialState.getInitString());
                    resultData = new Data(currentValue < previousValue ? fallingValue : risingValue);
                    break;
                case MATRIX:
                    Matrix currentMatrix = inputData.getMatrix();
                    Matrix matrixResult = new Matrix(currentMatrix.getRowDimension(), currentMatrix.getColumnDimension());

                    if (previousData.getDataType() == DataType.MATRIX) {
                        // Previous was matrix - element-wise comparison
                        Matrix previousMatrix = previousData.getMatrix();
                        for (int i = 0; i < currentMatrix.getRowDimension(); i++) {
                            for (int j = 0; j < currentMatrix.getColumnDimension(); j++) {
                                matrixResult.set(i, j, currentMatrix.get(i, j) < previousMatrix.get(i, j) ? fallingValue : risingValue);
                            }
                        }
                    } else {
                        // Previous was scalar - compare each element to scalar value
                        double previousScalarValue = previousData.getInitValue();
                        for (int i = 0; i < currentMatrix.getRowDimension(); i++) {
                            for (int j = 0; j < currentMatrix.getColumnDimension(); j++) {
                                matrixResult.set(i, j, currentMatrix.get(i, j) < previousScalarValue ? fallingValue : risingValue);
                            }
                        }
                    }
                    resultData = new Data(matrixResult);
                    break;
                default:
                    resultData = new Data(risingValue);
            }
        }

        out.setData(resultData);

        // Store copy of current input for next comparison - handle both scalar and matrix
        if (inputData.getDataType() == DataType.REAL) {
            previousData = new Data(inputData.getInitValue());
        } else {
            previousData = new Data(inputData.getMatrix());
        }
    }

    public void generateArraysCodeC(CodeStructC code) {
        // Populate all standard context variables (blockId, block, inputs, outputs, parameters, etc.)
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add DetectDecrease-specific variables
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("signal", signal);

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/DetectDecrease/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // Populate all standard context variables (blockId, block, inputs, outputs, parameters, etc.)
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String initCode="";
        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/DetectDecrease/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException{
        OutputPort out  = outputPortList.get(0);
        InputPort in  = inputPortList.get(0);
        OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
   }

    public void checkDimension() throws MatDimException{
    }
}
