package com.ncslab.block.source;

import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

import java.util.Vector;

public class RepeatingSequence extends Block {
	Parameter rep_seq_t;
	Parameter rep_seq_y;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");

        parameterNames.add("rep_seq_t");
        parameterNames.add("rep_seq_y");
    }

	public RepeatingSequence(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
		rep_seq_t=new Parameter(this,1,"rep_seq_t",paramValues.getString("rep_seq_t"));
		rep_seq_y=new Parameter(this,2,"rep_seq_y",paramValues.getString("rep_seq_y"));
		parameterList.add(rep_seq_t);
		parameterList.add(rep_seq_y);
		outputPortList.get(0).setHeight(1);
		outputPortList.get(0).setWidth(1);
	  }
	//define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
         VelocityContext context = new VelocityContext();
         context.put("block", this);
         context.put("rep_seq_t", rep_seq_t);

         String codeStr = TemplateManager.renderTemplate("c/source/RepeatingSequence/arrays.vm", context);
         code.addArraysCode(codeStr);
	 }
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("rep_seq_t", rep_seq_t);
		context.put("rep_seq_y", rep_seq_y);

		String codeStr = TemplateManager.renderTemplate("c/source/RepeatingSequence/init.vm", context);
		code.addInitCode(codeStr);
	}
	public void generateOutputCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
		context.put("outputs", getOutputPortVariables());
		context.put("rep_seq_t", rep_seq_t);
		context.put("rep_seq_y", rep_seq_y);

		String codeStr = TemplateManager.renderTemplate("c/source/RepeatingSequence/output.vm", context);
		code.addOutputCode(codeStr);
	}
	 public void updateDimension() throws MatDimException{
	    	if(rep_seq_t.getWidth()!=rep_seq_y.getWidth()) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
	    	}
	    	if(rep_seq_t.getHeight()!=1||rep_seq_y.getHeight()!=1||rep_seq_t.getWidth()==1||rep_seq_y.getWidth()==1) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions must be 1*n!");
				throw(e);
	    	}
	    	if(rep_seq_t.getDataType()==DataType.REAL||rep_seq_y.getDataType()==DataType.REAL) {
		    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions must be 1*n!");
					throw(e);
	    	}
	    }
}
