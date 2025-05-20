package com.ncslab.block.math;

import com.ncslab.block.io.Parameter;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class TrigFunction extends Block{
	Parameter trigFunc;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        //输入待根据循环确定
        parameterNames.add("TrigonometricFunction");
    }

    public TrigFunction(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,true));
		//����һ������
		inputPortList.add(new InputPort(this,1));
        String trigFuncString;
        if(paramValues.has("Function")) {
            trigFuncString = paramValues.getString("Function");
        }else{
            trigFuncString = paramValues.getString("TrigonometricFunction");
        }
        trigFunc=new Parameter(this,1,"TrigonometricFunction",trigFuncString);
        parameterList.add(trigFunc);

        if(trigFunc.getInitString().equals("atan2")) {
            inputPortList.add(new InputPort(this,2));
        }
	}
    public void generateOutputCodeC(CodeStructC code) {
        String outputCode="/*Code for output of block Tr:("+getBlockId()+")"+getBlockName()+"*/\n";
        OutputPort out  = outputPortList.get(0);
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        switch(signal.getDataType()) {
            case REAL:
                if(!trigFunc.getInitString().equals("atan2")) {
                    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+trigFunc.getInitString()+"("+signal.getName()+");\n";
                }else {
                    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+trigFunc.getInitString()+"("+signal.getName()+","+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+");\n";
                }
                break;
            case MATRIX:
                if(!trigFunc.getInitString().equals("atan2")) {
                    for(int i=0;i<signal.getHeight();i++) {
                        for(int j=0;j<signal.getWidth();j++) {
                            outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+trigFunc.getInitString()+"("+signal.getName()+"("+i+","+j+"));\n";
                        }
                    }
                }else {
                    for(int i=0;i<signal.getHeight();i++) {
                        for(int j=0;j<signal.getWidth();j++) {
                            outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+trigFunc.getInitString()+"("+signal.getName()+","+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+i+","+j+"));\n";
                        }
                    }
                }
                break;
        }
        code.addOutputCode(outputCode);
    }
	public void updateDimension() throws MatDimException{
		if(inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getDataType()==DataType.MATRIX) {
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
	   }
	}
    public void checkDimension() throws MatDimException{

	}

}

