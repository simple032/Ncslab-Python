package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.SubmatrixDto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

public class Submatrix extends Block {
    private Parameter startingRow;
    private Parameter endingRow;
    private Parameter startingColumn;
    private Parameter endingColumn;

    
    
    /**
     * DTO-NATIVE Constructor - Creates Submatrix block directly from BlockDto DTO
     */
    public Submatrix(SubmatrixDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Submatrix block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        
        outputNames.add("out1");
        inputNames.add("in1");
        
        PARAMETER_DEFAULTS.put("StartingRow", "1");
        PARAMETER_DEFAULTS.put("EndingRow", "-1");
        PARAMETER_DEFAULTS.put("StartingColumn", "1");
        PARAMETER_DEFAULTS.put("EndingColumn", "-1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public Submatrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        startingRow = getParameterByName("StartingRow");
        endingRow = getParameterByName("EndingRow");
        startingColumn = getParameterByName("StartingColumn");
        endingColumn = getParameterByName("EndingColumn");
        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("startingRow", startingRow);
        context.put("endingRow", endingRow);
        context.put("startingColumn", startingColumn);
        context.put("endingColumn", endingColumn);

        String codeStr = TemplateManager.renderTemplate("c/matrix/Submatrix/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input", getInputPortVariables()[0]);
        context.put("output", getOutputPortVariables()[0]);
        context.put("startingRow", startingRow.getData().getInitValue());
        context.put("endingRow", endingRow.getData().getInitValue());
        context.put("startingColumn", startingColumn.getData().getInitValue());
        context.put("endingColumn", endingColumn.getData().getInitValue());

        String codeStr = TemplateManager.renderTemplate("c/matrix/Submatrix/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("MatrixSquare: input is not matrix");
        }

        if (startingRow.getData().getInitValue() > endingRow.getData().getInitValue()) {
            throw new MatDimException("Submatrix: starting row is larger than ending row");
        }

        if (startingColumn.getData().getInitValue() > endingColumn.getData().getInitValue()) {
            throw new MatDimException("Submatrix: starting column is larger than ending column");
        }

        if (startingRow.getData().getInitValue() < 1 || endingRow.getData().getInitValue() > in.getHeight()) {
            throw new MatDimException("Submatrix: range of row is out of range");
        }

        if (startingColumn.getData().getInitValue() < 1 || endingColumn.getData().getInitValue() > in.getWidth()) {
            throw new MatDimException("Submatrix: range of column is out of range");
        }

        int outHeight = (int)(endingRow.getData().getInitValue() - startingRow.getData().getInitValue() + 1);
        int outWidth = (int)(endingColumn.getData().getInitValue() - startingColumn.getData().getInitValue() + 1);
        out.setHeight(outHeight);
        out.setWidth(outWidth);
        out.getOutputSignalC().setHeight(outHeight);
        out.getOutputSignalC().setWidth(outWidth);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }
}
