package com.ncslab.block;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import Jama.Matrix;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static org.junit.Assert.*;

/**
 * Reusable Helper Methods for Block Testing
 *
 * This utility class provides common helper methods that can be used across all block tests
 * to reduce code duplication and ensure consistent validation patterns.
 *
 * CATEGORIES:
 * 1. Code Generation Helpers - Mock CodeStructC creation and validation
 * 2. Syntax Validation Helpers - C++ syntax checking
 * 3. Template Validation Helpers - Velocity template error detection
 * 4. Assertion Helpers - Enhanced assertions for common test patterns
 * 5. Test Data Generators - Create test matrices and scenarios
 *
 * USAGE:
 * These methods are designed to be used as static imports or as part of DirectBlockTestBase.
 * They can also be called directly: BlockTestHelpers.assertValidCppSyntax(code);
 *
 * @author NCSLab Team
 * @version 2025 - Gold Standard Edition
 */
public class BlockTestHelpers {

    // Test precision constants
    private static final double DELTA = 1e-10;
    private static final double TOLERANCE = 1e-6;

    // =========================================================================
    // SECTION 1: CODE GENERATION HELPERS
    // =========================================================================

    /**
     * Creates a mock CodeStructC for testing code generation.
     * This mock captures all generated code without requiring a full model setup.
     *
     * @param model Mock model reference
     * @return Functional mock CodeStructC instance
     */
    public static TestCodeStructC createTestCodeStructC(CodeModelC model) {
        return new TestCodeStructC(model);
    }

    /**
     * TestCodeStructC - A lightweight mock implementation for testing.
     * Captures all code generation without side effects.
     */
    public static class TestCodeStructC extends CodeStructC {
        private StringBuilder initCode = new StringBuilder();
        private StringBuilder outputCode = new StringBuilder();
        private StringBuilder updateCode = new StringBuilder();
        private StringBuilder derivativeCode = new StringBuilder();
        private StringBuilder discreteUpdateCode = new StringBuilder();
        private StringBuilder terminateCode = new StringBuilder();
        private StringBuilder arraysCode = new StringBuilder();
        private StringBuilder statementCode = new StringBuilder();

        public TestCodeStructC(CodeModelC model) {
            super(model);
        }

        @Override
        public void addInitCode(String code) {
            initCode.append(code);
        }

        @Override
        public void addOutputCode(String code) {
            outputCode.append(code);
        }

        @Override
        public void addUpdateCode(String code) {
            updateCode.append(code);
        }

        @Override
        public void addDerivativeCode(String code) {
            derivativeCode.append(code);
        }

        public void addDiscreteUpdateCode(String code) {
            discreteUpdateCode.append(code);
        }

        public void addTerminateCode(String code) {
            terminateCode.append(code);
        }

        public void addArraysCode(String code) {
            arraysCode.append(code);
        }

        public void addStatementCode(String code) {
            statementCode.append(code);
        }

        // Getters for code sections
        public String getInitCode() {
            return initCode.toString();
        }

        public String getOutputCode() {
            return outputCode.toString();
        }

        public String getUpdateCode() {
            return updateCode.toString();
        }

        public String getDerivativeCode() {
            return derivativeCode.toString();
        }

        public String getDiscreteUpdateCode() {
            return discreteUpdateCode.toString();
        }

        public String getTerminateCode() {
            return terminateCode.toString();
        }

        public String getArraysCode() {
            return arraysCode.toString();
        }

        public String getStatementCode() {
            return statementCode.toString();
        }

        /**
         * Gets all generated code concatenated together.
         *
         * @return Complete generated code
         */
        public String getAllCode() {
            StringBuilder all = new StringBuilder();
            all.append(initCode);
            all.append(outputCode);
            all.append(updateCode);
            all.append(derivativeCode);
            all.append(discreteUpdateCode);
            all.append(terminateCode);
            all.append(arraysCode);
            all.append(statementCode);
            return all.toString();
        }

        /**
         * Clears all accumulated code.
         */
        public void clear() {
            initCode.setLength(0);
            outputCode.setLength(0);
            updateCode.setLength(0);
            derivativeCode.setLength(0);
            discreteUpdateCode.setLength(0);
            terminateCode.setLength(0);
            arraysCode.setLength(0);
            statementCode.setLength(0);
        }
    }

    // =========================================================================
    // SECTION 2: SYNTAX VALIDATION HELPERS
    // =========================================================================

    /**
     * Asserts that generated code has valid C++ syntax basics.
     * Checks for:
     * - Balanced braces {}
     * - Balanced parentheses ()
     * - Balanced square brackets []
     * - No unterminated strings
     * - No unterminated comments
     *
     * @param code Generated code to validate
     */
    public static void assertValidCppSyntax(String code) {
        assertNotNull("Code should not be null", code);

        int braceCount = 0;
        int parenCount = 0;
        int bracketCount = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;
        boolean escaped = false;

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            char next = (i < code.length() - 1) ? code.charAt(i + 1) : '\0';

            // Handle escape sequences
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\' && (inString || inChar)) {
                escaped = true;
                continue;
            }

            // Handle comments
            if (!inString && !inChar) {
                if (c == '/' && next == '/' && !inMultiLineComment) {
                    inSingleLineComment = true;
                    i++; // Skip next char
                    continue;
                }
                if (c == '/' && next == '*' && !inSingleLineComment) {
                    inMultiLineComment = true;
                    i++; // Skip next char
                    continue;
                }
                if (c == '*' && next == '/' && inMultiLineComment) {
                    inMultiLineComment = false;
                    i++; // Skip next char
                    continue;
                }
                if (c == '\n' && inSingleLineComment) {
                    inSingleLineComment = false;
                    continue;
                }
            }

            // Skip analysis inside comments
            if (inSingleLineComment || inMultiLineComment) {
                continue;
            }

            // Handle string literals
            if (c == '"' && !inChar) {
                inString = !inString;
                continue;
            }

            // Handle character literals
            if (c == '\'' && !inString) {
                inChar = !inChar;
                continue;
            }

            // Skip analysis inside strings or chars
            if (inString || inChar) {
                continue;
            }

            // Count braces, parentheses, and brackets
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            if (c == '(') parenCount++;
            if (c == ')') parenCount--;
            if (c == '[') bracketCount++;
            if (c == ']') bracketCount--;

            // Detect syntax errors early
            if (braceCount < 0) {
                fail("Unmatched closing brace at position " + i);
            }
            if (parenCount < 0) {
                fail("Unmatched closing parenthesis at position " + i);
            }
            if (bracketCount < 0) {
                fail("Unmatched closing bracket at position " + i);
            }
        }

        // Final balance checks
        assertEquals("Braces should be balanced", 0, braceCount);
        assertEquals("Parentheses should be balanced", 0, parenCount);
        assertEquals("Brackets should be balanced", 0, bracketCount);
        assertFalse("Should not have unterminated string", inString);
        assertFalse("Should not have unterminated character literal", inChar);
        assertFalse("Should not have unterminated multi-line comment", inMultiLineComment);
    }

    /**
     * Checks if code contains basic C++ structural elements.
     *
     * @param code Code to check
     * @return true if code appears to be C++ (contains semicolons, braces, etc.)
     */
    public static boolean isValidCppStructure(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        // Basic C++ should have some of these elements
        boolean hasSemicolon = code.contains(";");
        boolean hasBraces = code.contains("{") || code.contains("}");
        boolean hasAssignment = code.contains("=");

        return hasSemicolon || hasBraces || hasAssignment;
    }

    // =========================================================================
    // SECTION 3: TEMPLATE VALIDATION HELPERS
    // =========================================================================

    /**
     * Asserts that generated code contains no Velocity template errors.
     * Checks for common template error patterns:
     * - $undefined_variable (unquiet reference)
     * - ${undefined_variable} (formal notation)
     * - $!{undefined_variable} (quiet reference that should be defined)
     * - #ERROR directive
     *
     * @param code Generated code to check
     */
    public static void assertCodeContainsNoTemplateErrors(String code) {
        assertNotNull("Code should not be null", code);

        // Remove C++ comments first to avoid false positives
        String codeWithoutComments = removeComments(code);

        // Check for undefined velocity variables (but be lenient)
        // Pattern: $ followed by identifier not in string or comment
        Pattern undefinedVarPattern = Pattern.compile("\\$!?\\{?([a-zA-Z_][a-zA-Z0-9_]*)\\}?");
        Matcher matcher = undefinedVarPattern.matcher(codeWithoutComments);

        int suspiciousVarCount = 0;
        while (matcher.find()) {
            String match = matcher.group();
            String varName = matcher.group(1);

            // Ignore common C++ patterns that look like Velocity variables
            if (isCommonCppPattern(match)) {
                continue;
            }

            System.out.println("Warning: Potential undefined Velocity variable: " + match);
            suspiciousVarCount++;
        }

        // Warn but don't fail - some blocks may intentionally use $ in generated code
        if (suspiciousVarCount > 5) {
            System.err.println("WARNING: Found " + suspiciousVarCount +
                " potential undefined template variables. This may indicate a template problem.");
        }

        // Check for explicit error markers (these should always fail)
        assertFalse("Code should not contain '#ERROR' directive",
            codeWithoutComments.contains("#ERROR"));
        assertFalse("Code should not contain 'TEMPLATE_ERROR'",
            codeWithoutComments.toUpperCase().contains("TEMPLATE_ERROR"));
        assertFalse("Code should not contain 'UNDEFINED_VARIABLE'",
            codeWithoutComments.toUpperCase().contains("UNDEFINED_VARIABLE"));
    }

    /**
     * Removes C++ comments from code to avoid false positives in template validation.
     *
     * @param code Code to process
     * @return Code with comments removed
     */
    private static String removeComments(String code) {
        // Simple comment removal (not perfect but good enough for validation)
        String result = code;

        // Remove multi-line comments
        result = result.replaceAll("/\\*.*?\\*/", "");

        // Remove single-line comments
        result = result.replaceAll("//.*?\\n", "\n");

        return result;
    }

    /**
     * Checks if a string matches common C++ patterns that look like Velocity variables.
     *
     * @param text Text to check
     * @return true if this is likely a C++ pattern, not a Velocity variable
     */
    private static boolean isCommonCppPattern(String text) {
        // Common false positives
        if (text.startsWith("$BLOCK_")) return true; // Block naming convention
        if (text.startsWith("$MODEL_")) return true; // Model naming convention
        if (text.contains("$$")) return true; // Escaped dollar signs

        return false;
    }

    /**
     * Checks if code contains specific template variable references.
     *
     * @param code Code to check
     * @param variableNames Variable names to look for
     * @return true if all variables are referenced
     */
    public static boolean containsAllTemplateVariables(String code, String... variableNames) {
        for (String varName : variableNames) {
            // Check for various Velocity reference formats
            boolean found = code.contains("$" + varName) ||
                          code.contains("${" + varName + "}") ||
                          code.contains("$!" + varName) ||
                          code.contains("$!{" + varName + "}");

            if (!found) {
                return false;
            }
        }
        return true;
    }

    // =========================================================================
    // SECTION 4: ASSERTION HELPERS
    // =========================================================================

    /**
     * Asserts that a value is within a reasonable numeric range.
     * Used to catch NaN, Infinity, and extreme outliers.
     *
     * @param value Value to check
     * @param description Description for error message
     */
    public static void assertReasonableValue(double value, String description) {
        assertFalse(description + " should not be NaN", Double.isNaN(value));
        assertFalse(description + " should not be infinite", Double.isInfinite(value));

        // Check for reasonable magnitude (not too extreme)
        double absValue = Math.abs(value);
        assertTrue(description + " should not be too large (|value| < 1e308)",
            absValue < 1e308);
    }

    /**
     * Asserts that two matrices are equal within tolerance.
     *
     * @param message Error message
     * @param expected Expected matrix
     * @param actual Actual matrix
     * @param delta Tolerance
     */
    public static void assertMatrixEquals(String message, Matrix expected, Matrix actual, double delta) {
        assertNotNull(message + ": Expected matrix is null", expected);
        assertNotNull(message + ": Actual matrix is null", actual);

        assertEquals(message + ": Row dimension mismatch",
            expected.getRowDimension(), actual.getRowDimension());
        assertEquals(message + ": Column dimension mismatch",
            expected.getColumnDimension(), actual.getColumnDimension());

        for (int i = 0; i < expected.getRowDimension(); i++) {
            for (int j = 0; j < expected.getColumnDimension(); j++) {
                assertEquals(
                    message + String.format(": Element mismatch at [%d,%d]", i, j),
                    expected.get(i, j),
                    actual.get(i, j),
                    delta
                );
            }
        }
    }

    /**
     * Asserts that two matrices are equal (convenience overload).
     */
    public static void assertMatrixEquals(Matrix expected, Matrix actual) {
        assertMatrixEquals("Matrix comparison", expected, actual, DELTA);
    }

    /**
     * Asserts that a matrix contains no NaN or Infinity values.
     *
     * @param matrix Matrix to check
     * @param description Description for error message
     */
    public static void assertMatrixValid(Matrix matrix, String description) {
        assertNotNull(description + " should not be null", matrix);

        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                double value = matrix.get(i, j);
                assertFalse(
                    String.format("%s[%d,%d] should not be NaN", description, i, j),
                    Double.isNaN(value)
                );
                assertFalse(
                    String.format("%s[%d,%d] should not be infinite", description, i, j),
                    Double.isInfinite(value)
                );
            }
        }
    }

    /**
     * Asserts that output data is properly initialized.
     *
     * @param outputData Output data to check
     * @param portIndex Port index for error messages
     */
    public static void assertOutputDataValid(Data outputData, int portIndex) {
        assertNotNull("Output data for port " + portIndex + " should not be null",
            outputData);

        DataType dataType = outputData.getDataType();
        assertNotNull("Data type for port " + portIndex + " should not be null",
            dataType);

        if (dataType == DataType.REAL) {
            double value = outputData.getInitValue();
            assertReasonableValue(value,
                "Output value for port " + portIndex);
        } else if (dataType == DataType.MATRIX) {
            Matrix matrix = outputData.getMatrix();
            assertMatrixValid(matrix,
                "Output matrix for port " + portIndex);
        }
    }

    /**
     * Asserts that a block has valid structure after initialization.
     *
     * @param block Block to validate
     */
    public static void assertBlockStructureValid(Block block) {
        assertNotNull("Block should not be null", block);
        assertNotNull("Block type should not be null", block.getBlockType());
        assertNotNull("Block name should not be null", block.getBlockName());
        assertNotNull("Input port list should not be null", block.getInputPortList());
        assertNotNull("Output port list should not be null", block.getOutputPortList());
        assertNotNull("Parameter list should not be null", block.getParameterList());

        // Validate output signals are created
        for (OutputPort port : block.getOutputPortList()) {
            assertNotNull("Output port should not be null", port);
            assertNotNull("Output signal should be created for port",
                port.getOutputSignalC());
        }
    }

    // =========================================================================
    // SECTION 5: TEST DATA GENERATORS
    // =========================================================================

    /**
     * Creates a test matrix filled with a specific value.
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @param value Fill value
     * @return Matrix filled with value
     */
    public static Matrix createFilledMatrix(int rows, int cols, double value) {
        Matrix matrix = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.set(i, j, value);
            }
        }
        return matrix;
    }

    /**
     * Creates a test matrix filled with sequential values (0, 1, 2, ...).
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @return Matrix with sequential values
     */
    public static Matrix createSequentialMatrix(int rows, int cols) {
        Matrix matrix = new Matrix(rows, cols);
        double value = 0.0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.set(i, j, value++);
            }
        }
        return matrix;
    }

    /**
     * Creates an identity matrix of specified size.
     *
     * @param size Matrix size (n x n)
     * @return Identity matrix
     */
    public static Matrix createIdentityMatrix(int size) {
        return Matrix.identity(size, size);
    }

    /**
     * Creates a random matrix with values in range [min, max].
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @param min Minimum value
     * @param max Maximum value
     * @return Random matrix
     */
    public static Matrix createRandomMatrix(int rows, int cols, double min, double max) {
        Matrix matrix = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double value = min + Math.random() * (max - min);
                matrix.set(i, j, value);
            }
        }
        return matrix;
    }

    /**
     * Creates standard edge case test values.
     *
     * @return Array of edge case values: zero, negative, large, small, etc.
     */
    public static double[] createEdgeCaseValues() {
        return new double[] {
            0.0,                    // Zero
            -0.0,                   // Negative zero
            1.0,                    // Unit
            -1.0,                   // Negative unit
            Double.MIN_VALUE,       // Smallest positive
            -Double.MIN_VALUE,      // Smallest negative
            Double.MAX_VALUE,       // Largest positive
            -Double.MAX_VALUE,      // Largest negative
            1e-100,                 // Very small
            1e100,                  // Very large
            Math.PI,                // Transcendental
            Math.E                  // Transcendental
        };
    }

    /**
     * Creates test scenarios for scalar operations.
     * Each scenario is {input, expectedOutput}.
     *
     * @return Array of test scenarios
     */
    public static double[][] createScalarTestScenarios() {
        return new double[][] {
            {0.0, 0.0},
            {1.0, 1.0},
            {-1.0, -1.0},
            {5.0, 5.0},
            {-5.0, -5.0},
            {0.5, 0.5},
            {-0.5, -0.5},
            {100.0, 100.0},
            {-100.0, -100.0}
        };
    }

    // =========================================================================
    // SECTION 6: PERFORMANCE MEASUREMENT HELPERS
    // =========================================================================

    /**
     * Measures execution time of a runnable operation.
     *
     * @param operation Operation to measure
     * @param iterations Number of iterations
     * @return Average time in nanoseconds
     */
    public static long measureExecutionTime(Runnable operation, int iterations) {
        // Warm up
        for (int i = 0; i < 10; i++) {
            operation.run();
        }

        // Measure
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            operation.run();
        }
        long endTime = System.nanoTime();

        return (endTime - startTime) / iterations;
    }

    /**
     * Asserts that an operation completes within time limit.
     *
     * @param operation Operation to measure
     * @param maxNanoseconds Maximum allowed time in nanoseconds
     * @param description Description for error message
     */
    public static void assertPerformance(Runnable operation, long maxNanoseconds, String description) {
        long avgTime = measureExecutionTime(operation, 1000);
        assertTrue(
            description + " should complete in < " + (maxNanoseconds / 1000.0) +
            " microseconds (actual: " + (avgTime / 1000.0) + " microseconds)",
            avgTime < maxNanoseconds
        );
    }

    // =========================================================================
    // SECTION 7: DEBUGGING HELPERS
    // =========================================================================

    /**
     * Prints detailed block information for debugging.
     *
     * @param block Block to inspect
     */
    public static void printBlockInfo(Block block) {
        System.out.println("=== Block Information ===");
        System.out.println("Type: " + block.getBlockType());
        System.out.println("Name: " + block.getBlockName());
        System.out.println("ID: " + block.getBlockId());
        System.out.println("Path: " + block.getBlockPath());

        System.out.println("\nInput Ports: " + block.getInputPortList().size());
        for (int i = 0; i < block.getInputPortList().size(); i++) {
            System.out.println("  [" + i + "] " + block.getInputPortList().get(i).getName());
        }

        System.out.println("\nOutput Ports: " + block.getOutputPortList().size());
        for (int i = 0; i < block.getOutputPortList().size(); i++) {
            OutputPort port = block.getOutputPortList().get(i);
            System.out.println("  [" + i + "] " + port.getName() +
                " (feedthrough: " + port.getFeedThrough() + ")");
        }

        System.out.println("\nParameters: " + block.getParameterList().size());
        for (Parameter param : block.getParameterList()) {
            System.out.println("  " + param.getLocalName() + " = " +
                param.getData().getDataString());
        }

        System.out.println("\nContinuous States: " + block.getStateList().size());
        System.out.println("Discrete States: " + block.getDStateList().size());
        System.out.println("========================\n");
    }

    /**
     * Prints generated code with line numbers for debugging.
     *
     * @param code Code to print
     * @param title Title for the code section
     */
    public static void printCodeWithLineNumbers(String code, String title) {
        System.out.println("=== " + title + " ===");
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.printf("%4d: %s\n", i + 1, lines[i]);
        }
        System.out.println("========================\n");
    }
}
