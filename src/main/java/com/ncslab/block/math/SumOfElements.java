package com.ncslab.block.math;

import com.ncslab.block.data.Data;
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
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class SumOfElements extends Block {

    private String seq;
    boolean allDimensions = true;
    private int dimension;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
    }

    public SumOfElements(JSONObject blockJSON, NCSLabModel model) {
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

        allDimensions = "All dimensions".equals(paramValues.getString("SumOver"));
        dimension = paramValues.getInt("ElementsDimension");
    }

    @Override
    public void calculateInit() {
        // Initialization logic for SumOfElements block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data resultData = new Data(out.getHeight(), out.getWidth());

        if (isAllDimensions()) {
            for (int i = 0; i < seq.length(); i++) {
                if (seq.charAt(i) == '+') {
                    resultData = resultData.plus(inputPortList.get(i).getData());
                } else if (seq.charAt(i) == '-') {
                    resultData = resultData.minus(inputPortList.get(i).getData());
                }
            }
        } else {
            if (getDimension() == 1) {
                for (int i = 0; i < seq.length(); i++) {
                    if (seq.charAt(i) == '+') {
                        resultData = resultData.plus(extractRow(inputPortList.get(i).getData()));
                    } else if (seq.charAt(i) == '-') {
                        resultData = resultData.minus(extractRow(inputPortList.get(i).getData()));
                    }
                }
            } else if (getDimension() == 2) {
                for (int i = 0; i < seq.length(); i++) {
                    if (seq.charAt(i) == '+') {
                        resultData = resultData.plus(extractColumn(inputPortList.get(i).getData()));
                    } else if (seq.charAt(i) == '-') {
                        resultData = resultData.minus(extractColumn(inputPortList.get(i).getData()));
                    }
                }
            }
        }

        out.setData(resultData);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("sequence", getSequence());
        context.put("allDimensions", isAllDimensions());
        context.put("dimension", getDimension());

        String codeStr = TemplateManager.renderTemplate("c/math/SumOfElements/output.vm", context);
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
        for (int i = 0; i < seq.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }
        int m = signal[0].getHeight();
        int n = signal[0].getWidth();
        int v = 1;

        if (!isAllDimensions()) {
            if (getDimension() == 2) {
                for (int i = 0; i < seq.length(); i++) {
                    if ((signal[i].getWidth() != n) || (signal[i].getHeight() != m)) {
                        v = 0;
                        MatDimException e = new MatDimException("Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                        throw(e);
                    }
                }
            } else {
                for (int i = 0; i < seq.length(); i++) {
                    if ((signal[i].getWidth() != n) || (signal[i].getHeight() != m)) {
                        v = 0;
                        MatDimException e = new MatDimException("Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                        throw(e);
                    }
                }
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
    }

    public void checkDimension() throws MatDimException {
    }

    private Data extractRow(Data data) {
        double[] row = data.getMatrix().getRowPackedCopy();
        String rowString = arrayToString(row);
        return new Data(rowString);
    }

    private Data extractColumn(Data data) {
        double[] column = data.getMatrix().getColumnPackedCopy();
        String columnString = arrayToString(column);
        return new Data(columnString);
    }

    private String arrayToString(double[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
