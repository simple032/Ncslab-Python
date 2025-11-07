package com.ncslab.block.string;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.StringLengthDto;

// Internal imports - Core
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.data.StringData;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * String Length measurement block - outputs the number of characters in input string.
 *
 * This block measures the length of a string signal and outputs the character count
 * as a uint32 scalar value. It does not count the null terminator.
 *
 * SIMULINK Behavior:
 * - Input: String signal (scalar)
 * - Output: uint32 scalar (character count)
 * - Output CDataType: UINT32
 * - Output DataType: REAL (scalar)
 *
 * Example: Input string "Hello" produces output 5
 *
 * Parameters: None required (simple measurement)
 *
 * Based on MATLAB/Simulink R2024b String Length block specification.
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
public class StringLength extends Block {

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
        outputNames.add("len");

        // Input port defaults - string input
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "str");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL"); // String stored as Data with initString
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults - uint32 scalar output (has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "len");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL"); // Scalar output
        output1.put("feedthrough", true); // Direct measurement with feedthrough
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    /**
     * Legacy constructor from JSONObject
     */
    public StringLength(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Single input (string) and single output (uint32 scalar)
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    /**
     * DTO-NATIVE Constructor - Creates StringLength block directly from BlockDto DTO
     */
    public StringLength(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Single input (string) and single output (uint32 scalar)
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough

        System.out.println("DTO-NATIVE: StringLength block created successfully - " + blockDto.getBlockName());
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

                System.out.println("StringLength updateDimension: blockId=" + this.blockId +
                                 ", blockName=" + this.blockName);

                // Output is always a scalar (1x1) with UINT32 type
                out.setHeight(1);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);

                // Set data type to REAL for scalar output
                out.getOutputSignalC().setDataType(DataType.REAL);

                // CRITICAL: Set output CDataType to UINT32 since output is character count
                out.getOutputSignalC().setCDataType(CDataType.UINT32);

                System.out.println("StringLength after updateDimension: dataType=" + out.getOutputSignalC().getDataType() +
                                 ", cDataType=" + out.getOutputSignalC().getCDataType() +
                                 ", height=" + out.getOutputSignalC().getHeight() +
                                 ", width=" + out.getOutputSignalC().getWidth());
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // String Length has no specific dimension constraints
        // Input should be string (scalar), output will be uint32 scalar
    }

    @Override
    public void calculateInit() {
        // Initialize output to 0 (length of empty string)
        OutputPort out = outputPortList.get(0);
        Data outputData = new Data(1, 1);
        outputData.setInitValue(0.0);
        out.setData(outputData);

        System.out.println("StringLength '" + blockName + "' initialized with output: 0");
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);

        // Get input data
        Data inputData = in.getData();
        int length = 0;

        if (inputData != null) {
            // Check if input is StringData
            if (inputData instanceof StringData) {
                StringData stringData = (StringData) inputData;
                length = stringData.getLength();
            } else {
                // Fallback: get string from initString or dataString
                String str = inputData.getInitString();
                if (str == null) {
                    str = inputData.getDataString();
                }
                if (str != null) {
                    length = str.length();
                }
            }
        }

        // Create output data with string length
        Data outputData = new Data(1, 1);
        outputData.setInitValue((double) length);
        out.setData(outputData);

        System.out.println("StringLength '" + blockName + "' calculated length: " + length);
    }

    // === Code Generation Methods ===

    /**
     * Generates C initialization code for String Length block.
     *
     * @param code Code structure to append to
     */
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // Populate all standard template variables first
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringLength/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            // If template not found, generate inline initialization
            String outputVar = getOutputPortVariables()[0];
            code.addInitCode(String.format("    %s = 0; // Initialize string length to 0\n", outputVar));
        }
    }

    /**
     * Generates C output code for String Length block.
     * Uses template-based generation for consistent C++ code.
     *
     * @param code Code structure to append to
     */
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        // Populate all standard template variables first
        TemplateUtils.populateAllContext(context, this);

        // Add block-specific context
        context.put("inputSignal", getInputPortVariables()[0]);
        context.put("outputSignal", getOutputPortVariables()[0]);
        context.put("outputCppType", CDataType.UINT32.getCppType());

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringLength/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            // If template not found, generate inline code
            String inputVar = getInputPortVariables()[0];
            String outputVar = getOutputPortVariables()[0];
            code.addOutputCode(String.format("    // String Length calculation\n"));
            code.addOutputCode(String.format("    %s = static_cast<uint32_t>(%s.length());\n",
                                            outputVar, inputVar));
        }
    }

    /**
     * Generates MATLAB output code for String Length block.
     *
     * @param code Code structure to append to
     */
    public void generateOutputCodeM(CodeStructM code) {
        context.put("block", this);
        context.put("inputs", getInputPortVariables());
        context.put("outputs", getOutputPortVariables());

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/StringLength/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            // If template not found, generate inline MATLAB code
            String inputVar = getInputPortVariables()[0];
            String outputVar = getOutputPortVariables()[0];
            code.addOutputCode(String.format("    %% String Length calculation\n"));
            code.addOutputCode(String.format("    %s = length(%s);\n", outputVar, inputVar));
        }
    }

    /**
     * Helper method to get input string length from input port.
     * Used during dimension propagation.
     */
    private int getInputStringLength() {
        if (!inputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                Data inputData = inputSignal.getData();

                if (inputData instanceof StringData) {
                    return ((StringData) inputData).getLength();
                } else if (inputData != null) {
                    String str = inputData.getInitString();
                    if (str == null) {
                        str = inputData.getDataString();
                    }
                    if (str != null) {
                        return str.length();
                    }
                }
            }
        }
        // Default to 0 if unable to determine
        return 0;
    }
}
