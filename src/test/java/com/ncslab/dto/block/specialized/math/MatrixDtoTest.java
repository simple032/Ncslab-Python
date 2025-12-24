package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for MatrixDto
 *
 * Tests validation, serialization/deserialization, parameter mapping,
 * and integration with Block classes.
 *
 * Coverage:
 * - Validation tests for matrix format and boundary conditions
 * - JSON serialization and deserialization accuracy
 * - Round-trip consistency (DTO → JSON → DTO)
 * - Matrix format parsing (scalar, vector, matrix)
 * - paramValues map integration and legacy compatibility
 * - Utility method correctness (isScalar, getRowCount)
 * - Edge case handling (empty strings, invalid formats)
 *
 * @author NCSLab Test Automation
 * @version 1.0
 * @since 2025
 */
public class MatrixDtoTest {

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
     * Test validation with scalar matrix value.
     * Validates that single values are accepted.
     */
    @Test
    public void testValidation_ScalarValue() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Matrix1")
                .matrixValue(TypedParameter.of("5"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Scalar value should be valid", result.isValid());
    }

    /**
     * Test validation with bracketed scalar.
     * Validates that [value] format is accepted.
     */
    @Test
    public void testValidation_BracketedScalar() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1]"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Bracketed scalar should be valid", result.isValid());
    }

    /**
     * Test validation with row vector.
     * Validates that [1,2,3] format is accepted.
     */
    @Test
    public void testValidation_RowVector() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2,3]"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Row vector should be valid", result.isValid());
    }

    /**
     * Test validation with column vector.
     * Validates that [1;2;3] format is accepted.
     */
    @Test
    public void testValidation_ColumnVector() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1;2;3]"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Column vector should be valid", result.isValid());
    }

    /**
     * Test validation with 2D matrix.
     * Validates that [1,2;3,4] format is accepted.
     */
    @Test
    public void testValidation_2DMatrix() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2;3,4]"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("2D matrix should be valid", result.isValid());
    }

    /**
     * Test validation with null matrix value.
     * Validates that null matrix value is rejected.
     */
    @Test
    public void testValidation_NullMatrixValue() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(null)
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Null matrix value should fail validation", result.isValid());
        assertTrue("Should have matrix value error",
                   result.getErrors().stream().anyMatch(e -> e.contains("MatrixValue") && e.contains("required")));
    }

    /**
     * Test validation with empty matrix value.
     * Validates that empty string matrix value is rejected.
     */
    @Test
    public void testValidation_EmptyMatrixValue() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of(""))
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Empty matrix value should fail validation", result.isValid());
        assertTrue("Should have empty error",
                   result.getErrors().stream().anyMatch(e -> e.contains("empty")));
    }

    /**
     * Test validation with unbalanced brackets (opening only).
     * Validates that unbalanced brackets are rejected.
     */
    @Test
    public void testValidation_UnbalancedBrackets_OpeningOnly() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2,3"))
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Unbalanced brackets should fail validation", result.isValid());
        assertTrue("Should have brackets error",
                   result.getErrors().stream().anyMatch(e -> e.contains("brackets")));
    }

    /**
     * Test validation with unbalanced brackets (closing only).
     * Validates that unbalanced brackets are rejected.
     */
    @Test
    public void testValidation_UnbalancedBrackets_ClosingOnly() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("1,2,3]"))
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Unbalanced brackets should fail validation", result.isValid());
        assertTrue("Should have brackets error",
                   result.getErrors().stream().anyMatch(e -> e.contains("brackets")));
    }

    /**
     * Test validation with negative sample time (not -1).
     * Validates that negative sample times other than -1 are rejected.
     */
    @Test
    public void testValidation_NegativeSampleTime() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1]"))
                .sampleTime(TypedParameter.of(-2.0))
                .build();

        ValidationResult result = dto.validate();
        assertFalse("Negative sample time (not -1) should fail validation", result.isValid());
        assertTrue("Should have sample time error",
                   result.getErrors().stream().anyMatch(e -> e.contains("Sample time")));
    }

    // ============================================================================
    // SERIALIZATION TESTS
    // ============================================================================

    /**
     * Test JSON serialization of MatrixDto.
     * Validates that DTO can be converted to JSON string.
     */
    @Test
    public void testSerialization_ToJson() throws Exception {
        MatrixDto dto = MatrixDto.builder()
                .blockId(42)
                .blockName("TestMatrix")
                .matrixValue(TypedParameter.of("[1,2;3,4]"))
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("double"))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain block name", json.contains("TestMatrix"));
        assertTrue("JSON should contain block type", json.contains("Matrix"));
    }

    /**
     * Test JSON deserialization to MatrixDto.
     * Validates that JSON can be converted back to DTO.
     */
    @Test
    public void testDeserialization_FromJson() throws Exception {
        String json = "{\"blockId\":42,\"blockName\":\"TestMatrix\"," +
                      "\"blockType\":\"Matrix\"," +
                      "\"paramValues\":{\"MatrixValue\":\"[1,2;3,4]\"," +
                      "\"SampleTime\":-1.0,\"OutDataTypeStr\":\"double\"}}";

        MatrixDto dto = objectMapper.readValue(json, MatrixDto.class);

        assertNotNull("Deserialized DTO should not be null", dto);
        assertEquals("Block ID should match", 42, dto.getBlockId().intValue());
        assertEquals("Block name should match", "TestMatrix", dto.getBlockName());
        assertEquals("Matrix value should match", "[1,2;3,4]", dto.getMatrixValueAsString());
    }

    /**
     * Test round-trip consistency (DTO → JSON → DTO).
     * Validates that serialization and deserialization preserve data integrity.
     */
    @Test
    public void testSerialization_RoundTrip() throws Exception {
        MatrixDto original = MatrixDto.builder()
                .blockId(100)
                .blockName("RoundTripTest")
                .matrixValue(TypedParameter.of("[1,2,3]"))
                .sampleTime(TypedParameter.of(0.01))
                .outDataTypeStr(TypedParameter.of("double"))
                .build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(original);

        // Deserialize back to DTO
        MatrixDto restored = objectMapper.readValue(json, MatrixDto.class);

        assertNotNull("Restored DTO should not be null", restored);
        assertEquals("Block ID should be preserved", original.getBlockId(), restored.getBlockId());
        assertEquals("Block name should be preserved", original.getBlockName(), restored.getBlockName());
        assertEquals("Matrix value should be preserved", original.getMatrixValueAsString(), restored.getMatrixValueAsString());
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
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .build();

        assertEquals("Default matrix value should be [1]", "[1]", dto.getMatrixValueAsString());
        assertEquals("Default sample time should be -1", -1.0, dto.getSampleTimeValue(), DELTA);
        assertEquals("Default output data type should be double", "double", dto.getOutDataTypeStrValue());
    }

    /**
     * Test custom parameter values.
     * Validates that custom values are correctly stored and retrieved.
     */
    @Test
    public void testParameterAccess_CustomValues() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2,3;4,5,6]"))
                .sampleTime(TypedParameter.of(0.01))
                .outDataTypeStr(TypedParameter.of("single"))
                .build();

        assertEquals("Matrix value should match", "[1,2,3;4,5,6]", dto.getMatrixValueAsString());
        assertEquals("Sample time should match", 0.01, dto.getSampleTimeValue(), DELTA);
        assertEquals("Output data type should match", "single", dto.getOutDataTypeStrValue());
    }

    // ============================================================================
    // UTILITY METHOD TESTS
    // ============================================================================

    /**
     * Test isScalar() with scalar values.
     * Validates correct detection of scalar values.
     */
    @Test
    public void testUtility_IsScalar_PlainNumber() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("5"))
                .build();

        assertTrue("Plain number should be detected as scalar", dto.isScalar());
    }

    /**
     * Test isScalar() with bracketed scalar.
     * Validates correct detection of bracketed single values.
     */
    @Test
    public void testUtility_IsScalar_BracketedSingle() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[5]"))
                .build();

        assertTrue("Bracketed single value should be detected as scalar", dto.isScalar());
    }

    /**
     * Test isScalar() with vector.
     * Validates that vectors are not detected as scalars.
     */
    @Test
    public void testUtility_IsScalar_Vector() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2,3]"))
                .build();

        assertFalse("Vector should not be detected as scalar", dto.isScalar());
    }

    /**
     * Test isScalar() with matrix.
     * Validates that matrices are not detected as scalars.
     */
    @Test
    public void testUtility_IsScalar_Matrix() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2;3,4]"))
                .build();

        assertFalse("Matrix should not be detected as scalar", dto.isScalar());
    }

    /**
     * Test getRowCount() with scalar.
     * Validates correct row count for scalar values.
     */
    @Test
    public void testUtility_GetRowCount_Scalar() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("5"))
                .build();

        assertEquals("Scalar should have 1 row", 1, dto.getRowCount());
    }

    /**
     * Test getRowCount() with row vector.
     * Validates correct row count for row vectors.
     */
    @Test
    public void testUtility_GetRowCount_RowVector() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2,3]"))
                .build();

        assertEquals("Row vector should have 1 row", 1, dto.getRowCount());
    }

    /**
     * Test getRowCount() with column vector.
     * Validates correct row count for column vectors.
     */
    @Test
    public void testUtility_GetRowCount_ColumnVector() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1;2;3]"))
                .build();

        assertEquals("Column vector should have 3 rows", 3, dto.getRowCount());
    }

    /**
     * Test getRowCount() with 2D matrix.
     * Validates correct row count for 2D matrices.
     */
    @Test
    public void testUtility_GetRowCount_2DMatrix() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2;3,4;5,6]"))
                .build();

        assertEquals("Matrix should have 3 rows", 3, dto.getRowCount());
    }

    /**
     * Test isContinuous() utility method.
     * Validates correct detection of continuous time operation.
     */
    @Test
    public void testUtility_IsContinuous() {
        MatrixDto continuousDto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1]"))
                .sampleTime(TypedParameter.of(0.0))
                .build();

        assertTrue("Should detect continuous time (0.0)", continuousDto.isContinuous());
    }

    /**
     * Test isInherited() utility method.
     * Validates correct detection of inherited sample time.
     */
    @Test
    public void testUtility_IsInherited() {
        MatrixDto inheritedDto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1]"))
                .sampleTime(TypedParameter.of(-1.0))
                .build();

        assertTrue("Should detect inherited sample time (-1.0)", inheritedDto.isInherited());
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
        MatrixDto original = MatrixDto.builder()
                .blockId(1)
                .blockName("Original")
                .matrixValue(TypedParameter.of("[1,2;3,4]"))
                .sampleTime(TypedParameter.of(0.01))
                .build();

        MatrixDto copy = original.copy();

        assertNotNull("Copy should not be null", copy);
        assertEquals("Block ID should match", original.getBlockId(), copy.getBlockId());
        assertEquals("Block name should match", original.getBlockName(), copy.getBlockName());
        assertEquals("Matrix value should match", original.getMatrixValueAsString(), copy.getMatrixValueAsString());
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
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1,2;3,4]"))
                .sampleTime(TypedParameter.of(0.01))
                .outDataTypeStr(TypedParameter.of("double"))
                .build();

        var paramMap = dto.toParameterMap();

        assertNotNull("Parameter map should not be null", paramMap);
        assertNotNull("Should contain MatrixValue", paramMap.get("MatrixValue"));
        assertNotNull("Should contain SampleTime", paramMap.get("SampleTime"));
        assertNotNull("Should contain OutDataTypeStr", paramMap.get("OutDataTypeStr"));
    }

    // ============================================================================
    // EDGE CASE TESTS
    // ============================================================================

    /**
     * Test negative numbers in matrix.
     * Validates that negative values are accepted.
     */
    @Test
    public void testEdgeCase_NegativeNumbers() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[-1,-2;-3,-4]"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Negative numbers should be valid", result.isValid());
    }

    /**
     * Test decimal numbers in matrix.
     * Validates that decimal values are accepted.
     */
    @Test
    public void testEdgeCase_DecimalNumbers() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(1)
                .blockName("Test")
                .matrixValue(TypedParameter.of("[1.5,2.7;3.14,4.2]"))
                .build();

        ValidationResult result = dto.validate();
        assertTrue("Decimal numbers should be valid", result.isValid());
    }

    /**
     * Test toString() method output.
     * Validates that toString provides meaningful representation.
     */
    @Test
    public void testToString_Output() {
        MatrixDto dto = MatrixDto.builder()
                .blockId(42)
                .blockName("TestBlock")
                .matrixValue(TypedParameter.of("[1,2;3,4]"))
                .build();

        String str = dto.toString();

        assertNotNull("toString should not return null", str);
        assertTrue("toString should contain block ID", str.contains("42"));
        assertTrue("toString should contain block name", str.contains("TestBlock"));
        assertTrue("toString should contain matrix value", str.contains("[1,2;3,4]"));
    }
}
