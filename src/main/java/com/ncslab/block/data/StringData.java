package com.ncslab.block.data;

import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.apache.velocity.VelocityContext;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * StringData extends the Data class to support string storage and manipulation.
 * This class provides compatibility with C code generation through UTF-8 byte array
 * representation and supports both variable-length and fixed-length strings.
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li>String storage using Java String internally</li>
 *   <li>Conversion to/from uint8 byte arrays for C code generation</li>
 *   <li>Null-terminated UTF-8 representation</li>
 *   <li>Support for ISO/IEC 8859-1 character set (first 256 Unicode code points)</li>
 *   <li>Variable-length and fixed-length string support with maximum length configuration</li>
 * </ul>
 *
 * @author NCSLab
 * @version 1.0
 * @since 2025
 */
public class StringData extends Data {

    /**
     * The internal string value stored by this StringData instance.
     */
    @Getter
    private String stringValue;

    /**
     * The maximum length of the string in characters.
     * A value of 0 indicates unlimited length.
     */
    @Getter
    private int maxLength;

    /**
     * VelocityContext for template-based code generation.
     */
    private VelocityContext context = new VelocityContext();

    /**
     * Creates a new StringData instance with the specified string value and no length limit.
     *
     * @param value the string value to store (null will be converted to empty string)
     */
    public StringData(String value) {
        this(value, 0);
    }

    /**
     * Creates a new StringData instance with the specified string value and maximum length.
     * If the string exceeds maxLength, it will be truncated.
     *
     * @param value the string value to store (null will be converted to empty string)
     * @param maxLength the maximum length in characters (0 = unlimited)
     * @throws IllegalArgumentException if maxLength is negative
     */
    public StringData(String value, int maxLength) {
        super();

        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength cannot be negative: " + maxLength);
        }

        this.maxLength = maxLength;
        this.stringValue = value != null ? value : "";

        // Truncate if necessary
        if (maxLength > 0 && this.stringValue.length() > maxLength) {
            this.stringValue = this.stringValue.substring(0, maxLength);
        }

        // Set the data type to STRING
        super.setInitString(this.stringValue);
        super.setDataString(this.stringValue);
    }

    /**
     * Creates a new StringData instance from a UTF-8 byte array.
     * The byte array may be null-terminated (ending with 0 byte).
     *
     * @param utf8Bytes the UTF-8 encoded byte array (null will create empty string)
     * @throws IllegalArgumentException if the byte array cannot be decoded as UTF-8
     */
    public StringData(byte[] utf8Bytes) {
        this(utf8Bytes, 0);
    }

    /**
     * Creates a new StringData instance from a UTF-8 byte array with maximum length.
     * The byte array may be null-terminated (ending with 0 byte).
     *
     * @param utf8Bytes the UTF-8 encoded byte array (null will create empty string)
     * @param maxLength the maximum length in characters (0 = unlimited)
     * @throws IllegalArgumentException if maxLength is negative or byte array cannot be decoded
     */
    public StringData(byte[] utf8Bytes, int maxLength) {
        super();

        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength cannot be negative: " + maxLength);
        }

        this.maxLength = maxLength;

        if (utf8Bytes == null || utf8Bytes.length == 0) {
            this.stringValue = "";
        } else {
            try {
                // Find null terminator if present
                int length = utf8Bytes.length;
                for (int i = 0; i < utf8Bytes.length; i++) {
                    if (utf8Bytes[i] == 0) {
                        length = i;
                        break;
                    }
                }

                // Decode the byte array
                this.stringValue = new String(utf8Bytes, 0, length, StandardCharsets.UTF_8);

                // Truncate if necessary
                if (maxLength > 0 && this.stringValue.length() > maxLength) {
                    this.stringValue = this.stringValue.substring(0, maxLength);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to decode UTF-8 byte array: " + e.getMessage(), e);
            }
        }

        super.setInitString(this.stringValue);
        super.setDataString(this.stringValue);
    }

    /**
     * Returns the UTF-8 encoded byte array representation of this string.
     * The returned array includes a null terminator (0 byte) at the end
     * for compatibility with C-style strings.
     *
     * @return byte array containing UTF-8 encoded string with null terminator
     */
    public byte[] getUTF8Bytes() {
        byte[] stringBytes = stringValue.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[stringBytes.length + 1]; // +1 for null terminator
        System.arraycopy(stringBytes, 0, result, 0, stringBytes.length);
        result[stringBytes.length] = 0; // null terminator
        return result;
    }

    /**
     * Returns the UTF-8 encoded byte array representation without null terminator.
     *
     * @return byte array containing UTF-8 encoded string without null terminator
     */
    public byte[] getUTF8BytesWithoutTerminator() {
        return stringValue.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns the length of the string in characters (not bytes).
     *
     * @return the number of characters in the string
     */
    public int getLength() {
        return stringValue.length();
    }

    /**
     * Returns the length of the UTF-8 encoded byte representation including null terminator.
     *
     * @return the number of bytes needed to represent this string in UTF-8 with null terminator
     */
    public int getByteLength() {
        return getUTF8Bytes().length;
    }

    /**
     * Checks if the string is empty.
     *
     * @return true if the string length is 0, false otherwise
     */
    public boolean isEmpty() {
        return stringValue.isEmpty();
    }

    /**
     * Validates that all characters in the string are within the ISO/IEC 8859-1 character set
     * (first 256 Unicode code points: U+0000 to U+00FF).
     *
     * @return true if all characters are in ISO/IEC 8859-1, false otherwise
     */
    public boolean isISO88591Compatible() {
        for (int i = 0; i < stringValue.length(); i++) {
            char c = stringValue.charAt(i);
            if (c > 255) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the DataType for this data, which is always STRING.
     *
     * @return DataType.STRING
     */
    @Override
    public DataType getDataType() {
        return DataType.STRING;
    }

    /**
     * Returns the width dimension of this string data, which is always 1.
     *
     * @return 1
     */
    @Override
    public int getWidth() {
        return 1;
    }

    /**
     * Returns the height dimension of this string data, which is always 1.
     *
     * @return 1
     */
    @Override
    public int getHeight() {
        return 1;
    }

    /**
     * Checks if this string data is considered "zero" (empty string).
     *
     * @return true if the string is empty, false otherwise
     */
    @Override
    public boolean isZero() {
        return stringValue.isEmpty();
    }

    /**
     * Generates C code to initialize a string variable with this string's value.
     * The generated code creates a null-terminated char array.
     *
     * @param name the variable name to use in the generated code
     * @return C code string for initialization
     */
    @Override
    public String getInitCodeC(String name) {
        StringBuilder code = new StringBuilder();

        // Generate array initialization with individual byte values for compatibility
        byte[] bytes = getUTF8Bytes();
        code.append(String.format("uint8_t %s[%d] = {", name, bytes.length));

        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                code.append(", ");
            }
            code.append(String.format("%d", bytes[i] & 0xFF)); // Convert to unsigned
        }

        code.append("};\n");
        return code.toString();
    }

    /**
     * Generates MATLAB code to initialize a string variable with this string's value.
     *
     * @param name the variable name to use in the generated code
     * @return MATLAB code string for initialization
     */
    @Override
    public String getInitCodeM(String name) {
        // Escape single quotes in MATLAB strings
        String escapedString = stringValue.replace("'", "''");
        return String.format("%s = '%s';\n", name, escapedString);
    }

    /**
     * Generates C code to define a string variable with appropriate type and size.
     *
     * @param name the variable name to use in the generated code
     * @return C code string for definition
     */
    @Override
    public String getDefineCodeC(String name) {
        context.put("name", name);
        context.put("dataType", getDataType());
        context.put("stringDataType", DataType.STRING);
        context.put("byteLength", getByteLength());
        context.put("maxLength", maxLength > 0 ? maxLength + 1 : getByteLength()); // +1 for null terminator

        // Try to use template if available, otherwise generate inline
        try {
            return TemplateManager.renderTemplate("c/data/StringData/define.vm", context);
        } catch (Exception e) {
            // Fallback to inline generation if template not found
            int arraySize = maxLength > 0 ? maxLength + 1 : getByteLength();
            return String.format("uint8_t %s[%d];\n", name, arraySize);
        }
    }

    /**
     * Returns the string representation of this StringData.
     *
     * @return the string value
     */
    @Override
    public String toString() {
        return stringValue;
    }

    /**
     * Compares this StringData with another object for equality.
     * Two StringData objects are equal if they have the same string value and maxLength.
     *
     * @param obj the object to compare with
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StringData other = (StringData) obj;
        return maxLength == other.maxLength &&
               Objects.equals(stringValue, other.stringValue);
    }

    /**
     * Returns the hash code for this StringData.
     *
     * @return hash code based on string value and maxLength
     */
    @Override
    public int hashCode() {
        return Objects.hash(stringValue, maxLength);
    }

    /**
     * Creates a copy of this StringData with a new string value.
     * The maxLength is preserved from the original.
     *
     * @param newValue the new string value
     * @return a new StringData instance with the new value
     */
    public StringData withValue(String newValue) {
        return new StringData(newValue, this.maxLength);
    }

    /**
     * Creates a copy of this StringData with a new maximum length.
     * If the new maxLength is shorter than the current string, the string will be truncated.
     *
     * @param newMaxLength the new maximum length (0 = unlimited)
     * @return a new StringData instance with the new maxLength
     */
    public StringData withMaxLength(int newMaxLength) {
        return new StringData(this.stringValue, newMaxLength);
    }

    /**
     * Concatenates this StringData with another StringData or string.
     * The maxLength of the result is the maximum of the two maxLengths (0 if either is 0).
     *
     * @param other the other StringData to concatenate
     * @return a new StringData containing the concatenated strings
     */
    public StringData concat(StringData other) {
        if (other == null) {
            return this;
        }

        String concatenated = this.stringValue + other.stringValue;
        int newMaxLength = (this.maxLength == 0 || other.maxLength == 0) ?
                          0 : Math.max(this.maxLength, other.maxLength);

        return new StringData(concatenated, newMaxLength);
    }

    /**
     * Concatenates this StringData with a regular String.
     *
     * @param other the string to concatenate
     * @return a new StringData containing the concatenated strings
     */
    public StringData concat(String other) {
        if (other == null) {
            return this;
        }

        return new StringData(this.stringValue + other, this.maxLength);
    }

    /**
     * Returns a substring of this StringData.
     *
     * @param beginIndex the beginning index, inclusive
     * @param endIndex the ending index, exclusive
     * @return a new StringData containing the specified substring
     * @throws IndexOutOfBoundsException if indices are out of range
     */
    public StringData substring(int beginIndex, int endIndex) {
        String sub = stringValue.substring(beginIndex, endIndex);
        return new StringData(sub, this.maxLength);
    }

    /**
     * Returns a substring of this StringData from the specified beginning index to the end.
     *
     * @param beginIndex the beginning index, inclusive
     * @return a new StringData containing the specified substring
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public StringData substring(int beginIndex) {
        String sub = stringValue.substring(beginIndex);
        return new StringData(sub, this.maxLength);
    }

    /**
     * Converts this StringData to upper case.
     *
     * @return a new StringData with all characters converted to upper case
     */
    public StringData toUpperCase() {
        return new StringData(stringValue.toUpperCase(), this.maxLength);
    }

    /**
     * Converts this StringData to lower case.
     *
     * @return a new StringData with all characters converted to lower case
     */
    public StringData toLowerCase() {
        return new StringData(stringValue.toLowerCase(), this.maxLength);
    }

    /**
     * Trims whitespace from both ends of the string.
     *
     * @return a new StringData with leading and trailing whitespace removed
     */
    public StringData trim() {
        return new StringData(stringValue.trim(), this.maxLength);
    }
}
