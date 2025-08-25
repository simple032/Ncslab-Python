package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import com.ncslab.dto.block.specialized.math.AddDto;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.math.MathBlock;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Add block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - Inputs: String sequence defining input signs (e.g., "++", "+-", "++--")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - InputSameDT: Require inputs to have same data type
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Add extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private Parameter inputs;
    private Parameter sampleTime;
    private Parameter inputSameDT;
    private Parameter outDataType;
    private Parameter saturateOnIntegerOverflow;

    // === Operational Settings ===
    @Getter
    private String inputSequence;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "++");  // Two positive inputs by default
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("InputSameDT", "on");  // Require same data type
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
        // Input names are dynamic based on sequence length
    }

    // === Private Constructor with Typed Parameters ===
    private Add(Parameter inputs, Parameter sampleTime, Parameter inputSameDT,
               Parameter outDataType, Parameter saturateOnIntegerOverflow,
               String blockName, String blockPath, String blockUUID,
               NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Extract input sequence from inputs parameter
        this.inputSequence = inputs.getInitString();

        // Validate parameters
        validateParameters(inputs, sampleTime);

        // Assign parameters
        this.inputs = Objects.requireNonNull(inputs, "Inputs parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.inputSameDT = Objects.requireNonNull(inputSameDT, "InputSameDT parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports based on input sequence
        initializePorts();
    }

    @Deprecated
    public Add(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        initializeBlock();
    }


    public Add(AddDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Validate DTO
        dto.validate();
        
        // Extract parameters from AddDto (relying on @Builder.Default for defaults)
        String inputsValue = dto.getInputsValue();
        String sampleTimeValue = dto.getSampleTimeValue().toString();
        String inputSameDTValue = "on"; // Fixed parameter for Add block
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";

        // Initialize final parameters from DTO
        this.inputs = getParameterByName("Inputs");
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        this.inputSequence = this.inputs.getInitString();
        
        // Validate parameters
        validateParameters(this.inputs, this.sampleTime);
        
        // Initialize ports
        initializePorts();
    }

    public static Add fromDto(AddDto dto, NCSLabModel model) {
        return new Add(dto, model);
    }

    private void initializeBlock() {
        // Legacy JSONObject initialization
        this.inputs = getParameterByName("Inputs");
        if (this.inputs != null) {
            this.inputSequence = this.inputs.getInitString();
        } else {
            this.inputSequence = "++"; // Default value
        }
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Initialize ports
        initializePorts();
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Add fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            // Extract and validate JSON fields
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            // Create typed parameters from JSON with defaults
            Parameter inputs = createInputsFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter inputSameDT = createInputSameDTFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Add block = new Add(inputs, sampleTime, inputSameDT, outDataType, saturateParam,
                               blockName, blockPath, blockUUID, model);

            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, inputs, sampleTime, inputSameDT, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Add block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Add create(String name, String path, String inputSequence, NCSLabModel model) {
        return create(name, path, inputSequence, -1.0, true, "Inherit: Same as input", false, model);
    }

    public static Add create(String name, String path, String inputSequence,
                            double sampleTime, boolean inputSameDT, String outDataType,
                            boolean saturateOnOverflow, NCSLabModel model) {
        // Create parameters
        Parameter inputs = new Parameter(null, 1, "Inputs", inputSequence);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter inputSameDTParam = new Parameter(null, 3, "InputSameDT", inputSameDT ? "on" : "off");
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        Add block = new Add(inputs, sampleTimeParam, inputSameDTParam, outDataTypeParam, saturateParam,
                           name, path, "null", model);

        // Set block reference in parameters
        setParameterBlockReference(block, inputs, sampleTimeParam, inputSameDTParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter inputs, Parameter sampleTime) {
        // Validate input sequence
        String sequence = inputs.getInitString();
        if (sequence == null || sequence.trim().isEmpty()) {
            throw new IllegalArgumentException("Input sequence cannot be empty");
        }

        // Validate sequence contains only + and - characters
        for (char c : sequence.toCharArray()) {
            if (c != '+' && c != '-') {
                throw new IllegalArgumentException("Input sequence must contain only '+' and '-' characters");
            }
        }

        if (sequence.length() < 1) {
            throw new IllegalArgumentException("Input sequence must have at least one input");
        }

        // Validate sample time
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInputsFromJSON(JSONObject paramValues, String blockName) {
        String inputsValue = paramValues.optString("Inputs", "++");
        return new Parameter(null, 1, "Inputs", inputsValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }

    private static Parameter createInputSameDTFromJSON(JSONObject paramValues, String blockName) {
        String inputSameDTValue = paramValues.optString("InputSameDT", "on");
        return new Parameter(null, 3, "InputSameDT", inputSameDTValue);
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

    private static void setParameterBlockReference(Add block, Parameter... parameters) {
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
        identity.put("blockType", "Add");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to satisfy base constructor
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Create output port with feedthrough (add is instantaneous)
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        // Create input ports based on sequence length
        for (int i = 0; i < inputSequence.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        // Update input names for static reference
        inputNames.clear();
        for (int i = 0; i < inputSequence.length(); i++) {
            inputNames.add("in" + (i + 1));
        }
    }

    // === Legacy Compatibility Methods ===
    @Deprecated
    public String getSeq() {
        return inputSequence;
    }

    public boolean getSign(int n) {
        return inputSequence.charAt(n) == '+';
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add Add-specific context
        OutputPort out = outputPortList.get(0);
        OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        context.put("sequence", inputSequence);
        context.put("outputSignal", out.getOutputSignalC());
        context.put("firstInputSignal", ops1.getOutputSignalC());

        String codeStr = TemplateManager.renderTemplate("m/math/Add/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add Add-specific context
        context.put("sequence", getInputSequence());
        context.put("inputs", this.inputs);

        String codeStr = TemplateManager.renderTemplate("c/math/Add/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] signal = new OutputSignal[inputSequence.length()];
        for (int i = 0; i < inputSequence.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }
        int m = signal[0].getHeight();
        int n = signal[0].getWidth();
        int v = 1;
        for (OutputSignal x : signal) {
            if ((x.getHeight() != m) || (x.getWidth() != n)) {
                v = 0;
                MatDimException e = new MatDimException("Block " + this.blockName + " " + inputSequence.length() + " input dimensions doesn't match !\n \n");
                throw(e);
            }
        }
        if (v == 1) {
            out.setHeight(signal[0].getHeight());
            out.setWidth(signal[0].getWidth());
            out.getOutputSignalC().setHeight(signal[0].getHeight());
            out.getOutputSignalC().setWidth(signal[0].getWidth());
            out.getOutputSignalC().setDataType(signal[0].getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateOutput(double t){
        OutputPort out = outputPortList.get(0);
        Data data = new Data(out.getHeight(),out.getWidth());
        for (int i = 0; i < inputSequence.length(); i++) {
            if(inputSequence.charAt(i) == '+'){
                data = data.plus(inputPortList.get(i).getData());
            }else if(inputSequence.charAt(i) == '-'){
                data = data.minus(inputPortList.get(i).getData());
            }
        }
        out.setData(data);
    }
}

