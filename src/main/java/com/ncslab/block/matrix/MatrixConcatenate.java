package com.ncslab.block.matrix;

import java.util.Arrays;
import java.util.Vector;

import lombok.Getter;
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

import com.ncslab.util.TemplateManager;


public class MatrixConcatenate extends Block {
    private String seq;
    private Parameter ConcatenateDimension;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        //输入的个数不确定
        parameterNames.add("ConcatenateDimension");
    }

    public MatrixConcatenate(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        ConcatenateDimension = new Parameter(this, 1, "ConcatenateDimension",
                paramValues.getString("ConcatenateDimension"));

        parameterList.add(ConcatenateDimension);

        outputPortList.add(new OutputPort(this, 1, true));

        seq = paramValues.getString("Inputs");

        for (int i = 0; i < seq.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        StringBuilder initCode = new StringBuilder();
        initCode.append(String.format("/*Code for initialization of block Matrix Concatenate: (%d)%s*/\n", getBlockId(),
                getBlockName()));

        initCode.append(ConcatenateDimension.getInitCodeC());

        code.addInitCode(initCode.toString());
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputSignals", getInputPortVariables());
        context.put("output", getOutputPortVariables()[0]);
        context.put("concatDimension", ConcatenateDimension.getData().getInitValue());

        String codeStr = TemplateManager.renderTemplate("c/matrix/MatrixConcatenate/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal signal[] = new OutputSignal[seq.length()];
        int m[] = new int[seq.length()]; // height
        int n[] = new int[seq.length()]; // width
        for (int i = 0; i < seq.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            m[i] = signal[i].getHeight();
            n[i] = signal[i].getWidth();
        }

        int dim = (int) ConcatenateDimension.getData().getInitValue();

        if (dim != 1 && dim != 2) {
            throw new MatDimException("MatrixConcatenate: ConcatenateDimension is not 1 or 2");
        }

        for (int i = 0; i < seq.length() - 1; i++) {
            if (dim == 1 && n[i] != n[i + 1]) {
                throw new MatDimException("MatrixConcatenate: input column dimensions doesn't match");
            }
            if (dim == 2 && m[i] != m[i + 1]) {
                throw new MatDimException("MatrixConcatenate: input row dimensions doesn't match");
            }
        }

        int outHeight = (dim == 1) ? Arrays.stream(m).sum() : m[0];
        int outWidth = (dim == 1) ? n[0] : Arrays.stream(n).sum();

        out.setHeight(outHeight);
        out.setWidth(outWidth);
        out.getOutputSignalC().setHeight(outHeight);
        out.getOutputSignalC().setWidth(outWidth);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }
}
