package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.BangbangSwingUpInvertedPendulumSUSTDto;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class BangbangSwingUpInvertedPendulumSUST extends Block {
	private String name = "BangbangSwingUpInvertedPendulumSUST";
	Parameter v,vel;


    
    
    /**
     * DTO-NATIVE Constructor - Creates BangbangSwingUpInvertedPendulumSUST block directly from BlockDto DTO
     */
    public BangbangSwingUpInvertedPendulumSUST(BangbangSwingUpInvertedPendulumSUSTDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: BangbangSwingUpInvertedPendulumSUST block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {

        outputNames.add("AccOutput");
        outputNames.add("SpeedOutput");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
        inputNames.add("in4");
        inputNames.add("in5");
        
        // Parameter defaults
        PARAMETER_DEFAULTS.put("v", "0");
        PARAMETER_DEFAULTS.put("vel", "0");
    }
	public BangbangSwingUpInvertedPendulumSUST(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));//	Exe Flag
		inputPortList.add(new InputPort(this,2));//Angle (degree)
		inputPortList.add(new InputPort(this,3));//dif-Angle
		inputPortList.add(new InputPort(this,4));//v
		inputPortList.add(new InputPort(this,5));//vel
		outputPortList.add(new OutputPort(this,"AccOutput",1,false));
		outputPortList.add(new OutputPort(this,"SpeedOutput",2,false));
		v=new Parameter(this,parameterList.size()+1,"v",paramValues.getString("v"));

		vel=new Parameter(this,parameterList.size()+1,"vel",paramValues.getString("vel"));

		//pumpState=new State(this,1,"pumpState");
		//stateList.add(pumpState);
		//levelState=new State(this,2,"levelState");
		//stateList.add(levelState);
	}
	public void generateIncludeCodeC(CodeStructC code) {
		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
	}

	public void addLine(String originCode, String newLine) {

	}
	public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

	    String codeStr = TemplateManager.renderTemplate("c/testrig/BangbangSwingUpInvertedPendulumSUST/output.vm", context);
	    code.addOutputCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
	    super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

	    String codeStr = TemplateManager.renderTemplate("c/testrig/BangbangSwingUpInvertedPendulumSUST/init.vm", context);
	    code.addInitCode(codeStr);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addDerivativeCode(derivativeCode);
	}

	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		statementCode +="float velbangbang"+getBlockId()+"=0;\n";
		code.addStatementCode(statementCode);
	}
}
