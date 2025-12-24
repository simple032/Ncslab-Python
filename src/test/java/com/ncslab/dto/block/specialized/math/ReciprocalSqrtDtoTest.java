package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for ReciprocalSqrtDto
 *
 * Tests validation, serialization/deserialization, parameter mapping,
 * and integration with Block classes.
 *
 * Coverage:
 * - Validation tests for function parameter and boundary conditions
 * - JSON serialization and deserialization accuracy
 * - Round-trip consistency (DTO → JSON → DTO)
 * - Parameter type conversions and default value handling
 * - paramValues map integration and legacy compatibility
 * - Utility method correctness (isReciprocalSqrt, isForwardSqrt)
 * - Edge case handling
 *
 * @author NCSLab Test Automation
 * @version 1.0
 * @since 2025
 */
public class ReciprocalSqrtDtoTest {

    private static final double DELTA = 1e-10;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ============================================================================
    // VALIDATION TESTS
    // ============================================================================

    @Test
    public void testValidation_AllValidParameters() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("RSqrt1")
                .function(TypedParameter.of("rsqrt"))
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .saturateOnIntegerOverflow(TypedParameter.of(false))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("DTO should be valid with correct parameters", result.isValid());
    }

    @Test
    public void testValidation_RsqrtFunction() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of("rsqrt"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("rsqrt function should be valid", result.isValid());
    }

    @Test
    public void testValidation_SqrtFunction() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of("sqrt"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("sqrt function should be valid", result.isValid());
    }

    @Test
    public void testValidation_InvalidFunction() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of("cbrt"))  // Invalid
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Invalid function should fail validation", result.isValid());
        assertTrue("Should have function error",
                   result.getErrors().stream().anyMatch(e -> e.contains("Function") &&
                   (e.contains("rsqrt") || e.contains("sqrt"))));
    }

    @Test
    public void testValidation_InheritedSampleTime() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(-1.0))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Inherited sample time (-1) should be valid", result.isValid());
    }

    @Test
    public void testValidation_PositiveSampleTime() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.01))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Positive sample time should be valid", result.isValid());
    }

    @Test
    public void testValidation_NegativeSampleTime() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(-2.0))  // Invalid
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Negative sample time (not -1) should fail validation", result.isValid());
    }

    @Test
    public void testValidation_NullParameters() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(null)
                .sampleTime(null)
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Null parameters should use defaults and be valid", result.isValid());
    }

    // ============================================================================
    // SERIALIZATION TESTS
    // ============================================================================

    @Test
    public void testSerialization_ToJson() throws Exception {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(42)
                .blockName("TestRSqrt")
                .function(TypedParameter.of("rsqrt"))
                .sampleTime(TypedParameter.of(0.01))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain block name", json.contains("TestRSqrt"));
    }

    @Test
    public void testDeserialization_FromJson() throws Exception {
        String json = "{\"blockId\":42,\"blockName\":\"TestRSqrt\"," +
                      "\"blockType\":\"ReciprocalSqrt\"," +
                      "\"paramValues\":{\"Function\":\"rsqrt\"," +
                      "\"SampleTime\":0.01,\"OutDataTypeStr\":\"double\"}}";

        ReciprocalSqrtDto dto = objectMapper.readValue(json, ReciprocalSqrtDto.class);

        assertNotNull("Deserialized DTO should not be null", dto);
        assertEquals("Block ID should match", 42, dto.getBlockId().intValue());
        assertEquals("Function should match", "rsqrt", dto.getFunctionValue());
    }

    @Test
    public void testSerialization_RoundTrip() throws Exception {
        ReciprocalSqrtDto original = ReciprocalSqrtDto.builder()
                .blockId(100)
                .blockName("RoundTripTest")
                .function(TypedParameter.of("sqrt"))
                .sampleTime(TypedParameter.of(-1.0))
                .build();

        String json = objectMapper.writeValueAsString(original);
        ReciprocalSqrtDto restored = objectMapper.readValue(json, ReciprocalSqrtDto.class);

        assertNotNull("Restored DTO should not be null", restored);
        assertEquals("Block ID should be preserved", original.getBlockId(), restored.getBlockId());
        assertEquals("Function should be preserved", original.getFunctionValue(), restored.getFunctionValue());
    }

    // ============================================================================
    // PARAMETER ACCESS TESTS
    // ============================================================================

    @Test
    public void testParameterAccess_DefaultValues() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .build();

        assertEquals("Default function should be rsqrt", "rsqrt", dto.getFunctionValue());
        assertEquals("Default sample time should be -1", -1.0, dto.getSampleTimeValue(), DELTA);
        assertFalse("Default saturate should be false", dto.getSaturateOnIntegerOverflowValue());
    }

    @Test
    public void testParameterAccess_CustomValues() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of("sqrt"))
                .sampleTime(TypedParameter.of(0.05))
                .saturateOnIntegerOverflow(TypedParameter.of(true))
                .build();

        assertEquals("Function should match", "sqrt", dto.getFunctionValue());
        assertEquals("Sample time should match", 0.05, dto.getSampleTimeValue(), DELTA);
        assertTrue("Saturate should match", dto.getSaturateOnIntegerOverflowValue());
    }

    // ============================================================================
    // UTILITY METHOD TESTS
    // ============================================================================

    @Test
    public void testUtility_IsReciprocalSqrt() {
        ReciprocalSqrtDto rsqrtDto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of("rsqrt"))
                .build();

        ReciprocalSqrtDto sqrtDto = ReciprocalSqrtDto.builder()
                .blockId(2)
                .blockName("Test2")
                .function(TypedParameter.of("sqrt"))
                .build();

        assertTrue("Should detect rsqrt", rsqrtDto.isReciprocalSqrt());
        assertFalse("Should not detect rsqrt for sqrt", sqrtDto.isReciprocalSqrt());
    }

    @Test
    public void testUtility_IsForwardSqrt() {
        ReciprocalSqrtDto sqrtDto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of("sqrt"))
                .build();

        ReciprocalSqrtDto rsqrtDto = ReciprocalSqrtDto.builder()
                .blockId(2)
                .blockName("Test2")
                .function(TypedParameter.of("rsqrt"))
                .build();

        assertTrue("Should detect sqrt", sqrtDto.isForwardSqrt());
        assertFalse("Should not detect sqrt for rsqrt", rsqrtDto.isForwardSqrt());
    }

    @Test
    public void testUtility_IsContinuous() {
        ReciprocalSqrtDto continuousDto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.0))
                .build();

        assertTrue("Should detect continuous time (0.0)", continuousDto.isContinuous());
    }

    @Test
    public void testUtility_IsInherited() {
        ReciprocalSqrtDto inheritedDto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(-1.0))
                .build();

        assertTrue("Should detect inherited sample time (-1.0)", inheritedDto.isInherited());
    }

    // ============================================================================
    // COPY METHOD TESTS
    // ============================================================================

    @Test
    public void testCopy_CreatesIndependentCopy() {
        ReciprocalSqrtDto original = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Original")
                .function(TypedParameter.of("rsqrt"))
                .sampleTime(TypedParameter.of(0.01))
                .build();

        ReciprocalSqrtDto copy = original.copy();

        assertNotNull("Copy should not be null", copy);
        assertEquals("Block ID should match", original.getBlockId(), copy.getBlockId());
        assertEquals("Function should match", original.getFunctionValue(), copy.getFunctionValue());
        assertNotSame("Copy should be different instance", original, copy);
    }

    // ============================================================================
    // PARAMETER MAP TESTS
    // ============================================================================

    @Test
    public void testParameterMap_Conversion() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of("rsqrt"))
                .sampleTime(TypedParameter.of(0.01))
                .build();

        var paramMap = dto.toParameterMap();

        assertNotNull("Parameter map should not be null", paramMap);
        assertNotNull("Should contain Function", paramMap.get("Function"));
        assertNotNull("Should contain SampleTime", paramMap.get("SampleTime"));
    }

    // ============================================================================
    // EDGE CASE TESTS
    // ============================================================================

    @Test
    public void testEdgeCase_CaseSensitivity() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of("RSQRT"))  // Wrong case
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Case-incorrect function should fail validation", result.isValid());
    }

    @Test
    public void testEdgeCase_EmptyFunction() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(1)
                .blockName("Test")
                .function(TypedParameter.of(""))
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Empty function should fail validation", result.isValid());
    }

    @Test
    public void testToString_Output() {
        ReciprocalSqrtDto dto = ReciprocalSqrtDto.builder()
                .blockId(42)
                .blockName("TestBlock")
                .function(TypedParameter.of("rsqrt"))
                .build();

        String str = dto.toString();

        assertNotNull("toString should not return null", str);
        assertTrue("toString should contain block ID", str.contains("42"));
        assertTrue("toString should contain function", str.contains("rsqrt"));
    }
}
