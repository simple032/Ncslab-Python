package com.ncslab.block;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.*;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.line.Line;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.Config;
import com.ncslab.system.NCSLabSystem;

import Jama.Matrix;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Direct Block Testing Base Class - Eliminates WebSocket Dependencies
 *
 * This class provides a Mockito-based testing infrastructure for direct block unit testing
 * without requiring full model context, database connections, or WebSocket communication.
 *
 * Key Features:
 * - Direct block instantiation with mocked dependencies
 * - Helper methods for mocking input signals and parameters
 * - calculateOutput() testing with predefined test scenarios
 * - Support for both scalar and matrix signal testing
 * - Performance comparison utilities
 *
 * Usage Pattern:
 * 1. Extend this class in your block test
 * 2. Override createBlock() to instantiate your specific block
 * 3. Use helper methods to set up test scenarios
 * 4. Call calculateOutput() and verify results
 *
 * Example:
 * <pre>
 * public class GainDirectTest extends DirectBlockTestBase {
 *     private Gain gainBlock;
 *
 *     protected Block createBlock() {
 *         return Gain.create("testGain", "test", 2.0, mockModel);
 *     }
 *
 *     public void testScalarMultiplication() {
 *         setScalarInput(0, 5.0);
 *         block.calculateOutput(0.0);
 *         assertEquals(10.0, getScalarOutput(0), DELTA);
 *     }
 * }
 * </pre>
 *
 * @author NCSLab Team
 * @version 2025
 */
public abstract class DirectBlockTestBase {

    // === Test Configuration ===
    protected static final double DELTA = 1e-10;
    protected static final double TOLERANCE = 1e-6;

    @Rule
    public TestName testName = new TestName();

    // === Mocked Dependencies ===
    @Mock
    protected CodeModelC mockModel;

    @Mock
    protected NCSLabSystem mockSystem;

    @Mock
    protected Config mockConfig;

    protected AutoCloseable mockitoCloseable;

    // === Test Subject ===
    protected Block block;

    // === Helper Data Structures ===
    protected Map<Integer, Data> inputDataMap = new HashMap<>();
    protected Map<Integer, Data> outputDataMap = new HashMap<>();
    protected Map<String, Parameter> parameterMap = new HashMap<>();

    /**
     * Initialize Mockito mocks and set up test environment
     */
    @Before
    public void setUpDirectTest() throws Exception {
        // Initialize Mockito annotations
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        // Configure mock model behavior
        when(mockModel.getModelMode()).thenReturn(ModelMode.Simulation);
        when(mockModel.getConfig()).thenReturn(mockConfig);

        // Configure mock config behavior
        when(mockConfig.getSolver()).thenReturn("VariableStepAuto");
        when(mockConfig.getFixedStep()).thenReturn(0.01);
        when(mockConfig.getStartTime()).thenReturn(0.0);
        when(mockConfig.getStopTime()).thenReturn(20.0);

        // Create the block under test
        block = createBlock();
        assertNotNull("Block creation failed - createBlock() returned null", block);

        // Initialize OutputSignals for all output ports (required before calculateInit)
        for (int i = 0; i < block.getOutputPortList().size(); i++) {
            OutputPort outputPort = block.getOutputPortList().get(i);
            if (outputPort.getOutputSignalC() == null) {
                // Create OutputSignal: (Block block, int id, int outputPortId, String localName)
                OutputSignal outputSignal = new OutputSignal(block, i, i, "out" + (i + 1));
                outputPort.setOutputSignalC(outputSignal);
            }
        }

        // Initialize block if needed
        if (block != null) {
            try {
                block.calculateInit();
            } catch (Exception e) {
                // Some blocks may not need initialization
                System.out.println("Block initialization skipped: " + e.getMessage());
            }
        }

        System.out.println("=== Starting Test: " + testName.getMethodName() + " ===");
    }

    /**
     * Clean up resources after test
     */
    @After
    public void tearDownDirectTest() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }

        // Clear data maps
        inputDataMap.clear();
        outputDataMap.clear();
        parameterMap.clear();

        System.out.println("=== Test Completed: " + testName.getMethodName() + " ===\n");
    }

    /**
     * Abstract method to create the block under test.
     * Subclasses must implement this to instantiate their specific block type.
     *
     * @return Block instance for testing
     */
    protected abstract Block createBlock() throws Exception;

    // ============================================================================
    // INPUT SIGNAL MOCKING METHODS
    // ============================================================================

    /**
     * Set scalar input value for a specific input port
     *
     * @param inputPortIndex Input port index (0-based)
     * @param value Scalar value to set
     */
    protected void setScalarInput(int inputPortIndex, double value) {
        if (inputPortIndex >= block.getInputPortList().size()) {
            throw new IllegalArgumentException("Input port index " + inputPortIndex + " out of range");
        }

        Data inputData = new Data(value);
        inputDataMap.put(inputPortIndex, inputData);

        // Mock the line connection so InputPort.getData() works
        mockInputPortConnection(inputPortIndex, inputData);
    }

    /**
     * Set matrix input value for a specific input port
     *
     * @param inputPortIndex Input port index (0-based)
     * @param matrix Matrix value to set
     */
    protected void setMatrixInput(int inputPortIndex, Matrix matrix) {
        if (inputPortIndex >= block.getInputPortList().size()) {
            throw new IllegalArgumentException("Input port index " + inputPortIndex + " out of range");
        }

        Data inputData = new Data(matrix.getRowDimension(), matrix.getColumnDimension());
        inputData.setMatrix(matrix);
        inputDataMap.put(inputPortIndex, inputData);

        // Mock the line connection so InputPort.getData() works
        mockInputPortConnection(inputPortIndex, inputData);
    }

    /**
     * Set matrix input from 2D array
     *
     * @param inputPortIndex Input port index (0-based)
     * @param values 2D array of values
     */
    protected void setMatrixInput(int inputPortIndex, double[][] values) {
        Matrix matrix = new Matrix(values);
        setMatrixInput(inputPortIndex, matrix);
    }

    /**
     * Set string input value for a specific input port
     *
     * @param inputPortIndex Input port index (0-based)
     * @param value String value to set
     */
    protected void setStringInput(int inputPortIndex, String value) {
        if (inputPortIndex >= block.getInputPortList().size()) {
            throw new IllegalArgumentException("Input port index " + inputPortIndex + " out of range");
        }

        com.ncslab.block.data.StringData inputData = new com.ncslab.block.data.StringData(value);
        inputDataMap.put(inputPortIndex, inputData);

        // Mock the line connection so InputPort.getData() works
        mockInputPortConnection(inputPortIndex, inputData);
    }

    /**
     * Mock input port connection with output signal
     * This is needed for blocks that access input via getLinkedLine().getLinkedOutputPort()
     */
    private void mockInputPortConnection(int inputPortIndex, Data inputData) {
        mockInputPortConnection(block, inputPortIndex, inputData);
    }

    /**
     * Mock input port connection for a specific block with output signal
     * This is needed for blocks that access input via getLinkedLine().getLinkedOutputPort()
     *
     * @param targetBlock The block to mock connections for
     * @param inputPortIndex Input port index (0-based)
     * @param inputData Data to provide through the mocked connection
     */
    protected void mockInputPortConnection(Block targetBlock, int inputPortIndex, Data inputData) {
        InputPort inputPort = targetBlock.getInputPortList().get(inputPortIndex);

        // Create mock line and output port
        Line mockLine = mock(Line.class);
        OutputPort mockOutputPort = mock(OutputPort.class);
        OutputSignal mockOutputSignal = mock(OutputSignal.class);

        // Configure mock behavior
        when(mockOutputSignal.getData()).thenReturn(inputData);
        when(mockOutputSignal.getDataType()).thenReturn(inputData.getDataType());
        when(mockOutputSignal.getHeight()).thenReturn(inputData.getDataType() == DataType.MATRIX ?
            inputData.getMatrix().getRowDimension() : 1);
        when(mockOutputSignal.getWidth()).thenReturn(inputData.getDataType() == DataType.MATRIX ?
            inputData.getMatrix().getColumnDimension() : 1);

        when(mockOutputPort.getOutputSignalC()).thenReturn(mockOutputSignal);
        when(mockLine.getLinkedOutputPort()).thenReturn(mockOutputPort);

        // Set the line on input port using reflection if needed
        try {
            java.lang.reflect.Field lineField = InputPort.class.getDeclaredField("linkedLine");
            lineField.setAccessible(true);
            lineField.set(inputPort, mockLine);
        } catch (Exception e) {
            System.err.println("Warning: Could not set linkedLine on InputPort: " + e.getMessage());
        }
    }

    /**
     * Initialize OutputSignals for a block (required before calculateInit)
     * This should be called after creating custom blocks in tests.
     *
     * @param targetBlock The block to initialize OutputSignals for
     */
    protected void initializeOutputSignals(Block targetBlock) {
        for (int i = 0; i < targetBlock.getOutputPortList().size(); i++) {
            OutputPort outputPort = targetBlock.getOutputPortList().get(i);
            if (outputPort.getOutputSignalC() == null) {
                // Create OutputSignal: (Block block, int id, int outputPortId, String localName)
                OutputSignal outputSignal = new OutputSignal(targetBlock, i, i, "out" + (i + 1));
                outputPort.setOutputSignalC(outputSignal);
            }
        }
    }

    // ============================================================================
    // OUTPUT SIGNAL RETRIEVAL METHODS
    // ============================================================================

    /**
     * Get scalar output value from a specific output port
     *
     * @param outputPortIndex Output port index (0-based)
     * @return Scalar output value
     */
    protected double getScalarOutput(int outputPortIndex) {
        if (outputPortIndex >= block.getOutputPortList().size()) {
            throw new IllegalArgumentException("Output port index " + outputPortIndex + " out of range");
        }

        OutputPort outputPort = block.getOutputPortList().get(outputPortIndex);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        if (outputSignal == null) {
            throw new IllegalStateException("Output signal is null - did you call calculateOutput()?");
        }

        Data outputData = outputSignal.getData();

        if (outputData == null) {
            throw new IllegalStateException("Output data is null - did you call calculateOutput()?");
        }

        if (outputData.getDataType() == DataType.MATRIX) {
            Matrix matrix = outputData.getMatrix();
            if (matrix.getRowDimension() == 1 && matrix.getColumnDimension() == 1) {
                return matrix.get(0, 0);
            }
            throw new IllegalStateException("Output is a matrix, not a scalar. Use getMatrixOutput() instead.");
        }

        return outputData.getInitValue();
    }

    /**
     * Get matrix output value from a specific output port
     *
     * @param outputPortIndex Output port index (0-based)
     * @return Matrix output value
     */
    protected Matrix getMatrixOutput(int outputPortIndex) {
        if (outputPortIndex >= block.getOutputPortList().size()) {
            throw new IllegalArgumentException("Output port index " + outputPortIndex + " out of range");
        }

        OutputPort outputPort = block.getOutputPortList().get(outputPortIndex);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        if (outputSignal == null) {
            throw new IllegalStateException("Output signal is null - did you call calculateOutput()?");
        }

        Data outputData = outputSignal.getData();

        if (outputData == null) {
            throw new IllegalStateException("Output data is null - did you call calculateOutput()?");
        }

        if (outputData.getDataType() == DataType.REAL) {
            // Convert scalar to 1x1 matrix
            Matrix result = new Matrix(1, 1);
            result.set(0, 0, outputData.getInitValue());
            return result;
        }

        return outputData.getMatrix();
    }

    /**
     * Get string output value from a specific output port
     *
     * @param outputPortIndex Output port index (0-based)
     * @return String output value
     */
    protected String getStringOutput(int outputPortIndex) {
        if (outputPortIndex >= block.getOutputPortList().size()) {
            throw new IllegalArgumentException("Output port index " + outputPortIndex + " out of range");
        }

        OutputPort outputPort = block.getOutputPortList().get(outputPortIndex);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        if (outputSignal == null) {
            throw new IllegalStateException("Output signal is null - did you call calculateOutput()?");
        }

        Data outputData = outputSignal.getData();

        if (outputData == null) {
            throw new IllegalStateException("Output data is null - did you call calculateOutput()?");
        }

        if (outputData instanceof com.ncslab.block.data.StringData) {
            return ((com.ncslab.block.data.StringData) outputData).getStringValue();
        }

        // Fallback to string representation
        String str = outputData.getInitString();
        if (str != null) {
            return str;
        }

        str = outputData.getDataString();
        if (str != null) {
            return str;
        }

        throw new IllegalStateException("Output is not a string. Use getScalarOutput() or getMatrixOutput() instead.");
    }

    /**
     * Get output data object directly
     *
     * @param outputPortIndex Output port index (0-based)
     * @return Data object
     */
    protected Data getOutputData(int outputPortIndex) {
        if (outputPortIndex >= block.getOutputPortList().size()) {
            throw new IllegalArgumentException("Output port index " + outputPortIndex + " out of range");
        }

        OutputPort outputPort = block.getOutputPortList().get(outputPortIndex);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        if (outputSignal == null) {
            throw new IllegalStateException("Output signal is null - did you call calculateOutput()?");
        }

        return outputSignal.getData();
    }

    // ============================================================================
    // PARAMETER MOCKING METHODS
    // ============================================================================

    /**
     * Create a parameter with scalar value
     *
     * @param name Parameter name
     * @param value Scalar value
     * @return Parameter object
     */
    protected Parameter createScalarParameter(String name, double value) {
        Parameter param = new Parameter(block, 1, name, String.valueOf(value));
        parameterMap.put(name, param);
        return param;
    }

    /**
     * Create a parameter with matrix value
     *
     * @param name Parameter name
     * @param matrix Matrix value
     * @return Parameter object
     */
    protected Parameter createMatrixParameter(String name, Matrix matrix) {
        Parameter param = new Parameter(block, 1, name, matrixToString(matrix));
        parameterMap.put(name, param);
        return param;
    }

    /**
     * Create a parameter with string value
     *
     * @param name Parameter name
     * @param value String value
     * @return Parameter object
     */
    protected Parameter createStringParameter(String name, String value) {
        Parameter param = new Parameter(block, 1, name, value);
        parameterMap.put(name, param);
        return param;
    }

    /**
     * Convert matrix to string representation for parameter initialization
     */
    private String matrixToString(Matrix matrix) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            if (i > 0) sb.append(";");
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                if (j > 0) sb.append(",");
                sb.append(matrix.get(i, j));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // ============================================================================
    // ASSERTION HELPER METHODS
    // ============================================================================

    /**
     * Assert that two matrices are equal within tolerance
     */
    protected void assertMatrixEquals(Matrix expected, Matrix actual, double delta) {
        assertNotNull("Expected matrix is null", expected);
        assertNotNull("Actual matrix is null", actual);
        assertEquals("Matrix row dimension mismatch", expected.getRowDimension(), actual.getRowDimension());
        assertEquals("Matrix column dimension mismatch", expected.getColumnDimension(), actual.getColumnDimension());

        for (int i = 0; i < expected.getRowDimension(); i++) {
            for (int j = 0; j < expected.getColumnDimension(); j++) {
                assertEquals(
                    String.format("Matrix element mismatch at [%d,%d]", i, j),
                    expected.get(i, j),
                    actual.get(i, j),
                    delta
                );
            }
        }
    }

    /**
     * Assert that output is a scalar with expected value
     */
    protected void assertScalarOutput(int outputPortIndex, double expectedValue) {
        double actualValue = getScalarOutput(outputPortIndex);
        assertEquals("Scalar output mismatch", expectedValue, actualValue, DELTA);
    }

    /**
     * Assert that output is a matrix with expected values
     */
    protected void assertMatrixOutput(int outputPortIndex, double[][] expectedValues) {
        Matrix expected = new Matrix(expectedValues);
        Matrix actual = getMatrixOutput(outputPortIndex);
        assertMatrixEquals(expected, actual, DELTA);
    }

    // ============================================================================
    // TEST SCENARIO HELPERS
    // ============================================================================

    /**
     * Run a test scenario with given inputs and verify outputs
     *
     * @param time Simulation time
     * @param inputValues Input values (one per input port)
     * @param expectedOutputValues Expected output values (one per output port)
     */
    protected void runTestScenario(double time, double[] inputValues, double[] expectedOutputValues) {
        // Set inputs
        for (int i = 0; i < inputValues.length; i++) {
            setScalarInput(i, inputValues[i]);
        }

        // Calculate output
        block.calculateOutput(time);

        // Verify outputs
        for (int i = 0; i < expectedOutputValues.length; i++) {
            assertScalarOutput(i, expectedOutputValues[i]);
        }
    }

    /**
     * Run multiple test scenarios
     *
     * @param scenarios Array of test scenarios (time, inputs, expected outputs)
     */
    protected void runTestScenarios(BiConsumer<double[], double[]> scenarioRunner, Object[][] scenarios) {
        for (Object[] scenario : scenarios) {
            double time = (double) scenario[0];
            double[] inputs = (double[]) scenario[1];
            double[] expectedOutputs = (double[]) scenario[2];

            System.out.println(String.format("Testing scenario: t=%.2f, inputs=%s",
                time, java.util.Arrays.toString(inputs)));

            runTestScenario(time, inputs, expectedOutputs);
        }
    }

    // ============================================================================
    // EDGE CASE TESTING HELPERS
    // ============================================================================

    /**
     * Test with zero input
     */
    protected void testZeroInput() {
        double[] inputs = new double[block.getInputPortList().size()];
        // All inputs are zero by default

        for (int i = 0; i < inputs.length; i++) {
            setScalarInput(i, 0.0);
        }

        block.calculateOutput(0.0);

        // Verify no exceptions and outputs are valid
        for (int i = 0; i < block.getOutputPortList().size(); i++) {
            Data output = getOutputData(i);
            assertNotNull("Output " + i + " is null with zero input", output);
        }
    }

    /**
     * Test with NaN input (should handle gracefully)
     */
    protected void testNaNInput() {
        for (int i = 0; i < block.getInputPortList().size(); i++) {
            setScalarInput(i, Double.NaN);
        }

        try {
            block.calculateOutput(0.0);

            // Verify outputs - some blocks may propagate NaN, others may handle it
            for (int i = 0; i < block.getOutputPortList().size(); i++) {
                Data output = getOutputData(i);
                assertNotNull("Output " + i + " is null with NaN input", output);
            }
        } catch (Exception e) {
            // Some blocks may throw exceptions for NaN - this is acceptable
            System.out.println("Block handled NaN with exception: " + e.getMessage());
        }
    }

    /**
     * Test with infinity input
     */
    protected void testInfinityInput() {
        for (int i = 0; i < block.getInputPortList().size(); i++) {
            setScalarInput(i, Double.POSITIVE_INFINITY);
        }

        try {
            block.calculateOutput(0.0);

            for (int i = 0; i < block.getOutputPortList().size(); i++) {
                Data output = getOutputData(i);
                assertNotNull("Output " + i + " is null with infinity input", output);
            }
        } catch (Exception e) {
            System.out.println("Block handled infinity with exception: " + e.getMessage());
        }
    }

    /**
     * Test with large input values
     */
    protected void testLargeValues() {
        double largeValue = 1e100;

        for (int i = 0; i < block.getInputPortList().size(); i++) {
            setScalarInput(i, largeValue);
        }

        block.calculateOutput(0.0);

        for (int i = 0; i < block.getOutputPortList().size(); i++) {
            Data output = getOutputData(i);
            assertNotNull("Output " + i + " is null with large input", output);
        }
    }

    /**
     * Test with small input values
     */
    protected void testSmallValues() {
        double smallValue = 1e-100;

        for (int i = 0; i < block.getInputPortList().size(); i++) {
            setScalarInput(i, smallValue);
        }

        block.calculateOutput(0.0);

        for (int i = 0; i < block.getOutputPortList().size(); i++) {
            Data output = getOutputData(i);
            assertNotNull("Output " + i + " is null with small input", output);
        }
    }

    // ============================================================================
    // PERFORMANCE TESTING UTILITIES
    // ============================================================================

    /**
     * Measure execution time of calculateOutput()
     *
     * @param iterations Number of iterations to run
     * @return Average execution time in nanoseconds
     */
    protected long measureCalculateOutputPerformance(int iterations) {
        // Warm up
        for (int i = 0; i < 10; i++) {
            block.calculateOutput(0.0);
        }

        // Measure
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            block.calculateOutput(0.0);
        }
        long endTime = System.nanoTime();

        long avgTime = (endTime - startTime) / iterations;
        System.out.println(String.format("Average calculateOutput() time: %.2f microseconds",
            avgTime / 1000.0));

        return avgTime;
    }

    /**
     * Print block information for debugging
     */
    protected void printBlockInfo() {
        System.out.println("Block Type: " + block.getBlockType());
        System.out.println("Block Name: " + block.getBlockName());
        System.out.println("Input Ports: " + block.getInputPortList().size());
        System.out.println("Output Ports: " + block.getOutputPortList().size());
        System.out.println("Parameters: " + block.getParameterList().size());
        System.out.println("States: " + block.getStateList().size());
    }

    // ============================================================================
    // HELPER METHODS FOR TESTING ADDITIONAL BLOCKS
    // ============================================================================

    /**
     * Set scalar input value for a specific block's input port
     *
     * @param targetBlock The block to set input for
     * @param inputPortIndex Input port index (0-based)
     * @param value Scalar value to set
     */
    protected void setScalarInputForBlock(Block targetBlock, int inputPortIndex, double value) {
        if (inputPortIndex >= targetBlock.getInputPortList().size()) {
            throw new IllegalArgumentException("Input port index " + inputPortIndex + " out of range");
        }

        Data inputData = new Data(value);

        // Mock the line connection so InputPort.getData() works
        mockInputPortConnection(targetBlock, inputPortIndex, inputData);
    }

    /**
     * Set matrix input value for a specific block's input port
     *
     * @param targetBlock The block to set input for
     * @param inputPortIndex Input port index (0-based)
     * @param matrix Matrix value to set
     */
    protected void setMatrixInputForBlock(Block targetBlock, int inputPortIndex, Matrix matrix) {
        if (inputPortIndex >= targetBlock.getInputPortList().size()) {
            throw new IllegalArgumentException("Input port index " + inputPortIndex + " out of range");
        }

        Data inputData = new Data(matrix.getRowDimension(), matrix.getColumnDimension());
        inputData.setMatrix(matrix);

        // Mock the line connection so InputPort.getData() works
        mockInputPortConnection(targetBlock, inputPortIndex, inputData);
    }

    /**
     * Get scalar output value from a specific block's output port
     *
     * @param targetBlock The block to get output from
     * @param outputPortIndex Output port index (0-based)
     * @return Scalar output value
     */
    protected double getScalarOutputForBlock(Block targetBlock, int outputPortIndex) {
        if (outputPortIndex >= targetBlock.getOutputPortList().size()) {
            throw new IllegalArgumentException("Output port index " + outputPortIndex + " out of range");
        }

        OutputPort outputPort = targetBlock.getOutputPortList().get(outputPortIndex);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        if (outputSignal == null) {
            throw new IllegalStateException("Output signal is null - did you call calculateOutput()?");
        }

        Data outputData = outputSignal.getData();

        if (outputData == null) {
            throw new IllegalStateException("Output data is null - did you call calculateOutput()?");
        }

        if (outputData.getDataType() == DataType.MATRIX) {
            Matrix matrix = outputData.getMatrix();
            if (matrix.getRowDimension() == 1 && matrix.getColumnDimension() == 1) {
                return matrix.get(0, 0);
            }
            throw new IllegalStateException("Output is a matrix, not a scalar. Use getMatrixOutputForBlock() instead.");
        }

        return outputData.getInitValue();
    }

    /**
     * Get matrix output value from a specific block's output port
     *
     * @param targetBlock The block to get output from
     * @param outputPortIndex Output port index (0-based)
     * @return Matrix output value
     */
    protected Matrix getMatrixOutputForBlock(Block targetBlock, int outputPortIndex) {
        if (outputPortIndex >= targetBlock.getOutputPortList().size()) {
            throw new IllegalArgumentException("Output port index " + outputPortIndex + " out of range");
        }

        OutputPort outputPort = targetBlock.getOutputPortList().get(outputPortIndex);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        if (outputSignal == null) {
            throw new IllegalStateException("Output signal is null - did you call calculateOutput()?");
        }

        Data outputData = outputSignal.getData();

        if (outputData == null) {
            throw new IllegalStateException("Output data is null - did you call calculateOutput()?");
        }

        if (outputData.getDataType() == DataType.REAL) {
            // Convert scalar to 1x1 matrix
            Matrix result = new Matrix(1, 1);
            result.set(0, 0, outputData.getInitValue());
            return result;
        }

        return outputData.getMatrix();
    }

    /**
     * Get output data object directly from a specific block
     *
     * @param targetBlock The block to get output from
     * @param outputPortIndex Output port index (0-based)
     * @return Data object
     */
    protected Data getOutputDataForBlock(Block targetBlock, int outputPortIndex) {
        if (outputPortIndex >= targetBlock.getOutputPortList().size()) {
            throw new IllegalArgumentException("Output port index " + outputPortIndex + " out of range");
        }

        OutputPort outputPort = targetBlock.getOutputPortList().get(outputPortIndex);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        if (outputSignal == null) {
            throw new IllegalStateException("Output signal is null - did you call calculateOutput()?");
        }

        return outputSignal.getData();
    }

    // ============================================================================
    // DIMENSION MANAGEMENT METHODS (for Route/Matrix blocks)
    // ============================================================================

    /**
     * Set output port dimensions for blocks that need explicit dimension setup
     * (e.g., Mux, Demux blocks that calculate output based on pre-set dimensions)
     *
     * @param outputPortIndex Output port index (0-based)
     * @param height Height of output matrix
     * @param width Width of output matrix
     */
    protected void setOutputDimensions(int outputPortIndex, int height, int width) {
        setOutputDimensionsForBlock(block, outputPortIndex, height, width);
    }

    /**
     * Set output port dimensions for a specific block
     *
     * @param targetBlock The block to set dimensions for
     * @param outputPortIndex Output port index (0-based)
     * @param height Height of output matrix
     * @param width Width of output matrix
     */
    protected void setOutputDimensionsForBlock(Block targetBlock, int outputPortIndex, int height, int width) {
        if (outputPortIndex >= targetBlock.getOutputPortList().size()) {
            throw new IllegalArgumentException("Output port index " + outputPortIndex + " out of range");
        }

        OutputPort outputPort = targetBlock.getOutputPortList().get(outputPortIndex);
        outputPort.setHeight(height);
        outputPort.setWidth(width);
        outputPort.getOutputSignalC().setHeight(height);
        outputPort.getOutputSignalC().setWidth(width);

        // Set appropriate data type based on dimensions
        if (height > 1 || width > 1) {
            outputPort.getOutputSignalC().setDataType(DataType.MATRIX);
        } else {
            outputPort.getOutputSignalC().setDataType(DataType.REAL);
        }
    }
}
