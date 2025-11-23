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
import com.ncslab.dto.block.specialized.string.ToStringDto;

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
 * ToString block - converts numeric value to string signal.
 *
 * This block converts a scalar numeric input (integer or floating-point) to a string
 * representation.
 *
 * SIMULINK Behavior:
 * - Input: Numeric signal (scalar: uint8, int8, uint16, int16, uint32, int32, single, double)
 * - Output: String signal
 * - Output CDataType: STRING
 * - Output DataType: REAL (scalar)
 *
 * Examples:
 * - Input: 123 (int) → Output: "123"
 * - Input: 3.14159 (double) → Output: "3.14159"
 * - Input: -42 (int) → Output: "-42"
 *
 * Parameters: None required (simple conversion)
 *
 * Based on MATLAB/Simulink R2024b ToString block specification.
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
public class ToString extends Block {

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
        inputNames.add("num");
        outputNames.add("str");

        // Input port defaults - numeric input
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "num");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults - string output (has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "str");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    /**
     * Legacy constructor from JSONObject
     */
    public ToString(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Single input (numeric) and single output (string)
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    /**
     * DTO-NATIVE Constructor - Creates ToString block directly from BlockDto DTO
     */
    public ToString(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Single input (numeric) and single output (string)
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough

        System.out.println("DTO-NATIVE: ToString block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create ToString from ToStringDto.
     *
     * @param dto The ToStringDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New ToString instance
     * @throws BlockCreationException if block creation fails
     */
    public static ToString createFromDto(ToStringDto dto, NCSLabModel model) throws BlockCreationException {
        return new ToString(dto, model);
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal inputSignal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

                System.out.println("ToString updateDimension: blockId=" + this.blockId +
                                 ", blockName=" + this.blockName);

                // Output is always a scalar string (1x1)
                out.setHeight(1);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);

                // Set data type to REAL for scalar output
                out.getOutputSignalC().setDataType(DataType.REAL);

                // CRITICAL: Set output CDataType to STRING
                out.getOutputSignalC().setCDataType(CDataType.STRING);

                System.out.println("ToString after updateDimension: dataType=" + out.getOutputSignalC().getDataType() +
                                 ", cDataType=" + out.getOutputSignalC().getCDataType() +
                                 ", height=" + out.getOutputSignalC().getHeight() +
                                 ", width=" + out.getOutputSignalC().getWidth());
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // ToString has no specific dimension constraints
        // Input should be numeric scalar, output will be string
    }

    @Override
    public void calculateInit() {
        // Initialize output to empty string
        OutputPort out = outputPortList.get(0);
        StringData outputData = new StringData("");
        out.setData(outputData);

        System.out.println("ToString '" + blockName + "' initialized with empty string output");
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);

        // Get input data
        Data inputData = in.getData();
        String result = "";

        if (inputData != null) {
            // Convert numeric value to string
            double value = inputData.getInitValue();

            // Check if value is an integer (no fractional part)
            if (value == Math.floor(value) && !Double.isInfinite(value)) {
                // Integer value - format without decimal point
                result = String.valueOf((long) value);
            } else {
                // Floating-point value - format with decimal
                result = String.valueOf(value);
            }
        }

        // Create output data with string representation
        StringData outputData = new StringData(result);
        out.setData(outputData);

        System.out.println("ToString '" + blockName + "' output: \"" + result + "\"");
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

        // ToString typically doesn't require initialization code
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/ToString/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering ToString template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/ToString/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("ToString MATLAB template: " + e.getMessage());
            e.printStackTrace();
        }
    }

}