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

public class Transpose extends Block {
    public Transpose(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        outputCode.append(String.format("/*Code for output of permute matrix: (%d)%s*/\n", getBlockId(), getBlockName()));
        OutputPort out = outputPortList.get(0);

        String sOut = out.getOutputSignalC().getName();
        String sIn = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        outputCode.append(String.format("%s = %s.transpose();\n", sOut, sIn));
        code.addOutputCode(outputCode.toString());
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("PermuteMatrix: input is not matrix");
        }

        out.setHeight(in.getWidth());
        out.setWidth(in.getHeight());
        out.getOutputSignalC().setHeight(in.getWidth());
        out.getOutputSignalC().setWidth(in.getHeight());
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }
}
