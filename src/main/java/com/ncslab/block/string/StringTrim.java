package com.ncslab.block.string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.StringTrimDto;
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
 * StringTrim Block
 *
 * Removes leading and trailing whitespace from a string.
 *
 * SIMULINK Block Behavior:
 * - Input: String signal
 * - Output: String signal with whitespace trimmed
 * - No parameters required
 * - Removes spaces, tabs, newlines from both ends
 *
 * Examples:
 * - Input="  Hello  " → Output="Hello"
 * - Input="\tTest\n" → Output="Test"
 * - Input="NoSpaces" → Output="NoSpaces"
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 4 String Block Implementation 2025
 */
public class StringTrim extends Block {

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        inputNames.add("str");
        outputNames.add("trimmed");
    }

    public StringTrim(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    public StringTrim(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    /**
     * Factory method to create StringTrim from StringTrimDto.
     *
     * @param dto The StringTrimDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New StringTrim instance
     * @throws BlockCreationException if block creation fails
     */
    public static StringTrim createFromDto(StringTrimDto dto, NCSLabModel model) throws BlockCreationException {
        return new StringTrim(dto, model);
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                out.setHeight(1);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(DataType.REAL);
                out.getOutputSignalC().setCDataType(CDataType.STRING);
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {}

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        StringData outputData = new StringData("");
        out.setData(outputData);
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);

        Data inputData = in.getData();
        String result = "";

        if (inputData != null) {
            String inputString;
            if (inputData instanceof StringData) {
                inputString = ((StringData) inputData).getStringValue();
            } else {
                inputString = inputData.getInitString();
                if (inputString == null) {
                    inputString = inputData.getDataString();
                }
            }

            if (inputString != null) {
                result = inputString.trim();
            }
        }

        StringData outputData = new StringData(result);
        out.setData(outputData);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);
        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringTrim/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {}
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);
        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringTrim/output.vm", context);
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
            String codeStr = TemplateManager.renderTemplate("m/string/StringTrim/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
