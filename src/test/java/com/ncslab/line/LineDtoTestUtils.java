package com.ncslab.line;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.dto.model.LineDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.SimulationModel;

/**
 * Utility class providing shared testing utilities for line DTO tests.
 * 
 * This class contains:
 * - Factory methods for creating test data
 * - Common mock object configurations
 * - Assertion helpers for line testing
 * - Test data builders and patterns
 * 
 * @author Claude Code
 */
public class LineDtoTestUtils {
    
    // ==================== Mock Object Factories ====================
    
    /**
     * Creates a mock block with specified ports and properties.
     * 
     * @param name Block name
     * @param uuid Block UUID
     * @param type Block type
     * @param numInputPorts Number of input ports to create
     * @param numOutputPorts Number of output ports to create
     * @return Configured mock block
     */
    public static Block createMockBlock(String name, String uuid, String type, 
                                       int numInputPorts, int numOutputPorts) {
        Block block = mock(Block.class);
        when(block.getBlockName()).thenReturn(name);
        when(block.getBlockUUID()).thenReturn(uuid);
        when(block.getBlockType()).thenReturn(type);
        
        // Create input ports
        List<InputPort> inputPorts = new ArrayList<>();
        for (int i = 1; i <= numInputPorts; i++) {
            InputPort port = mock(InputPort.class);
            when(port.getNumber()).thenReturn(i);
            inputPorts.add(port);
        }
        when(block.getInputPortList()).thenReturn(inputPorts);
        
        // Create output ports
        List<OutputPort> outputPorts = new ArrayList<>();
        for (int i = 1; i <= numOutputPorts; i++) {
            OutputPort port = mock(OutputPort.class);
            when(port.getNumber()).thenReturn(i);
            outputPorts.add(port);
        }
        when(block.getOutputPortList()).thenReturn(outputPorts);
        
        return block;
    }
    
    /**
     * Creates a mock subsystem In block with proper configuration.
     */
    public static Block createMockInBlock(String name, String uuid) {
        Block inBlock = createMockBlock(name, uuid, "In", 1, 1);
        return inBlock;
    }
    
    /**
     * Creates a mock subsystem Out block with proper configuration.
     */
    public static Block createMockOutBlock(String name, String uuid) {
        Block outBlock = createMockBlock(name, uuid, "Out", 1, 1);
        return outBlock;
    }
    
    /**
     * Creates a mock subsystem block with input and output ports.
     */
    public static Block createMockSubsystemBlock(String name, String uuid, 
                                                int numInputs, int numOutputs) {
        return createMockBlock(name, uuid, "Subsystem", numInputs, numOutputs);
    }
    
    // ==================== DTO Factories ====================
    
    /**
     * Creates a LineDto DTO with all required fields.
     */
    public static LineDto createLineDto(String fromBlockName, String toBlockName,
                                        String fromUUID, String toUUID,
                                        Object fromPort, Object toPort,
                                        String linePath) {
        LineDto line = new LineDto();
        line.setFromBlockName(fromBlockName);
        line.setToBlockName(toBlockName);
        line.setFromBlockUUID(fromUUID);
        line.setToBlockUUID(toUUID);
        line.setFromPortNo(fromPort);
        line.setToPortNo(toPort);
        line.setLinePath(linePath);
        return line;
    }
    
    /**
     * Creates a simple LineDto with name-based lookup (no UUIDs).
     */
    public static LineDto createNameBasedLineDto(String fromBlock, String toBlock, 
                                                 int fromPort, int toPort) {
        return createLineDto(fromBlock, toBlock, null, null, fromPort, toPort, "TestModel");
    }
    
    /**
     * Creates a LineDto with UUID-based lookup.
     */
    public static LineDto createUuidBasedLineDto(String fromBlock, String toBlock,
                                                 String fromUUID, String toUUID,
                                                 int fromPort, int toPort) {
        return createLineDto(fromBlock, toBlock, fromUUID, toUUID, fromPort, toPort, "TestModel");
    }
    
    /**
     * Creates a subsystem line DTO.
     */
    public static LineDto createSubsystemLineDto(String fromBlock, String toBlock,
                                                 String fromUUID, String toUUID,
                                                 int fromPort, int toPort,
                                                 String subsystemPath) {
        return createLineDto(fromBlock, toBlock, fromUUID, toUUID, fromPort, toPort, subsystemPath);
    }
    
    /**
     * Creates an invalid LineDto for testing error handling.
     */
    public static LineDto createInvalidLineDto() {
        LineDto line = new LineDto();
        line.setFromBlockName(null); // Invalid - null from block
        line.setToBlockName("ValidBlock");
        line.setFromPortNo(1);
        line.setToPortNo(1);
        return line;
    }
    
    // ==================== Model Factories ====================
    
    /**
     * Creates a simple test model with basic blocks.
     */
    public static TestModelSetup createSimpleTestModel() {
        Block sourceBlock = createMockBlock("Source", "src-001", "Constant", 0, 1);
        Block processBlock = createMockBlock("Process", "proc-002", "Gain", 1, 1);
        Block sinkBlock = createMockBlock("Sink", "sink-003", "Scope", 1, 0);
        
        List<Block> blocks = Arrays.asList(sourceBlock, processBlock, sinkBlock);
        
        LineDto line1 = createUuidBasedLineDto("Source", "Process", "src-001", "proc-002", 1, 1);
        LineDto line2 = createUuidBasedLineDto("Process", "Sink", "proc-002", "sink-003", 1, 1);
        
        List<LineDto> lines = Arrays.asList(line1, line2);
        
        return new TestModelSetup(blocks, lines);
    }
    
    /**
     * Creates a test model with subsystem blocks.
     */
    public static TestModelSetup createSubsystemTestModel() {
        Block sourceBlock = createMockBlock("Source", "src-001", "Constant", 0, 1);
        Block subsystemBlock = createMockSubsystemBlock("Subsystem", "sub-002", 1, 1);
        Block inBlock = createMockInBlock("In", "in-003");
        Block outBlock = createMockOutBlock("Out", "out-004");
        Block sinkBlock = createMockBlock("Sink", "sink-005", "Scope", 1, 0);
        
        List<Block> blocks = Arrays.asList(sourceBlock, subsystemBlock, inBlock, outBlock, sinkBlock);
        
        // External to subsystem input
        LineDto line1 = createSubsystemLineDto("Source", "In", "src-001", "in-003", 1, 1, 
                                               "TestModel/Subsystem");
        
        // Subsystem output to external
        LineDto line2 = createSubsystemLineDto("Out", "Sink", "out-004", "sink-005", 1, 1,
                                               "TestModel/Subsystem");
        
        List<LineDto> lines = Arrays.asList(line1, line2);
        
        return new TestModelSetup(blocks, lines);
    }
    
    /**
     * Creates a large test model for performance testing.
     */
    public static TestModelSetup createLargeTestModel(int numBlocks, int numLines) {
        List<Block> blocks = new ArrayList<>();
        List<LineDto> lines = new ArrayList<>();
        
        // Create blocks
        for (int i = 0; i < numBlocks; i++) {
            String name = "Block" + i;
            String uuid = "uuid-" + i;
            String type = i == 0 ? "Constant" : (i == numBlocks - 1 ? "Scope" : "Gain");
            int inputs = i == 0 ? 0 : 1;
            int outputs = i == numBlocks - 1 ? 0 : 1;
            
            blocks.add(createMockBlock(name, uuid, type, inputs, outputs));
        }
        
        // Create lines (chain blocks together)
        for (int i = 0; i < Math.min(numLines, numBlocks - 1); i++) {
            Block fromBlock = blocks.get(i);
            Block toBlock = blocks.get(i + 1);
            
            LineDto line = createUuidBasedLineDto(
                fromBlock.getBlockName(), toBlock.getBlockName(),
                fromBlock.getBlockUUID(), toBlock.getBlockUUID(),
                1, 1
            );
            lines.add(line);
        }
        
        return new TestModelSetup(blocks, lines);
    }
    
    // ==================== Test Model Setup Class ====================
    
    /**
     * Container class for test model setup data.
     */
    public static class TestModelSetup {
        private final List<Block> blocks;
        private final List<LineDto> lines;
        
        public TestModelSetup(List<Block> blocks, List<LineDto> lines) {
            this.blocks = blocks;
            this.lines = lines;
        }
        
        public List<Block> getBlocks() {
            return blocks;
        }
        
        public List<LineDto> getLines() {
            return lines;
        }
        
        public NCSLabModel createModel() {
            // Create a mock model since SimulationModel requires constructor parameters
            NCSLabModel model = mock(NCSLabModel.class);
            when(model.getBlockList()).thenReturn(blocks);
            return model;
        }
    }
    
    // ==================== Assertion Helpers ====================
    
    /**
     * Asserts that a line is properly connected.
     */
    public static void assertLineConnected(Line line, String message) {
        if (line == null) {
            throw new AssertionError(message + ": Line is null");
        }
        if (line.getLinkedOutputPort() == null) {
            throw new AssertionError(message + ": Output port is not connected");
        }
        if (line.getLinkedInputPort() == null) {
            throw new AssertionError(message + ": Input port is not connected");
        }
    }
    
    /**
     * Asserts that a LineDto DTO is valid.
     */
    public static void assertLineDtoValid(LineDto lineDto, String message) {
        if (lineDto == null) {
            throw new AssertionError(message + ": LineDto is null");
        }
        if (!lineDto.isValid()) {
            throw new AssertionError(message + ": LineDto is invalid - " + lineDto.getValidationError());
        }
    }
    
    /**
     * Asserts that two lines have equivalent connections.
     */
    public static void assertLinesEquivalent(Line line1, Line line2, String message) {
        if (line1 == null && line2 == null) {
            return; // Both null is equivalent
        }
        if (line1 == null || line2 == null) {
            throw new AssertionError(message + ": One line is null, the other is not");
        }
        
        if (!arePortsEquivalent(line1.getLinkedOutputPort(), line2.getLinkedOutputPort())) {
            throw new AssertionError(message + ": Output ports are not equivalent");
        }
        
        if (!arePortsEquivalent(line1.getLinkedInputPort(), line2.getLinkedInputPort())) {
            throw new AssertionError(message + ": Input ports are not equivalent");
        }
    }
    
    private static boolean arePortsEquivalent(Object port1, Object port2) {
        if (port1 == null && port2 == null) return true;
        if (port1 == null || port2 == null) return false;
        return port1.equals(port2);
    }
    
    // ==================== Random Data Generators ====================
    
    /**
     * Generates a random UUID string for testing.
     */
    public static String generateRandomUUID() {
        return "test-" + UUID.randomUUID().toString();
    }
    
    /**
     * Generates a random block name for testing.
     */
    public static String generateRandomBlockName(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * Creates random test lines between blocks in a list.
     */
    public static List<LineDto> generateRandomLines(List<Block> blocks, int numLines) {
        List<LineDto> lines = new ArrayList<>();
        
        for (int i = 0; i < numLines && blocks.size() >= 2; i++) {
            Block fromBlock = blocks.get((int)(Math.random() * blocks.size()));
            Block toBlock = blocks.get((int)(Math.random() * blocks.size()));
            
            if (!fromBlock.equals(toBlock) && 
                !fromBlock.getOutputPortList().isEmpty() && 
                !toBlock.getInputPortList().isEmpty()) {
                
                LineDto line = createUuidBasedLineDto(
                    fromBlock.getBlockName(), toBlock.getBlockName(),
                    fromBlock.getBlockUUID(), toBlock.getBlockUUID(),
                    1, 1
                );
                lines.add(line);
            }
        }
        
        return lines;
    }
    
    // ==================== Builder Pattern for Complex Test Cases ====================
    
    /**
     * Builder for creating complex test scenarios.
     */
    public static class TestScenarioBuilder {
        private List<Block> blocks = new ArrayList<>();
        private List<LineDto> lines = new ArrayList<>();
        private String modelName = "TestModel";
        
        public TestScenarioBuilder withBlock(String name, String uuid, String type, 
                                           int inputs, int outputs) {
            blocks.add(createMockBlock(name, uuid, type, inputs, outputs));
            return this;
        }
        
        public TestScenarioBuilder withLine(String fromBlock, String toBlock, 
                                          String fromUUID, String toUUID,
                                          int fromPort, int toPort) {
            lines.add(createUuidBasedLineDto(fromBlock, toBlock, fromUUID, toUUID, fromPort, toPort));
            return this;
        }
        
        public TestScenarioBuilder withSubsystemLine(String fromBlock, String toBlock,
                                                   String fromUUID, String toUUID,
                                                   int fromPort, int toPort,
                                                   String subsystemPath) {
            lines.add(createSubsystemLineDto(fromBlock, toBlock, fromUUID, toUUID, 
                                           fromPort, toPort, subsystemPath));
            return this;
        }
        
        public TestScenarioBuilder withModelName(String name) {
            this.modelName = name;
            return this;
        }
        
        public TestModelSetup build() {
            return new TestModelSetup(blocks, lines);
        }
    }
    
    public static TestScenarioBuilder scenario() {
        return new TestScenarioBuilder();
    }
}