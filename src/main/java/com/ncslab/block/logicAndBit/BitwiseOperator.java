package com.ncslab.block.logicAndBit;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.logic.BitwiseOperatorDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
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
 * BitwiseOperator block with SIMULINK-compatible parameters and type-safe constructors.
 * Performs bitwise operations on integer data types.
 *
 * SIMULINK Parameters:
 * - Operator: Bitwise operation to perform (AND, OR, XOR, NOT)
 * - Inputs: Number of input ports
 * - UseBitMask: Enable bit mask for selective bit operations
 * - BitMask: Hexadecimal mask value (e.g., "0xFF")
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification (uint8, uint16, uint32, int8, int16, int32)
 */
public class BitwiseOperator extends Block {
    private int num;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter operator;
    private final Parameter inputs;
    private final Parameter useBitMask;
    private final Parameter bitMask;
    private final Parameter sampleTime;
    private final Parameter outDataTypeStr;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Operator", "AND");
        PARAMETER_DEFAULTS.put("Inputs", "2");
        PARAMETER_DEFAULTS.put("UseBitMask", "off");
        PARAMETER_DEFAULTS.put("BitMask", "0xFF");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "uint32");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
        // Input names are dynamic based on number of inputs
    }

    /**
     * Legacy constructor from JSONObject
     */
    public BitwiseOperator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create parameters for backward compatibility
        this.operator = getParameterByName("Operator");
        this.inputs = getParameterByName("Inputs");
        this.useBitMask = getParameterByName("UseBitMask");
        this.bitMask = getParameterByName("BitMask");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");

        // Create ports
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        parseParamValues();
    }

    private void parseParamValues() {
        try {
            num = (int) Math.round(Double.parseDouble(inputs.getInitString()));
        } catch (NumberFormatException e) {
            num = 2; // Default to 2 inputs
        }

        for (int i = 0; i < num; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    /**
     * DTO constructor
     */
    public BitwiseOperator(BitwiseOperatorDto dto, NCSLabModel model) {
        super(dto, model);

        // Initialize parameters from DTO with null safety
        this.operator = getParameterByName("Operator");
        this.inputs = getParameterByName("Inputs");
        this.useBitMask = getParameterByName("UseBitMask");
        this.bitMask = getParameterByName("BitMask");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");

        // Parse number of inputs and create ports
        this.num = dto.getInputsValue();

        // Create ports based on DTO configuration
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        for (int i = 0; i < num; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }

    /**
     * Generic DTO constructor for factory compatibility
     */
    public BitwiseOperator(BlockDto blockDto, NCSLabModel model) {
        this((BitwiseOperatorDto) blockDto, model);
    }

    @Override
    public void calculateInit() {
        // Initialize output to zero
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            out.setData(new Data(0.0));
        }
    }

    @Override
    public void calculateOutput(double t) {
        if (inputPortList.isEmpty() || outputPortList.isEmpty()) {
            return;
        }

        OutputPort out = outputPortList.get(0);
        String op = operator.getInitString();
        Data resultData = null;

        // Get bit mask if enabled
        long maskValue = 0xFFFFFFFFL; // Default: all bits
        if ("on".equalsIgnoreCase(useBitMask.getInitString())) {
            maskValue = parseBitMask(bitMask.getInitString());
        }

        switch (out.getOutputSignalC().getDataType()) {
            case REAL:
                long firstValue = (long) inputPortList.get(0).getData().getInitValue();
                firstValue &= maskValue;

                for (int i = 1; i < num; i++) {
                    Data inputData = inputPortList.get(i).getData();
                    if (inputData.getDataType() != DataType.REAL) {
                        break;
                    }
                    long val = (long) inputData.getInitValue();
                    val &= maskValue;
                    firstValue = applyBitwiseOperator(firstValue, val, op);
                }
                resultData = new Data((double) firstValue);
                break;

            case MATRIX:
                Matrix firstMatrix = inputPortList.get(0).getData().getMatrix();
                Matrix maskedMatrix = applyMaskToMatrix(firstMatrix, maskValue);

                for (int i = 1; i < num; i++) {
                    Data inputData = inputPortList.get(i).getData();
                    if (inputData.getDataType() != DataType.MATRIX) {
                        break;
                    }
                    Matrix inputMatrix = applyMaskToMatrix(inputData.getMatrix(), maskValue);
                    maskedMatrix = applyMatrixBitwiseOperator(maskedMatrix, inputMatrix, op);
                }
                resultData = new Data(maskedMatrix);
                break;
        }

        out.setData(resultData);
    }

    /**
     * Apply bitwise operator on two long values
     */
    private long applyBitwiseOperator(long a, long b, String operator) {
        switch (operator.toUpperCase()) {
            case "AND":
                return a & b;
            case "OR":
                return a | b;
            case "XOR":
                return a ^ b;
            case "NOT":
                return ~a;
            case "NAND":
                return ~(a & b);
            case "NOR":
                return ~(a | b);
            default:
                return 0;
        }
    }

    /**
     * Apply bitwise operator element-wise on two matrices
     */
    private Matrix applyMatrixBitwiseOperator(Matrix a, Matrix b, String operator) {
        Matrix result = new Matrix(a.getRowDimension(), a.getColumnDimension());
        for (int i = 0; i < a.getRowDimension(); i++) {
            for (int j = 0; j < a.getColumnDimension(); j++) {
                long valA = (long) a.get(i, j);
                long valB = (long) b.get(i, j);
                result.set(i, j, (double) applyBitwiseOperator(valA, valB, operator));
            }
        }
        return result;
    }

    /**
     * Apply bit mask to matrix elements
     */
    private Matrix applyMaskToMatrix(Matrix matrix, long mask) {
        Matrix result = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                long val = (long) matrix.get(i, j);
                result.set(i, j, (double) (val & mask));
            }
        }
        return result;
    }

    /**
     * Parse bit mask from hex string (e.g., "0xFF", "0xFFFF")
     */
    private long parseBitMask(String maskStr) {
        try {
            if (maskStr.startsWith("0x") || maskStr.startsWith("0X")) {
                return Long.parseLong(maskStr.substring(2), 16);
            } else {
                return Long.parseLong(maskStr);
            }
        } catch (NumberFormatException e) {
            return 0xFFFFFFFFL; // Default mask
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add BitwiseOperator-specific context
        context.put("operator", operator.getInitString());
        context.put("inputLength", num);
        context.put("useBitMask", useBitMask.getInitString());
        context.put("bitMask", bitMask.getInitString());

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/BitwiseOperator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add BitwiseOperator-specific context
        context.put("operator", operator.getInitString());
        context.put("inputLength", num);
        context.put("useBitMask", useBitMask.getInitString());
        context.put("bitMask", bitMask.getInitString());

        String codeStr = TemplateManager.renderTemplate("m/logicAndBit/BitwiseOperator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (inputPortList.isEmpty() || outputPortList.isEmpty()) {
            return;
        }

        OutputPort out = outputPortList.get(0);
        OutputSignal[] signals = new OutputSignal[num];

        for (int i = 0; i < num; i++) {
            signals[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }

        // Check all inputs have same dimensions
        int height = signals[0].getHeight();
        int width = signals[0].getWidth();

        for (OutputSignal signal : signals) {
            if (signal.getHeight() != height || signal.getWidth() != width) {
                throw new MatDimException("Block " + this.blockName + " input dimensions don't match!");
            }
        }

        // Set output dimensions
        out.setHeight(height);
        out.setWidth(width);
        out.getOutputSignalC().setHeight(height);
        out.getOutputSignalC().setWidth(width);
        out.getOutputSignalC().setDataType(signals[0].getDataType());

        // Set output C data type based on parameter
        CDataType outputCType = parseOutputDataType();
        out.getOutputSignalC().setCDataType(outputCType);
    }

    /**
     * Parse OutDataTypeStr parameter to determine C data type
     */
    private CDataType parseOutputDataType() {
        if (outDataTypeStr == null) {
            return CDataType.UINT32;
        }

        String typeStr = outDataTypeStr.getInitString();
        return CDataType.fromString(typeStr);
    }

    @Override
    public void checkDimension() throws MatDimException {
        if ("NOT".equalsIgnoreCase(operator.getInitString()) && num > 1) {
            throw new MatDimException("Block " + this.blockName +
                " with NOT operator must have only one input!");
        }
    }
}
