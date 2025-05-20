package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.DataType;
import org.apache.velocity.VelocityContext;
import lombok.Getter;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class DetectIncrease extends com.ncslab.block.Block{



    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }


	VelocityContext context;

	public DetectIncrease(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);
		inputPortList.add(new InputPort(this, 1));
		outputPortList.add(new OutputPort(this, 1, true));

		context = new VelocityContext();
		context.put("block", this);
		context.put("realDataType", DataType.REAL);
	}

	public void generateArraysCodeC(CodeStructC code) {
		try {
			OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			context.put("signal", signal);
			String codeStr = TemplateManager.renderTemplate("c/logicAndBit/DetectIncrease/arrays.vm", context);
			code.addArraysCode(codeStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		try {
			String templatePath = "c/logicAndBit/DetectIncrease/init.vm";
			String codeStr = TemplateManager.renderTemplate(templatePath, context);
			code.addInitCode(codeStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateOutputCodeC(CodeStructC code) {
		try {
			String templatePath = "c/logicAndBit/DetectIncrease/output.vm";
			String codeStr = TemplateManager.renderTemplate(templatePath, context);
			code.addOutputCode(codeStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
