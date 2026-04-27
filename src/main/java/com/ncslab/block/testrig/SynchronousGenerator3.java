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

public class SynchronousGenerator3 extends Block {

	public static final List<String> outputNames = new ArrayList<>();
	public static final List<String> inputNames = new ArrayList<>();
	public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

	static {
		outputNames.add("Vd");
		outputNames.add("Vq");
		outputNames.add("Vt");
		outputNames.add("Pe");
		outputNames.add("P_inst");
		outputNames.add("Q_inst");
		outputNames.add("Theta");
		outputNames.add("Omega");

		inputNames.add("Id");
		inputNames.add("Iq");

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

	InputPort inId = new InputPort(this, 1, "Id");
	InputPort inIq = new InputPort(this, 2, "Iq");

	OutputPort outVd = new OutputPort(this, "Vd", 1, false);
	OutputPort outVq = new OutputPort(this, "Vq", 2, false);
	OutputPort outVt = new OutputPort(this, "Vt", 3, false);
	OutputPort outPe = new OutputPort(this, "Pe", 4, false);
	OutputPort outPInst = new OutputPort(this, "P_inst", 5, false);
	OutputPort outQInst = new OutputPort(this, "Q_inst", 6, false);
	OutputPort outTheta = new OutputPort(this, "Theta", 7, false);
	OutputPort outOmega = new OutputPort(this, "Omega", 8, false);

	/* States */
	State stateTheta = new State(this, 1, "Theta");
	State stateOmega = new State(this, 2, "Omega");
	State stateEqPrime = new State(this, 3, "Eq_prime");
	State stateAvrInt = new State(this, 4, "avr_int");
	State stateGovInt = new State(this, 5, "gov_int");

	public SynchronousGenerator3(BlockDto blockDto, NCSLabModel model) {
		super(blockDto, model);
		initializeBlock();
		System.out.println("DTO-NATIVE: SynchronousGenerator3 block created successfully - " + blockDto.getBlockName());
	}

	public SynchronousGenerator3(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);
		initializeBlock();
	}

	private void initializeBlock() {
		inputPortList.add(inId);
		inputPortList.add(inIq);

		outputPortList.add(outVd);
		outputPortList.add(outVq);
		outputPortList.add(outVt);
		outputPortList.add(outPe);
		outputPortList.add(outPInst);
		outputPortList.add(outQInst);
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

		/* States */
		stateList.add(stateTheta);
		stateList.add(stateOmega);
		stateList.add(stateEqPrime);
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
		String codeStr = TemplateManager.renderTemplate("c/testrig/SynchronousGenerator3/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block SynchronousGenerator3:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		code.addArraysCode(arraysCode);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		String derivativeCode = TemplateManager.renderTemplate("c/testrig/SynchronousGenerator3/derivative.vm", context);
		code.addDerivativeCode(derivativeCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		String codeStr = TemplateManager.renderTemplate("c/testrig/SynchronousGenerator3/output.vm", context);
		code.addOutputCode(codeStr);
	}
}
