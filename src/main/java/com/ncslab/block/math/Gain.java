package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;
import com.ncslab.util.TemplateManager;

public class Gain extends Block {
    protected Parameter gain;
    boolean multiplication = false;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Gain(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        this.gain = new Parameter(this, 1, getBlockName(), paramValues.getString("Gain"));
        this.multiplication = "Matrix(*)".equals(paramValues.getString("Multiplication"));

        if (!this.multiplication) {
            outputPortList.add(new OutputPort(this, 1, true));
            inputPortList.add(new InputPort(this, 1));
        } else {
            outputPortList.add(new OutputPort(this, 1, true));
            inputPortList.add(new InputPort(this, 1));
        }

        parameterList.add(gain);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode = "";
        initCode += gain.getInitCodeM();
        code.addInitCode(initCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        String outputCode = "";

        if (this.multiplication == false) {
            switch (getGain().getDataType()) {
                case REAL:
                    switch (ops.getOutputSignalC().getDataType()) {
                        case REAL:
                            outputCode += out.getOutputSignalC().getName() + "=";
                            outputCode += getGain().getName() + "*" + ops.getOutputSignalC().getName() + ";\n";
                            break;
                        case MATRIX:
                            for (int i = 1; i <= ops.getHeight(); i++) {
                                for (int j = 1; j <= ops.getWidth(); j++) {
                                    outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=";
                                    outputCode += getGain().getName() + "*" + ops.getOutputSignalC().getName() + "(" + i + "," + j + ");\n";
                                }
                            }
                            break;
                    }
                    break;
                case MATRIX:
                    switch (ops.getOutputSignalC().getDataType()) {
                        case REAL:
                            for (int i = 1; i <= getGain().getHeight(); i++) {
                                for (int j = 1; j <= getGain().getWidth(); j++) {
                                    outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=";
                                    outputCode += getGain().getName() + "(" + i + "," + j + ")*" + ops.getOutputSignalC().getName() + ";\n";
                                }
                            }
                            break;
                        case MATRIX:
                            for (int i = 1; i <= ops.getHeight(); i++) {
                                for (int j = 1; j <= ops.getWidth(); j++) {
                                    outputCode += out.getOutputSignalC().getName() + "(" + i + "," + j + ")=";
                                    outputCode += getGain().getName() + "(" + i + "," + j + ")*" + ops.getOutputSignalC().getName() + "(" + i + "," + j + ");\n";
                                }
                            }
                            break;
                    }
                    break;
            }
        } else {
            switch (getGain().getDataType()) {
                case REAL:
                    switch (ops.getOutputSignalC().getDataType()) {
                        case REAL:
                            outputCode += out.getOutputSignalC().getName() + "=";
                            outputCode += getGain().getName() + "*" + ops.getOutputSignalC().getName() + ";\n";
                            break;
                        case MATRIX:
                            outputCode += "for(int i=0;i<" + ops.getHeight() + ";i++){\n";
                            outputCode += out.getOutputSignalC().getName() + "(i,0)=" + getGain().getName() + "*" + ops.getOutputSignalC().getName() + "(i,0);\n";
                            outputCode += "}\n";
                            break;
                    }
                    break;
                case MATRIX:
                    switch (ops.getOutputSignalC().getDataType()) {
                        case REAL:
                            outputCode += "for(int i=0;i<" + getGain().getWidth() + ";i++){\n";
                            outputCode += out.getOutputSignalC().getName() + "[0][i]=" + getGain().getName() + "[0][i]*" + ops.getOutputSignalC().getName() + ";\n";
                            outputCode += "}\n";
                            break;
                        case MATRIX:
                            outputCode += out.getOutputSignalC().getName() + "=" + ops.getOutputSignalC().getName() + "*" + getGain().getName() + ";\n";
                            break;
                    }
            }
        }

        code.addOutputCode(outputCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = getGain().getInitCodeC();
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("gain", getGain());
        context.put("multiplication", isMultiplication());

        String codeStr = TemplateManager.renderTemplate("c/math/Gain/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private Parameter getGain() {
        return gain;
    }

    private boolean isMultiplication() {
        return multiplication;
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (this.multiplication == false) {
            switch (getGain().getDataType()) {
                case REAL:
                    out.setHeight(signal.getHeight());
                    out.setWidth(signal.getWidth());
                    out.getOutputSignalC().setHeight(signal.getHeight());
                    out.getOutputSignalC().setWidth(signal.getWidth());
                    out.getOutputSignalC().setDataType(signal.getDataType());
                    break;
                case MATRIX:
                    switch (signal.getDataType()) {
                        case REAL:
                            out.setHeight(getGain().getHeight());
                            out.setWidth(getGain().getWidth());
                            out.getOutputSignalC().setHeight(getGain().getHeight());
                            out.getOutputSignalC().setWidth(getGain().getWidth());
                            out.getOutputSignalC().setDataType(getGain().getDataType());
                            break;
                        case MATRIX:
                            if (signal.getHeight() != getGain().getHeight() || signal.getWidth() != getGain().getWidth()) {
                                MatDimException e = new MatDimException("Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
                                throw(e);
                            }
                            out.setHeight(signal.getHeight());
                            out.setWidth(signal.getWidth());
                            out.getOutputSignalC().setHeight(signal.getHeight());
                            out.getOutputSignalC().setWidth(signal.getWidth());
                            out.getOutputSignalC().setDataType(signal.getDataType());
                            break;
                    }
                    break;
            }
        } else {
            if (signal.getWidth() != getGain().getHeight()) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
                throw(e);
            }

            out.setHeight(signal.getHeight());
            out.setWidth(getGain().getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(getGain().getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateInit() {
        
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data data = inputPortList.get(0).getData().times(gain.getData());        
        out.setData(data);
    }
}
