package com.ncslab.block.advancedControl;

import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

/**
 * only support matrix input
 */
public class LQRController extends Block {
    // u = -Kx, directly related
    private boolean feedThrough = true;

    private Parameter A;
    private Parameter B;
    private Parameter Q;
    private Parameter R;

    public LQRController(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // parseVector();
        A = new Parameter(this, 1, "A", paramValues.getString("A"));
        B = new Parameter(this, 2, "B", paramValues.getString("B"));
        Q = new Parameter(this, 3, "Q", paramValues.getString("Q"));
        R = new Parameter(this, 4, "R", paramValues.getString("R"));

        parameterList.add(A);
        parameterList.add(B);
        parameterList.add(Q);
        parameterList.add(R);

        // xState = new State(this, 1, "x", A.getWidth(), 1);

        // stateList.add(xState);

        // set input and output port
        InputPort input;
        OutputPort output;

        input = new InputPort(this, 1);
        output = new OutputPort(this, 1, feedThrough);
        output.setHeight(B.getWidth());

        inputPortList.add(input);
        outputPortList.add(output);

    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        StringBuilder initCode = new StringBuilder();
        initCode.append(String.format("/*Code for initialization of block LQR Controller: (%d)%s*/\n", getBlockId(),
                getBlockName()));
        initCode.append(A.getInitCodeC());
        initCode.append(B.getInitCodeC());
        initCode.append(Q.getInitCodeC());
        initCode.append(R.getInitCodeC());

        // calculate K
        // K.height = B.width
        // K.width = A.width = A.height
        initCode.append(
                String.format("Matrix K = lqr(%s, %s, %s, %s);\n", A.getName(), B.getName(), Q.getName(), R.getName()));

        code.addInitCode(initCode.toString());
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        StringBuilder outputCode = new StringBuilder();
        outputCode.append(
                String.format("/*Code for output of block LQR Controller: (%d)%s*/\n", getBlockId(), getBlockName()));
        outputCode.append("/*******************************/\n");

        OutputPort out = this.getOutputPortList().get(0);
        // u = -K * x
        switch (out.getOutputSignalC().getDataType()) {
            case MATRIX:
                outputCode.append(
                        String.format("%s = -K * %s;\n", this.getOutputPortVariable(0), this.getInputPortVariable(0)));
                break;
            case REAL:
                outputCode.append(String.format("%s = (-K * %s)(0,0);\n", this.getOutputPortVariable(0),
                        this.getInputPortVariable(0)));
                break;
        }

        outputCode.append("/*******************************/\n");

        code.addOutputCode(outputCode.toString());
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);

        if (this.feedThrough) {
            if (A.getWidth() != A.getHeight() // if A is square
                    || A.getHeight() != B.getHeight() // if A and B match
                    || A.getHeight() != Q.getHeight() // if A and Q match
                    || Q.getWidth() != Q.getHeight() // if Q is square
                    || R.getWidth() != R.getHeight() // if R is square
                    || R.getWidth() != B.getWidth() // if R and B match
                    || A.getHeight() != in.getHeight() // if A and input match
                    || out.getHeight() != B.getWidth() // if B and output match
                    || in.getWidth() != 1 // 输入必须是列向量
            ) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match!");
                throw (e);

            }
        }

        // based on params, set output dimension
        out.setHeight(B.getWidth());
        out.setWidth(1);
        out.getOutputSignalC().setHeight(B.getWidth());
        out.getOutputSignalC().setWidth(1);

        out.getOutputSignalC().setDataType(B.getWidth() > 1 ? DataType.MATRIX : DataType.REAL);
    }

    @Override
    public void checkDimension() throws MatDimException {

    }
}
