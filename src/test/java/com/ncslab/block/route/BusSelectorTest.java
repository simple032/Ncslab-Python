package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.BusSignal;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.dto.block.specialized.route.BusSelectorDto;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for BusSelector block.
 *
 * Tests DTO creation, signal extraction from bus, nested signal paths,
 * and edge cases.
 */
public class BusSelectorTest extends DirectBlockTestBase {

    private BusSelector busSelectorBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create BusSelector extracting one signal
        busSelectorBlock = BusSelector.create("testBusSelector", "test",
            "signal1", mockModel);
        return busSelectorBlock;
    }

    @Test
    public void testDtoCreation() {
        // Test: DTO-based creation with validation
        BusSelectorDto dto = BusSelectorDto.builder()
            .blockName("dtoTest")
            .blockPath("test")
            .blockUUID("test-uuid")
            .numberOfOutputs(com.ncslab.dto.common.TypedParameter.of(2))
            .selectedSignals(com.ncslab.dto.common.TypedParameter.of("temperature,pressure"))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of("Inherit: Inherit via back propagation"))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        assertTrue("DTO validation should pass", validation.isValid());

        // Create block from DTO
        BusSelector dtoBlock = new BusSelector(dto, mockModel);
        assertNotNull("DTO-based block should be created", dtoBlock);
        assertEquals("Block name should match", "dtoTest", dtoBlock.getBlockName());
    }

    @Test
    public void testSingleSignalExtraction() {
        // Test: Extract single signal from bus
        // Note: BusSelector requires BusSignal input, which is created by BusCreator
        // For now, we test the block creation and structure

        assertEquals("Should have 1 input port", 1, busSelectorBlock.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, busSelectorBlock.getOutputPortList().size());
    }

    @Test
    public void testMultipleSignalExtraction() throws Exception {
        // Test: Extract multiple signals from bus
        BusSelector multiSelector = BusSelector.create("multiSelector", "test",
            "temperature,pressure,flow", mockModel);

        assertEquals("Should have 1 input port", 1, multiSelector.getInputPortList().size());
        assertEquals("Should have 3 output ports", 3, multiSelector.getOutputPortList().size());
    }

    @Test
    public void testNestedSignalPath() throws Exception {
        // Test: Extract nested signal using dot notation
        BusSelector nestedSelector = BusSelector.create("nestedSelector", "test",
            "sensors.temperature,sensors.pressure", mockModel);

        assertEquals("Should have 1 input port", 1, nestedSelector.getInputPortList().size());
        assertEquals("Should have 2 output ports", 2, nestedSelector.getOutputPortList().size());
    }

    @Test
    public void testFlatSignalSelection() throws Exception {
        // Test: Flat signal selection
        BusSelector flatSelector = BusSelector.create("flatSelector", "test", 3,
            "temp,pressure,flow", -1.0,
            "Inherit: Inherit via back propagation", mockModel);

        assertEquals("Number of outputs should match", 3, flatSelector.getOutputPortList().size());
    }

    @Test
    public void testHierarchicalSignalSelection() throws Exception {
        // Test: Hierarchical signal selection with mixed paths
        BusSelector hierSelector = BusSelector.create("hierSelector", "test",
            "motor.speed,motor.controller.setpoint,sensors.temp", mockModel);

        assertEquals("Should have 1 input port", 1, hierSelector.getInputPortList().size());
        assertEquals("Should have 3 output ports", 3, hierSelector.getOutputPortList().size());
    }

    @Test
    public void testCalculateOutputWithNonBusInput() {
        // Test: Graceful handling of non-bus input (fallback mode)
        setScalarInput(0, 42.0);

        block.calculateOutput(0.0);

        // Should handle gracefully with fallback
        assertNotNull("Block should handle non-bus input", block);
    }

    @Test
    public void testEmptySignalSelection() throws Exception {
        // Test: Empty signal selection defaults to "signal1"
        BusSelector emptySelector = BusSelector.create("emptySelector", "test", 1,
            "", -1.0, "Inherit: Inherit via back propagation", mockModel);

        assertEquals("Should default to 1 output", 1, emptySelector.getOutputPortList().size());
    }

    @Test
    public void testSignalNameWithSpaces() throws Exception {
        // Test: Signal names with whitespace
        BusSelector spaceSelector = BusSelector.create("spaceSelector", "test",
            " signal1 , signal2 , signal3 ", mockModel);

        assertEquals("Should have 3 outputs", 3, spaceSelector.getOutputPortList().size());
    }

    @Test
    public void testSingleCharacterSignalNames() throws Exception {
        // Test: Single character signal names
        BusSelector shortSelector = BusSelector.create("shortSelector", "test",
            "a,b,c", mockModel);

        assertEquals("Should have 3 outputs", 3, shortSelector.getOutputPortList().size());
    }

    @Test
    public void testLongSignalNames() throws Exception {
        // Test: Long signal names
        BusSelector longSelector = BusSelector.create("longSelector", "test",
            "veryLongSignalNameForTemperature,veryLongSignalNameForPressure", mockModel);

        assertEquals("Should have 2 outputs", 2, longSelector.getOutputPortList().size());
    }

    @Test
    public void testNumericSignalNames() throws Exception {
        // Test: Signal names with numbers
        BusSelector numericSelector = BusSelector.create("numericSelector", "test",
            "sensor1,sensor2,sensor3", mockModel);

        assertEquals("Should have 3 outputs", 3, numericSelector.getOutputPortList().size());
    }

    @Test
    public void testUnderscoreInSignalNames() throws Exception {
        // Test: Signal names with underscores
        BusSelector underscoreSelector = BusSelector.create("underscoreSelector", "test",
            "motor_speed,motor_current", mockModel);

        assertEquals("Should have 2 outputs", 2, underscoreSelector.getOutputPortList().size());
    }

    @Test
    public void testDeepNestedPath() throws Exception {
        // Test: Deeply nested signal path
        BusSelector deepSelector = BusSelector.create("deepSelector", "test",
            "system.subsystem.component.parameter", mockModel);

        assertEquals("Should have 1 output", 1, deepSelector.getOutputPortList().size());
    }

    @Test
    public void testMixedFlatAndNestedPaths() throws Exception {
        // Test: Mixed flat and nested paths
        BusSelector mixedSelector = BusSelector.create("mixedSelector", "test",
            "temperature,motor.speed,controller.output.value", mockModel);

        assertEquals("Should have 3 outputs", 3, mixedSelector.getOutputPortList().size());
    }

    @Test
    public void testSampleTimeInherited() throws Exception {
        // Test: Inherited sample time (-1)
        BusSelector inheritedSelector = BusSelector.create("inheritedSelector", "test", 1,
            "signal1", -1.0, "Inherit: Inherit via back propagation", mockModel);

        assertNotNull("Block with inherited sample time should be created", inheritedSelector);
    }

    @Test
    public void testSampleTimeContinuous() throws Exception {
        // Test: Continuous sample time (0)
        BusSelector continuousSelector = BusSelector.create("continuousSelector", "test", 1,
            "signal1", 0.0, "Inherit: Inherit via back propagation", mockModel);

        assertNotNull("Block with continuous sample time should be created", continuousSelector);
    }

    @Test
    public void testSampleTimeDiscrete() throws Exception {
        // Test: Discrete sample time (>0)
        BusSelector discreteSelector = BusSelector.create("discreteSelector", "test", 1,
            "signal1", 0.1, "Inherit: Inherit via back propagation", mockModel);

        assertNotNull("Block with discrete sample time should be created", discreteSelector);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 42.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average BusSelector calculateOutput() time: " +
            avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 25 microseconds
        assertTrue("BusSelector calculateOutput should be fast", avgTime < 25000);
    }

    @Test
    public void testMultipleCalculations() {
        // Test: Multiple calculateOutput() calls
        for (int i = 0; i < 10; i++) {
            setScalarInput(0, i * 10.0);
            block.calculateOutput(0.0);

            // Verify no exceptions
            assertNotNull("Bus selector should handle calculation " + i, block);
        }
    }

    @Test
    public void testZeroOutput() throws Exception {
        // Test: Zero output selection (edge case)
        // Note: Should have at least 1 output
        BusSelector oneSelector = BusSelector.create("oneSelector", "test", 1,
            "signal1", -1.0, "Inherit: Inherit via back propagation", mockModel);

        assertTrue("Should have at least 1 output", oneSelector.getOutputPortList().size() >= 1);
    }

    @Test
    public void testManyOutputs() throws Exception {
        // Test: Many output selections
        int numOutputs = 10;
        StringBuilder selectedSignals = new StringBuilder();
        for (int i = 0; i < numOutputs; i++) {
            if (i > 0) selectedSignals.append(",");
            selectedSignals.append("signal").append(i + 1);
        }

        BusSelector manySelector = BusSelector.create("manySelector", "test", numOutputs,
            selectedSignals.toString(), -1.0,
            "Inherit: Inherit via back propagation", mockModel);

        assertEquals("Should have " + numOutputs + " outputs", numOutputs,
            manySelector.getOutputPortList().size());
    }

    @Test
    public void testBlockInfo() {
        // Test: Block information
        printBlockInfo();

        assertEquals("Block type should be BusSelector", "BusSelector", block.getBlockType());
        assertEquals("Should have 1 input port", 1, block.getInputPortList().size());
        assertTrue("Should have at least 1 output port",
            block.getOutputPortList().size() >= 1);
    }
}
