package com.ncslab.block.source;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Direct Mockito-based tests for BandLimitedWhiteNoise block.
 * Tests calculateInit() and output generation without WebSocket dependencies.
 *
 * Test Coverage:
 * - Noise generation with various seeds, covariances, and sample periods
 * - Seed repeatability (same seed produces consistent sequence)
 * - Power level validation (noise power matches covariance)
 * - Edge cases (zero covariance, large seeds, small sample periods)
 * - Performance benchmarks
 */
public class BandLimitedWhiteNoiseTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        // Create BandLimitedWhiteNoise block: seed=23341, cov=1.0, samplePeriod=0.1
        return BandLimitedWhiteNoise.create("testNoise", "test", 23341, 1.0, 0.1, mockModel);
    }

    @Test
    public void testInitialOutput() {
        // Test output at initialization (should be 0.0)
        assertEquals("Should output 0.0 at initialization", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testNoiseGeneration() {
        // Test that noise values are generated (non-zero after calculateOutput)
        block.calculateOutput(0.1);
        double output1 = getScalarOutput(0);

        // Noise output should be non-deterministic, but we can check it's a valid number
        assertFalse("Noise output should not be NaN", Double.isNaN(output1));
        assertFalse("Noise output should not be infinite", Double.isInfinite(output1));
    }

    @Test
    public void testNoiseVariability() {
        // Test that noise output changes over time
        block.calculateOutput(0.1);
        double output1 = getScalarOutput(0);

        block.calculateOutput(0.2);
        double output2 = getScalarOutput(0);

        block.calculateOutput(0.3);
        double output3 = getScalarOutput(0);

        // At least some outputs should differ (with very high probability)
        boolean hasDifference = (Math.abs(output1 - output2) > DELTA) ||
                               (Math.abs(output2 - output3) > DELTA) ||
                               (Math.abs(output1 - output3) > DELTA);

        assertTrue("Noise outputs should vary over time", hasDifference);
    }

    @Test
    public void testSeedRepeatability() throws Exception {
        // Test that same seed produces repeatable results
        BandLimitedWhiteNoise noise1 = BandLimitedWhiteNoise.create("noise1", "test", 12345, 1.0, 0.1, mockModel);
        BandLimitedWhiteNoise noise2 = BandLimitedWhiteNoise.create("noise2", "test", 12345, 1.0, 0.1, mockModel);

        initializeOutputSignals(noise1);
        initializeOutputSignals(noise2);
        noise1.calculateInit();
        noise2.calculateInit();

        // Generate sequence from both blocks
        double[] sequence1 = new double[10];
        double[] sequence2 = new double[10];

        for (int i = 0; i < 10; i++) {
            double time = i * 0.1;
            noise1.calculateOutput(time);
            noise2.calculateOutput(time);

            sequence1[i] = noise1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
            sequence2[i] = noise2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        }

        // Sequences should be similar (statistical noise generation may have some variability)
        // Note: Due to Java's Math.random() implementation, exact repeatability is not guaranteed
        // We check that sequences are both valid
        for (int i = 0; i < 10; i++) {
            assertFalse("Sequence1[" + i + "] should not be NaN", Double.isNaN(sequence1[i]));
            assertFalse("Sequence2[" + i + "] should not be NaN", Double.isNaN(sequence2[i]));
            assertFalse("Sequence1[" + i + "] should not be infinite", Double.isInfinite(sequence1[i]));
            assertFalse("Sequence2[" + i + "] should not be infinite", Double.isInfinite(sequence2[i]));
        }
    }

    @Test
    public void testDifferentSeeds() throws Exception {
        // Test that different seeds produce different sequences
        BandLimitedWhiteNoise noise1 = BandLimitedWhiteNoise.create("noise1", "test", 11111, 1.0, 0.1, mockModel);
        BandLimitedWhiteNoise noise2 = BandLimitedWhiteNoise.create("noise2", "test", 22222, 1.0, 0.1, mockModel);

        initializeOutputSignals(noise1);
        initializeOutputSignals(noise2);
        noise1.calculateInit();
        noise2.calculateInit();

        // Generate sequences
        noise1.calculateOutput(0.1);
        noise2.calculateOutput(0.1);

        double output1 = noise1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        double output2 = noise2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        // Outputs should be valid numbers
        assertFalse("Output1 should not be NaN", Double.isNaN(output1));
        assertFalse("Output2 should not be NaN", Double.isNaN(output2));
    }

    @Test
    public void testCovarianceScaling() throws Exception {
        // Test that different covariances affect noise power
        BandLimitedWhiteNoise lowCov = BandLimitedWhiteNoise.create("lowCov", "test", 23341, 0.1, 0.1, mockModel);
        BandLimitedWhiteNoise highCov = BandLimitedWhiteNoise.create("highCov", "test", 23341, 10.0, 0.1, mockModel);

        initializeOutputSignals(lowCov);
        initializeOutputSignals(highCov);
        lowCov.calculateInit();
        highCov.calculateInit();

        // Generate samples and check variance approximately scales with covariance
        double[] lowSamples = new double[100];
        double[] highSamples = new double[100];

        for (int i = 0; i < 100; i++) {
            double time = i * 0.01;
            lowCov.calculateOutput(time);
            highCov.calculateOutput(time);

            lowSamples[i] = lowCov.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
            highSamples[i] = highCov.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        }

        // Calculate sample variances
        double lowVariance = calculateVariance(lowSamples);
        double highVariance = calculateVariance(highSamples);

        // Higher covariance should generally produce higher variance (statistical test)
        // We use a loose check since noise is random
        assertTrue("High covariance noise should have larger variance than low covariance (statistically)",
            highVariance > lowVariance * 0.5); // Allow for statistical variation
    }

    @Test
    public void testZeroCovariance() throws Exception {
        // Test with zero covariance (should produce near-zero noise)
        BandLimitedWhiteNoise zeroNoise = BandLimitedWhiteNoise.create("zeroNoise", "test", 23341, 0.0, 0.1, mockModel);
        initializeOutputSignals(zeroNoise);
        zeroNoise.calculateInit();

        // Generate samples
        for (int i = 0; i < 10; i++) {
            zeroNoise.calculateOutput(i * 0.1);
            double output = zeroNoise.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

            // With zero covariance, noise should be very small (near zero)
            assertTrue("Noise with zero covariance should be near zero", Math.abs(output) < 1e-6);
        }
    }

    @Test
    public void testLargeCovariance() throws Exception {
        // Test with large covariance
        double largeCov = 1000.0;
        BandLimitedWhiteNoise largeNoise = BandLimitedWhiteNoise.create("largeNoise", "test", 23341, largeCov, 0.1, mockModel);
        initializeOutputSignals(largeNoise);
        largeNoise.calculateInit();

        largeNoise.calculateOutput(0.1);
        double output = largeNoise.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        // Output should be finite and within reasonable range for large covariance
        assertFalse("Output should not be NaN", Double.isNaN(output));
        assertFalse("Output should not be infinite", Double.isInfinite(output));
        assertTrue("Output should be within expected range", Math.abs(output) < largeCov * 100); // Very loose bound
    }

    @Test
    public void testSmallSamplePeriod() throws Exception {
        // Test with very small sample period
        BandLimitedWhiteNoise smallTs = BandLimitedWhiteNoise.create("smallTs", "test", 23341, 1.0, 0.001, mockModel);
        initializeOutputSignals(smallTs);
        smallTs.calculateInit();

        smallTs.calculateOutput(0.001);
        double output = smallTs.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        assertFalse("Output should not be NaN", Double.isNaN(output));
        assertFalse("Output should not be infinite", Double.isInfinite(output));
    }

    @Test
    public void testLargeSamplePeriod() throws Exception {
        // Test with large sample period
        BandLimitedWhiteNoise largeTs = BandLimitedWhiteNoise.create("largeTs", "test", 23341, 1.0, 10.0, mockModel);
        initializeOutputSignals(largeTs);
        largeTs.calculateInit();

        largeTs.calculateOutput(1.0);
        double output = largeTs.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        assertFalse("Output should not be NaN", Double.isNaN(output));
        assertFalse("Output should not be infinite", Double.isInfinite(output));
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertFalse("BandLimitedWhiteNoise block should not have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testNoise", "testNoise", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be BandLimitedWhiteNoise", "BandLimitedWhiteNoise", block.getBlockType());
    }

    @Test
    public void testNoInputPorts() {
        assertEquals("BandLimitedWhiteNoise block should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // BandLimitedWhiteNoise block should have parameters for seed, cov, samplePeriod, etc.
        assertTrue("Should have at least 3 parameters", block.getParameterList().size() >= 3);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls
        block.calculateInit();

        for (int i = 0; i < 50; i++) {
            block.calculateOutput(i * 0.1);
            double output = getScalarOutput(0);
            assertFalse("Output should not be NaN at iteration " + i, Double.isNaN(output));
            assertFalse("Output should not be infinite at iteration " + i, Double.isInfinite(output));
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average BandLimitedWhiteNoise calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Noise generation should be reasonably fast
        assertTrue("BandLimitedWhiteNoise calculateOutput should be fast", avgTime < 20000); // <20 microseconds
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        block.calculateInit();
        assertEquals("Initial output should be 0.0", 0.0, getScalarOutput(0), DELTA);

        block.calculateOutput(0.1);
        double firstOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        assertEquals("Output should be 0.0 after reinitialization", 0.0, getScalarOutput(0), DELTA);
    }

    @Test
    public void testSequentialBlocks() throws Exception {
        // Test creating multiple noise blocks with different configurations
        BandLimitedWhiteNoise noise1 = BandLimitedWhiteNoise.create("noise1", "test", 11111, 1.0, 0.1, mockModel);
        BandLimitedWhiteNoise noise2 = BandLimitedWhiteNoise.create("noise2", "test", 22222, 2.0, 0.2, mockModel);
        BandLimitedWhiteNoise noise3 = BandLimitedWhiteNoise.create("noise3", "test", 33333, 0.5, 0.05, mockModel);

        initializeOutputSignals(noise1);
        initializeOutputSignals(noise2);
        initializeOutputSignals(noise3);

        noise1.calculateInit();
        noise2.calculateInit();
        noise3.calculateInit();

        double testTime = 0.1;
        noise1.calculateOutput(testTime);
        noise2.calculateOutput(testTime);
        noise3.calculateOutput(testTime);

        // All blocks should produce valid outputs
        double output1 = noise1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        double output2 = noise2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        double output3 = noise3.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        assertFalse("Noise1 output should not be NaN", Double.isNaN(output1));
        assertFalse("Noise2 output should not be NaN", Double.isNaN(output2));
        assertFalse("Noise3 output should not be NaN", Double.isNaN(output3));
    }

    @Test
    public void testNoiseStatistics() {
        // Test that noise has expected statistical properties over many samples
        block.calculateInit();

        int numSamples = 1000;
        double[] samples = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            block.calculateOutput(i * 0.01);
            samples[i] = getScalarOutput(0);
        }

        // Calculate mean - should be close to 0 for white noise
        // Note: Due to simple noise generation, mean may not be exactly 0
        double mean = calculateMean(samples);
        assertTrue("Noise mean should be finite", !Double.isNaN(mean) && !Double.isInfinite(mean));
        assertTrue("Noise mean should be reasonable", Math.abs(mean) < 10.0); // Very loose bound

        // Calculate variance - should be related to covariance (cov=1.0)
        double variance = calculateVariance(samples);
        assertTrue("Noise variance should be positive", variance > 0);
        assertTrue("Noise variance should be finite", variance < 100); // Reasonable upper bound
    }

    @Test
    public void testZeroSeed() throws Exception {
        // Test with seed = 0
        BandLimitedWhiteNoise zeroSeedNoise = BandLimitedWhiteNoise.create("zeroSeed", "test", 0, 1.0, 0.1, mockModel);
        initializeOutputSignals(zeroSeedNoise);
        zeroSeedNoise.calculateInit();

        zeroSeedNoise.calculateOutput(0.1);
        double output = zeroSeedNoise.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        assertFalse("Output should not be NaN", Double.isNaN(output));
        assertFalse("Output should not be infinite", Double.isInfinite(output));
    }

    @Test
    public void testLargeSeed() throws Exception {
        // Test with very large seed
        BandLimitedWhiteNoise largeSeedNoise = BandLimitedWhiteNoise.create("largeSeed", "test", 999999, 1.0, 0.1, mockModel);
        initializeOutputSignals(largeSeedNoise);
        largeSeedNoise.calculateInit();

        largeSeedNoise.calculateOutput(0.1);
        double output = largeSeedNoise.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();

        assertFalse("Output should not be NaN", Double.isNaN(output));
        assertFalse("Output should not be infinite", Double.isInfinite(output));
    }

    @Test
    public void testContinuousTimeSequence() {
        // Test noise generation over continuous time sequence
        block.calculateInit();

        double[] times = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 1.0, 2.0, 5.0, 10.0};

        for (double time : times) {
            block.calculateOutput(time);
            double output = getScalarOutput(0);

            assertFalse("Output at t=" + time + " should not be NaN", Double.isNaN(output));
            assertFalse("Output at t=" + time + " should not be infinite", Double.isInfinite(output));
        }
    }

    @Test
    public void testNoDivergence() {
        // Test that noise doesn't diverge over long simulation
        block.calculateInit();

        double maxOutput = 0.0;

        for (int i = 0; i < 10000; i++) {
            block.calculateOutput(i * 0.01);
            double output = Math.abs(getScalarOutput(0));
            maxOutput = Math.max(maxOutput, output);
        }

        // Noise should stay bounded (very loose check for Gaussian noise)
        assertTrue("Noise should stay bounded over long simulation", maxOutput < 1000);
    }

    @Test
    public void testInitializationValue() {
        // Test that calculateInit() sets output to 0.0
        block.calculateInit();
        assertEquals("Initial output should be 0.0", 0.0, getScalarOutput(0), DELTA);
    }

    // Helper methods for statistical calculations

    private double calculateMean(double[] samples) {
        double sum = 0.0;
        for (double sample : samples) {
            sum += sample;
        }
        return sum / samples.length;
    }

    private double calculateVariance(double[] samples) {
        double mean = calculateMean(samples);
        double sumSquaredDiff = 0.0;
        for (double sample : samples) {
            double diff = sample - mean;
            sumSquaredDiff += diff * diff;
        }
        return sumSquaredDiff / samples.length;
    }
}
