package com.ncslab.block.source;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

import java.util.Vector;

public class Ramp extends Block {
	    Parameter slope;
	    Parameter start;
	    Parameter initial_output;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");

        parameterNames.add("slope");
        parameterNames.add("start");
        parameterNames.add("initial_output");
    }
	public Ramp(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
		slope=new Parameter(this,1,"slope",paramValues.getString("slope"));
		start=new Parameter(this,2,"start",paramValues.getString("start"));
		initial_output=new Parameter(this,3,"initial_output",paramValues.getString("X0"));
		parameterList.add(slope);
		parameterList.add(start);
		parameterList.add(initial_output);
		outputPortList.get(0).setHeight(slope.getHeight());
		outputPortList.get(0).setWidth(slope.getWidth());
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=slope.getInitCodeM();
		initCode+=start.getInitCodeM();
		initCode+=initial_output.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("outputs", getOutputPortVariables());
		context.put("slope", slope);
		context.put("start", start);
		context.put("initial_output", initial_output);

		String codeStr = TemplateManager.renderTemplate("m/source/Ramp/output.vm", context);
		code.addOutputCode(codeStr);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Ramp:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=slope.getInitCodeC();
		initCode+=start.getInitCodeC();
		initCode+=initial_output.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
        OutputSignal signal = outputPortList.get(0).getOutputSignalC();
		context.put("block", this);
        context.put("realDataType", DataType.REAL);
		context.put("outputs", getOutputPortVariables());
		context.put("signal", signal);
		context.put("slope", slope);
        context.put("slopeValue", slope.getDataString());
		context.put("start", start.getInitString());
		context.put("slopeHeightIndex", slope.getHeight() - 1);
		context.put("slopeWidthIndex", slope.getWidth() - 1);
		context.put("initial_output", initial_output.getInitString());

		String codeStr = TemplateManager.renderTemplate("c/source/Ramp/output.vm", context);
		code.addOutputCode(codeStr);
	}

	 public void updateDimension() throws MatDimException{
	    	if(slope.getWidth()!=initial_output.getWidth()
	    			||slope.getWidth()!=start.getWidth()
	    			||slope.getHeight()!=start.getHeight()
	    			||slope.getHeight()!=initial_output.getHeight()) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
	    	}
	    }
}
