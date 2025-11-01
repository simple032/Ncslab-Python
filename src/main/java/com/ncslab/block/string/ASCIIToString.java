package com.ncslab.block.string;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.ASCIIToStringDto;
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
 * ASCII To String block - converts uint8 vector to string signal.
 *
 * This block converts ASCII byte values to alphanumeric characters, concatenating
 * them into a single string output. Control characters (0x00-0x1F and 0x7F-0x9F)
 * are escaped as octal sequences.
 *
 * Example: [72, 101, 108, 108, 111] → "Hello"
 *
 * SIMULINK Parameters: None
 *
 * Port Configuration:
 * - Input 1: uint8 vector (1D or 2D) - MatrixU8 or Matrix with UINT8 CDataType
 * - Output 1: String signal (scalar) - stored as String in Java, std::string in C++
 *
 * Special Character Handling:
 * - Control characters (0x00-0x1F): Escaped as octal (e.g., \000, \001)
 * - Extended control characters (0x7F-0x9F): Escaped as octal (e.g., \177, \200)
 * - Printable characters (0x20-0x7E, 0xA0-0xFF): Direct character conversion
 *
 * @author NCSLab Team
 * @version 2025
 */
public class ASCIIToString extends Block {

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters for ASCIIToString - simple conversion
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        inputNames.add("in1");
        outputNames.add("out1");

        // Input port defaults - uint8 vector
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "MATRIX");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults - string scalar (feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL"); // String is treated as scalar
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    /**
     * Legacy constructor from JSONObject
     */
    public ASCIIToString(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Single input port for uint8 vector
        inputPortList.add(new InputPort(this, 1));

        // Single output port for string scalar (has feedthrough)
        outputPortList.add(new OutputPort(this, 1, true));
    }

    /**
     * DTO constructor
     */
    public ASCIIToString(ASCIIToStringDto dto, NCSLabModel model) {
        super(dto, model);

        // Single input port for uint8 vector
        inputPortList.add(new InputPort(this, 1));

        // Single output port for string scalar
        outputPortList.add(new OutputPort(this, 1, true));
    }

    /**
     * Generic DTO constructor for factory compatibility
     */
    public ASCIIToString(BlockDto blockDto, NCSLabModel model) {
        this((ASCIIToStringDto) blockDto, model);
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Output is always scalar string regardless of input dimensions
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            // String is always scalar (1x1)
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);

            // String is treated as scalar data type
            out.getOutputSignalC().setDataType(DataType.REAL);

            // Note: For future string data type support, could use a STRING CDataType
            // For now, strings are handled as special scalar values
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Input should be uint8 vector, but we accept any numeric input
        // No strict dimension constraints
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Initialize string output to empty
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            context.put("outputSignal", out.getOutputSignalC().getName());
        }

        String codeStr = TemplateManager.renderTemplate("c/string/ASCIIToString/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add ASCIIToString-specific context
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                context.put("inputSignal", inputSignal.getName());
                context.put("outputSignal", out.getOutputSignalC().getName());
                context.put("inputHeight", inputSignal.getHeight());
                context.put("inputWidth", inputSignal.getWidth());

                // Add input CDataType information
                CDataType inputCType = inputSignal.getCDataType();
                context.put("inputCDataType", inputCType);
                context.put("inputMatrixType", inputCType.getMatrixTypeName());
            }
        }

        String codeStr = TemplateManager.renderTemplate("c/string/ASCIIToString/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add ASCIIToString-specific context for MATLAB
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                context.put("inputSignal", inputSignal.getName());
                context.put("outputSignal", out.getOutputSignalC().getName());
            }
        }

        String codeStr = TemplateManager.renderTemplate("m/string/ASCIIToString/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateOutput(double t) {
        // Java simulation: convert uint8 vector to string
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getData() != null) {
                String outputString = convertASCIIToString(in.getData());

                // Store string in Data object (using initString field)
                Data stringData = new Data(0.0); // Scalar placeholder
                stringData.setInitString(outputString);
                out.setData(stringData);
            } else {
                // No input data, output empty string
                Data emptyString = new Data(0.0);
                emptyString.setInitString("");
                out.setData(emptyString);
            }
        }
    }

    @Override
    public void calculateInit() {
        // Initialize with empty string output
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            Data emptyString = new Data(0.0);
            emptyString.setInitString("");
            out.setData(emptyString);
        }
    }

    /**
     * Converts uint8 vector (stored as Data object) to string.
     * Handles control characters by escaping them as octal sequences.
     *
     * @param input Data object containing uint8 vector values
     * @return Converted string with control characters escaped
     */
    private String convertASCIIToString(Data input) {
        StringBuilder result = new StringBuilder();

        if (input.getDataType() == DataType.REAL) {
            // Single scalar value
            int asciiValue = (int) input.getInitValue();
            appendCharacter(result, asciiValue);
        } else if (input.getDataType() == DataType.MATRIX) {
            // Matrix of uint8 values
            Matrix matrix = input.getMatrix();
            int height = matrix.getRowDimension();
            int width = matrix.getColumnDimension();

            // Process matrix in row-major order
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int asciiValue = (int) matrix.get(i, j);
                    appendCharacter(result, asciiValue);
                }
            }
        }

        return result.toString();
    }

    /**
     * Appends a single character to the string builder, escaping control characters.
     *
     * Control characters (0x00-0x1F and 0x7F-0x9F) are escaped as octal sequences.
     * Printable characters are appended directly.
     *
     * @param builder StringBuilder to append to
     * @param asciiValue ASCII value (0-255)
     */
    private void appendCharacter(StringBuilder builder, int asciiValue) {
        // Ensure value is in valid byte range
        asciiValue = asciiValue & 0xFF;

        // Check if character should be escaped
        if (shouldEscapeCharacter(asciiValue)) {
            // Escape as octal: \000 to \377
            builder.append(String.format("\\%03o", asciiValue));
        } else {
            // Append printable character directly
            builder.append((char) asciiValue);
        }
    }

    /**
     * Determines if a character should be escaped as octal.
     *
     * Characters in Unicode range 0x00-0x1F (control characters) and
     * 0x7F-0x9F (extended control characters) should be escaped.
     *
     * @param asciiValue ASCII value (0-255)
     * @return true if character should be escaped, false otherwise
     */
    private boolean shouldEscapeCharacter(int asciiValue) {
        // Control characters: 0x00-0x1F (0-31)
        if (asciiValue >= 0x00 && asciiValue <= 0x1F) {
            return true;
        }

        // DEL and extended control characters: 0x7F-0x9F (127-159)
        if (asciiValue >= 0x7F && asciiValue <= 0x9F) {
            return true;
        }

        // Printable characters: 0x20-0x7E, 0xA0-0xFF
        return false;
    }
}
