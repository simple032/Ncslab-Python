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
import com.ncslab.dto.block.specialized.string.StringCompareDto;

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
 * StringCompare block - compares two input strings for equality.
 *
 * SIMULINK Behavior:
 * - Input 1: First string signal
 * - Input 2: Second string signal
 * - Output: Boolean (1.0 if equal, 0.0 if not equal)
 * - Parameter: CaseSensitive - "on" or "off" (default: "on")
 *
 * Examples:
 * - Input1="Hello", Input2="Hello", CaseSensitive="on" → Output=1
 * - Input1="Hello", Input2="hello", CaseSensitive="on" → Output=0
 * - Input1="Hello", Input2="hello", CaseSensitive="off" → Output=1
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
public class StringCompare extends Block {

    @Getter
    private final Parameter caseSensitive;

    public static final Map<String, Object> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("CaseSensitive", "on");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        inputNames.add("str1");
        inputNames.add("str2");
        outputNames.add("equal");

        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "str1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        Map<String, Object> input2 = new HashMap<>();
        input2.put("name", "str2");
        input2.put("width", 1);
        input2.put("height", 1);
        input2.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input2);

        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "equal");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    public StringCompare(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        this.caseSensitive = getParameterByName("CaseSensitive");
        if (this.caseSensitive == null) {
            throw new IllegalStateException("StringCompare parameters not properly initialized");
        }

        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    public StringCompare(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        this.caseSensitive = getParameterByName("CaseSensitive");
        if (this.caseSensitive == null) {
            throw new IllegalStateException("StringCompare parameters not properly initialized from DTO");
        }

        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, 1, true));

        System.out.println("DTO-NATIVE: StringCompare block created successfully - " + blockDto.getBlockName());
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
            out.getOutputSignalC().setCDataType(CDataType.UINT8); // Boolean output (0 or 1)
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // No specific dimension constraints
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        Data outputData = new Data(1, 1);
        outputData.setInitValue(0.0);
        out.setData(outputData);
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in1 = inputPortList.get(0);
        InputPort in2 = inputPortList.get(1);
        OutputPort out = outputPortList.get(0);

        String str1 = extractString(in1.getData());
        String str2 = extractString(in2.getData());

        boolean isCaseSensitive = "on".equalsIgnoreCase(caseSensitive.getInitString());
        boolean isEqual;

        if (isCaseSensitive) {
            isEqual = str1.equals(str2);
        } else {
            isEqual = str1.equalsIgnoreCase(str2);
        }

        Data outputData = new Data(1, 1);
        outputData.setInitValue(isEqual ? 1.0 : 0.0);
        out.setData(outputData);
    }

    private String extractString(Data data) {
        if (data == null) return "";
        if (data instanceof StringData) {
            return ((StringData) data).getStringValue();
        }
        String str = data.getInitString();
        if (str == null) str = data.getDataString();
        return (str != null) ? str : "";
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

        // StringCompare typically doesn't require initialization code
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringCompare/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering StringCompare template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/StringCompare/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering StringCompare MATLAB template: " + e.getMessage());
            e.printStackTrace();
        }
    }

}