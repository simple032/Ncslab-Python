package com.ncslab.line;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

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
 * Comprehensive unit tests for DTO-native line processing methods.
 * 
 * Tests the following implementations:
 * 1. Line.createLine() factory methods
 * 2. Line construction with DTO objects
 * 3. Edge cases and error handling
 * 
 * Uses JUnit 4, Mockito for mocking, and comprehensive edge case testing.
 * 
 * @author Claude Code
 */
@RunWith(MockitoJUnitRunner.class)
public class LineDtoProcessingTest {
    
    @Mock
    private NCSLabModel mockModel;
    
    @Mock
    private Block mockFromBlock;
    
    @Mock
    private Block mockToBlock;
    
    @Mock
    private Block mockInBlock;
    
    @Mock
    private Block mockOutBlock;
    
    @Mock
    private OutputPort mockOutputPort;
    
    @Mock
    private InputPort mockInputPort;
    
    private List<Block> blockList;
    private LineDto validLineDto;
    private LineDto invalidLineDto;
    private LineDto subsystemLineDto;
    
    @Before
    public void setUp() {
        blockList = new ArrayList<>();
        setupMockBlocks();
        setupTestData();
    }
    
    private void setupMockBlocks() {
        // Setup mock from block
        when(mockFromBlock.getBlockName()).thenReturn("SourceBlock");
        when(mockFromBlock.getBlockUUID()).thenReturn("source-uuid-123");
        when(mockFromBlock.getBlockType()).thenReturn("Constant");
        when(mockFromBlock.getOutputPortList()).thenReturn(Arrays.asList(mockOutputPort));
        
        // Setup mock to block
        when(mockToBlock.getBlockName()).thenReturn("SinkBlock");
        when(mockToBlock.getBlockUUID()).thenReturn("sink-uuid-456");
        when(mockToBlock.getBlockType()).thenReturn("Scope");
        when(mockToBlock.getInputPortList()).thenReturn(Arrays.asList(mockInputPort));
        
        // Setup mock In block for subsystem testing
        when(mockInBlock.getBlockName()).thenReturn("InBlock");
        when(mockInBlock.getBlockUUID()).thenReturn("in-uuid-789");
        when(mockInBlock.getBlockType()).thenReturn("In");
        when(mockInBlock.getInputPortList()).thenReturn(Arrays.asList(mockInputPort));
        
        // Setup mock Out block for subsystem testing
        when(mockOutBlock.getBlockName()).thenReturn("OutBlock");
        when(mockOutBlock.getBlockUUID()).thenReturn("out-uuid-abc");
        when(mockOutBlock.getBlockType()).thenReturn("Out");
        when(mockOutBlock.getOutputPortList()).thenReturn(Arrays.asList(mockOutputPort));
        
        // Setup mock ports
        when(mockOutputPort.getNumber()).thenReturn(1);
        when(mockInputPort.getNumber()).thenReturn(1);
        
        // Add blocks to list
        blockList.addAll(Arrays.asList(mockFromBlock, mockToBlock, mockInBlock, mockOutBlock));
        
        // Setup mock model
        when(mockModel.getBlockList()).thenReturn(blockList);
    }
    
    private void setupTestData() {
        // Valid line DTO
        validLineDto = new LineDto();
        validLineDto.setFromBlockName("SourceBlock");
        validLineDto.setToBlockName("SinkBlock");
        validLineDto.setFromBlockUUID("source-uuid-123");
        validLineDto.setToBlockUUID("sink-uuid-456");
        validLineDto.setFromPortNo(1);
        validLineDto.setToPortNo(1);
        validLineDto.setLinePath("TestModel");
        
        // Invalid line DTO (missing required fields)
        invalidLineDto = new LineDto();
        invalidLineDto.setFromBlockName(null);
        invalidLineDto.setToBlockName("SinkBlock");
        
        // Subsystem line DTO
        subsystemLineDto = new LineDto();
        subsystemLineDto.setFromBlockName("OutBlock");
        subsystemLineDto.setToBlockName("InBlock");
        subsystemLineDto.setFromBlockUUID("out-uuid-abc");
        subsystemLineDto.setToBlockUUID("in-uuid-789");
        subsystemLineDto.setFromPortNo(1);
        subsystemLineDto.setToPortNo(1);
        subsystemLineDto.setLinePath("TestModel/Subsystem");
    }
    
    // ==================== Line.createLine() Tests ====================
    
    @Test
    public void testCreateLineFromDto_WithModel_ValidDto_Success() {
        // Act
        Line result = Line.createLine(validLineDto, mockModel.getBlockList());
        
        // Assert
        assertNotNull("Line should be created successfully", result);
        assertEquals("Output port should be linked", mockOutputPort, result.getLinkedOutputPort());
        assertEquals("Input port should be linked", mockInputPort, result.getLinkedInputPort());
        
        // Verify port linking behavior
        verify(mockOutputPort).addLinkedLine(result);
        verify(mockInputPort).setLinkedLine(result);
    }
    
    @Test
    public void testCreateLineFromDto_WithBlockList_ValidDto_Success() {
        // Act
        Line result = Line.createLine(validLineDto, blockList);
        
        // Assert
        assertNotNull("Line should be created successfully", result);
        assertEquals("Output port should be linked", mockOutputPort, result.getLinkedOutputPort());
        assertEquals("Input port should be linked", mockInputPort, result.getLinkedInputPort());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateLineFromDto_NullDto_ThrowsException() {
        // Act
        Line.createLine((LineDto)null, mockModel.getBlockList());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateLineFromDto_NullModel_ThrowsException() {
        // Act
        Line.createLine(validLineDto, (List<Block>) null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateLineFromDto_NullBlockList_ThrowsException() {
        // Act
        Line.createLine(validLineDto, (List<Block>) null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateLineFromDto_InvalidDto_ThrowsException() {
        // Act
        Line.createLine(invalidLineDto, mockModel.getBlockList());
    }
    
    @Test
    public void testCreateLineFromDto_UuidLookup_vs_NameLookup() {
        // Create DTO with UUIDs
        LineDto uuidDto = new LineDto();
        uuidDto.setFromBlockName("SourceBlock");
        uuidDto.setToBlockName("SinkBlock");
        uuidDto.setFromBlockUUID("source-uuid-123");
        uuidDto.setToBlockUUID("sink-uuid-456");
        uuidDto.setFromPortNo(1);
        uuidDto.setToPortNo(1);
        
        // Create DTO without UUIDs (name-based lookup)
        LineDto nameDto = new LineDto();
        nameDto.setFromBlockName("SourceBlock");
        nameDto.setToBlockName("SinkBlock");
        nameDto.setFromBlockUUID(null);
        nameDto.setToBlockUUID(null);
        nameDto.setFromPortNo(1);
        nameDto.setToPortNo(1);
        
        // Act
        Line uuidLine = Line.createLine(uuidDto, blockList);
        Line nameLine = Line.createLine(nameDto, blockList);
        
        // Assert - both should succeed and create equivalent lines
        assertNotNull("UUID-based line should be created", uuidLine);
        assertNotNull("Name-based line should be created", nameLine);
        assertEquals("Both lines should link same output port", 
                     uuidLine.getLinkedOutputPort(), nameLine.getLinkedOutputPort());
        assertEquals("Both lines should link same input port", 
                     uuidLine.getLinkedInputPort(), nameLine.getLinkedInputPort());
    }
    
    @Test
    public void testCreateLineFromDto_SpecialBlockTypes_InAndOut() {
        // Test with "In" block type
        LineDto inLineDto = new LineDto();
        inLineDto.setFromBlockName("SourceBlock");
        inLineDto.setToBlockName("InBlock");
        inLineDto.setFromBlockUUID("source-uuid-123");
        inLineDto.setToBlockUUID("in-uuid-789");
        inLineDto.setFromPortNo(1);
        inLineDto.setToPortNo(999); // Should be ignored for "In" blocks
        
        // Test with "Out" block type
        LineDto outLineDto = new LineDto();
        outLineDto.setFromBlockName("OutBlock");
        outLineDto.setToBlockName("SinkBlock");
        outLineDto.setFromBlockUUID("out-uuid-abc");
        outLineDto.setToBlockUUID("sink-uuid-456");
        outLineDto.setFromPortNo(999); // Should be ignored for "Out" blocks
        outLineDto.setToPortNo(1);
        
        // Act
        Line inLine = Line.createLine(inLineDto, blockList);
        Line outLine = Line.createLine(outLineDto, blockList);
        
        // Assert
        assertNotNull("Line to In block should be created", inLine);
        assertNotNull("Line from Out block should be created", outLine);
        
        // Verify that special block types use port 0 regardless of specified port number
        assertEquals("In block should use first input port", mockInputPort, inLine.getLinkedInputPort());
        assertEquals("Out block should use first output port", mockOutputPort, outLine.getLinkedOutputPort());
    }
    
    @Test
    public void testCreateLineFromDto_MissingBlocks_ReturnsPartialLine() {
        // Create DTO with non-existent block names
        LineDto missingBlockDto = new LineDto();
        missingBlockDto.setFromBlockName("NonExistentSource");
        missingBlockDto.setToBlockName("NonExistentSink");
        missingBlockDto.setFromPortNo(1);
        missingBlockDto.setToPortNo(1);
        
        // Act
        Line result = Line.createLine(missingBlockDto, blockList);
        
        // Assert - should return a line with null ports due to early return in constructor
        assertNotNull("Line object should be created", result);
        assertNull("Output port should be null when from block not found", result.getLinkedOutputPort());
        assertNull("Input port should be null when to block not found", result.getLinkedInputPort());
    }
    
    @Test
    public void testCreateLineFromDto_MissingPorts_ReturnsPartialLine() {
        // Setup blocks with no ports
        Block emptyFromBlock = mock(Block.class);
        Block emptyToBlock = mock(Block.class);
        lenient().when(emptyFromBlock.getBlockName()).thenReturn("EmptySource");
        lenient().when(emptyFromBlock.getBlockUUID()).thenReturn("empty-source-123");
        lenient().when(emptyFromBlock.getBlockType()).thenReturn("Constant");
        lenient().when(emptyFromBlock.getOutputPortList()).thenReturn(new ArrayList<>());
        
        lenient().when(emptyToBlock.getBlockName()).thenReturn("EmptySink");
        lenient().when(emptyToBlock.getBlockUUID()).thenReturn("empty-sink-456");
        lenient().when(emptyToBlock.getBlockType()).thenReturn("Scope");
        lenient().when(emptyToBlock.getInputPortList()).thenReturn(new ArrayList<>());
        
        List<Block> emptyPortBlocks = Arrays.asList(emptyFromBlock, emptyToBlock);
        
        LineDto emptyPortDto = new LineDto();
        emptyPortDto.setFromBlockName("EmptySource");
        emptyPortDto.setToBlockName("EmptySink");
        emptyPortDto.setFromBlockUUID("empty-source-123");
        emptyPortDto.setToBlockUUID("empty-sink-456");
        emptyPortDto.setFromPortNo(1);
        emptyPortDto.setToPortNo(1);
        
        // Act
        Line result = Line.createLine(emptyPortDto, emptyPortBlocks);
        
        // Assert
        assertNotNull("Line object should be created", result);
        assertNull("Output port should be null when port not found", result.getLinkedOutputPort());
        assertNull("Input port should be null when port not found", result.getLinkedInputPort());
    }
    
    @Test
    public void testPortNumberParsing_IntegerAndString() {
        // Test with string port numbers
        LineDto stringPortDto = new LineDto();
        stringPortDto.setFromBlockName("SourceBlock");
        stringPortDto.setToBlockName("SinkBlock");
        stringPortDto.setFromBlockUUID("source-uuid-123");
        stringPortDto.setToBlockUUID("sink-uuid-456");
        stringPortDto.setFromPortNo("1");  // String port number
        stringPortDto.setToPortNo("1");    // String port number
        
        // Act
        Line result = Line.createLine(stringPortDto, blockList);
        
        // Assert
        assertNotNull("Line should be created with string port numbers", result);
        assertEquals("Should link correct output port", mockOutputPort, result.getLinkedOutputPort());
        assertEquals("Should link correct input port", mockInputPort, result.getLinkedInputPort());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPortNumberParsing_InvalidFormat() {
        // Test with invalid port number format
        LineDto invalidPortDto = new LineDto();
        invalidPortDto.setFromBlockName("SourceBlock");
        invalidPortDto.setToBlockName("SinkBlock");
        invalidPortDto.setFromBlockUUID("source-uuid-123");
        invalidPortDto.setToBlockUUID("sink-uuid-456");
        invalidPortDto.setFromPortNo("invalid_number");
        invalidPortDto.setToPortNo(1);
        
        // Act - should throw IllegalArgumentException
        Line.createLine(invalidPortDto, blockList);
    }
    
    // ==================== Behavioral Verification Tests ====================
    
    @Test
    public void testBehavioralEquivalence_DtoVsJsonMethods() {
        // This test verifies that DTO methods produce equivalent results to JSON methods
        // by comparing the final state of created lines
        
        // Create equivalent JSON object for comparison
        org.json.JSONObject jsonLine = new org.json.JSONObject();
        jsonLine.put("fromBlockName", validLineDto.getFromBlockName());
        jsonLine.put("toBlockName", validLineDto.getToBlockName());
        jsonLine.put("fromBlockUUID", validLineDto.getFromBlockUUID());
        jsonLine.put("toBlockUUID", validLineDto.getToBlockUUID());
        jsonLine.put("fromPortNo", validLineDto.getFromPortNo());
        jsonLine.put("toPortNo", validLineDto.getToPortNo());
        
        // Act - create lines using both methods
        Line dtoLine = Line.createLine(validLineDto, blockList);
        Line jsonLine_obj = Line.createLine(jsonLine, blockList);
        
        // Assert - both should produce equivalent results
        assertNotNull("DTO line should be created", dtoLine);
        assertNotNull("JSON line should be created", jsonLine_obj);
        
        assertEquals("Both methods should link same output port", 
                     jsonLine_obj.getLinkedOutputPort(), dtoLine.getLinkedOutputPort());
        assertEquals("Both methods should link same input port", 
                     jsonLine_obj.getLinkedInputPort(), dtoLine.getLinkedInputPort());
    }
    
    // ==================== Edge Cases and Error Handling ====================
    
    @Test
    public void testCreateLineFromDto_EmptyBlockList_ReturnsPartialLine() {
        // Act
        Line result = Line.createLine(validLineDto, new ArrayList<>());
        
        // Assert
        assertNotNull("Line object should be created even with empty block list", result);
        assertNull("Output port should be null with empty block list", result.getLinkedOutputPort());
        assertNull("Input port should be null with empty block list", result.getLinkedInputPort());
    }
    
    @Test
    public void testCreateLineFromDto_NullPortNumbers_ThrowsException() {
        LineDto nullPortDto = new LineDto();
        nullPortDto.setFromBlockName("SourceBlock");
        nullPortDto.setToBlockName("SinkBlock");
        nullPortDto.setFromBlockUUID("source-uuid-123");
        nullPortDto.setToBlockUUID("sink-uuid-456");
        nullPortDto.setFromPortNo(null);  // Null port number
        nullPortDto.setToPortNo(1);
        
        try {
            Line.createLine(nullPortDto, blockList);
            fail("Should throw IllegalArgumentException for null port number");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention port number", 
                       e.getMessage().contains("Port number cannot be null"));
        }
    }
    
    @Test
    public void testCreateLineFromDto_InvalidPortNumberType_ThrowsException() {
        LineDto invalidTypeDto = new LineDto();
        invalidTypeDto.setFromBlockName("SourceBlock");
        invalidTypeDto.setToBlockName("SinkBlock");
        invalidTypeDto.setFromBlockUUID("source-uuid-123");
        invalidTypeDto.setToBlockUUID("sink-uuid-456");
        invalidTypeDto.setFromPortNo(new Object());  // Invalid type
        invalidTypeDto.setToPortNo(1);
        
        try {
            Line.createLine(invalidTypeDto, blockList);
            fail("Should throw IllegalArgumentException for invalid port number type");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention port number type", 
                       e.getMessage().contains("Port number must be Integer or String"));
        }
    }
    
    @Test
    public void testCreateLineFromDto_ConcurrentModification_ThreadSafety() {
        // Test thread safety with concurrent access to block list
        final List<Block> concurrentBlockList = new ArrayList<>(blockList);
        final List<Exception> exceptions = new ArrayList<>();
        
        // Create multiple threads that access the line creation concurrently
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        Line.createLine(validLineDto, concurrentBlockList);
                    }
                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join(1000); // 1 second timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Assert no exceptions occurred
        assertTrue("No exceptions should occur during concurrent access: " + exceptions, 
                   exceptions.isEmpty());
    }
    
    // ==================== Performance Tests ====================
    
    @Test
    public void testCreateLineFromDto_Performance_LargeBlockList() {
        // Create a large block list to test performance
        List<Block> largeBlockList = new ArrayList<>(blockList);
        
        // Add many dummy blocks
        for (int i = 0; i < 1000; i++) {
            Block dummyBlock = mock(Block.class);
            lenient().when(dummyBlock.getBlockName()).thenReturn("DummyBlock" + i);
            lenient().when(dummyBlock.getBlockUUID()).thenReturn("dummy-uuid-" + i);
            lenient().when(dummyBlock.getBlockType()).thenReturn("Dummy");
            largeBlockList.add(dummyBlock);
        }
        
        // Measure performance
        long startTime = System.nanoTime();
        Line result = Line.createLine(validLineDto, largeBlockList);
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        
        // Assert
        assertNotNull("Line should be created even with large block list", result);
        assertTrue("Line creation should complete within reasonable time (< 100ms), actual: " + 
                   durationMs + "ms", durationMs < 100.0);
    }
    
    // ==================== Test Data Factories ====================
    
    /**
     * Factory method to create test LineDto instances with realistic data.
     */
    public static LineDto createTestLineDto(String fromBlockName, String toBlockName, 
                                             String fromUUID, String toUUID, 
                                             Object fromPort, Object toPort) {
        LineDto line = new LineDto();
        line.setFromBlockName(fromBlockName);
        line.setToBlockName(toBlockName);
        line.setFromBlockUUID(fromUUID);
        line.setToBlockUUID(toUUID);
        line.setFromPortNo(fromPort);
        line.setToPortNo(toPort);
        line.setLinePath("TestModel");
        return line;
    }
    
    /**
     * Factory method to create a realistic block mock with ports.
     */
    public static Block createMockBlockWithPorts(String name, String uuid, String type, 
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
}