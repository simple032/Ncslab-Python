package com.ncslab.block.logicAndBit;

import com.ncslab.block.logicAndBit.LogicBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.logic.ShiftArithmeticDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
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
 * ShiftArithmetic block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - BitShiftNumber: Number of bits to shift (positive for left shift, negative for right shift)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class ShiftArithmetic extends LogicBlock {
    // Legacy field for backward compatibility
    Parameter value;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter bitShiftNumber;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("BitShiftNumber", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Private Constructor with Typed Parameters ===
    private ShiftArithmetic(Parameter bitShiftNumber, Parameter sampleTime, Parameter outDataType,
                           Parameter saturateOnIntegerOverflow,
                           String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.bitShiftNumber = Objects.requireNonNull(bitShiftNumber, "BitShiftNumber parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Legacy field mapping for backward compatibility
        this.value = this.bitShiftNumber;
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public ShiftArithmetic(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.bitShiftNumber = getParameterByName("BitShiftNumber");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list

        // Legacy field mapping for backward compatibility
        this.value = this.bitShiftNumber;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates ShiftArithmetic block directly from BlockDto DTO
     */
    public ShiftArithmetic(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.bitShiftNumber = getParameterByName("BitShiftNumber");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }// === Static Factory Method for JSON Deserialization ===
    public static ShiftArithmetic fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter bitShiftNumber = createBitShiftNumberFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            ShiftArithmetic block = new ShiftArithmetic(bitShiftNumber, sampleTime, outDataType, saturateParam,
                                                        blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, bitShiftNumber, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create ShiftArithmetic block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation (Simple Overload) ===
    public static ShiftArithmetic create(String name, String path, int bitShiftNumber, NCSLabModel model) {
        return create(name, path, String.valueOf(bitShiftNumber), -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a ShiftArithmetic block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * NOTE: ShiftArithmeticDto supports shiftDirection, numberOfBits, and arithmeticShift fields.
     * The bitShiftNumber parameter is converted to direction and numberOfBits.
     * The additional parameters (sampleTime, outDataType, saturateOnOverflow) are
     * stored in the base BlockDto parameter map for backward compatibility.
     *
     * @param name Block name
     * @param path Block path
     * @param bitShiftNumber Number of bits to shift (positive for left, negative for right)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return ShiftArithmetic block instance
     */
    public static ShiftArithmetic create(String name, String path, String bitShiftNumber, double sampleTime,
                                        String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Parse bitShiftNumber to determine direction and number of bits
        int shiftValue = Integer.parseInt(bitShiftNumber);
        String direction = shiftValue >= 0 ? "Left" : "Right";
        int numberOfBits = Math.abs(shiftValue);

        // Build DTO using type-safe builder pattern
        // NOTE: ShiftArithmeticDto has shiftDirection, numberOfBits, arithmeticShift but missing some fields
        ShiftArithmeticDto dto = ShiftArithmeticDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .shiftDirection(com.ncslab.dto.common.TypedParameter.of(direction))
            .numberOfBits(com.ncslab.dto.common.TypedParameter.of(numberOfBits))
            .arithmeticShift(com.ncslab.dto.common.TypedParameter.of(true))  // Default to arithmetic shift
            .build();

        // Add missing parameters to the DTO's parameter map (workaround for incomplete DTO)
        if (dto.getParameters() == null) {
            dto.setParameters(new com.ncslab.dto.common.TypedParameterMap());
        }
        dto.getParameters().put("BitShiftNumber", com.ncslab.dto.common.TypedParameter.of(shiftValue));
        dto.getParameters().put("SampleTime", com.ncslab.dto.common.TypedParameter.of(sampleTime));
        dto.getParameters().put("OutDataTypeStr", com.ncslab.dto.common.TypedParameter.of(outDataType));
        dto.getParameters().put("SaturateOnIntegerOverflow", com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow));

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid ShiftArithmetic parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new ShiftArithmetic(dto, model);
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createBitShiftNumberFromJSON(JSONObject paramValues, String blockName) {
        String bitShiftNumberValue = paramValues.optString("BitShiftNumber", "1");
        return new Parameter(null, 1, "BitShiftNumber", bitShiftNumberValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 3, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 4, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(ShiftArithmetic block, Parameter... parameters) {
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
        identity.put("blockType", "ShiftArithmetic");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    @Override
    public void calculateInit() {
        // Initialization logic for ShiftArithmetic block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();
        int shiftValue = (int) bitShiftNumber.getDouble();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                resultData = new Data(shift(inputData.getInitValue(), shiftValue));
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        matrixResult.set(i, j, shift(inputData.getMatrix().get(i, j), shiftValue));
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private double shift(double inputValue, int shiftAmount) {
        return inputValue * Math.pow(2, shiftAmount);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // Populate all standard context variables (blockId, block, inputs, outputs, parameters, etc.)
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add ShiftArithmetic-specific variables for backward compatibility
        context.put("value", bitShiftNumber);

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/ShiftArithmetic/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/ShiftArithmetic/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        if (bitShiftNumber.getDataType() == DataType.MATRIX) {
            MatDimException e = new MatDimException("Block " + this.blockName + " param Number can't be MATRIX!\n \n");
            throw(e);
        }
    }
}
