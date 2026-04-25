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

public class HGGenerator12 extends Block {

	public static final List<String> outputNames = new ArrayList<>();
	public static final List<String> inputNames = new ArrayList<>();
	public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

	static {
		outputNames.add("ea1");
		outputNames.add("eb1");
		outputNames.add("ec1");
		outputNames.add("ea2");
		outputNames.add("eb2");
		outputNames.add("ec2");
		outputNames.add("ea3");
		outputNames.add("eb3");
		outputNames.add("ec3");
		outputNames.add("ea4");
		outputNames.add("eb4");
		outputNames.add("ec4");
		outputNames.add("omega");

		inputNames.add("C1");
		inputNames.add("Efd");
		inputNames.add("Ia1");
		inputNames.add("Ib1");
		inputNames.add("Ic1");
		inputNames.add("Ia2");
		inputNames.add("Ib2");
		inputNames.add("Ic2");
		inputNames.add("Ia3");
		inputNames.add("Ib3");
		inputNames.add("Ic3");
		inputNames.add("Ia4");
		inputNames.add("Ib4");
		inputNames.add("Ic4");

		PARAMETER_DEFAULTS.put("Xd", "1.5");
		PARAMETER_DEFAULTS.put("Xdd", "0.3");
		PARAMETER_DEFAULTS.put("Xddd", "0.2");
		PARAMETER_DEFAULTS.put("Xq", "1.0");
		PARAMETER_DEFAULTS.put("Xqq", "0.5");
		PARAMETER_DEFAULTS.put("Xqqq", "0.25");
		PARAMETER_DEFAULTS.put("Xl", "0.15");
		PARAMETER_DEFAULTS.put("Td0", "5.0");
		PARAMETER_DEFAULTS.put("Td000", "0.1");
		PARAMETER_DEFAULTS.put("Tq0", "1.0");
		PARAMETER_DEFAULTS.put("Tq000", "0.05");
		PARAMETER_DEFAULTS.put("Rs", "0.005");
		PARAMETER_DEFAULTS.put("fn", "50");
		PARAMETER_DEFAULTS.put("th", "0");
		PARAMETER_DEFAULTS.put("pha", "0");
	}

	public static List<String> getInputNames() {
		return inputNames;
	}

	public static List<String> getOutputNames() {
		return outputNames;
	}

	Parameter cParaXd;
	Parameter cParaXdd;
	Parameter cParaXddd;
	Parameter cParaXq;
	Parameter cParaXqq;
	Parameter cParaXqqq;
	Parameter cParaXl;
	Parameter cParaTd0;
	Parameter cParaTd000;
	Parameter cParaTq0;
	Parameter cParaTq000;
	Parameter cParaRs;
	Parameter cParafn;
	Parameter cParath;
	Parameter cParapha;

	Parameter cParaXad;
	Parameter cParaXaq;
	Parameter cParaXfl;
	Parameter cParaXDl;
	Parameter cParaXQ1l;
	Parameter cParaXQ2l;
	Parameter cParaXQ2l1;
	Parameter cParaXQ2l2;
	Parameter cParaomega0;
	Parameter cParaRf;
	Parameter cParaRD;
	Parameter cParaRQ1;
	Parameter cParaRQ2;
	Parameter cParaRQ2v;
	Parameter cParaXadd;
	Parameter cParaXaqq;
	Parameter cParakf;
	Parameter cParakD;
	Parameter cParakQ1;
	Parameter cParakQ22;

	InputPort inC1 = new InputPort(this, 1, "C1");
	InputPort inEfd = new InputPort(this, 2, "Efd");
	InputPort inIa1 = new InputPort(this, 3, "Ia1");
	InputPort inIb1 = new InputPort(this, 4, "Ib1");
	InputPort inIc1 = new InputPort(this, 5, "Ic1");
	InputPort inIa2 = new InputPort(this, 6, "Ia2");
	InputPort inIb2 = new InputPort(this, 7, "Ib2");
	InputPort inIc2 = new InputPort(this, 8, "Ic2");
	InputPort inIa3 = new InputPort(this, 9, "Ia3");
	InputPort inIb3 = new InputPort(this, 10, "Ib3");
	InputPort inIc3 = new InputPort(this, 11, "Ic3");
	InputPort inIa4 = new InputPort(this, 12, "Ia4");
	InputPort inIb4 = new InputPort(this, 13, "Ib4");
	InputPort inIc4 = new InputPort(this, 14, "Ic4");

	OutputPort outea1 = new OutputPort(this, "ea1", 1, false);
	OutputPort outeb1 = new OutputPort(this, "eb1", 2, false);
	OutputPort outec1 = new OutputPort(this, "ec1", 3, false);
	OutputPort outea2 = new OutputPort(this, "ea2", 4, false);
	OutputPort outeb2 = new OutputPort(this, "eb2", 5, false);
	OutputPort outec2 = new OutputPort(this, "ec2", 6, false);
	OutputPort outea3 = new OutputPort(this, "ea3", 7, false);
	OutputPort outeb3 = new OutputPort(this, "eb3", 8, false);
	OutputPort outec3 = new OutputPort(this, "ec3", 9, false);
	OutputPort outea4 = new OutputPort(this, "ea4", 10, false);
	OutputPort outeb4 = new OutputPort(this, "eb4", 11, false);
	OutputPort outec4 = new OutputPort(this, "ec4", 12, false);
	OutputPort outomega = new OutputPort(this, "omega", 13, false);

	State statepsif = new State(this, 1, "psif");
	State statepsiD = new State(this, 2, "psiD");
	State statepsiQ1 = new State(this, 3, "psiQ1");
	State statepsiQ21 = new State(this, 4, "psiQ21");
	State statepsiQ22 = new State(this, 5, "psiQ22");
	State statetheta = new State(this, 6, "theta");
	State stateomega = new State(this, 7, "omega");

	/**
	 * DTO-NATIVE Constructor - Creates HGGenerator12 block directly from BlockDto DTO
	 */
	public HGGenerator12(BlockDto blockDto, NCSLabModel model) {
		super(blockDto, model);
		initializeBlock();
		System.out.println("DTO-NATIVE: HGGenerator12 block created successfully - " + blockDto.getBlockName());
	}

	public HGGenerator12(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);
		initializeBlock();
	}

	private void initializeBlock() {
		inputPortList.add(inC1);
		inputPortList.add(inEfd);
		inputPortList.add(inIa1);
		inputPortList.add(inIb1);
		inputPortList.add(inIc1);
		inputPortList.add(inIa2);
		inputPortList.add(inIb2);
		inputPortList.add(inIc2);
		inputPortList.add(inIa3);
		inputPortList.add(inIb3);
		inputPortList.add(inIc3);
		inputPortList.add(inIa4);
		inputPortList.add(inIb4);
		inputPortList.add(inIc4);

		outputPortList.add(outea1);
		outputPortList.add(outeb1);
		outputPortList.add(outec1);
		outputPortList.add(outea2);
		outputPortList.add(outeb2);
		outputPortList.add(outec2);
		outputPortList.add(outea3);
		outputPortList.add(outeb3);
		outputPortList.add(outec3);
		outputPortList.add(outea4);
		outputPortList.add(outeb4);
		outputPortList.add(outec4);
		outputPortList.add(outomega);

		cParaXd = addParameterIfAbsent("Xd", PARAMETER_DEFAULTS.get("Xd"));
		cParaXdd = addParameterIfAbsent("Xdd", PARAMETER_DEFAULTS.get("Xdd"));
		cParaXddd = addParameterIfAbsent("Xddd", PARAMETER_DEFAULTS.get("Xddd"));
		cParaXq = addParameterIfAbsent("Xq", PARAMETER_DEFAULTS.get("Xq"));
		cParaXqq = addParameterIfAbsent("Xqq", PARAMETER_DEFAULTS.get("Xqq"));
		cParaXqqq = addParameterIfAbsent("Xqqq", PARAMETER_DEFAULTS.get("Xqqq"));
		cParaXl = addParameterIfAbsent("Xl", PARAMETER_DEFAULTS.get("Xl"));
		cParaTd0 = addParameterIfAbsent("Td0", PARAMETER_DEFAULTS.get("Td0"));
		cParaTd000 = addParameterIfAbsent("Td000", PARAMETER_DEFAULTS.get("Td000"));
		cParaTq0 = addParameterIfAbsent("Tq0", PARAMETER_DEFAULTS.get("Tq0"));
		cParaTq000 = addParameterIfAbsent("Tq000", PARAMETER_DEFAULTS.get("Tq000"));
		cParaRs = addParameterIfAbsent("Rs", PARAMETER_DEFAULTS.get("Rs"));
		cParafn = addParameterIfAbsent("fn", PARAMETER_DEFAULTS.get("fn"));
		cParath = addParameterIfAbsent("th", PARAMETER_DEFAULTS.get("th"));
		cParapha = addParameterIfAbsent("pha", PARAMETER_DEFAULTS.get("pha"));

		stateList.add(statepsif);
		stateList.add(statepsiD);
		stateList.add(statepsiQ1);
		stateList.add(statepsiQ21);
		stateList.add(statepsiQ22);
		stateList.add(statetheta);
		stateList.add(stateomega);
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

		String[] keys = context.getKeys();
		for (String key : keys) {
			System.out.println(key + ":\t" + context.get(key));
		}

		String codeStr = TemplateManager.renderTemplate("c/testrig/HGGenerator12/init.vm", context);
		code.addInitCode(codeStr);
	}

	// define arrays to save data
	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block Generator:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		code.addArraysCode(arraysCode);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String derivativeCode = TemplateManager.renderTemplate("c/testrig/HGGenerator12/derivative.vm", context);
		code.addDerivativeCode(derivativeCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);

		String codeStr = TemplateManager.renderTemplate("c/testrig/HGGenerator12/output.vm", context);
		code.addOutputCode(codeStr);
	}
}
