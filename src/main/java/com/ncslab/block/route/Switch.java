package com.ncslab.block.route;

import com.ncslab.block.Block;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;


import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;

import java.util.Vector;

public class Switch extends Block {
	Parameter threshold;
    Parameter relop;
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
        parameterNames.add("threshold");
    }
	public Switch(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		inputPortList.add(new InputPort(this,3));
		outputPortList.add(new OutputPort(this,1,true));
	    threshold=new Parameter(this,1,"threshold",paramValues.getString("Threshold"));
		parameterList.add(threshold);
        relop=new Parameter(this,2,"relop",paramValues.optString("Relop", ">="));
        parameterList.add(relop);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		context.put("blockId", getBlockId());
		context.put("blockName", getBlockName());
		context.put("threshold", threshold);

		String initCode = TemplateManager.renderTemplate("c/route/Switch/init.vm", context);
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		context.put("blockId", getBlockId());
		context.put("blockName", getBlockName());
		context.put("inputPortList", getInputPortList());
		context.put("outputPortList", getOutputPortList());
		context.put("threshold", threshold);

		String outputCode = TemplateManager.renderTemplate("c/route/Switch/output.vm", context);
		code.addOutputCode(outputCode);
	}
	  public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in1  = inputPortList.get(0);
			InputPort in3  =  inputPortList.get(2);
			OutputSignal signal1=in1.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal3=in3.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal1.getHeight()!=signal3.getHeight()||signal1.getWidth()!=signal3.getWidth()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input1 and input3 dimension doesn't match!\n \n");
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

    @Override
    public void calculateOutput(double t) {
        InputPort in1 = inputPortList.get(0);
        InputPort inctrl = inputPortList.get(1);
        InputPort in2 = inputPortList.get(2);
        OutputPort out = outputPortList.get(0);
        boolean satisfied = false;
        if(relop.getInitString()=="~=") {
            satisfied = inctrl.getData().getInitValue()!=threshold.getData().getInitValue();
        }else if(relop.getInitString()==">=") {
            satisfied = inctrl.getData().getInitValue()>=threshold.getData().getInitValue();
        }else if(relop.getInitString()==">") {
            satisfied = inctrl.getData().getInitValue()>threshold.getData().getInitValue();
        }
        if(satisfied){
            out.setData(in1.getData());
        }else {
            out.setData(in2.getData());
        }
    }
}
