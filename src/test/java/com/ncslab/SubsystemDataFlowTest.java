package com.ncslab;

import com.ncslab.block.Block;
import com.ncslab.block.source.Constant;
import com.ncslab.block.sink.Scope;
import com.ncslab.block.subsystem.Subsystem;
import com.ncslab.block.subsystem.In;
import com.ncslab.block.subsystem.Out;
import com.ncslab.line.Line;
import com.ncslab.ncslablink.Config;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test to trace the data flow issue where In1 receives 0 instead of 1.
 * This test creates a simple model: Constant2(1) -> Subsystem1 -> Scope2
 * And verifies the data flows correctly through the subsystem boundary.
 */
public class SubsystemDataFlowTest {
    
    private NCSLabModel createMockModel() {
        NCSLabModel mockModel = Mockito.mock(NCSLabModel.class);
        Config mockConfig = Mockito.mock(Config.class);
        when(mockModel.getConfig()).thenReturn(mockConfig);
        when(mockConfig.getSolver()).thenReturn("ode45");
        when(mockModel.assignNextBlockSequence()).thenReturn(1, 2, 3, 4, 5);
        when(mockModel.assignNextLineSequence()).thenReturn(1, 2, 3);
        return mockModel;
    }
    
    @Test
    public void testSubsystemBoundaryDataFlow() throws Exception {
        System.out.println("=== STARTING SUBSYSTEM BOUNDARY DATA FLOW TEST ===");
        
        // Create mock model
        NCSLabModel model = createMockModel();
        
        // Create blocks manually to have full control
        
        // 1. Create Constant2 block with value 1
        JSONObject constantJSON = new JSONObject();
        constantJSON.put("blockType", "Constant");
        constantJSON.put("blockName", "Constant2");
        constantJSON.put("blockPath", "");
        constantJSON.put("blockUUID", "constant-uuid");
        JSONObject constantParams = new JSONObject();
        constantParams.put("Value", "1");
        constantJSON.put("paramValues", constantParams);
        
        Constant constant2 = new Constant(constantJSON, model);
        
        // Verify parameter was set correctly
        System.out.printf("Constant2 created with parameter count: %d%n", constant2.getParameterList().size());
        
        // 2. Create Subsystem1
        JSONObject subsystemJSON = new JSONObject();
        subsystemJSON.put("blockType", "Subsystem");
        subsystemJSON.put("blockName", "Subsystem1");
        subsystemJSON.put("blockPath", "");
        subsystemJSON.put("blockUUID", "subsystem-uuid");
        subsystemJSON.put("paramValues", new JSONObject());
        
        Subsystem subsystem1 = new Subsystem(subsystemJSON, model);
        
        // 3. Create Scope2
        JSONObject scopeJSON = new JSONObject();
        scopeJSON.put("blockType", "Scope");
        scopeJSON.put("blockName", "Scope2");
        scopeJSON.put("blockPath", "");
        scopeJSON.put("blockUUID", "scope-uuid");
        scopeJSON.put("paramValues", new JSONObject());
        
        Scope scope2 = new Scope(scopeJSON, model);
        
        // 4. Create In1 block inside subsystem
        In in1 = In.create("In1", "/Subsystem1", 1, model);
        subsystem1.addIn(in1);
        
        // 5. Create Out1 block inside subsystem
        Out out1 = Out.create("Out1", "/Subsystem1", 1, model);
        subsystem1.addOut(out1);
        
        System.out.println("=== CREATED ALL BLOCKS ===");
        System.out.printf("Constant2: %s%n", constant2.getBlockName());
        System.out.printf("Subsystem1: %s (inputs: %d, outputs: %d)%n", 
            subsystem1.getBlockName(), subsystem1.getInputPortList().size(), subsystem1.getOutputPortList().size());
        System.out.printf("Scope2: %s%n", scope2.getBlockName());
        System.out.printf("In1: %s%n", in1.getBlockName());
        System.out.printf("Out1: %s%n", out1.getBlockName());
        
        // Create lines manually using the actual line creation methods
        List<Block> blockList = new ArrayList<>();
        blockList.add(constant2);
        blockList.add(subsystem1);
        blockList.add(scope2);
        blockList.add(in1);
        blockList.add(out1);
        
        // Create line: Constant2 -> Subsystem1
        JSONObject line1JSON = new JSONObject();
        line1JSON.put("fromBlockName", "Constant2");
        line1JSON.put("fromPortNo", 1);
        line1JSON.put("toBlockName", "Subsystem1");
        line1JSON.put("toPortNo", 1);
        
        Line line1 = Line.createLine(line1JSON, blockList);
        System.out.printf("Created line1: %s%n", (line1 != null ? "SUCCESS" : "FAILED"));
        if (line1 != null && line1.getLinkedOutputPort() != null && line1.getLinkedInputPort() != null) {
            System.out.printf("Line1 connects: %s.%d -> %s.%d%n", 
                line1.getLinkedOutputPort().getBlock().getBlockName(), line1.getLinkedOutputPort().getNumber(),
                line1.getLinkedInputPort().getBlock().getBlockName(), line1.getLinkedInputPort().getNumber());
        }
        
        // Create line inside subsystem: In1 -> Out1
        JSONObject line2JSON = new JSONObject();
        line2JSON.put("fromBlockName", "In1");
        line2JSON.put("fromPortNo", 1);
        line2JSON.put("toBlockName", "Out1");
        line2JSON.put("toPortNo", 1);
        
        Line line2 = Line.createLine(line2JSON, blockList);
        System.out.printf("Created line2: %s%n", (line2 != null ? "SUCCESS" : "FAILED"));
        if (line2 != null && line2.getLinkedOutputPort() != null && line2.getLinkedInputPort() != null) {
            System.out.printf("Line2 connects: %s.%d -> %s.%d%n", 
                line2.getLinkedOutputPort().getBlock().getBlockName(), line2.getLinkedOutputPort().getNumber(),
                line2.getLinkedInputPort().getBlock().getBlockName(), line2.getLinkedInputPort().getNumber());
        }
        
        // Create line: Subsystem1 -> Scope2
        JSONObject line3JSON = new JSONObject();
        line3JSON.put("fromBlockName", "Subsystem1");
        line3JSON.put("fromPortNo", 1);
        line3JSON.put("toBlockName", "Scope2");
        line3JSON.put("toPortNo", 1);
        
        Line line3 = Line.createLine(line3JSON, blockList);
        System.out.printf("Created line3: %s%n", (line3 != null ? "SUCCESS" : "FAILED"));
        if (line3 != null && line3.getLinkedOutputPort() != null && line3.getLinkedInputPort() != null) {
            System.out.printf("Line3 connects: %s.%d -> %s.%d%n", 
                line3.getLinkedOutputPort().getBlock().getBlockName(), line3.getLinkedOutputPort().getNumber(),
                line3.getLinkedInputPort().getBlock().getBlockName(), line3.getLinkedInputPort().getNumber());
        }
        
        System.out.println("=== CREATING OUTPUT SIGNALS ===");
        
        // Create output signals for all blocks first (required before calculateInit)
        constant2.updateBlock();
        subsystem1.updateBlock(); 
        scope2.updateBlock();
        in1.updateBlock();
        out1.updateBlock();
        
        System.out.println("=== INITIALIZING ALL BLOCKS ===");
        
        // Initialize blocks in proper order
        constant2.calculateInit();
        subsystem1.calculateInit();
        scope2.calculateInit();
        
        System.out.println("=== EXECUTING SIMULATION STEP ===");
        
        // Execute blocks in proper order
        constant2.calculateOutput(0.1);
        subsystem1.calculateOutput(0.1);
        scope2.calculateOutput(0.1);
        
        System.out.println("=== VERIFYING DATA FLOW ===");
        
        // Check constant output
        double constantValue = constant2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        System.out.printf("Constant2 output: %.6f%n", constantValue);
        assertEquals("Constant2 should output 1.0", 1.0, constantValue, 0.001);
        
        // Check subsystem input port
        if (!subsystem1.getInputPortList().isEmpty() && subsystem1.getInputPortList().get(0).getLinkedLine() != null) {
            double subsystemInput = subsystem1.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getData().getInitValue();
            System.out.printf("Subsystem1 input: %.6f%n", subsystemInput);
        }
        
        // Check In1 block output
        double in1Output = in1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue();
        System.out.printf("In1 output: %.6f%n", in1Output);
        
        // This should be 1.0, not 0.0 - this is the critical test
        assertEquals("In1 should receive the value 1 from Constant2", 1.0, in1Output, 0.001);
        
        System.out.println("=== TEST COMPLETED ===");
    }
}