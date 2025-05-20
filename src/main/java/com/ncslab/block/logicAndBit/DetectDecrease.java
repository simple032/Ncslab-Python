package com.ncslab.block.logicAndBit;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

public class DetectDecrease extends com.ncslab.block.Block{




    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }

	public DetectDecrease(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
    }

	public void generateArraysCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("realDataType", DataType.REAL);
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		context.put("signal", signal);
		
		String codeStr = TemplateManager.renderTemplate("c/logicAndBit/DetectDecrease/arrays.vm", context);
		code.addArraysCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Detect Decrease:("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("realDataType", DataType.REAL);
		context.put("inputs", getInputPortVariables());
		context.put("outputs", getOutputPortVariables());
		
		String codeStr = TemplateManager.renderTemplate("c/logicAndBit/DetectDecrease/output.vm", context);
		code.addOutputCode(codeStr);
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
