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
import com.ncslab.dto.block.specialized.string.StringFindDto;

// Internal imports - Core
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.BlockCreationException;
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
 * StringFind block - searches for pattern in string and returns index.
 *
 * This block searches for the first occurrence of a pattern string within
 * an input string and returns the 1-based index (MATLAB convention).
 *
 * SIMULINK Behavior:
 * - Input 1: String signal (text to search in)
 * - Input 2: String signal (pattern to search for)
 * - Output: int32 scalar (1-based index, 0 if not found)
 * - Output CDataType: INT32
 * - Output DataType: REAL (scalar)
 *
 * Examples:
 * - Input1="HelloWorld", Input2="World" → Output=6
 * - Input1="Test", Input2="xyz" → Output=0
 * - Input1="abcabc", Input2="bc" → Output=2 (first occurrence)
 *
 * Parameters: None required (simple search)
 *
 * Based on MATLAB/Simulink R2024b strfind block specification.
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
public class StringFind extends Block {

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
        inputNames.add("pattern");
        outputNames.add("index");

        // Input port defaults - two string inputs
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "str");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        Map<String, Object> input2 = new HashMap<>();
        input2.put("name", "pattern");
        input2.put("width", 1);
        input2.put("height", 1);
        input2.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input2);

        // Output port defaults - int32 scalar output (has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "index");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    /**
     * Legacy constructor from JSONObject
     */
    public StringFind(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Two inputs (str, pattern) and single output (index)
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    /**
     * DTO-NATIVE Constructor - Creates StringFind block directly from BlockDto DTO
     */
    public StringFind(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Two inputs (str, pattern) and single output (index)
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough

        System.out.println("DTO-NATIVE: StringFind block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create StringFind from StringFindDto.
     *
     * @param dto The StringFindDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New StringFind instance
     * @throws BlockCreationException if block creation fails
     */
    public static StringFind createFromDto(StringFindDto dto, NCSLabModel model) throws BlockCreationException {
        return new StringFind(dto, model);
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            System.out.println("StringFind updateDimension: blockId=" + this.blockId +
                             ", blockName=" + this.blockName);

            // Output is always a scalar (1x1) with INT32 type
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);

            // Set data type to REAL for scalar output
            out.getOutputSignalC().setDataType(DataType.REAL);

            // CRITICAL: Set output CDataType to INT32 since output is index
            out.getOutputSignalC().setCDataType(CDataType.INT32);

            System.out.println("StringFind after updateDimension: dataType=" + out.getOutputSignalC().getDataType() +
                             ", cDataType=" + out.getOutputSignalC().getCDataType() +
                             ", height=" + out.getOutputSignalC().getHeight() +
                             ", width=" + out.getOutputSignalC().getWidth());
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // StringFind has no specific dimension constraints
        // Input should be two strings (scalar), output will be int32 scalar
    }

    @Override
    public void calculateInit() {
        // Initialize output to 0 (not found)
        OutputPort out = outputPortList.get(0);
        Data outputData = new Data(1, 1);
        outputData.setInitValue(0.0);
        out.setData(outputData);

        System.out.println("StringFind '" + blockName + "' initialized with output: 0");
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in1 = inputPortList.get(0);
        InputPort in2 = inputPortList.get(1);
        OutputPort out = outputPortList.get(0);

        // Get input data
        Data inputData1 = in1.getData();
        Data inputData2 = in2.getData();
        int index = 0; // 0 means not found

        if (inputData1 != null && inputData2 != null) {
            // Get input strings
            String inputString = extractString(inputData1);
            String patternString = extractString(inputData2);

            // Search for pattern in input string
            int foundIndex = inputString.indexOf(patternString);

            if (foundIndex >= 0) {
                // Convert from 0-based (Java) to 1-based (MATLAB) index
                index = foundIndex + 1;
            }
            // else: index remains 0 (not found)
        }

        // Create output data with index
        Data outputData = new Data(1, 1);
        outputData.setInitValue((double) index);
        out.setData(outputData);

        System.out.println("StringFind '" + blockName + "' output: " + index);
    }

    /**
     * Helper method to extract string from Data or StringData
     */
    private String extractString(Data data) {
        if (data == null) {
            return "";
        }

        // Check if input is StringData
        if (data instanceof StringData) {
            StringData stringData = (StringData) data;
            return stringData.getStringValue();
        } else {
            // Fallback: get string from initString or dataString
            String str = data.getInitString();
            if (str == null) {
                str = data.getDataString();
            }
            if (str == null) {
                str = "";
            }
            return str;
        }
    }

    // === Static Methods ===
    public static List<String> getInputNames() {
        return inputNames;
    }

    public static List<String> getOutputNames() {
        return outputNames;
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // StringFind typically doesn't require initialization code
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringFind/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering StringFind template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/StringFind/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering StringFind MATLAB template: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

