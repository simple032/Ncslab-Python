package com.ncslab.block.string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.StringToSingleDto;
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
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

/**
 * StringToSingle Block
 *
 * Converts a string to a single/float numeric value.
 *
 * SIMULINK Block Behavior:
 * - Input: String signal (text representation of number)
 * - Output: Single/float numeric value (32-bit precision)
 * - No parameters required
 * - Uses standard string-to-float conversion
 * - Invalid strings result in NaN output
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 3 String Block Implementation 2025
 */
public class StringToSingle extends Block {

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        inputNames.add("str");
        outputNames.add("val");
    }

    /**
     * Legacy constructor from JSONObject
     */
    public StringToSingle(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    /**
     * DTO-NATIVE Constructor
     */
    public StringToSingle(StringToSingleDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                // Output is always a scalar (1x1) with SINGLE type
                out.setHeight(1);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(DataType.REAL);
                out.getOutputSignalC().setCDataType(CDataType.SINGLE);
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Input should be string, output will be single scalar
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
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);

        Data inputData = in.getData();
        float result = 0.0f;

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
                try {
                    String trimmed = inputString.trim();
                    if (!trimmed.isEmpty()) {
                        result = Float.parseFloat(trimmed);
                    } else {
                        result = Float.NaN;
                    }
                } catch (NumberFormatException e) {
                    result = Float.NaN;
                }
            }
        }

        Data outputData = new Data(1, 1);
        outputData.setInitValue((double) result);
        out.setData(outputData);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);
        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringToSingle/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            // Empty init
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);
        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringToSingle/output.vm", context);
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
            String codeStr = TemplateManager.renderTemplate("m/string/StringToSingle/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
