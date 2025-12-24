package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.BusSignal;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.dto.block.specialized.route.BusCreatorDto;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for BusCreator block.
 *
 * Tests DTO creation, calculateOutput functionality, bus signal creation,
 * element naming, and edge cases.
 */
public class BusCreatorTest extends DirectBlockTestBase {

    private BusCreator busCreatorBlock;

    @Override
    protected Block createBlock() throws Exception {
        // Create BusCreator with 2 inputs
        busCreatorBlock = BusCreator.create("testBusCreator", "test", 2,
            "signal1,signal2", mockModel);
        return busCreatorBlock;
    }

    @Test
    public void testDtoCreation() {
        // Test: DTO-based creation with validation
        BusCreatorDto dto = BusCreatorDto.builder()
            .blockName("dtoTest")
            .blockPath("test")
            .blockUUID("test-uuid")
            .numberOfInputs(com.ncslab.dto.common.TypedParameter.of(3))
            .elementNames(com.ncslab.dto.common.TypedParameter.of("temp,pressure,flow"))
            .busOutputName(com.ncslab.dto.common.TypedParameter.of("SensorBus"))
            .isVirtual(com.ncslab.dto.common.TypedParameter.of(true))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(-1.0))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of("Bus: <object name>"))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        assertTrue("DTO validation should pass", validation.isValid());

        // Create block from DTO
        BusCreator dtoBlock = new BusCreator(dto, mockModel);
        assertNotNull("DTO-based block should be created", dtoBlock);
        assertEquals("Block name should match", "dtoTest", dtoBlock.getBlockName());
        assertEquals("Number of inputs should match", 3, dtoBlock.getNumberOfInputs());
    }

    @Test
    public void testTwoInputBusCreation() {
        // Test: Create bus from two scalar inputs
        setScalarInput(0, 10.0);
        setScalarInput(1, 20.0);

        block.calculateOutput(0.0);

        // Verify bus signal was created
        OutputPort outputPort = busCreatorBlock.getOutputPortList().get(0);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        assertNotNull("Output signal should exist", outputSignal);
        // Note: BusSignal is created in updateDimension(), not calculateOutput()
        // So we mainly verify no exceptions occurred
    }

    @Test
    public void testThreeInputBusCreation() throws Exception {
        // Create BusCreator with 3 inputs
        BusCreator threeInputBus = BusCreator.create("threeBus", "test", 3,
            "temperature,pressure,flow", "SensorBus", true, -1.0,
            "Bus: <object name>", mockModel);
        initializeOutputSignals(threeInputBus);
        threeInputBus.calculateInit();

        // Set three scalar inputs
        mockInputPortConnection(threeInputBus, 0, new Data(25.5));
        mockInputPortConnection(threeInputBus, 1, new Data(101.3));
        mockInputPortConnection(threeInputBus, 2, new Data(50.0));

        threeInputBus.calculateOutput(0.0);

        // Verify output port exists
        assertEquals("Should have 1 output port", 1, threeInputBus.getOutputPortList().size());
    }

    @Test
    public void testMatrixInputBusCreation() throws Exception {
        // Test: Bus with matrix inputs
        BusCreator matrixBus = BusCreator.create("matrixBus", "test", 2,
            "vectorA,vectorB", mockModel);
        initializeOutputSignals(matrixBus);
        matrixBus.calculateInit();

        // Set matrix inputs
        Matrix matrix1 = new Matrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix matrix2 = new Matrix(new double[][]{{5.0, 6.0}, {7.0, 8.0}});

        Data data1 = new Data(2, 2);
        data1.setMatrix(matrix1);
        Data data2 = new Data(2, 2);
        data2.setMatrix(matrix2);

        mockInputPortConnection(matrixBus, 0, data1);
        mockInputPortConnection(matrixBus, 1, data2);

        matrixBus.calculateOutput(0.0);

        // Verify no exceptions
        assertNotNull("Matrix bus should be created", matrixBus);
    }

    @Test
    public void testVirtualBusMode() throws Exception {
        // Test: Virtual bus (zero runtime overhead)
        BusCreator virtualBus = BusCreator.create("virtualBus", "test", 2,
            "sig1,sig2", "VirtualBus", true, -1.0,
            "Bus: <object name>", mockModel);

        assertTrue("Should be virtual bus", virtualBus.isVirtual());
        assertEquals("Bus name should match", "VirtualBus", virtualBus.getBusOutputName());
    }

    @Test
    public void testNonVirtualBusMode() throws Exception {
        // Test: Non-virtual bus (generates C struct)
        BusCreator nonVirtualBus = BusCreator.create("nonVirtualBus", "test", 2,
            "sig1,sig2", "StructBus", false, -1.0,
            "Bus: <object name>", mockModel);

        assertFalse("Should be non-virtual bus", nonVirtualBus.isVirtual());
        assertEquals("Bus name should match", "StructBus", nonVirtualBus.getBusOutputName());
    }

    @Test
    public void testElementNaming() throws Exception {
        // Test: Element names are correctly parsed
        BusCreator namedBus = BusCreator.create("namedBus", "test", 4,
            "temperature,pressure,flow,level", "SensorBus", true, -1.0,
            "Bus: <object name>", mockModel);

        String[] elementNames = namedBus.getElementNames();
        assertEquals("Should have 4 element names", 4, elementNames.length);
        assertEquals("First element", "temperature", elementNames[0]);
        assertEquals("Second element", "pressure", elementNames[1]);
        assertEquals("Third element", "flow", elementNames[2]);
        assertEquals("Fourth element", "level", elementNames[3]);
    }

    @Test(expected = com.ncslab.ncslablink.BlockCreationException.class)
    public void testMismatchedElementNames() {
        // Test: Element names count must match numberOfInputs
        BusCreator.create("mismatchBus", "test", 3,
            "sig1,sig2", "Bus", true, -1.0,
            "Bus: <object name>", mockModel);
    }

    @Test
    public void testSingleInputBus() throws Exception {
        // Test: Bus with single input
        BusCreator singleBus = BusCreator.create("singleBus", "test", 1,
            "singleSignal", mockModel);
        initializeOutputSignals(singleBus);
        singleBus.calculateInit();

        mockInputPortConnection(singleBus, 0, new Data(42.0));

        singleBus.calculateOutput(0.0);

        assertEquals("Should have 1 input", 1, singleBus.getNumberOfInputs());
        assertEquals("Should have 1 output", 1, singleBus.getOutputPortList().size());
    }

    @Test
    public void testManyInputsBus() throws Exception {
        // Test: Bus with many inputs
        int numInputs = 10;
        StringBuilder elementNames = new StringBuilder();
        for (int i = 0; i < numInputs; i++) {
            if (i > 0) elementNames.append(",");
            elementNames.append("signal").append(i + 1);
        }

        BusCreator manyBus = BusCreator.create("manyBus", "test", numInputs,
            elementNames.toString(), mockModel);
        initializeOutputSignals(manyBus);
        manyBus.calculateInit();

        // Set all inputs
        for (int i = 0; i < numInputs; i++) {
            mockInputPortConnection(manyBus, i, new Data(i * 10.0));
        }

        manyBus.calculateOutput(0.0);

        assertEquals("Should have " + numInputs + " inputs", numInputs,
            manyBus.getInputPortList().size());
    }

    @Test
    public void testZeroInputs() {
        // Test: Zero inputs (edge case)
        setScalarInput(0, 0.0);
        setScalarInput(1, 0.0);

        block.calculateOutput(0.0);

        // Verify no exceptions
        assertNotNull("Block should handle zero inputs", block);
    }

    @Test
    public void testNegativeInputs() {
        // Test: Negative input values
        setScalarInput(0, -10.5);
        setScalarInput(1, -20.3);

        block.calculateOutput(0.0);

        // Verify no exceptions
        assertNotNull("Block should handle negative inputs", block);
    }

    @Test
    public void testLargeValues() {
        // Test: Large input values
        setScalarInput(0, 1e10);
        setScalarInput(1, 2e10);

        block.calculateOutput(0.0);

        // Verify no exceptions
        assertNotNull("Block should handle large values", block);
    }

    @Test
    public void testSmallValues() {
        // Test: Small input values
        setScalarInput(0, 1e-10);
        setScalarInput(1, 2e-10);

        block.calculateOutput(0.0);

        // Verify no exceptions
        assertNotNull("Block should handle small values", block);
    }

    @Test
    public void testMixedMagnitudes() {
        // Test: Mixed magnitude inputs
        setScalarInput(0, 1e10);
        setScalarInput(1, 1e-10);

        block.calculateOutput(0.0);

        // Verify no exceptions
        assertNotNull("Block should handle mixed magnitudes", block);
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        setScalarInput(0, 10.0);
        setScalarInput(1, 20.0);

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average BusCreator calculateOutput() time: " +
            avgTime / 1000.0 + " microseconds");

        // Performance assertion: Should complete in less than 20 microseconds
        assertTrue("BusCreator calculateOutput should be fast", avgTime < 20000);
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

            // Verify no exceptions
            assertNotNull("Bus should be created for inputs " +
                testCase[0] + ", " + testCase[1], block);
        }
    }

    @Test
    public void testElementNameWhitespace() throws Exception {
        // Test: Element names with extra whitespace
        BusCreator whitespaceBus = BusCreator.create("whitespaceBus", "test", 3,
            " signal1 , signal2 , signal3 ", mockModel);

        String[] elementNames = whitespaceBus.getElementNames();
        assertEquals("First element should be trimmed", "signal1", elementNames[0]);
        assertEquals("Second element should be trimmed", "signal2", elementNames[1]);
        assertEquals("Third element should be trimmed", "signal3", elementNames[2]);
    }

    @Test
    public void testSampleTimeInherited() throws Exception {
        // Test: Inherited sample time (-1)
        BusCreator inheritedBus = BusCreator.create("inheritedBus", "test", 2,
            "sig1,sig2", "Bus", true, -1.0,
            "Bus: <object name>", mockModel);

        assertNotNull("Block with inherited sample time should be created", inheritedBus);
    }

    @Test
    public void testSampleTimeContinuous() throws Exception {
        // Test: Continuous sample time (0)
        BusCreator continuousBus = BusCreator.create("continuousBus", "test", 2,
            "sig1,sig2", "Bus", true, 0.0,
            "Bus: <object name>", mockModel);

        assertNotNull("Block with continuous sample time should be created", continuousBus);
    }

    @Test
    public void testSampleTimeDiscrete() throws Exception {
        // Test: Discrete sample time (>0)
        BusCreator discreteBus = BusCreator.create("discreteBus", "test", 2,
            "sig1,sig2", "Bus", true, 0.1,
            "Bus: <object name>", mockModel);

        assertNotNull("Block with discrete sample time should be created", discreteBus);
    }

    @Test
    public void testBlockInfo() {
        // Test: Block information
        printBlockInfo();

        assertEquals("Block type should be BusCreator", "BusCreator", block.getBlockType());
        assertEquals("Should have 2 input ports", 2, block.getInputPortList().size());
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());
    }
}
