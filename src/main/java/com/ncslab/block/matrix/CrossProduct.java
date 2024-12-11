package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.Vector;

public class CrossProduct extends Block {





    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
    }
    public CrossProduct(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        outputCode.append(
                String.format("/*Code for output of cross product: (%d)%s*/\n", getBlockId(), getBlockName()));
        OutputPort out = outputPortList.get(0);
        String sOut = out.getOutputSignalC().getName();
        String sIn1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        String sIn2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();

        outputCode.append(String.format("%s = %s.crossProduct(%s);\n", sOut, sIn1, sIn2));

        code.addOutputCode(outputCode.toString());
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal in2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in1.getDataType() != DataType.MATRIX || in2.getDataType() != DataType.MATRIX) {
            throw new MatDimException("CrossProduct: input is not matrix");
        }

        if (in1.getHeight() * in1.getWidth() != 3 || in2.getHeight() * in2.getWidth() != 3) {
            throw new MatDimException("CrossProduct: input is not 3x1 or 1x3 matrix");
        }

        out.setHeight(1);
        out.setWidth(3);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(3);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    public void checkDimension() throws MatDimException {
    }
}
