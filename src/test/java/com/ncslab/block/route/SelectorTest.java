package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.dto.block.specialized.route.SelectorDto;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for Selector block.
 *
 * Tests DTO creation, index-based signal selection, MATLAB-style indexing,
 * various index modes, and edge cases.
 */
public class SelectorTest extends DirectBlockTestBase {

    private Selector selectorBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create Selector with default index vector "[1]"
        selectorBlock = Selector.create("testSelector", "test", "[1]", mockModel);
        return selectorBlock;
    }

    @Test
    public void testDtoCreation() {
        // Test: DTO-based creation with validation
        SelectorDto dto = SelectorDto.builder()
            .blockName("dtoTest")
            .blockPath("test")
            .blockUUID("test-uuid")
            .indexMode(com.ncslab.dto.common.TypedParameter.of("Index vector"))
            .indexVector(com.ncslab.dto.common.TypedParameter.of("[1, 3, 5]"))
            .indexOptions(com.ncslab.dto.common.TypedParameter.of("All"))
            .numberOfDimensions(com.ncslab.dto.common.TypedParameter.of(1))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of("Inherit: Same as input"))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        assertTrue("DTO validation should pass", validation.isValid());

        // Create block from DTO
        Selector dtoBlock = new Selector(dto, mockModel);
        assertNotNull("DTO-based block should be created", dtoBlock);
        assertEquals("Block name should match", "dtoTest", dtoBlock.getBlockName());
    }

    @Test
    public void testSingleElementSelection() throws Exception {
        // Test: Select single element from vector (index 1)
        Selector singleSelector = Selector.create("singleSelector", "test", "[1]", mockModel);
        initializeOutputSignals(singleSelector);
        singleSelector.calculateInit();

        // Create vector input [10, 20, 30]
        Matrix inputMatrix = new Matrix(3, 1);
        inputMatrix.set(0, 0, 10.0);
        inputMatrix.set(1, 0, 20.0);
        inputMatrix.set(2, 0, 30.0);
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(singleSelector, 0, inputData);
        singleSelector.calculateOutput(0.0);

        double output = getScalarOutputForBlock(singleSelector, 0);
        assertEquals("Index 1 should select first element", 10.0, output, DELTA);
    }

    @Test
    public void testMultipleElementSelection() throws Exception {
        // Test: Select multiple elements using array notation [1, 3, 5]
        Selector multiSelector = Selector.create("multiSelector", "test", "[1, 3, 5]", mockModel);
        initializeOutputSignals(multiSelector);
        multiSelector.calculateInit();

        // Create vector input [10, 20, 30, 40, 50]
        Matrix inputMatrix = new Matrix(5, 1);
        for (int i = 0; i < 5; i++) {
            inputMatrix.set(i, 0, (i + 1) * 10.0);
        }
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(multiSelector, 0, inputData);
        multiSelector.calculateOutput(0.0);

        // Output should be [10, 30, 50] as a column vector
        Matrix output = getMatrixOutputForBlock(multiSelector, 0);
        assertEquals("Should output 3 elements", 3, output.getRowDimension());
        assertEquals("Index 1 -> 10.0", 10.0, output.get(0, 0), DELTA);
        assertEquals("Index 3 -> 30.0", 30.0, output.get(1, 0), DELTA);
        assertEquals("Index 5 -> 50.0", 50.0, output.get(2, 0), DELTA);
    }

    @Test
    public void testMATLABRangeSyntax() throws Exception {
        // Test: MATLAB range syntax "1:3" (select elements 1, 2, 3)
        Selector rangeSelector = Selector.create("rangeSelector", "test", "1:3", mockModel);
        initializeOutputSignals(rangeSelector);
        rangeSelector.calculateInit();

        // Create vector input [10, 20, 30, 40, 50]
        Matrix inputMatrix = new Matrix(5, 1);
        for (int i = 0; i < 5; i++) {
            inputMatrix.set(i, 0, (i + 1) * 10.0);
        }
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(rangeSelector, 0, inputData);
        rangeSelector.calculateOutput(0.0);

        // Output should be [10, 20, 30]
        Matrix output = getMatrixOutputForBlock(rangeSelector, 0);
        assertEquals("Should output 3 elements", 3, output.getRowDimension());
        assertEquals("Element 1", 10.0, output.get(0, 0), DELTA);
        assertEquals("Element 2", 20.0, output.get(1, 0), DELTA);
        assertEquals("Element 3", 30.0, output.get(2, 0), DELTA);
    }

    @Test
    public void testMATLABRangeWithStep() throws Exception {
        // Test: MATLAB range with step "1:2:10" (odd indices: 1, 3, 5, 7, 9)
        Selector stepSelector = Selector.create("stepSelector", "test", "1:2:10", mockModel);
        initializeOutputSignals(stepSelector);
        stepSelector.calculateInit();

        // Create vector input [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
        Matrix inputMatrix = new Matrix(10, 1);
        for (int i = 0; i < 10; i++) {
            inputMatrix.set(i, 0, (i + 1) * 10.0);
        }
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(stepSelector, 0, inputData);
        stepSelector.calculateOutput(0.0);

        // Output should be [10, 30, 50, 70, 90] (indices 1, 3, 5, 7, 9)
        Matrix output = getMatrixOutputForBlock(stepSelector, 0);
        assertEquals("Should output 5 elements", 5, output.getRowDimension());
        assertEquals("Index 1", 10.0, output.get(0, 0), DELTA);
        assertEquals("Index 3", 30.0, output.get(1, 0), DELTA);
        assertEquals("Index 5", 50.0, output.get(2, 0), DELTA);
    }

    @Test
    public void testIndexModeVector() throws Exception {
        // Test: Index vector mode (explicit list)
        Selector vectorSelector = Selector.create("vectorSelector", "test", "Index vector",
            "[2, 4]", "All", 1, -1.0, "Inherit: Inherit via internal rule", mockModel);
        initializeOutputSignals(vectorSelector);
        vectorSelector.calculateInit();

        // Create vector input [10, 20, 30, 40, 50]
        Matrix inputMatrix = new Matrix(5, 1);
        for (int i = 0; i < 5; i++) {
            inputMatrix.set(i, 0, (i + 1) * 10.0);
        }
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(vectorSelector, 0, inputData);
        vectorSelector.calculateOutput(0.0);

        // Output should be [20, 40] (indices 2 and 4)
        Matrix output = getMatrixOutputForBlock(vectorSelector, 0);
        assertEquals("Should output 2 elements", 2, output.getRowDimension());
        assertEquals("Index 2", 20.0, output.get(0, 0), DELTA);
        assertEquals("Index 4", 40.0, output.get(1, 0), DELTA);
    }

    @Test
    public void testScalarInput() throws Exception {
        // Test: Scalar input (select first element)
        Selector scalarSelector = Selector.create("scalarSelector", "test", "[1]", mockModel);
        initializeOutputSignals(scalarSelector);
        scalarSelector.calculateInit();

        mockInputPortConnection(scalarSelector, 0, new Data(42.0));
        scalarSelector.calculateOutput(0.0);

        double output = getScalarOutputForBlock(scalarSelector, 0);
        assertEquals("Should select scalar value", 42.0, output, DELTA);
    }

    @Test
    public void testRowVectorInput() throws Exception {
        // Test: Row vector input
        Selector rowSelector = Selector.create("rowSelector", "test", "[1, 3]", mockModel);
        initializeOutputSignals(rowSelector);
        rowSelector.calculateInit();

        // Create row vector [10, 20, 30, 40, 50]
        Matrix inputMatrix = new Matrix(1, 5);
        for (int i = 0; i < 5; i++) {
            inputMatrix.set(0, i, (i + 1) * 10.0);
        }
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(rowSelector, 0, inputData);
        rowSelector.calculateOutput(0.0);

        // Should select elements at indices 1 and 3
        Matrix output = getMatrixOutputForBlock(rowSelector, 0);
        assertEquals("Should output 2 elements", 2, output.getRowDimension());
        assertEquals("Index 1", 10.0, output.get(0, 0), DELTA);
        assertEquals("Index 3", 30.0, output.get(1, 0), DELTA);
    }

    @Test
    public void testNegativeValues() throws Exception {
        // Test: Negative input values
        Selector negSelector = Selector.create("negSelector", "test", "[1, 2, 3]", mockModel);
        initializeOutputSignals(negSelector);
        negSelector.calculateInit();

        Matrix inputMatrix = new Matrix(3, 1);
        inputMatrix.set(0, 0, -10.5);
        inputMatrix.set(1, 0, -20.3);
        inputMatrix.set(2, 0, -30.7);
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(negSelector, 0, inputData);
        negSelector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(negSelector, 0);
        assertEquals("Should handle negative values", -10.5, output.get(0, 0), DELTA);
        assertEquals("Should handle negative values", -20.3, output.get(1, 0), DELTA);
        assertEquals("Should handle negative values", -30.7, output.get(2, 0), DELTA);
    }

    @Test
    public void testZeroValues() throws Exception {
        // Test: Zero input values
        Selector zeroSelector = Selector.create("zeroSelector", "test", "[1, 2]", mockModel);
        initializeOutputSignals(zeroSelector);
        zeroSelector.calculateInit();

        Matrix inputMatrix = new Matrix(3, 1);
        inputMatrix.set(0, 0, 0.0);
        inputMatrix.set(1, 0, 0.0);
        inputMatrix.set(2, 0, 0.0);
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(zeroSelector, 0, inputData);
        zeroSelector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(zeroSelector, 0);
        assertEquals("Should handle zero values", 0.0, output.get(0, 0), DELTA);
        assertEquals("Should handle zero values", 0.0, output.get(1, 0), DELTA);
    }

    @Test
    public void testLargeValueInputs() throws Exception {
        // Test: Large input values
        Selector largeSelector = Selector.create("largeSelector", "test", "[1, 2]", mockModel);
        initializeOutputSignals(largeSelector);
        largeSelector.calculateInit();

        Matrix inputMatrix = new Matrix(2, 1);
        inputMatrix.set(0, 0, 1e10);
        inputMatrix.set(1, 0, 2e10);
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(largeSelector, 0, inputData);
        largeSelector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(largeSelector, 0);
        assertEquals("Should handle large values", 1e10, output.get(0, 0), 1e-4);
        assertEquals("Should handle large values", 2e10, output.get(1, 0), 1e-4);
    }

    @Test
    public void testSmallValueInputs() throws Exception {
        // Test: Small input values
        Selector smallSelector = Selector.create("smallSelector", "test", "[1, 2]", mockModel);
        initializeOutputSignals(smallSelector);
        smallSelector.calculateInit();

        Matrix inputMatrix = new Matrix(2, 1);
        inputMatrix.set(0, 0, 1e-10);
        inputMatrix.set(1, 0, 2e-10);
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(smallSelector, 0, inputData);
        smallSelector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(smallSelector, 0);
        assertEquals("Should handle small values", 1e-10, output.get(0, 0), 1e-20);
        assertEquals("Should handle small values", 2e-10, output.get(1, 0), 1e-20);
    }

    @Test
    public void testSingleIndexFromLargeVector() throws Exception {
        // Test: Select single element from large vector
        Selector singleFromLarge = Selector.create("singleFromLarge", "test", "[5]", mockModel);
        initializeOutputSignals(singleFromLarge);
        singleFromLarge.calculateInit();

        Matrix inputMatrix = new Matrix(10, 1);
        for (int i = 0; i < 10; i++) {
            inputMatrix.set(i, 0, (i + 1) * 100.0);
        }
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(singleFromLarge, 0, inputData);
        singleFromLarge.calculateOutput(0.0);

        double output = getScalarOutputForBlock(singleFromLarge, 0);
        assertEquals("Should select 5th element", 500.0, output, DELTA);
    }

    @Test
    public void testReverseSelection() throws Exception {
        // Test: Reverse order selection [5, 4, 3, 2, 1]
        Selector reverseSelector = Selector.create("reverseSelector", "test",
            "[5, 4, 3, 2, 1]", mockModel);
        initializeOutputSignals(reverseSelector);
        reverseSelector.calculateInit();

        Matrix inputMatrix = new Matrix(5, 1);
        for (int i = 0; i < 5; i++) {
            inputMatrix.set(i, 0, (i + 1) * 10.0);
        }
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(reverseSelector, 0, inputData);
        reverseSelector.calculateOutput(0.0);

        // Output should be [50, 40, 30, 20, 10]
        Matrix output = getMatrixOutputForBlock(reverseSelector, 0);
        assertEquals("Should reverse order", 50.0, output.get(0, 0), DELTA);
        assertEquals("Should reverse order", 40.0, output.get(1, 0), DELTA);
        assertEquals("Should reverse order", 30.0, output.get(2, 0), DELTA);
    }

    @Test
    public void testDuplicateIndices() throws Exception {
        // Test: Duplicate indices [1, 1, 2, 2]
        Selector dupSelector = Selector.create("dupSelector", "test", "[1, 1, 2, 2]", mockModel);
        initializeOutputSignals(dupSelector);
        dupSelector.calculateInit();

        Matrix inputMatrix = new Matrix(3, 1);
        inputMatrix.set(0, 0, 10.0);
        inputMatrix.set(1, 0, 20.0);
        inputMatrix.set(2, 0, 30.0);
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(dupSelector, 0, inputData);
        dupSelector.calculateOutput(0.0);

        // Output should be [10, 10, 20, 20]
        Matrix output = getMatrixOutputForBlock(dupSelector, 0);
        assertEquals("Should handle duplicates", 10.0, output.get(0, 0), DELTA);
        assertEquals("Should handle duplicates", 10.0, output.get(1, 0), DELTA);
        assertEquals("Should handle duplicates", 20.0, output.get(2, 0), DELTA);
        assertEquals("Should handle duplicates", 20.0, output.get(3, 0), DELTA);
    }

    @Test
    public void testSampleTimeInherited() throws Exception {
        // Test: Inherited sample time (-1)
        Selector inheritedSelector = Selector.create("inheritedSelector", "test", "Index vector",
            "[1]", "All", 1, -1.0, "Inherit: Inherit via internal rule", mockModel);

        assertNotNull("Block with inherited sample time should be created", inheritedSelector);
    }

    @Test
    public void testSampleTimeContinuous() throws Exception {
        // Test: Continuous sample time (0)
        Selector continuousSelector = Selector.create("continuousSelector", "test", "Index vector",
            "[1]", "All", 1, 0.0, "Inherit: Inherit via internal rule", mockModel);

        assertNotNull("Block with continuous sample time should be created", continuousSelector);
    }

    @Test
    public void testSampleTimeDiscrete() throws Exception {
        // Test: Discrete sample time (>0)
        Selector discreteSelector = Selector.create("discreteSelector", "test", "Index vector",
            "[1]", "All", 1, 0.1, "Inherit: Inherit via internal rule", mockModel);

        assertNotNull("Block with discrete sample time should be created", discreteSelector);
    }

    @Test
    public void testPerformance() throws Exception {
        // Performance test: Measure calculateOutput() execution time
        Selector perfSelector = Selector.create("perfSelector", "test", "[1, 2, 3]", mockModel);
        initializeOutputSignals(perfSelector);
        perfSelector.calculateInit();

        Matrix inputMatrix = new Matrix(5, 1);
        for (int i = 0; i < 5; i++) {
            inputMatrix.set(i, 0, (i + 1) * 10.0);
        }
        Data inputData = new Data(inputMatrix);

        mockInputPortConnection(perfSelector, 0, inputData);

        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            perfSelector.calculateOutput(0.0);
        }
        long endTime = System.nanoTime();
        long avgTime = (endTime - startTime) / 10000;

        System.out.println("Average Selector calculateOutput() time: " +
            avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 30 microseconds
        assertTrue("Selector calculateOutput should be fast", avgTime < 30000);
    }

    @Test
    public void testMultipleCalculations() throws Exception {
        // Test: Multiple calculateOutput() calls
        Selector multiCalcSelector = Selector.create("multiCalcSelector", "test", "[1, 2]", mockModel);
        initializeOutputSignals(multiCalcSelector);
        multiCalcSelector.calculateInit();

        for (int i = 0; i < 10; i++) {
            Matrix inputMatrix = new Matrix(3, 1);
            inputMatrix.set(0, 0, i * 10.0);
            inputMatrix.set(1, 0, i * 20.0);
            inputMatrix.set(2, 0, i * 30.0);
            Data inputData = new Data(inputMatrix);

            mockInputPortConnection(multiCalcSelector, 0, inputData);
            multiCalcSelector.calculateOutput(0.0);

            // Verify no exceptions
            assertNotNull("Selector should handle calculation " + i, multiCalcSelector);
        }
    }

    @Test
    public void testBlockInfo() {
        // Test: Block information
        printBlockInfo();

        assertEquals("Block type should be Selector", "Selector", block.getBlockType());
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }
}
