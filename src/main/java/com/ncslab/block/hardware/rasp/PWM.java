package com.ncslab.block.hardware.rasp;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class PWM extends com.ncslab.block.Block{

	Parameter port;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();


    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        parameterNames.add("port");
        inputNames.add("in1");
    }
	public PWM(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		inputPortList.add(new InputPort(this,1));

		port=new Parameter(this,1,"port",paramValues.getString("port"));
		parameterList.add(port);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode=port.getName()+"="+paramValues.getDouble("port")+";\n";

		// Removed unused initCode reference
		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode=outputPortList.get(0).getOutputSignalC().getName()+"="+port.getName()+";\n";

		// Removed unused outputCode reference
		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		context.put("block", this);
	
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/rasp/PWM/init.vm", context));


		// Removed unused initCode reference
	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
	
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/rasp/PWM/output.vm", context));

		// Removed unused outputCode reference
	}
}
