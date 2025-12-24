package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.dto.block.specialized.route.MergeDto;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for Merge block.
 *
 * Tests DTO creation, time-based input selection, multiple input merging,
 * and edge cases.
 */
public class MergeTest extends DirectBlockTestBase {

    private Merge mergeBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create Merge block with 2 inputs, initialOutput=0.0
        mergeBlock = Merge.create("testMerge", "test", 2, mockModel);
        return mergeBlock;
    }

    @Test
    public void testDtoCreation() {
        // Test: DTO-based creation with validation
        MergeDto dto = MergeDto.builder()
            .blockName("dtoTest")
            .blockPath("test")
            .blockUUID("test-uuid")
            .numberOfInputs(com.ncslab.dto.common.TypedParameter.of(3))
            .initialOutput(com.ncslab.dto.common.TypedParameter.of(0.0))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of("Inherit: Inherit via internal rule"))
            .allowUnconnectedInputs(com.ncslab.dto.common.TypedParameter.of(false))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        assertTrue("DTO validation should pass", validation.isValid());

        // Create block from DTO
        Merge dtoBlock = new Merge(dto, mockModel);
        assertNotNull("DTO-based block should be created", dtoBlock);
        assertEquals("Block name should match", "dtoTest", dtoBlock.getBlockName());
    }

    @Test
    public void testTwoInputMerge() {
        // Test: Merge two inputs (selects most recent)
        setScalarInput(0, 10.0);
        setScalarInput(1, 20.0);

        block.calculateOutput(0.0);

        // Should select one of the inputs (in simplified version, selects last valid)
        double output = getScalarOutput(0);
        assertTrue("Output should be one of the inputs",
            output == 10.0 || output == 20.0);
    }

    @Test
    public void testThreeInputMerge() throws Exception {
        // Test: Merge three inputs
        Merge threeInputMerge = Merge.create("threeMerge", "test", 3, mockModel);
        initializeOutputSignals(threeInputMerge);
        threeInputMerge.calculateInit();

        mockInputPortConnection(threeInputMerge, 0, new Data(10.0));
        mockInputPortConnection(threeInputMerge, 1, new Data(20.0));
        mockInputPortConnection(threeInputMerge, 2, new Data(30.0));

        threeInputMerge.calculateOutput(0.0);

        // Should select one of the inputs
        double output = getScalarOutputForBlock(threeInputMerge, 0);
        assertTrue("Output should be one of the inputs",
            output == 10.0 || output == 20.0 || output == 30.0);
    }

    @Test
    public void testInitialOutputValue() {
        // Test: Initial output before any input updates
        block.calculateInit();

        double output = getScalarOutput(0);
        assertEquals("Initial output should be 0.0", 0.0, output, DELTA);
    }

    @Test
    public void testNonZeroInitialOutput() throws Exception {
        // Test: Non-zero initial output
        Merge nonZeroMerge = Merge.create("nonZeroMerge", "test", 2, 5.0, -1.0,
            "Inherit: Inherit via internal rule", false, mockModel);
        initializeOutputSignals(nonZeroMerge);
        nonZeroMerge.calculateInit();

        double output = getScalarOutputForBlock(nonZeroMerge, 0);
        assertEquals("Initial output should be 5.0", 5.0, output, DELTA);
    }

    @Test
    public void testNegativeInputs() {
        // Test: Negative input values
        setScalarInput(0, -10.5);
        setScalarInput(1, -20.3);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Output should be one of the negative inputs",
            Math.abs(output + 10.5) < DELTA || Math.abs(output + 20.3) < DELTA);
    }

    @Test
    public void testZeroInputs() {
        // Test: Zero inputs
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should handle zero inputs", 0.0, output, DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test: Large input values
        setScalarInput(0, 1e10);
        setScalarInput(1, 2e10);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Should handle large values",
            Math.abs(output - 1e10) < 1e-4 || Math.abs(output - 2e10) < 1e-4);
    }

    @Test
    public void testSmallValues() {
        // Test: Small input values
        setScalarInput(0, 1e-10);
        setScalarInput(1, 2e-10);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Should handle small values",
            Math.abs(output - 1e-10) < 1e-20 || Math.abs(output - 2e-10) < 1e-20);
    }

    @Test
    public void testMatrixInputs() {
        // Test: Matrix input merging
        double[][] matrix0 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] matrix1 = {{5.0, 6.0}, {7.0, 8.0}};

        setMatrixInput(0, matrix0);
        setMatrixInput(1, matrix1);

        block.calculateOutput(0.0);

        // Should select one of the matrices
        Matrix outputMatrix = getMatrixOutput(0);
        assertNotNull("Output matrix should exist", outputMatrix);
        assertEquals("Matrix height should be 2", 2, outputMatrix.getRowDimension());
        assertEquals("Matrix width should be 2", 2, outputMatrix.getColumnDimension());
    }

    @Test
    public void testManyInputs() throws Exception {
        // Test: Many inputs
        int numInputs = 5;
        Merge manyMerge = Merge.create("manyMerge", "test", numInputs, mockModel);
        initializeOutputSignals(manyMerge);
        manyMerge.calculateInit();

        // Set all inputs
        for (int i = 0; i < numInputs; i++) {
            mockInputPortConnection(manyMerge, i, new Data(i * 10.0));
        }

        manyMerge.calculateOutput(0.0);

        // Should select one of the inputs
        double output = getScalarOutputForBlock(manyMerge, 0);
        boolean validOutput = false;
        for (int i = 0; i < numInputs; i++) {
            if (Math.abs(output - i * 10.0) < DELTA) {
                validOutput = true;
                break;
            }
        }
        assertTrue("Output should be one of the inputs", validOutput);
    }

    @Test
    public void testAllowUnconnectedInputsTrue() throws Exception {
        // Test: Allow unconnected inputs enabled
        Merge allowUnconnected = Merge.create("allowUnconnected", "test", 3, 0.0, -1.0,
            "Inherit: Inherit via internal rule", true, mockModel);

        assertNotNull("Block with allowUnconnectedInputs=true should be created", allowUnconnected);
    }

    @Test
    public void testAllowUnconnectedInputsFalse() throws Exception {
        // Test: Allow unconnected inputs disabled
        Merge disallowUnconnected = Merge.create("disallowUnconnected", "test", 3, 0.0, -1.0,
            "Inherit: Inherit via internal rule", false, mockModel);

        assertNotNull("Block with allowUnconnectedInputs=false should be created", disallowUnconnected);
    }

    @Test
    public void testSampleTimeInherited() throws Exception {
        // Test: Inherited sample time (-1)
        Merge inheritedMerge = Merge.create("inheritedMerge", "test", 2, 0.0, -1.0,
            "Inherit: Inherit via internal rule", false, mockModel);

        assertNotNull("Block with inherited sample time should be created", inheritedMerge);
    }

    @Test
    public void testSampleTimeContinuous() throws Exception {
        // Test: Continuous sample time (0)
        Merge continuousMerge = Merge.create("continuousMerge", "test", 2, 0.0, 0.0,
            "Inherit: Inherit via internal rule", false, mockModel);

        assertNotNull("Block with continuous sample time should be created", continuousMerge);
    }

    @Test
    public void testSampleTimeDiscrete() throws Exception {
        // Test: Discrete sample time (>0)
        Merge discreteMerge = Merge.create("discreteMerge", "test", 2, 0.0, 0.1,
            "Inherit: Inherit via internal rule", false, mockModel);

        assertNotNull("Block with discrete sample time should be created", discreteMerge);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 10.0);
        setScalarInput(1, 20.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Merge calculateOutput() time: " +
            avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds
        assertTrue("Merge calculateOutput should be fast", avgTime < 20000);
    }

    @Test
    public void testMultipleCalculations() {
        // Test: Multiple calculateOutput() calls with different inputs
        double[][] testCases = {
            {1.0, 2.0},
            {10.0, 20.0},
            {-5.0, -10.0},
            {0.0, 0.0},
            {100.0, 200.0}
        };

        for (double[] testCase : testCases) {
            setScalarInput(0, testCase[0]);
            setScalarInput(1, testCase[1]);
            block.calculateOutput(0.0);

            double output = getScalarOutput(0);
            assertTrue("Output should be one of the inputs",
                Math.abs(output - testCase[0]) < DELTA ||
                Math.abs(output - testCase[1]) < DELTA);
        }
    }

    @Test
    public void testSequentialUpdates() {
        // Test: Sequential input updates
        for (int i = 0; i < 5; i++) {
            setScalarInput(0, i * 10.0);
            setScalarInput(1, i * 20.0);
            block.calculateOutput(i * 0.1);

            // Verify no exceptions
            assertNotNull("Merge should handle sequential updates", block);
        }
    }

    @Test
    public void testMixedMagnitudes() {
        // Test: Mixed magnitude inputs
        setScalarInput(0, 1e10);
        setScalarInput(1, 1e-10);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertTrue("Should handle mixed magnitudes",
            Math.abs(output - 1e10) < 1e-4 || Math.abs(output - 1e-10) < 1e-20);
    }

    @Test
    public void testBlockInfo() {
        // Test: Block information
        printBlockInfo();

        assertEquals("Block type should be Merge", "Merge", block.getBlockType());
        assertEquals("Should have 2 input ports", 2, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }
}
