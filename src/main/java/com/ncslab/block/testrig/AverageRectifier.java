package com.ncslab.block.testrig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

public class AverageRectifier extends Block {

	public static final List<String> outputNames = new ArrayList<>();
	public static final List<String> inputNames = new ArrayList<>();
	public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

	static {
		outputNames.add("Vdc");
		outputNames.add("Id");
		outputNames.add("Iq");

		inputNames.add("P_inst");
		inputNames.add("Q_inst");
		inputNames.add("Idc_load");

		/* DC link */
		PARAMETER_DEFAULTS.put("Vdc_ref", "700.0");
		PARAMETER_DEFAULTS.put("Cdc", "0.022");

		/* Vdc controller */
		PARAMETER_DEFAULTS.put("Kp_v", "0.5");
		PARAMETER_DEFAULTS.put("Ki_v", "0.5");
		PARAMETER_DEFAULTS.put("mq", "0.0016");

		/* Power / current scaling */
		PARAMETER_DEFAULTS.put("S_base", "9800.0");
		PARAMETER_DEFAULTS.put("tau_i", "0.001");
		PARAMETER_DEFAULTS.put("Iq_max", "2.0");

		/* Soft-start / sequencing */
		PARAMETER_DEFAULTS.put("t_precharge", "1.0");
		PARAMETER_DEFAULTS.put("t_ramp", "2.0");
	}

	public static List<String> getInputNames() {
		return inputNames;
	}

	public static List<String> getOutputNames() {
		return outputNames;
	}

	/* DC link */
	Parameter cParaVdcRef, cParaCdc;

	/* Vdc controller */
	Parameter cParaKpV, cParaKiV, cParaMq;

	/* Power / current scaling */
	Parameter cParaSBase, cParaTauI, cParaIqMax;

	/* Soft-start */
	Parameter cParaTPrecharge, cParaTRamp;

	InputPort inPInst = new InputPort(this, 1, "P_inst");
	InputPort inQInst = new InputPort(this, 2, "Q_inst");
	InputPort inIdcLoad = new InputPort(this, 3, "Idc_load");

	OutputPort outVdc = new OutputPort(this, "Vdc", 1, false);
	OutputPort outId = new OutputPort(this, "Id", 2, false);
	OutputPort outIq = new OutputPort(this, "Iq", 3, false);

	/* States */
	State stateVdcInt = new State(this, 1, "vdc_int");
	State stateId = new State(this, 2, "Id");
	State stateIq = new State(this, 3, "Iq");
	State stateVdcErrInt = new State(this, 4, "vdc_err_int");
	State statePFlt = new State(this, 5, "P_flt");
	State stateQFlt = new State(this, 6, "Q_flt");

	public AverageRectifier(BlockDto blockDto, NCSLabModel model) {
		super(blockDto, model);
		initializeBlock();
		System.out.println("DTO-NATIVE: AverageRectifier block created successfully - " + blockDto.getBlockName());
	}

	public AverageRectifier(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);
		initializeBlock();
	}

	private void initializeBlock() {
		inputPortList.add(inPInst);
		inputPortList.add(inQInst);
		inputPortList.add(inIdcLoad);

		outputPortList.add(outVdc);
		outputPortList.add(outId);
		outputPortList.add(outIq);

		/* DC link */
		cParaVdcRef = addParameterIfAbsent("Vdc_ref", PARAMETER_DEFAULTS.get("Vdc_ref"));
		cParaCdc = addParameterIfAbsent("Cdc", PARAMETER_DEFAULTS.get("Cdc"));

		/* Vdc controller */
		cParaKpV = addParameterIfAbsent("Kp_v", PARAMETER_DEFAULTS.get("Kp_v"));
		cParaKiV = addParameterIfAbsent("Ki_v", PARAMETER_DEFAULTS.get("Ki_v"));
		cParaMq = addParameterIfAbsent("mq", PARAMETER_DEFAULTS.get("mq"));

		/* Power / current scaling */
		cParaSBase = addParameterIfAbsent("S_base", PARAMETER_DEFAULTS.get("S_base"));
		cParaTauI = addParameterIfAbsent("tau_i", PARAMETER_DEFAULTS.get("tau_i"));
		cParaIqMax = addParameterIfAbsent("Iq_max", PARAMETER_DEFAULTS.get("Iq_max"));

		/* Soft-start */
		cParaTPrecharge = addParameterIfAbsent("t_precharge", PARAMETER_DEFAULTS.get("t_precharge"));
		cParaTRamp = addParameterIfAbsent("t_ramp", PARAMETER_DEFAULTS.get("t_ramp"));

		/* States */
		stateList.add(stateVdcInt);
		stateList.add(stateId);
		stateList.add(stateIq);
		stateList.add(stateVdcErrInt);
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
		String codeStr = TemplateManager.renderTemplate("c/testrig/AverageRectifier/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block AverageRectifier:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		code.addArraysCode(arraysCode);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		String derivativeCode = TemplateManager.renderTemplate("c/testrig/AverageRectifier/derivative.vm", context);
		code.addDerivativeCode(derivativeCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		String codeStr = TemplateManager.renderTemplate("c/testrig/AverageRectifier/output.vm", context);
		code.addOutputCode(codeStr);
	}
}
