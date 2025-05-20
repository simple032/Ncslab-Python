package com.ncslab.block.math;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import java.io.StringWriter;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Abs extends Block{


    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }
	public Abs(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Abs:("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
	    VelocityContext context = new VelocityContext();
	    context.put("blockId", getBlockId());
	    context.put("blockName", getBlockName());
	    context.put("inputPortList", getInputPortList());
	    context.put("outputPortList", getOutputPortList());
	    
	    String codeStr = TemplateManager.renderTemplate("c/math/Abs/output.vm", context);
	    code.addOutputCode(codeStr);
	}
     public void updateDimension() throws MatDimException{
	 }

	public void checkDimension() throws MatDimException{
	}
}
