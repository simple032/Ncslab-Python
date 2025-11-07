package com.ncslab.block.string;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.ScanStringDto;

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
 * ScanString block - parses a string into multiple outputs using scanf-style format.
 *
 * This block uses a format string with specifiers (%d, %f, %s) to parse
 * a string into multiple numeric or string outputs.
 *
 * SIMULINK Behavior:
 * - Input: String signal (text to parse)
 * - Output 1..N: Numeric or string signals (number determined by format string)
 * - Parameters:
 *   - Format: Format string with specifiers (default: "%d")
 *   - NumberOfOutputs: Number of output ports (default: 1, range: 1-32)
 *
 * Format Specifiers:
 * - %d: Integer (int32)
 * - %f: Floating-point (double)
 * - %s: String (reads until whitespace)
 *
 * Examples:
 * - Input="Value: 42", Format="Value: %d" → Output=42
 * - Input="Pi: 3.14", Format="%s: %f" → Output1="Pi", Output2=3.14
 *
 * Based on MATLAB/Simulink R2024b Scan String block specification.
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
public class ScanString extends Block {

    @Getter
    private final Parameter format;

    @Getter
    private final Parameter numberOfOutputs;

    public static final Map<String, Object> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Format", "%d");
        PARAMETER_DEFAULTS.put("NumberOfOutputs", 1);
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        inputNames.add("str");
        // Output names - dynamically populated based on NumberOfOutputs
    }

    public ScanString(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        this.format = getParameterByName("Format");
        this.numberOfOutputs = getParameterByName("NumberOfOutputs");

        if (this.format == null || this.numberOfOutputs == null) {
            throw new IllegalStateException("ScanString parameters not properly initialized");
        }

        // Single input port (string)
        inputPortList.add(new InputPort(this, 1));

        // Create dynamic number of output ports
        int numOutputs = Integer.parseInt(numberOfOutputs.getInitString());
        for (int i = 0; i < numOutputs; i++) {
            outputPortList.add(new OutputPort(this, i + 1, true));
        }
    }

    public ScanString(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        this.format = getParameterByName("Format");
        this.numberOfOutputs = getParameterByName("NumberOfOutputs");

        if (this.format == null || this.numberOfOutputs == null) {
            throw new IllegalStateException("ScanString parameters not properly initialized from DTO");
        }

        // Single input port (string)
        inputPortList.add(new InputPort(this, 1));

        // Create dynamic number of output ports
        int numOutputs = Integer.parseInt(numberOfOutputs.getInitString());
        for (int i = 0; i < numOutputs; i++) {
            outputPortList.add(new OutputPort(this, i + 1, true));
        }

        System.out.println("DTO-NATIVE: ScanString block created successfully - " + blockDto.getBlockName());
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Set dimensions for all output ports
        for (OutputPort out : outputPortList) {
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
            // CDataType will be determined based on format specifier
            // For now, default to DOUBLE
            out.getOutputSignalC().setCDataType(CDataType.DOUBLE);
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        int numOutputs = Integer.parseInt(numberOfOutputs.getInitString());
        if (numOutputs < 1 || numOutputs > 32) {
            throw new MatDimException("NumberOfOutputs must be between 1 and 32, got: " + numOutputs);
        }
    }

    @Override
    public void calculateInit() {
        // Initialize all outputs to 0 or empty string
        for (OutputPort out : outputPortList) {
            Data outputData = new Data(1, 1);
            outputData.setInitValue(0.0);
            out.setData(outputData);
        }
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in = inputPortList.get(0);
        Data inputData = in.getData();

        String inputString = "";
        if (inputData != null) {
            if (inputData instanceof StringData) {
                inputString = ((StringData) inputData).getStringValue();
            } else {
                String str = inputData.getInitString();
                if (str == null) {
                    str = inputData.getDataString();
                }
                inputString = (str != null) ? str : "";
            }
        }

        String formatStr = format.getInitString();
        int numOutputs = Integer.parseInt(numberOfOutputs.getInitString());

        // Parse the input string according to format
        // Simplified parsing: extract format specifiers and match them
        try {
            Scanner scanner = new Scanner(inputString);
            int outputIndex = 0;

            // Simple format parsing (very basic implementation)
            // Convert %d, %f, %s to regex patterns
            String regexPattern = formatStr;
            regexPattern = regexPattern.replaceAll("%d", "(-?\\\\d+)");
            regexPattern = regexPattern.replaceAll("%f", "(-?\\\\d+\\\\.?\\\\d*)");
            regexPattern = regexPattern.replaceAll("%s", "(\\\\S+)");

            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(inputString);

            if (matcher.find()) {
                for (int i = 0; i < numOutputs && i < matcher.groupCount() && i < outputPortList.size(); i++) {
                    String value = matcher.group(i + 1);
                    OutputPort out = outputPortList.get(i);

                    // Determine type from format specifier
                    if (formatStr.contains("%d")) {
                        // Integer
                        try {
                            int intValue = Integer.parseInt(value);
                            Data outputData = new Data(1, 1);
                            outputData.setInitValue((double) intValue);
                            out.setData(outputData);
                        } catch (NumberFormatException e) {
                            Data outputData = new Data(1, 1);
                            outputData.setInitValue(0.0);
                            out.setData(outputData);
                        }
                    } else if (formatStr.contains("%f")) {
                        // Float
                        try {
                            double doubleValue = Double.parseDouble(value);
                            Data outputData = new Data(1, 1);
                            outputData.setInitValue(doubleValue);
                            out.setData(outputData);
                        } catch (NumberFormatException e) {
                            Data outputData = new Data(1, 1);
                            outputData.setInitValue(0.0);
                            out.setData(outputData);
                        }
                    } else {
                        // String
                        StringData outputData = new StringData(value);
                        out.setData(outputData);
                    }
                }
            } else {
                // No match - set all outputs to default
                for (OutputPort out : outputPortList) {
                    Data outputData = new Data(1, 1);
                    outputData.setInitValue(0.0);
                    out.setData(outputData);
                }
            }
        } catch (Exception e) {
            System.err.println("ScanString parse error: " + e.getMessage());
            // Set all outputs to default on error
            for (OutputPort out : outputPortList) {
                Data outputData = new Data(1, 1);
                outputData.setInitValue(0.0);
                out.setData(outputData);
            }
        }
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

        // ScanString typically doesn't require initialization code
        // The string parsing is performed in output code
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/ScanString/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering ScanString template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/ScanString/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering ScanString MATLAB template: " + e.getMessage());
            e.printStackTrace();
        }
    }

}