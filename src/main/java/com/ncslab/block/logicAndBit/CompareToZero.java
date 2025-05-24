package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import Jama.Matrix;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class CompareToZero extends Block{

    String relop;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }

	public CompareToZero(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));

        relop=paramValues.getString("relop");
        if(relop.equals("~=")) {
            relop = "!=";
        }
    }

    @Override
    public void calculateInit() {
        // Initialization logic for CompareToZero block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                resultData = new Data(compare(inputData.getInitValue(), relop));
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        matrixResult.set(i, j, compare(inputData.getMatrix().get(i, j), relop));
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private double compare(double inputValue, String operator) {
        switch (operator) {
            case "==":
                return inputValue == 0 ? 1.0 : 0.0;
            case "!=":
                return inputValue != 0 ? 1.0 : 0.0;
            case "<":
                return inputValue < 0 ? 1.0 : 0.0;
            case "<=":
                return inputValue <= 0 ? 1.0 : 0.0;
            case ">":
                return inputValue > 0 ? 1.0 : 0.0;
            case ">=":
                return inputValue >= 0 ? 1.0 : 0.0;
            default:
                return 0.0;
        }
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode="/*Code for initialization of block Compare To Zero:("+getBlockId()+")"+getBlockName()+"*/\n";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode="/*Code for output of block Compare To Zreo:("+getBlockId()+")"+getBlockName()+"*/\n";
        OutputPort out  = outputPortList.get(0);
        OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();


        switch(signal.getDataType()) {
        case REAL:
            outputCode+="if("+signal.getName()+relop+"0) {\n";
            outputCode+=out.getOutputSignalC().getName()+"= 1.0;}else{\n";
            outputCode+=out.getOutputSignalC().getName()+"= 0.0;}\n";
            break;
        case MATRIX:
            for(int i=0; i < signal.getHeight(); i++) {
                for(int j=0; j < signal.getWidth(); j++) {
                    outputCode+="if("+signal.getName()+"("+i+","+j+")"+relop+"0) {\n";
                    outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 1.0;}else{\n";
                    outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 0.0;}\n";
                   }
               }
            break;
        }
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException{
        OutputPort out  = outputPortList.get(0);
        InputPort in  = inputPortList.get(0);
        OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
   }

    public void checkDimension() throws MatDimException{
    }

}
