package com.ncslab.block.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.string.StringToEnumDto;
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
 * StringToEnum Block
 *
 * Converts a string to an enumeration value (0-based index).
 *
 * SIMULINK Block Behavior:
 * - Input: String signal
 * - Output: Integer index (0-based) of matching enum value
 * - Parameters: EnumValues (comma-separated list)
 * - Case-sensitive string matching
 * - Returns 0 if no match found
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 3 String Block Implementation 2025
 */
public class StringToEnum extends Block {

    private Parameter enumValues;

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("EnumValues", "Value1,Value2,Value3");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        inputNames.add("str");
        outputNames.add("idx");
    }

    /**
     * Legacy constructor from JSONObject
     */
    public StringToEnum(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // Parent Block class parses parameterList automatically

        // Get parameter by name from the automatically populated parameterList
        this.enumValues = getParameterByName("EnumValues");

        // Verify parameter is properly initialized
        if (this.enumValues == null) {
            throw new IllegalStateException("StringToEnum parameter not properly initialized");
        }

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough
    }

    /**
     * DTO-NATIVE Constructor
     */
    public StringToEnum(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model); // Parent Block class handles DTO parameters

        // Get parameter by name from the automatically populated parameterList
        this.enumValues = getParameterByName("EnumValues");

        // Verify parameter is properly initialized
        if (this.enumValues == null) {
            throw new IllegalStateException("StringToEnum parameter not properly initialized from DTO");
        }

        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough

        System.out.println("DTO-NATIVE: StringToEnum block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create StringToEnum from StringToEnumDto.
     *
     * @param dto The StringToEnumDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New StringToEnum instance
     * @throws BlockCreationException if block creation fails
     */
    public static StringToEnum createFromDto(StringToEnumDto dto, NCSLabModel model) throws BlockCreationException {
        return new StringToEnum(dto, model);
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);
            OutputPort out = outputPortList.get(0);

            if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
                // Output is always a scalar (1x1) with INT32 type
                out.setHeight(1);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(DataType.REAL);
                out.getOutputSignalC().setCDataType(CDataType.INT32);
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Input should be string, output will be int32 scalar
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
        int matchIndex = 0;

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
                String enumValuesStr = enumValues.getInitString();
                List<String> enumList = Arrays.asList(enumValuesStr.split(","));
                String trimmedInput = inputString.trim();

                for (int i = 0; i < enumList.size(); i++) {
                    if (enumList.get(i).trim().equals(trimmedInput)) {
                        matchIndex = i;
                        break;
                    }
                }
            }
        }

        Data outputData = new Data(1, 1);
        outputData.setInitValue((double) matchIndex);
        out.setData(outputData);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // Add computed enum values list (keep)
        if (enumValues != null) {
            String enumValuesStr = enumValues.getInitString();
            List<String> enumList = Arrays.asList(enumValuesStr.split(","));
            List<String> trimmedEnumList = new ArrayList<>();
            for (String enumValue : enumList) {
                trimmedEnumList.add(enumValue.trim());
            }
            context.put("EnumValuesList", trimmedEnumList);  // COMPUTED - split, trim, collect
        }

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringToEnum/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // Add computed enum values list (keep)
        if (enumValues != null) {
            String enumValuesStr = enumValues.getInitString();
            List<String> enumList = Arrays.asList(enumValuesStr.split(","));
            List<String> trimmedEnumList = new ArrayList<>();
            for (String enumValue : enumList) {
                trimmedEnumList.add(enumValue.trim());
            }
            context.put("EnumValuesList", trimmedEnumList);  // COMPUTED - split, trim, collect
        }

        try {
            String codeStr = TemplateManager.renderTemplate("c/string/StringToEnum/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateOutputCodeM(CodeStructM code) {
        TemplateUtils.populateAllContext(context, this);

        // Computed values for MATLAB (keep)
        context.put("inputs", getInputPortVariables());
        context.put("outputs", getOutputPortVariables());

        // Add computed enum values list (keep)
        if (enumValues != null) {
            String enumValuesStr = enumValues.getInitString();
            List<String> enumList = Arrays.asList(enumValuesStr.split(","));
            List<String> trimmedEnumList = new ArrayList<>();
            for (String enumValue : enumList) {
                trimmedEnumList.add(enumValue.trim());
            }
            context.put("EnumValuesList", trimmedEnumList);  // COMPUTED - split, trim, collect
        }

        try {
            String codeStr = TemplateManager.renderTemplate("m/string/StringToEnum/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Parameter getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(Parameter enumValues) {
        this.enumValues = enumValues;
    }
}
