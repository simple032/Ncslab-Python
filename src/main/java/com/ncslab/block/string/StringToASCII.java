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
import com.ncslab.dto.block.specialized.string.StringToASCIIDto;
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
 * String To ASCII conversion block - converts string signal to uint8 vector of ASCII values.
 *
 * This block converts each character in the input string to its corresponding ASCII decimal value.
 * The output is a 1D column vector (uint8 type) where each element represents the ASCII value
 * of the corresponding character in the input string.
 *
 * Example: Input string "Hello" produces output vector [72, 101, 108, 108, 111]
 *
 * SIMULINK Behavior:
 * - Input: String signal (scalar)
 * - Output: uint8 vector (1D column vector, size = length of input string)
 * - Output CDataType: UINT8
 * - Output DataType: MATRIX (1D vector)
 *
 * Parameters: None required (simple conversion)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
public class StringToASCII extends Block {

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters required for this block
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        inputNames.add("str");
        outputNames.add("ascii");

        // Input port defaults - string input
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "str");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL"); // String stored as Data with initString
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults - uint8 vector output (has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "ascii");
        output1.put("width", 1);
        output1.put("height", 1); // Will be updated based on input string length
        output1.put("dataType", "MATRIX"); // uint8 vector
        output1.put("feedthrough", true); // Direct conversion with feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    /**
     * Legacy constructor from JSONObject
     */
    public StringToASCII(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Single input (string) and single output (uint8 vector)
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    /**
     * DTO constructor
     */
    public StringToASCII(StringToASCIIDto dto, NCSLabModel model) {
        super(dto, model);

        // Single input (string) and single output (uint8 vector)
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

                // Get string data to determine length
                int stringLength = getInputStringLength();

                System.out.println("StringToASCII updateDimension: stringLength=" + stringLength +
                                 ", blockId=" + this.blockId + ", blockName=" + this.blockName);

                // Output is a uint8 column vector (stringLength x 1)
                out.setHeight(stringLength);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(stringLength);
                out.getOutputSignalC().setWidth(1);

                // Set data type to MATRIX for vector output (even if length = 1)
                out.getOutputSignalC().setDataType(stringLength > 1 ? DataType.MATRIX : DataType.REAL);

                // CRITICAL: Set output CDataType to UINT8 since output is ASCII byte array
                out.getOutputSignalC().setCDataType(CDataType.UINT8);

                System.out.println("StringToASCII after updateDimension: dataType=" + out.getOutputSignalC().getDataType() +
                                 ", cDataType=" + out.getOutputSignalC().getCDataType() +
                                 ", height=" + out.getOutputSignalC().getHeight() +
                                 ", width=" + out.getOutputSignalC().getWidth());
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // String To ASCII has no specific dimension constraints
        // Input should be string (scalar), output will be uint8 vector
    }

    @Override
    public void calculateInit() {
        // Initialize output with empty ASCII vector
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            // Convert initial string data to ASCII bytes
            if (in.getData() != null) {
                String inputString = getStringFromData(in.getData());
                Matrix asciiVector = stringToAsciiMatrix(inputString);
                out.setData(new Data(asciiVector));
            } else {
                // Default to empty ASCII vector
                Matrix asciiVector = new Matrix(1, 1);
                asciiVector.set(0, 0, 0.0);
                out.setData(new Data(asciiVector));
            }
        }
    }

    @Override
    public void calculateOutput(double t) {
        // Convert input string to ASCII byte vector
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getData() != null) {
                String inputString = getStringFromData(in.getData());
                Matrix asciiVector = stringToAsciiMatrix(inputString);
                out.setData(new Data(asciiVector));
            }
        }
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // String To ASCII typically doesn't require initialization code
        // The conversion is performed in output code
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add StringToASCII-specific context
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            OutputPort inputOps = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();

            context.put("outputSignal", out.getOutputSignalC().getName());
            context.put("inputSignal", inputOps.getOutputSignalC().getName());
            context.put("outputHeight", out.getOutputSignalC().getHeight());
            context.put("outputWidth", out.getOutputSignalC().getWidth());
            context.put("outputCDataType", out.getOutputSignalC().getCDataType());
            context.put("outputCppType", out.getOutputSignalC().getCDataType().getCppType());
        }

        String codeStr = TemplateManager.renderTemplate("c/string/StringToASCII/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add StringToASCII-specific context
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            OutputPort inputOps = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();

            context.put("outputSignal", out.getOutputSignalC().getName());
            context.put("inputSignal", inputOps.getOutputSignalC().getName());
        }

        String codeStr = TemplateManager.renderTemplate("m/string/StringToASCII/output.vm", context);
        code.addOutputCode(codeStr);
    }

    // === Helper Methods ===

    /**
     * Get the input string length for dimension calculation.
     * Returns a reasonable buffer size if string data is not yet available.
     *
     * CRITICAL: Since ComposeString output length is unknown at compile time,
     * we allocate an initial buffer size. The template will resize() the output
     * vector at runtime to match the actual string length.
     *
     * @return Length of the input string (default: 64 for dynamic strings)
     */
    private int getInputStringLength() {
        if (!inputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            if (in.getData() != null) {
                String inputString = getStringFromData(in.getData());
                if (inputString != null && !inputString.isEmpty()) {
                    return Math.max(inputString.length(), 1);
                }
            }
        }
        // Default to reasonable buffer for dynamic strings (e.g., from ComposeString)
        // The template will resize this at runtime to match actual string length
        return 64; // Reasonable default for serial communication strings
    }

    /**
     * Extract string value from Data object.
     * Handles both initString and dataString fields.
     *
     * @param data Data object containing string
     * @return String value (empty string if null)
     */
    private String getStringFromData(Data data) {
        if (data == null) {
            return "";
        }

        // Try to get string from initString field first
        String str = data.getInitString();
        if (str != null && !str.isEmpty()) {
            return str;
        }

        // Fall back to dataString field
        str = data.getDataString();
        if (str != null && !str.isEmpty()) {
            return str;
        }

        return "";
    }

    /**
     * Convert string to ASCII byte matrix (column vector).
     * Each character is converted to its ASCII decimal value (0-255).
     *
     * @param inputString Input string to convert
     * @return Matrix representing ASCII values as column vector (N x 1)
     */
    private Matrix stringToAsciiMatrix(String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            // Return single-element zero vector for empty string
            Matrix result = new Matrix(1, 1);
            result.set(0, 0, 0.0);
            return result;
        }

        int length = inputString.length();
        Matrix asciiVector = new Matrix(length, 1); // Column vector (N x 1)

        for (int i = 0; i < length; i++) {
            char c = inputString.charAt(i);
            // Convert character to ASCII value (0-255 for ISO-8859-1)
            int asciiValue = (int) c;
            asciiVector.set(i, 0, (double) asciiValue);
        }

        return asciiVector;
    }
}
