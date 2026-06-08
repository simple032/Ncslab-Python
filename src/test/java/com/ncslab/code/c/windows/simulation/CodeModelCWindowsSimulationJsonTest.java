package com.ncslab.code.c.windows.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.json.JSONObject;
import org.junit.Test;

public class CodeModelCWindowsSimulationJsonTest {

    @Test
    public void toJsonNumber_keepsFiniteValues() {
        Object value = CodeModelCWindowsSimulation.toJsonNumber(12.5);

        assertEquals(Double.valueOf(12.5), value);
    }

    @Test
    public void toJsonNumber_convertsNonFiniteValuesToJsonNull() {
        assertSame(JSONObject.NULL, CodeModelCWindowsSimulation.toJsonNumber(Double.NaN));
        assertSame(JSONObject.NULL, CodeModelCWindowsSimulation.toJsonNumber(Double.POSITIVE_INFINITY));
        assertSame(JSONObject.NULL, CodeModelCWindowsSimulation.toJsonNumber(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void nonFiniteText_preservesDisplayLabel() {
        assertEquals("NaN", CodeModelCWindowsSimulation.nonFiniteText(Double.NaN));
        assertEquals("Infinity", CodeModelCWindowsSimulation.nonFiniteText(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", CodeModelCWindowsSimulation.nonFiniteText(Double.NEGATIVE_INFINITY));
    }
}
