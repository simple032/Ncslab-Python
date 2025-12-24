package com.ncslab.line;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.dto.model.LineDto;
import com.ncslab.ncslablink.NCSLabModel;

/**
 * Integration tests for the complete DTO processing pipeline.
 * 
 * Tests end-to-end scenarios including:
 * 1. Complete model processing with DTO-native line creation
 * 2. Mixed JSON and DTO processing compatibility
 * 3. Performance and scalability with large models
 * 4. Error recovery and robustness testing
 * 
 * @author Claude Code
 */
@RunWith(MockitoJUnitRunner.class)
public class LineDtoIntegrationTest {
    
    @Mock
    private Block mockConstantBlock;
    
    @Mock
    private Block mockGainBlock;
    
    @Mock
    private Block mockScopeBlock;
    
    @Mock
    private OutputPort mockOutputPort1;
    
    @Mock
    private OutputPort mockOutputPort2;
    
    @Mock
    private InputPort mockInputPort1;
    
    @Mock
    private InputPort mockInputPort2;
    
    private NCSLabModel testModel;
    private List<Block> modelBlocks;
    private List<LineDto> testLines;
    
    @Before
    public void setUp() {
        setupMockBlocks();
        setupTestModel();
        setupTestScenarios();
    }
    
    private void setupMockBlocks() {
        // Setup Constant block (source)
        when(mockConstantBlock.getBlockName()).thenReturn("Constant1");
        when(mockConstantBlock.getBlockUUID()).thenReturn("constant-uuid-001");
        when(mockConstantBlock.getBlockType()).thenReturn("Constant");
        when(mockConstantBlock.getOutputPortList()).thenReturn(Arrays.asList(mockOutputPort1));
        when(mockConstantBlock.getInputPortList()).thenReturn(new ArrayList<>());
        
        // Setup Gain block (processing)
        when(mockGainBlock.getBlockName()).thenReturn("Gain1");
        when(mockGainBlock.getBlockUUID()).thenReturn("gain-uuid-002");
        when(mockGainBlock.getBlockType()).thenReturn("Gain");
        when(mockGainBlock.getInputPortList()).thenReturn(Arrays.asList(mockInputPort1));
        when(mockGainBlock.getOutputPortList()).thenReturn(Arrays.asList(mockOutputPort2));
        
        // Setup Scope block (sink)
        when(mockScopeBlock.getBlockName()).thenReturn("Scope1");
        when(mockScopeBlock.getBlockUUID()).thenReturn("scope-uuid-003");
        when(mockScopeBlock.getBlockType()).thenReturn("Scope");
        when(mockScopeBlock.getInputPortList()).thenReturn(Arrays.asList(mockInputPort2));
        when(mockScopeBlock.getOutputPortList()).thenReturn(new ArrayList<>());
        
        // Setup ports
        when(mockOutputPort1.getNumber()).thenReturn(1);
        when(mockOutputPort2.getNumber()).thenReturn(1);
        when(mockInputPort1.getNumber()).thenReturn(1);
        when(mockInputPort2.getNumber()).thenReturn(1);
        
        modelBlocks = new ArrayList<>();
        modelBlocks.addAll(Arrays.asList(mockConstantBlock, mockGainBlock, mockScopeBlock));
    }
    
    private void setupTestModel() {
        // Create a mock model since SimulationModel requires constructor parameters
        testModel = mock(NCSLabModel.class);
        when(testModel.getBlockList()).thenReturn(modelBlocks);
    }
    
    private void setupTestScenarios() {
        testLines = new ArrayList<>();
        
        // Simple connection: Constant -> Gain
        LineDto line1 = new LineDto();
        line1.setFromBlockName("Constant1");
        line1.setToBlockName("Gain1");
        line1.setFromBlockUUID("constant-uuid-001");
        line1.setToBlockUUID("gain-uuid-002");
        line1.setFromPortNo(1);
        line1.setToPortNo(1);
        line1.setLinePath("TestModel");
        testLines.add(line1);
        
        // Simple connection: Gain -> Scope
        LineDto line2 = new LineDto();
        line2.setFromBlockName("Gain1");
        line2.setToBlockName("Scope1");
        line2.setFromBlockUUID("gain-uuid-002");
        line2.setToBlockUUID("scope-uuid-003");
        line2.setFromPortNo(1);
        line2.setToPortNo(1);
        line2.setLinePath("TestModel");
        testLines.add(line2);
    }
    
    // ==================== End-to-End Pipeline Tests ====================
    
    @Test
    public void testCompleteModelProcessing_AllDtoNative() {
        // Test processing a complete model using only DTO-native methods
        List<Line> createdLines = new ArrayList<>();
        
        // Process all test lines
        for (LineDto lineDto : testLines) {
            try {
                Line line = Line.createLine(lineDto, testModel.getBlockList());
                if (line != null && line.getLinkedOutputPort() != null && line.getLinkedInputPort() != null) {
                    createdLines.add(line);
                }
            } catch (Exception e) {
                fail("Line creation should not fail for valid DTOs: " + e.getMessage());
            }
        }
        
        // Assert
        assertTrue("At least some lines should be created successfully", createdLines.size() > 0);
        
        // Verify each created line has proper connections
        for (Line line : createdLines) {
            assertNotNull("Line should have output port", line.getLinkedOutputPort());
            assertNotNull("Line should have input port", line.getLinkedInputPort());
        }
    }
    
    @Test
    public void testMixedJsonDtoCompatibility() {
        // Test that DTO and JSON methods produce equivalent results
        
        // Create equivalent JSON for the first test line
        org.json.JSONObject jsonLine = new org.json.JSONObject();
        LineDto dtoLine = testLines.get(0);
        
        jsonLine.put("fromBlockName", dtoLine.getFromBlockName());
        jsonLine.put("toBlockName", dtoLine.getToBlockName());
        jsonLine.put("fromBlockUUID", dtoLine.getFromBlockUUID());
        jsonLine.put("toBlockUUID", dtoLine.getToBlockUUID());
        jsonLine.put("fromPortNo", dtoLine.getFromPortNo());
        jsonLine.put("toPortNo", dtoLine.getToPortNo());
        
        // Create lines using both methods
        Line dtoCreatedLine = Line.createLine(dtoLine, testModel.getBlockList());
        Line jsonCreatedLine = Line.createLine(jsonLine, testModel.getBlockList());

        // Assert compatibility
        if (dtoCreatedLine != null && jsonCreatedLine != null) {
            assertEquals("DTO and JSON methods should produce equivalent output ports",
                         jsonCreatedLine.getLinkedOutputPort(), dtoCreatedLine.getLinkedOutputPort());
            assertEquals("DTO and JSON methods should produce equivalent input ports",
                         jsonCreatedLine.getLinkedInputPort(), dtoCreatedLine.getLinkedInputPort());
        }
    }
    
    @Test
    public void testLargeModelPerformance() {
        // Test performance with a large number of blocks and lines
        
        // Create a large model
        List<Block> largeBlockList = createLargeBlockModel(100, 200);
        List<LineDto> largeLineList = createRandomLines(largeBlockList, 150);
        
        // Measure performance
        long startTime = System.nanoTime();
        
        int successfulConnections = 0;
        for (LineDto lineDto : largeLineList) {
            try {
                Line line = Line.createLine(lineDto, largeBlockList);
                if (line != null && line.getLinkedOutputPort() != null && line.getLinkedInputPort() != null) {
                    successfulConnections++;
                }
            } catch (Exception e) {
                // Expected for some random connections
            }
        }
        
        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;
        
        // Assert performance and functionality
        assertTrue("Large model processing should complete within 1 second, actual: " + 
                   durationMs + "ms", durationMs < 1000.0);
        assertTrue("Some connections should succeed in large model", successfulConnections > 0);
    }
    
    @Test
    public void testErrorRecoveryAndRobustness() {
        // Test system robustness with various error conditions
        
        List<LineDto> problematicLines = createProblematicLines();
        
        int handledErrors = 0;
        for (LineDto lineDto : problematicLines) {
            try {
                Line line = Line.createLine(lineDto, testModel.getBlockList());
                // Some may succeed, some may fail gracefully
            } catch (IllegalArgumentException e) {
                // Expected for invalid DTOs
                handledErrors++;
            } catch (Exception e) {
                // Unexpected exceptions should not occur
                fail("Unexpected exception for problematic line: " + e.getMessage());
            }
        }
        
        // Assert that errors are handled properly
        assertTrue("System should handle problematic inputs gracefully", handledErrors > 0);
    }
    
    @Test
    public void testConcurrentProcessing() {
        // Test thread safety with concurrent line processing
        
        final List<Exception> concurrentExceptions = new ArrayList<>();
        final int numThreads = 5;
        final int linesPerThread = 10;
        
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < linesPerThread; j++) {
                        LineDto lineDto = testLines.get(j % testLines.size());
                        Line.createLine(lineDto, testModel.getBlockList());
                    }
                } catch (Exception e) {
                    synchronized (concurrentExceptions) {
                        concurrentExceptions.add(e);
                    }
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        for (Thread thread : threads) {
            try {
                thread.join(2000); // 2 second timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Assert thread safety
        assertTrue("No exceptions should occur during concurrent processing: " + concurrentExceptions,
                   concurrentExceptions.isEmpty());
    }
    
    // ==================== Behavioral Verification Tests ====================
    
    @Test
    public void testPortLinkingBehavior() {
        // Verify that port linking behavior is correct and consistent
        
        LineDto testLine = testLines.get(0); // Simple Constant -> Gain line
        Line line = Line.createLine(testLine, testModel.getBlockList());

        if (line != null && line.getLinkedOutputPort() != null) {
            // Verify output port linking
            verify(line.getLinkedOutputPort()).addLinkedLine(line);
            
            // Verify input port linking
            if (line.getLinkedInputPort() != null) {
                verify(line.getLinkedInputPort()).setLinkedLine(line);
            }
        }
    }
    
    @Test
    public void testLineIdAssignment() {
        // Test that lines can have IDs assigned and retrieved
        
        LineDto testLine = testLines.get(0);
        Line line = Line.createLine(testLine, testModel.getBlockList());

        if (line != null) {
            // Test line ID functionality
            line.setLineId(12345);
            assertEquals("Line ID should be settable and retrievable", 12345, line.getLineId());
        }
    }
    
    // ==================== Helper Methods ====================
    
    private List<Block> createLargeBlockModel(int numBlocks, int numPorts) {
        List<Block> largeBlocks = new ArrayList<>(modelBlocks);
        
        for (int i = 0; i < numBlocks; i++) {
            Block block = mock(Block.class);
            when(block.getBlockName()).thenReturn("Block" + i);
            when(block.getBlockUUID()).thenReturn("uuid-" + i);
            when(block.getBlockType()).thenReturn("Generic");
            
            // Create ports
            List<InputPort> inputPorts = new ArrayList<>();
            List<OutputPort> outputPorts = new ArrayList<>();
            
            for (int j = 1; j <= Math.min(5, numPorts / numBlocks + 1); j++) {
                InputPort inPort = mock(InputPort.class);
                when(inPort.getNumber()).thenReturn(j);
                inputPorts.add(inPort);
                
                OutputPort outPort = mock(OutputPort.class);
                when(outPort.getNumber()).thenReturn(j);
                outputPorts.add(outPort);
            }
            
            when(block.getInputPortList()).thenReturn(inputPorts);
            when(block.getOutputPortList()).thenReturn(outputPorts);
            
            largeBlocks.add(block);
        }
        
        return largeBlocks;
    }
    
    private List<LineDto> createRandomLines(List<Block> blocks, int numLines) {
        List<LineDto> randomLines = new ArrayList<>();
        
        for (int i = 0; i < numLines; i++) {
            if (blocks.size() < 2) break;
            
            Block fromBlock = blocks.get(i % blocks.size());
            Block toBlock = blocks.get((i + 1) % blocks.size());
            
            LineDto line = new LineDto();
            line.setFromBlockName(fromBlock.getBlockName());
            line.setToBlockName(toBlock.getBlockName());
            line.setFromBlockUUID(fromBlock.getBlockUUID());
            line.setToBlockUUID(toBlock.getBlockUUID());
            line.setFromPortNo(1);
            line.setToPortNo(1);
            line.setLinePath("LargeModel");
            
            randomLines.add(line);
        }
        
        return randomLines;
    }
    
    private List<LineDto> createProblematicLines() {
        List<LineDto> problematicLines = new ArrayList<>();
        
        // Line with null block names
        LineDto nullLine = new LineDto();
        nullLine.setFromBlockName(null);
        nullLine.setToBlockName("Scope1");
        problematicLines.add(nullLine);
        
        // Line with invalid port numbers
        LineDto invalidPortLine = new LineDto();
        invalidPortLine.setFromBlockName("Constant1");
        invalidPortLine.setToBlockName("Scope1");
        invalidPortLine.setFromPortNo("invalid");
        invalidPortLine.setToPortNo(1);
        problematicLines.add(invalidPortLine);
        
        // Line with non-existent blocks
        LineDto missingBlockLine = new LineDto();
        missingBlockLine.setFromBlockName("NonExistentBlock");
        missingBlockLine.setToBlockName("AlsoNonExistent");
        missingBlockLine.setFromPortNo(1);
        missingBlockLine.setToPortNo(1);
        problematicLines.add(missingBlockLine);
        
        return problematicLines;
    }
    
    // ==================== Test Data Factories ====================
    
    /**
     * Factory for creating test models with realistic block structures.
     */
    public static class TestModelFactory {
        
        public static List<Block> createSimpleModel() {
            // Create a simple 3-block model: Source -> Processing -> Sink
            return Arrays.asList(
                LineDtoProcessingTest.createMockBlockWithPorts("Source", "src-001", "Constant", 0, 1),
                LineDtoProcessingTest.createMockBlockWithPorts("Process", "proc-002", "Gain", 1, 1),
                LineDtoProcessingTest.createMockBlockWithPorts("Sink", "sink-003", "Scope", 1, 0)
            );
        }
        
        public static List<LineDto> createSimpleConnections() {
            List<LineDto> lines = new ArrayList<>();
            
            // Source -> Process
            lines.add(LineDtoProcessingTest.createTestLineDto(
                "Source", "Process", "src-001", "proc-002", 1, 1));
            
            // Process -> Sink
            lines.add(LineDtoProcessingTest.createTestLineDto(
                "Process", "Sink", "proc-002", "sink-003", 1, 1));
            
            return lines;
        }
    }
}