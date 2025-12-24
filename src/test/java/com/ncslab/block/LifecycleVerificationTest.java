package com.ncslab.block;

import com.ncslab.block.data.Data;
import com.ncslab.block.lan.SimuBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Verification test for enhanced SimuBlock lifecycle methods.
 * Tests that all lifecycle methods can be called and default implementations work.
 */
public class LifecycleVerificationTest {

    /**
     * Test block implementing all lifecycle methods
     */
    private static class TestBlock implements SimuBlock {
        private boolean checkParametersCalled = false;
        private boolean initCalled = false;
        private boolean startCalled = false;
        private boolean enableCalled = false;
        private boolean outputCalled = false;
        private boolean derivativeCalled = false;
        private boolean updateCalled = false;
        private boolean discreteUpdateCalled = false;
        private boolean disableCalled = false;
        private boolean stopCalled = false;
        private boolean terminateCalled = false;
        private boolean resetCalled = false;

        @Override
        public void calculateCheckParameters() {
            checkParametersCalled = true;
        }

        @Override
        public void calculateInit() {
            initCalled = true;
        }

        @Override
        public void calculateStart() {
            startCalled = true;
        }

        @Override
        public void calculateEnable() {
            enableCalled = true;
        }

        @Override
        public void calculateOutput(double t) {
            outputCalled = true;
        }

        @Override
        public void calculateDerivative(double t) {
            derivativeCalled = true;
        }

        @Override
        public void calculateUpdate(double t) {
            updateCalled = true;
        }

        @Override
        public void calculateDiscreteUpdate(double t) {
            discreteUpdateCalled = true;
        }

        @Override
        public void calculateDisable() {
            disableCalled = true;
        }

        @Override
        public void calculateStop() {
            stopCalled = true;
        }

        @Override
        public void calculateTerminate(double t) {
            terminateCalled = true;
        }

        @Override
        public void calculateReset(double t) {
            resetCalled = true;
        }
    }

    @Test
    public void testFullLifecycleSequence() {
        TestBlock block = new TestBlock();

        // Pre-simulation phase
        block.calculateCheckParameters();
        assertTrue("calculateCheckParameters should be called", block.checkParametersCalled);

        block.calculateInit();
        assertTrue("calculateInit should be called", block.initCalled);

        block.calculateStart();
        assertTrue("calculateStart should be called", block.startCalled);

        // In-simulation phase
        block.calculateEnable();
        assertTrue("calculateEnable should be called", block.enableCalled);

        block.calculateOutput(0.0);
        assertTrue("calculateOutput should be called", block.outputCalled);

        block.calculateDerivative(0.0);
        assertTrue("calculateDerivative should be called", block.derivativeCalled);

        block.calculateUpdate(0.0);
        assertTrue("calculateUpdate should be called", block.updateCalled);

        block.calculateDiscreteUpdate(0.0);
        assertTrue("calculateDiscreteUpdate should be called", block.discreteUpdateCalled);

        block.calculateDisable();
        assertTrue("calculateDisable should be called", block.disableCalled);

        // Post-simulation phase
        block.calculateStop();
        assertTrue("calculateStop should be called", block.stopCalled);

        block.calculateTerminate(1.0);
        assertTrue("calculateTerminate should be called", block.terminateCalled);

        // Utility methods
        block.calculateReset(0.5);
        assertTrue("calculateReset should be called", block.resetCalled);
    }

    @Test
    public void testDefaultImplementations() {
        // Test that default implementations don't throw exceptions
        SimuBlock defaultBlock = new SimuBlock() {
            @Override
            public void calculateInit() {
                // Required method
            }

            @Override
            public void calculateOutput(double t) {
                // Required method
            }

            @Override
            public void calculateDerivative(double t) {
                // Required method
            }

            @Override
            public void calculateUpdate(double t) {
                // Required method
            }

            @Override
            public void calculateTerminate(double t) {
                // Required method
            }
        };

        // All default implementations should work without exceptions
        defaultBlock.calculateCheckParameters();
        defaultBlock.calculateStart();
        defaultBlock.calculateEnable();
        defaultBlock.calculateDiscreteUpdate(0.0);
        assertNull("Default zero-crossings should return null",
                   defaultBlock.calculateZeroCrossings(0.0));
        defaultBlock.calculateDisable();
        defaultBlock.calculateStop();
        defaultBlock.calculateReset(0.0);

        // Test should pass if no exceptions thrown
        assertTrue("Default implementations should work", true);
    }

    @Test
    public void testSimuBlockInterface() {
        // Test that SimuBlock interface methods work correctly
        SimuBlock testBlock = new SimuBlock() {
            @Override
            public void calculateInit() {}

            @Override
            public void calculateOutput(double t) {}

            @Override
            public void calculateDerivative(double t) {}

            @Override
            public void calculateUpdate(double t) {}

            @Override
            public void calculateTerminate(double t) {}
        };

        // All lifecycle methods should be callable without exceptions
        testBlock.calculateCheckParameters();
        testBlock.calculateStart();
        testBlock.calculateEnable();
        testBlock.calculateDiscreteUpdate(0.0);
        assertNull("Default zero-crossings should return null",
                   testBlock.calculateZeroCrossings(0.0));
        testBlock.calculateDisable();
        testBlock.calculateStop();
        testBlock.calculateReset(0.5);

        // Test should pass if no exceptions thrown
        assertTrue("SimuBlock interface lifecycle methods should work", true);
    }
}
