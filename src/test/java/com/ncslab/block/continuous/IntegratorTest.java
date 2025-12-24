package com.ncslab.block.continuous;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for Integrator block.
 * Tests calculateInit(), calculateDerivative(), and calculateOutput() directly
 * without WebSocket dependencies.
 */
public class IntegratorTest extends DirectBlockTestBase {

    private Integrator integratorBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create Integrator block with initial condition of 0.0
        integratorBlock = Integrator.create("testIntegrator", "test", 0.0, mockModel);
        return integratorBlock;
    }

    @Test
    public void testInitialization() {
        // Test: Integrator should initialize to initial condition
        block.calculateInit();

        OutputPort output = integratorBlock.getOutputPortList().get(0);
        Data outputData = output.getOutputSignalC().getData();

        assertNotNull("Output data should be initialized", outputData);
        assertEquals("Initial output should match initial condition", 0.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testConstantInput() {
        // Test: Constant input of 1.0 should accumulate linearly
        block.calculateInit();

        // Set constant input
        setScalarInput(0, 1.0);

        // Simulate integration over time
        block.calculateDerivative(0.0);
        State state = integratorBlock.getState();

        assertNotNull("State should be created", state);
        assertEquals("Derivative should equal input", 1.0, state.getDerivateData().getInitValue(), DELTA);
    }

    @Test
    public void testZeroInput() {
        // Test: Zero input should maintain initial condition
        block.calculateInit();
        setScalarInput(0, 0.0);

        block.calculateDerivative(0.0);
        block.calculateOutput(0.0);

        State state = integratorBlock.getState();
        assertEquals("Derivative should be zero", 0.0, state.getDerivateData().getInitValue(), DELTA);
        assertEquals("Output should remain at initial condition", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNegativeInput() {
        // Test: Negative input should decrease integrated value
        block.calculateInit();
        setScalarInput(0, -2.0);

        block.calculateDerivative(0.0);
        State state = integratorBlock.getState();

        assertEquals("Derivative should equal negative input", -2.0, state.getDerivateData().getInitValue(), DELTA);
    }

    @Test
    public void testNonZeroInitialCondition() throws Exception {
        // Create integrator with non-zero initial condition
        Integrator integrator = Integrator.create("testIC", "test", 5.0, mockModel);
        integrator.calculateInit();

        OutputPort output = integrator.getOutputPortList().get(0);
        Data outputData = output.getOutputSignalC().getData();

        assertEquals("Initial output should match initial condition", 5.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testSaturationLimits() throws Exception {
        // Create integrator with saturation limits
        Integrator satIntegrator = Integrator.create("satIntegrator", "test", 0.0,
            "none", "internal", true, 10.0, -10.0,
            false, false, 0.0, "Inherit: Same as input", false, mockModel);

        satIntegrator.calculateInit();

        // Set state to exceed upper limit
        State state = satIntegrator.getState();
        state.setData(new Data(15.0));

        satIntegrator.calculateOutput(0.0);

        // Verify saturation
        double output = satIntegrator.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Output should be saturated to upper limit", 10.0, output, DELTA);
    }

    @Test
    public void testLowerSaturationLimit() throws Exception {
        // Create integrator with saturation limits
        Integrator satIntegrator = Integrator.create("satIntegrator", "test", 0.0,
            "none", "internal", true, 10.0, -10.0,
            false, false, 0.0, "Inherit: Same as input", false, mockModel);

        satIntegrator.calculateInit();

        // Set state to exceed lower limit
        State state = satIntegrator.getState();
        state.setData(new Data(-15.0));

        satIntegrator.calculateOutput(0.0);

        // Verify saturation
        double output = satIntegrator.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Output should be saturated to lower limit", -10.0, output, DELTA);
    }

    @Test
    public void testMatrixIntegration() throws Exception {
        // Create integrator for matrix input
        Integrator matrixIntegrator = Integrator.create("matrixInt", "test", 0.0, mockModel);

        // Note: Matrix integration would require proper dimension setup via updateDimension()
        // This is a basic test to ensure the block can be created
        assertNotNull("Matrix integrator should be created", matrixIntegrator);
    }

    @Test
    public void testEdgeCaseNaN() throws Exception {
        // Test: NaN handling in integrator
        Integrator nanIntegrator = Integrator.create("nanInt", "test", 0.0, mockModel);
        nanIntegrator.calculateInit();

        // Set state to NaN
        State state = nanIntegrator.getState();
        state.setData(new Data(Double.NaN));

        nanIntegrator.calculateOutput(0.0);

        // Verify NaN is handled gracefully (should reset to initial condition)
        double output = nanIntegrator.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertFalse("Output should not be NaN after recovery", Double.isNaN(output));
        assertEquals("Output should reset to initial condition", 0.0, output, DELTA);
    }

    @Test
    public void testEdgeCaseInfinity() throws Exception {
        // Test: Infinity handling with saturation enabled
        Integrator infIntegrator = Integrator.create("infInt", "test", 0.0,
            "none", "internal", true, 100.0, -100.0,
            false, false, 0.0, "Inherit: Same as input", false, mockModel);

        infIntegrator.calculateInit();

        // Set state to positive infinity
        State state = infIntegrator.getState();
        state.setData(new Data(Double.POSITIVE_INFINITY));

        infIntegrator.calculateOutput(0.0);

        // Verify infinity is clamped to upper limit
        double output = infIntegrator.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        assertEquals("Positive infinity should be clamped to upper limit", 100.0, output, DELTA);
    }

    @Test
    public void testMultipleTimeSteps() {
        // Test: Integration over multiple time steps
        block.calculateInit();

        double[] inputs = {1.0, 2.0, 3.0, 2.0, 1.0};

        for (int i = 0; i < inputs.length; i++) {
            setScalarInput(0, inputs[i]);
            block.calculateDerivative(i * 0.1);

            State state = integratorBlock.getState();
            assertEquals("Derivative at step " + i + " should match input",
                inputs[i], state.getDerivateData().getInitValue(), DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();
        State state = integratorBlock.getState();
        state.setData(new Data(5.0));

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Integrator calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds on average
        assertTrue("Integrator calculateOutput should be fast", avgTime < 20000);
    }
}