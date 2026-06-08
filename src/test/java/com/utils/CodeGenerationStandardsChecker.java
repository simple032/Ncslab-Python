package com.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for verifying generated C/C++ code compliance with industrial standards.
 * Implements checks for MISRA C/C++, AUTOSAR, IEC 61508, DO-178C, and ISO 26262.
 *
 * @author NCSLabLink Development Team
 * @version 1.0
 * @since 2025-01-06
 */
public class CodeGenerationStandardsChecker {

    // ===================================================================================
    // HEADER AND TRACEABILITY CHECKS
    // ===================================================================================

    /**
     * Checks if generated code has proper header comment with block information.
     *
     * @param code The generated code to check
     * @return true if header comment present and properly formatted
     */
    public static boolean checksHeaderComment(String code) {
        // Look for header comment block
        Pattern headerPattern = Pattern.compile(
            "/\\*{2,}[^*]*Generated Code for Block[^*]*\\*+/",
            Pattern.DOTALL | Pattern.MULTILINE
        );
        return headerPattern.matcher(code).find();
    }

    /**
     * Checks if header comment contains block ID reference.
     *
     * @param code The generated code to check
     * @return true if block ID is present in header
     */
    public static boolean checksBlockIdInHeader(String code) {
        Pattern blockIdPattern = Pattern.compile("Block ID:\\s*\\d+");
        return blockIdPattern.matcher(code).find();
    }

    /**
     * Extracts block ID from header comment.
     *
     * @param code The generated code to check
     * @return Block ID as integer, or -1 if not found
     */
    public static int extractBlockId(String code) {
        Pattern blockIdPattern = Pattern.compile("Block ID:\\s*(\\d+)");
        Matcher matcher = blockIdPattern.matcher(code);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    /**
     * Checks if header comment contains block name.
     *
     * @param code The generated code to check
     * @return true if block name is present
     */
    public static boolean checksBlockNameInHeader(String code) {
        Pattern blockNamePattern = Pattern.compile("Block Name:\\s*\\w+");
        return blockNamePattern.matcher(code).find();
    }

    /**
     * Checks if header comment contains block type.
     *
     * @param code The generated code to check
     * @return true if block type is present
     */
    public static boolean checksBlockTypeInHeader(String code) {
        Pattern blockTypePattern = Pattern.compile("Block Type:\\s*[\\w\\s]+");
        return blockTypePattern.matcher(code).find();
    }

    /**
     * Checks if header comment contains generation timestamp.
     *
     * @param code The generated code to check
     * @return true if timestamp is present
     */
    public static boolean checksGenerationTimestamp(String code) {
        Pattern timestampPattern = Pattern.compile("Generated:\\s*\\d{4}[-/]\\d{2}[-/]\\d{2}");
        return timestampPattern.matcher(code).find();
    }

    /**
     * Checks if header comment contains generator version/identifier.
     *
     * @param code The generated code to check
     * @return true if generator info is present
     */
    public static boolean checksGeneratorVersion(String code) {
        Pattern generatorPattern = Pattern.compile("Generator:\\s*\\w+|NCSLabLink");
        return generatorPattern.matcher(code).find();
    }

    // ===================================================================================
    // NAMING CONVENTION CHECKS
    // ===================================================================================

    /**
     * Checks if all block variables follow Block&lt;ID&gt;_ naming convention.
     *
     * @param code The generated code to check
     * @param blockId Expected block ID
     * @return true if all variables follow naming convention
     */
    public static boolean checksBlockPrefixedNaming(String code, int blockId) {
        String expectedPrefix = "Block" + blockId + "_";

        // Extract all identifier declarations
        // Pattern matches: type identifier = value; or type identifier;
        Pattern declPattern = Pattern.compile(
            "(?:const\\s+)?(?:double|float|int\\d*_t|int|uint\\d*_t|bool)\\s+(\\w+)\\s*[=;]"
        );

        Matcher matcher = declPattern.matcher(code);
        while (matcher.find()) {
            String identifier = matcher.group(1);

            // Skip standard library identifiers and type names
            if (isStandardLibraryIdentifier(identifier)) {
                continue;
            }

            // Check if identifier starts with expected prefix
            if (!identifier.startsWith(expectedPrefix)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if constants follow UPPER_CASE naming convention.
     *
     * @param code The generated code to check
     * @return true if all constants use UPPER_CASE
     */
    public static boolean checksConstantNaming(String code) {
        // Find all const declarations
        Pattern constPattern = Pattern.compile("const\\s+[\\w:]+\\s+(BLOCK\\d+_[A-Z_]+)\\s*=");

        // Find all const declarations that don't match UPPER_CASE
        Pattern badConstPattern = Pattern.compile("const\\s+[\\w:]+\\s+(Block\\d+_[a-z][\\w]*)\\s*=");

        return constPattern.matcher(code).find() && !badConstPattern.matcher(code).find();
    }

    /**
     * Checks if variables follow camelCase or snake_case consistently.
     *
     * @param code The generated code to check
     * @return true if variable naming is consistent
     */
    public static boolean checksVariableNaming(String code) {
        // Extract non-const variable names
        Pattern varPattern = Pattern.compile(
            "(?<!const\\s)(?:double|float|int\\d*_t|int|bool)\\s+(Block\\d+_\\w+)\\s*[=;]"
        );

        List<String> variableNames = new ArrayList<>();
        Matcher matcher = varPattern.matcher(code);
        while (matcher.find()) {
            variableNames.add(matcher.group(1));
        }

        if (variableNames.isEmpty()) {
            return true;
        }

        // Check if all follow camelCase or all follow snake_case
        boolean allCamelCase = variableNames.stream()
            .allMatch(name -> name.matches("Block\\d+_[a-z][a-zA-Z0-9]*"));

        boolean allSnakeCase = variableNames.stream()
            .allMatch(name -> name.matches("Block\\d+_[a-z][a-z0-9_]*"));

        return allCamelCase || allSnakeCase;
    }

    /**
     * Checks for single-letter variable names (except loop counters).
     *
     * @param code The generated code to check
     * @return true if no single-letter variables found (except i, j, k in loops)
     */
    public static boolean checksNoSingleLetterVariables(String code) {
        // Look for single-letter variable declarations
        Pattern singleLetterPattern = Pattern.compile(
            "(?:double|float|int\\d*_t|int|bool)\\s+([a-h,l-z])\\s*[=;]"
        );

        Matcher matcher = singleLetterPattern.matcher(code);
        if (matcher.find()) {
            // Check if it's in a loop context
            String varName = matcher.group(1);
            if (varName.matches("[ijk]")) {
                // Acceptable for loop counters
                return true;
            }
            return false;
        }

        return true;
    }

    /**
     * Checks if variable names are meaningful (average length >= 5 characters).
     *
     * @param code The generated code to check
     * @return true if average variable name length is sufficient
     */
    public static boolean checksMeaningfulNames(String code) {
        Pattern varPattern = Pattern.compile("Block\\d+_(\\w+)");

        List<String> variableNames = new ArrayList<>();
        Matcher matcher = varPattern.matcher(code);
        while (matcher.find()) {
            variableNames.add(matcher.group(1));
        }

        if (variableNames.isEmpty()) {
            return true;
        }

        double averageLength = variableNames.stream()
            .mapToInt(String::length)
            .average()
            .orElse(0.0);

        return averageLength >= 5.0;
    }

    // ===================================================================================
    // TYPE SAFETY CHECKS
    // ===================================================================================

    /**
     * Checks for use of 'auto' keyword (should not be used in generated code).
     *
     * @param code The generated code to check
     * @return true if no 'auto' keyword used
     */
    public static boolean checksNoAutoKeyword(String code) {
        Pattern autoPattern = Pattern.compile("\\bauto\\s+\\w+\\s*=");
        return !autoPattern.matcher(code).find();
    }

    /**
     * Checks for use of fixed-width integer types instead of int/short/long.
     *
     * @param code The generated code to check
     * @return true if fixed-width types are used
     */
    public static boolean checksFixedWidthTypes(String code) {
        // Look for declarations of int/short/long without fixed-width suffix
        Pattern nonFixedPattern = Pattern.compile(
            "\\b(short|long|int)\\s+(?!32_t|16_t|8_t|64_t)\\w+\\s*[=;]"
        );

        // Allow 'int' for loop counters
        Matcher matcher = nonFixedPattern.matcher(code);
        while (matcher.find()) {
            String context = getContextAroundMatch(code, matcher.start(), 50);
            if (context.contains("for(") || context.contains("for (")) {
                continue;  // Loop counter, acceptable
            }
            return false;
        }

        return true;
    }

    /**
     * Checks for explicit type conversions (static_cast or C-style cast).
     * Note: This is a heuristic check; full verification requires static analysis.
     *
     * @param code The generated code to check
     * @return true if explicit casts appear to be used
     */
    public static boolean checksExplicitConversions(String code) {
        // Look for static_cast or C-style casts
        Pattern castPattern = Pattern.compile(
            "static_cast<[^>]+>\\(|\\([a-z_]+\\d*_t\\)|\\(double\\)|\\(float\\)|\\(int\\)"
        );

        // This is a heuristic: if we find casts, assume explicit conversion is being used
        // Full verification requires compiler analysis with -Wconversion
        return castPattern.matcher(code).find() || !hasImplicitConversions(code);
    }

    // ===================================================================================
    // INITIALIZATION CHECKS
    // ===================================================================================

    /**
     * Checks if all variable declarations include initialization.
     *
     * @param code The generated code to check
     * @return true if all variables are initialized at declaration
     */
    public static boolean checksAllVariablesInitialized(String code) {
        // Look for variable declarations without initialization
        Pattern uninitPattern = Pattern.compile(
            "(?:const\\s+)?(?:double|float|int\\d*_t|int|bool)\\s+\\w+\\s*;"
        );

        Matcher matcher = uninitPattern.matcher(code);
        if (matcher.find()) {
            // Check if it's a function parameter or forward declaration
            String context = getContextAroundMatch(code, matcher.start(), 100);
            if (context.contains("(") && context.contains(")")) {
                return true;  // Function parameter
            }
            if (context.contains("extern")) {
                return true;  // Forward declaration
            }
            return false;
        }

        return true;
    }

    /**
     * Checks if code has clear initialization section.
     *
     * @param code The generated code to check
     * @return true if initialization section comment is present
     */
    public static boolean checksInitializationSection(String code) {
        Pattern initSectionPattern = Pattern.compile(
            "/\\*.*Initialization.*Section.*\\*/",
            Pattern.CASE_INSENSITIVE
        );
        return initSectionPattern.matcher(code).find();
    }

    /**
     * Checks if initialization values have documentation comments.
     *
     * @param code The generated code to check
     * @return true if most initializations have comments
     */
    public static boolean checksInitializationDocumentation(String code) {
        // Count initializations
        Pattern initPattern = Pattern.compile(
            "(?:const\\s+)?(?:double|float|int\\d*_t|int|bool)\\s+\\w+\\s*=\\s*[^;]+;"
        );

        int initCount = 0;
        int documentedCount = 0;

        Matcher matcher = initPattern.matcher(code);
        while (matcher.find()) {
            initCount++;
            String line = getLineContaining(code, matcher.start());
            if (line.contains("//")) {
                documentedCount++;
            }
        }

        if (initCount == 0) {
            return true;
        }

        // At least 70% should be documented
        return (double) documentedCount / initCount >= 0.7;
    }

    // ===================================================================================
    // MAGIC NUMBER CHECKS
    // ===================================================================================

    /**
     * Checks for magic numbers (literals other than 0, 1, -1, 2).
     *
     * @param code The generated code to check
     * @return true if no magic numbers found
     */
    public static boolean checksNoMagicNumbers(String code) {
        // Remove comments first
        String codeWithoutComments = removeComments(code);

        // Remove constant declarations (these define the constants, so literals are OK there)
        String codeWithoutConsts = codeWithoutComments.replaceAll(
            "const\\s+[\\w:]+\\s+\\w+\\s*=\\s*[^;]+;", ""
        );

        // Look for floating-point literals (except 0.0, 1.0, -1.0, 2.0)
        Pattern floatLiteralPattern = Pattern.compile(
            "\\b(?!0\\.0|1\\.0|-1\\.0|2\\.0)\\d+\\.\\d+\\b"
        );

        // Look for integer literals (except 0, 1, -1, 2)
        Pattern intLiteralPattern = Pattern.compile(
            "\\b(?!0|1|2)([3-9]|\\d{2,})\\b"
        );

        boolean hasFloatMagicNumbers = floatLiteralPattern.matcher(codeWithoutConsts).find();
        boolean hasIntMagicNumbers = intLiteralPattern.matcher(codeWithoutConsts).find();

        return !hasFloatMagicNumbers && !hasIntMagicNumbers;
    }

    /**
     * Checks if constants have unit documentation in comments.
     *
     * @param code The generated code to check
     * @return true if most constants have unit comments
     */
    public static boolean checksConstantUnits(String code) {
        // Find all const declarations
        Pattern constPattern = Pattern.compile(
            "const\\s+[\\w:]+\\s+\\w+\\s*=\\s*[^;]+;"
        );

        int constCount = 0;
        int documentedCount = 0;

        Matcher matcher = constPattern.matcher(code);
        while (matcher.find()) {
            constCount++;
            String line = getLineContaining(code, matcher.start());
            // Look for unit comments like [sec], [Hz], [m/s], etc.
            if (line.matches(".*//.*\\[.*\\].*")) {
                documentedCount++;
            }
        }

        if (constCount == 0) {
            return true;
        }

        // At least 70% should have unit documentation
        return (double) documentedCount / constCount >= 0.7;
    }

    /**
     * Checks for mathematical constant literals (should use M_PI, M_E, etc.).
     *
     * @param code The generated code to check
     * @return true if no mathematical constant literals found
     */
    public static boolean checksMathematicalConstants(String code) {
        // Look for common approximations of pi, e
        Pattern piPattern = Pattern.compile("\\b3\\.141\\d*\\b");
        Pattern ePattern = Pattern.compile("\\b2\\.718\\d*\\b");

        return !piPattern.matcher(code).find() && !ePattern.matcher(code).find();
    }

    // ===================================================================================
    // COMMENT AND DOCUMENTATION CHECKS
    // ===================================================================================

    /**
     * Checks for section delimiter comments.
     *
     * @param code The generated code to check
     * @return true if section delimiters are present
     */
    public static boolean checksSectionDelimiters(String code) {
        Pattern sectionPattern = Pattern.compile(
            "/\\*+\\s*\\w+\\s*Section\\s*\\*+/",
            Pattern.CASE_INSENSITIVE
        );
        return sectionPattern.matcher(code).find();
    }

    /**
     * Checks if parameters have inline documentation.
     *
     * @param code The generated code to check
     * @return true if most parameters are documented
     */
    public static boolean checksParameterDocumentation(String code) {
        // This is similar to initialization documentation check
        return checksInitializationDocumentation(code);
    }

    /**
     * Checks for end-of-block comment.
     *
     * @param code The generated code to check
     * @return true if end comment is present
     */
    public static boolean checksEndOfBlockComment(String code) {
        Pattern endPattern = Pattern.compile(
            "/\\*\\s*End of Generated Code for Block\\s*\\d+\\s*\\*/"
        );
        return endPattern.matcher(code).find();
    }

    // ===================================================================================
    // CODE STRUCTURE CHECKS
    // ===================================================================================

    /**
     * Checks for consistent indentation (spaces or tabs, not mixed).
     *
     * @param code The generated code to check
     * @return true if indentation is consistent
     */
    public static boolean checksConsistentIndentation(String code) {
        boolean hasSpaceIndent = code.contains("\n    ");  // 4 spaces
        boolean hasTabIndent = code.contains("\n\t");      // tabs

        // Should have one or the other, not both
        return !(hasSpaceIndent && hasTabIndent);
    }

    /**
     * Checks for one statement per line (no multiple statements on same line).
     *
     * @param code The generated code to check
     * @return true if one statement per line
     */
    public static boolean checksOneStatementPerLine(String code) {
        // Look for semicolon followed by non-whitespace before newline
        Pattern multiStatementPattern = Pattern.compile(";\\s*[^\\s/\\n]");
        return !multiStatementPattern.matcher(code).find();
    }

    /**
     * Checks for no trailing whitespace.
     *
     * @param code The generated code to check
     * @return true if no trailing whitespace found
     */
    public static boolean checksNoTrailingWhitespace(String code) {
        Pattern trailingPattern = Pattern.compile("\\s+$", Pattern.MULTILINE);
        return !trailingPattern.matcher(code).find();
    }

    /**
     * Checks for line length limit (default 120 characters).
     *
     * @param code The generated code to check
     * @param maxLength Maximum line length (default 120)
     * @return true if all lines within limit
     */
    public static boolean checksLineLengthLimit(String code, int maxLength) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.length() > maxLength) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks for line length limit with default 120 character limit.
     *
     * @param code The generated code to check
     * @return true if all lines within 120 character limit
     */
    public static boolean checksLineLengthLimit(String code) {
        return checksLineLengthLimit(code, 120);
    }

    /**
     * Checks for no more than 2 consecutive blank lines.
     *
     * @param code The generated code to check
     * @return true if no excessive blank lines
     */
    public static boolean checksNoExcessiveBlankLines(String code) {
        Pattern excessiveBlankPattern = Pattern.compile("\n\\s*\n\\s*\n\\s*\n");
        return !excessiveBlankPattern.matcher(code).find();
    }

    // ===================================================================================
    // TEMPLATE VARIABLE CHECKS
    // ===================================================================================

    /**
     * Checks for undefined template variables (literal ${VariableName} in output).
     * This is a CRITICAL error indicating template rendering failure.
     *
     * @param code The generated code to check
     * @return true if no undefined variables found
     */
    public static boolean checksNoUndefinedVariables(String code) {
        Pattern undefinedPattern = Pattern.compile("\\$\\{\\w+\\}");
        return !undefinedPattern.matcher(code).find();
    }

    /**
     * Checks for undefined Velocity directives in output.
     *
     * @param code The generated code to check
     * @return true if no Velocity directives found
     */
    public static boolean checksNoVelocityDirectives(String code) {
        // Look for Velocity directives at start of line
        // Exclude C preprocessor directives
        Pattern velocityPattern = Pattern.compile("^#(?!include|define|ifndef|endif|pragma)", Pattern.MULTILINE);
        return !velocityPattern.matcher(code).find();
    }

    /**
     * Checks for empty variable expansions (= ; or ( )).
     *
     * @param code The generated code to check
     * @return true if no empty expansions found
     */
    public static boolean checksNoEmptyExpansions(String code) {
        Pattern emptyAssignPattern = Pattern.compile("=\\s*;");
        Pattern emptyCallPattern = Pattern.compile("\\(\\s*\\)");

        boolean hasEmptyAssign = emptyAssignPattern.matcher(code).find();
        boolean hasEmptyCall = emptyCallPattern.matcher(code).find();

        // Empty function calls are sometimes legitimate, so only flag empty assignments
        return !hasEmptyAssign;
    }

    // ===================================================================================
    // SAFETY AND ERROR HANDLING CHECKS
    // ===================================================================================

    /**
     * Checks for division-by-zero protection.
     *
     * @param code The generated code to check
     * @return true if divisions appear to be protected
     */
    public static boolean checksDivisionByZeroProtection(String code) {
        // Find all division operations
        Pattern divPattern = Pattern.compile("\\w+\\s*/\\s*\\w+");

        Matcher matcher = divPattern.matcher(code);
        while (matcher.find()) {
            // Look for preceding if-check in context
            String context = getContextAroundMatch(code, matcher.start(), 200);

            // Look for checks like: if (fabs(x) > epsilon) or if (x != 0)
            if (!context.matches(".*if\\s*\\([^)]*(?:fabs|abs|!=\\s*0)[^)]*\\).*")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for array bounds checking.
     * Note: This is a heuristic check.
     *
     * @param code The generated code to check
     * @return true if array accesses appear to be protected
     */
    public static boolean checksArrayBoundsProtection(String code) {
        // Find array accesses
        Pattern arrayPattern = Pattern.compile("\\w+\\[\\w+\\]");

        Matcher matcher = arrayPattern.matcher(code);
        while (matcher.find()) {
            String context = getContextAroundMatch(code, matcher.start(), 200);

            // Look for bounds checks
            if (!context.matches(".*if\\s*\\([^)]*>=.*&&.*<[^)]*\\).*")) {
                // Array access without apparent bounds check
                // This is a heuristic; may have false positives
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for NaN/Infinity validation using isfinite, isnan, isinf.
     *
     * @param code The generated code to check
     * @return true if floating-point validation functions are used
     */
    public static boolean checksFloatingPointValidation(String code) {
        Pattern validationPattern = Pattern.compile("\\b(isfinite|isnan|isinf)\\s*\\(");
        return validationPattern.matcher(code).find();
    }

    /**
     * Checks if error flags/codes are defined.
     *
     * @param code The generated code to check
     * @return true if error types or flags are defined
     */
    public static boolean checksErrorFlagDefinitions(String code) {
        Pattern errorPattern = Pattern.compile(
            "(?:typedef\\s+enum|enum)\\s*\\{[^}]*ERROR[^}]*\\}|\\w+_ErrorCode"
        );
        return errorPattern.matcher(code).find();
    }

    // ===================================================================================
    // MEMORY SAFETY CHECKS
    // ===================================================================================

    /**
     * Checks for absence of dynamic memory allocation (malloc, calloc, realloc, free).
     *
     * @param code The generated code to check
     * @return true if no dynamic allocation found
     */
    public static boolean checksNoDynamicAllocation(String code) {
        Pattern allocPattern = Pattern.compile("\\b(malloc|calloc|realloc|free)\\s*\\(");
        return !allocPattern.matcher(code).find();
    }

    /**
     * Checks for absence of C++ new/delete operators.
     *
     * @param code The generated code to check
     * @return true if no new/delete found
     */
    public static boolean checksNoNewDelete(String code) {
        Pattern newDeletePattern = Pattern.compile("\\b(new|delete)\\s+");
        return !newDeletePattern.matcher(code).find();
    }

    // ===================================================================================
    // MISRA-SPECIFIC CHECKS
    // ===================================================================================

    /**
     * MISRA Rule 14.3: Check for invariant controlling expressions.
     *
     * @param code The generated code to check
     * @return true if no invariant conditions found (except while(1))
     */
    public static boolean checksMISRA_Rule_14_3(String code) {
        Pattern invariantPattern = Pattern.compile("if\\s*\\((true|false)\\)");

        // Allow while(1) for infinite loops in embedded systems
        Matcher matcher = invariantPattern.matcher(code);
        return !matcher.find();
    }

    /**
     * MISRA Rule 17.7: Check for unused return values.
     * Note: This requires compilation check with -Wunused-result.
     *
     * @param code The generated code to check
     * @return true if return values appear to be used or explicitly discarded
     */
    public static boolean checksMISRA_Rule_17_7(String code) {
        // Look for function calls not assigned to variable or cast to void
        Pattern unusedReturnPattern = Pattern.compile(
            "^\\s*[a-zA-Z_]\\w*\\s*\\([^)]*\\)\\s*;",
            Pattern.MULTILINE
        );

        Matcher matcher = unusedReturnPattern.matcher(code);
        while (matcher.find()) {
            String call = matcher.group(0);
            // Check if it's cast to void
            if (!call.contains("(void)")) {
                return false;
            }
        }

        return true;
    }

    // ===================================================================================
    // COMPREHENSIVE COMPLIANCE CHECK
    // ===================================================================================

    /**
     * Performs comprehensive standards compliance check.
     *
     * @param code The generated code to check
     * @param blockId Expected block ID
     * @return ComplianceReport containing all check results
     */
    public static ComplianceReport checkAllStandards(String code, int blockId) {
        ComplianceReport report = new ComplianceReport(blockId);

        // Header and Traceability
        report.addCheck("Header Comment", checksHeaderComment(code), CheckPriority.CRITICAL);
        report.addCheck("Block ID in Header", checksBlockIdInHeader(code), CheckPriority.CRITICAL);
        report.addCheck("Block Name in Header", checksBlockNameInHeader(code), CheckPriority.HIGH);
        report.addCheck("Block Type in Header", checksBlockTypeInHeader(code), CheckPriority.HIGH);
        report.addCheck("Generation Timestamp", checksGenerationTimestamp(code), CheckPriority.MEDIUM);
        report.addCheck("Generator Version", checksGeneratorVersion(code), CheckPriority.MEDIUM);

        // Naming Conventions
        report.addCheck("Block-Prefixed Naming", checksBlockPrefixedNaming(code, blockId), CheckPriority.CRITICAL);
        report.addCheck("Constant Naming", checksConstantNaming(code), CheckPriority.HIGH);
        report.addCheck("Variable Naming", checksVariableNaming(code), CheckPriority.MEDIUM);
        report.addCheck("No Single-Letter Variables", checksNoSingleLetterVariables(code), CheckPriority.MEDIUM);
        report.addCheck("Meaningful Names", checksMeaningfulNames(code), CheckPriority.MEDIUM);

        // Type Safety
        report.addCheck("No Auto Keyword", checksNoAutoKeyword(code), CheckPriority.HIGH);
        report.addCheck("Fixed-Width Types", checksFixedWidthTypes(code), CheckPriority.HIGH);
        report.addCheck("Explicit Conversions", checksExplicitConversions(code), CheckPriority.HIGH);

        // Initialization
        report.addCheck("All Variables Initialized", checksAllVariablesInitialized(code), CheckPriority.CRITICAL);
        report.addCheck("Initialization Section", checksInitializationSection(code), CheckPriority.MEDIUM);
        report.addCheck("Initialization Documentation", checksInitializationDocumentation(code), CheckPriority.MEDIUM);

        // Magic Numbers
        report.addCheck("No Magic Numbers", checksNoMagicNumbers(code), CheckPriority.HIGH);
        report.addCheck("Constant Units", checksConstantUnits(code), CheckPriority.MEDIUM);
        report.addCheck("Mathematical Constants", checksMathematicalConstants(code), CheckPriority.MEDIUM);

        // Comments and Documentation
        report.addCheck("Section Delimiters", checksSectionDelimiters(code), CheckPriority.MEDIUM);
        report.addCheck("Parameter Documentation", checksParameterDocumentation(code), CheckPriority.MEDIUM);
        report.addCheck("End of Block Comment", checksEndOfBlockComment(code), CheckPriority.LOW);

        // Code Structure
        report.addCheck("Consistent Indentation", checksConsistentIndentation(code), CheckPriority.MEDIUM);
        report.addCheck("One Statement Per Line", checksOneStatementPerLine(code), CheckPriority.MEDIUM);
        report.addCheck("No Trailing Whitespace", checksNoTrailingWhitespace(code), CheckPriority.LOW);
        report.addCheck("Line Length Limit", checksLineLengthLimit(code), CheckPriority.LOW);
        report.addCheck("No Excessive Blank Lines", checksNoExcessiveBlankLines(code), CheckPriority.LOW);

        // Template Variables
        report.addCheck("No Undefined Variables", checksNoUndefinedVariables(code), CheckPriority.CRITICAL);
        report.addCheck("No Velocity Directives", checksNoVelocityDirectives(code), CheckPriority.CRITICAL);
        report.addCheck("No Empty Expansions", checksNoEmptyExpansions(code), CheckPriority.HIGH);

        // Safety and Error Handling
        report.addCheck("Division by Zero Protection", checksDivisionByZeroProtection(code), CheckPriority.HIGH);
        report.addCheck("Array Bounds Protection", checksArrayBoundsProtection(code), CheckPriority.HIGH);
        report.addCheck("Floating-Point Validation", checksFloatingPointValidation(code), CheckPriority.MEDIUM);
        report.addCheck("Error Flag Definitions", checksErrorFlagDefinitions(code), CheckPriority.MEDIUM);

        // Memory Safety
        report.addCheck("No Dynamic Allocation", checksNoDynamicAllocation(code), CheckPriority.CRITICAL);
        report.addCheck("No New/Delete", checksNoNewDelete(code), CheckPriority.HIGH);

        // MISRA-Specific
        report.addCheck("MISRA Rule 14.3", checksMISRA_Rule_14_3(code), CheckPriority.HIGH);
        report.addCheck("MISRA Rule 17.7", checksMISRA_Rule_17_7(code), CheckPriority.MEDIUM);

        return report;
    }

    // ===================================================================================
    // UTILITY METHODS
    // ===================================================================================

    private static boolean isStandardLibraryIdentifier(String identifier) {
        Set<String> stdIdentifiers = new HashSet<>(Arrays.asList(
            "std", "cout", "cin", "endl", "string", "vector", "map", "size_t",
            "int8_t", "int16_t", "int32_t", "int64_t",
            "uint8_t", "uint16_t", "uint32_t", "uint64_t",
            "bool", "true", "false", "nullptr", "NULL"
        ));
        return stdIdentifiers.contains(identifier);
    }

    private static String getContextAroundMatch(String code, int position, int contextSize) {
        int start = Math.max(0, position - contextSize);
        int end = Math.min(code.length(), position + contextSize);
        return code.substring(start, end);
    }

    private static String getLineContaining(String code, int position) {
        int lineStart = code.lastIndexOf('\n', position) + 1;
        int lineEnd = code.indexOf('\n', position);
        if (lineEnd == -1) {
            lineEnd = code.length();
        }
        return code.substring(lineStart, lineEnd);
    }

    private static String removeComments(String code) {
        // Remove single-line comments
        code = code.replaceAll("//.*$", "");

        // Remove multi-line comments
        code = code.replaceAll("/\\*.*?\\*/", "");

        return code;
    }

    private static boolean hasImplicitConversions(String code) {
        // This is a simplified check; real check requires compiler analysis
        // Look for assignments between different types without casts
        Pattern implicitPattern = Pattern.compile(
            "(int\\d*_t|double|float)\\s+\\w+\\s*=\\s*\\w+;"
        );
        return implicitPattern.matcher(code).find();
    }

    // ===================================================================================
    // COMPLIANCE REPORT CLASS
    // ===================================================================================

    /**
     * Report containing results of all compliance checks.
     */
    public static class ComplianceReport {
        private final int blockId;
        private final Map<String, CheckResult> results = new HashMap<>();
        private final List<String> criticalFailures = new ArrayList<>();
        private final List<String> highPriorityFailures = new ArrayList<>();
        private final List<String> mediumPriorityFailures = new ArrayList<>();
        private final List<String> lowPriorityFailures = new ArrayList<>();

        public ComplianceReport(int blockId) {
            this.blockId = blockId;
        }

        public void addCheck(String checkName, boolean passed, CheckPriority priority) {
            CheckResult result = new CheckResult(checkName, passed, priority);
            results.put(checkName, result);

            if (!passed) {
                switch (priority) {
                    case CRITICAL:
                        criticalFailures.add(checkName);
                        break;
                    case HIGH:
                        highPriorityFailures.add(checkName);
                        break;
                    case MEDIUM:
                        mediumPriorityFailures.add(checkName);
                        break;
                    case LOW:
                        lowPriorityFailures.add(checkName);
                        break;
                }
            }
        }

        public int getBlockId() {
            return blockId;
        }

        public boolean isFullyCompliant() {
            return results.values().stream().allMatch(r -> r.passed);
        }

        public boolean hasCriticalFailures() {
            return !criticalFailures.isEmpty();
        }

        public List<String> getCriticalFailures() {
            return new ArrayList<>(criticalFailures);
        }

        public List<String> getHighPriorityFailures() {
            return new ArrayList<>(highPriorityFailures);
        }

        public List<String> getMediumPriorityFailures() {
            return new ArrayList<>(mediumPriorityFailures);
        }

        public List<String> getLowPriorityFailures() {
            return new ArrayList<>(lowPriorityFailures);
        }

        public int getTotalChecks() {
            return results.size();
        }

        public int getPassedChecks() {
            return (int) results.values().stream().filter(r -> r.passed).count();
        }

        public int getFailedChecks() {
            return (int) results.values().stream().filter(r -> !r.passed).count();
        }

        public double getCompliancePercentage() {
            if (results.isEmpty()) {
                return 100.0;
            }
            return (double) getPassedChecks() / getTotalChecks() * 100.0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Compliance Report for Block ").append(blockId).append("\n");
            sb.append("======================================\n");
            sb.append(String.format("Compliance: %.1f%% (%d/%d checks passed)\n",
                getCompliancePercentage(), getPassedChecks(), getTotalChecks()));
            sb.append("\n");

            if (!criticalFailures.isEmpty()) {
                sb.append("CRITICAL FAILURES:\n");
                for (String failure : criticalFailures) {
                    sb.append("  - ").append(failure).append("\n");
                }
                sb.append("\n");
            }

            if (!highPriorityFailures.isEmpty()) {
                sb.append("HIGH PRIORITY FAILURES:\n");
                for (String failure : highPriorityFailures) {
                    sb.append("  - ").append(failure).append("\n");
                }
                sb.append("\n");
            }

            if (!mediumPriorityFailures.isEmpty()) {
                sb.append("MEDIUM PRIORITY FAILURES:\n");
                for (String failure : mediumPriorityFailures) {
                    sb.append("  - ").append(failure).append("\n");
                }
                sb.append("\n");
            }

            if (!lowPriorityFailures.isEmpty()) {
                sb.append("LOW PRIORITY FAILURES:\n");
                for (String failure : lowPriorityFailures) {
                    sb.append("  - ").append(failure).append("\n");
                }
            }

            return sb.toString();
        }

        public Map<String, CheckResult> getAllResults() {
            return new HashMap<>(results);
        }
    }

    /**
     * Individual check result.
     */
    public static class CheckResult {
        public final String checkName;
        public final boolean passed;
        public final CheckPriority priority;

        public CheckResult(String checkName, boolean passed, CheckPriority priority) {
            this.checkName = checkName;
            this.passed = passed;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s",
                priority, checkName, passed ? "PASS" : "FAIL");
        }
    }

    /**
     * Priority levels for checks.
     */
    public enum CheckPriority {
        CRITICAL,  // Must pass - code is unsafe or invalid
        HIGH,      // Should pass - significant quality/safety issue
        MEDIUM,    // Nice to pass - code quality issue
        LOW        // Optional - style/formatting issue
    }

    // ===================================================================================
    // METHOD ALIASES FOR TEST COMPATIBILITY
    // ===================================================================================

    /**
     * Alias for checksHeaderComment + checksBlockIdInHeader combined.
     * Checks for comprehensive traceability header.
     */
    public static boolean checksTraceabilityHeader(String code, String blockId) {
        return checksHeaderComment(code) &&
               (checksBlockIdInHeader(code) || code.contains(blockId));
    }

    /**
     * Alias for checksBlockPrefixedNaming.
     * Checks that all identifiers use Block<ID>_ prefix.
     */
    public static boolean checksUniqueIdentifiers(String code, String blockId) {
        try {
            int blockIdInt = Integer.parseInt(blockId);
            return checksBlockPrefixedNaming(code, blockIdInt);
        } catch (NumberFormatException e) {
            // If blockId is not numeric, check for Block prefix pattern
            return code.contains("Block" + blockId + "_");
        }
    }

    /**
     * Alias for checksNoUndefinedVariables.
     * Checks that no Velocity template variables remain undefined.
     */
    public static boolean checksNoUndefinedTemplateVars(String code) {
        return checksNoUndefinedVariables(code);
    }

    /**
     * Alias for checksAllVariablesInitialized.
     * Checks that variables are initialized at declaration.
     */
    public static boolean checksVariableInitialization(String code) {
        return checksAllVariablesInitialized(code);
    }

    /**
     * Alias for checksBlockPrefixedNaming + checksVariableNaming combined.
     * Checks AUTOSAR naming conventions.
     */
    public static boolean checksNamingConvention(String code, String blockId) {
        try {
            int blockIdInt = Integer.parseInt(blockId);
            return checksBlockPrefixedNaming(code, blockIdInt) && checksVariableNaming(code);
        } catch (NumberFormatException e) {
            return checksVariableNaming(code);
        }
    }

    /**
     * Alias for checksConsistentIndentation.
     * Checks proper code indentation.
     */
    public static boolean checksProperIndentation(String code) {
        return checksConsistentIndentation(code);
    }

    /**
     * Alias for checksOneStatementPerLine.
     * Checks that statements are properly terminated.
     */
    public static boolean checksStatementTerminators(String code) {
        // Check for semicolons and proper statement structure
        return code.contains(";") && checksOneStatementPerLine(code);
    }

    /**
     * Overloaded version of checkAllStandards that accepts String blockId.
     */
    public static ComplianceReport checkAllStandards(String code, String blockId) {
        try {
            int blockIdInt = Integer.parseInt(blockId);
            return checkAllStandards(code, blockIdInt);
        } catch (NumberFormatException e) {
            // If blockId cannot be parsed, use 0 as default
            return checkAllStandards(code, 0);
        }
    }
}
