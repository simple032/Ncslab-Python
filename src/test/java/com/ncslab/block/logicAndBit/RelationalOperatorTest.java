package com.ncslab.block.logicAndBit;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for RelationalOperator block using DirectBlockTestBase pattern.
 * Tests all two-input comparison operators: ==, !=, <, <=, >, >=
 */
public class RelationalOperatorTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Default block with >= operator
        return RelationalOperator.create("testRelationalOperator", "test", ">=", mockModel);
    }

    // ===== Equals (==) Operator Tests =====

    @Test
    public void testEquals_Equal() {
        block = RelationalOperator.create("testEquals", "test", "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 == 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEquals_Greater() {
        block = RelationalOperator.create("testEquals", "test", "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("15 == 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEquals_Less() {
        block = RelationalOperator.create("testEquals", "test", "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("5 == 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testEquals_NegativeValues() {
        block = RelationalOperator.create("testEquals", "test", "==", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        setScalarInput(1, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-5 == -5 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Not Equals (!=) Operator Tests =====

    @Test
    public void testNotEquals_Equal() {
        block = RelationalOperator.create("testNotEquals", "test", "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 != 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEquals_Greater() {
        block = RelationalOperator.create("testNotEquals", "test", "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("15 != 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEquals_Less() {
        block = RelationalOperator.create("testNotEquals", "test", "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("5 != 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNotEquals_ZeroAndNonZero() {
        block = RelationalOperator.create("testNotEquals", "test", "!=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 0.0);
        setScalarInput(1, 0.001);
        block.calculateOutput(0.0);
        assertEquals("0 != 0.001 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Less Than (<) Operator Tests =====

    @Test
    public void testLessThan_Less() {
        block = RelationalOperator.create("testLessThan", "test", "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("5 < 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThan_Equal() {
        block = RelationalOperator.create("testLessThan", "test", "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 < 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThan_Greater() {
        block = RelationalOperator.create("testLessThan", "test", "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("15 < 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThan_NegativeValues() {
        block = RelationalOperator.create("testLessThan", "test", "<", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -10.0);
        setScalarInput(1, -5.0);
        block.calculateOutput(0.0);
        assertEquals("-10 < -5 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Less Than or Equal (<=) Operator Tests =====

    @Test
    public void testLessThanOrEqual_Less() {
        block = RelationalOperator.create("testLessEqual", "test", "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("5 <= 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanOrEqual_Equal() {
        block = RelationalOperator.create("testLessEqual", "test", "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 <= 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testLessThanOrEqual_Greater() {
        block = RelationalOperator.create("testLessEqual", "test", "<=", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("15 <= 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    // ===== Greater Than (>) Operator Tests =====

    @Test
    public void testGreaterThan_Greater() {
        block = RelationalOperator.create("testGreaterThan", "test", ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 15.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("15 > 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThan_Equal() {
        block = RelationalOperator.create("testGreaterThan", "test", ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 10.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 > 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThan_Less() {
        block = RelationalOperator.create("testGreaterThan", "test", ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, 5.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("5 > 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThan_NegativeValues() {
        block = RelationalOperator.create("testGreaterThan", "test", ">", mockModel);
        initializeOutputSignals(block);

        setScalarInput(0, -5.0);
        setScalarInput(1, -10.0);
        block.calculateOutput(0.0);
        assertEquals("-5 > -10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Greater Than or Equal (>=) Operator Tests =====

    @Test
    public void testGreaterThanOrEqual_Greater() {
        setScalarInput(0, 15.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("15 >= 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanOrEqual_Equal() {
        setScalarInput(0, 10.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("10 >= 10 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanOrEqual_Less() {
        setScalarInput(0, 5.0);
        setScalarInput(1, 10.0);
        block.calculateOutput(0.0);
        assertEquals("5 >= 10 should be false", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testGreaterThanOrEqual_ZeroValues() {
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);
        assertEquals("0 >= 0 should be true", 1.0, getScalarOutput(0), DELTA);
    }

    // ===== Performance Test =====

    @Test
    public void testPerformance() {
        setScalarInput(0, 15.0);
        setScalarInput(1, 10.0);

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
