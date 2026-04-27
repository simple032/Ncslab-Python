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

public class ACDCDroop extends Block {

	public static final List<String> outputNames = new ArrayList<>();
	public static final List<String> inputNames = new ArrayList<>();
	public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

	static {
		outputNames.add("Ud");
		outputNames.add("Uq");
		outputNames.add("Vdc");
		outputNames.add("P");
		outputNames.add("Q");

		inputNames.add("Id");
		inputNames.add("Iq");
		inputNames.add("Theta");
		inputNames.add("Omega");
		inputNames.add("Idc_load");

		PARAMETER_DEFAULTS.put("mp", "0.0001");
		PARAMETER_DEFAULTS.put("mq", "0.0016");
		PARAMETER_DEFAULTS.put("fn", "50.0");
		PARAMETER_DEFAULTS.put("Vn", "311.0");
		PARAMETER_DEFAULTS.put("Vdc_ref", "700.0");
		PARAMETER_DEFAULTS.put("Lf", "0.001732");
		PARAMETER_DEFAULTS.put("Rf", "0.01");
		PARAMETER_DEFAULTS.put("Cdc", "0.0022");
		PARAMETER_DEFAULTS.put("Kp_v", "0.5");
		PARAMETER_DEFAULTS.put("Ki_v", "2.5");
		PARAMETER_DEFAULTS.put("Kp_i", "2.5");
		PARAMETER_DEFAULTS.put("Ki_i", "1.0");
	}

	public static List<String> getInputNames() {
		return inputNames;
	}

	public static List<String> getOutputNames() {
		return outputNames;
	}

	Parameter cParaMp;
	Parameter cParaMq;
	Parameter cParaFn;
	Parameter cParaVn;
	Parameter cParaVdcRef;
	Parameter cParaLf;
	Parameter cParaRf;
	Parameter cParaCdc;
	Parameter cParaKpV;
	Parameter cParaKiV;
	Parameter cParaKpI;
	Parameter cParaKiI;

	InputPort inId = new InputPort(this, 1, "Id");
	InputPort inIq = new InputPort(this, 2, "Iq");
	InputPort inTheta = new InputPort(this, 3, "Theta");
	InputPort inOmega = new InputPort(this, 4, "Omega");
	InputPort inIdcLoad = new InputPort(this, 5, "Idc_load");

	OutputPort outUd = new OutputPort(this, "Ud", 1, false);
	OutputPort outUq = new OutputPort(this, "Uq", 2, false);
	OutputPort outVdc = new OutputPort(this, "Vdc", 3, false);
	OutputPort outP = new OutputPort(this, "P", 4, false);
	OutputPort outQ = new OutputPort(this, "Q", 5, false);

	State stateIdInt = new State(this, 1, "id_int");
	State stateIqInt = new State(this, 2, "iq_int");
	State stateVdcInt = new State(this, 3, "vdc_int");
	State statePFlt = new State(this, 4, "P_flt");
	State stateQFlt = new State(this, 5, "Q_flt");

	/**
	 * DTO-NATIVE Constructor - Creates ACDCDroop block directly from BlockDto DTO
	 */
	public ACDCDroop(BlockDto blockDto, NCSLabModel model) {
		super(blockDto, model);
		initializeBlock();
		System.out.println("DTO-NATIVE: ACDCDroop block created successfully - " + blockDto.getBlockName());
	}

	public ACDCDroop(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);
		initializeBlock();
	}

	private void initializeBlock() {
		inputPortList.add(inId);
		inputPortList.add(inIq);
		inputPortList.add(inTheta);
		inputPortList.add(inOmega);
		inputPortList.add(inIdcLoad);

		outputPortList.add(outUd);
		outputPortList.add(outUq);
		outputPortList.add(outVdc);
		outputPortList.add(outP);
		outputPortList.add(outQ);

		cParaMp = addParameterIfAbsent("mp", PARAMETER_DEFAULTS.get("mp"));
		cParaMq = addParameterIfAbsent("mq", PARAMETER_DEFAULTS.get("mq"));
		cParaFn = addParameterIfAbsent("fn", PARAMETER_DEFAULTS.get("fn"));
		cParaVn = addParameterIfAbsent("Vn", PARAMETER_DEFAULTS.get("Vn"));
		cParaVdcRef = addParameterIfAbsent("Vdc_ref", PARAMETER_DEFAULTS.get("Vdc_ref"));
		cParaLf = addParameterIfAbsent("Lf", PARAMETER_DEFAULTS.get("Lf"));
		cParaRf = addParameterIfAbsent("Rf", PARAMETER_DEFAULTS.get("Rf"));
		cParaCdc = addParameterIfAbsent("Cdc", PARAMETER_DEFAULTS.get("Cdc"));
		cParaKpV = addParameterIfAbsent("Kp_v", PARAMETER_DEFAULTS.get("Kp_v"));
		cParaKiV = addParameterIfAbsent("Ki_v", PARAMETER_DEFAULTS.get("Ki_v"));
		cParaKpI = addParameterIfAbsent("Kp_i", PARAMETER_DEFAULTS.get("Kp_i"));
		cParaKiI = addParameterIfAbsent("Ki_i", PARAMETER_DEFAULTS.get("Ki_i"));

		stateList.add(stateIdInt);
		stateList.add(stateIqInt);
		stateList.add(stateVdcInt);
		stateList.add(statePFlt);
		stateList.add(stateQFlt);
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
		String codeStr = TemplateManager.renderTemplate("c/testrig/ACDCDroop/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block ACDCDroop:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		code.addArraysCode(arraysCode);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		String derivativeCode = TemplateManager.renderTemplate("c/testrig/ACDCDroop/derivative.vm", context);
		code.addDerivativeCode(derivativeCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		String codeStr = TemplateManager.renderTemplate("c/testrig/ACDCDroop/output.vm", context);
		code.addOutputCode(codeStr);
	}
}
