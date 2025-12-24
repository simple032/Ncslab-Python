package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.dto.block.specialized.route.IndexVectorDto;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for IndexVector block.
 *
 * Tests DTO creation, index generation (MATLAB-style), zero-based/one-based indexing,
 * range notation parsing, and edge cases.
 */
public class IndexVectorTest extends DirectBlockTestBase {

    private IndexVector indexVectorBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create IndexVector with default range "1:10"
        indexVectorBlock = IndexVector.create("testIndexVector", "test", "1:10", mockModel);
        return indexVectorBlock;
    }

    @Test
    public void testDtoCreation() {
        // Test: DTO-based creation with validation
        IndexVectorDto dto = IndexVectorDto.builder()
            .blockName("dtoTest")
            .blockPath("test")
            .blockUUID("test-uuid")
            .indexMode(com.ncslab.dto.common.TypedParameter.of("One-based"))
            .indexParamArray(com.ncslab.dto.common.TypedParameter.of("1:5"))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of("int32"))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        assertTrue("DTO validation should pass", validation.isValid());

        // Create block from DTO
        IndexVector dtoBlock = new IndexVector(dto, mockModel);
        assertNotNull("DTO-based block should be created", dtoBlock);
        assertEquals("Block name should match", "dtoTest", dtoBlock.getBlockName());
    }

    @Test
    public void testOneBasedRange() {
        // Test: One-based range "1:10" should generate [1, 2, 3, ..., 10]
        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("Should generate 10 elements", 10, output.getRowDimension());
        assertEquals("First element should be 1", 1.0, output.get(0, 0), DELTA);
        assertEquals("Last element should be 10", 10.0, output.get(9, 0), DELTA);
    }

    @Test
    public void testOneBasedRangeWithStep() throws Exception {
        // Test: One-based range with step "1:2:10" should generate [1, 3, 5, 7, 9]
        IndexVector stepVector = IndexVector.create("stepVector", "test", "1:2:10", mockModel);
        initializeOutputSignals(stepVector);
        stepVector.calculateInit();

        stepVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(stepVector, 0);
        assertEquals("Should generate 5 elements", 5, output.getRowDimension());
        assertEquals("First element", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 3.0, output.get(1, 0), DELTA);
        assertEquals("Third element", 5.0, output.get(2, 0), DELTA);
        assertEquals("Fourth element", 7.0, output.get(3, 0), DELTA);
        assertEquals("Fifth element", 9.0, output.get(4, 0), DELTA);
    }

    @Test
    public void testZeroBasedRange() throws Exception {
        // Test: Zero-based range "1:10" should generate [0, 1, 2, ..., 9]
        IndexVector zeroBasedVector = IndexVector.create("zeroBasedVector", "test", "Zero-based",
            "1:10", -1.0, "int32", mockModel);
        initializeOutputSignals(zeroBasedVector);
        zeroBasedVector.calculateInit();

        zeroBasedVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(zeroBasedVector, 0);
        assertEquals("Should generate 10 elements", 10, output.getRowDimension());
        assertEquals("First element should be 0 (zero-based)", 0.0, output.get(0, 0), DELTA);
        assertEquals("Last element should be 9 (zero-based)", 9.0, output.get(9, 0), DELTA);
    }

    @Test
    public void testZeroBasedRangeWithStep() throws Exception {
        // Test: Zero-based range with step "1:2:10" should generate [0, 2, 4, 6, 8]
        IndexVector zeroStepVector = IndexVector.create("zeroStepVector", "test", "Zero-based",
            "1:2:10", -1.0, "int32", mockModel);
        initializeOutputSignals(zeroStepVector);
        zeroStepVector.calculateInit();

        zeroStepVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(zeroStepVector, 0);
        assertEquals("Should generate 5 elements", 5, output.getRowDimension());
        assertEquals("First element", 0.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 2.0, output.get(1, 0), DELTA);
        assertEquals("Third element", 4.0, output.get(2, 0), DELTA);
        assertEquals("Fourth element", 6.0, output.get(3, 0), DELTA);
        assertEquals("Fifth element", 8.0, output.get(4, 0), DELTA);
    }

    @Test
    public void testArrayNotation() throws Exception {
        // Test: Array notation "[1, 3, 5, 7]"
        IndexVector arrayVector = IndexVector.create("arrayVector", "test", "[1, 3, 5, 7]", mockModel);
        initializeOutputSignals(arrayVector);
        arrayVector.calculateInit();

        arrayVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(arrayVector, 0);
        assertEquals("Should generate 4 elements", 4, output.getRowDimension());
        assertEquals("First element", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 3.0, output.get(1, 0), DELTA);
        assertEquals("Third element", 5.0, output.get(2, 0), DELTA);
        assertEquals("Fourth element", 7.0, output.get(3, 0), DELTA);
    }

    @Test
    public void testArrayNotationZeroBased() throws Exception {
        // Test: Array notation with zero-based indexing "[1, 3, 5]" -> [0, 2, 4]
        IndexVector zeroArrayVector = IndexVector.create("zeroArrayVector", "test", "Zero-based",
            "[1, 3, 5]", -1.0, "int32", mockModel);
        initializeOutputSignals(zeroArrayVector);
        zeroArrayVector.calculateInit();

        zeroArrayVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(zeroArrayVector, 0);
        assertEquals("Should generate 3 elements", 3, output.getRowDimension());
        assertEquals("First element (zero-based)", 0.0, output.get(0, 0), DELTA);
        assertEquals("Second element (zero-based)", 2.0, output.get(1, 0), DELTA);
        assertEquals("Third element (zero-based)", 4.0, output.get(2, 0), DELTA);
    }

    @Test
    public void testSingleIndex() throws Exception {
        // Test: Single index "5"
        IndexVector singleVector = IndexVector.create("singleVector", "test", "5", mockModel);
        initializeOutputSignals(singleVector);
        singleVector.calculateInit();

        singleVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(singleVector, 0);
        assertEquals("Should generate 1 element", 1, output.getRowDimension());
        assertEquals("Should be index 5", 5.0, output.get(0, 0), DELTA);
    }

    @Test
    public void testNegativeStepRange() throws Exception {
        // Test: Negative step range "10:-1:1" (descending)
        IndexVector negStepVector = IndexVector.create("negStepVector", "test", "10:-1:1", mockModel);
        initializeOutputSignals(negStepVector);
        negStepVector.calculateInit();

        negStepVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(negStepVector, 0);
        assertEquals("Should generate 10 elements", 10, output.getRowDimension());
        assertEquals("First element", 10.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 9.0, output.get(1, 0), DELTA);
        assertEquals("Last element", 1.0, output.get(9, 0), DELTA);
    }

    @Test
    public void testNegativeStepWithLargeStep() throws Exception {
        // Test: Negative step "10:-2:1" -> [10, 8, 6, 4, 2]
        IndexVector negLargeStepVector = IndexVector.create("negLargeStepVector", "test",
            "10:-2:1", mockModel);
        initializeOutputSignals(negLargeStepVector);
        negLargeStepVector.calculateInit();

        negLargeStepVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(negLargeStepVector, 0);
        assertEquals("Should generate 5 elements", 5, output.getRowDimension());
        assertEquals("First element", 10.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 8.0, output.get(1, 0), DELTA);
        assertEquals("Third element", 6.0, output.get(2, 0), DELTA);
        assertEquals("Fourth element", 4.0, output.get(3, 0), DELTA);
        assertEquals("Fifth element", 2.0, output.get(4, 0), DELTA);
    }

    @Test
    public void testLargeRange() throws Exception {
        // Test: Large range "1:100"
        IndexVector largeVector = IndexVector.create("largeVector", "test", "1:100", mockModel);
        initializeOutputSignals(largeVector);
        largeVector.calculateInit();

        largeVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(largeVector, 0);
        assertEquals("Should generate 100 elements", 100, output.getRowDimension());
        assertEquals("First element", 1.0, output.get(0, 0), DELTA);
        assertEquals("Last element", 100.0, output.get(99, 0), DELTA);
    }

    @Test
    public void testSmallRange() throws Exception {
        // Test: Small range "1:3"
        IndexVector smallVector = IndexVector.create("smallVector", "test", "1:3", mockModel);
        initializeOutputSignals(smallVector);
        smallVector.calculateInit();

        smallVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(smallVector, 0);
        assertEquals("Should generate 3 elements", 3, output.getRowDimension());
        assertEquals("First element", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 2.0, output.get(1, 0), DELTA);
        assertEquals("Third element", 3.0, output.get(2, 0), DELTA);
    }

    @Test
    public void testLargeStepRange() throws Exception {
        // Test: Large step "1:10:100" -> [1, 11, 21, ..., 91]
        IndexVector largeStepVector = IndexVector.create("largeStepVector", "test",
            "1:10:100", mockModel);
        initializeOutputSignals(largeStepVector);
        largeStepVector.calculateInit();

        largeStepVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(largeStepVector, 0);
        assertEquals("Should generate 10 elements", 10, output.getRowDimension());
        assertEquals("First element", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 11.0, output.get(1, 0), DELTA);
        assertEquals("Last element", 91.0, output.get(9, 0), DELTA);
    }

    @Test
    public void testArrayWithWhitespace() throws Exception {
        // Test: Array notation with whitespace "[ 1 , 3 , 5 ]"
        IndexVector whitespaceVector = IndexVector.create("whitespaceVector", "test",
            "[ 1 , 3 , 5 ]", mockModel);
        initializeOutputSignals(whitespaceVector);
        whitespaceVector.calculateInit();

        whitespaceVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(whitespaceVector, 0);
        assertEquals("Should generate 3 elements", 3, output.getRowDimension());
        assertEquals("First element", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 3.0, output.get(1, 0), DELTA);
        assertEquals("Third element", 5.0, output.get(2, 0), DELTA);
    }

    @Test
    public void testCalculateInit() throws Exception {
        // Test: calculateInit() should initialize output with index vector
        IndexVector initVector = IndexVector.create("initVector", "test", "1:5", mockModel);
        initializeOutputSignals(initVector);
        initVector.calculateInit();

        Matrix output = getMatrixOutputForBlock(initVector, 0);
        assertEquals("Should have 5 elements after init", 5, output.getRowDimension());
        assertEquals("First element after init", 1.0, output.get(0, 0), DELTA);
    }

    @Test
    public void testMultipleCalculations() throws Exception {
        // Test: Multiple calculateOutput() calls should produce same result
        IndexVector multiCalcVector = IndexVector.create("multiCalcVector", "test",
            "1:3", mockModel);
        initializeOutputSignals(multiCalcVector);
        multiCalcVector.calculateInit();

        for (int i = 0; i < 10; i++) {
            multiCalcVector.calculateOutput(i * 0.1);

            Matrix output = getMatrixOutputForBlock(multiCalcVector, 0);
            assertEquals("Should always output same indices", 1.0, output.get(0, 0), DELTA);
            assertEquals("Should always output same indices", 2.0, output.get(1, 0), DELTA);
            assertEquals("Should always output same indices", 3.0, output.get(2, 0), DELTA);
        }
    }

    @Test
    public void testOutputIsVector() throws Exception {
        // Test: Output should be column vector
        IndexVector vectorOutput = IndexVector.create("vectorOutput", "test", "1:5", mockModel);
        initializeOutputSignals(vectorOutput);
        vectorOutput.calculateInit();

        vectorOutput.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(vectorOutput, 0);
        assertEquals("Output should be column vector (height=5)", 5, output.getRowDimension());
        assertEquals("Output should be column vector (width=1)", 1, output.getColumnDimension());
    }

    @Test
    public void testSampleTimeInherited() throws Exception {
        // Test: Inherited sample time (-1)
        IndexVector inheritedVector = IndexVector.create("inheritedVector", "test", "One-based",
            "1:5", -1.0, "int32", mockModel);

        assertNotNull("Block with inherited sample time should be created", inheritedVector);
    }

    @Test
    public void testSampleTimeContinuous() throws Exception {
        // Test: Continuous sample time (0)
        IndexVector continuousVector = IndexVector.create("continuousVector", "test", "One-based",
            "1:5", 0.0, "int32", mockModel);

        assertNotNull("Block with continuous sample time should be created", continuousVector);
    }

    @Test
    public void testSampleTimeDiscrete() throws Exception {
        // Test: Discrete sample time (>0)
        IndexVector discreteVector = IndexVector.create("discreteVector", "test", "One-based",
            "1:5", 0.1, "int32", mockModel);

        assertNotNull("Block with discrete sample time should be created", discreteVector);
    }

    @Test
    public void testPerformance() throws Exception {
        // Performance test: Measure calculateOutput() execution time
        IndexVector perfVector = IndexVector.create("perfVector", "test", "1:100", mockModel);
        initializeOutputSignals(perfVector);
        perfVector.calculateInit();

        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            perfVector.calculateOutput(0.0);
        }
        long endTime = System.nanoTime();
        long avgTime = (endTime - startTime) / 10000;

        System.out.println("Average IndexVector calculateOutput() time: " +
            avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 25 microseconds
        assertTrue("IndexVector calculateOutput should be fast", avgTime < 25000);
    }

    @Test
    public void testEmptyArrayNotation() throws Exception {
        // Test: Empty array notation "[]" should default to "[1]"
        IndexVector emptyVector = IndexVector.create("emptyVector", "test", "[]", mockModel);
        initializeOutputSignals(emptyVector);
        emptyVector.calculateInit();

        emptyVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(emptyVector, 0);
        assertTrue("Should have at least 1 element", output.getRowDimension() >= 1);
    }

    @Test
    public void testNonContiguousArray() throws Exception {
        // Test: Non-contiguous array "[1, 10, 5, 15]"
        IndexVector nonContiguousVector = IndexVector.create("nonContiguousVector", "test",
            "[1, 10, 5, 15]", mockModel);
        initializeOutputSignals(nonContiguousVector);
        nonContiguousVector.calculateInit();

        nonContiguousVector.calculateOutput(0.0);

        Matrix output = getMatrixOutputForBlock(nonContiguousVector, 0);
        assertEquals("Should generate 4 elements", 4, output.getRowDimension());
        assertEquals("First element", 1.0, output.get(0, 0), DELTA);
        assertEquals("Second element", 10.0, output.get(1, 0), DELTA);
        assertEquals("Third element", 5.0, output.get(2, 0), DELTA);
        assertEquals("Fourth element", 15.0, output.get(3, 0), DELTA);
    }

    @Test
    public void testBlockInfo() {
        // Test: Block information
        printBlockInfo();

        assertEquals("Block type should be IndexVector", "IndexVector", block.getBlockType());
        assertEquals("Should have 0 input ports (source block)", 0, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }
}
