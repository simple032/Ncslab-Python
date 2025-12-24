package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.source.Constant;
import com.ncslab.line.Line;
import com.ncslab.ncslablink.Config;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.SimulationModel;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SubsystemTest {

    private NCSLabModel createMockModel() {
        NCSLabModel mockModel = Mockito.mock(NCSLabModel.class);
        Config mockConfig = Mockito.mock(Config.class);
        when(mockModel.getConfig()).thenReturn(mockConfig);
        when(mockConfig.getSolver()).thenReturn("ode45");
        return mockModel;
    }

    @Test
    public void testSubsystemBasicFunctionality() {
        // Create mock model
        NCSLabModel mockModel = createMockModel();
        
        // Create basic subsystem JSON
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("blockUUID", "test-uuid");
        subsystemJSON.put("paramValues", new JSONObject());
        
        // Create subsystem
        Subsystem subsystem = new Subsystem(subsystemJSON, mockModel);
        
        // Test basic properties
        assertEquals("TestSubsystem", subsystem.getBlockName());
        assertEquals("Subsystem", subsystem.getBlockType());
        assertNotNull(subsystem.getInBlockList());
        assertNotNull(subsystem.getOutBlockList());
        assertNotNull(subsystem.getContainedBlocks());
        
        // Test initial state
        assertEquals(0, subsystem.getInBlockList().size());
        assertEquals(0, subsystem.getOutBlockList().size());
        assertEquals(0, subsystem.getBlockCount());
        assertEquals(0, subsystem.getLineCount());
        assertNotNull(subsystem.getContainedLines());
    }
    
    @Test
    public void testAddInBlock() {
        // Create mock model
        NCSLabModel mockModel = createMockModel();
        
        // Create subsystem
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem = new Subsystem(subsystemJSON, mockModel);
        
        // Create In block
        In inBlock = In.create("In1", "model/TestSubsystem", 1, mockModel);
        
        // Add In block to subsystem
        subsystem.addIn(inBlock);
        
        // Verify In block was added
        assertEquals(1, subsystem.getInBlockList().size());
        assertEquals(1, subsystem.getBlockCount());
        assertTrue(subsystem.containsBlock(inBlock));
        assertEquals(subsystem, inBlock.getSubsystem());
        
        // Verify input port was created on subsystem
        assertEquals(1, subsystem.getInputPortList().size());
    }
    
    @Test
    public void testAddOutBlock() {
        // Create mock model
        NCSLabModel mockModel = createMockModel();
        
        // Create subsystem
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem = new Subsystem(subsystemJSON, mockModel);
        
        // Create Out block
        Out outBlock = Out.create("Out1", "model/TestSubsystem", 1, mockModel);
        
        // Add Out block to subsystem
        subsystem.addOut(outBlock);
        
        // Verify Out block was added
        assertEquals(1, subsystem.getOutBlockList().size());
        assertEquals(1, subsystem.getBlockCount());
        assertTrue(subsystem.containsBlock(outBlock));
        assertEquals(subsystem, outBlock.getSubsystem());
        
        // Verify output port was created on subsystem
        assertEquals(1, subsystem.getOutputPortList().size());
    }
    
    @Test
    public void testCleanup() {
        // Create mock model
        NCSLabModel mockModel = createMockModel();
        
        // Create subsystem with blocks
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem = new Subsystem(subsystemJSON, mockModel);
        
        // Add some blocks
        In inBlock = In.create("In1", "model/TestSubsystem", 1, mockModel);
        Out outBlock = Out.create("Out1", "model/TestSubsystem", 1, mockModel);
        
        subsystem.addIn(inBlock);
        subsystem.addOut(outBlock);
        
        // Verify blocks are present
        assertEquals(2, subsystem.getBlockCount());
        
        // Cleanup
        subsystem.cleanup();
        
        // Verify cleanup worked
        assertEquals(0, subsystem.getBlockCount());
        assertEquals(0, subsystem.getInBlockList().size());
        assertEquals(0, subsystem.getOutBlockList().size());
        assertEquals(0, subsystem.getLineCount());
    }
    
    @Test
    public void testLineManagement() {
        // Create mock model
        NCSLabModel mockModel = createMockModel();
        
        // Create subsystem
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem = new Subsystem(subsystemJSON, mockModel);
        
        // Create some blocks for line connection testing
        In inBlock = In.create("In1", "model/TestSubsystem", 1, mockModel);
        Out outBlock = Out.create("Out1", "model/TestSubsystem", 1, mockModel);
        
        subsystem.addIn(inBlock);
        subsystem.addOut(outBlock);
        
        // Test initial line state
        assertEquals(0, subsystem.getLineCount());

        
        // Create a mock line (normally would be created by Line.createLine)
        Line mockLine = Mockito.mock(Line.class);
        when(mockLine.getLinkedOutputPort()).thenReturn(inBlock.getOutputPortList().get(0));
        when(mockLine.getLinkedInputPort()).thenReturn(outBlock.getInputPortList().get(0));
        
        // Add line to subsystem
        subsystem.addLine(mockLine);
        
        // Verify line management
        assertEquals(1, subsystem.getLineCount());
        assertTrue(subsystem.containsLine(mockLine));
        
        // Test line removal
        subsystem.removeLine(mockLine);
        assertEquals(0, subsystem.getLineCount());
        assertFalse(subsystem.containsLine(mockLine));
    }
    
    @Test  
    public void testBoundaryLineClassification() {
        // Create mock model
        NCSLabModel mockModel = createMockModel();
        
        // Create subsystem
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");  
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem = new Subsystem(subsystemJSON, mockModel);
        
        // Create blocks
        In inBlock = In.create("In1", "model/TestSubsystem", 1, mockModel);
        Out outBlock = Out.create("Out1", "model/TestSubsystem", 1, mockModel);
        
        subsystem.addIn(inBlock);
        subsystem.addOut(outBlock);
        
        // Mock boundary line (connects to In block)
        Line boundaryLine = Mockito.mock(Line.class);
        when(boundaryLine.getLinkedOutputPort()).thenReturn(inBlock.getOutputPortList().get(0));
        when(boundaryLine.getLinkedInputPort()).thenReturn(outBlock.getInputPortList().get(0));
        
        subsystem.addLine(boundaryLine);

    }
    
    @Test
    public void testSubsystemBlockAndLineManagement() {
        // Create mock model
        NCSLabModel mockModel = createMockModel();
        
        // Create subsystem
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem = new Subsystem(subsystemJSON, mockModel);
        
        // Test addBlock method directly
        In inBlock = In.create("In1", "model/TestSubsystem", 1, mockModel);
        Out outBlock = Out.create("Out1", "model/TestSubsystem", 1, mockModel);
        
        // Add blocks using addBlock method (this simulates what refactorSubsystemBlocks does)
        subsystem.addBlock(inBlock);
        subsystem.addBlock(outBlock);
        
        // Verify blocks are properly managed
        assertEquals(2, subsystem.getBlockCount());
        assertTrue(subsystem.containsBlock(inBlock));
        assertTrue(subsystem.containsBlock(outBlock));
        
        // Verify In/Out blocks are also in the specialized lists
        assertEquals(1, subsystem.getInBlockList().size());
        assertEquals(1, subsystem.getOutBlockList().size());
        assertEquals(inBlock, subsystem.getInBlockList().get(0));
        assertEquals(outBlock, subsystem.getOutBlockList().get(0));
        
        // Test line management
        Line mockLine = Mockito.mock(Line.class);
        when(mockLine.getLinkedOutputPort()).thenReturn(inBlock.getOutputPortList().get(0));
        when(mockLine.getLinkedInputPort()).thenReturn(outBlock.getInputPortList().get(0));
        
        subsystem.addLine(mockLine);
        
        assertEquals(1, subsystem.getLineCount());
        assertTrue(subsystem.containsLine(mockLine));
        
        // Test the enhanced behavior
        assertEquals("model/TestSubsystem", subsystem.getFullPath());
    }

    @Test
    public void testLineConsistencyWithModelLineList() {
        // Create a real model that has the getLineList() method
        NCSLabModel realModel = createMockModelWithLineList();
        
        // Create subsystem
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem = new Subsystem(subsystemJSON, realModel);
        
        // Create blocks
        In inBlock = In.create("In1", "model/TestSubsystem", 1, realModel);
        Out outBlock = Out.create("Out1", "model/TestSubsystem", 1, realModel);
        
        subsystem.addIn(inBlock);
        subsystem.addOut(outBlock);
        
        // Create a mock line with proper connections
        Line mockLine = Mockito.mock(Line.class);
        when(mockLine.getLinkedOutputPort()).thenReturn(inBlock.getOutputPortList().get(0));
        when(mockLine.getLinkedInputPort()).thenReturn(outBlock.getInputPortList().get(0));
        when(mockLine.getLineId()).thenReturn(1);
        
        // Verify initial state - no lines in model or subsystem
        assertEquals(0, realModel.getLineList().size());
        assertEquals(0, subsystem.getLineCount());
        
        // Add line to subsystem - this should automatically add to model's lineList
        subsystem.addLine(mockLine);
        
        // Verify line is in both subsystem and model
        assertEquals(1, subsystem.getLineCount());
        assertEquals(1, realModel.getLineList().size());
        assertTrue(subsystem.containsLine(mockLine));
        assertTrue(realModel.getLineList().contains(mockLine));
        
        // Verify line consistency validation passes
        assertTrue(subsystem.validateLineConsistency());
        
        // Remove line from subsystem - this should also remove from model's lineList
        subsystem.removeLine(mockLine);
        
        // Verify line is removed from both subsystem and model
        assertEquals(0, subsystem.getLineCount());
        assertEquals(0, realModel.getLineList().size());
        assertFalse(subsystem.containsLine(mockLine));
        assertFalse(realModel.getLineList().contains(mockLine));
        
        // Verify line consistency validation still passes (no orphaned lines)
        assertTrue(subsystem.validateLineConsistency());
    }
    
    @Test
    public void testLineConsistencyRepair() {
        // Create a real model that has the getLineList() method
        NCSLabModel realModel = createMockModelWithLineList();
        
        // Create subsystem
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "TestSubsystem");
        subsystemJSON.put("blockPath", "model");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem = new Subsystem(subsystemJSON, realModel);
        
        // Create blocks
        In inBlock = In.create("In1", "model/TestSubsystem", 1, realModel);
        Out outBlock = Out.create("Out1", "model/TestSubsystem", 1, realModel);
        
        subsystem.addIn(inBlock);
        subsystem.addOut(outBlock);
        
        // Create a mock line
        Line orphanedLine = Mockito.mock(Line.class);
        when(orphanedLine.getLinkedOutputPort()).thenReturn(inBlock.getOutputPortList().get(0));
        when(orphanedLine.getLinkedInputPort()).thenReturn(outBlock.getInputPortList().get(0));
        when(orphanedLine.getLineId()).thenReturn(0); // Simulates line without sequence assigned
        
        // Manually add line to subsystem's containedLines but NOT to model's lineList
        // This simulates the old broken behavior
        subsystem.getContainedLines().add(orphanedLine);
        
        // Verify inconsistent state
        assertEquals(1, subsystem.getLineCount());
        assertEquals(0, realModel.getLineList().size());
        assertFalse(subsystem.validateLineConsistency()); // Should fail validation
        
        // Repair the consistency
        int repairedLines = subsystem.repairLineConsistency();
        assertEquals(1, repairedLines);
        
        // Verify repaired state
        assertEquals(1, subsystem.getLineCount());
        assertEquals(1, realModel.getLineList().size());
        assertTrue(subsystem.validateLineConsistency()); // Should now pass validation
        assertTrue(realModel.getLineList().contains(orphanedLine));
        
        // Verify line sequence was assigned during repair
        verify(orphanedLine).setLineId(anyInt());
    }
    
    private NCSLabModel createMockModelWithLineList() {
        NCSLabModel mockModel = Mockito.mock(NCSLabModel.class);
        Config mockConfig = Mockito.mock(Config.class);
        
        // Create a real ArrayList for lineList to test actual functionality
        List<Line> lineList = new ArrayList<>();
        
        when(mockModel.getConfig()).thenReturn(mockConfig);
        when(mockConfig.getSolver()).thenReturn("ode45");
        when(mockModel.getLineList()).thenReturn(lineList);
        when(mockModel.assignNextLineSequence()).thenReturn(1, 2, 3, 4, 5); // Return incrementing sequences
        
        return mockModel;
    }
}