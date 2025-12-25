package com.ncslab.block.continuous;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.continuous.TransferFcnDto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Objects;

/**
 * TransferFcn block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Numerator: Numerator coefficients in descending powers of s
 * - Denominator: Denominator coefficients in descending powers of s
 * - AbsoluteTolerance: Absolute tolerance for simulation
 * - ContinuousStateAttributes: Attributes for continuous states
 * - RealizeZeroPoleGain: Realization method for zero-pole-gain
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class TransferFcn extends ContinuousBlock {
    // === Internal Implementation ===
    private double D = 0;
    private boolean feedThrough = false;
    private double[] num;
    private double[] den;
    private List<State> xStateList = new ArrayList<>();
    
    // === SIMULINK-Compatible Parameters ===
    private final Parameter numerator;
    @Getter
    private final Parameter denominator;
    private final Parameter absoluteTolerance;
    private final Parameter continuousStateAttributes;
    private final Parameter realizeZeroPoleGain;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Port References ===
    private OutputPort output;
    private InputPort input;
    
    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        
        // Parameter defaults for control system transfer functions
        PARAMETER_DEFAULTS.put("Numerator", "[1]");
        PARAMETER_DEFAULTS.put("Denominator", "[1 1]");
        PARAMETER_DEFAULTS.put("AbsoluteTolerance", "auto");
        PARAMETER_DEFAULTS.put("ContinuousStateAttributes", "'''");
        PARAMETER_DEFAULTS.put("RealizeZeroPoleGain", "off");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (feedthrough determined by numerator degree)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false); // Default, will be updated based on numerator/denominator order
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private TransferFcn(Parameter numerator, Parameter denominator, Parameter absoluteTolerance,
                       Parameter continuousStateAttributes, Parameter realizeZeroPoleGain,
                       Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                       String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Validate parameters
        validateParameters(numerator, denominator, sampleTime);
        
        // Assign parameters
        this.numerator = Objects.requireNonNull(numerator, "Numerator parameter cannot be null");
        this.denominator = Objects.requireNonNull(denominator, "Denominator parameter cannot be null");
        this.absoluteTolerance = Objects.requireNonNull(absoluteTolerance, "Absolute tolerance parameter cannot be null");
        this.continuousStateAttributes = Objects.requireNonNull(continuousStateAttributes, "Continuous state attributes parameter cannot be null");
        this.realizeZeroPoleGain = Objects.requireNonNull(realizeZeroPoleGain, "Realize zero pole gain parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Parse transfer function coefficients
        parseTransferFunction();
        
        // Initialize states
        initializeStates();
        
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public TransferFcn(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.numerator = getParameterByName("Numerator");
        this.denominator = getParameterByName("Denominator");
        
        // Create missing SIMULINK parameters with defaults
        this.absoluteTolerance = getParameterByName("AbsoluteTolerance");
        this.continuousStateAttributes = getParameterByName("ContinuousStateAttributes");
        this.realizeZeroPoleGain = getParameterByName("RealizeZeroPoleGain");
        this.sampleTime = getParameterByName("SampleTime"); // 0 for continuous transfer function
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list
        
        // Parse transfer function coefficients (legacy method)
        parseVector();

        // Initialize states
        for (int i = 0; i < num.length; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }

        // Initialize ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedThrough));
    }    /**
     * DTO-NATIVE Constructor - Creates TransferFcn block directly from BlockDto DTO
     */
    public TransferFcn(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.numerator = getParameterByName("Numerator");
        this.denominator = getParameterByName("Denominator");
        this.absoluteTolerance = getParameterByName("AbsoluteTolerance");
        this.continuousStateAttributes = getParameterByName("ContinuousStateAttributes");
        this.realizeZeroPoleGain = getParameterByName("RealizeZeroPoleGain");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Parse transfer function coefficients (same as legacy method)
        parseVector();

        // Initialize states
        for (int i = 0; i < num.length; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }

        // Initialize ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedThrough));

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    
    // === Static Factory Method for JSON Deserialization ===
    public static TransferFcn fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter absoluteTolerance = createAbsoluteToleranceFromJSON(paramValues, blockName);
            Parameter continuousStateAttributes = createContinuousStateAttributesFromJSON(paramValues, blockName);
            Parameter realizeZeroPoleGain = createRealizeZeroPoleGainFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            TransferFcn block = new TransferFcn(numerator, denominator, absoluteTolerance,
                                              continuousStateAttributes, realizeZeroPoleGain,
                                              sampleTime, outDataType, saturateParam,
                                              blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, numerator, denominator, absoluteTolerance,
                                     continuousStateAttributes, realizeZeroPoleGain,
                                     sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create TransferFcn block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static TransferFcn create(String name, String path, String numerator, String denominator, NCSLabModel model) {
        return create(name, path, numerator, denominator, "auto", "'''", "off",
                     0.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a TransferFcn block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param numerator Numerator coefficients in descending powers of s
     * @param denominator Denominator coefficients in descending powers of s
     * @param absoluteTolerance Absolute tolerance for simulation
     * @param continuousStateAttributes Attributes for continuous states
     * @param realizeZeroPoleGain Realization method for zero-pole-gain
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return TransferFcn block instance
     */
    public static TransferFcn create(String name, String path, String numerator, String denominator,
                                   String absoluteTolerance, String continuousStateAttributes, String realizeZeroPoleGain,
                                   double sampleTime, String outDataType, boolean saturateOnOverflow,
                                   NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        TransferFcnDto dto = TransferFcnDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .numerator(com.ncslab.dto.common.TypedParameter.of(numerator))
            .denominator(com.ncslab.dto.common.TypedParameter.of(denominator))
            .absoluteTolerance(com.ncslab.dto.common.TypedParameter.of(absoluteTolerance))
            .continuousStateAttributes(com.ncslab.dto.common.TypedParameter.of(continuousStateAttributes))
            .realizeZeroPoleGain(com.ncslab.dto.common.TypedParameter.of(realizeZeroPoleGain))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow ? "on" : "off"))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid TransferFcn parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new TransferFcn(dto, model);
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter numerator, Parameter denominator, Parameter sampleTime) {
        double[] numArray = numerator.getDoubleArray();
        double[] denArray = denominator.getDoubleArray();
        
        if (numArray.length == 0) {
            throw new IllegalArgumentException("Numerator coefficients cannot be empty");
        }
        
        if (denArray.length == 0) {
            throw new IllegalArgumentException("Denominator coefficients cannot be empty");
        }
        
        // Check that first coefficient of denominator is non-zero
        if (Math.abs(denArray[0]) < 1e-15) {
            throw new IllegalArgumentException("Leading coefficient of denominator cannot be zero");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
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
    
    private static Parameter createAbsoluteToleranceFromJSON(JSONObject paramValues, String blockName) {
        String absoluteToleranceValue = paramValues.optString("AbsoluteTolerance", "auto");
        return new Parameter(null, 3, "AbsoluteTolerance", absoluteToleranceValue);
    }
    
    private static Parameter createContinuousStateAttributesFromJSON(JSONObject paramValues, String blockName) {
        String continuousStateAttributesValue = paramValues.optString("ContinuousStateAttributes", "'''");
        return new Parameter(null, 4, "ContinuousStateAttributes", continuousStateAttributesValue);
    }
    
    private static Parameter createRealizeZeroPoleGainFromJSON(JSONObject paramValues, String blockName) {
        String realizeZeroPoleGainValue = paramValues.optString("RealizeZeroPoleGain", "off");
        return new Parameter(null, 5, "RealizeZeroPoleGain", realizeZeroPoleGainValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
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
    
    private static void setParameterBlockReference(TransferFcn block, Parameter... parameters) {
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
        identity.put("blockType", "TransferFcn");
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
        
        // Main output port (feedthrough depends on transfer function order)
        output = new OutputPort(this, 1, feedThrough);
        outputPortList.add(output);
    }
    
    // === State Initialization ===
    private void initializeStates() {
        for (int i = 0; i < num.length; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }
    }
    
    // === Transfer Function Parsing (New Method) ===
    private void parseTransferFunction() {
        // Fail fast - validate parameters exist
        if (numerator == null) {
            throw new BlockCreationException("TransferFcn block requires numerator parameter");
        }
        if (denominator == null) {
            throw new BlockCreationException("TransferFcn block requires denominator parameter");
        }
        
        num = numerator.getDoubleArray();
        den = denominator.getDoubleArray();
        
        // Fail fast - validate arrays exist and are non-empty
        if (num == null || num.length == 0) {
            throw new BlockCreationException("TransferFcn block numerator coefficients cannot be null or empty");
        }
        if (den == null || den.length == 0) {
            throw new BlockCreationException("TransferFcn block denominator coefficients cannot be null or empty");
        }

        // Normalize by leading coefficient of denominator
        double unit = den[0];
        if (Math.abs(unit) < 1e-15) {
            throw new BlockCreationException("TransferFcn block leading coefficient of denominator cannot be zero");
        }
        
        for (int i = 0; i < den.length; i++) {
            den[i] = den[i] / unit;
        }

        for (int i = 0; i < num.length; i++) {
            num[i] = num[i] / unit;
        }

        // Check for direct feedthrough (same order in numerator and denominator)
        if (num.length == den.length) {
            feedThrough = true;
            D = num[0] / den[0];

            // Remove direct feedthrough from numerator
            for (int i = 0; i < num.length; i++) {
                num[i] = num[i] - D * den[i];
            }

            // Reduce order of numerator
            if (num.length > 1) {
                double[] numShort = new double[num.length - 1];
                System.arraycopy(num, 1, numShort, 0, num.length - 1);
                num = numShort;
            } else {
                num = new double[0]; // Empty array if single element
            }
        }

        // Remove leading coefficient from denominator (it's now 1)
        if (den.length > 1) {
            double[] denShort = new double[den.length - 1];
            System.arraycopy(den, 1, denShort, 0, den.length - 1);
            den = denShort;
        } else {
            den = new double[0]; // Empty array if single element
        }

        // Pad numerator with leading zeros if necessary
        if (den.length > 0) {
            double[] numShort = new double[den.length];
            for (int i = 0; i < den.length; i++) {
                if (i < den.length - num.length) {
                    numShort[i] = 0;
                } else {
                    int srcIndex = i - (den.length - num.length);
                    if (srcIndex >= 0 && srcIndex < num.length) {
                        numShort[i] = num[srcIndex];
                    } else {
                        numShort[i] = 0; // Should not happen with proper bounds
                    }
                }
            }
            num = numShort;
        }
    }

    private void parseVector() {
        // Legacy method - uses new parameter objects
        
        // Fail fast - validate parameters exist
        if (numerator == null || denominator == null) {
            throw new BlockCreationException("TransferFcn block requires both numerator and denominator parameters");
        }
        
        // num can be a scalar or a vector
        num = new double[1];
        if(numerator.getDataType() == DataType.REAL){
            num[0] = numerator.getValue();
        }else{
            num = numerator.getDoubleArray();
        }               

        den = new double[1];
        if(denominator.getDataType() == DataType.REAL){
            den[0] = denominator.getValue();
        }else{
            den = denominator.getDoubleArray();
        }
        
        // Fail fast - validate arrays exist and are non-empty
        if (num == null || num.length == 0) {
            throw new BlockCreationException("TransferFcn block numerator coefficients cannot be null or empty");
        }
        if (den == null || den.length == 0) {
            throw new BlockCreationException("TransferFcn block denominator coefficients cannot be null or empty");
        }

        // Normalize
        double unit = den[0];
        if (Math.abs(unit) < 1e-15) {
            throw new BlockCreationException("TransferFcn block leading coefficient of denominator cannot be zero");
        }
        
        for (int i = 0; i < den.length; i++) {
            den[i] = den[i] / unit;
        }

        for (int i = 0; i < num.length; i++) {
            num[i] = num[i] / unit;
        }

        if (num.length == den.length) {
            feedThrough = true;
            D = num[0] / den[0];

            for (int i = 0; i < num.length; i++) {
                num[i] = num[i] - D * den[i];
            }

            if (num.length > 1) {
                double[] numShort = new double[num.length - 1];
                System.arraycopy(num, 1, numShort, 0, num.length - 1);
                num = numShort;
            } else {
                num = new double[0]; // Empty array if single element
            }
        }

        if (den.length > 1) {
            double[] denShort = new double[den.length - 1];
            System.arraycopy(den, 1, denShort, 0, den.length - 1);
            den = denShort;
        } else {
            den = new double[0]; // Empty array if single element
        }

        if (den.length > 0) {
            double[] numShort = new double[den.length];
            for (int i = 0; i < den.length; i++) {
                if (i < den.length - num.length) {
                    numShort[i] = 0;
                } else {
                    int srcIndex = i - (den.length - num.length);
                    if (srcIndex >= 0 && srcIndex < num.length) {
                        numShort[i] = num[srcIndex];
                    } else {
                        numShort[i] = 0; // Should not happen with proper bounds
                    }
                }
            }
            num = numShort;
        }
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);        
        Data data = new Data(0);
        for (State state : xStateList) {
            state.setData(data);
        }
        out.setData(data);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data currentState = new Data();
        Data inputData = inputPortList.get(0).getData();

        if (feedThrough) {
            currentState = new Data(D).times(inputData);
        }

        int i=num.length-1;
        for (State xState:xStateList) {
            currentState = currentState.plus(xState.getData().times(new Data(num[i])));
            i--;
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {

        for(int i = 0; i < xStateList.size() - 1; i++){
            xStateList.get(i).setDerivateData(xStateList.get(i+1).getData());
        }

        Data derivativeData = inputPortList.get(0).getData();;
        int i=den.length-1;
        for(State xState:xStateList) {
            derivativeData = derivativeData.minus(xState.getData().times(new Data(den[i])));
            i--;
        }
        xStateList.get(xStateList.size() - 1).setDerivateData(derivativeData);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);

        String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);
        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
        context.put("feedThrough", feedThrough);
        context.put("D", D);

        String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);
        context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));

        String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Fail fast - validate required states exist
        context.put("states", xStateList);
        
        // Add individual state names for easy template access
        for (int i = 0; i < xStateList.size(); i++) {
            State state = xStateList.get(i);
            if (state == null) {
                throw new BlockCreationException("TransferFcn block state " + i + " cannot be null");
            }
            context.put("stateName" + i, state.getName());
            context.put("stateDerivativeName" + i, state.getDerivativeName());
        }
        
        if (!xStateList.isEmpty()) {
            State firstState = xStateList.get(0);
            context.put("stateName", context.get(firstState.getLocalName())); // Use state local name mapped by TemplateUtils // C variable name // For single state access
        }

        String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Fail fast - validate required states exist
        context.put("states", xStateList);
        
        // Fail fast - validate numerator array exists
        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
        
        context.put("feedThrough", feedThrough);
        context.put("D", D);
        
        // Add individual state names for easy template access
        for (int i = 0; i < xStateList.size(); i++) {
            State state = xStateList.get(i);
            if (state == null) {
                throw new BlockCreationException("TransferFcn block state " + i + " cannot be null");
            }
            context.put("stateName" + i, state.getName());
            context.put("stateDerivativeName" + i, state.getDerivativeName());
        }
        
        if (!xStateList.isEmpty()) {
            State firstState = xStateList.get(0);
            context.put("stateName", context.get(firstState.getLocalName())); // Use state local name mapped by TemplateUtils // C variable name // For single state access
        }

        String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Fail fast - validate required states exist
        context.put("states", xStateList);
        
        // Fail fast - validate denominator array exists
        context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));
        
        // Add individual state names for easy template access
        for (int i = 0; i < xStateList.size(); i++) {
            State state = xStateList.get(i);
            if (state == null) {
                throw new BlockCreationException("TransferFcn block state " + i + " cannot be null");
            }
            context.put("stateName" + i, state.getName());
            context.put("stateDerivativeName" + i, state.getDerivativeName());
        }
        
        if (!xStateList.isEmpty()) {
            State firstState = xStateList.get(0);
            context.put("stateName", context.get(firstState.getLocalName())); // Use state local name mapped by TemplateUtils // C variable name // For single state access
        }

        String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
