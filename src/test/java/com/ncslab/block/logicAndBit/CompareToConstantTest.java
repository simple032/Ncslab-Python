package com.ncslab.block.logicAndBit;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for CompareToConstant block using DirectBlockTestBase pattern.
 * Tests all comparison operators with a constant value: ==, !=, <, <=, >, >=
 */
public class CompareToConstantTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Default block with >= operator, constant = 10.0
        return CompareToConstant.create("testCompareToConstant", "test", 10.0, ">=", mockModel);
    }

    // ===== Equals (==) Operator Tests =====

    @Test
    public void testEqualToConstant_Equal() {
        block = CompareToConstant.create("testEquals", "test", 10.0, "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 == 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEqualToConstant_Greater() {
        block = CompareToConstant.create("testEquals", "test", 10.0, "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        block.calculateOutput(0.0);
        assertEquals("15 == 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEqualToConstant_Less() {
        block = CompareToConstant.create("testEquals", "test", 10.0, "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 == 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEqualToConstant_NegativeConstant() {
        block = CompareToConstant.create("testEquals", "test", -5.0, "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 == -5 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Not Equals (!=) Operator Tests =====

    @Test
    public void testNotEqualToConstant_Equal() {
        block = CompareToConstant.create("testNotEquals", "test", 10.0, "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 != 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEqualToConstant_Greater() {
        block = CompareToConstant.create("testNotEquals", "test", 10.0, "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        block.calculateOutput(0.0);
        assertEquals("15 != 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEqualToConstant_Less() {
        block = CompareToConstant.create("testNotEquals", "test", 10.0, "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 != 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEqualToConstant_Zero() {
        block = CompareToConstant.create("testNotEquals", "test", 0.0, "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.001);
        block.calculateOutput(0.0);
        assertEquals("0.001 != 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Less Than (<) Operator Tests =====

    @Test
    public void testLessThanConstant_Less() {
        block = CompareToConstant.create("testLessThan", "test", 10.0, "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 < 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanConstant_Equal() {
        block = CompareToConstant.create("testLessThan", "test", 10.0, "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 < 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanConstant_Greater() {
        block = CompareToConstant.create("testLessThan", "test", 10.0, "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        block.calculateOutput(0.0);
        assertEquals("15 < 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanConstant_Negative() {
        block = CompareToConstant.create("testLessThan", "test", 0.0, "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 < 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Less Than or Equal (<=) Operator Tests =====

    @Test
    public void testLessThanOrEqualConstant_Less() {
        block = CompareToConstant.create("testLessEqual", "test", 10.0, "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 <= 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanOrEqualConstant_Equal() {
        block = CompareToConstant.create("testLessEqual", "test", 10.0, "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 <= 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanOrEqualConstant_Greater() {
        block = CompareToConstant.create("testLessEqual", "test", 10.0, "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        block.calculateOutput(0.0);
        assertEquals("15 <= 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Greater Than (>) Operator Tests =====

    @Test
    public void testGreaterThanConstant_Greater() {
        block = CompareToConstant.create("testGreaterThan", "test", 10.0, ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        block.calculateOutput(0.0);
        assertEquals("15 > 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanConstant_Equal() {
        block = CompareToConstant.create("testGreaterThan", "test", 10.0, ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 > 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanConstant_Less() {
        block = CompareToConstant.create("testGreaterThan", "test", 10.0, ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 > 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanConstant_Positive() {
        block = CompareToConstant.create("testGreaterThan", "test", 0.0, ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 > 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Greater Than or Equal (>=) Operator Tests =====

    @Test
    public void testGreaterThanOrEqualConstant_Greater() {
        setScalarInput(0, 15.0);
        block.calculateOutput(0.0);
        assertEquals("15 >= 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanOrEqualConstant_Equal() {
        setScalarInput(0, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 >= 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanOrEqualConstant_Less() {
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);
        assertEquals("5 >= 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Edge Case Tests =====

    @Test
    public void testZeroConstant() {
        block = CompareToConstant.create("testZero", "test", 0.0, "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 == 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLargeConstant() {
        block = CompareToConstant.create("testLarge", "test", 1000000.0, "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 999999.0);
        block.calculateOutput(0.0);
        assertEquals("999999 < 1000000 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Performance Test =====

    @Test
    public void testPerformance() {
        setScalarInput(0, 15.0);

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
