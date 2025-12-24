package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Sum block.
 * Tests calculateOutput() directly without WebSocket dependencies.
 *
 * Test Coverage:
 * - Scalar addition/subtraction
 * - Matrix element-wise operations
 * - Multiple inputs (2, 3, 4+ inputs)
 * - Edge cases (zeros, negatives, large/small values)
 * - Performance benchmarks
 */
public class SumTest extends DirectBlockTestBase {

    private Sum sumBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create Sum block with 2 inputs: ++
        sumBlock = Sum.create("testSum", "test", "++", mockModel);
        return sumBlock;
    }

    @Test
    public void testTwoInputAddition() {
        // Test: 3.0 + 4.0 = 7.0
        setScalarInput(0, 3.0);
        setScalarInput(1, 4.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Sum should add 3.0 + 4.0", 7.0, output, DELTA);
    }

    @Test
    public void testTwoInputSubtraction() throws Exception {
        // Create Sum block with subtraction: +-
        Sum subBlock = Sum.create("subtract", "test", "+-", mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        // Set inputs using helper method (mocks the connection chain properly)
        Data input0 = new Data(10.0);
        Data input1 = new Data(3.0);

        // Mock connections for this block
        mockInputPortConnection(subBlock, 0, input0);
        mockInputPortConnection(subBlock, 1, input1);

        // Calculate: 10.0 - 3.0 = 7.0
        subBlock.calculateOutput(0.0);

        OutputSignal outputSignal = subBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Subtraction should give 10.0 - 3.0", 7.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testThreeInputMixed() throws Exception {
        // Create Sum block: +-+
        Sum mixedBlock = Sum.create("mixed", "test", "+-+", mockModel);
        initializeOutputSignals(mixedBlock);
        mixedBlock.calculateInit();

        // Set inputs: 10.0 - 3.0 + 5.0 = 12.0
        mockInputPortConnection(mixedBlock, 0, new Data(10.0));
        mockInputPortConnection(mixedBlock, 1, new Data(3.0));
        mockInputPortConnection(mixedBlock, 2, new Data(5.0));

        mixedBlock.calculateOutput(0.0);

        OutputSignal outputSignal = mixedBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Mixed sum should give 10.0 - 3.0 + 5.0", 12.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testZeroInputs() {
        // Test: 0.0 + 0.0 = 0.0
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Sum of zeros should be zero", 0.0, output, DELTA);
    }

    @Test
    public void testNegativeInputs() {
        // Test: -5.0 + -3.0 = -8.0
        setScalarInput(0, -5.0);
        setScalarInput(1, -3.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Sum of negatives", -8.0, output, DELTA);
    }

    @Test
    public void testMixedSignInputs() {
        // Test: 10.0 + (-4.0) = 6.0
        setScalarInput(0, 10.0);
        setScalarInput(1, -4.0);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Sum of mixed signs", 6.0, output, DELTA);
    }

    @Test
    public void testMatrixAddition() {
        // Test: Matrix element-wise addition
        double[][] matrix1 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] matrix2 = {{5.0, 6.0}, {7.0, 8.0}};
        double[][] expected = {{6.0, 8.0}, {10.0, 12.0}};

        setMatrixInput(0, matrix1);
        setMatrixInput(1, matrix2);
        block.calculateOutput(0.0);

        assertMatrixOutput(0, expected);
    }

    @Test
    public void testMatrixSubtraction() throws Exception {
        // Create Sum block with subtraction: +-
        Sum subBlock = Sum.create("matrixSub", "test", "+-", mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        // Create matrices
        Matrix matrix1 = new Matrix(new double[][]{{10.0, 20.0}, {30.0, 40.0}});
        Matrix matrix2 = new Matrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        // Set inputs
        Data input0 = new Data(matrix1.getRowDimension(), matrix1.getColumnDimension());
        input0.setMatrix(matrix1);
        Data input1 = new Data(matrix2.getRowDimension(), matrix2.getColumnDimension());
        input1.setMatrix(matrix2);

        mockInputPortConnection(subBlock, 0, input0);
        mockInputPortConnection(subBlock, 1, input1);

        // Calculate
        subBlock.calculateOutput(0.0);

        // Verify
        OutputSignal outputSignal = subBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        Matrix outputMatrix = outputData.getMatrix();

        double[][] expected = {{9.0, 18.0}, {27.0, 36.0}};
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals("Matrix subtraction element [" + i + "][" + j + "]",
                           expected[i][j], outputMatrix.get(i, j), DELTA);
            }
        }
    }

    @Test
    public void testLargeValues() {
        // Test: Large values
        setScalarInput(0, 1e10);
        setScalarInput(1, 2e10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Sum should handle large values", 3e10, output, 1.0);
    }

    @Test
    public void testSmallValues() {
        // Test: Small values
        setScalarInput(0, 1e-10);
        setScalarInput(1, 2e-10);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Sum should handle small values", 3e-10, output, 1e-20);
    }

    @Test
    public void testCancellation() throws Exception {
        // Test: Values that cancel out
        Sum subBlock = Sum.create("cancel", "test", "+-", mockModel);
        initializeOutputSignals(subBlock);
        subBlock.calculateInit();

        // Set inputs: 5.0 - 5.0 = 0.0
        mockInputPortConnection(subBlock, 0, new Data(5.0));
        mockInputPortConnection(subBlock, 1, new Data(5.0));

        subBlock.calculateOutput(0.0);

        OutputSignal outputSignal = subBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Cancellation should give zero", 0.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testFourInputs() throws Exception {
        // Create Sum block with 4 inputs: ++-+
        Sum fourInputBlock = Sum.create("fourInput", "test", "++-+", mockModel);
        initializeOutputSignals(fourInputBlock);
        fourInputBlock.calculateInit();

        // Set inputs: 10 + 5 - 3 + 2 = 14
        mockInputPortConnection(fourInputBlock, 0, new Data(10.0));
        mockInputPortConnection(fourInputBlock, 1, new Data(5.0));
        mockInputPortConnection(fourInputBlock, 2, new Data(3.0));
        mockInputPortConnection(fourInputBlock, 3, new Data(2.0));

        fourInputBlock.calculateOutput(0.0);

        OutputSignal outputSignal = fourInputBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Four input sum", 14.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testAllSubtraction() throws Exception {
        // Create Sum block: ---
        Sum allSubBlock = Sum.create("allSub", "test", "---", mockModel);
        initializeOutputSignals(allSubBlock);
        allSubBlock.calculateInit();

        // Set inputs: -5 - 3 - 2 = -10
        mockInputPortConnection(allSubBlock, 0, new Data(5.0));
        mockInputPortConnection(allSubBlock, 1, new Data(3.0));
        mockInputPortConnection(allSubBlock, 2, new Data(2.0));

        allSubBlock.calculateOutput(0.0);

        OutputSignal outputSignal = allSubBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("All subtraction", -10.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 5.0);
        setScalarInput(1, 3.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Sum calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion
        assertTrue("Sum calculateOutput should be fast", avgTime < 10000);
    }

    @Test
    public void testMultipleCalculations() {
        // Test: Multiple calculations with different inputs
        double[][] testCases = {
            {1.0, 2.0, 3.0},
            {-5.0, 3.0, -2.0},
            {0.0, 0.0, 0.0},
            {100.0, -50.0, 50.0},
            {1e-5, 2e-5, 3e-5}
        };

        for (double[] testCase : testCases) {
            setScalarInput(0, testCase[0]);
            setScalarInput(1, testCase[1]);
            block.calculateOutput(0.0);

            double output = getScalarOutput(0);
            assertEquals("Sum should add " + testCase[0] + " + " + testCase[1],
                testCase[2], output, DELTA);
        }
    }

    @Test
    public void testEdgeCases() {
        // Edge case: Very small positive values
        setScalarInput(0, Double.MIN_VALUE);
        setScalarInput(1, Double.MIN_VALUE);
        block.calculateOutput(0.0);
        double output = getScalarOutput(0);
        assertEquals("Sum of MIN_VALUE", 2 * Double.MIN_VALUE, output, 1e-300);

        // Edge case: Mixing very large and very small
        setScalarInput(0, 1e10);
        setScalarInput(1, 1e-10);
        block.calculateOutput(0.0);
        output = getScalarOutput(0);
        assertEquals("Sum of large and small", 1e10, output, 1.0);
    }

    @Test
    public void testPrecision() {
        // Test: Precision with floating point arithmetic
        setScalarInput(0, 0.1);
        setScalarInput(1, 0.2);
        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Sum should handle floating point precision", 0.3, output, 1e-10);
    }

    @Test
    public void testRepeatedCalculation() {
        // Test: Same inputs calculated multiple times should give same result
        setScalarInput(0, 7.5);
        setScalarInput(1, 2.5);

        block.calculateOutput(0.0);
        double firstOutput = getScalarOutput(0);

        block.calculateOutput(0.1);
        double secondOutput = getScalarOutput(0);

        block.calculateOutput(0.2);
        double thirdOutput = getScalarOutput(0);

        assertEquals("First calculation", 10.0, firstOutput, DELTA);
        assertEquals("Second calculation should match first", firstOutput, secondOutput, DELTA);
        assertEquals("Third calculation should match first", firstOutput, thirdOutput, DELTA);
    }

    @Test
    public void testSingleInput() throws Exception {
        // Create Sum block with single input (pass-through): +
        Sum singleBlock = Sum.create("single", "test", "+", mockModel);
        initializeOutputSignals(singleBlock);
        singleBlock.calculateInit();

        // Set input
        mockInputPortConnection(singleBlock, 0, new Data(42.0));

        singleBlock.calculateOutput(0.0);

        OutputSignal outputSignal = singleBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Single input pass-through", 42.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testSingleInputNegation() throws Exception {
        // Create Sum block with single negative input: -
        Sum negBlock = Sum.create("negate", "test", "-", mockModel);
        initializeOutputSignals(negBlock);
        negBlock.calculateInit();

        // Set input: -5.0
        mockInputPortConnection(negBlock, 0, new Data(5.0));

        negBlock.calculateOutput(0.0);

        OutputSignal outputSignal = negBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Single input negation", -5.0, outputData.getInitValue(), DELTA);
    }
}
