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
import com.ncslab.dto.block.specialized.string.ComposeStringDto;

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
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * ComposeString block - formats multiple inputs into a string using printf-style format.
 *
 * This block uses a format string with placeholders (%d, %f, %s) to compose
 * a string from multiple numeric or string inputs.
 *
 * SIMULINK Behavior:
 * - Input 1..N: Numeric or string signals (number determined by format string)
 * - Output: String signal (formatted result)
 * - Parameters:
 *   - Format: Format string with placeholders (default: "%d")
 *   - NumberOfInputs: Number of input ports (default: 1, range: 1-32)
 *
 * Format Specifiers:
 * - %d: Integer (int32)
 * - %f: Floating-point (double)
 * - %s: String
 * - %%: Literal % character
 *
 * Examples:
 * - Format="Value: %d", Input=42 → Output="Value: 42"
 * - Format="%s: %f", Input1="Pi", Input2=3.14 → Output="Pi: 3.14"
 *
 * Based on MATLAB/Simulink R2024b Compose String block specification.
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
public class ComposeString extends Block {

    @Getter
    private final Parameter format;

    @Getter
    private final Parameter numberOfInputs;

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Format", "%d");
        PARAMETER_DEFAULTS.put("NumberOfInputs", "1");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names - dynamically populated based on NumberOfInputs
        inputNames.add("in1");
        outputNames.add("out1");
    }

    public ComposeString(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        this.format = getParameterByName("Format");
        this.numberOfInputs = getParameterByName("NumberOfInputs");

        if (this.format == null || this.numberOfInputs == null) {
            throw new IllegalStateException("ComposeString parameters not properly initialized");
        }

        // Create dynamic number of input ports
        int numInputs = Integer.parseInt(numberOfInputs.getInitString());
        for (int i = 0; i < numInputs; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        // Single output port (string)
        outputPortList.add(new OutputPort(this, 1, true));
    }

    public ComposeString(ComposeStringDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        this.format = getParameterByName("Format");
        this.numberOfInputs = getParameterByName("NumberOfInputs");

        if (this.format == null || this.numberOfInputs == null) {
            throw new IllegalStateException("ComposeString parameters not properly initialized from DTO");
        }

        // Create dynamic number of input ports
        int numInputs = Integer.parseInt(numberOfInputs.getInitString());
        for (int i = 0; i < numInputs; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        // Single output port (string)
        outputPortList.add(new OutputPort(this, 1, true));

        System.out.println("DTO-NATIVE: ComposeString block created successfully - " + blockDto.getBlockName());
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.STRING);  // ✅ Fixed: Use STRING, not REAL
            out.getOutputSignalC().setCDataType(CDataType.STRING);
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        int numInputs = Integer.parseInt(numberOfInputs.getInitString());
        if (numInputs < 1 || numInputs > 32) {
            throw new MatDimException("NumberOfInputs must be between 1 and 32, got: " + numInputs);
        }
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        StringData outputData = new StringData("");
        out.setData(outputData);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        String formatStr = format.getInitString();
        int numInputs = Integer.parseInt(numberOfInputs.getInitString());

        // Collect input values
        Object[] args = new Object[numInputs];
        for (int i = 0; i < numInputs; i++) {
            if (i < inputPortList.size()) {
                InputPort in = inputPortList.get(i);
                Data inputData = in.getData();

                if (inputData != null) {
                    // Check if string or numeric
                    if (inputData instanceof StringData) {
                        args[i] = ((StringData) inputData).getStringValue();
                    } else {
                        String str = inputData.getInitString();
                        if (str != null && !str.isEmpty()) {
                            // String input
                            args[i] = str;
                        } else {
                            // Numeric input
                            args[i] = inputData.getInitValue();
                        }
                    }
                } else {
                    args[i] = 0.0; // Default for missing input
                }
            } else {
                args[i] = 0.0;
            }
        }

        // Format the string
        String result;
        try {
            result = String.format(formatStr, args);
        } catch (Exception e) {
            result = "Error: " + e.getMessage();
            System.err.println("ComposeString format error: " + e.getMessage());
        }

        StringData outputData = new StringData(result);
        out.setData(outputData);

        System.out.println("ComposeString '" + blockName + "' output: \"" + result + "\"");
    }

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

        // ComposeString typically doesn't require initialization code
        // The string formatting is performed in output code
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // Add ComposeString-specific context
        if (format != null) {
            context.put("Format", format.getInitString());
        }

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/ComposeString/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering ComposeString template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        // Add ComposeString-specific context
        if (format != null) {
            context.put("Format", format.getInitString());
        }

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/ComposeString/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering ComposeString MATLAB template: " + e.getMessage());
            e.printStackTrace();
        }
    }

}