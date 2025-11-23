package com.ncslab.block.string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.StringReplaceDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.data.StringData;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * StringReplace Block
 *
 * Replaces all occurrences of a substring with another substring.
 *
 * SIMULINK Block Behavior:
 * - Input: String signal to search in
 * - Output: String signal with replacements made
 * - Parameters:
 *   - OldSubstring: Substring to find
 *   - NewSubstring: Substring to replace with
 * - Replaces ALL occurrences (not just first)
 * - Case-sensitive matching
 * - If OldSubstring not found, output equals input
 *
 * Examples:
 * - Input="Hello World", OldSubstring="World", NewSubstring="Java" → Output="Hello Java"
 * - Input="test test", OldSubstring="test", NewSubstring="pass" → Output="pass pass"
 * - Input="Hello", OldSubstring="Bye", NewSubstring="Hi" → Output="Hello" (no change)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 4 String Block Implementation 2025
 */
public class StringReplace extends Block {

    private Parameter oldSubstring;
    private Parameter newSubstring;

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("OldSubstring", "old");
        PARAMETER_DEFAULTS.put("NewSubstring", "new");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        inputNames.add("str");
        outputNames.add("out");
    }

    /**
     * Legacy constructor from JSONObject
     */
    public StringReplace(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // Parent Block class parses parameterList automatically

        // Get parameters by name from the automatically populated parameterList
        this.oldSubstring = getParameterByName("OldSubstring");
        this.newSubstring = getParameterByName("NewSubstring");

        // Verify parameters are properly initialized
        if (this.oldSubstring == null || this.newSubstring == null) {
            throw new IllegalStateException("StringReplace parameters not properly initialized");
        }

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    /**
     * DTO-NATIVE Constructor
     */
    public StringReplace(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model); // Parent Block class handles DTO parameters

        // Get parameters by name from the automatically populated parameterList
        this.oldSubstring = getParameterByName("OldSubstring");
        this.newSubstring = getParameterByName("NewSubstring");

        // Verify parameters are properly initialized
        if (this.oldSubstring == null || this.newSubstring == null) {
            throw new IllegalStateException("StringReplace parameters not properly initialized from DTO");
        }

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough

        System.out.println("DTO-NATIVE: StringReplace block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create StringReplace from StringReplaceDto.
     *
     * @param dto The StringReplaceDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New StringReplace instance
     * @throws BlockCreationException if block creation fails
     */
    public static StringReplace createFromDto(StringReplaceDto dto, NCSLabModel model) throws BlockCreationException {
        return new StringReplace(dto, model);
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                // Output is string type (same dimension as input)
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
    public void checkDimension() throws MatDimException {
        // Input should be string, output will be string
    }

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
                String oldStr = oldSubstring.getInitString();
                String newStr = newSubstring.getInitString();

                // Replace all occurrences
                result = inputString.replace(oldStr, newStr);
            }
        }

        StringData outputData = new StringData(result);
        out.setData(outputData);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringReplace/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            // Empty init
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringReplace/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateOutputCodeM(CodeStructM code) {
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/StringReplace/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Parameter getOldSubstring() {
        return oldSubstring;
    }

    public void setOldSubstring(Parameter oldSubstring) {
        this.oldSubstring = oldSubstring;
    }

    public Parameter getNewSubstring() {
        return newSubstring;
    }

    public void setNewSubstring(Parameter newSubstring) {
        this.newSubstring = newSubstring;
    }
}
