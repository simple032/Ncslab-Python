package com.ncslab.circuit2.block.element;

import com.ncslab.ncslablink.Config;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Ensures circuit codegen uses C++ {@code INFINITY}/{@code NAN} macros instead of Java {@code Infinity}.
 */
public class CircuitElementCxxInfinityTest {

	private NCSLabModel model;

	@Before
	public void setUp() {
		model = Mockito.mock(NCSLabModel.class);
		Config config = Mockito.mock(Config.class);
		when(config.getFixedStep()).thenReturn(0.01);
		when(model.getConfig()).thenReturn(config);
	}

	private static JSONObject resistorJson(String rExpr) {
		JSONObject block = new JSONObject();
		block.put("blockType", "Resistor");
		block.put("blockName", "R1");
		block.put("blockPath", "m/R1");
		block.put("blockUUID", JSONObject.NULL);
		JSONObject pv = new JSONObject();
		pv.put("R", rExpr);
		block.put("paramValues", pv);
		return block;
	}

	private static JSONObject capacitorJson(String cExpr) {
		JSONObject block = new JSONObject();
		block.put("blockType", "Capacitor");
		block.put("blockName", "C1");
		block.put("blockPath", "m/C1");
		block.put("blockUUID", JSONObject.NULL);
		JSONObject pv = new JSONObject();
		pv.put("c", cExpr);
		block.put("paramValues", pv);
		return block;
	}

	private static JSONObject inductorJson(String lExpr) {
		JSONObject block = new JSONObject();
		block.put("blockType", "Inductor");
		block.put("blockName", "L1");
		block.put("blockPath", "m/L1");
		block.put("blockUUID", JSONObject.NULL);
		JSONObject pv = new JSONObject();
		pv.put("l", lExpr);
		block.put("paramValues", pv);
		return block;
	}

	@Test
	public void resistor_infinity_emitsInfinityMacro() {
		Resistor r = new Resistor(0, resistorJson("Infinity"), model);
		assertEquals("(INFINITY*1.0)", r.getRString());
	}

	@Test
	public void resistor_nan_emitsNanMacro() {
		Resistor r = new Resistor(0, resistorJson("NaN"), model);
		assertEquals("(NAN*1.0)", r.getRString());
	}

	@Test
	public void resistor_negativeInfinity_treatedAsShort() {
		Resistor r = new Resistor(0, resistorJson("-Infinity"), model);
		assertEquals("1.0", r.getRString());
	}

	@Test
	public void capacitor_infinity_emitsInfinityInStamp() {
		Capacitor c = new Capacitor(0, capacitorJson("Infinity"), model);
		assertEquals("((model.stepSize)/2.0/(INFINITY))", c.getRString());
	}

	@Test
	public void inductor_infinity_emitsInfinityInStamp() {
		Inductor l = new Inductor(0, inductorJson("Infinity"), model);
		assertEquals("(2.0*(INFINITY)/(model.stepSize))", l.getRString());
	}
}
