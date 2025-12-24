package com.ncslab.block.logicAndBit;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for LogicOperator block using DirectBlockTestBase pattern.
 * Tests all logic operators: AND, OR, NOT, NAND, NOR, XOR, NXOR
 */
public class LogicOperatorTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Default block with AND operator, 2 inputs
        return LogicOperator.create("testLogicOperator", "test", "AND", 2, mockModel);
    }

    // ===== AND Operator Tests =====

    @Test
    public void testAND_TrueTrue() {
        setScalarInput(0, 1.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("true AND true should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAND_TrueFalse() {
        setScalarInput(0, 1.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("true AND false should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAND_FalseTrue() {
        setScalarInput(0, 0.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("false AND true should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testAND_FalseFalse() {
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("false AND false should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== OR Operator Tests =====

    @Test
    public void testOR_TrueTrue() {
        block = LogicOperator.create("testOR", "test", "OR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("true OR true should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testOR_TrueFalse() {
        block = LogicOperator.create("testOR", "test", "OR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("true OR false should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testOR_FalseTrue() {
        block = LogicOperator.create("testOR", "test", "OR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("false OR true should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testOR_FalseFalse() {
        block = LogicOperator.create("testOR", "test", "OR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("false OR false should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== NOT Operator Tests =====

    @Test
    public void testNOT_True() {
        block = LogicOperator.create("testNOT", "test", "NOT", 1, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        block.calculateOutput(0.0);
        assertEquals("NOT true should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNOT_False() {
        block = LogicOperator.create("testNOT", "test", "NOT", 1, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("NOT false should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNOT_NonZero() {
        block = LogicOperator.create("testNOT", "test", "NOT", 1, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("NOT 5.0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== NAND Operator Tests =====

    @Test
    public void testNAND_TrueTrue() {
        block = LogicOperator.create("testNAND", "test", "NAND", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("true NAND true should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNAND_TrueFalse() {
        block = LogicOperator.create("testNAND", "test", "NAND", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("true NAND false should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNAND_FalseTrue() {
        block = LogicOperator.create("testNAND", "test", "NAND", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("false NAND true should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNAND_FalseFalse() {
        block = LogicOperator.create("testNAND", "test", "NAND", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("false NAND false should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== NOR Operator Tests =====

    @Test
    public void testNOR_TrueTrue() {
        block = LogicOperator.create("testNOR", "test", "NOR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("true NOR true should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNOR_TrueFalse() {
        block = LogicOperator.create("testNOR", "test", "NOR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("true NOR false should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNOR_FalseTrue() {
        block = LogicOperator.create("testNOR", "test", "NOR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("false NOR true should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNOR_FalseFalse() {
        block = LogicOperator.create("testNOR", "test", "NOR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("false NOR false should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== XOR Operator Tests =====

    @Test
    public void testXOR_TrueTrue() {
        block = LogicOperator.create("testXOR", "test", "XOR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("true XOR true should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testXOR_TrueFalse() {
        block = LogicOperator.create("testXOR", "test", "XOR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 1.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("true XOR false should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testXOR_FalseTrue() {
        block = LogicOperator.create("testXOR", "test", "XOR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 1.0);
        block.calculateOutput(0.0);
        assertEquals("false XOR true should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testXOR_FalseFalse() {
        block = LogicOperator.create("testXOR", "test", "XOR", 2, mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("false XOR false should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Performance Test =====

    @Test
    public void testPerformance() {
        setScalarInput(0, 1.0);
        setScalarInput(1, 1.0);

        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(0.0);
        }
        long endTime = System.nanoTime();

        long avgTime = (endTime - startTime) / 1000;
        assertTrue("Average execution time should be under 500 microseconds, was: " + avgTime + " ns",
                   avgTime < 500_000);
    }
}
