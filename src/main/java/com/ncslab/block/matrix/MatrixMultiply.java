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
import com.ncslab.util.TemplateManager;

public class MatrixMultiply extends Block {

    private String seq;



    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        //输入个数不确定
    }

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
        context.put("block", this);
        context.put("inputSignals", getInputPortVariables());
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("c/matrix/MatrixMultiply/output.vm", context);
        code.addOutputCode(codeStr);
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
