package com.ncslab.block.data;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

/**
 * Comprehensive test suite for StringData class.
 * Tests all functionality including constructors, conversions, operations, and edge cases.
 *
 * @author NCSLab
 * @version 1.0
 */
public class StringDataTest {

    private StringData emptyString;
    private StringData simpleString;
    private StringData unicodeString;
    private StringData maxLengthString;

    @Before
    public void setUp() {
        emptyString = new StringData("");
        simpleString = new StringData("Hello World");
        unicodeString = new StringData("Hello 世界");
        maxLengthString = new StringData("This is a long string", 10);
    }

    /**
     * Test basic string construction
     */
    @Test
    public void testBasicConstruction() {
        assertEquals("Hello World", simpleString.getStringValue());
        assertEquals("Hello World", simpleString.toString());
        assertEquals(11, simpleString.getLength());
        assertFalse(simpleString.isEmpty());
    }

    /**
     * Test empty string construction
     */
    @Test
    public void testEmptyStringConstruction() {
        assertTrue(emptyString.isEmpty());
        assertEquals(0, emptyString.getLength());
        assertEquals("", emptyString.getStringValue());
        assertTrue(emptyString.isZero());
    }

    /**
     * Test null string handling
     */
    @Test
    public void testNullStringHandling() {
        StringData nullString = new StringData((String) null);
        assertNotNull(nullString.getStringValue());
        assertTrue(nullString.isEmpty());
        assertEquals("", nullString.getStringValue());
    }

    /**
     * Test maximum length constraint
     */
    @Test
    public void testMaxLengthConstraint() {
        assertEquals(10, maxLengthString.getMaxLength());
        assertEquals("This is a ", maxLengthString.getStringValue());
        assertEquals(10, maxLengthString.getLength());
    }

    /**
     * Test maximum length with shorter string
     */
    @Test
    public void testMaxLengthWithShorterString() {
        StringData shortString = new StringData("Short", 20);
        assertEquals("Short", shortString.getStringValue());
        assertEquals(5, shortString.getLength());
        assertEquals(20, shortString.getMaxLength());
    }

    /**
     * Test zero and unlimited max length
     */
    @Test
    public void testUnlimitedMaxLength() {
        StringData unlimited = new StringData("Any length string", 0);
        assertEquals(0, unlimited.getMaxLength());
        assertEquals("Any length string", unlimited.getStringValue());
    }

    /**
     * Test negative max length throws exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMaxLength() {
        new StringData("test", -1);
    }

    /**
     * Test UTF-8 byte array conversion with null terminator
     */
    @Test
    public void testUTF8BytesWithNullTerminator() {
        byte[] bytes = simpleString.getUTF8Bytes();
        assertNotNull(bytes);

        // Check null terminator
        assertEquals(0, bytes[bytes.length - 1]);

        // Verify length includes null terminator
        assertEquals(simpleString.getStringValue().getBytes(StandardCharsets.UTF_8).length + 1,
                     bytes.length);

        // Verify content (excluding null terminator)
        byte[] expected = "Hello World".getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], bytes[i]);
        }
    }

    /**
     * Test UTF-8 byte array conversion without null terminator
     */
    @Test
    public void testUTF8BytesWithoutTerminator() {
        byte[] bytes = simpleString.getUTF8BytesWithoutTerminator();
        byte[] expected = "Hello World".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(expected, bytes);
        assertEquals(expected.length, bytes.length);
    }

    /**
     * Test byte length calculation
     */
    @Test
    public void testByteLength() {
        int expectedByteLength = "Hello World".getBytes(StandardCharsets.UTF_8).length + 1; // +1 for null terminator
        assertEquals(expectedByteLength, simpleString.getByteLength());
    }

    /**
     * Test construction from UTF-8 byte array
     */
    @Test
    public void testConstructionFromByteArray() {
        byte[] bytes = "Test String".getBytes(StandardCharsets.UTF_8);
        StringData fromBytes = new StringData(bytes);

        assertEquals("Test String", fromBytes.getStringValue());
        assertEquals(11, fromBytes.getLength());
    }

    /**
     * Test construction from null-terminated byte array
     */
    @Test
    public void testConstructionFromNullTerminatedByteArray() {
        byte[] original = "Test String".getBytes(StandardCharsets.UTF_8);
        byte[] withNull = new byte[original.length + 1];
        System.arraycopy(original, 0, withNull, 0, original.length);
        withNull[original.length] = 0; // null terminator

        StringData fromBytes = new StringData(withNull);
        assertEquals("Test String", fromBytes.getStringValue());
    }

    /**
     * Test construction from byte array with maxLength
     */
    @Test
    public void testConstructionFromByteArrayWithMaxLength() {
        byte[] bytes = "Very Long String".getBytes(StandardCharsets.UTF_8);
        StringData fromBytes = new StringData(bytes, 8);

        assertEquals(8, fromBytes.getLength());
        assertEquals("Very Lon", fromBytes.getStringValue());
    }

    /**
     * Test construction from null byte array
     */
    @Test
    public void testConstructionFromNullByteArray() {
        StringData fromNull = new StringData((byte[]) null);
        assertTrue(fromNull.isEmpty());
        assertEquals("", fromNull.getStringValue());
    }

    /**
     * Test construction from empty byte array
     */
    @Test
    public void testConstructionFromEmptyByteArray() {
        StringData fromEmpty = new StringData(new byte[0]);
        assertTrue(fromEmpty.isEmpty());
        assertEquals("", fromEmpty.getStringValue());
    }

    /**
     * Test DataType returns STRING
     */
    @Test
    public void testDataType() {
        assertEquals(DataType.STRING, simpleString.getDataType());
        assertEquals(DataType.STRING, emptyString.getDataType());
        assertEquals(DataType.STRING, maxLengthString.getDataType());
    }

    /**
     * Test dimension methods
     */
    @Test
    public void testDimensions() {
        assertEquals(1, simpleString.getWidth());
        assertEquals(1, simpleString.getHeight());
        assertEquals(1, emptyString.getWidth());
        assertEquals(1, emptyString.getHeight());
    }

    /**
     * Test isZero method
     */
    @Test
    public void testIsZero() {
        assertTrue(emptyString.isZero());
        assertFalse(simpleString.isZero());
        assertFalse(maxLengthString.isZero());
    }

    /**
     * Test ISO 8859-1 compatibility
     */
    @Test
    public void testISO88591Compatibility() {
        StringData asciiString = new StringData("Hello World 123");
        assertTrue(asciiString.isISO88591Compatible());

        StringData latin1String = new StringData("Café");
        assertTrue(latin1String.isISO88591Compatible());

        StringData unicodeString = new StringData("Hello 世界");
        assertFalse(unicodeString.isISO88591Compatible());
    }

    /**
     * Test C code generation for initialization
     */
    @Test
    public void testInitCodeC() {
        StringData testString = new StringData("Hi");
        String code = testString.getInitCodeC("myVar");

        assertNotNull(code);
        assertTrue(code.contains("uint8_t myVar"));
        assertTrue(code.contains("72")); // 'H' in ASCII
        assertTrue(code.contains("105")); // 'i' in ASCII
        assertTrue(code.contains("0")); // null terminator
    }

    /**
     * Test MATLAB code generation for initialization
     */
    @Test
    public void testInitCodeM() {
        String code = simpleString.getInitCodeM("myVar");

        assertNotNull(code);
        assertEquals("myVar = 'Hello World';\n", code);
    }

    /**
     * Test MATLAB code generation with quote escaping
     */
    @Test
    public void testInitCodeMWithQuotes() {
        StringData withQuotes = new StringData("It's a test");
        String code = withQuotes.getInitCodeM("myVar");

        assertNotNull(code);
        assertTrue(code.contains("It''s a test")); // Single quotes escaped in MATLAB
    }

    /**
     * Test C code generation for definition
     */
    @Test
    public void testDefineCodeC() {
        String code = simpleString.getDefineCodeC("myVar");

        assertNotNull(code);
        assertTrue(code.contains("uint8_t myVar"));
        assertTrue(code.contains("[")); // Array declaration
        assertTrue(code.contains("]"));
    }

    /**
     * Test equals method
     */
    @Test
    public void testEquals() {
        StringData str1 = new StringData("Test");
        StringData str2 = new StringData("Test");
        StringData str3 = new StringData("Different");
        StringData str4 = new StringData("Test", 10);

        assertEquals(str1, str2);
        assertNotEquals(str1, str3);
        assertNotEquals(str1, str4); // Different maxLength
        assertEquals(str1, str1); // Same instance
        assertNotEquals(str1, null);
        assertNotEquals(str1, "Test"); // Different type
    }

    /**
     * Test hashCode consistency
     */
    @Test
    public void testHashCode() {
        StringData str1 = new StringData("Test");
        StringData str2 = new StringData("Test");

        assertEquals(str1.hashCode(), str2.hashCode());
        assertEquals(str1.hashCode(), str1.hashCode()); // Consistency
    }

    /**
     * Test withValue method
     */
    @Test
    public void testWithValue() {
        StringData original = new StringData("Original", 20);
        StringData modified = original.withValue("Modified");

        assertEquals("Modified", modified.getStringValue());
        assertEquals(20, modified.getMaxLength()); // Preserved
        assertEquals("Original", original.getStringValue()); // Original unchanged
    }

    /**
     * Test withMaxLength method
     */
    @Test
    public void testWithMaxLength() {
        StringData original = new StringData("Hello World");
        StringData modified = original.withMaxLength(5);

        assertEquals("Hello", modified.getStringValue());
        assertEquals(5, modified.getMaxLength());
        assertEquals("Hello World", original.getStringValue()); // Original unchanged
    }

    /**
     * Test concat with another StringData
     */
    @Test
    public void testConcatStringData() {
        StringData str1 = new StringData("Hello");
        StringData str2 = new StringData(" World");
        StringData result = str1.concat(str2);

        assertEquals("Hello World", result.getStringValue());
        assertEquals(0, result.getMaxLength()); // Both have 0, result has 0
    }

    /**
     * Test concat with maxLength
     */
    @Test
    public void testConcatWithMaxLength() {
        StringData str1 = new StringData("Hello", 10);
        StringData str2 = new StringData(" World", 15);
        StringData result = str1.concat(str2);

        // "Hello World" has 11 characters, maxLength is max(10,15) = 15, so no truncation
        assertEquals("Hello World", result.getStringValue());
        assertEquals(15, result.getMaxLength());
    }

    /**
     * Test concat with null
     */
    @Test
    public void testConcatWithNull() {
        StringData result = simpleString.concat((StringData) null);
        assertEquals(simpleString, result);
        assertEquals(simpleString.getStringValue(), result.getStringValue());
    }

    /**
     * Test concat with String
     */
    @Test
    public void testConcatWithString() {
        StringData result = simpleString.concat(" Suffix");
        assertEquals("Hello World Suffix", result.getStringValue());
    }

    /**
     * Test substring with two indices
     */
    @Test
    public void testSubstringTwoIndices() {
        StringData result = simpleString.substring(0, 5);
        assertEquals("Hello", result.getStringValue());
        assertEquals(simpleString.getMaxLength(), result.getMaxLength());
    }

    /**
     * Test substring with one index
     */
    @Test
    public void testSubstringOneIndex() {
        StringData result = simpleString.substring(6);
        assertEquals("World", result.getStringValue());
    }

    /**
     * Test substring out of bounds
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringOutOfBounds() {
        simpleString.substring(0, 100);
    }

    /**
     * Test toUpperCase
     */
    @Test
    public void testToUpperCase() {
        StringData result = simpleString.toUpperCase();
        assertEquals("HELLO WORLD", result.getStringValue());
        assertEquals(simpleString.getMaxLength(), result.getMaxLength());
        assertEquals("Hello World", simpleString.getStringValue()); // Original unchanged
    }

    /**
     * Test toLowerCase
     */
    @Test
    public void testToLowerCase() {
        StringData result = simpleString.toLowerCase();
        assertEquals("hello world", result.getStringValue());
    }

    /**
     * Test trim
     */
    @Test
    public void testTrim() {
        StringData withSpaces = new StringData("  Hello World  ");
        StringData result = withSpaces.trim();

        assertEquals("Hello World", result.getStringValue());
        assertEquals("  Hello World  ", withSpaces.getStringValue()); // Original unchanged
    }

    /**
     * Test Unicode string handling
     */
    @Test
    public void testUnicodeString() {
        StringData unicode = new StringData("Hello 世界");
        assertEquals("Hello 世界", unicode.getStringValue());
        assertEquals(8, unicode.getLength()); // 6 ASCII + 2 Chinese characters

        // UTF-8 encoding of Chinese characters uses 3 bytes each
        byte[] bytes = unicode.getUTF8BytesWithoutTerminator();
        assertTrue(bytes.length > unicode.getLength()); // More bytes than characters
    }

    /**
     * Test empty string special cases
     */
    @Test
    public void testEmptyStringSpecialCases() {
        StringData empty = new StringData("");

        assertTrue(empty.isEmpty());
        assertTrue(empty.isZero());
        assertEquals(0, empty.getLength());
        assertEquals(1, empty.getByteLength()); // Only null terminator
        assertTrue(empty.isISO88591Compatible());

        byte[] bytes = empty.getUTF8Bytes();
        assertEquals(1, bytes.length);
        assertEquals(0, bytes[0]); // Only null terminator
    }

    /**
     * Test round-trip conversion: String -> Bytes -> String
     */
    @Test
    public void testRoundTripConversion() {
        String original = "Test String 123";
        StringData str1 = new StringData(original);
        byte[] bytes = str1.getUTF8BytesWithoutTerminator();
        StringData str2 = new StringData(bytes);

        assertEquals(original, str2.getStringValue());
        assertEquals(str1.getStringValue(), str2.getStringValue());
    }

    /**
     * Test immutability of operations
     */
    @Test
    public void testImmutability() {
        StringData original = new StringData("Original");
        String originalValue = original.getStringValue();

        original.toUpperCase();
        original.toLowerCase();
        original.trim();
        original.concat(" suffix");
        original.withValue("New");

        // Original should be unchanged
        assertEquals(originalValue, original.getStringValue());
    }

    /**
     * Test special characters and escaping
     */
    @Test
    public void testSpecialCharacters() {
        StringData withNewlines = new StringData("Line1\nLine2\nLine3");
        assertEquals("Line1\nLine2\nLine3", withNewlines.getStringValue());

        StringData withTabs = new StringData("Col1\tCol2\tCol3");
        assertEquals("Col1\tCol2\tCol3", withTabs.getStringValue());

        StringData withQuotes = new StringData("He said \"Hello\"");
        assertEquals("He said \"Hello\"", withQuotes.getStringValue());
    }

    /**
     * Test very long strings
     */
    @Test
    public void testVeryLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Test");
        }
        String longString = sb.toString();

        StringData longData = new StringData(longString);
        assertEquals(4000, longData.getLength());
        assertEquals(longString, longData.getStringValue());
    }

    /**
     * Performance test for byte conversion
     */
    @Test
    public void testByteConversionPerformance() {
        StringData data = new StringData("Performance Test String");

        // Multiple conversions should work correctly
        for (int i = 0; i < 1000; i++) {
            byte[] bytes = data.getUTF8Bytes();
            assertNotNull(bytes);
            assertEquals(0, bytes[bytes.length - 1]);
        }
    }
}
