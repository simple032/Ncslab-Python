package com.ncslab.block.string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.StringContainsDto;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.data.StringData;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * StringContains Block
 *
 * Checks if a string contains a substring.
 *
 * SIMULINK Block Behavior:
 * - Input 1: String signal (haystack)
 * - Input 2: String signal (needle/substring to find)
 * - Output: Boolean (double: 1.0 if found, 0.0 if not found)
 * - No parameters required
 * - Case-sensitive matching
 *
 * Examples:
 * - Input1="Hello World", Input2="World" → Output=1.0 (true)
 * - Input1="Hello World", Input2="world" → Output=0.0 (false, case-sensitive)
 * - Input1="test", Input2="testing" → Output=0.0 (false)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 4 String Block Implementation 2025
 */
public class StringContains extends Block {

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        inputNames.add("str");
        inputNames.add("substr");
        outputNames.add("found");
    }

    public StringContains(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    public StringContains(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    /**
     * Factory method to create StringContains from StringContainsDto.
     *
     * @param dto The StringContainsDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New StringContains instance
     * @throws BlockCreationException if block creation fails
     */
    public static StringContains createFromDto(StringContainsDto dto, NCSLabModel model) throws BlockCreationException {
        return new StringContains(dto, model);
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            if (inputPortList.get(0).getLinkedLine() != null &&
                inputPortList.get(0).getLinkedLine().getLinkedOutputPort() != null) {
                // Output is boolean (as double: 0.0 or 1.0)
                out.setHeight(1);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(DataType.REAL);
                out.getOutputSignalC().setCDataType(CDataType.DOUBLE);
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {}

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

        Data inputData1 = in1.getData();
        Data inputData2 = in2.getData();
        boolean found = false;

        if (inputData1 != null && inputData2 != null) {
            String str1, str2;

            // Get first string
            if (inputData1 instanceof StringData) {
                str1 = ((StringData) inputData1).getStringValue();
            } else {
                str1 = inputData1.getInitString();
                if (str1 == null) str1 = inputData1.getDataString();
            }

            // Get second string
            if (inputData2 instanceof StringData) {
                str2 = ((StringData) inputData2).getStringValue();
            } else {
                str2 = inputData2.getInitString();
                if (str2 == null) str2 = inputData2.getDataString();
            }

            if (str1 != null && str2 != null) {
                found = str1.contains(str2);
            }
        }

        Data outputData = new Data(1, 1);
        outputData.setInitValue(found ? 1.0 : 0.0);
        out.setData(outputData);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);
        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringContains/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {}
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);
        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringContains/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateOutputCodeM(CodeStructM code) {
        context.put("block", this);
        context.put("inputs", getInputPortVariables());
        context.put("outputs", getOutputPortVariables());
        try {
            String codeStr = TemplateManager.renderTemplate("m/string/StringContains/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
