package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for ComplexToMagnitudeAngleDto
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
public class ComplexToMagnitudeAngleDtoTest {

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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("ComplexConverter1")
                .angleUnits(TypedParameter.of("radians"))
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .saturateOnIntegerOverflow(TypedParameter.of(false))
                .build();

        ValidationResult result = dto.validate();

        assertNotNull("Validation result should not be null", result);
        assertTrue("DTO should be valid with correct parameters", result.isValid());
        assertEquals("Should have no errors", 0, result.getErrors().size());
    }

    /**
     * Test validation with radians angle units.
     * Validates that "radians" is accepted as a valid angle unit.
     */
    @Test
    public void testValidation_RadiansAngleUnits() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of("radians"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Radians should be valid", result.isValid());
    }

    /**
     * Test validation with degrees angle units.
     * Validates that "degrees" is accepted as a valid angle unit.
     */
    @Test
    public void testValidation_DegreesAngleUnits() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of("degrees"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Degrees should be valid", result.isValid());
    }

    /**
     * Test validation with invalid angle units.
     * Validates that invalid angle unit strings are rejected.
     */
    @Test
    public void testValidation_InvalidAngleUnits() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of("gradians"))  // Invalid
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Invalid angle units should fail validation", result.isValid());
        assertTrue("Should have angle units error",
                   result.getErrors().stream().anyMatch(e -> e.contains("radians") || e.contains("degrees")));
    }

    /**
     * Test validation with inherited sample time.
     * Validates that -1 (inherited) is a valid sample time.
     */
    @Test
    public void testValidation_InheritedSampleTime() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
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
     * Validates that exactly 0 is NOT rejected (continuous time).
     */
    @Test
    public void testValidation_ZeroSampleTime() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(null)
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
     * Test JSON serialization of ComplexToMagnitudeAngleDto.
     * Validates that DTO can be converted to JSON string.
     */
    @Test
    public void testSerialization_ToJson() throws Exception {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(42)
                .blockName("TestConverter")
                .angleUnits(TypedParameter.of("degrees"))
                .sampleTime(TypedParameter.of(0.01))
                .outDataTypeStr(TypedParameter.of("double"))
                .saturateOnIntegerOverflow(TypedParameter.of(true))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain block name", json.contains("TestConverter"));
        assertTrue("JSON should contain block type", json.contains("ComplexToMagnitudeAngle"));
    }

    /**
     * Test JSON deserialization to ComplexToMagnitudeAngleDto.
     * Validates that JSON can be converted back to DTO.
     */
    @Test
    public void testDeserialization_FromJson() throws Exception {
        String json = "{\"blockId\":42,\"blockName\":\"TestConverter\"," +
                      "\"blockType\":\"ComplexToMagnitudeAngle\"," +
                      "\"paramValues\":{\"AngleUnits\":\"degrees\"," +
                      "\"SampleTime\":0.01,\"OutDataTypeStr\":\"double\"," +
                      "\"SaturateOnIntegerOverflow\":true}}";

        ComplexToMagnitudeAngleDto dto = objectMapper.readValue(json, ComplexToMagnitudeAngleDto.class);

        assertNotNull("Deserialized DTO should not be null", dto);
        assertEquals("Block ID should match", 42, dto.getBlockId().intValue());
        assertEquals("Block name should match", "TestConverter", dto.getBlockName());
        assertEquals("Angle units should match", "degrees", dto.getAngleUnitsValue());
        assertEquals("Sample time should match", 0.01, dto.getSampleTimeValue(), DELTA);
    }

    /**
     * Test round-trip consistency (DTO → JSON → DTO).
     * Validates that serialization and deserialization preserve data integrity.
     */
    @Test
    public void testSerialization_RoundTrip() throws Exception {
        ComplexToMagnitudeAngleDto original = ComplexToMagnitudeAngleDto.builder()
                .blockId(100)
                .blockName("RoundTripTest")
                .angleUnits(TypedParameter.of("radians"))
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .saturateOnIntegerOverflow(TypedParameter.of(false))
                .build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(original);

        // Deserialize back to DTO
        ComplexToMagnitudeAngleDto restored = objectMapper.readValue(json, ComplexToMagnitudeAngleDto.class);

        assertNotNull("Restored DTO should not be null", restored);
        assertEquals("Block ID should be preserved", original.getBlockId(), restored.getBlockId());
        assertEquals("Block name should be preserved", original.getBlockName(), restored.getBlockName());
        assertEquals("Angle units should be preserved", original.getAngleUnitsValue(), restored.getAngleUnitsValue());
        assertEquals("Sample time should be preserved", original.getSampleTimeValue(), restored.getSampleTimeValue(), DELTA);
    }

    /**
     * Test paramValues map handling during deserialization.
     * Validates that legacy paramValues map is correctly parsed into TypedParameters.
     */
    @Test
    public void testSerialization_ParamValuesMap() throws Exception {
        String json = "{\"blockId\":1,\"blockName\":\"Test\"," +
                      "\"blockType\":\"ComplexToMagnitudeAngle\"," +
                      "\"paramValues\":{\"AngleUnits\":\"degrees\"," +
                      "\"SampleTime\":0.1,\"OutDataTypeStr\":\"single\"," +
                      "\"SaturateOnIntegerOverflow\":false}}";

        ComplexToMagnitudeAngleDto dto = objectMapper.readValue(json, ComplexToMagnitudeAngleDto.class);

        assertNotNull("DTO should not be null", dto);
        assertNotNull("AngleUnits parameter should be populated", dto.getAngleUnits());
        assertNotNull("SampleTime parameter should be populated", dto.getSampleTime());
        assertNotNull("OutDataTypeStr parameter should be populated", dto.getOutDataTypeStr());
        assertNotNull("SaturateOnIntegerOverflow parameter should be populated", dto.getSaturateOnIntegerOverflow());

        assertEquals("AngleUnits value should match", "degrees", dto.getAngleUnitsValue());
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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .build();

        assertEquals("Default angle units should be radians", "radians", dto.getAngleUnitsValue());
        assertEquals("Default sample time should be -1", -1.0, dto.getSampleTimeValue(), DELTA);
        assertEquals("Default output data type should be inherit", "Inherit: Same as input", dto.getOutDataTypeStrValue());
        assertFalse("Default saturate on overflow should be false", dto.getSaturateOnIntegerOverflowValue());
    }

    /**
     * Test custom parameter values.
     * Validates that custom values are correctly stored and retrieved.
     */
    @Test
    public void testParameterAccess_CustomValues() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of("degrees"))
                .sampleTime(TypedParameter.of(0.05))
                .outDataTypeStr(TypedParameter.of("single"))
                .saturateOnIntegerOverflow(TypedParameter.of(true))
                .build();

        assertEquals("Angle units should match", "degrees", dto.getAngleUnitsValue());
        assertEquals("Sample time should match", 0.05, dto.getSampleTimeValue(), DELTA);
        assertEquals("Output data type should match", "single", dto.getOutDataTypeStrValue());
        assertTrue("Saturate on overflow should match", dto.getSaturateOnIntegerOverflowValue());
    }

    // ============================================================================
    // UTILITY METHOD TESTS
    // ============================================================================

    /**
     * Test isAngleInDegrees() utility method.
     * Validates correct detection of degrees angle unit.
     */
    @Test
    public void testUtility_IsAngleInDegrees() {
        ComplexToMagnitudeAngleDto degreesDto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of("degrees"))
                .build();

        ComplexToMagnitudeAngleDto radiansDto = ComplexToMagnitudeAngleDto.builder()
                .blockId(2)
                .blockName("Test2")
                .angleUnits(TypedParameter.of("radians"))
                .build();

        assertTrue("Should detect degrees", degreesDto.isAngleInDegrees());
        assertFalse("Should not detect degrees for radians", radiansDto.isAngleInDegrees());
    }

    /**
     * Test isAngleInRadians() utility method.
     * Validates correct detection of radians angle unit.
     */
    @Test
    public void testUtility_IsAngleInRadians() {
        ComplexToMagnitudeAngleDto radiansDto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of("radians"))
                .build();

        ComplexToMagnitudeAngleDto degreesDto = ComplexToMagnitudeAngleDto.builder()
                .blockId(2)
                .blockName("Test2")
                .angleUnits(TypedParameter.of("degrees"))
                .build();

        assertTrue("Should detect radians", radiansDto.isAngleInRadians());
        assertFalse("Should not detect radians for degrees", degreesDto.isAngleInRadians());
    }

    /**
     * Test isContinuous() utility method.
     * Validates correct detection of continuous time operation.
     */
    @Test
    public void testUtility_IsContinuous() {
        ComplexToMagnitudeAngleDto continuousDto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(0.0))
                .build();

        ComplexToMagnitudeAngleDto discreteDto = ComplexToMagnitudeAngleDto.builder()
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
        ComplexToMagnitudeAngleDto inheritedDto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .sampleTime(TypedParameter.of(-1.0))
                .build();

        ComplexToMagnitudeAngleDto discreteDto = ComplexToMagnitudeAngleDto.builder()
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
        ComplexToMagnitudeAngleDto original = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Original")
                .angleUnits(TypedParameter.of("degrees"))
                .sampleTime(TypedParameter.of(0.01))
                .build();

        ComplexToMagnitudeAngleDto copy = original.copy();

        assertNotNull("Copy should not be null", copy);
        assertEquals("Block ID should match", original.getBlockId(), copy.getBlockId());
        assertEquals("Block name should match", original.getBlockName(), copy.getBlockName());
        assertEquals("Angle units should match", original.getAngleUnitsValue(), copy.getAngleUnitsValue());
        assertEquals("Sample time should match", original.getSampleTimeValue(), copy.getSampleTimeValue(), DELTA);

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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of("degrees"))
                .sampleTime(TypedParameter.of(0.01))
                .outDataTypeStr(TypedParameter.of("double"))
                .saturateOnIntegerOverflow(TypedParameter.of(true))
                .build();

        var paramMap = dto.toParameterMap();

        assertNotNull("Parameter map should not be null", paramMap);
        assertNotNull("Should contain AngleUnits", paramMap.get("AngleUnits"));
        assertNotNull("Should contain SampleTime", paramMap.get("SampleTime"));
        assertNotNull("Should contain OutDataTypeStr", paramMap.get("OutDataTypeStr"));
        assertNotNull("Should contain SaturateOnIntegerOverflow", paramMap.get("SaturateOnIntegerOverflow"));
    }

    // ============================================================================
    // EDGE CASE TESTS
    // ============================================================================

    /**
     * Test empty string angle units.
     * Validates handling of empty string values.
     */
    @Test
    public void testEdgeCase_EmptyStringAngleUnits() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of(""))
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Empty string angle units should fail validation", result.isValid());
    }

    /**
     * Test case sensitivity of angle units.
     * Validates that angle units are case-sensitive.
     */
    @Test
    public void testEdgeCase_CaseSensitivityAngleUnits() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(1)
                .blockName("Test")
                .angleUnits(TypedParameter.of("Radians"))  // Wrong case
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Case-incorrect angle units should fail validation", result.isValid());
    }

    /**
     * Test very small positive sample time.
     * Validates that very small positive values are accepted.
     */
    @Test
    public void testEdgeCase_VerySmallSampleTime() {
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
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
        ComplexToMagnitudeAngleDto dto = ComplexToMagnitudeAngleDto.builder()
                .blockId(42)
                .blockName("TestBlock")
                .angleUnits(TypedParameter.of("degrees"))
                .build();

        String str = dto.toString();

        assertNotNull("toString should not return null", str);
        assertTrue("toString should contain block ID", str.contains("42"));
        assertTrue("toString should contain block name", str.contains("TestBlock"));
        assertTrue("toString should contain angle units", str.contains("degrees"));
    }
}
