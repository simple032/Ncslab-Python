package com.ncslab.block.discontinuous;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Backlash extends Block {
    Parameter backlashWidth;
    Parameter initialOutput;

    private State xState;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("BacklashWidth");
        parameterNames.add("InitialOutput");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Backlash(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // 一个输入端口，一个输出端口
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
        backlashWidth = new Parameter(this, 1, "BacklashWidth", paramValues.getString("BacklashWidth"));
        initialOutput = new Parameter(this, 2, "InitialOutput", paramValues.getString("InitialOutput"));
        parameterList.add(backlashWidth);
        parameterList.add(initialOutput);
    }

    @Override
    public void calculateInit() {
        // 初始化逻辑
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                double lowerLimit = backlashWidth.getDouble();
                double previousOutput = xState != null ? xState.getData().getInitValue() : 0.0;
                double currentInput = inputData.getInitValue();
                if (currentInput >= previousOutput + lowerLimit) {
                    resultData = new Data(currentInput - lowerLimit);
                } else if (currentInput <= previousOutput - lowerLimit) {
                    resultData = new Data(currentInput + lowerLimit);
                } else {
                    resultData = new Data(previousOutput);
                }
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        double lowerLimitMatrix = backlashWidth.getMatrix().get(i, j);
                        double previousOutputMatrix = xState != null ? xState.getData().getMatrix().get(i, j) : 0.0;
                        double currentInputMatrix = inputData.getMatrix().get(i, j);
                        if (currentInputMatrix >= previousOutputMatrix + lowerLimitMatrix) {
                            matrixResult.set(i, j, currentInputMatrix - lowerLimitMatrix);
                        } else if (currentInputMatrix <= previousOutputMatrix - lowerLimitMatrix) {
                            matrixResult.set(i, j, currentInputMatrix + lowerLimitMatrix);
                        } else {
                            matrixResult.set(i, j, previousOutputMatrix);
                        }
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
        if (xState != null) {
            xState.setData(resultData);
        }
    }

    private void prepareContext() {
        
        OutputPort out  = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("block", this);
        context.put("inputPortList", inputPortList);
        context.put("outputPortList", outputPortList);
        context.put("backlashWidth", backlashWidth);
        context.put("initialOutput", initialOutput);
        context.put("xState", xState);
        context.put("signal",signal);
        context.put("ops", ops);
        context.put("matrixDataType", DataType.MATRIX);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode="";
        initCode+=backlashWidth.getInitCodeM();
        initCode+=initialOutput.getInitCodeM();
        code.addInitCode(initCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        OutputPort out  = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        String outputCode="";

        switch(signal.getDataType()) {
            case REAL:
                outputCode+=out.getOutputSignalC().getName()+"=max("+backlashWidth.getName()+","+signal.getName()+");\n";
                break;
            case MATRIX:
                for(int i=1; i<=signal.getHeight(); i++) {
                    for(int j=1; j<=signal.getWidth(); j++) {
                        outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=max("+backlashWidth.getName()+","+signal.getName()+"("+i+","+j+"));\n";
                    }
                }
                break;
        }
        code.addOutputCode(outputCode);
    }

    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        prepareContext();
        String initCode = TemplateManager.renderTemplate("c/discontinuous/Backlash/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code){
        super.generateOutputCodeC(code);
        String outputCode = TemplateManager.renderTemplate("c/discontinuous/Backlash/output.vm", context);
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException{
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if(backlashWidth.getWidth()!=initialOutput.getWidth()||backlashWidth.getHeight()!=initialOutput.getHeight()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
            throw(e);
        }
        if(backlashWidth.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
            out.setHeight(backlashWidth.getHeight());
            out.setWidth(backlashWidth.getWidth());
            out.getOutputSignalC().setHeight(backlashWidth.getHeight());
            out.getOutputSignalC().setWidth(backlashWidth.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }
        else if(backlashWidth.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        }
        else{
            if(backlashWidth.getWidth()!=signal.getWidth()||backlashWidth.getHeight()!=signal.getHeight()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the gain dimension!\n \n");
            throw(e);
            }
            out.setHeight(backlashWidth.getHeight());
            out.setWidth(backlashWidth.getWidth());
            out.getOutputSignalC().setHeight(backlashWidth.getHeight());
            out.getOutputSignalC().setWidth(backlashWidth.getWidth());
            out.getOutputSignalC().setDataType(backlashWidth.getDataType());
        }
    }
    public void checkDimension() throws MatDimException{
    }
}
