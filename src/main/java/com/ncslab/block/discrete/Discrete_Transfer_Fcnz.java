package com.ncslab.block.discrete;

import com.ncslab.block.io.Parameter;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Discrete_Transfer_Fcnz extends DiscreteBlock{

    Parameter sampleTime;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
    }

	public Discrete_Transfer_Fcnz(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		inputPortList.add(new InputPort(this,3));
		outputPortList.add(new OutputPort(this,1,feedthrough));

        sampleTime = new Parameter(this, 1, "SampleTime", String.valueOf(paramValues.optDouble("SampleTime", -1)));
        parameterList.add(sampleTime);
        setSampleTime(sampleTime);
	}
	//define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal3 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal1", signal1);
        context.put("signal3", signal3);

        String arraysCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("sampleTime", sampleTime);

        String initCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal3 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("inputPortList", inputPortList);
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("signal1", signal1);
        context.put("signal2", signal2);
        context.put("signal3", signal3);

        String outputCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/output.vm", context);
        code.addOutputCode(outputCode);
    }

	 public void updateDimension() throws MatDimException{
		 super.updateDimension();
			OutputPort out  = outputPortList.get(0);
			OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal3=inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal2.getHeight()!=1||signal3.getHeight()!=1) {
				 MatDimException e=new MatDimException("The input port2 signal and input port3 signal of"+this.blockName+"must be Matrix(1*n)!\n");
				 throw(e);
			}
			if(signal2.getWidth()>signal3.getWidth()) {
				MatDimException e=new MatDimException("The order of the denominator must be greater than or equal to the order of the numerator.\n");
				 throw(e);
			}
			out.setHeight(signal1.getHeight());
			out.setWidth(signal1.getWidth());
			out.getOutputSignalC().setHeight(signal1.getHeight());
			out.getOutputSignalC().setWidth(signal1.getWidth());
			out.getOutputSignalC().setDataType(signal1.getDataType());
	}
	 public void checkDimension() throws MatDimException{

    }


}
