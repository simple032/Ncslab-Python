package com.ncslab.block.matrix;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

/**
 * matrix square = A * A^H;
 * for real matrix, A^H = A^T
 */
public class MatrixSquare extends Block {
    public MatrixSquare(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        outputCode.append(String.format("/*Code for output of matrix square: (%d)%s*/\n", getBlockId(), getBlockName()));
        OutputPort out = outputPortList.get(0);

        String sOut = out.getOutputSignalC().getName();
        String sIn = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        outputCode.append(String.format("%s = %s * %s.transpose();\n", sOut, sIn, sIn));
        code.addOutputCode(outputCode.toString());
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("MatrixSquare: input is not matrix");
        }

        out.setHeight(in.getHeight());
        out.setWidth(in.getHeight());
        out.getOutputSignalC().setHeight(in.getHeight());
        out.getOutputSignalC().setWidth(in.getHeight());
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }
}
