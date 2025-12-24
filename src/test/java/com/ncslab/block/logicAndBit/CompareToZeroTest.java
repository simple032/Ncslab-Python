package com.ncslab.block.logicAndBit;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for CompareToZero block using DirectBlockTestBase pattern.
 * Tests all comparison operators: ==, !=, <, <=, >, >=
 */
public class CompareToZeroTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Default block with >= operator
        return CompareToZero.create("testCompareToZero", "test", ">=", mockModel);
    }

    // ===== Equals (==) Operator Tests =====

    @Test
    public void testEqualToZero() {
        block = CompareToZero.create("testEquals", "test", "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 == 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEqualToZero_Positive() {
        block = CompareToZero.create("testEquals", "test", "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 == 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEqualToZero_Negative() {
        block = CompareToZero.create("testEquals", "test", "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 == 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEqualToZero_SmallPositive() {
        block = CompareToZero.create("testEquals", "test", "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.001);
        block.calculateOutput(0.0);
        assertEquals("0.001 == 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Not Equals (!=) Operator Tests =====

    @Test
    public void testNotEqualToZero() {
        block = CompareToZero.create("testNotEquals", "test", "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 != 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEqualToZero_Positive() {
        block = CompareToZero.create("testNotEquals", "test", "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 != 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEqualToZero_Negative() {
        block = CompareToZero.create("testNotEquals", "test", "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 != 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEqualToZero_SmallNegative() {
        block = CompareToZero.create("testNotEquals", "test", "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -0.001);
        block.calculateOutput(0.0);
        assertEquals("-0.001 != 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Less Than (<) Operator Tests =====

    @Test
    public void testLessThanZero_Negative() {
        block = CompareToZero.create("testLessThan", "test", "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 < 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanZero_Zero() {
        block = CompareToZero.create("testLessThan", "test", "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 < 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanZero_Positive() {
        block = CompareToZero.create("testLessThan", "test", "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 < 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanZero_SmallNegative() {
        block = CompareToZero.create("testLessThan", "test", "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -0.001);
        block.calculateOutput(0.0);
        assertEquals("-0.001 < 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Less Than or Equal (<=) Operator Tests =====

    @Test
    public void testLessThanOrEqualZero_Negative() {
        block = CompareToZero.create("testLessEqual", "test", "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 <= 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanOrEqualZero_Zero() {
        block = CompareToZero.create("testLessEqual", "test", "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 <= 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanOrEqualZero_Positive() {
        block = CompareToZero.create("testLessEqual", "test", "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 <= 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Greater Than (>) Operator Tests =====

    @Test
    public void testGreaterThanZero_Positive() {
        block = CompareToZero.create("testGreaterThan", "test", ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 > 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanZero_Zero() {
        block = CompareToZero.create("testGreaterThan", "test", ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 > 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanZero_Negative() {
        block = CompareToZero.create("testGreaterThan", "test", ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 > 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanZero_SmallPositive() {
        block = CompareToZero.create("testGreaterThan", "test", ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.001);
        block.calculateOutput(0.0);
        assertEquals("0.001 > 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Greater Than or Equal (>=) Operator Tests =====

    @Test
    public void testGreaterThanOrEqualZero_Positive() {
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 >= 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanOrEqualZero_Zero() {
        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 >= 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanOrEqualZero_Negative() {
        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 >= 0 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Performance Test =====

    @Test
    public void testPerformance() {
        setScalarInput(0, 5.0);

        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            block.calculateOutput(0.0);
        }
        long endTime = System.nanoTime();

        long avgTime = (endTime - startTime) / 1000;
        assertTrue("Average execution time should be under 200 microseconds, was: " + avgTime + " ns",
                   avgTime < 200_000);
    }
}
