package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import java.util.Vector;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

public class Sum extends Block {

    private String seq;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
    }

    public Sum(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        paraseParamValues();
    }

    private void paraseParamValues() {
        seq = paramValues.getString("Inputs");

        for (int i = 0; i < seq.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        VelocityContext context = new VelocityContext();
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("sequence", getSequence());

        String codeStr = TemplateManager.renderTemplate("c/math/Sum/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private String getSequence() {
        return seq;
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] signal = new OutputSignal[seq.length()];
        int m = -1, n = -1;
        int v = 1;

        for (int i = 0; i < seq.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

            if (i == 0) {
                m = signal[i].getHeight();
                n = signal[i].getWidth();
            } else {
                if ((signal[i].getHeight() != m) || (signal[i].getWidth() != n)) {
                    v = 0;
                    MatDimException e = new MatDimException("Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                    throw(e);
                }
            }
        }

        if (v == 1) {
            out.setHeight(signal[0].getHeight());
            out.setWidth(signal[0].getWidth());
            out.getOutputSignalC().setHeight(signal[0].getHeight());
            out.getOutputSignalC().setWidth(signal[0].getWidth());
            out.getOutputSignalC().setDataType(signal[0].getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
    }
}
