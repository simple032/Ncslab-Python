package com.ncslab.block.instrument;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.data.DataTypeConversionDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import org.json.JSONObject;
import Jama.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Type Conversion block - converts input signals from one data type to another,
 * with optional rounding methods for numeric conversions.
 *
 * SIMULINK Parameters:
 * - OutDataTypeStr: Output data type specification (e.g., "int16", "double")
 * - RndMeth: Rounding method for conversion (e.g., "Round", "Floor", "Ceiling")
 */
public class DataTypeConversion extends Block {
    private final Parameter outDataTypeStr;
    private final Parameter rndMeth;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("RndMeth", "Floor");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");

        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (data type conversion has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    /**
     * Legacy constructor from JSONObject
     */
    public DataTypeConversion(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        this.outDataTypeStr = getParameterByName("OutDataTypeStr");
        this.rndMeth = getParameterByName("RndMeth");

        // Single input and output
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    /**
     * DTO constructor
     */
    public DataTypeConversion(DataTypeConversionDto dto, NCSLabModel model) {
        super(dto, model);

        this.outDataTypeStr = getParameterByName("OutDataTypeStr");
        this.rndMeth = getParameterByName("RndMeth");

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Output dimensions match input dimensions
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null) {
                com.ncslab.block.io.OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                out.setHeight(inputSignal.getHeight());
                out.setWidth(inputSignal.getWidth());
                out.getOutputSignalC().setHeight(inputSignal.getHeight());
                out.getOutputSignalC().setWidth(inputSignal.getWidth());

                // Set dimension type (REAL for scalar, MATRIX for vector/matrix)
                if (inputSignal.getHeight() > 1 || inputSignal.getWidth() > 1) {
                    out.getOutputSignalC().setDataType(DataType.MATRIX);
                } else {
                    out.getOutputSignalC().setDataType(DataType.REAL);
                }

                // Set actual C data type based on OutDataTypeStr parameter
                CDataType outputCType = parseOutputDataType();
                out.getOutputSignalC().setCDataType(outputCType);
            }
        }
    }

    /**
     * Parse OutDataTypeStr parameter to determine C data type
     */
    private CDataType parseOutputDataType() {
        if (outDataTypeStr == null) {
            return CDataType.DOUBLE;
        }

        String typeStr = outDataTypeStr.getInitString();
        return CDataType.fromString(typeStr);
    }

    @Override
    public void checkDimension() throws MatDimException {
        // No specific dimension checks
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        // Data type conversion typically doesn't require initialization
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add DataTypeConversion-specific context
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
            context.put("outputSignal", out.getOutputSignalC().getName());
            context.put("inputSignal", ops.getOutputSignalC().getName());
            context.put("roundingMethod", getRoundingMethod());
        }

        String codeStr = TemplateManager.renderTemplate("c/instrument/DataTypeConversion/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add DataTypeConversion-specific context
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
            context.put("outputSignal", out.getOutputSignalC().getName());
            context.put("inputSignal", ops.getOutputSignalC().getName());
            context.put("roundingMethod", getRoundingMethod());
        }

        String codeStr = TemplateManager.renderTemplate("m/instrument/DataTypeConversion/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateOutput(double t) {
        // Java simulation: convert input data type to output type with rounding
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getData() != null) {
                Data inputData = in.getData();
                Data convertedData = applyRounding(inputData);
                out.setData(convertedData);
            }
        }
    }

    @Override
    public void calculateInit() {
        // Initialize data type conversion
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getData() != null) {
                Data convertedData = applyRounding(in.getData());
                out.setData(convertedData);
            } else {
                out.setData(new Data(0.0));
            }
        }
    }

    /**
     * Get rounding method string for code generation
     */
    private String getRoundingMethod() {
        if (rndMeth != null) {
            return rndMeth.getInitString();
        }
        return "Floor";
    }

    /**
     * Apply rounding to data based on rounding method
     */
    private Data applyRounding(Data input) {
        String method = getRoundingMethod();

        if (input.getDataType() == DataType.REAL) {
            double value = input.getInitValue();
            double rounded = applyRoundingToValue(value, method);
            return new Data(rounded);
        } else if (input.getDataType() == DataType.MATRIX) {
            Matrix inputMatrix = input.getMatrix();
            Matrix roundedMatrix = new Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    roundedMatrix.set(i, j, applyRoundingToValue(value, method));
                }
            }

            return new Data(roundedMatrix);
        }

        return input;
    }

    /**
     * Apply rounding method to a single value
     */
    private double applyRoundingToValue(double value, String method) {
        switch (method) {
            case "Round":
            case "Nearest":
                return Math.round(value);
            case "Floor":
                return Math.floor(value);
            case "Ceiling":
            case "Ceil":
                return Math.ceil(value);
            case "Zero":
            case "Truncate":
                return (value >= 0) ? Math.floor(value) : Math.ceil(value);
            default:
                return Math.floor(value); // Default to floor
        }
    }
}
