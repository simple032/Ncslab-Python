package com.ncslab.block.source;

import com.ncslab.block.Block;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Constant extends Block {

	Parameter value;

	@Getter
	public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

	static {
		parameterNames.add("Value");
        outputNames.add("out1");
	}

	public Constant(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);

		// Initialize parameter
		value = new Parameter(this, 1, getBlockName(), paramValues.getString("Value"));
		parameterList.add(value);
        outputPortList.add(new OutputPort(this, 1, true));
		outputPortList.get(0).setHeight(value.getHeight());
		outputPortList.get(0).setWidth(value.getWidth());
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("value", value);
		
		String codeStr = TemplateManager.renderTemplate("m/source/Constant/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("value", value);
		context.put("outputs", getOutputPortVariables());
		
		String codeStr = TemplateManager.renderTemplate("m/source/Constant/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("value", value);
		
		String codeStr = TemplateManager.renderTemplate("c/source/Constant/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("value", value);
		context.put("outputs", getOutputPortVariables());
		
		String codeStr = TemplateManager.renderTemplate("c/source/Constant/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateUpdateCodePLC(CodeStructC code) {
		// No update code needed for Constant
	}

	public void generateOutputCodePLC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("value", value);
		context.put("outputs", getOutputPortVariables());
		
		String codeStr = TemplateManager.renderTemplate("c/source/Constant/output_plc.vm", context);
		code.addOutputCode(codeStr);
	}
}