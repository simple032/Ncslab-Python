package com.ncslab.block.source;

import lombok.Getter;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

import com.ncslab.util.TemplateManager;

public class Clock extends com.ncslab.block.Block{

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");

    }
	public Clock(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		outputPortList.add(new OutputPort(this,1,false));
	}
	public void generateOutputCodeC(CodeStructC code) {
		super.generateOutputCodeC(code);
        VelocityContext context = new VelocityContext();
        context.put("block", this);
        context.put("outputVar", outputPortList.get(0).getOutputSignalC().getName());
        String outputCode=TemplateManager.renderTemplate("c/source/Clock/output.vm", context);
		code.addOutputCode(outputCode);
	}
    public void updateDimension() throws MatDimException{

    }
	public void checkDimension() throws MatDimException{

	}
}
