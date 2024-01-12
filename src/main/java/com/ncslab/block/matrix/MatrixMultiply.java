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

public class MatrixMultiply extends Block {

    private String seq;

    public MatrixMultiply(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);
        paraseParamValues();
    }

    /**
     * parse the paramValues to get the inputs
     */
    public void paraseParamValues() {
        seq = paramValues.getString("Inputs");

        for (int i = 0; i < seq.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        outputCode.append(
                String.format("/*Code for output of matrix multiply: (%d)%s*/\n", getBlockId(), getBlockName()));
        OutputPort out = outputPortList.get(0);
        OutputSignal signal[] = new OutputSignal[seq.length()];
        for (int i = 0; i < seq.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }

        outputCode.append(out.getOutputSignalC().getName() + "=" + signal[0].getName());
        for (int i = 1; i < seq.length(); i++) {
            outputCode.append("*" + signal[i].getName());
        }
        outputCode.append(";\n");

        code.addOutputCode(outputCode.toString());
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal signal[] = new OutputSignal[seq.length()];
        int m[] = new int[seq.length()];
        int n[] = new int[seq.length()];
        for (int i = 0; i < seq.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            m[i] = signal[i].getHeight();
            n[i] = signal[i].getWidth();
        }

        for (int i = 0; i < seq.length() - 1; i++) {
            if (n[i] != m[i + 1]) {
                MatDimException e = new MatDimException(
                        "Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                throw (e);
            }
        }
        out.setHeight(signal[0].getHeight());
        out.setWidth(signal[seq.length() - 1].getWidth());
        out.getOutputSignalC().setHeight(signal[0].getHeight());
        out.getOutputSignalC().setWidth(signal[seq.length() - 1].getWidth());
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    public void checkDimension() throws MatDimException {
    }
}
