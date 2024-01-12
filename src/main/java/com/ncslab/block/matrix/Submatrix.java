package com.ncslab.block.matrix;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

public class Submatrix extends Block {
    private Parameter startingRow;
    private Parameter endingRow;
    private Parameter startingColumn;
    private Parameter endingColumn;

    public Submatrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        startingRow = new Parameter(this, 1, "StartingRow", paramValues.getString("StartingRow"));
        endingRow = new Parameter(this, 2, "EndingRow", paramValues.getString("EndingRow"));
        startingColumn = new Parameter(this, 3, "StartingColumn", paramValues.getString("StartingColumn"));
        endingColumn = new Parameter(this, 4, "EndingColumn", paramValues.getString("EndingColumn"));

        parameterList.add(startingRow);
        parameterList.add(endingRow);
        parameterList.add(startingColumn);
        parameterList.add(endingColumn);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        StringBuilder initCode = new StringBuilder();
        initCode.append(String.format("/*Code for initialization of block Submatrix: (%d)%s*/\n", getBlockId(),
                getBlockName()));

        initCode.append(startingRow.getInitCodeC());
        initCode.append(endingRow.getInitCodeC());
        initCode.append(startingColumn.getInitCodeC());
        initCode.append(endingColumn.getInitCodeC());

        code.addInitCode(initCode.toString());
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        outputCode.append(
                String.format("/*Code for output of block Submatrix: (%d)%s*/\n", getBlockId(), getBlockName()));
        OutputPort out = outputPortList.get(0);

        String sOut = out.getOutputSignalC().getName();
        String sIn = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        outputCode.append(String.format("%s = %s.submatrix(%s,%s,%s,%s);\n", sOut, sIn, startingRow.getName(),
                endingRow.getName(), startingColumn.getName(), endingColumn.getName()));

        code.addOutputCode(outputCode.toString());
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
