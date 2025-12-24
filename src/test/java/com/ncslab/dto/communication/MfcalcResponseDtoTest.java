package com.ncslab.dto.communication;

import org.junit.Test;

import com.ncslab.dto.ui.FiguresData;
import com.utils.ResourceReader;

import org.junit.Before;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive test suite for MfcalcResponseDto parsing and handling
 * Tests various response scenarios from MFCalc server
 */
public class MfcalcResponseDtoTest {
    
    private String validJsonWithFigures;
    private String malformedJsonWithCorruptedEscape;
    private String plainNumberResponse;
    private String emptyResponse;
    private String nullResponse;
    private String nonJsonResponse;

    String filePath = "com/ncslab/dto/communication/jsonWithFigures.json";

    @Before
    public void setUp() {

        try {
            validJsonWithFigures = ResourceReader.readResourceAsString(filePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        plainNumberResponse = "26772";
        emptyResponse = "";
        nullResponse = null;
        nonJsonResponse = "Some random text that is not JSON";
    }
    
    @Test
    public void testValidJsonWithFigures() {
        MfcalcResponseDto dto = MfcalcResponseDto.fromJsonString(validJsonWithFigures);
        
        assertNotNull("DTO should not be null", dto);
        assertTrue("Should indicate success", dto.isSuccess());
        assertEquals("Message type should match", "run_script_result", dto.getMessageType());
        assertEquals("Status should be success", "success", dto.getStatus());
        
        // Test figures data
        assertNotNull("Figures should not be null", dto.getFigures());
        assertEquals("Figure version should match", "1.0", dto.getFigures().getVersion());
        assertEquals("Figure type should match", "figure_message", dto.getFigures().getType());
        assertEquals("Figure count should be 1", Integer.valueOf(1), dto.getFigures().getFigureCount());
        
        assertNotNull("Figures list should not be null", dto.getFigures().getFigures());
        assertEquals("Should have 1 figure", 1, dto.getFigures().getFigures().size());
        
        FiguresData.Figure figure = dto.getFigures().getFigures().get(0);
        assertNotNull("Figure should not be null", figure);
        assertEquals("Figure ID should be -1", Integer.valueOf(-1), figure.getFigureId());
        
        assertNotNull("Plot should not be null", figure.getPlot());
        assertEquals("Plot title should match", "Plot", figure.getPlot().getTitle());
        assertEquals("X label should match", "X Axis", figure.getPlot().getXlabel());
        assertEquals("Y label should match", "Y Axis", figure.getPlot().getYlabel());
        
        assertNotNull("Lines should not be null", figure.getPlot().getLines());
        assertEquals("Should have 1 line", 1, figure.getPlot().getLines().size());
        
        FiguresData.Figure.Plot.Line line = figure.getPlot().getLines().get(0);
        assertNotNull("Line should not be null", line);
        assertEquals("Point count should be 100", Integer.valueOf(100), line.getPointCount());
        assertEquals("Style should match", "lines", line.getStyle());
        assertNotNull("X data should not be null", line.getXData());
        assertNotNull("Y data should not be null", line.getYData());
        assertEquals("X data should have 100 points", 100, line.getXData().length);
        assertEquals("Y data should have 100 points", 100, line.getYData().length);
    }
    
    @Test
    public void testMalformedJsonWithCorruptedEscape() {
        MfcalcResponseDto dto = MfcalcResponseDto.fromJsonString(malformedJsonWithCorruptedEscape);
        
        assertNotNull("DTO should not be null", dto);
        assertTrue("Should indicate error", dto.isError());
        assertEquals("Status should be error", "error", dto.getStatus());
        assertNotNull("Error message should be present", dto.getError());
        assertTrue("Error should mention malformed JSON", 
                   dto.getError().contains("Malformed JSON") || dto.getError().contains("Failed to parse"));
    }
    
    @Test
    public void testPlainNumberResponse() {
        MfcalcResponseDto dto = MfcalcResponseDto.fromJsonString(plainNumberResponse);
        
        assertNotNull("DTO should not be null", dto);
        assertTrue("Should indicate error", dto.isError());
        assertEquals("Status should be error", "error", dto.getStatus());
        assertNotNull("Error message should be present", dto.getError());
        assertTrue("Error should mention plain number", dto.getError().contains("plain number"));
        assertTrue("Error should contain the number", dto.getError().contains("26772"));
    }
    
    @Test
    public void testEmptyResponse() {
        MfcalcResponseDto dto = MfcalcResponseDto.fromJsonString(emptyResponse);
        
        assertNotNull("DTO should not be null", dto);
        assertTrue("Should indicate error", dto.isError());
        assertEquals("Status should be error", "error", dto.getStatus());
        assertNotNull("Error message should be present", dto.getError());
        assertTrue("Error should mention null or empty", dto.getError().contains("empty"));
    }
    
    @Test
    public void testNullResponse() {
        MfcalcResponseDto dto = MfcalcResponseDto.fromJsonString(nullResponse);
        
        assertNotNull("DTO should not be null", dto);
        assertTrue("Should indicate error", dto.isError());
        assertEquals("Status should be error", "error", dto.getStatus());
        assertNotNull("Error message should be present", dto.getError());
        assertTrue("Error should mention null", dto.getError().contains("Null"));
    }
    
    @Test
    public void testNonJsonResponse() {
        MfcalcResponseDto dto = MfcalcResponseDto.fromJsonString(nonJsonResponse);
        
        assertNotNull("DTO should not be null", dto);
        assertTrue("Should indicate error", dto.isError());
        assertEquals("Status should be error", "error", dto.getStatus());
        assertNotNull("Error message should be present", dto.getError());
        assertTrue("Error should mention invalid format", dto.getError().contains("Invalid response format"));
    }
    
    @Test
    public void testIsSuccessMethod() {
        // Test success case
        MfcalcResponseDto successDto = MfcalcResponseDto.builder()
                .status("success")
                .build();
        assertTrue("Should be success", successDto.isSuccess());
        
        // Test null status case (should be considered success for backward compatibility)
        MfcalcResponseDto nullStatusDto = MfcalcResponseDto.builder()
                .status(null)
                .build();
        assertTrue("Null status should be considered success", nullStatusDto.isSuccess());
        
        // Test error case
        MfcalcResponseDto errorDto = MfcalcResponseDto.builder()
                .status("error")
                .build();
        assertFalse("Should not be success", errorDto.isSuccess());
    }
    
    @Test
    public void testIsErrorMethod() {
        // Test explicit error status
        MfcalcResponseDto errorStatusDto = MfcalcResponseDto.builder()
                .status("error")
                .build();
        assertTrue("Should be error", errorStatusDto.isError());
        
        // Test error message present
        MfcalcResponseDto errorMessageDto = MfcalcResponseDto.builder()
                .error("Some error occurred")
                .build();
        assertTrue("Should be error when error message present", errorMessageDto.isError());
        
        // Test success case
        MfcalcResponseDto successDto = MfcalcResponseDto.builder()
                .status("success")
                .build();
        assertFalse("Should not be error", successDto.isError());
    }
    
    @Test
    public void testGetErrorInfo() {
        // Test with error message
        MfcalcResponseDto dtoWithError = MfcalcResponseDto.builder()
                .error("Custom error message")
                .build();
        assertEquals("Should return error message", "Custom error message", dtoWithError.getErrorInfo());
        
        // Test with error status but no message
        MfcalcResponseDto dtoWithErrorStatus = MfcalcResponseDto.builder()
                .status("error")
                .build();
        assertEquals("Should return unknown error", "Unknown MFCalc error", dtoWithErrorStatus.getErrorInfo());
        
        // Test success case
        MfcalcResponseDto successDto = MfcalcResponseDto.builder()
                .status("success")
                .build();
        assertNull("Should return null for success", successDto.getErrorInfo());
    }
    
    @Test
    public void testCreateScriptSuccess() {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        
        MfcalcResponseDto dto = MfcalcResponseDto.createScriptSuccess(
                "run_script", "msg123", result, "Console output");
        
        assertNotNull("DTO should not be null", dto);
        assertEquals("Message type should match", "run_script", dto.getMessageType());
        assertEquals("Message ID should match", "msg123", dto.getMessageId());
        assertEquals("Status should be success", "success", dto.getStatus());
        assertEquals("Output should match", "Console output", dto.getOutput());
        assertTrue("Should indicate success", dto.isSuccess());
    }
    
    @Test
    public void testCreateError() {
        MfcalcResponseDto dto = MfcalcResponseDto.createError(
                "run_script", "msg456", "Test error message");
        
        assertNotNull("DTO should not be null", dto);
        assertEquals("Message type should match", "run_script", dto.getMessageType());
        assertEquals("Message ID should match", "msg456", dto.getMessageId());
        assertEquals("Status should be error", "error", dto.getStatus());
        assertEquals("Error should match", "Test error message", dto.getError());
        assertTrue("Should indicate error", dto.isError());
    }
}