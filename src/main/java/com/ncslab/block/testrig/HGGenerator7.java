package com.ncslab.block.testrig;

import java.io.InputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

public class HGGenerator7 extends Block {

	public static final List<String> outputNames = new ArrayList<>();
	public static final List<String> inputNames = new ArrayList<>();
	public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

	static {
		outputNames.add("Id");
		outputNames.add("Iq");
		outputNames.add("Theta");
		outputNames.add("Omega");
		inputNames.add("C1");
		inputNames.add("Efd");
		inputNames.add("Ud");
		inputNames.add("Uq");

		PARAMETER_DEFAULTS.put("Omega0", "314.15926");
		PARAMETER_DEFAULTS.put("Xd", "1.346");
		PARAMETER_DEFAULTS.put("Xq", "0.940");
		PARAMETER_DEFAULTS.put("Xdd", "0.446");
		PARAMETER_DEFAULTS.put("Xddd", "0.330");
		PARAMETER_DEFAULTS.put("Xqqq", "0.370");
		PARAMETER_DEFAULTS.put("X1", "0.243");
		PARAMETER_DEFAULTS.put("Td0", "1.660");
		PARAMETER_DEFAULTS.put("Td000", "0.118");
		PARAMETER_DEFAULTS.put("Tq000", "0.035");
		PARAMETER_DEFAULTS.put("H", "1.2");
		PARAMETER_DEFAULTS.put("R", "0.006");
	}

	public static List<String> getInputNames() {
		return inputNames;
	}

	public static List<String> getOutputNames() {
		return outputNames;
	}

	Parameter cParaOmega0;
	Parameter cParaXd;
	Parameter cParaXq;
	Parameter cParaXdd;
	Parameter cParaXddd;
	Parameter cParaXqqq;
	Parameter cParaX1;
	Parameter cParaTd0;
	Parameter cParaTd000;
	Parameter cParaTq000;
	Parameter cParaH;
	Parameter cParaR;
	
	
	Parameter cParaRQ;
	Parameter cParaRF;
	Parameter cParaRD;
	
	Parameter cParaXl;
	Parameter cParaLd;
	Parameter cParaLq;
	Parameter cParaLAD;
	Parameter cParaLAQ;
	Parameter cParaLQ;
	Parameter cParaLF;
	Parameter cParaLD;
	
	Parameter cParaLMD;
	Parameter cParaLMQ;
	
	InputPort inC1=new InputPort(this,1,"C1");
	InputPort inEfd=new InputPort(this,2,"Efd");
	InputPort inUd=new InputPort(this,3,"Ud");
	InputPort inUq=new InputPort(this,4,"Uq");
	
	OutputPort outId=new OutputPort(this,"Id",1,false);
	OutputPort outIq=new OutputPort(this,"Iq",2,false);
	OutputPort outTheta=new OutputPort(this,"Theta",3,false);
	OutputPort outOmega=new OutputPort(this,"Omega",4,false);
	
	
	State stateTheta=new State(this,1,"Theta");
	State stateOmega=new State(this,2,"Omega");
	State statePsiq=new State(this,3,"Psiq");
	State statePsiQ=new State(this,4,"PsiQ");
	State statePsid=new State(this,5,"Psid");
	State statePsiD=new State(this,6,"PsiD");
	State statePsiF=new State(this,7,"PsiF");
	
	/**
	 * DTO-NATIVE Constructor - Creates HGGenerator7 block directly from BlockDto DTO
	 */
	public HGGenerator7(BlockDto blockDto, NCSLabModel model) {
		super(blockDto, model);
		initializeBlock();
		System.out.println("DTO-NATIVE: HGGenerator7 block created successfully - " + blockDto.getBlockName());
	}

	public HGGenerator7(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);
		initializeBlock();
	}

	private void initializeBlock() {
		inputPortList.add(inC1);
		inputPortList.add(inEfd);
		inputPortList.add(inUd);
		inputPortList.add(inUq);

		outputPortList.add(outId);
		outputPortList.add(outIq);
		outputPortList.add(outTheta);
		outputPortList.add(outOmega);

		// Only add parameters not already created by Block.parseParameterList()
		cParaOmega0 = addParameterIfAbsent("Omega0", PARAMETER_DEFAULTS.get("Omega0"));
		cParaXd = addParameterIfAbsent("Xd", PARAMETER_DEFAULTS.get("Xd"));
		cParaXq = addParameterIfAbsent("Xq", PARAMETER_DEFAULTS.get("Xq"));
		cParaXdd = addParameterIfAbsent("Xdd", PARAMETER_DEFAULTS.get("Xdd"));
		cParaXddd = addParameterIfAbsent("Xddd", PARAMETER_DEFAULTS.get("Xddd"));
		cParaXqqq = addParameterIfAbsent("Xqqq", PARAMETER_DEFAULTS.get("Xqqq"));
		cParaX1 = addParameterIfAbsent("X1", PARAMETER_DEFAULTS.get("X1"));
		cParaTd0 = addParameterIfAbsent("Td0", PARAMETER_DEFAULTS.get("Td0"));
		cParaTd000 = addParameterIfAbsent("Td000", PARAMETER_DEFAULTS.get("Td000"));
		cParaTq000 = addParameterIfAbsent("Tq000", PARAMETER_DEFAULTS.get("Tq000"));
		cParaH = addParameterIfAbsent("H", PARAMETER_DEFAULTS.get("H"));
		cParaR = addParameterIfAbsent("R", PARAMETER_DEFAULTS.get("R"));

		// Derived / internal parameters (not in PARAMETER_DEFAULTS)
		cParaRQ = addParameterIfAbsent("rQ", "0");
		cParaRF = addParameterIfAbsent("rF", "0");
		cParaRD = addParameterIfAbsent("rD", "0");
		cParaXl = addParameterIfAbsent("Xl", "0");
		cParaLd = addParameterIfAbsent("ld", "0");
		cParaLq = addParameterIfAbsent("lq", "0");
		cParaLAD = addParameterIfAbsent("lAD", "0");
		cParaLAQ = addParameterIfAbsent("lAQ", "0");
		cParaLQ = addParameterIfAbsent("lQ", "0");
		cParaLF = addParameterIfAbsent("lF", "0");
		cParaLD = addParameterIfAbsent("lD", "0");
		cParaLMD = addParameterIfAbsent("LMD", "0");
		cParaLMQ = addParameterIfAbsent("LMQ", "0");

		stateList.add(stateTheta);
		stateList.add(stateOmega);
		stateList.add(statePsiq);
		stateList.add(statePsiQ);
		stateList.add(statePsid);
		stateList.add(statePsiD);
		stateList.add(statePsiF);
	}

	private Parameter addParameterIfAbsent(String name, String defaultValue) {
		Parameter existing = getParameterByName(name);
		if (existing != null) {
			return existing;
		}
		String value = paramValues.has(name) ? paramValues.getString(name) : defaultValue;
		Parameter p = new Parameter(this, parameterList.size() + 1, name, value);
		parameterList.add(p);
		return p;
	}
	
	
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String[] keys=context.getKeys();
        for(String key:keys) {
        	System.out.println(key+":\t"+context.get(key));
        }

        String codeStr = TemplateManager.renderTemplate("c/testrig/HGGenerator7/init.vm", context);
        code.addInitCode(codeStr);
	}
	
	//define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block Generator:("+getBlockId()+")"+getBlockName()+"*/\n";
		 
		 code.addArraysCode(arraysCode);
	 }
	 
	 public void  generateDerivativeCodeC(CodeStructC code) {
		 com.ncslab.util.TemplateUtils.populateAllContext(context, this);

	     String derivativeCode = TemplateManager.renderTemplate("c/testrig/HGGenerator7/derivative.vm", context);
	     code.addDerivativeCode(derivativeCode);
	 }
	 
	 public void generateOutputCodeC(CodeStructC code) {
		 com.ncslab.util.TemplateUtils.populateAllContext(context, this);
 
	     String codeStr = TemplateManager.renderTemplate("c/testrig/HGGenerator7/output.vm", context);
	     code.addOutputCode(codeStr);
	 }
}