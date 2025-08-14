package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.ncslab.util.TemplateManager;


public class Kirchhoff extends Block {

	Parameter BCM;
	Parameter AD1;
	Parameter AD2;
	Parameter AD3;
	Parameter AD4;
	Parameter AD5;
	Parameter AD6;
	Parameter AD7;


    private static final Vector<String> outputNames = new Vector<>();
    private static final Vector<String> inputNames = new Vector<>();

    private static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {

        outputNames.add("AD1");
        outputNames.add("AD2");
        outputNames.add("AD3");
        outputNames.add("AD4");
        outputNames.add("AD5");
        outputNames.add("AD6");
        outputNames.add("AD7");
        inputNames.add("in1");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("BCM", "18");
        PARAMETER_DEFAULTS.put("AD1", "0");
        PARAMETER_DEFAULTS.put("AD2", "1");
        PARAMETER_DEFAULTS.put("AD3", "2");
        PARAMETER_DEFAULTS.put("AD4", "3");
        PARAMETER_DEFAULTS.put("AD5", "4");
        PARAMETER_DEFAULTS.put("AD6", "5");
        PARAMETER_DEFAULTS.put("AD7", "6");

    }

	public Kirchhoff(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);

		// 一个输入
		inputPortList.add(new InputPort(this, 1));
		BCM = new Parameter(this, 1, "BCM", paramValues.getString("BCM"));
		// 七个输出
		outputPortList.add(new OutputPort(this, "AD1", 1, false));
		AD1 = new Parameter(this, 2, "AD1", paramValues.getString("AD1"));
		outputPortList.add(new OutputPort(this, "AD2", 2, false));
		AD2 = new Parameter(this, 3, "AD2", paramValues.getString("AD2"));
		outputPortList.add(new OutputPort(this, "AD3", 3, false));
		AD3 = new Parameter(this, 4, "AD3", paramValues.getString("AD3"));
		outputPortList.add(new OutputPort(this, "AD4", 4, false));
		AD4 = new Parameter(this, 5, "AD4", paramValues.getString("AD4"));
		outputPortList.add(new OutputPort(this, "AD5", 5, false));
		AD5 = new Parameter(this, 6, "AD5", paramValues.getString("AD5"));
		outputPortList.add(new OutputPort(this, "AD6", 6, false));
		AD6 = new Parameter(this, 7, "AD6", paramValues.getString("AD6"));
		outputPortList.add(new OutputPort(this, "AD7", 7, false));
		AD7 = new Parameter(this, 8, "AD7", paramValues.getString("AD7"));

	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode = "";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode = "";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		java.util.List<Parameter> adParameters = java.util.Arrays.asList(AD1, AD2, AD3, AD4, AD5, AD6, AD7);
		
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		context.put("adParameters", adParameters);
		context.put("bcmParameter", BCM);
		
		String codeStr = TemplateManager.renderTemplate("c/testrig/Kirchhoff/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

	    String codeStr = TemplateManager.renderTemplate("c/testrig/Kirchhoff/output.vm", context);
	    code.addOutputCode(codeStr);
	}

	private Vector<State> getStates() {
	    return stateList;
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		
		String codeStr = TemplateManager.renderTemplate("c/testrig/Kirchhoff/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}

}
