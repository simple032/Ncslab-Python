package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for DotProductDto
 *
 * Tests validation, serialization/deserialization, parameter mapping,
 * and integration with Block classes.
 *
 * Coverage:
 * - Validation tests for all parameters and boundary conditions
 * - JSON serialization and deserialization accuracy
 * - Round-trip consistency (DTO → JSON → DTO)
 * - Parameter type conversions and default value handling
 * - paramValues map integration and legacy compatibility
 * - Utility method correctness
 * - Edge case handling
 *
 * @author NCSLab Test Automation
 * @version 1.0
 * @since 2025
 */
public class DotProductDtoTest {

    private static final double DELTA = 1e-10;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ============================================================================
    // VALIDATION TESTS
    // ============================================================================

    /**
     * Test validation with all valid parameters.
     * This validates the happy path where all parameters are correctly specified.
     */
    @Test
    public void testValidation_AllValidParameters() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("DotProd1")
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as first input"))
                .saturateOnIntegerOverflow(TypedParameter.of(false))
                .build();

        ValidationResult result = dto.validate();

        assertNotNull("Validation result should not be null", result);
        assertTrue("DTO should be valid with correct parameters", result.isValid());
        assertEquals("Should have no errors", 0, result.getErrors().size());
    }

    /**
     * Test validation with inherited sample time.
     * Validates that -1 (inherited) is a valid sample time.
     */
    @Test
    public void testValidation_InheritedSampleTime() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(-1.0))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Inherited sample time (-1) should be valid", result.isValid());
    }

    /**
     * Test validation with continuous sample time.
     * Validates that 0 (continuous) is a valid sample time.
     */
    @Test
    public void testValidation_ContinuousSampleTime() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.0))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Continuous sample time (0) should be valid", result.isValid());
    }

    /**
     * Test validation with positive discrete sample time.
     * Validates that positive sample times are valid for discrete operation.
     */
    @Test
    public void testValidation_PositiveSampleTime() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.01))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Positive sample time should be valid", result.isValid());
    }

    /**
     * Test validation with negative sample time (not -1).
     * Validates that negative sample times other than -1 are rejected.
     */
    @Test
    public void testValidation_NegativeSampleTime() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(-2.0))  // Invalid
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Negative sample time (not -1) should fail validation", result.isValid());
        assertTrue("Should have sample time error",
                   result.getErrors().stream().anyMatch(e -> e.contains("Sample time")));
    }

    /**
     * Test validation with zero sample time (boundary case).
     * Validates that exactly 0 is valid (continuous time).
     */
    @Test
    public void testValidation_ZeroSampleTime() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.0))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Zero sample time (continuous) should be valid", result.isValid());
    }

    /**
     * Test validation with null parameters.
     * Validates that missing parameters use defaults and don't cause validation failure.
     */
    @Test
    public void testValidation_NullParameters() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(null)
                .outDataTypeStr(null)
                .saturateOnIntegerOverflow(null)
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Null parameters should use defaults and be valid", result.isValid());
    }

    // ============================================================================
    // SERIALIZATION TESTS
    // ============================================================================

    /**
     * Test JSON serialization of DotProductDto.
     * Validates that DTO can be converted to JSON string.
     */
    @Test
    public void testSerialization_ToJson() throws Exception {
        DotProductDto dto = DotProductDto.builder()
                .blockId(42)
                .blockName("TestDotProduct")
                .sampleTime(TypedParameter.of(0.01))
                .outDataTypeStr(TypedParameter.of("double"))
                .saturateOnIntegerOverflow(TypedParameter.of(true))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain block name", json.contains("TestDotProduct"));
        assertTrue("JSON should contain block type", json.contains("DotProduct"));
    }

    /**
     * Test JSON deserialization to DotProductDto.
     * Validates that JSON can be converted back to DTO.
     */
    @Test
    public void testDeserialization_FromJson() throws Exception {
        String json = "{\"blockId\":42,\"blockName\":\"TestDotProduct\"," +
                      "\"blockType\":\"DotProduct\"," +
                      "\"paramValues\":{\"SampleTime\":0.01," +
                      "\"OutDataTypeStr\":\"double\"," +
                      "\"SaturateOnIntegerOverflow\":true}}";

        DotProductDto dto = objectMapper.readValue(json, DotProductDto.class);

        assertNotNull("Deserialized DTO should not be null", dto);
        assertEquals("Block ID should match", 42, dto.getBlockId().intValue());
        assertEquals("Block name should match", "TestDotProduct", dto.getBlockName());
        assertEquals("Sample time should match", 0.01, dto.getSampleTimeValue(), DELTA);
        assertEquals("Output data type should match", "double", dto.getOutDataTypeStrValue());
        assertTrue("Saturate on overflow should match", dto.getSaturateOnIntegerOverflowValue());
    }

    /**
     * Test round-trip consistency (DTO → JSON → DTO).
     * Validates that serialization and deserialization preserve data integrity.
     */
    @Test
    public void testSerialization_RoundTrip() throws Exception {
        DotProductDto original = DotProductDto.builder()
                .blockId(100)
                .blockName("RoundTripTest")
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as first input"))
                .saturateOnIntegerOverflow(TypedParameter.of(false))
                .build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(original);

        // Deserialize back to DTO
        DotProductDto restored = objectMapper.readValue(json, DotProductDto.class);

        assertNotNull("Restored DTO should not be null", restored);
        assertEquals("Block ID should be preserved", original.getBlockId(), restored.getBlockId());
        assertEquals("Block name should be preserved", original.getBlockName(), restored.getBlockName());
        assertEquals("Sample time should be preserved", original.getSampleTimeValue(), restored.getSampleTimeValue(), DELTA);
        assertEquals("Output data type should be preserved", original.getOutDataTypeStrValue(), restored.getOutDataTypeStrValue());
    }

    /**
     * Test paramValues map handling during deserialization.
     * Validates that legacy paramValues map is correctly parsed into TypedParameters.
     */
    @Test
    public void testSerialization_ParamValuesMap() throws Exception {
        String json = "{\"blockId\":1,\"blockName\":\"Test\"," +
                      "\"blockType\":\"DotProduct\"," +
                      "\"paramValues\":{\"SampleTime\":0.1," +
                      "\"OutDataTypeStr\":\"single\"," +
                      "\"SaturateOnIntegerOverflow\":false}}";

        DotProductDto dto = objectMapper.readValue(json, DotProductDto.class);

        assertNotNull("DTO should not be null", dto);
        assertNotNull("SampleTime parameter should be populated", dto.getSampleTime());
        assertNotNull("OutDataTypeStr parameter should be populated", dto.getOutDataTypeStr());
        assertNotNull("SaturateOnIntegerOverflow parameter should be populated", dto.getSaturateOnIntegerOverflow());

        assertEquals("SampleTime value should match", 0.1, dto.getSampleTimeValue(), DELTA);
        assertEquals("OutDataTypeStr value should match", "single", dto.getOutDataTypeStrValue());
        assertFalse("SaturateOnIntegerOverflow value should match", dto.getSaturateOnIntegerOverflowValue());
    }

    // ============================================================================
    // PARAMETER ACCESS TESTS
    // ============================================================================

    /**
     * Test default parameter values when not specified.
     * Validates that proper defaults are returned for missing parameters.
     */
    @Test
    public void testParameterAccess_DefaultValues() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .build();

        assertEquals("Default sample time should be -1", -1.0, dto.getSampleTimeValue(), DELTA);
        assertEquals("Default output data type should be inherit",
                     "Inherit: Same as first input", dto.getOutDataTypeStrValue());
        assertFalse("Default saturate on overflow should be false", dto.getSaturateOnIntegerOverflowValue());
    }

    /**
     * Test custom parameter values.
     * Validates that custom values are correctly stored and retrieved.
     */
    @Test
    public void testParameterAccess_CustomValues() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.05))
                .outDataTypeStr(TypedParameter.of("single"))
                .saturateOnIntegerOverflow(TypedParameter.of(true))
                .build();

        assertEquals("Sample time should match", 0.05, dto.getSampleTimeValue(), DELTA);
        assertEquals("Output data type should match", "single", dto.getOutDataTypeStrValue());
        assertTrue("Saturate on overflow should match", dto.getSaturateOnIntegerOverflowValue());
    }

    // ============================================================================
    // UTILITY METHOD TESTS
    // ============================================================================

    /**
     * Test isContinuous() utility method.
     * Validates correct detection of continuous time operation.
     */
    @Test
    public void testUtility_IsContinuous() {
        DotProductDto continuousDto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.0))
                .build();

        DotProductDto discreteDto = DotProductDto.builder()
                .blockId(2)
                .blockName("Test2")
                .sampleTime(TypedParameter.of(0.01))
                .build();

        assertTrue("Should detect continuous time (0.0)", continuousDto.isContinuous());
        assertFalse("Should not detect continuous for discrete", discreteDto.isContinuous());
    }

    /**
     * Test isInherited() utility method.
     * Validates correct detection of inherited sample time.
     */
    @Test
    public void testUtility_IsInherited() {
        DotProductDto inheritedDto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(-1.0))
                .build();

        DotProductDto discreteDto = DotProductDto.builder()
                .blockId(2)
                .blockName("Test2")
                .sampleTime(TypedParameter.of(0.01))
                .build();

        assertTrue("Should detect inherited sample time (-1.0)", inheritedDto.isInherited());
        assertFalse("Should not detect inherited for discrete", discreteDto.isInherited());
    }

    // ============================================================================
    // COPY METHOD TESTS
    // ============================================================================

    /**
     * Test copy() method creates independent copy.
     * Validates that copied DTO is independent of original.
     */
    @Test
    public void testCopy_CreatesIndependentCopy() {
        DotProductDto original = DotProductDto.builder()
                .blockId(1)
                .blockName("Original")
                .sampleTime(TypedParameter.of(0.01))
                .outDataTypeStr(TypedParameter.of("double"))
                .build();

        DotProductDto copy = original.copy();

        assertNotNull("Copy should not be null", copy);
        assertEquals("Block ID should match", original.getBlockId(), copy.getBlockId());
        assertEquals("Block name should match", original.getBlockName(), copy.getBlockName());
        assertEquals("Sample time should match", original.getSampleTimeValue(), copy.getSampleTimeValue(), DELTA);
        assertEquals("Output data type should match", original.getOutDataTypeStrValue(), copy.getOutDataTypeStrValue());

        // Verify independence
        assertNotSame("Copy should be different instance", original, copy);
    }

    // ============================================================================
    // PARAMETER MAP TESTS
    // ============================================================================

    /**
     * Test toParameterMap() conversion.
     * Validates that DTO parameters are correctly converted to parameter map.
     */
    @Test
    public void testParameterMap_Conversion() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.01))
                .outDataTypeStr(TypedParameter.of("double"))
                .saturateOnIntegerOverflow(TypedParameter.of(true))
                .build();

        var paramMap = dto.toParameterMap();

        assertNotNull("Parameter map should not be null", paramMap);
        assertNotNull("Should contain SampleTime", paramMap.get("SampleTime"));
        assertNotNull("Should contain OutDataTypeStr", paramMap.get("OutDataTypeStr"));
        assertNotNull("Should contain SaturateOnIntegerOverflow", paramMap.get("SaturateOnIntegerOverflow"));
    }

    // ============================================================================
    // EDGE CASE TESTS
    // ============================================================================

    /**
     * Test very small positive sample time.
     * Validates that very small positive values are accepted.
     */
    @Test
    public void testEdgeCase_VerySmallSampleTime() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(1e-10))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Very small positive sample time should be valid", result.isValid());
    }

    /**
     * Test very large sample time.
     * Validates that large sample times are accepted.
     */
    @Test
    public void testEdgeCase_VeryLargeSampleTime() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(1e10))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Very large sample time should be valid", result.isValid());
    }

    /**
     * Test toString() method output.
     * Validates that toString provides meaningful representation.
     */
    @Test
    public void testToString_Output() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(42)
                .blockName("TestBlock")
                .sampleTime(TypedParameter.of(0.01))
                .build();

        String str = dto.toString();

        assertNotNull("toString should not return null", str);
        assertTrue("toString should contain block ID", str.contains("42"));
        assertTrue("toString should contain block name", str.contains("TestBlock"));
        assertTrue("toString should contain sample time", str.contains("0.01"));
    }

    /**
     * Test behavior with multiple null parameters.
     * Validates that multiple null parameters are handled gracefully.
     */
    @Test
    public void testEdgeCase_MultipleNullParameters() {
        DotProductDto dto = DotProductDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(null)
                .outDataTypeStr(null)
                .saturateOnIntegerOverflow(null)
                .build();

        // Should use defaults
        assertEquals("Null sample time should use default", -1.0, dto.getSampleTimeValue(), DELTA);
        assertNotNull("Null output data type should use default", dto.getOutDataTypeStrValue());
        assertNotNull("Null saturate should use default", dto.getSaturateOnIntegerOverflowValue());
    }
}
