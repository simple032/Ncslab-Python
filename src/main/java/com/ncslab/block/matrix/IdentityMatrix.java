package com.ncslab.block.matrix;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class IdentityMatrix extends Block {
    private Parameter outputDimensions;

    public IdentityMatrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputDimensions = new Parameter(this, 1, "outputDimensions", paramValues.getString("outputDimensions"));
        parameterList.add(outputDimensions);

        outputPortList.add(new OutputPort(this, 1, false));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        StringBuilder initCode = new StringBuilder();
        initCode.append(String.format("/*Code for initialization of block Submatrix: (%d)%s*/\n", getBlockId(),
                getBlockName()));

        initCode.append(outputDimensions.getInitCodeC());

        code.addInitCode(initCode.toString());
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        outputCode.append(
                String.format("/*Code for output of block Submatrix: (%d)%s*/\n", getBlockId(), getBlockName()));
        OutputPort out = outputPortList.get(0);
        String sOut = out.getOutputSignalC().getName();
        outputCode.append(String.format("%s = Matrix.identity(%s);\n", sOut, outputDimensions.getName()));

        code.addOutputCode(outputCode.toString());
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        int n = (int)outputDimensions.getData().getInitValue();
        out.setHeight(n);
        out.setWidth(n);
        out.getOutputSignalC().setHeight(n);
        out.getOutputSignalC().setWidth(n);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }
}
