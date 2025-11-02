package com.ncslab.block.discontinuous;

import com.ncslab.block.discontinuous.DiscontinuousBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discontinuous.CoulombDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Coulomb block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Offset: Offset value for Coulomb friction
 * - Gain: Gain value for Coulomb friction
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Coulomb extends DiscontinuousBlock {
    // Legacy fields for backward compatibility
    Parameter offset;
    Parameter gain;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter offsetParam;
    private final Parameter gainParam;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    public static final HashMap<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<HashMap<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<HashMap<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Parameter defaults
        PARAMETER_DEFAULTS.put("Offset", "0");
        PARAMETER_DEFAULTS.put("Gain", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        HashMap<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (coulomb has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        HashMap<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private Coulomb(Parameter offset, Parameter gain, Parameter sampleTime,
                   Parameter outDataType, Parameter saturateOnIntegerOverflow,
                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.offsetParam = Objects.requireNonNull(offset, "Offset parameter cannot be null");
        this.gainParam = Objects.requireNonNull(gain, "Gain parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Legacy field mapping for backward compatibility
        this.offset = this.offsetParam;
        this.gain = this.gainParam;
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Coulomb(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.offsetParam = getParameterByName("Offset");
        this.gainParam = getParameterByName("Gain");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list

        // Legacy field mapping for backward compatibility
        this.offset = this.offsetParam;
        this.gain = this.gainParam;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates Coulomb block directly from BlockDto DTO
     */
    public Coulomb(CoulombDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.offsetParam = getParameterByName("Offset");
        this.gainParam = getParameterByName("Gain");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
        
        // Legacy field mapping for backward compatibility
        this.offset = this.offsetParam;
        this.gain = this.gainParam;

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static Coulomb fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter offset = createOffsetFromJSON(paramValues, blockName);
            Parameter gain = createGainFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Coulomb block = new Coulomb(offset, gain, sampleTime, outDataType, saturateParam,
                                       blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, offset, gain, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Coulomb block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Coulomb create(String name, String path, String offset, String gain, NCSLabModel model) {
        return create(name, path, offset, gain, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a Coulomb block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param offset Offset value for Coulomb friction
     * @param gain Gain value for Coulomb friction
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Coulomb block instance
     */
    public static Coulomb create(String name, String path, String offset, String gain,
                                double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        CoulombDto dto = CoulombDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .offset(com.ncslab.dto.common.TypedParameter.of(offset))
            .gain(com.ncslab.dto.common.TypedParameter.of(gain))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Coulomb parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Coulomb(dto, model);
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createOffsetFromJSON(JSONObject paramValues, String blockName) {
        String offsetValue = paramValues.optString("offset", "1");
        return new Parameter(null, 1, "Offset", offsetValue);
    }
    
    private static Parameter createGainFromJSON(JSONObject paramValues, String blockName) {
        String gainValue = paramValues.optString("gain", "1");
        return new Parameter(null, 2, "Gain", gainValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 4, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(Coulomb block, Parameter... parameters) {
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
        identity.put("blockType", "Coulomb");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    @Override
    public void calculateInit() {
        // Initialize Coulomb friction block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                double signValue = inputData.getInitValue() >= 0 ? 1.0 : -1.0;
                double absValue = Math.abs(inputData.getInitValue());
                double offsetValue = offset.getDouble();
                double gainValue = gain.getDouble();
                resultData = new Data(signValue * (gainValue * absValue + offsetValue));
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        double signValueMatrix = inputData.getMatrix().get(i, j) >= 0 ? 1.0 : -1.0;
                        double absValueMatrix = Math.abs(inputData.getMatrix().get(i, j));
                        double offsetValueMatrix = offset.getMatrix().get(i, j);
                        double gainValueMatrix = gain.getMatrix().get(i, j);
                        matrixResult.set(i, j, signValueMatrix * (gainValueMatrix * absValueMatrix + offsetValueMatrix));
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private void prepareContext() {
        // Fail fast - validate required ports and connections exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new BlockCreationException("Coulomb block cannot prepare context: no output ports configured");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("Coulomb block cannot prepare context: no input ports configured");
        }
        
        OutputPort out = outputPortList.get(0);
        InputPort inputPort = inputPortList.get(0);
        
        if (out == null) {
            throw new BlockCreationException("Coulomb block cannot prepare context: output port is null");
        }
        if (inputPort == null || inputPort.getLinkedLine() == null || 
            inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            throw new BlockCreationException("Coulomb block cannot prepare context: input port not properly connected");
        }
        
        OutputPort ops = inputPort.getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = ops.getOutputSignalC();
        
        if (signal == null) {
            throw new BlockCreationException("Coulomb block cannot prepare context: input signal is null");
        }

        context.put("block", this); // 当前Block对象（含getBlockId()）
        context.put("inputPortList", inputPortList); // 输入端口列表
        context.put("outputPortList", outputPortList); // 输出端口列表
        context.put("offset", offset); // 偏移量参数对象
        context.put("gain", gain); // 增益参数对象
        context.put("signal",signal);
        context.put("ops", ops);
        context.put("realDataType", DataType.REAL); // 实数类型标识
        context.put("matrixDataType", DataType.MATRIX); // 矩阵类型标识
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("offset", offset);
        context.put("gain", gain);
        
        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Coulomb/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        
        // Fail fast - validate required ports and connections exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new BlockCreationException("Coulomb block cannot generate MATLAB output code: no output ports configured");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("Coulomb block cannot generate MATLAB output code: no input ports configured");
        }
        
        OutputPort out = outputPortList.get(0);
        InputPort inputPort = inputPortList.get(0);
        
        if (out == null) {
            throw new BlockCreationException("Coulomb block cannot generate MATLAB output code: output port is null");
        }
        if (inputPort == null || inputPort.getLinkedLine() == null || 
            inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            throw new BlockCreationException("Coulomb block cannot generate MATLAB output code: input port not properly connected");
        }
        
        OutputPort ops = inputPort.getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = ops.getOutputSignalC();
        
        if (signal == null) {
            throw new BlockCreationException("Coulomb block cannot generate MATLAB output code: input signal is null");
        }
        
        context.put("block", this);
        context.put("outputSignal", out.getOutputSignalC().getName()); // Use signal name, not object
        context.put("inputSignal", signal);
        context.put("gain", gain);
        context.put("offset", offset);
        context.put("inputHeight", ops.getHeight());
        context.put("inputWidth", ops.getWidth());
        context.put("gainHeight", gain.getHeight());
        context.put("gainWidth", gain.getWidth());
        
        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Coulomb/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        prepareContext();
        
        // Add initialization code variables for Coulomb template
        context.put("offsetInitCodeC", "/* Offset initialization for " + offset.getName() + " */");
        context.put("gainInitCodeC", "/* Gain initialization for " + gain.getName() + " */");
        
        String initCode = TemplateManager.renderTemplate("c/discontinuous/Coulomb/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code){
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        prepareContext();  // Add the prepareContext call to provide all necessary template variables
        
        // Add specific template variables needed for Coulomb
        if (inputPortList != null && !inputPortList.isEmpty()) {
            InputPort inputPort = inputPortList.get(0);
            if (inputPort != null && inputPort.getLinkedLine() != null && 
                inputPort.getLinkedLine().getLinkedOutputPort() != null) {
                OutputPort ops = inputPort.getLinkedLine().getLinkedOutputPort();
                context.put("opsHeight", ops.getHeight());
                context.put("opsWidth", ops.getWidth());
                context.put("signalName", getInputPortVariable(0));
                context.put("gainName", context.get(gain.getLocalName())); // Use parameter local name mapped by TemplateUtils // C variable name
                context.put("offsetName", context.get(offset.getLocalName())); // Use parameter local name mapped by TemplateUtils
                context.put("outputSignalName", getOutputPortVariable(0));
            }
        }
        
        String outputCode = TemplateManager.renderTemplate("c/discontinuous/Coulomb/output.vm", context);
        code.addOutputCode(outputCode);
    }

     public void updateDimension() throws MatDimException{
        // Fail fast - validate required ports and connections exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new MatDimException("Coulomb block cannot update dimensions: no output ports configured");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new MatDimException("Coulomb block cannot update dimensions: no input ports configured");
        }
        
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        
        if (out == null) {
            throw new MatDimException("Coulomb block cannot update dimensions: output port is null");
        }
        if (in == null || in.getLinkedLine() == null || in.getLinkedLine().getLinkedOutputPort() == null) {
            throw new MatDimException("Coulomb block cannot update dimensions: input port not properly connected");
        }
        
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new MatDimException("Coulomb block cannot update dimensions: input signal is null");
        }
        if(offset.getWidth()!=gain.getWidth()||offset.getHeight()!=gain.getHeight()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
            throw(e);
        }
        if(offset.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
            out.setHeight(offset.getHeight());
            out.setWidth(offset.getWidth());
            out.getOutputSignalC().setHeight(offset.getHeight());
            out.getOutputSignalC().setWidth(offset.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }
        else if(offset.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        }
        else{
            if(offset.getWidth()!=signal.getWidth()||offset.getHeight()!=signal.getHeight()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the Coulomb dimension!\n \n");
            throw(e);
            }
            out.setHeight(offset.getHeight());
            out.setWidth(offset.getWidth());
            out.getOutputSignalC().setHeight(offset.getHeight());
            out.getOutputSignalC().setWidth(offset.getWidth());
            out.getOutputSignalC().setDataType(offset.getDataType());
        }
    }
    public void checkDimension() throws MatDimException{
    }
}
