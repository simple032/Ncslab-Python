package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.dto.block.specialized.route.MultiportSwitchDto;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for MultiportSwitch block.
 *
 * Tests DTO creation, control signal routing, data port order (zero-based/one-based),
 * default output handling, and edge cases.
 */
public class MultiportSwitchTest extends DirectBlockTestBase {

    private MultiportSwitch multiportSwitchBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create MultiportSwitch with 3 data inputs, zero-based indexing
        multiportSwitchBlock = MultiportSwitch.create("testMultiportSwitch", "test", 3, mockModel);
        return multiportSwitchBlock;
    }

    @Test
    public void testDtoCreation() {
        // Test: DTO-based creation with validation
        MultiportSwitchDto dto = MultiportSwitchDto.builder()
            .blockName("dtoTest")
            .blockPath("test")
            .blockUUID("test-uuid")
            .numberOfDataInputs(com.ncslab.dto.common.TypedParameter.of(4))
            .dataPortOrder(com.ncslab.dto.common.TypedParameter.of("Zero-based contiguous"))
            .defaultOutput(com.ncslab.dto.common.TypedParameter.of(0.0))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of("Inherit: Same as input"))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        assertTrue("DTO validation should pass", validation.isValid());

        // Create block from DTO
        MultiportSwitch dtoBlock = new MultiportSwitch(dto, mockModel);
        assertNotNull("DTO-based block should be created", dtoBlock);
        assertEquals("Block name should match", "dtoTest", dtoBlock.getBlockName());
    }

    @Test
    public void testZeroBasedControl0() {
        // Test: Zero-based indexing, control=0 should select data input 1 (first)
        setScalarInput(0, 0.0);  // Control input
        setScalarInput(1, 10.0); // Data input 0
        setScalarInput(2, 20.0); // Data input 1
        setScalarInput(3, 30.0); // Data input 2

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Zero-based control=0 should select data input 0", 10.0, output, DELTA);
    }

    @Test
    public void testZeroBasedControl1() {
        // Test: Zero-based indexing, control=1 should select data input 2 (second)
        setScalarInput(0, 1.0);  // Control input
        setScalarInput(1, 10.0); // Data input 0
        setScalarInput(2, 20.0); // Data input 1
        setScalarInput(3, 30.0); // Data input 2

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Zero-based control=1 should select data input 1", 20.0, output, DELTA);
    }

    @Test
    public void testZeroBasedControl2() {
        // Test: Zero-based indexing, control=2 should select data input 3 (third)
        setScalarInput(0, 2.0);  // Control input
        setScalarInput(1, 10.0); // Data input 0
        setScalarInput(2, 20.0); // Data input 1
        setScalarInput(3, 30.0); // Data input 2

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Zero-based control=2 should select data input 2", 30.0, output, DELTA);
    }

    @Test
    public void testOneBasedControl() throws Exception {
        // Test: One-based indexing
        MultiportSwitch oneBasedSwitch = MultiportSwitch.create("oneBasedSwitch", "test", 3,
            "One-based contiguous", 0.0, -1.0, "Inherit: Same as input", mockModel);
        initializeOutputSignals(oneBasedSwitch);
        oneBasedSwitch.calculateInit();

        mockInputPortConnection(oneBasedSwitch, 0, new Data(1.0));  // Control = 1
        mockInputPortConnection(oneBasedSwitch, 1, new Data(10.0)); // Data input 0
        mockInputPortConnection(oneBasedSwitch, 2, new Data(20.0)); // Data input 1
        mockInputPortConnection(oneBasedSwitch, 3, new Data(30.0)); // Data input 2

        oneBasedSwitch.calculateOutput(0.0);

        double output = getScalarOutputForBlock(oneBasedSwitch, 0);
        assertEquals("One-based control=1 should select data input 0", 10.0, output, DELTA);
    }

    @Test
    public void testOneBasedControl2() throws Exception {
        // Test: One-based indexing, control=2
        MultiportSwitch oneBasedSwitch = MultiportSwitch.create("oneBasedSwitch", "test", 3,
            "One-based contiguous", 0.0, -1.0, "Inherit: Same as input", mockModel);
        initializeOutputSignals(oneBasedSwitch);
        oneBasedSwitch.calculateInit();

        mockInputPortConnection(oneBasedSwitch, 0, new Data(2.0));  // Control = 2
        mockInputPortConnection(oneBasedSwitch, 1, new Data(10.0)); // Data input 0
        mockInputPortConnection(oneBasedSwitch, 2, new Data(20.0)); // Data input 1
        mockInputPortConnection(oneBasedSwitch, 3, new Data(30.0)); // Data input 2

        oneBasedSwitch.calculateOutput(0.0);

        double output = getScalarOutputForBlock(oneBasedSwitch, 0);
        assertEquals("One-based control=2 should select data input 1", 20.0, output, DELTA);
    }

    @Test
    public void testControlOutOfRangeNegative() {
        // Test: Control < 0 should use default output
        setScalarInput(0, -1.0); // Control input (out of range)
        setScalarInput(1, 10.0);
        setScalarInput(2, 20.0);
        setScalarInput(3, 30.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Control out of range should use default output", 0.0, output, DELTA);
    }

    @Test
    public void testControlOutOfRangeTooHigh() {
        // Test: Control >= numberOfDataInputs should use default output
        setScalarInput(0, 3.0);  // Control input (out of range for 3 data inputs)
        setScalarInput(1, 10.0);
        setScalarInput(2, 20.0);
        setScalarInput(3, 30.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Control out of range should use default output", 0.0, output, DELTA);
    }

    @Test
    public void testNonZeroDefaultOutput() throws Exception {
        // Test: Non-zero default output
        MultiportSwitch defaultSwitch = MultiportSwitch.create("defaultSwitch", "test", 3,
            "Zero-based contiguous", 99.0, -1.0, "Inherit: Same as input", mockModel);
        initializeOutputSignals(defaultSwitch);
        defaultSwitch.calculateInit();

        mockInputPortConnection(defaultSwitch, 0, new Data(-1.0)); // Control (out of range)
        mockInputPortConnection(defaultSwitch, 1, new Data(10.0));
        mockInputPortConnection(defaultSwitch, 2, new Data(20.0));
        mockInputPortConnection(defaultSwitch, 3, new Data(30.0));

        defaultSwitch.calculateOutput(0.0);

        double output = getScalarOutputForBlock(defaultSwitch, 0);
        assertEquals("Should use custom default output", 99.0, output, DELTA);
    }

    @Test
    public void testControlNaN() {
        // Test: NaN control should use default output
        setScalarInput(0, Double.NaN); // Control input (invalid)
        setScalarInput(1, 10.0);
        setScalarInput(2, 20.0);
        setScalarInput(3, 30.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("NaN control should use default output", 0.0, output, DELTA);
    }

    @Test
    public void testControlInfinity() {
        // Test: Infinity control should use default output
        setScalarInput(0, Double.POSITIVE_INFINITY); // Control input (invalid)
        setScalarInput(1, 10.0);
        setScalarInput(2, 20.0);
        setScalarInput(3, 30.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Infinity control should use default output", 0.0, output, DELTA);
    }

    @Test
    public void testControlRounding() {
        // Test: Control value is rounded to nearest integer
        setScalarInput(0, 0.6);  // Should round to 1
        setScalarInput(1, 10.0);
        setScalarInput(2, 20.0);
        setScalarInput(3, 30.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Control 0.6 should round to 1", 20.0, output, DELTA);
    }

    @Test
    public void testControlRoundingDown() {
        // Test: Control value rounded down
        setScalarInput(0, 0.4);  // Should round to 0
        setScalarInput(1, 10.0);
        setScalarInput(2, 20.0);
        setScalarInput(3, 30.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Control 0.4 should round to 0", 10.0, output, DELTA);
    }

    @Test
    public void testTwoDataInputs() throws Exception {
        // Test: Switch with 2 data inputs
        MultiportSwitch twoInputSwitch = MultiportSwitch.create("twoInputSwitch", "test", 2, mockModel);
        initializeOutputSignals(twoInputSwitch);
        twoInputSwitch.calculateInit();

        mockInputPortConnection(twoInputSwitch, 0, new Data(0.0)); // Control
        mockInputPortConnection(twoInputSwitch, 1, new Data(100.0));
        mockInputPortConnection(twoInputSwitch, 2, new Data(200.0));

        twoInputSwitch.calculateOutput(0.0);

        double output = getScalarOutputForBlock(twoInputSwitch, 0);
        assertEquals("Control=0 should select first data input", 100.0, output, DELTA);
    }

    @Test
    public void testManyDataInputs() throws Exception {
        // Test: Switch with many data inputs
        MultiportSwitch manyInputSwitch = MultiportSwitch.create("manyInputSwitch", "test", 5, mockModel);
        initializeOutputSignals(manyInputSwitch);
        manyInputSwitch.calculateInit();

        mockInputPortConnection(manyInputSwitch, 0, new Data(4.0)); // Control (select last input)
        for (int i = 0; i < 5; i++) {
            mockInputPortConnection(manyInputSwitch, i + 1, new Data(i * 100.0));
        }

        manyInputSwitch.calculateOutput(0.0);

        double output = getScalarOutputForBlock(manyInputSwitch, 0);
        assertEquals("Control=4 should select last data input", 400.0, output, DELTA);
    }

    @Test
    public void testMatrixInputs() {
        // Test: Matrix data inputs
        double[][] matrix0 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] matrix1 = {{5.0, 6.0}, {7.0, 8.0}};
        double[][] matrix2 = {{9.0, 10.0}, {11.0, 12.0}};

        setScalarInput(0, 1.0); // Control = 1
        setMatrixInput(1, matrix0);
        setMatrixInput(2, matrix1);
        setMatrixInput(3, matrix2);

        block.calculateOutput(0.0);

        Matrix output = getMatrixOutput(0);
        assertEquals("Should select matrix1", 5.0, output.get(0, 0), DELTA);
        assertEquals("Should select matrix1", 6.0, output.get(0, 1), DELTA);
    }

    @Test
    public void testNegativeDataInputs() {
        // Test: Negative data inputs
        setScalarInput(0, 1.0);   // Control
        setScalarInput(1, -10.0);
        setScalarInput(2, -20.0);
        setScalarInput(3, -30.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should handle negative inputs", -20.0, output, DELTA);
    }

    @Test
    public void testZeroDataInputs() {
        // Test: Zero data inputs
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);
        setScalarInput(2, 0.0);
        setScalarInput(3, 0.0);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should handle zero inputs", 0.0, output, DELTA);
    }

    @Test
    public void testLargeValues() {
        // Test: Large data values
        setScalarInput(0, 0.0);
        setScalarInput(1, 1e10);
        setScalarInput(2, 2e10);
        setScalarInput(3, 3e10);

        block.calculateOutput(0.0);

        double output = getScalarOutput(0);
        assertEquals("Should handle large values", 1e10, output, 1e-4);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 1.0);
        setScalarInput(1, 10.0);
        setScalarInput(2, 20.0);
        setScalarInput(3, 30.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average MultiportSwitch calculateOutput() time: " +
            avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds
        assertTrue("MultiportSwitch calculateOutput should be fast", avgTime < 20000);
    }

    @Test
    public void testMultipleCalculations() {
        // Test: Multiple calculations with different control values
        double[] controlValues = {0.0, 1.0, 2.0, 0.0, 1.0};
        double[] expectedOutputs = {10.0, 20.0, 30.0, 10.0, 20.0};

        for (int i = 0; i < controlValues.length; i++) {
            setScalarInput(0, controlValues[i]);
            setScalarInput(1, 10.0);
            setScalarInput(2, 20.0);
            setScalarInput(3, 30.0);

            block.calculateOutput(0.0);

            double output = getScalarOutput(0);
            assertEquals("Control=" + controlValues[i] + " should output " + expectedOutputs[i],
                expectedOutputs[i], output, DELTA);
        }
    }

    @Test
    public void testBlockInfo() {
        // Test: Block information
        printBlockInfo();

        assertEquals("Block type should be Multiport Switch", "Multiport Switch", block.getBlockType());
        assertEquals("Should have 4 input ports (1 control + 3 data)", 4,
            block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }
}
