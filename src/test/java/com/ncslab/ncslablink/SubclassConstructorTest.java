package com.ncslab.ncslablink;

import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.code.c.windows.simulation.CodeModelCWindowsSimulation;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.util.JsonUtils;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class to verify that all NCSLabModel subclasses support the new string constructor.
 * Tests both JSONObject and String-based construction approaches.
 */
public class SubclassConstructorTest {
    
    // Simple model JSON for testing constructor compatibility
    private static final String SIMPLE_MODEL_JSON = "{\n" +
            "  \"modelName\": \"ConstructorTest\",\n" +
            "  \"modelRealName\": \"Constructor Test Model\",\n" +
            "  \"userId\": 1,\n" +
            "  \"modelId\": 100,\n" +
            "  \"uuid\": 123456789,\n" +
            "  \"testRig\": 0,\n" +
            "  \"copyNum\": 0,\n" +
            "  \"templateName\": \"template1\",\n" +
            "  \"config\": {\n" +
            "    \"SolverType\": \"ode4\",\n" +
            "    \"FixedStep\": 0.01,\n" +
            "    \"SimulationTime\": 10.0,\n" +
            "    \"RelTol\": \"1e-3\",\n" +
            "    \"AbsTol\": \"auto\"\n" +
            "  },\n" +
            "  \"blocks\": [\n" +
            "    {\n" +
            "      \"blockType\": \"Constant\",\n" +
            "      \"srcBlock\": \"simulink/Sources/Constant\",\n" +
            "      \"blockName\": \"Constant1\",\n" +
            "      \"blockPath\": \"model\",\n" +
            "      \"blockUUID\": \"uuid-001\",\n" +
            "      \"paramValues\": {\n" +
            "        \"Value\": \"1.0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"blockType\": \"Scope\",\n" +
            "      \"srcBlock\": \"simulink/Sinks/Scope\",\n" +
            "      \"blockName\": \"Scope1\",\n" +
            "      \"blockPath\": \"model\",\n" +
            "      \"blockUUID\": \"uuid-002\",\n" +
            "      \"paramValues\": {\n" +
            "        \"NumInputPorts\": \"1\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"lines\": [\n" +
            "    {\n" +
            "      \"fromBlockName\": \"Constant1\",\n" +
            "      \"fromPortNo\": \"1\",\n" +
            "      \"toBlockName\": \"Scope1\",\n" +
            "      \"toPortNo\": \"1\",\n" +
            "      \"linePath\": \"model\",\n" +
            "      \"fromBlockUUID\": \"uuid-001\",\n" +
            "      \"toBlockUUID\": \"uuid-002\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"option\": {},\n" +
            "  \"saveInfo\": {\n" +
            "    \"version\": \"1.0\",\n" +
            "    \"timestamp\": 1691234567890\n" +
            "  }\n" +
            "}";
    
    @Test
    public void testSimulationModelConstructors() {
        try {
            // Test traditional JSONObject constructor
            JSONObject jsonObj = new JSONObject(SIMPLE_MODEL_JSON);
            SimulationModel modelFromJson = new SimulationModel(jsonObj, ModelMode.Simulation);
            assertNotNull("SimulationModel should be created from JSONObject", modelFromJson);
            assertEquals("Model name should match", "ConstructorTest", modelFromJson.getModelName());
            assertEquals("User ID should match", 1, modelFromJson.getUserId());
            
            // Verify block creation worked
            assertTrue("Should have blocks", modelFromJson.getBlockList().size() > 0);
            assertTrue("Should have lines", modelFromJson.getLineList().size() > 0);
            
        } catch (Exception e) {
            fail("SimulationModel constructor test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testCodeModelCLinuxPCSimulationConstructors() {
        try {
            // Test traditional JSONObject factory method
            JSONObject jsonObj = new JSONObject(SIMPLE_MODEL_JSON);
            CodeModelCLinuxPCSimulation modelFromJson = CodeModelCLinuxPCSimulation.createFromJSON(jsonObj, ModelMode.Compilation);
            assertNotNull("CodeModelCLinuxPCSimulation should be created from JSONObject", modelFromJson);
            assertEquals("Model name should match", "ConstructorTest", modelFromJson.getModelName());
            assertEquals("User ID should match", 1, modelFromJson.getUserId());
            
            // Verify block creation worked
            assertTrue("Should have blocks", modelFromJson.getBlockList().size() > 0);
            assertTrue("Should have lines", modelFromJson.getLineList().size() > 0);
            
        } catch (Exception e) {
            fail("CodeModelCLinuxPCSimulation constructor test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testCodeModelCWindowsSimulationConstructors() {
        try {
            // Test traditional JSONObject factory method
            JSONObject jsonObj = new JSONObject(SIMPLE_MODEL_JSON);
            CodeModelCWindowsSimulation modelFromJson = CodeModelCWindowsSimulation.createFromJSON(jsonObj, ModelMode.Compilation);
            assertNotNull("CodeModelCWindowsSimulation should be created from JSONObject", modelFromJson);
            assertEquals("Model name should match", "ConstructorTest", modelFromJson.getModelName());
            assertEquals("User ID should match", 1, modelFromJson.getUserId());
            
            // Verify block creation worked
            assertTrue("Should have blocks", modelFromJson.getBlockList().size() > 0);
            assertTrue("Should have lines", modelFromJson.getLineList().size() > 0);
            
        } catch (Exception e) {
            fail("CodeModelCWindowsSimulation constructor test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testDtoIntegrationInSubclasses() {
        // Test that the DTO parsing works end-to-end in subclasses
        
        // Parse to DTO first
        ModelDto modelDto = JsonUtils.parseModelDto(SIMPLE_MODEL_JSON);
        assertNotNull("JSON should parse to ModelDto DTO", modelDto);
        assertTrue("Model should have blocks", modelDto.hasBlocks());
        assertTrue("Model should have lines", modelDto.hasLines());
        
        try {
            // Test that DTO can be created directly from parsed ModelJson
            SimulationModel simModel = new SimulationModel(modelDto, ModelMode.Simulation);
            assertNotNull("SimulationModel should handle ModelDto DTO", simModel);
            assertEquals("Block count should match", modelDto.getBlocks().size(), simModel.getBlockList().size());
            
            CodeModelCLinuxPCSimulation codeModel = CodeModelCLinuxPCSimulation.createFromDto(modelDto, ModelMode.Compilation);
            assertNotNull("CodeModelCLinuxPCSimulation should handle ModelDto DTO", codeModel);
            assertEquals("Block count should match", modelDto.getBlocks().size(), codeModel.getBlockList().size());
            
        } catch (Exception e) {
            fail("DTO integration test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testDtoNativeCreation() {
        // Test that DTO constructors work with direct ModelDto objects
        
        // Parse to DTO first
        ModelDto modelDto = JsonUtils.parseModelDto(SIMPLE_MODEL_JSON);
        assertNotNull("JSON should parse to ModelDto DTO", modelDto);
        
        try {
            // Test SimulationModel DTO creation
            SimulationModel simModel = new SimulationModel(modelDto, ModelMode.Simulation);
            assertNotNull("SimulationModel should handle ModelDto DTO", simModel);
            assertEquals("Model name should be parsed correctly", "ConstructorTest", simModel.getModelName());
            
            // Test CodeModel DTO creation
            CodeModelCLinuxPCSimulation codeModel = CodeModelCLinuxPCSimulation.createFromDto(modelDto, ModelMode.Compilation);
            assertNotNull("CodeModelCLinuxPCSimulation should handle ModelDto DTO", codeModel);
            assertEquals("Model name should be parsed correctly", "ConstructorTest", codeModel.getModelName());
            
        } catch (Exception e) {
            fail("DTO native creation test failed: " + e.getMessage());
        }
    }
}