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
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Coulomb extends Block {
    Parameter offset;
    Parameter gain;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("offset");
        parameterNames.add("gain");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Coulomb(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // 一个输入端口，一个输出端口
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
        offset = new Parameter(this, 1, "offset", paramValues.getString("offset"));
        gain = new Parameter(this, 2, "gain", paramValues.getString("gain"));
        parameterList.add(offset);
        parameterList.add(gain);
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
                double signValue = inputData.getInitValue() >= 0 ? 1.0 : -1.0;
                double absValue = Math.abs(inputData.getInitValue());
                double offsetValue = offset.getDouble();
                double gainValue = gain.getDouble();
                resultData = new Data(signValue * (gainValue * absValue + offsetValue));
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        double signValueMatrix = inputData.getMatrix().get(i, j) >= 0 ? 1.0 : -1.0;
                        double absValueMatrix = Math.abs(inputData.getMatrix().get(i, j));
                        double offsetValueMatrix = offset.getMatrix().get(i, j);
                        double gainValueMatrix = gain.getMatrix().get(i, j);
                        matrixResult.set(i, j, signValueMatrix * (gainValueMatrix * absValueMatrix + offsetValueMatrix));
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private void prepareContext() {
        OutputPort out  = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("block", this); // 当前Block对象（含getBlockId()）
        context.put("inputPortList", inputPortList); // 输入端口列表
        context.put("outputPortList", outputPortList); // 输出端口列表
        context.put("offset", offset); // 偏移量参数对象
        context.put("gain", gain); // 增益参数对象
        context.put("signal",signal);
        context.put("ops", ops);
        context.put("realDataType", DataType.REAL); // 实数类型标识
        context.put("matrixDataType", DataType.MATRIX); // 矩阵类型标识
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode="";
        initCode+=offset.getInitCodeM();
        initCode+=gain.getInitCodeM();
        code.addInitCode(initCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        OutputPort out  = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        String outputCode="";

        switch(gain.getDataType()) {
            case REAL:
                switch(ops.getOutputSignalC().getDataType()) {
                    case REAL:
                        outputCode+=out.getOutputSignalC().getName()+"="+"sign("+signal.getName()+")*("+gain.getName()+"*abs("+signal.getName()+")+"+offset.getName()+");\n";
                        break;
                    case MATRIX:
                        for(int i=1; i<=ops.getHeight(); i++) {
                            for(int j=1; j<=ops.getWidth(); j++) {
                                outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+"sign("+signal.getName()+"("+i+","+j+"))*("+gain.getName()+"*abs("+signal.getName()+"("+i+","+j+"))+"+offset.getName()+");\n";
                            }
                        }
                        break;
                }
                break;
            case MATRIX:
                switch(ops.getOutputSignalC().getDataType()) {
                    case REAL:
                        for(int i=1; i<=gain.getHeight(); i++) {
                            for(int j=1; j<=gain.getWidth(); j++) {
                                outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+"sign("+signal.getName()+")*("+gain.getName()+"("+i+","+j+")*abs("+signal.getName()+")+"+offset.getName()+"("+i+","+j+"));\n";
                            }
                        }
                        break;
                    case MATRIX:
                        for(int i=1; i<=ops.getHeight(); i++) {
                            for(int j=1; j<=ops.getWidth(); j++) {
                                outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+"sign("+signal.getName()+"("+i+","+j+"))*("+gain.getName()+"("+i+","+j+")*abs("+signal.getName()+"("+i+","+j+"))+"+offset.getName()+"("+i+","+j+"));\n";
                            }
                        }
                        break;
                }
                break;
        }
        code.addOutputCode(outputCode);
    }

    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        prepareContext();
        String initCode = TemplateManager.renderTemplate("c/discontinuous/Coulomb/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code){
        super.generateOutputCodeC(code);
        String outputCode = TemplateManager.renderTemplate("c/discontinuous/Coulomb/output.vm", context);
        code.addOutputCode(outputCode);
    }

     public void updateDimension() throws MatDimException{
        OutputPort out  = outputPortList.get(0);
        InputPort in  = inputPortList.get(0);
        OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if(offset.getWidth()!=gain.getWidth()||offset.getHeight()!=gain.getHeight()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
            throw(e);
        }
        if(offset.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
            out.setHeight(offset.getHeight());
            out.setWidth(offset.getWidth());
            out.getOutputSignalC().setHeight(offset.getHeight());
            out.getOutputSignalC().setWidth(offset.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }
        else if(offset.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        }
        else{
            if(offset.getWidth()!=signal.getWidth()||offset.getHeight()!=signal.getHeight()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the Coulomb dimension!\n \n");
            throw(e);
            }
            out.setHeight(offset.getHeight());
            out.setWidth(offset.getWidth());
            out.getOutputSignalC().setHeight(offset.getHeight());
            out.getOutputSignalC().setWidth(offset.getWidth());
            out.getOutputSignalC().setDataType(offset.getDataType());
        }
    }
    public void checkDimension() throws MatDimException{
    }
}
