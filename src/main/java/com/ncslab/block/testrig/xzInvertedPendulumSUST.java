package com.ncslab.block.testrig;

import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import org.apache.velocity.VelocityContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class xzInvertedPendulumSUST extends Block {
	private String name = "xzInvertedPendulumSUST";
	Parameter Vspeed;
	Parameter ENAOrDIS;
	Parameter POS0Flag;
//	State speedState;
//	State spState;

    
    
    /**
     * DTO-NATIVE Constructor - Creates xzInvertedPendulumSUST block directly from BlockJson DTO
     */
    public xzInvertedPendulumSUST(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: xzInvertedPendulumSUST block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Vspeed", "1.0");
        PARAMETER_DEFAULTS.put("ENAOrDIS", "1");
        PARAMETER_DEFAULTS.put("POS0Flag", "0");

    }

    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("Real_X");
        outputNames.add("Angle");
        inputNames.add("in1");
        inputNames.add("in2");

    }

	public xzInvertedPendulumSUST(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		outputPortList.add(new OutputPort(this,"Real_X",1,false));
//		outputPortList.add(new OutputPort(this,"AngleSpeed",2,false));
		outputPortList.add(new OutputPort(this,"Angle",2,false));
		//outputPortList.add(new OutputPort(this,"Water_Level",2,false));

//		spState=new State(this,1,"SerialPortState");
//		stateList.add(spState);
		Vspeed=new Parameter(this,parameterList.size()+1,"Vspeed",paramValues.getString("Vspeed"));
		ENAOrDIS=new Parameter(this,parameterList.size()+1,"ENAOrDIS",paramValues.getString("ENAOrDIS"));
		POS0Flag=new Parameter(this,parameterList.size()+1,"POS0Flag",paramValues.getString("POS0Flag"));
		//pumpState=new State(this,1,"pumpState");
		//stateList.add(pumpState);
		//levelState=new State(this,2,"levelState");
		//stateList.add(levelState);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		code.addInitCode(initCode);
	}

	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);

		String derivativeCode="";

		code.addDerivativeCode(derivativeCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

		code.addOutputCode(outputCode);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		context.put("ENAOrDIS", ENAOrDIS);

		String initCode = TemplateManager.renderTemplate("c/testrig/xzInvertedPendulumSUST/init.vm", context);
		code.addInitCode(initCode);
	}

	public void generateIncludeCodeC(CodeStructC code) {
		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
	}

	public void addLine(String originCode, String newLine) {

	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("inputPortVariable1", getInputPortVariable(0));
		context.put("inputPortVariable2", getInputPortVariable(1));
		context.put("outputPortVariable1", outputPortList.get(0).getOutputSignalC().getName());
		context.put("outputPortVariable2", outputPortList.get(1).getOutputSignalC().getName());
		context.put("POS0Flag", POS0Flag);
		context.put("ENAOrDIS", ENAOrDIS);

		String outputCode = TemplateManager.renderTemplate("c/testrig/xzInvertedPendulumSUST/output.vm", context);
		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addDerivativeCode(derivativeCode);
	}

	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = TemplateManager.renderTemplate("c/testrig/xzInvertedPendulumSUST/statement.vm", context);
		code.addStatementCode(statementCode);
	}
}
