package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.block.math.MathBlock;
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
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Complex to Magnitude-Angle block with SIMULINK-compatible parameters.
 *
 * Converts complex numbers from rectangular form (real + imaginary) to polar form (magnitude + angle).
 *
 * SIMULINK Parameters:
 * - AngleUnits: Output angle units ("radians" or "degrees")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Mathematical Behavior:
 * - magnitude = sqrt(real^2 + imag^2)
 * - angle = atan2(imag, real)
 * - Supports both scalar and matrix inputs (element-wise)
 *
 * Inputs:
 * - Input 1 (Re): Real part of complex number
 * - Input 2 (Im): Imaginary part of complex number
 *
 * Outputs:
 * - Output 1 (Mag): Magnitude (amplitude)
 * - Output 2 (Angle): Phase angle (in radians or degrees)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class ComplexToMagnitudeAngle extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter angleUnits;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("AngleUnits", "radians");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");

        // Port names
        outputNames.add("Mag");  // Magnitude output
        outputNames.add("Angle"); // Angle output
        inputNames.add("Re");    // Real part input
        inputNames.add("Im");    // Imaginary part input

        // Input port defaults (real and imaginary inputs)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> inputRe = new HashMap<>();
        inputRe.put("name", "Re");
        inputRe.put("width", 1);
        inputRe.put("height", 1);
        inputRe.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(inputRe);

        Map<String, Object> inputIm = new HashMap<>();
        inputIm.put("name", "Im");
        inputIm.put("width", 1);
        inputIm.put("height", 1);
        inputIm.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(inputIm);

        // Output port defaults (magnitude and angle outputs with feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> outputMag = new HashMap<>();
        outputMag.put("name", "Mag");
        outputMag.put("width", 1);
        outputMag.put("height", 1);
        outputMag.put("dataType", "REAL");
        outputMag.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(outputMag);

        Map<String, Object> outputAngle = new HashMap<>();
        outputAngle.put("name", "Angle");
        outputAngle.put("width", 1);
        outputAngle.put("height", 1);
        outputAngle.put("dataType", "REAL");
        outputAngle.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(outputAngle);
    }

    // === Private Constructor with Typed Parameters ===
    private ComplexToMagnitudeAngle(Parameter angleUnits, Parameter sampleTime, Parameter outDataType,
                Parameter saturateOnIntegerOverflow, String blockName, String blockPath,
                String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(angleUnits, sampleTime);

        // Assign parameters
        this.angleUnits = Objects.requireNonNull(angleUnits, "AngleUnits parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.angleUnits);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public ComplexToMagnitudeAngle(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create parameters from JSON with defaults
        this.angleUnits = getParameterByName("AngleUnits");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Initialize ports
        initializePorts();
    }

    // === Static Factory Method for JSON Deserialization ===
    public static ComplexToMagnitudeAngle fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter angleUnits = createAngleUnitsFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            ComplexToMagnitudeAngle block = new ComplexToMagnitudeAngle(angleUnits, sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, angleUnits, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create ComplexToMagnitudeAngle block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static ComplexToMagnitudeAngle create(String name, String path, NCSLabModel model) {
        return create(name, path, "radians", -1.0, "Inherit: Same as input", false, model);
    }

    public static ComplexToMagnitudeAngle create(String name, String path, String angleUnits, double sampleTime,
                             String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter angleUnitsParam = new Parameter(null, 1, "AngleUnits", angleUnits);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        ComplexToMagnitudeAngle block = new ComplexToMagnitudeAngle(angleUnitsParam, sampleTimeParam, outDataTypeParam, saturateParam,
                             name, path, "null", model);

        setParameterBlockReference(block, angleUnitsParam, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter angleUnits, Parameter sampleTime) {
        String angleUnitsValue = angleUnits.getInitString();
        if (angleUnitsValue == null || angleUnitsValue.trim().isEmpty()) {
            throw new IllegalArgumentException("AngleUnits parameter cannot be empty");
        }

        // Validate angle units
        if (!angleUnitsValue.equals("radians") && !angleUnitsValue.equals("degrees")) {
            throw new IllegalArgumentException("AngleUnits must be 'radians' or 'degrees'");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createAngleUnitsFromJSON(JSONObject paramValues, String blockName) {
        String angleUnitsValue = paramValues.optString("AngleUnits", "radians");
        return new Parameter(null, 1, "AngleUnits", angleUnitsValue);
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

    private static void setParameterBlockReference(ComplexToMagnitudeAngle block, Parameter... parameters) {
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
        identity.put("blockType", "ComplexToMagnitudeAngle");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Two input ports: Real and Imaginary
        inputPortList.add(new InputPort(this, 1)); // Re
        inputPortList.add(new InputPort(this, 2)); // Im

        // Two output ports: Magnitude and Angle (both with feedthrough)
        outputPortList.add(new OutputPort(this, 1, true)); // Magnitude
        outputPortList.add(new OutputPort(this, 2, true)); // Angle
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("angleUnits", angleUnits);

        // Add input/output variables for template
        if (inputPortList != null && inputPortList.size() >= 2 &&
            inputPortList.get(0).getLinkedLine() != null &&
            inputPortList.get(0).getLinkedLine().getLinkedOutputPort() != null &&
            inputPortList.get(1).getLinkedLine() != null &&
            inputPortList.get(1).getLinkedLine().getLinkedOutputPort() != null) {

            String inputReVar = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
            String inputImVar = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
            context.put("inputReVar", inputReVar);
            context.put("inputImVar", inputImVar);

            // Get dimensions from first input (real part)
            OutputSignal signalRe = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("inputHeight", signalRe.getHeight());
            context.put("inputWidth", signalRe.getWidth());
        }

        if (outputPortList != null && outputPortList.size() >= 2) {
            String outputMagVar = outputPortList.get(0).getOutputSignalC().getName();
            String outputAngleVar = outputPortList.get(1).getOutputSignalC().getName();
            context.put("outputMagVar", outputMagVar);
            context.put("outputAngleVar", outputAngleVar);
        }

        // Add angle units flag for template
        boolean useDegrees = "degrees".equals(angleUnits.getInitString());
        context.put("useDegrees", useDegrees);

        String codeStr = TemplateManager.renderTemplate("c/math/ComplexToMagnitudeAngle/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // Validate that both inputs have the same dimensions
        OutputSignal signalRe = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signalIm = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signalRe.getHeight() != signalIm.getHeight() || signalRe.getWidth() != signalIm.getWidth()) {
            throw new MatDimException("ComplexToMagnitudeAngle block " + this.blockName +
                ": Input dimensions must match! Real part [" + signalRe.getHeight() + "x" + signalRe.getWidth() +
                "] vs Imaginary part [" + signalIm.getHeight() + "x" + signalIm.getWidth() + "]");
        }

        // Both outputs have same dimensions as inputs
        OutputPort outMag = outputPortList.get(0);
        OutputPort outAngle = outputPortList.get(1);

        outMag.setHeight(signalRe.getHeight());
        outMag.setWidth(signalRe.getWidth());
        outMag.getOutputSignalC().setHeight(signalRe.getHeight());
        outMag.getOutputSignalC().setWidth(signalRe.getWidth());
        outMag.getOutputSignalC().setDataType(signalRe.getDataType());

        outAngle.setHeight(signalRe.getHeight());
        outAngle.setWidth(signalRe.getWidth());
        outAngle.getOutputSignalC().setHeight(signalRe.getHeight());
        outAngle.getOutputSignalC().setWidth(signalRe.getWidth());
        outAngle.getOutputSignalC().setDataType(DataType.REAL);
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateInit() {
        // Initialization logic for ComplexToMagnitudeAngle block
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK ComplexToMagnitudeAngle block: converts rectangular to polar coordinates
        Data inputReData = inputPortList.get(0).getData();
        Data inputImData = inputPortList.get(1).getData();
        String angleUnitsValue = angleUnits.getInitString();
        boolean useDegrees = "degrees".equals(angleUnitsValue);

        Data outputMagData;
        Data outputAngleData;

        if (inputReData.getDataType() == DataType.MATRIX && inputImData.getDataType() == DataType.MATRIX) {
            // Matrix inputs - compute magnitude and angle element-wise
            Jama.Matrix reMatrix = inputReData.getMatrix();
            Jama.Matrix imMatrix = inputImData.getMatrix();
            Jama.Matrix magMatrix = new Jama.Matrix(reMatrix.getRowDimension(), reMatrix.getColumnDimension());
            Jama.Matrix angleMatrix = new Jama.Matrix(reMatrix.getRowDimension(), reMatrix.getColumnDimension());

            for (int i = 0; i < reMatrix.getRowDimension(); i++) {
                for (int j = 0; j < reMatrix.getColumnDimension(); j++) {
                    double re = reMatrix.get(i, j);
                    double im = imMatrix.get(i, j);

                    // Magnitude = sqrt(re^2 + im^2)
                    double magnitude = Math.sqrt(re * re + im * im);

                    // Angle = atan2(im, re)
                    double angle = Math.atan2(im, re);

                    // Convert to degrees if needed
                    if (useDegrees) {
                        angle = Math.toDegrees(angle);
                    }

                    magMatrix.set(i, j, magnitude);
                    angleMatrix.set(i, j, angle);
                }
            }
            outputMagData = new Data(magMatrix);
            outputAngleData = new Data(angleMatrix);

        } else if (inputReData.getDataType() == DataType.MATRIX) {
            // Real part is matrix, imaginary is scalar - broadcast scalar
            Jama.Matrix reMatrix = inputReData.getMatrix();
            double imScalar = inputImData.getInitValue();
            Jama.Matrix magMatrix = new Jama.Matrix(reMatrix.getRowDimension(), reMatrix.getColumnDimension());
            Jama.Matrix angleMatrix = new Jama.Matrix(reMatrix.getRowDimension(), reMatrix.getColumnDimension());

            for (int i = 0; i < reMatrix.getRowDimension(); i++) {
                for (int j = 0; j < reMatrix.getColumnDimension(); j++) {
                    double re = reMatrix.get(i, j);
                    double magnitude = Math.sqrt(re * re + imScalar * imScalar);
                    double angle = Math.atan2(imScalar, re);
                    if (useDegrees) {
                        angle = Math.toDegrees(angle);
                    }
                    magMatrix.set(i, j, magnitude);
                    angleMatrix.set(i, j, angle);
                }
            }
            outputMagData = new Data(magMatrix);
            outputAngleData = new Data(angleMatrix);

        } else if (inputImData.getDataType() == DataType.MATRIX) {
            // Imaginary part is matrix, real is scalar - broadcast scalar
            double reScalar = inputReData.getInitValue();
            Jama.Matrix imMatrix = inputImData.getMatrix();
            Jama.Matrix magMatrix = new Jama.Matrix(imMatrix.getRowDimension(), imMatrix.getColumnDimension());
            Jama.Matrix angleMatrix = new Jama.Matrix(imMatrix.getRowDimension(), imMatrix.getColumnDimension());

            for (int i = 0; i < imMatrix.getRowDimension(); i++) {
                for (int j = 0; j < imMatrix.getColumnDimension(); j++) {
                    double im = imMatrix.get(i, j);
                    double magnitude = Math.sqrt(reScalar * reScalar + im * im);
                    double angle = Math.atan2(im, reScalar);
                    if (useDegrees) {
                        angle = Math.toDegrees(angle);
                    }
                    magMatrix.set(i, j, magnitude);
                    angleMatrix.set(i, j, angle);
                }
            }
            outputMagData = new Data(magMatrix);
            outputAngleData = new Data(angleMatrix);

        } else {
            // Both scalars
            double re = inputReData.getInitValue();
            double im = inputImData.getInitValue();

            // Magnitude = sqrt(re^2 + im^2)
            double magnitude = Math.sqrt(re * re + im * im);

            // Angle = atan2(im, re)
            double angle = Math.atan2(im, re);

            // Convert to degrees if needed
            if (useDegrees) {
                angle = Math.toDegrees(angle);
            }

            outputMagData = new Data(1, 1);
            outputMagData.setInitValue(magnitude);

            outputAngleData = new Data(1, 1);
            outputAngleData.setInitValue(angle);
        }

        outputPortList.get(0).setData(outputMagData);
        outputPortList.get(1).setData(outputAngleData);
    }

    /**
     * Get static input names for block definition
     */
    public static List<String> getInputNames() {
        return inputNames;
    }

    /**
     * Get static output names for block definition
     */
    public static List<String> getOutputNames() {
        return outputNames;
    }
}
