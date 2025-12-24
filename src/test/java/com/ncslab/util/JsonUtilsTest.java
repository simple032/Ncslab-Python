package com.ncslab.util;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.model.LineDto;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.block.specialized.math.SumDto;
import com.ncslab.dto.block.specialized.source.ConstantDto;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class to verify Jackson integration with existing JSON structure.
 * Tests minimal functionality to ensure DTOs work correctly.
 */
public class JsonUtilsTest {
    
    @Test
    public void testBasicJsonParsing() {
        String jsonString = "{}";
        assertTrue("Basic JSON should be valid", JsonUtils.isValidJson(jsonString));
        
        String invalidJson = "{invalid";
        assertFalse("Invalid JSON should be detected", JsonUtils.isValidJson(invalidJson));
    }
    
    @Test
    public void testBlockJsonSerialization() {
        ConstantDto block = new ConstantDto();
        block.setBlockName("Constant1");
        block.setBlockPath("model");
        block.setBlockUUID("uuid-123");
        
        Map<String, Object> params = new HashMap<>();
        params.put("Value", "1");
        block.setParamValues(params);
        
        String json = JsonUtils.toJson(block);
        assertNotNull("Block should serialize to JSON", json);
        assertTrue("JSON should contain blockType", json.contains("Constant"));
        assertTrue("JSON should contain blockName", json.contains("Constant1"));
    }
    
    @Test
    public void testBlockJsonDeserialization() {
        String json = "{"
                + "\"blockType\": \"Sum\","
                + "\"blockName\": \"Sum1\","
                + "\"blockPath\": \"model/Subsystem1\","
                + "\"blockUUID\": \"uuid-456\","
                + "\"paramValues\": {\"Inputs\": \"+|++\"}"
                + "}";
        
        BlockDto block = JsonUtils.parseJson(json, BlockDto.class);
        assertNotNull("Block should deserialize from JSON", block);
        assertEquals("Block type should match", "Sum", block.getBlockType());
        assertEquals("Block name should match", "Sum1", block.getBlockName());
        assertEquals("Block path should match", "model/Subsystem1", block.getBlockPath());
        assertEquals("UUID should match", "uuid-456", block.getBlockUUID());
        assertNotNull("Param values should not be null", block.getParamValues());
        assertEquals("Inputs param should match", "+|++", block.getParamValues().get("Inputs"));
    }
    
    @Test
    public void testLineJsonSerialization() {
        LineDto line = new LineDto();
        line.setFromBlockName("Constant1");
        line.setFromPortNo("1");
        line.setToBlockName("Sum1");
        line.setToPortNo("1");
        line.setLinePath("model/Subsystem1");
        line.setFromBlockUUID("uuid-123");
        line.setToBlockUUID("uuid-456");
        
        String json = JsonUtils.toJson(line);
        assertNotNull("Line should serialize to JSON", json);
        assertTrue("JSON should contain fromBlockName", json.contains("Constant1"));
        assertTrue("JSON should contain toBlockName", json.contains("Sum1"));
    }
    
    @Test
    public void testLineJsonDeserialization() {
        String json = "{"
                + "\"fromBlockName\": \"Step1\","
                + "\"fromPortNo\": \"1\","
                + "\"toBlockName\": \"Scope1\","
                + "\"toPortNo\": \"1\","
                + "\"linePath\": \"model\","
                + "\"fromBlockUUID\": \"uuid-789\","
                + "\"toBlockUUID\": \"uuid-012\""
                + "}";
        
        LineDto line = JsonUtils.parseJson(json, LineDto.class);
        assertNotNull("Line should deserialize from JSON", line);
        assertEquals("From block name should match", "Step1", line.getFromBlockName());
        assertEquals("To block name should match", "Scope1", line.getToBlockName());
        assertEquals("Line path should match", "model", line.getLinePath());
    }
    

    @Test
    public void testIgnoreUnknownProperties() {
        String json = "{"
                + "\"blockType\": \"Gain\","
                + "\"blockName\": \"Gain1\","
                + "\"unknownProperty\": \"should be ignored\","
                + "\"anotherUnknown\": 42"
                + "}";
        
        BlockDto block = JsonUtils.parseJson(json, BlockDto.class);
        assertNotNull("Block should deserialize despite unknown properties", block);
        assertEquals("Block type should match", "Gain", block.getBlockType());
        assertEquals("Block name should match", "Gain1", block.getBlockName());
    }
    
}