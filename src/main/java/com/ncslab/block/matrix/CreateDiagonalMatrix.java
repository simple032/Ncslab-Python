package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CreateDiagonalMatrix extends Block{
    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters for CreateDiagonalMatrix
    }

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }
    public CreateDiagonalMatrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input", getInputPortVariables());
        context.put("output", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/matrix/CreateDiagonalMatrix/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("CreateDiagonalMatrix: input is not matrix");
        }

        int n = in.getHeight() > in.getWidth() ? in.getHeight() : in.getWidth();

        out.setHeight(n);
        out.setWidth(n);
        out.getOutputSignalC().setHeight(n);
        out.getOutputSignalC().setWidth(n);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }
}
