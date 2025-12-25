package com.ncslab.block.continuous;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.continuous.ZeroPoleDto;

import java.util.ArrayList;
import java.util.List;
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
 * ZeroPole block - Transfer function in zero-pole-gain form.
 *
 * SIMULINK Parameters:
 * - Zeros: Zero locations [z1, z2, ...]
 * - Poles: Pole locations [p1, p2, ...]
 * - Gain: System gain K
 * - AbsoluteTolerance: Absolute tolerance for simulation
 * - ContinuousStateAttributes: Attributes for continuous states
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Transfer Function: H(s) = K * prod(s - z_i) / prod(s - p_i)
 * Internally converted to transfer function form for state-space realization.
 */
public class ZeroPole extends ContinuousBlock {
    // === Internal Implementation ===
    private double D = 0;
    private boolean feedThrough = false;
    private double[] num;
    private double[] den;
    private List<State> xStateList = new ArrayList<>();

    // === SIMULINK-Compatible Parameters ===
    private final Parameter zeros;
    private final Parameter poles;
    private final Parameter gain;
    private final Parameter absoluteTolerance;
    private final Parameter continuousStateAttributes;
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

        // Parameter defaults for zero-pole-gain form
        PARAMETER_DEFAULTS.put("Zeros", "[]");
        PARAMETER_DEFAULTS.put("Poles", "[-1]");
        PARAMETER_DEFAULTS.put("Gain", "1");
        PARAMETER_DEFAULTS.put("AbsoluteTolerance", "auto");
        PARAMETER_DEFAULTS.put("ContinuousStateAttributes", "'''");
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

        // Output port defaults (feedthrough determined by zero/pole count)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false); // Default, will be updated based on zpk
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private ZeroPole(Parameter zeros, Parameter poles, Parameter gain,
                    Parameter absoluteTolerance, Parameter continuousStateAttributes,
                    Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                    String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(zeros, poles, gain, sampleTime);

        // Assign parameters
        this.zeros = Objects.requireNonNull(zeros, "Zeros parameter cannot be null");
        this.poles = Objects.requireNonNull(poles, "Poles parameter cannot be null");
        this.gain = Objects.requireNonNull(gain, "Gain parameter cannot be null");
        this.absoluteTolerance = Objects.requireNonNull(absoluteTolerance, "Absolute tolerance parameter cannot be null");
        this.continuousStateAttributes = Objects.requireNonNull(continuousStateAttributes, "Continuous state attributes parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Convert zpk to transfer function
        convertZpkToTf();

        // Initialize states
        initializeStates();

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public ZeroPole(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.zeros = getParameterByName("Zeros");
        this.poles = getParameterByName("Poles");
        this.gain = getParameterByName("Gain");

        // Create missing SIMULINK parameters with defaults
        this.absoluteTolerance = getParameterByName("AbsoluteTolerance");
        this.continuousStateAttributes = getParameterByName("ContinuousStateAttributes");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Convert zpk to transfer function (legacy method)
        convertZpkToTfLegacy();

        // Initialize states
        for (int i = 0; i < num.length; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }

        // Initialize ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedThrough));
    }

    /**
     * DTO-NATIVE Constructor - Creates ZeroPole block directly from BlockDto DTO
     */
    public ZeroPole(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.zeros = getParameterByName("Zeros");
        this.poles = getParameterByName("Poles");
        this.gain = getParameterByName("Gain");
        this.absoluteTolerance = getParameterByName("AbsoluteTolerance");
        this.continuousStateAttributes = getParameterByName("ContinuousStateAttributes");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Convert zpk to transfer function (same as legacy method)
        convertZpkToTfLegacy();

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
    public static ZeroPole fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter zeros = createZerosFromJSON(paramValues, blockName);
            Parameter poles = createPolesFromJSON(paramValues, blockName);
            Parameter gain = createGainFromJSON(paramValues, blockName);
            Parameter absoluteTolerance = createAbsoluteToleranceFromJSON(paramValues, blockName);
            Parameter continuousStateAttributes = createContinuousStateAttributesFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            ZeroPole block = new ZeroPole(zeros, poles, gain, absoluteTolerance,
                                         continuousStateAttributes, sampleTime, outDataType, saturateParam,
                                         blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, zeros, poles, gain, absoluteTolerance,
                                     continuousStateAttributes, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create ZeroPole block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static ZeroPole create(String name, String path, String zeros, String poles, double gain, NCSLabModel model) {
        return create(name, path, zeros, poles, gain, "auto", "'''", 0.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a ZeroPole block with full parameters (DTO-based approach).
     */
    public static ZeroPole create(String name, String path, String zeros, String poles, double gain,
                                 String absoluteTolerance, String continuousStateAttributes,
                                 double sampleTime, String outDataType, boolean saturateOnOverflow,
                                 NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        ZeroPoleDto dto = ZeroPoleDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .zeros(com.ncslab.dto.common.TypedParameter.of(zeros))
            .poles(com.ncslab.dto.common.TypedParameter.of(poles))
            .gain(com.ncslab.dto.common.TypedParameter.of(gain))
            .absoluteTolerance(com.ncslab.dto.common.TypedParameter.of(absoluteTolerance))
            .continuousStateAttributes(com.ncslab.dto.common.TypedParameter.of(continuousStateAttributes))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow ? "on" : "off"))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid ZeroPole parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new ZeroPole(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter zeros, Parameter poles, Parameter gain, Parameter sampleTime) {
        double[] poleArray = poles.getDoubleArray();

        if (poleArray.length == 0) {
            throw new IllegalArgumentException("Poles array cannot be empty");
        }

        double gainValue = gain.getDouble();
        if (Double.isNaN(gainValue) || Double.isInfinite(gainValue)) {
            throw new IllegalArgumentException("Gain must be a finite number");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= -1.0");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createZerosFromJSON(JSONObject paramValues, String blockName) {
        String zerosValue = paramValues.optString("Zeros", "[]");
        return new Parameter(null, 1, "Zeros", zerosValue);
    }

    private static Parameter createPolesFromJSON(JSONObject paramValues, String blockName) {
        String polesValue = paramValues.optString("Poles", "[-1]");
        return new Parameter(null, 2, "Poles", polesValue);
    }

    private static Parameter createGainFromJSON(JSONObject paramValues, String blockName) {
        String gainValue = paramValues.optString("Gain", "1");
        return new Parameter(null, 3, "Gain", gainValue);
    }

    private static Parameter createAbsoluteToleranceFromJSON(JSONObject paramValues, String blockName) {
        String absoluteToleranceValue = paramValues.optString("AbsoluteTolerance", "auto");
        return new Parameter(null, 4, "AbsoluteTolerance", absoluteToleranceValue);
    }

    private static Parameter createContinuousStateAttributesFromJSON(JSONObject paramValues, String blockName) {
        String continuousStateAttributesValue = paramValues.optString("ContinuousStateAttributes", "'''");
        return new Parameter(null, 5, "ContinuousStateAttributes", continuousStateAttributesValue);
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

    private static void setParameterBlockReference(ZeroPole block, Parameter... parameters) {
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
        identity.put("blockType", "ZeroPole");
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

    // === ZPK to Transfer Function Conversion ===
    private void convertZpkToTf() {
        // Get zpk parameters
        double[] z = zeros.getDoubleArray();
        double[] p = poles.getDoubleArray();
        double k = gain.getDouble();

        // Validate
        if (p == null || p.length == 0) {
            throw new BlockCreationException("ZeroPole block poles cannot be null or empty");
        }

        // Convert zeros to polynomial coefficients
        double[] numCoeffs = zpkToPolynomial(z, k);

        // Convert poles to polynomial coefficients
        double[] denCoeffs = zpkToPolynomial(p, 1.0);

        // Store as transfer function form
        num = numCoeffs;
        den = denCoeffs;

        // Normalize by leading coefficient of denominator
        double unit = den[0];
        if (Math.abs(unit) < 1e-15) {
            throw new BlockCreationException("ZeroPole block leading coefficient of denominator cannot be zero");
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
                num = new double[0];
            }
        }

        // Remove leading coefficient from denominator (it's now 1)
        if (den.length > 1) {
            double[] denShort = new double[den.length - 1];
            System.arraycopy(den, 1, denShort, 0, den.length - 1);
            den = denShort;
        } else {
            den = new double[0];
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
                        numShort[i] = 0;
                    }
                }
            }
            num = numShort;
        }
    }

    private void convertZpkToTfLegacy() {
        // Legacy method - uses Parameter objects
        if (zeros == null || poles == null || gain == null) {
            throw new BlockCreationException("ZeroPole block requires zeros, poles, and gain parameters");
        }

        // Get zpk parameters
        double[] z = zeros.getDoubleArray();
        double[] p = poles.getDoubleArray();

        // Handle scalar gain
        double k = 1.0;
        if (gain.getDataType() == DataType.REAL) {
            k = gain.getValue();
        } else {
            double[] gainArray = gain.getDoubleArray();
            if (gainArray != null && gainArray.length > 0) {
                k = gainArray[0];
            }
        }

        // Handle empty zeros
        if (z == null) {
            z = new double[0];
        }

        // Validate poles
        if (p == null || p.length == 0) {
            throw new BlockCreationException("ZeroPole block poles cannot be null or empty");
        }

        // Convert zeros to polynomial coefficients
        double[] numCoeffs = zpkToPolynomial(z, k);

        // Convert poles to polynomial coefficients
        double[] denCoeffs = zpkToPolynomial(p, 1.0);

        // Store as transfer function form
        num = numCoeffs;
        den = denCoeffs;

        // Normalize by leading coefficient of denominator
        double unit = den[0];
        if (Math.abs(unit) < 1e-15) {
            throw new BlockCreationException("ZeroPole block leading coefficient of denominator cannot be zero");
        }

        for (int i = 0; i < den.length; i++) {
            den[i] = den[i] / unit;
        }

        for (int i = 0; i < num.length; i++) {
            num[i] = num[i] / unit;
        }

        // Check for direct feedthrough
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
                num = new double[0];
            }
        }

        if (den.length > 1) {
            double[] denShort = new double[den.length - 1];
            System.arraycopy(den, 1, denShort, 0, den.length - 1);
            den = denShort;
        } else {
            den = new double[0];
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
                        numShort[i] = 0;
                    }
                }
            }
            num = numShort;
        }
    }

    /**
     * Convert zeros/poles to polynomial coefficients.
     * For roots [r1, r2, ..., rn], computes coefficients of (s-r1)(s-r2)...(s-rn)
     * multiplied by gain k.
     */
    private double[] zpkToPolynomial(double[] roots, double k) {
        if (roots == null || roots.length == 0) {
            return new double[]{k};
        }

        // Start with (s - r0)
        double[] poly = new double[]{1, -roots[0]};

        // Multiply by (s - ri) for each remaining root
        for (int i = 1; i < roots.length; i++) {
            poly = multiplyPolynomial(poly, new double[]{1, -roots[i]});
        }

        // Multiply by gain
        for (int i = 0; i < poly.length; i++) {
            poly[i] *= k;
        }

        return poly;
    }

    /**
     * Multiply two polynomials.
     */
    private double[] multiplyPolynomial(double[] a, double[] b) {
        double[] result = new double[a.length + b.length - 1];

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                result[i + j] += a[i] * b[j];
            }
        }

        return result;
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

        int i = num.length - 1;
        for (State xState : xStateList) {
            currentState = currentState.plus(xState.getData().times(new Data(num[i])));
            i--;
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {

        for (int i = 0; i < xStateList.size() - 1; i++) {
            xStateList.get(i).setDerivateData(xStateList.get(i + 1).getData());
        }

        Data derivativeData = inputPortList.get(0).getData();
        int i = den.length - 1;
        for (State xState : xStateList) {
            derivativeData = derivativeData.minus(xState.getData().times(new Data(den[i])));
            i--;
        }
        xStateList.get(xStateList.size() - 1).setDerivateData(derivativeData);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);

        String codeStr = TemplateManager.renderTemplate("m/continuous/ZeroPole/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);
        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
        context.put("feedThrough", feedThrough);
        context.put("D", D);

        String codeStr = TemplateManager.renderTemplate("m/continuous/ZeroPole/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("states", xStateList);
        context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));

        String codeStr = TemplateManager.renderTemplate("m/continuous/ZeroPole/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("states", xStateList);

        // Add individual state names for easy template access
        for (int i = 0; i < xStateList.size(); i++) {
            State state = xStateList.get(i);
            if (state == null) {
                throw new BlockCreationException("ZeroPole block state " + i + " cannot be null");
            }
            context.put("stateName" + i, state.getName());
            context.put("stateDerivativeName" + i, state.getDerivativeName());
        }

        if (!xStateList.isEmpty()) {
            State firstState = xStateList.get(0);
            context.put("stateName", context.get(firstState.getLocalName()));
        }

        String codeStr = TemplateManager.renderTemplate("c/continuous/ZeroPole/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("states", xStateList);
        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
        context.put("feedThrough", feedThrough);
        context.put("D", D);

        // Add individual state names for easy template access
        for (int i = 0; i < xStateList.size(); i++) {
            State state = xStateList.get(i);
            if (state == null) {
                throw new BlockCreationException("ZeroPole block state " + i + " cannot be null");
            }
            context.put("stateName" + i, state.getName());
            context.put("stateDerivativeName" + i, state.getDerivativeName());
        }

        if (!xStateList.isEmpty()) {
            State firstState = xStateList.get(0);
            context.put("stateName", context.get(firstState.getLocalName()));
        }

        String codeStr = TemplateManager.renderTemplate("c/continuous/ZeroPole/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("states", xStateList);
        context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));

        // Add individual state names for easy template access
        for (int i = 0; i < xStateList.size(); i++) {
            State state = xStateList.get(i);
            if (state == null) {
                throw new BlockCreationException("ZeroPole block state " + i + " cannot be null");
            }
            context.put("stateName" + i, state.getName());
            context.put("stateDerivativeName" + i, state.getDerivativeName());
        }

        if (!xStateList.isEmpty()) {
            State firstState = xStateList.get(0);
            context.put("stateName", context.get(firstState.getLocalName()));
        }

        String codeStr = TemplateManager.renderTemplate("c/continuous/ZeroPole/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateArraysCodeC(CodeStructC code) {
        // ZeroPole block does not require array declarations
    }
}
