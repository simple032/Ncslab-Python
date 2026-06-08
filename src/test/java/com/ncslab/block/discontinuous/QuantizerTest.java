package com.ncslab.block.discontinuous;

import com.ncslab.block.Block;
import com.ncslab.block.DirectBlockTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Direct tests for the Simulink-compatible Quantizer block.
 */
public class QuantizerTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() {
        return Quantizer.create("testQuantizer", "test", "0.5", mockModel);
    }

    @Test
    public void testScalarRoundsToNearestInterval() {
        setScalarInput(0, 1.26);

        block.calculateOutput(0.0);

        assertEquals("Should round to nearest 0.5 interval", 1.5, getScalarOutput(0), DELTA);
    }

    @Test
    public void testScalarRoundsNegativeInput() {
        setScalarInput(0, -1.26);

        block.calculateOutput(0.0);

        assertEquals("Should round negative input to nearest 0.5 interval", -1.5, getScalarOutput(0), DELTA);
    }
}
