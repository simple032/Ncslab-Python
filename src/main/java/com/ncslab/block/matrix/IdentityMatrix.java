package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class IdentityMatrix extends Block {
    private Parameter outputDimensions;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");
        parameterNames.add("outputDimensions");
    }

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
        context.put("block", this);
        context.put("dimensions", outputDimensions.getData().getInitValue());

        String codeStr = TemplateManager.renderTemplate("c/matrix/IdentityMatrix/output.vm", context);
        code.addOutputCode(codeStr);
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
