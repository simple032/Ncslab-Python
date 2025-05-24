package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;

import Jama.Matrix;

import java.util.Vector;

public class Add extends Block {

    @Getter
    private String seq;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
    }

    public Add(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        paraseParamValues();
    }

    // get inputport list
    public void paraseParamValues() {
        seq = paramValues.getString("Inputs");

        for (int i = 0; i < seq.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    public boolean getSign(int n) {
        return seq.charAt(n) == '+';
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        OutputPort out = outputPortList.get(0);
        OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        String outputCode = "";
        switch (ops1.getOutputSignalC().getDataType()) {
            case REAL:
                outputCode += out.getOutputSignalC().getName() + "=0";
                for (int i = 0; i < seq.length(); i++) {
                    if (seq.charAt(i) == '+') {
                        outputCode += "+";
                    }
                    if (seq.charAt(i) == '-') {
                        outputCode += "-";
                    }
                    outputCode += inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
                }
                outputCode += ";\n";
                break;
            case MATRIX:
                for (int m = 1; m <= ops1.getHeight(); m++) {
                    for (int n = 1; n <= ops1.getWidth(); n++) {
                        outputCode += out.getOutputSignalC().getName() + "(" + m + "," + n + ") = 0";
                        for (int i = 0; i < seq.length(); i++) {
                            if (seq.charAt(i) == '+') {
                                outputCode += "+";
                            }
                            if (seq.charAt(i) == '-') {
                                outputCode += "-";
                            }
                            outputCode += inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + "(" + m + "," + n + ")";
                        }
                        outputCode += ";\n";
                    }
                }
                break;
        }
        code.addOutputCode(outputCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("sequence", getSeq());

        // 预计算矩阵维度
        if (!getInputPortList().isEmpty()) {
            OutputSignal firstInput = getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            if (firstInput.getDataType() == DataType.MATRIX) {
                context.put("inputHeight", firstInput.getHeight());
                context.put("inputWidth", firstInput.getWidth());
            }
        }

        String codeStr = TemplateManager.renderTemplate("c/math/Add/output.vm", context);
        code.addOutputCode(codeStr);
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
        for (OutputSignal x : signal) {
            if ((x.getHeight() != m) || (x.getWidth() != n)) {
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
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateOutput(double t){

        OutputPort out = outputPortList.get(0);
        Data data = new Data(out.getHeight(),out.getWidth());
        for (int i = 0; i < seq.length(); i++) {
            if(seq.charAt(i) == '+'){
                data = data.plus(inputPortList.get(i).getData());
            }else if(seq.charAt(i) == '-'){
                data = data.minus(inputPortList.get(i).getData());
            }
        }
        out.setData(data);
    }
}
