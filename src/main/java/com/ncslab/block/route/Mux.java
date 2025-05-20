package com.ncslab.block.route;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;
import java.util.List;

public class Mux extends Block {
	private int num;

	private boolean feedThrough = true;


    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        //输入个数不确定
    }

	public Mux(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		this.num = Integer.parseInt(paramValues.getString("Inputs"));

		// Create input ports based on parameter
		for(int i=0; i<num; i++) {
			inputPortList.add(new InputPort(this, i+1));
		}
		outputPortList.add(new OutputPort(this, 1, feedThrough));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("outputs", getOutputPortVariables());
		
		String codeStr = TemplateManager.renderTemplate("m/route/Mux/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("inputs", getInputPortVariables());
		context.put("outputs", getOutputPortVariables());
		
		String codeStr = TemplateManager.renderTemplate("m/route/Mux/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		
		String codeStr = TemplateManager.renderTemplate("c/route/Mux/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("inputs", inputPortList);
		context.put("outputs", getOutputPortVariables());
		
		String codeStr = TemplateManager.renderTemplate("c/route/Mux/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		
		String codeStr = TemplateManager.renderTemplate("c/route/Mux/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}
	
	public void generateUpdateCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		
		String codeStr = TemplateManager.renderTemplate("c/route/Mux/update.vm", context);
		code.addUpdateCode(codeStr);
	}

	public void updateDimension() throws MatDimException {
		//super.updateDimension();
		int size=0;
		for(InputPort inputPort:inputPortList) {
			if(inputPort.isVector()==true||inputPort.isReal()==true) {
				size+=inputPort.getVectorSize();
			}
			else {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension error!\n Matrix is not applicable for demux\n");
				throw(e);
			}
		}

		OutputPort output=getOutputPortList().get(0);
		output.setHeight(size);
		output.setWidth(1);
		output.getOutputSignalC().setHeight(size);
		output.getOutputSignalC().setWidth(1);
		output.getOutputSignalC().setDataType(DataType.MATRIX);
	}

	public void checkDimension() throws MatDimException {
		// Implementation can be added if needed
	}
}