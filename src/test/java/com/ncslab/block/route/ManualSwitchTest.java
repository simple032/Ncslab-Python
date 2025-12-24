package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.dto.block.specialized.route.ManualSwitchDto;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for ManualSwitch block.
 *
 * Tests DTO creation, switch control functionality, manual switching between inputs,
 * and edge cases.
 */
public class ManualSwitchTest extends DirectBlockTestBase {

    private ManualSwitch manualSwitchBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create ManualSwitch with control="0" (select first input)
        manualSwitchBlock = ManualSwitch.create("testManualSwitch", "test", mockModel);
        return manualSwitchBlock;
    }

    @Test
    public void testDtoCreation() {
        // Test: DTO-based creation with validation
        ManualSwitchDto dto = ManualSwitchDto.builder()
            .blockName("dtoTest")
            .blockPath("test")
            .blockUUID("test-uuid")
            .switchControl(com.ncslab.dto.common.TypedParameter.of("1"))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of("Inherit: Inherit via internal rule"))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        assertTrue("DTO validation should pass", validation.isValid());

        // Create block from DTO
        ManualSwitch dtoBlock = new ManualSwitch(dto, mockModel);
        assertNotNull("DTO-based block should be created", dtoBlock);
        assertEquals("Block name should match", "dtoTest", dtoBlock.getBlockName());
    }

    @Test
    public void testSwitchControlZero() {
        // Test: SwitchControl="0" should select input 0
        setScalarInput(0, 10.0);
        setScalarInput(1, 20.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Control=0 should select input 0", 10.0, output, DELTA);
    }

    @Test
    public void testSwitchControlOne() throws Exception {
        // Test: SwitchControl="1" should select input 1
        ManualSwitch switchOne = ManualSwitch.create("switchOne", "test", "1", -1.0,
            "Inherit: Inherit via internal rule", mockModel);
        initializeOutputSignals(switchOne);
        switchOne.calculateInit();

        mockInputPortConnection(switchOne, 0, new Data(10.0));
        mockInputPortConnection(switchOne, 1, new Data(20.0));

        switchOne.calculateOutput(0.0);

        double output = getScalarOutputForBlock(switchOne, 0);
        assertEquals("Control=1 should select input 1", 20.0, output, DELTA);
    }

    @Test
    public void testSwitchingBetweenInputs() throws Exception {
        // Test: Create two switches with different control values
        ManualSwitch switch0 = ManualSwitch.create("switch0", "test", "0", -1.0,
            "Inherit: Inherit via internal rule", mockModel);
        ManualSwitch switch1 = ManualSwitch.create("switch1", "test", "1", -1.0,
            "Inherit: Inherit via internal rule", mockModel);

        initializeOutputSignals(switch0);
        initializeOutputSignals(switch1);
        switch0.calculateInit();
        switch1.calculateInit();

        // Set same inputs for both switches
        mockInputPortConnection(switch0, 0, new Data(100.0));
        mockInputPortConnection(switch0, 1, new Data(200.0));
        mockInputPortConnection(switch1, 0, new Data(100.0));
        mockInputPortConnection(switch1, 1, new Data(200.0));

        switch0.calculateOutput(0.0);
        switch1.calculateOutput(0.0);

        // Verify different outputs based on control
        assertEquals("Switch0 should output input 0", 100.0,
            getScalarOutputForBlock(switch0, 0), DELTA);
        assertEquals("Switch1 should output input 1", 200.0,
            getScalarOutputForBlock(switch1, 0), DELTA);
    }

    @Test
    public void testNegativeInputs() {
        // Test: Negative input values
        setScalarInput(0, -10.5);
        setScalarInput(1, -20.3);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should handle negative inputs", -10.5, output, DELTA);
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
        assertEquals("Should handle large values", 1e10, output, 1e-4);
    }

    @Test
    public void testSmallValues() {
        // Test: Small input values
        setScalarInput(0, 1e-10);
        setScalarInput(1, 2e-10);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should handle small values", 1e-10, output, 1e-20);
    }

    @Test
    public void testMatrixInputs() {
        // Test: Matrix input routing
        double[][] matrix0 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] matrix1 = {{5.0, 6.0}, {7.0, 8.0}};

        setMatrixInput(0, matrix0);
        setMatrixInput(1, matrix1);

        block.calculateOutput(0.0);

        // Control=0, so should select matrix0
        Matrix outputMatrix = getMatrixOutput(0);
        assertEquals("Matrix height should match", 2, outputMatrix.getRowDimension());
        assertEquals("Matrix width should match", 2, outputMatrix.getColumnDimension());
        assertEquals("First element should match", 1.0, outputMatrix.get(0, 0), DELTA);
    }

    @Test
    public void testVectorInputs() {
        // Test: Vector input routing
        double[][] vector0 = {{1.0, 2.0, 3.0}};
        double[][] vector1 = {{4.0, 5.0, 6.0}};

        setMatrixInput(0, vector0);
        setMatrixInput(1, vector1);

        block.calculateOutput(0.0);

        // Control=0, so should select vector0
        Matrix outputVector = getMatrixOutput(0);
        assertEquals("Vector should have 1 row", 1, outputVector.getRowDimension());
        assertEquals("Vector should have 3 columns", 3, outputVector.getColumnDimension());
        assertEquals("First element should match", 1.0, outputVector.get(0, 0), DELTA);
    }

    @Test
    public void testMixedMagnitudes() {
        // Test: Mixed magnitude inputs
        setScalarInput(0, 1e10);
        setScalarInput(1, 1e-10);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should handle mixed magnitudes", 1e10, output, 1e-4);
    }

    @Test
    public void testDefaultControlValue() throws Exception {
        // Test: Default control value should be "0"
        ManualSwitch defaultSwitch = ManualSwitch.create("defaultSwitch", "test", mockModel);
        initializeOutputSignals(defaultSwitch);
        defaultSwitch.calculateInit();

        mockInputPortConnection(defaultSwitch, 0, new Data(10.0));
        mockInputPortConnection(defaultSwitch, 1, new Data(20.0));

        defaultSwitch.calculateOutput(0.0);

        double output = getScalarOutputForBlock(defaultSwitch, 0);
        assertEquals("Default control should select input 0", 10.0, output, DELTA);
    }

    @Test
    public void testSampleTimeInherited() throws Exception {
        // Test: Inherited sample time (-1)
        ManualSwitch inheritedSwitch = ManualSwitch.create("inheritedSwitch", "test", "0",
            -1.0, "Inherit: Inherit via internal rule", mockModel);

        assertNotNull("Block with inherited sample time should be created", inheritedSwitch);
    }

    @Test
    public void testSampleTimeContinuous() throws Exception {
        // Test: Continuous sample time (0)
        ManualSwitch continuousSwitch = ManualSwitch.create("continuousSwitch", "test", "0",
            0.0, "Inherit: Inherit via internal rule", mockModel);

        assertNotNull("Block with continuous sample time should be created", continuousSwitch);
    }

    @Test
    public void testSampleTimeDiscrete() throws Exception {
        // Test: Discrete sample time (>0)
        ManualSwitch discreteSwitch = ManualSwitch.create("discreteSwitch", "test", "0",
            0.1, "Inherit: Inherit via internal rule", mockModel);

        assertNotNull("Block with discrete sample time should be created", discreteSwitch);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 10.0);
        setScalarInput(1, 20.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average ManualSwitch calculateOutput() time: " +
            avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 15 microseconds
        assertTrue("ManualSwitch calculateOutput should be fast", avgTime < 15000);
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
            assertEquals("Should output first input (control=0)", testCase[0], output, DELTA);
        }
    }

    @Test
    public void testBoundaryConditions() throws Exception {
        // Test: Boundary input values
        double[] boundaryValues = {Double.MIN_VALUE, -1e-100, 0.0, 1e-100, Double.MAX_VALUE};

        for (double value : boundaryValues) {
            ManualSwitch boundarySwitch = ManualSwitch.create("boundarySwitch", "test", "0",
                -1.0, "Inherit: Inherit via internal rule", mockModel);
            initializeOutputSignals(boundarySwitch);
            boundarySwitch.calculateInit();

            mockInputPortConnection(boundarySwitch, 0, new Data(value));
            mockInputPortConnection(boundarySwitch, 1, new Data(value * 2));

            boundarySwitch.calculateOutput(0.0);

            double output = getScalarOutputForBlock(boundarySwitch, 0);
            assertEquals("Should handle boundary value " + value, value, output, 1e-300);
        }
    }

    @Test
    public void testBlockInfo() {
        // Test: Block information
        printBlockInfo();

        assertEquals("Block type should be Manual Switch", "Manual Switch", block.getBlockType());
        assertEquals("Should have 2 input ports", 2, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }
}
