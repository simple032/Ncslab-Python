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

public class ProductOfElements extends Block {

    private String seq;
    boolean allDimensions = true;
    boolean multiplication = false;
    private int dimension;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
    }

    public ProductOfElements(JSONObject blockJSON, NCSLabModel model) {
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

        allDimensions = "All dimensions".equals(paramValues.getString("MultiplyOver"));
        dimension = paramValues.getInt("ElementsDimension");
    }

    public void generateOutputCodeC(CodeStructC code) {
        VelocityContext context = new VelocityContext();
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("sequence", getSequence());
        context.put("allDimensions", isAllDimensions());
        context.put("dimension", getDimension());

        String codeStr = TemplateManager.renderTemplate("c/math/ProductOfElements/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private String getSequence() {
        return seq;
    }

    private boolean isAllDimensions() {
        return allDimensions;
    }

    private int getDimension() {
        return dimension;
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
                if (seq.length() == 1) {
                    int height = 1, width = 1;
                    DataType type = DataType.REAL;
                    if (!isAllDimensions()) {
                        if (getDimension() == 2) {
                            width = signal[0].getWidth();
                        } else {
                            height = signal[0].getHeight();
                        }
                        type = DataType.MATRIX;
                    }
                    out.setHeight(height);
                    out.setWidth(width);
                    out.getOutputSignalC().setHeight(height);
                    out.getOutputSignalC().setWidth(width);
                    out.getOutputSignalC().setDataType(type);
                } else {
                    out.setHeight(signal[0].getHeight());
                    out.setWidth(signal[0].getWidth());
                    out.getOutputSignalC().setHeight(signal[0].getHeight());
                    out.getOutputSignalC().setWidth(signal[0].getWidth());
                    out.getOutputSignalC().setDataType(signal[0].getDataType());
                }
            }
        } else {
            if (seq.length() == 1) {
                if (seq.charAt(0) == '/' && n[0] != m[0]) {
                    MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions doesn't match !\n \n");
                    throw(e);
                }
            } else {
                for (int i = 0; i < seq.length() - 1; i++) {
                    if (seq.charAt(i) == '/' && n[i] != m[i]) {
                        v = 0;
                        MatDimException e = new MatDimException("Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                        throw(e);
                    }
                    if (n[i] != m[i + 1]) {
                        v = 0;
                        MatDimException e = new MatDimException("Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                        throw(e);
                    }
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
