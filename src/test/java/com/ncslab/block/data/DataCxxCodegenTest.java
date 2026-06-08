package com.ncslab.block.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataCxxCodegenTest {

	@Test
	public void evaluatedJavaScalarToCxxToken_infinityVariants() {
		assertEquals("INFINITY", Data.evaluatedJavaScalarToCxxToken("Infinity"));
		assertEquals("INFINITY", Data.evaluatedJavaScalarToCxxToken("infinity"));
		assertEquals("INFINITY", Data.evaluatedJavaScalarToCxxToken("INF"));
		assertEquals("INFINITY", Data.evaluatedJavaScalarToCxxToken("  +infinity  "));
	}

	@Test
	public void evaluatedJavaScalarToCxxToken_negativeInfinity() {
		assertEquals("(-INFINITY)", Data.evaluatedJavaScalarToCxxToken("-Infinity"));
		assertEquals("(-INFINITY)", Data.evaluatedJavaScalarToCxxToken("-inf"));
	}

	@Test
	public void evaluatedJavaScalarToCxxToken_nan() {
		assertEquals("NAN", Data.evaluatedJavaScalarToCxxToken("NaN"));
		assertEquals("NAN", Data.evaluatedJavaScalarToCxxToken("nan"));
	}

	@Test
	public void evaluatedJavaScalarToCxxToken_finiteUnchanged() {
		assertEquals("1.23e-4", Data.evaluatedJavaScalarToCxxToken("  1.23e-4 "));
		assertEquals("42", Data.evaluatedJavaScalarToCxxToken("42"));
	}

	@Test
	public void cxxScalarMacroForNonFiniteDouble_mapsInfAndNan() {
		assertEquals("INFINITY", Data.cxxScalarMacroForNonFiniteDouble(Double.POSITIVE_INFINITY));
		assertEquals("(-INFINITY)", Data.cxxScalarMacroForNonFiniteDouble(Double.NEGATIVE_INFINITY));
		assertEquals("NAN", Data.cxxScalarMacroForNonFiniteDouble(Double.NaN));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cxxScalarMacroForNonFiniteDouble_rejectsFiniteZero() {
		Data.cxxScalarMacroForNonFiniteDouble(0.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void cxxScalarMacroForNonFiniteDouble_rejectsFiniteOne() {
		Data.cxxScalarMacroForNonFiniteDouble(1.0);
	}
}
