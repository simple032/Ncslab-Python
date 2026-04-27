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

public class ACDCGenerator extends Block {

	public static final List<String> outputNames = new ArrayList<>();
	public static final List<String> inputNames = new ArrayList<>();
	public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

	static {
		outputNames.add("Vdc");
		outputNames.add("P");
		outputNames.add("Q");
		outputNames.add("Id");
		outputNames.add("Iq");
		outputNames.add("Theta");
		outputNames.add("Omega");

		inputNames.add("Idc_load");

		/* Generator electrical (3-order model) */
		PARAMETER_DEFAULTS.put("Omega0", "314.15926");
		PARAMETER_DEFAULTS.put("Xd", "1.346");
		PARAMETER_DEFAULTS.put("Xq", "0.94");
		PARAMETER_DEFAULTS.put("Xdd", "0.446");
		PARAMETER_DEFAULTS.put("Td0", "1.66");
		PARAMETER_DEFAULTS.put("Ra", "0.006");
		PARAMETER_DEFAULTS.put("H", "1.2");
		PARAMETER_DEFAULTS.put("D", "10.0");

		/* AVR / excitation */
		PARAMETER_DEFAULTS.put("Vt_ref", "1.0");
		PARAMETER_DEFAULTS.put("Kp_avr", "50.0");
		PARAMETER_DEFAULTS.put("Ki_avr", "10.0");
		PARAMETER_DEFAULTS.put("Efd0", "1.0");
		PARAMETER_DEFAULTS.put("Efd_min", "0.0");
		PARAMETER_DEFAULTS.put("Efd_max", "5.0");

		/* Governor / speed */
		PARAMETER_DEFAULTS.put("omega_ref", "1.0");
		PARAMETER_DEFAULTS.put("Kp_gov", "20.0");
		PARAMETER_DEFAULTS.put("Ki_gov", "5.0");
		PARAMETER_DEFAULTS.put("Pm0", "0.05");
		PARAMETER_DEFAULTS.put("Pm_min", "0.0");
		PARAMETER_DEFAULTS.put("Pm_max", "0.8");

		/* Rectifier / DC */
		PARAMETER_DEFAULTS.put("Vdc_ref", "700.0");
		PARAMETER_DEFAULTS.put("Cdc", "0.022");
		PARAMETER_DEFAULTS.put("Kp_v", "0.5");
		PARAMETER_DEFAULTS.put("Ki_v", "0.5");
		PARAMETER_DEFAULTS.put("mq", "0.0016");
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

	/* Generator electrical */
	Parameter cParaOmega0, cParaXd, cParaXq, cParaXdd, cParaTd0;
	Parameter cParaRa, cParaH, cParaD;

	/* AVR */
	Parameter cParaVtRef, cParaKpAvr, cParaKiAvr;
	Parameter cParaEfd0, cParaEfdMin, cParaEfdMax;

	/* Governor */
	Parameter cParaOmegaRef, cParaKpGov, cParaKiGov;
	Parameter cParaPm0, cParaPmMin, cParaPmMax;

	/* Rectifier */
	Parameter cParaVdcRef, cParaCdc, cParaKpV, cParaKiV;
	Parameter cParaMq, cParaSBase, cParaTauI, cParaIqMax;

	/* Soft-start */
	Parameter cParaTPrecharge, cParaTRamp;

	InputPort inIdcLoad = new InputPort(this, 1, "Idc_load");

	OutputPort outVdc = new OutputPort(this, "Vdc", 1, false);
	OutputPort outP = new OutputPort(this, "P", 2, false);
	OutputPort outQ = new OutputPort(this, "Q", 3, false);
	OutputPort outId = new OutputPort(this, "Id", 4, false);
	OutputPort outIq = new OutputPort(this, "Iq", 5, false);
	OutputPort outTheta = new OutputPort(this, "Theta", 6, false);
	OutputPort outOmega = new OutputPort(this, "Omega", 7, false);

	/* States */
	State stateTheta = new State(this, 1, "Theta");
	State stateOmega = new State(this, 2, "Omega");
	State stateEqPrime = new State(this, 3, "Eq_prime");
	State stateId = new State(this, 4, "Id");
	State stateIq = new State(this, 5, "Iq");
	State stateVdcInt = new State(this, 6, "vdc_int");
	State statePFlt = new State(this, 7, "P_flt");
	State stateQFlt = new State(this, 8, "Q_flt");
	State stateVdcErrInt = new State(this, 9, "vdc_err_int");
	State stateAvrInt = new State(this, 10, "avr_int");
	State stateGovInt = new State(this, 11, "gov_int");

	public ACDCGenerator(BlockDto blockDto, NCSLabModel model) {
		super(blockDto, model);
		initializeBlock();
		System.out.println("DTO-NATIVE: ACDCGenerator block created successfully - " + blockDto.getBlockName());
	}

	public ACDCGenerator(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);
		initializeBlock();
	}

	private void initializeBlock() {
		inputPortList.add(inIdcLoad);

		outputPortList.add(outVdc);
		outputPortList.add(outP);
		outputPortList.add(outQ);
		outputPortList.add(outId);
		outputPortList.add(outIq);
		outputPortList.add(outTheta);
		outputPortList.add(outOmega);

		/* Generator electrical */
		cParaOmega0 = addParameterIfAbsent("Omega0", PARAMETER_DEFAULTS.get("Omega0"));
		cParaXd = addParameterIfAbsent("Xd", PARAMETER_DEFAULTS.get("Xd"));
		cParaXq = addParameterIfAbsent("Xq", PARAMETER_DEFAULTS.get("Xq"));
		cParaXdd = addParameterIfAbsent("Xdd", PARAMETER_DEFAULTS.get("Xdd"));
		cParaTd0 = addParameterIfAbsent("Td0", PARAMETER_DEFAULTS.get("Td0"));
		cParaRa = addParameterIfAbsent("Ra", PARAMETER_DEFAULTS.get("Ra"));
		cParaH = addParameterIfAbsent("H", PARAMETER_DEFAULTS.get("H"));
		cParaD = addParameterIfAbsent("D", PARAMETER_DEFAULTS.get("D"));

		/* AVR */
		cParaVtRef = addParameterIfAbsent("Vt_ref", PARAMETER_DEFAULTS.get("Vt_ref"));
		cParaKpAvr = addParameterIfAbsent("Kp_avr", PARAMETER_DEFAULTS.get("Kp_avr"));
		cParaKiAvr = addParameterIfAbsent("Ki_avr", PARAMETER_DEFAULTS.get("Ki_avr"));
		cParaEfd0 = addParameterIfAbsent("Efd0", PARAMETER_DEFAULTS.get("Efd0"));
		cParaEfdMin = addParameterIfAbsent("Efd_min", PARAMETER_DEFAULTS.get("Efd_min"));
		cParaEfdMax = addParameterIfAbsent("Efd_max", PARAMETER_DEFAULTS.get("Efd_max"));

		/* Governor */
		cParaOmegaRef = addParameterIfAbsent("omega_ref", PARAMETER_DEFAULTS.get("omega_ref"));
		cParaKpGov = addParameterIfAbsent("Kp_gov", PARAMETER_DEFAULTS.get("Kp_gov"));
		cParaKiGov = addParameterIfAbsent("Ki_gov", PARAMETER_DEFAULTS.get("Ki_gov"));
		cParaPm0 = addParameterIfAbsent("Pm0", PARAMETER_DEFAULTS.get("Pm0"));
		cParaPmMin = addParameterIfAbsent("Pm_min", PARAMETER_DEFAULTS.get("Pm_min"));
		cParaPmMax = addParameterIfAbsent("Pm_max", PARAMETER_DEFAULTS.get("Pm_max"));

		/* Rectifier */
		cParaVdcRef = addParameterIfAbsent("Vdc_ref", PARAMETER_DEFAULTS.get("Vdc_ref"));
		cParaCdc = addParameterIfAbsent("Cdc", PARAMETER_DEFAULTS.get("Cdc"));
		cParaKpV = addParameterIfAbsent("Kp_v", PARAMETER_DEFAULTS.get("Kp_v"));
		cParaKiV = addParameterIfAbsent("Ki_v", PARAMETER_DEFAULTS.get("Ki_v"));
		cParaMq = addParameterIfAbsent("mq", PARAMETER_DEFAULTS.get("mq"));
		cParaSBase = addParameterIfAbsent("S_base", PARAMETER_DEFAULTS.get("S_base"));
		cParaTauI = addParameterIfAbsent("tau_i", PARAMETER_DEFAULTS.get("tau_i"));
		cParaIqMax = addParameterIfAbsent("Iq_max", PARAMETER_DEFAULTS.get("Iq_max"));

		/* Soft-start */
		cParaTPrecharge = addParameterIfAbsent("t_precharge", PARAMETER_DEFAULTS.get("t_precharge"));
		cParaTRamp = addParameterIfAbsent("t_ramp", PARAMETER_DEFAULTS.get("t_ramp"));

		/* States */
		stateList.add(stateTheta);
		stateList.add(stateOmega);
		stateList.add(stateEqPrime);
		stateList.add(stateId);
		stateList.add(stateIq);
		stateList.add(stateVdcInt);
		stateList.add(statePFlt);
		stateList.add(stateQFlt);
		stateList.add(stateVdcErrInt);
		stateList.add(stateAvrInt);
		stateList.add(stateGovInt);
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
		String codeStr = TemplateManager.renderTemplate("c/testrig/ACDCGenerator/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block ACDCGenerator:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		code.addArraysCode(arraysCode);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		String derivativeCode = TemplateManager.renderTemplate("c/testrig/ACDCGenerator/derivative.vm", context);
		code.addDerivativeCode(derivativeCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		String codeStr = TemplateManager.renderTemplate("c/testrig/ACDCGenerator/output.vm", context);
		code.addOutputCode(codeStr);
	}
}
