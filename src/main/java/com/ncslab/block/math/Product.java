package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Product extends Block {

    private String seq;
    boolean multiplication = false;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
    }

    public Product(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        this.multiplication = "Matrix(*)".equals(paramValues.getString("Multiplication"));

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        paraseParamValues();
    }

    // Parse parameter values
    public void paraseParamValues() {
        seq = paramValues.getString("Inputs");

        for (int i = 0; i < seq.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("sequence", getSequence());

        String codeStr = TemplateManager.renderTemplate("c/math/Product/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private String getSequence() {
        return seq;
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] signal = new OutputSignal[seq.length()];
        int m[] = new int[seq.length()];
        int n[] = new int[seq.length()];

        for (int i = 0; i < seq.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            m[i] = signal[i].getHeight();
            n[i] = signal[i].getWidth();
        }

        int v = 1;
        if (!isMultiplication()) {
            for (OutputSignal x : signal) {
                if ((x.getHeight() != m[0]) || (x.getWidth() != n[0])) {
                    v = 0;
                    MatDimException e = new MatDimException("Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                    throw(e);
                }
            }
            if (v == 1) {
                out.setHeight(signal[0].getHeight());
                out.setWidth(signal[0].getWidth());
                out.getOutputSignalC().setHeight(signal[0].getHeight());
                out.getOutputSignalC().setWidth(signal[0].getWidth());
                out.getOutputSignalC().setDataType(signal[0].getDataType());
            }
        } else {
            for (int i = 0; i < seq.length() - 1; i++) {
                if (n[i] != m[i + 1]) {
                    v = 0;
                    MatDimException e = new MatDimException("Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                    throw(e);
                }
            }
            out.setHeight(signal[0].getHeight());
            out.setWidth(signal[seq.length() - 1].getWidth());
            out.getOutputSignalC().setHeight(signal[0].getHeight());
            out.getOutputSignalC().setWidth(signal[seq.length() - 1].getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }
    }

    public void checkDimension() throws MatDimException {
    }

    private boolean isMultiplication() {
        return multiplication;
    }
}
