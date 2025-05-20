package com.ncslab.block.route;

import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.MatDimException;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Demux extends Block {
	private int num;
	private boolean feedThrough = true;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // Output names are dynamic based on parameter
        inputNames.add("in1");
    }

	public Demux(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		this.feedThrough = true;
		this.num = paramValues.getInt("Outputs");

		// Create output ports based on parameter
		for(int i=0; i<num; i++) {
			outputPortList.add(new OutputPort(this, i+1, feedThrough));
		}
		inputPortList.add(new InputPort(this, 1));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("outputs", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("m/route/Demux/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("inputs", getInputPortVariables());
		context.put("outputs", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("m/route/Demux/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);

		String codeStr = TemplateManager.renderTemplate("m/route/Demux/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("inputs", getInputPortVariables());
		context.put("outputs", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}

	public void generateUpdateCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);

		String codeStr = TemplateManager.renderTemplate("c/route/Demux/update.vm", context);
		code.addUpdateCode(codeStr);
	}

	public void updateDimension() throws MatDimException {
		// Implementation left as is
	}

	public void checkDimension() throws MatDimException {
		if(this.getInputPortList().get(0).isVector()==false||this.getInputPortList().get(0).isReal()==true) {
			MatDimException e=new MatDimException("Block "+this.blockName+" input dimension error!\n Only a vector is applicable for demux\n");
			throw(e);
		}

		if(this.getInputPortList().get(0).getVectorSize()!=num) {
			MatDimException e=new MatDimException("Block "+this.blockName+" output dimension error!\n The input signal width is "+this.getInputPortList().get(0).getVectorSize()+", but the number of output is "+num+"\n");
			throw(e);
		}
	}
}
